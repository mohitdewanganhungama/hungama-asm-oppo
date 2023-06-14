package com.hungama.music.auto.media.library

import android.content.Context
import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import com.android.volley.VolleyError
import com.google.gson.Gson
import com.hungama.music.auto.api.model.BodyDataItem
import com.hungama.music.auto.api.model.HomeModel
import com.hungama.music.auto.media.extensions.*
import com.hungama.music.data.webservice.remote.data.DataManager
import com.hungama.music.data.webservice.remote.data.DataValues
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.*
import java.util.concurrent.TimeUnit

/**
 * Source of [MediaMetadataCompat] objects created from a basic JSON stream.
 *
 * The definition of the JSON is specified in the docs of [JsonMusic] in this file,
 * which is the object representation of it.
 */
class JsonSource(private val source: Uri, context: Context) : AbstractMusicSource() {
    val TAG="JsonSource"

    companion object {
        const val ORIGINAL_ARTWORK_URI_KEY = "com.example.android.uamp.JSON_ARTWORK_URI"
        var model: JsonCatalog? = null
    }
    private var catalog: List<MediaMetadataCompat> = emptyList()
    private val context = context

    init {
        state = STATE_INITIALIZING
        model = JsonCatalog()
        Log.d(TAG, "JsonSource: init")

    }

    override fun iterator(): Iterator<MediaMetadataCompat> = catalog.iterator()

    override suspend fun load() {
        updateCatalog(source, this.context)?.let { updatedCatalog ->
            catalog = updatedCatalog
            state = STATE_INITIALIZED
        } ?: run {
            catalog = emptyList()
            state = STATE_ERROR
        }
    }

    /**
     * Function to connect to a remote URI and download/process the JSON file that corresponds to
     * [MediaMetadataCompat] objects.
     */
    private suspend fun updateCatalog(catalogUri: Uri, context: Context): List<MediaMetadataCompat>? {
        Log.d(TAG, "updateCatalog: ")

        return withContext(Dispatchers.IO) {
            Log.d("updateCatalog"," updateCatalog downloadJson start ")

            var musicCat=downloadJson(catalogUri, context)
            delay(10000)

            if(musicCat==null){
                musicCat= model!!
            }
Log.d("updateCatalog","musicCat:${musicCat} ")
            // Get the base URI to fix up relative references later.
            val baseUri = catalogUri.toString().removeSuffix(catalogUri.lastPathSegment ?: "")
            Log.d("updateCatalog","baseUri:${baseUri} musicCat.music:${musicCat.music}")
            val mediaMetadataCompats = musicCat.music.map { song ->
                // The JSON may have paths that are relative to the source of the JSON
                // itself. We need to fix them up here to turn them into absolute paths.
                catalogUri.scheme?.let { scheme ->
                    if (!song.source.startsWith(scheme)) {
                        song.source = baseUri + song.source
                    }
                    if (!song.image.startsWith(scheme)) {
                        song.image = baseUri + song.image
                    }
                }
                val imageUri = AlbumArtContentProvider.mapUri(Uri.parse(song.image))

                Log.d("TAG", "updateCatalog: imageUri:${imageUri} song.image:${song.image}")

                MediaMetadataCompat.Builder()
                    .from(song)
                    .apply {
                        displayIconUri = song.image.toString() // Used by ExoPlayer and Notification
                        albumArtUri = song.image.toString()
                        displayTitle= song.title.toString()
                        displaySubtitle= song.artist.toString()
                        mediaUri= song.source.toString()
                        putString(ORIGINAL_ARTWORK_URI_KEY, song.image.toString())
                    }
                    .build()
            }.toList()
            // Add description keys to be used by the ExoPlayer MediaSession extension when
            // announcing metadata changes.
            mediaMetadataCompats.forEach { it.description.extras?.putAll(it.bundle) }
            mediaMetadataCompats


        }
    }

    /**
     * Attempts to download a catalog from a given Uri.
     *
     * @param catalogUri URI to attempt to download the catalog form.
     * @return The catalog downloaded, or an empty catalog if an error occurred.
     */
    @Throws(IOException::class)
    @Synchronized
    suspend private fun downloadJson(catalogUri: Uri, context: Context)= runBlocking {
        val tasks = listOf(
            async { getHomeListDataLatest(context, "") }
        )
        Log.d(TAG, "downloadJson: getHomeListDataLatest model?.music?.size:${model?.music?.size}")
        tasks?.awaitAll()
        model
    }


