package com.example.spotifay.data.network

import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

/**
 * Audius sends no Cache-Control on API responses, so OkHttp would never reuse
 * them. Attach a per-endpoint lifetime and the whole catalogue becomes
 * cacheable: repeat loads are instant and work offline.
 */
class CacheTtlInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (!request.method.equals("GET", ignoreCase = true) || response.code != 200) return response

        // Only the catalogue JSON needs a synthetic lifetime; artwork already
        // ships a long-lived Cache-Control of its own.
        val json = response.header("Content-Type")?.contains("json", ignoreCase = true) == true
        if (!json) return response

        val seconds = ttlSeconds(request.url.encodedPath)
        return response.newBuilder()
            .header("Cache-Control", "public, max-age=$seconds, immutable")
            .build()
    }

    private fun ttlSeconds(path: String): Long = when {
        path.contains("/tracks/trending") -> 10 * 60L
        path.contains("/playlists/trending") -> 15 * 60L
        path.contains("/search") -> 6 * 60 * 60L
        path.endsWith("/tracks") -> 12 * 60 * 60L // playlist track listings
        else -> 60 * 60L
    }
}

/**
 * Lets the repository ask for either "refresh, ignore what you have" or
 * "give me whatever you cached, even if stale". Both travel as a header so the
 * Retrofit signatures stay declarative.
 */
class CachePolicyInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val policy = request.header(HEADER) ?: return chain.proceed(request)

        val builder = request.newBuilder().removeHeader(HEADER)
        when (policy) {
            CACHE_ONLY -> builder.cacheControl(
                CacheControl.Builder()
                    .onlyIfCached()
                    .maxStale(7, TimeUnit.DAYS)
                    .build(),
            )

            REFRESH -> builder.cacheControl(CacheControl.Builder().noCache().build())
        }
        return chain.proceed(builder.build())
    }

    companion object {
        const val HEADER = "X-Spotifay-Cache"
        const val CACHE_ONLY = "cache-only"
        const val REFRESH = "refresh"
    }
}