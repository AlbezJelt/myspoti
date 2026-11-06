package com.music.spotui.di

import app.myspoti.host.android.CarLyricLine
import app.myspoti.host.android.PlaybackService
import com.music.spotui.data.api.LyricsApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Hands the playing song's synced lyrics to the playback service, which shows them line by line in
 * Android Auto. Asks for them only while the car is connected and its lyrics are on
 * ([PlaybackService.wantsCarLyrics]); the phone's own lyrics views are left as they are.
 */
object CarLyricsFeed {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val lock = Any()
    /** The song playing ([app.myspoti.host.android.NowPlayingInfo.id]) and its lyrics request. */
    private var songId = ""
    private var track: LyricsApi.Track? = null
    /** The song whose lines were asked for: once per song. */
    private var askedId: String? = null
    private var job: Job? = null

    /** The song now playing; [track] null when it has no lyrics to look for (an ad, an episode). */
    fun onSong(id: String, track: LyricsApi.Track?) {
        if (PlaybackService.onCarLyricsWanted == null) PlaybackService.onCarLyricsWanted = { refresh() }
        synchronized(lock) {
            if (id != songId) {
                songId = id
                this.track = track
            } else if (this.track == null) this.track = track
        }
        refresh()
    }

    /** Asks for the playing song's lines, when the car wants them and they were not asked for yet. */
    fun refresh() {
        val (id, t) = synchronized(lock) {
            val t = track
            if (songId.isBlank() || t == null || askedId == songId || !PlaybackService.wantsCarLyrics(Graph.app)) return
            askedId = songId
            job?.cancel()
            songId to t
        }
        job = scope.launch {
            val lyrics = runCatching { LyricsApi.fetch(t) }.getOrNull()
            // Unsynced lyrics have no times: the car keeps the song's title then.
            val lines = lyrics?.takeIf { it.synced && !it.isEmpty }?.lines
                ?.map { CarLyricLine(it.timeMs, it.text) }.orEmpty()
            app.myspoti.host.android.Logbook.log("car lyrics: ${lines.size} lines")
            PlaybackService.updateCarLyrics(id, lines)
        }
    }
}
