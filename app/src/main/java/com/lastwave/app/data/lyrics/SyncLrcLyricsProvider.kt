package com.lastwave.app.data.lyrics

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class SyncLrcRecord(
    val id: String? = null,
    val title: String? = null,
    @SerialName("track_name") val trackName: String? = null,
    val artist: String? = null,
    @SerialName("artist_name") val artistName: String? = null,
    val album: String? = null,
    @SerialName("album_name") val albumName: String? = null,
    val duration: Double? = null,
    val instrumental: Boolean? = false,
    @SerialName("plain_lyrics") val plainLyrics: String? = null,
    @SerialName("plainLyrics") val plainLyricsAlt: String? = null,
    @SerialName("synced_lyrics") val syncedLyrics: String? = null,
    @SerialName("syncedLyrics") val syncedLyricsAlt: String? = null,
)

@Singleton
class SyncLrcLyricsProvider @Inject constructor(
    private val okHttpClient: OkHttpClient,
) {
    private val client: OkHttpClient by lazy {
        okHttpClient.newBuilder()
            .connectTimeout(6, TimeUnit.SECONDS)
            .readTimeout(6, TimeUnit.SECONDS)
            .build()
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun getLyrics(
        title: String,
        artist: String,
        album: String? = null,
        durationSeconds: Int? = null,
    ): Result<LyricsData> = withContext(Dispatchers.IO) {
        runCatching {
            if (title.isBlank() || artist.isBlank()) {
                throw IllegalArgumentException("Title and artist cannot be empty")
            }

            val sanitizedTitle = cleanMetadata(title)
            val sanitizedArtist = cleanMetadata(artist)

            // 1. Query exact lookup: https://synclrc.dev/api/lyrics
            val exactRecord = fetchExactLyrics(sanitizedTitle, sanitizedArtist, album, durationSeconds)
            if (exactRecord != null) {
                val synced = exactRecord.syncedLyrics ?: exactRecord.syncedLyricsAlt
                val plain = exactRecord.plainLyrics ?: exactRecord.plainLyricsAlt
                val isInst = exactRecord.instrumental ?: false

                if (!synced.isNullOrBlank() || !plain.isNullOrBlank() || isInst) {
                    return@runCatching LyricsData(
                        syncedLyrics = synced,
                        plainLyrics = plain,
                        isInstrumental = isInst,
                        source = "SyncLRC"
                    )
                }
            }

            // 2. Fallback search endpoint: https://synclrc.dev/api/search
            val searchRecord = fetchSearchLyrics(sanitizedTitle, sanitizedArtist, durationSeconds)
            if (searchRecord != null) {
                val synced = searchRecord.syncedLyrics ?: searchRecord.syncedLyricsAlt
                val plain = searchRecord.plainLyrics ?: searchRecord.plainLyricsAlt
                val isInst = searchRecord.instrumental ?: false

                if (!synced.isNullOrBlank() || !plain.isNullOrBlank() || isInst) {
                    return@runCatching LyricsData(
                        syncedLyrics = synced,
                        plainLyrics = plain,
                        isInstrumental = isInst,
                        source = "SyncLRC"
                    )
                }
            }

            throw IOException("No lyrics found on SyncLRC for $sanitizedArtist - $sanitizedTitle")
        }
    }

    private fun fetchExactLyrics(
        title: String,
        artist: String,
        album: String?,
        durationSeconds: Int?,
    ): SyncLrcRecord? {
        val urlBuilder = "https://synclrc.dev/api/lyrics".toHttpUrlOrNull()?.newBuilder() ?: return null
        urlBuilder.addQueryParameter("title", title)
        urlBuilder.addQueryParameter("artist", artist)
        if (!album.isNullOrBlank()) {
            urlBuilder.addQueryParameter("album", cleanMetadata(album))
        }
        if (durationSeconds != null && durationSeconds > 0) {
            urlBuilder.addQueryParameter("duration", durationSeconds.toString())
        }

        val request = Request.Builder()
            .url(urlBuilder.build())
            .header("User-Agent", "LastXWave-Android/1.7.0 (contact@lastwave.app)")
            .header("Accept", "application/json")
            .build()

        return try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return null
            val bodyString = response.body?.string() ?: return null
            if (bodyString.isBlank()) return null
            json.decodeFromString<SyncLrcRecord>(bodyString)
        } catch (_: Exception) {
            null
        }
    }

    private fun fetchSearchLyrics(
        title: String,
        artist: String,
        durationSeconds: Int?,
    ): SyncLrcRecord? {
        val urlBuilder = "https://synclrc.dev/api/search".toHttpUrlOrNull()?.newBuilder() ?: return null
        urlBuilder.addQueryParameter("q", "$artist $title")
        if (durationSeconds != null && durationSeconds > 0) {
            urlBuilder.addQueryParameter("duration", durationSeconds.toString())
        }

        val request = Request.Builder()
            .url(urlBuilder.build())
            .header("User-Agent", "LastXWave-Android/1.7.0")
            .header("Accept", "application/json")
            .build()

        return try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return null
            val bodyString = response.body?.string() ?: return null
            if (bodyString.isBlank()) return null
            val list = json.decodeFromString<List<SyncLrcRecord>>(bodyString)
            list.firstOrNull {
                !it.syncedLyrics.isNullOrBlank() || !it.syncedLyricsAlt.isNullOrBlank() || !it.plainLyrics.isNullOrBlank()
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun cleanMetadata(value: String): String {
        return value
            .replace(Regex("""\s*[\(\[\{].*?(feat|ft|official|audio|video|remastered|version|bonus|deluxe).*?[\)\]\}]""", RegexOption.IGNORE_CASE), "")
            .trim()
    }
}
