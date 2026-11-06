package com.lastwave.app.data.lyrics

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class LyricsData(
    val syncedLyrics: String? = null,
    val plainLyrics: String? = null,
    val isInstrumental: Boolean = false,
    val source: String = "lrc.red"
)

data class SyncedLyricsResult(
    val id: String?,
    val trackName: String,
    val artistName: String,
    val albumName: String?,
    val durationSeconds: Int,
    val syncedLyrics: String?,
    val plainLyrics: String?,
    val isInstrumental: Boolean = false,
    val source: String = "lrc.red"
)

@Singleton
class LrcRedLyricsProvider @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    private val client: OkHttpClient by lazy {
        okHttpClient.newBuilder()
            .connectTimeout(6, TimeUnit.SECONDS)
            .readTimeout(6, TimeUnit.SECONDS)
            .build()
    }

    suspend fun fetchLyrics(
        trackTitle: String,
        artistName: String,
        albumName: String? = null,
        durationSeconds: Int = 0
    ): Result<SyncedLyricsResult> = withContext(Dispatchers.IO) {
        try {
            val sanitizedTitle = cleanMetadata(trackTitle)
            val sanitizedArtist = cleanMetadata(artistName)

            val urlBuilder = "https://lrc.red/api/lyrics".toHttpUrlOrNull()?.newBuilder()
                ?: return@withContext Result.failure(IllegalArgumentException("Invalid base URL"))

            urlBuilder.addQueryParameter("title", sanitizedTitle)
            urlBuilder.addQueryParameter("track_name", sanitizedTitle)
            urlBuilder.addQueryParameter("artist", sanitizedArtist)
            urlBuilder.addQueryParameter("artist_name", sanitizedArtist)
            if (!albumName.isNullOrBlank()) {
                val cleanAlbum = cleanMetadata(albumName)
                urlBuilder.addQueryParameter("album", cleanAlbum)
                urlBuilder.addQueryParameter("album_name", cleanAlbum)
            }
            if (durationSeconds > 0) {
                urlBuilder.addQueryParameter("duration", durationSeconds.toString())
            }

            val request = Request.Builder()
                .url(urlBuilder.build())
                .header("User-Agent", "LastXWave/1.7.0 (contact@lastwave.app)")
                .header("Accept", "application/json")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext searchFallback(sanitizedTitle, sanitizedArtist)
                }

                val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty body"))
                val json = JSONObject(body)

                val synced = json.optString("syncedLyrics").ifBlank { json.optString("synced_lyrics") }.takeIf { it.isNotBlank() }
                val plain = json.optString("plainLyrics").ifBlank { json.optString("plain_lyrics") }.takeIf { it.isNotBlank() }
                val instrumental = json.optBoolean("instrumental", false)

                Result.success(
                    SyncedLyricsResult(
                        id = json.optString("id"),
                        trackName = json.optString("title", json.optString("track_name", sanitizedTitle)),
                        artistName = json.optString("artist", json.optString("artist_name", sanitizedArtist)),
                        albumName = json.optString("album", json.optString("album_name", albumName.orEmpty())),
                        durationSeconds = json.optInt("duration", durationSeconds),
                        syncedLyrics = synced,
                        plainLyrics = plain,
                        isInstrumental = instrumental,
                        source = "lrc.red"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLyrics(
        title: String,
        artist: String,
        album: String? = null,
        durationSeconds: Int? = null,
    ): Result<LyricsData> {
        val res = fetchLyrics(title, artist, album, durationSeconds ?: 0)
        return res.map {
            LyricsData(
                syncedLyrics = it.syncedLyrics,
                plainLyrics = it.plainLyrics,
                isInstrumental = it.isInstrumental,
                source = it.source
            )
        }
    }

    private fun searchFallback(title: String, artist: String): Result<SyncedLyricsResult> {
        val searchUrl = "https://lrc.red/api/search".toHttpUrlOrNull()?.newBuilder()
            ?.addQueryParameter("q", "$artist $title")
            ?.addQueryParameter("track_name", title)
            ?.addQueryParameter("artist_name", artist)
            ?.build() ?: return Result.failure(Exception("Failed to build search URL"))

        val request = Request.Builder()
            .url(searchUrl)
            .header("User-Agent", "LastXWave/1.7.0")
            .get()
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return Result.failure(Exception("Search failed with code ${response.code}"))
                val text = response.body?.string() ?: return Result.failure(Exception("Empty response"))
                val array = JSONArray(text)
                if (array.length() == 0) return Result.failure(Exception("No results found on lrc.red"))

                val bestMatch = array.getJSONObject(0)
                Result.success(
                    SyncedLyricsResult(
                        id = bestMatch.optString("id"),
                        trackName = bestMatch.optString("title", bestMatch.optString("track_name", title)),
                        artistName = bestMatch.optString("artist", bestMatch.optString("artist_name", artist)),
                        albumName = bestMatch.optString("album", bestMatch.optString("album_name")),
                        durationSeconds = bestMatch.optInt("duration", 0),
                        syncedLyrics = bestMatch.optString("syncedLyrics").ifBlank { bestMatch.optString("synced_lyrics") }.takeIf { it.isNotBlank() },
                        plainLyrics = bestMatch.optString("plainLyrics").ifBlank { bestMatch.optString("plain_lyrics") }.takeIf { it.isNotBlank() },
                        isInstrumental = bestMatch.optBoolean("instrumental", false),
                        source = "lrc.red"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun cleanMetadata(value: String): String {
        return value
            .replace(Regex("""\s*[\(\[\{].*?(feat|ft|official|audio|video|remastered|version|bonus|deluxe).*?[\)\]\}]""", RegexOption.IGNORE_CASE), "")
            .trim()
    }
}
