package com.bestradio.app.ui.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.bestradio.app.data.model.Station
import com.bestradio.app.playback.PlaybackService
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PlayerUiState(
    val currentStation: Station? = null,
    val isPlaying: Boolean = false,
)

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

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
        val sessionToken = SessionToken(context, android.content.ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        future.addListener(
            {
                controller = future.get().also { it.addListener(playerListener) }
            },
            MoreExecutors.directExecutor(),
        )
    }

    fun play(station: Station) {
        val mediaItem = MediaItem.Builder()
            .setMediaId(station.id)
            .setUri(station.streamUrl)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(station.name)
                    .setArtist(station.genre.ifBlank { null })
                    .setArtworkUri(station.faviconUrl.takeIf { it.isNotBlank() }?.let { android.net.Uri.parse(it) })
                    .build()
            )
            .build()

        controller?.apply {
            setMediaItem(mediaItem)
            prepare()
            play()
        }
        _uiState.value = _uiState.value.copy(currentStation = station)
    }

    fun togglePlayPause() {
        controller?.apply {
            if (isPlaying) pause() else play()
        }
    }

    override fun onCleared() {
        controller?.removeListener(playerListener)
        controller?.release()
        controller = null
        super.onCleared()
    }
}
