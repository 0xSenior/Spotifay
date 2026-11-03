package com.example.spotifay.data

import android.os.SystemClock
import com.example.spotifay.data.audius.AudiusApi
import com.example.spotifay.data.audius.AudiusConfig
import com.example.spotifay.data.audius.AudiusPlaylist
import com.example.spotifay.data.audius.AudiusTrack
import com.example.spotifay.data.network.CachePolicyInterceptor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

private const val TTL_TRENDING_TRACKS = 10 * 60_000L
private const val TTL_TRENDING_PLAYLISTS = 15 * 60_000L
private const val TTL_SEARCH = 6 * 60 * 60_000L
private const val TTL_PLAYLIST_TRACKS = 12 * 60 * 60_000L
private const val MAX_MEMORY_ENTRIES = 40

/**
 * Single source of music. No session, no token — Audius serves everything
 * anonymously, so every call here works on a cold start.
 *
 * Every read goes through two cache layers:
 *  - an in-memory copy so tab switches and back navigation render with no I/O;
 *  - OkHttp's disk cache (see [CachePolicyInterceptor]) so a cold start is fast
 *    and a dropped connection can still be served from what was already fetched.
 *
 * Concurrent identical requests share one network call.
 */
class MusicRepository(private val api: AudiusApi) {

    private val appName = AudiusConfig.APP_NAME
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val memory = MemoryCache(MAX_MEMORY_ENTRIES)
    private val inFlight = ConcurrentHashMap<String, Deferred<Any>>()

    suspend fun trendingTracks(limit: Int = 40, force: Boolean = false): List<AudiusTrack> =
        cached("trending-tracks:$limit", TTL_TRENDING_TRACKS, force) { policy ->
            api.trendingTracks(appName, limit, "week", policy).data.orEmpty().filter { it.isPlayable }
        }

    suspend fun searchTracks(query: String, limit: Int = 25, force: Boolean = false): List<AudiusTrack> =
        cached("search-tracks:${query.lowercase()}:$limit", TTL_SEARCH, force) { policy ->
            api.searchTracks(query, appName, limit, policy).data.orEmpty().filter { it.isPlayable }
        }

    suspend fun trendingPlaylists(limit: Int = 20, force: Boolean = false): List<AudiusPlaylist> =
        cached("trending-playlists:$limit", TTL_TRENDING_PLAYLISTS, force) { policy ->
            api.trendingPlaylists(appName, limit, policy).data.orEmpty()
        }

    suspend fun searchPlaylists(query: String, limit: Int = 10, force: Boolean = false): List<AudiusPlaylist> =
        cached("search-playlists:${query.lowercase()}:$limit", TTL_SEARCH, force) { policy ->
            api.searchPlaylists(query, appName, limit, policy).data.orEmpty()
        }

    suspend fun playlistTracks(playlistId: String, force: Boolean = false): List<AudiusTrack> =
        cached("playlist-tracks:$playlistId", TTL_PLAYLIST_TRACKS, force) { policy ->
            api.playlistTracks(playlistId, appName, policy).data.orEmpty().filter { it.isPlayable }
        }

    /**
     * Fills the cache as soon as the process starts, so the first Home frame
     * usually has nothing left to wait for.
     */
    fun warmUp() {
        scope.launch {
            launch { runCatching { trendingTracks() } }
            launch { runCatching { trendingPlaylists() } }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun <T : Any> cached(
        key: String,
        ttlMs: Long,
        force: Boolean,
        fetch: suspend (String?) -> T,
    ): T {
        if (!force) {
            memory.get(key)?.takeIf { it.isFresh(ttlMs) }?.let { return it.valueAs() }
        }

        // Share one in-flight call between every caller that wants the same key.
        val job = inFlight.compute(key) { _, running ->
            if (running != null && !running.isCompleted) running else scope.async { load(key, force, fetch) }
        }!!

        try {
            return job.await() as T
        } finally {
            inFlight.remove(key, job)
        }
    }

    private suspend fun <T : Any> load(
        key: String,
        force: Boolean,
        fetch: suspend (String?) -> T,
    ): T {
        return try {
            val value = fetch(if (force) CachePolicyInterceptor.REFRESH else null)
            memory.put(key, value)
            value
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            // Offline or the catalogue hiccupped: an older copy still beats an error.
            memory.get(key)?.let { return it.valueAs() }

            val stale = runCatching { fetch(CachePolicyInterceptor.CACHE_ONLY) }.getOrNull()
            if (stale != null) {
                memory.put(key, stale)
                return stale
            }
            throw failure
        }
    }

    private class Entry(val value: Any, val storedAt: Long) {
        fun isFresh(ttlMs: Long) = SystemClock.elapsedRealtime() - storedAt < ttlMs

        @Suppress("UNCHECKED_CAST")
        fun <T : Any> valueAs(): T = value as T
    }

    private class MemoryCache(private val maxEntries: Int) {
        private val entries = object : LinkedHashMap<String, Entry>(16, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Entry>): Boolean =
                size > maxEntries
        }

        @Synchronized
        operator fun get(key: String): Entry? = entries[key]

        @Synchronized
        fun put(key: String, value: Any) {
            entries[key] = Entry(value, SystemClock.elapsedRealtime())
        }
    }
}