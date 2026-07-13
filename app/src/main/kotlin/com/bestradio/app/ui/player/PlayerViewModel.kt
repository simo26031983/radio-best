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
        _uiState.value = _uiState.value.copy(currentStation = station)
        viewModelScope.launch { playbackStateStore.setLastStationId(station.id) }
    }

    fun togglePlayPause() {
        controller?.apply {
            if (isPlaying) pause() else play()
        }
    }

    private fun buildMediaItem(station: Station): MediaItem = MediaItem.Builder()
        .setMediaId(station.id)
        .setUri(station.streamUrl)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(station.name)
                .setArtist(station.genre.ifBlank { null })
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