    suspend fun getHomeListDataLatest(context: Context, url: String): JsonCatalog {

        return withContext(Dispatchers.IO) {
            Log.d(TAG, "downloadJson: getHomeListDataLatest model?.music?.size:${model?.music?.size}")
            if (model?.music?.isNullOrEmpty()!!) {
                val dataManger = DataManager.getInstance(context)

//        var url="https://page.api.hungama.com/v1/page/home?uid=5&alang=en&mlang=en,&vlang=en&device=carPlay&platform=a&storeId=1"
                var url =
                    "https://cpage.api.hungama.com/v1/page/podcast?alang=en&vlang=en,hi&mlang=en,hi&platform=a&device=carPlay&variant=v1&uid=632155051&storeId=1"
                Log.d(TAG, "downloadJson: getHomeListDataLatest url:${url}")
                var music: ArrayList<JsonMusic> = ArrayList()

                dataManger?.getVolleyRequest(context, url, JSONObject(), object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        try {
                            val homeModel = Gson().fromJson<HomeModel>(
                                response.toString(),
                                HomeModel::class.java
                            ) as HomeModel
                            Log.d(TAG, "downloadJson: rows size:${homeModel?.data?.body?.rows?.size}")

                            homeModel?.data?.body?.rows?.forEach {
                                var model = it
                                try {
                                    var trackNumber=1L
                                    model?.items?.forEach {
                                        val jsonJsonMusic = JsonMusic()
                                        jsonJsonMusic?.id = "" + it?.data?.id!!
                                        jsonJsonMusic?.bucketHeaderName = "" + model?.heading!!
                                        jsonJsonMusic?.album = "" + model?.heading!!
                                        if(!it?.data?.misc?.artist?.isNullOrEmpty()!!){
                                            jsonJsonMusic?.artist = "" + it?.data?.misc?.artist?.get(0)!!
                                        }
                                        jsonJsonMusic?.image = "" + it?.data?.image
                                        jsonJsonMusic?.title = "" + it?.data?.title
                                        if(!it?.data?.genre?.isNullOrEmpty()!!){
                                            jsonJsonMusic?.genre = "" + it?.data?.genre?.get(0)
                                        }
                                        jsonJsonMusic.trackNumber=trackNumber
                                        trackNumber++
                                        jsonJsonMusic?.source = "https://pagalworld.com.se/files/download/type/64/id/2542"
                                        music.add(jsonJsonMusic)

//                                        runBlocking {
//                                            getDetail(context,it?.data!!)
//                                        }


                                        Log.d(TAG, "downloadJson: jsonJsonMusic:${jsonJsonMusic} music:${music?.size}")
                                    }

                                }catch (exp:Exception){
                                    exp.printStackTrace()
                                }

                            }

                            model?.music = music

                            Log.d(TAG, "downloadJson: size:${model?.music?.size}")

                        } catch (exp: Exception) {
                            exp.printStackTrace()
                        }
                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
                        Log.d(TAG, "downloadJson: volleyError:${volleyError}")
//                homeResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                    }

                })


            }

            model!!
        }


    }

    suspend fun getDetail(context: Context, dataModel: BodyDataItem) {
        withContext(Dispatchers.IO) {
            Log.d(TAG, "downloadJson: getHomeListDataLatest model?.music?.size:${model?.music?.size}")
            if (model?.music?.isNullOrEmpty()!!) {
                val dataManger = DataManager.getInstance(context)
//        var url="https://page.api.hungama.com/v1/page/home?uid=5&alang=en&mlang=en,&vlang=en&device=carPlay&platform=a&storeId=1"
                var url =
                    "https://cpage.api.hungama.com/v2/page/content/"+dataModel?.id+"/podcast/detail/?alang=en&vlang=hi,en&mlang=hi,en&platform=a&device=android&variant=v2&uid=896965566&storeId=1"
                Log.d(TAG, "downloadJson: getHomeListDataLatest url:${url}")
                var music: ArrayList<JsonMusic> = ArrayList()

                dataManger?.getVolleyRequest(context, url, JSONObject(), object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        try {
                            val homeModel = Gson().fromJson<HomeModel>(
                                response.toString(),
                                HomeModel::class.java
                            ) as HomeModel
                            Log.d(TAG, "downloadJson: rows size:${homeModel?.data?.body?.rows?.size}")

                            homeModel?.data?.body?.rows?.forEach {
                                var model = it
                                try {
                                    var trackNumber=1L
                                    model?.items?.forEach {
                                        val jsonJsonMusic = JsonMusic()
                                        jsonJsonMusic?.id = "" + it?.data?.id!!
                                        jsonJsonMusic?.album = "" + dataModel?.title!!
                                        if(!it?.data?.misc?.artist?.isNullOrEmpty()!!){
                                            jsonJsonMusic?.artist = "" + it?.data?.misc?.artist?.get(0)!!
                                        }
                                        jsonJsonMusic?.image = "" + it?.data?.image
                                        jsonJsonMusic?.title = "" + it?.data?.title
                                        if(!it?.data?.genre?.isNullOrEmpty()!!){
                                            jsonJsonMusic?.genre = "" + it?.data?.genre?.get(0)
                                        }
                                        jsonJsonMusic.trackNumber=trackNumber
                                        trackNumber++
                                        jsonJsonMusic?.source = "https://pagalworld.com.se/files/download/type/64/id/2542"
                                        music.add(jsonJsonMusic)

                                        Log.d(TAG, "downloadJson: jsonJsonMusic:${jsonJsonMusic} music:${music?.size}")
                                    }

                                }catch (exp:Exception){
                                    exp.printStackTrace()
                                }

                            }

                            model?.music = music

                            Log.d(TAG, "downloadJson: size:${model?.music?.size}")

                        } catch (exp: Exception) {
                            exp.printStackTrace()
                        }
                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
                        Log.d(TAG, "downloadJson: volleyError:${volleyError}")
//                homeResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                    }

                })


            }

            model!!
        }


    }
}

