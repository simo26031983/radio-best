package com.bestradio.app.playback

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionError
import com.bestradio.app.BestRadioApplication
import com.bestradio.app.MainActivity
import com.bestradio.app.data.local.FavoritesStore
import com.bestradio.app.data.local.PlaybackStateStore
import com.bestradio.app.data.model.Country
import com.bestradio.app.data.model.Station
import com.bestradio.app.data.repository.StationsRepository
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.guava.future

/** Hosts the single ExoPlayer instance + MediaLibrarySession for the whole app.
 *
 * Both surfaces talk to this one session:
 *  - the phone UI (`PlayerViewModel`) connects as a [androidx.media3.session.MediaController]
 *    and drives playback directly by setting fully-populated MediaItems (URI included);
 *  - Android Auto connects as a [androidx.media3.session.MediaBrowser], walks the browse
 *    tree exposed by [MediaLibrarySession.Callback], and taps a browse leaf — which arrives
 *    back here as a metadata-only MediaItem (its URI is stripped in transit over the binder),
 *    so [LibrarySessionCallback.onAddMediaItems] re-resolves the real stream URL by media id.
 *
 * There is exactly one playback path; the car does not duplicate any playback logic. */
class PlaybackService : MediaLibraryService() {

    private var player: Player? = null
    private var mediaLibrarySession: MediaLibrarySession? = null

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private lateinit var stationsRepository: StationsRepository
    private lateinit var favoritesStore: FavoritesStore
    private lateinit var playbackStateStore: PlaybackStateStore

