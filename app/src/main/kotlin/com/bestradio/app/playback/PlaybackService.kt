package com.bestradio.app.playback

import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

/** Hosts the single ExoPlayer instance + MediaSession for the whole app.
 * A future phase upgrades this to a full MediaLibraryService with a browse
 * tree for Android Auto; the session/player lifecycle established here is
 * shared by both the phone UI and, later, the car surface. */
class PlaybackService : MediaSessionService() {

    private var player: Player? = null
    private var mediaSession: MediaSession? = null

    @UnstableApi
    override fun onCreate() {
        super.onCreate()
        val exoPlayer = ExoPlayerFactory.create(this)
        player = exoPlayer
        mediaSession = MediaSession.Builder(this, exoPlayer).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        player = null
        super.onDestroy()
    }
}
