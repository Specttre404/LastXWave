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
import javax.inject.Inject
import javax.inject.Singleton

data class LyricsData(
    val syncedLyrics: String? = null,
    val plainLyrics: String? = null,
    val isInstrumental: Boolean = false,
    val source: String = "LRC.red"
)

@Serializable
private data class LrcRedRecord(
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
class LrcRedLyricsProvider @Inject constructor(
    private val okHttpClient: OkHttpClient,
) {
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

            // 1. Try exact endpoint: https://lrc.red/api/lyrics
            val exactRecord = fetchExactLyrics(title, artist, album, durationSeconds)
            if (exactRecord != null) {
                val synced = exactRecord.syncedLyrics ?: exactRecord.syncedLyricsAlt
                val plain = exactRecord.plainLyrics ?: exactRecord.plainLyricsAlt
                val isInst = exactRecord.instrumental ?: false

                if (!synced.isNullOrBlank() || !plain.isNullOrBlank() || isInst) {
                    return@runCatching LyricsData(
                        syncedLyrics = synced,
                        plainLyrics = plain,
                        isInstrumental = isInst,
                        source = "LRC.red"
                    )
                }
            }

            // 2. Fallback search endpoint: https://lrc.red/api/search
            val searchRecord = fetchSearchLyrics(title, artist, durationSeconds)
            if (searchRecord != null) {
                val synced = searchRecord.syncedLyrics ?: searchRecord.syncedLyricsAlt
                val plain = searchRecord.plainLyrics ?: searchRecord.plainLyricsAlt
                val isInst = searchRecord.instrumental ?: false

                if (!synced.isNullOrBlank() || !plain.isNullOrBlank() || isInst) {
                    return@runCatching LyricsData(
                        syncedLyrics = synced,
                        plainLyrics = plain,
                        isInstrumental = isInst,
                        source = "LRC.red"
                    )
                }
            }

            throw IOException("No lyrics found on LRC.red for $artist - $title")
        }
    }

    private fun fetchExactLyrics(
        title: String,
        artist: String,
        album: String?,
        durationSeconds: Int?,
    ): LrcRedRecord? {
        val urlBuilder = "https://lrc.red/api/lyrics".toHttpUrlOrNull()?.newBuilder() ?: return null
        urlBuilder.addQueryParameter("track_name", title)
        urlBuilder.addQueryParameter("artist_name", artist)
        if (!album.isNullOrBlank()) {
            urlBuilder.addQueryParameter("album_name", album)
        }
        if (durationSeconds != null && durationSeconds > 0) {
            urlBuilder.addQueryParameter("duration", durationSeconds.toString())
        }

        val request = Request.Builder()
            .url(urlBuilder.build())
            .header("User-Agent", "LastXWave-Android/1.0")
            .build()

        return try {
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) return null
            val bodyString = response.body?.string() ?: return null
            if (bodyString.isBlank()) return null
            json.decodeFromString<LrcRedRecord>(bodyString)
        } catch (_: Exception) {
            null
        }
    }

    private fun fetchSearchLyrics(
        title: String,
        artist: String,
        durationSeconds: Int?,
    ): LrcRedRecord? {
        val urlBuilder = "https://lrc.red/api/search".toHttpUrlOrNull()?.newBuilder() ?: return null
        urlBuilder.addQueryParameter("track_name", title)
        urlBuilder.addQueryParameter("artist_name", artist)
        if (durationSeconds != null && durationSeconds > 0) {
            urlBuilder.addQueryParameter("duration", durationSeconds.toString())
        }

        val request = Request.Builder()
            .url(urlBuilder.build())
            .header("User-Agent", "LastXWave-Android/1.0")
            .build()

        return try {
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) return null
            val bodyString = response.body?.string() ?: return null
            if (bodyString.isBlank()) return null
            val list = json.decodeFromString<List<LrcRedRecord>>(bodyString)
            list.firstOrNull {
                !it.syncedLyrics.isNullOrBlank() || !it.syncedLyricsAlt.isNullOrBlank() || !it.plainLyrics.isNullOrBlank()
            }
        } catch (_: Exception) {
            null
        }
    }
}
