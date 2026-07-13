package com.bestradio.app.ui.player

import android.app.Application
import android.content.ComponentName
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.bestradio.app.data.local.PlaybackStateStore
import com.bestradio.app.data.model.Station
import com.bestradio.app.data.repository.StationsRepository
import com.bestradio.app.playback.PlaybackService
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class PlayerUiState(
    val currentStation: Station? = null,
    val isPlaying: Boolean = false,
    /** Live ICY "now playing" text (artist/song) for the current stream, or
     * null when the stream carries no ICY metadata. Transient per-stream
     * state, deliberately not part of [Station]. */
    val nowPlayingText: String? = null,
)

class PlayerViewModel(
    application: Application,
    private val stationsRepository: StationsRepository,
    private val playbackStateStore: PlaybackStateStore,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var controller: MediaController? = null

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
        }

        /** PlaybackService folds the stream's ICY "now playing" text into the
         * current item's MediaMetadata.artist (that field is reserved for it
         * app-wide — nothing sets a static artist), and the session forwards
         * the merged metadata here. */
        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            _uiState.value = _uiState.value.copy(
                nowPlayingText = mediaMetadata.artist?.toString()?.takeIf { it.isNotBlank() },
            )
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            // A new stream started (or playback was cleared): drop the previous
            // stream's now-playing text immediately rather than showing stale
            // song info against the new station.
            _uiState.value = _uiState.value.copy(nowPlayingText = null)
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_IDLE) {
                _uiState.value = _uiState.value.copy(nowPlayingText = null)
            }
        }
    }

    init {
        val context = application.applicationContext
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        future.addListener(
            {
                controller = future.get().also { it.addListener(playerListener) }
                restoreLastStation()
            },
            MoreExecutors.directExecutor(),
        )
    }

    /** Loads the last-played station into the mini-player without auto-playing
     * it — the user taps play to resume, rather than audio starting
     * unexpectedly the moment the app opens. */
    private fun restoreLastStation() {
        viewModelScope.launch {
            val lastId = playbackStateStore.lastStationId.first() ?: return@launch
            val station = stationsRepository.getStationById(lastId) ?: return@launch
            controller?.apply {
                setMediaItem(buildMediaItem(station))
                prepare()
            }
            _uiState.value = _uiState.value.copy(currentStation = station, isPlaying = false)
        }
    }

    fun play(station: Station) {
        controller?.apply {
            setMediaItem(buildMediaItem(station))
            prepare()
            play()
        }
        _uiState.value = _uiState.value.copy(currentStation = station, nowPlayingText = null)
        viewModelScope.launch { playbackStateStore.setLastStationId(station.id) }
    }

    fun togglePlayPause() {
        controller?.apply {
            if (isPlaying) pause() else play()
        }
    }

    /** No static artist here: MediaMetadata.artist is reserved app-wide as the
     * channel for the live ICY "now playing" text (PlaybackService writes it,
     * this ViewModel reads it back via onMediaMetadataChanged). Genre was
     * dropped from the notification for the same reason the car list carries
     * no subtitle — logo + station name is enough. */
    private fun buildMediaItem(station: Station): MediaItem = MediaItem.Builder()
        .setMediaId(station.id)
        .setUri(station.streamUrl)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(station.name)
                .setArtworkUri(station.faviconUrl.takeIf { it.isNotBlank() }?.let { Uri.parse(it) })
                .build()
        )
        .build()

    override fun onCleared() {
        controller?.removeListener(playerListener)
        controller?.release()
        controller = null
        super.onCleared()
    }

    companion object {
        fun factory(application: Application, stationsRepository: StationsRepository, playbackStateStore: PlaybackStateStore) =
            viewModelFactory {
                initializer { PlayerViewModel(application, stationsRepository, playbackStateStore) }
            }
    }
}