    @UnstableApi
    override fun onCreate() {
        super.onCreate()

        val container = (application as BestRadioApplication).container
        stationsRepository = container.stationsRepository
        favoritesStore = container.favoritesStore
        playbackStateStore = container.playbackStateStore

        val exoPlayer = ExoPlayerFactory.create(this)
        player = exoPlayer

        val sessionActivity = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )
        mediaLibrarySession = MediaLibrarySession.Builder(this, exoPlayer, LibrarySessionCallback())
            .setSessionActivity(sessionActivity)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        Log.d(TAG, "onGetSession: package=${controllerInfo.packageName} uid=${controllerInfo.uid}")
        return mediaLibrarySession
    }

    override fun onDestroy() {
        mediaLibrarySession?.run {
            player.release()
            release()
        }
        mediaLibrarySession = null
        player = null
        serviceScope.cancel()
        super.onDestroy()
    }

    @UnstableApi
    private inner class LibrarySessionCallback : MediaLibrarySession.Callback {

        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
        ): MediaSession.ConnectionResult {
            Log.d(TAG, "onConnect: package=${controller.packageName} uid=${controller.uid}")
            val result = super.onConnect(session, controller)
            Log.d(TAG, "onConnect result: accepted=${result.isAccepted} availableSessionCommands=${result.availableSessionCommands}")
            return result
        }

        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?,
        ): ListenableFuture<LibraryResult<MediaItem>> {
            Log.d(TAG, "onGetLibraryRoot: package=${browser.packageName} params=$params")
            // TEMP DIAGNOSTIC: bypass the coroutine entirely to isolate whether
            // serviceScope.future{} is what's failing to complete.
            val result = LibraryResult.ofItem(rootItem(), params)
            Log.d(TAG, "onGetLibraryRoot -> resultCode=${result.resultCode} (sync path)")
            return Futures.immediateFuture(result)
        }

        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?,
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            Log.d(TAG, "onGetChildren: parentId=$parentId page=$page pageSize=$pageSize package=${browser.packageName}")
            return serviceScope.future {
            val children: List<MediaItem> = when (parentId) {
                ROOT_ID -> listOf(
                    categoryItem(FAVORITES_ID, "Favoris"),
                    categoryItem(FRANCE_ID, "France"),
                    categoryItem(MOROCCO_ID, "Maroc"),
                )

                FAVORITES_ID -> {
                    val favoriteIds = favoritesStore.favoriteIds.first()
                    stationsRepository.getAllStations()
                        .filter { it.id in favoriteIds }
                        .map { stationItem(it) }
                }

                FRANCE_ID -> stationsRepository.getStations(Country.FRANCE).map { stationItem(it) }
                MOROCCO_ID -> stationsRepository.getStations(Country.MOROCCO).map { stationItem(it) }

                else -> return@future LibraryResult.ofError(SessionError.ERROR_BAD_VALUE)
            }
                Log.d(TAG, "onGetChildren($parentId) -> ${children.size} items")
                LibraryResult.ofItemList(children, params)
            }
        }

        override fun onGetItem(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            mediaId: String,
        ): ListenableFuture<LibraryResult<MediaItem>> = serviceScope.future {
            when (mediaId) {
                ROOT_ID -> LibraryResult.ofItem(rootItem(), null)
                FAVORITES_ID -> LibraryResult.ofItem(categoryItem(FAVORITES_ID, "Favoris"), null)
                FRANCE_ID -> LibraryResult.ofItem(categoryItem(FRANCE_ID, "France"), null)
                MOROCCO_ID -> LibraryResult.ofItem(categoryItem(MOROCCO_ID, "Maroc"), null)
                else -> {
                    val station = stationsRepository.getStationById(mediaId)
                    if (station != null) {
                        LibraryResult.ofItem(stationItem(station), null)
                    } else {
                        LibraryResult.ofError(SessionError.ERROR_BAD_VALUE)
                    }
                }
            }
        }

        /** Android Auto / a media button sends a MediaItem carrying only a media id (its stream
         * URI having been stripped in transit). Resolve each such item back to a fully-playable
         * MediaItem with the real stream URL so the player has something it can actually open.
         * Items that already carry a LocalConfiguration (e.g. from the phone UI, which sets the
         * URI directly) are passed through untouched. */
        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>,
        ): ListenableFuture<MutableList<MediaItem>> = serviceScope.future {
            mediaItems.map { item ->
                if (item.localConfiguration != null) {
                    item
                } else {
                    val station = stationsRepository.getStationById(item.mediaId)
                    if (station != null) stationItem(station) else item
                }
            }.toMutableList()
        }

        /** Backs Android Auto's "resume" affordance and Android's system media resumption:
         * rebuild the last-played station as a playable item and hand it back positioned at
         * the start (live radio has no meaningful resume offset). */
        override fun onPlaybackResumption(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
        ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> = serviceScope.future {
            val lastId = playbackStateStore.lastStationId.first()
            val station = lastId?.let { stationsRepository.getStationById(it) }
                ?: throw IllegalStateException("No resumable station")
            MediaSession.MediaItemsWithStartPosition(
                listOf(stationItem(station)),
                /* startIndex = */ 0,
                /* startPositionMs = */ C.TIME_UNSET,
            )
        }
    }

    private fun rootItem(): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle("Best Radio")
            .setIsBrowsable(true)
            .setIsPlayable(false)
            .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)
            .build()
        return MediaItem.Builder()
            .setMediaId(ROOT_ID)
            .setMediaMetadata(metadata)
            .build()
    }

    private fun categoryItem(id: String, title: String): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setIsBrowsable(true)
            .setIsPlayable(false)
            .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_RADIO_STATIONS)
            .build()
        return MediaItem.Builder()
            .setMediaId(id)
            .setMediaMetadata(metadata)
            .build()
    }

    private fun stationItem(station: Station): MediaItem {
        // Deliberately no subtitle/genre: the car list shows just the logo and station name.
        val metadata = MediaMetadata.Builder()
            .setTitle(station.name)
            .setArtworkUri(station.faviconUrl.takeIf { it.isNotBlank() }?.let { Uri.parse(it) })
            .setIsBrowsable(false)
            .setIsPlayable(true)
            .setMediaType(MediaMetadata.MEDIA_TYPE_RADIO_STATION)
            .build()
        return MediaItem.Builder()
            .setMediaId(station.id)
            .setUri(station.streamUrl)
            .setMediaMetadata(metadata)
            .build()
    }

    private companion object {
        const val TAG = "PlaybackService"
        const val ROOT_ID = "root"
        const val FAVORITES_ID = "favoris"
        const val FRANCE_ID = "france"
        const val MOROCCO_ID = "maroc"
    }
}