/**
 * Extension method for [MediaMetadataCompat.Builder] to set the fields from
 * our JSON constructed object (to make the code a bit easier to see).
 */
fun MediaMetadataCompat.Builder.from(jsonMusic: JsonMusic): MediaMetadataCompat.Builder {
    // The duration from the JSON is given in seconds, but the rest of the code works in
    // milliseconds. Here's where we convert to the proper units.
    val durationMs = TimeUnit.SECONDS.toMillis(jsonMusic.duration)

    id = jsonMusic.id
    title = jsonMusic.title
    artist = jsonMusic.artist
    album = jsonMusic.album
    duration = durationMs
    genre = jsonMusic.genre
    mediaUri = jsonMusic.source
    albumArtUri = jsonMusic.image
    trackNumber = jsonMusic.trackNumber
    trackCount = jsonMusic.totalTrackCount
    flag = MediaBrowserCompat.MediaItem.FLAG_PLAYABLE

    // To make things easier for *displaying* these, set the display properties as well.
    displayTitle = jsonMusic.title
    displaySubtitle = jsonMusic.artist
    displayDescription = jsonMusic.album
    displayIconUri = jsonMusic.image

    // Add downloadStatus to force the creation of an "extras" bundle in the resulting
    // MediaMetadataCompat object. This is needed to send accurate metadata to the
    // media session during updates.
    downloadStatus = MediaDescriptionCompat.STATUS_NOT_DOWNLOADED

    // Allow it to be used in the typical builder style.
    return this
}

/**
 * Wrapper object for our JSON in order to be processed easily by GSON.
 */
class JsonCatalog {
    var music: ArrayList<JsonMusic> = ArrayList()
    override fun toString(): String {
        return "JsonCatalog(music=$music)"
    }
}

/**
 * An individual piece of music included in our JSON catalog.
 * The format from the server is as specified:
 * ```
 *     { "music" : [
 *     { "title" : // Title of the piece of music
 *     "album" : // Album title of the piece of music
 *     "artist" : // Artist of the piece of music
 *     "genre" : // Primary genre of the music
 *     "source" : // Path to the music, which may be relative
 *     "image" : // Path to the art for the music, which may be relative
 *     "trackNumber" : // Track number
 *     "totalTrackCount" : // Track count
 *     "duration" : // Duration of the music in seconds
 *     "site" : // Source of the music, if applicable
 *     }
 *     ]}
 * ```
 *
 * `source` and `image` can be provided in either relative or
 * absolute paths. For example:
 * ``
 *     "source" : "https://www.example.com/music/ode_to_joy.mp3",
 *     "image" : "ode_to_joy.jpg"
 * ``
 *
 * The `source` specifies the full URI to download the piece of music from, but
 * `image` will be fetched relative to the path of the JSON file itself. This means
 * that if the JSON was at "https://www.example.com/json/music.json" then the image would be found
 * at "https://www.example.com/json/ode_to_joy.jpg".
 */
@Suppress("unused")
class JsonMusic {
    var id: String = ""
    var title: String = ""
    var album: String = ""
    var bucketHeaderName: String = ""
    var artist: String = ""
    var genre: String = ""
    var source: String = ""
    var image: String = ""
    var trackNumber: Long = 0
    var totalTrackCount: Long = 0
    var duration: Long = -1
    var site: String = ""
    override fun toString(): String {
        return "JsonMusic(id='$id', title='$title', album='$album', artist='$artist', genre='$genre', source='$source', image='$image', trackNumber=$trackNumber, totalTrackCount=$totalTrackCount, duration=$duration, site='$site')"
    }
}
