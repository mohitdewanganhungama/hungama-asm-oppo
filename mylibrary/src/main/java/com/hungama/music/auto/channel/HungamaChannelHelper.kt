package com.hungama.music.auto.channel

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import com.android.volley.VolleyError
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.gson.Gson
import com.hungama.music.HungamaMusicApp
import com.hungama.music.auto.api.model.DetailDynamicModel
import com.hungama.music.auto.api.model.HomeModel
import com.hungama.music.auto.api.model.PlayableContentModel
import com.hungama.music.data.model.ContinueWhereLeftModel
import com.hungama.music.data.model.EventModel
import com.hungama.music.data.webservice.remote.data.DataManager
import com.hungama.music.data.webservice.remote.data.DataValues
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.home.eventreporter.UserAttributeEvent
import com.hungama.music.player.audioplayer.services.AudioPlayerService
import com.hungama.music.player.audioplayer.services.AudioPlayerService.Companion.HUNGAMA_LIBRARY_ALBUM_DETAIL_ROOT
import com.hungama.music.player.audioplayer.services.AudioPlayerService.Companion.HUNGAMA_LIBRARY_ARTIST_DETAIL_ROOT
import com.hungama.music.player.audioplayer.services.AudioPlayerService.Companion.HUNGAMA_LIBRARY_PLAYLIST_DETAIL_ROOT
import com.hungama.music.player.audioplayer.services.AudioPlayerService.Companion.HUNGAMA_PODCAST_DETAIL_ROOT
import com.hungama.music.player.audioplayer.services.AudioPlayerService.Companion.songList
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.Constant
import com.hungama.music.utils.preference.PrefConstant
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import kotlinx.coroutines.*
import kotlinx.coroutines.guava.future
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume


object HungamaChannelHelper {

    val TAG = "HungamaChannelHelper"
    val DEVICE = "carPlay"
    var userID = SharedPrefHelper.getInstance().getUserId()

    fun updateUserID(){
        userID= SharedPrefHelper.getInstance().getUserId()
    }

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    /**
     * get uri to drawable or any other resource type if u wish
     * @param context - context
     * @param drawableId - drawable res id
     * @return - uri
     */
    fun getUriToDrawable(
        context: Context,
        drawableId: Int
    ): Uri {
        return Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE
                .toString() + "://" + context.resources.getResourcePackageName(drawableId)
                    + '/' + context.resources.getResourceTypeName(drawableId)
                    + '/' + context.resources.getResourceEntryName(drawableId)
        )
    }
    fun createBrowsableListing(context: Context): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> = runBlocking {
            CommonUtils.setLog("HungamaChannelHelper", "createBrowsableListing: called")

            if (AudioPlayerService.mediaDescListing?.size!! > 0) {
                return@runBlocking Futures.immediateFuture(
                    LibraryResult.ofItemList(
                        AudioPlayerService.mediaDescListing!!,
                        MediaLibraryService.LibraryParams.Builder().build()))
            }else{
                AudioPlayerService.mediaDescListing = mutableListOf<MediaItem>()

                // Add discover
                val discoverUri = getUriToDrawable(context, R.drawable.ic_bottom_discover_black)
                val discoverMediaItem = MediaItem.Builder()
                    .setMediaId(AudioPlayerService.MEDIA_ID_DISCOVER)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(context.getString(R.string.discover_title))
                            .setFolderType(MediaMetadata.FOLDER_TYPE_MIXED)
                            .setArtworkUri(discoverUri)
                            .setIsPlayable(false)
                            .build()
                    )
                    .build()
                AudioPlayerService.mediaDescListing.add(discoverMediaItem)



                // Add recentlyUriMetadata
                val recentlyUri = getUriToDrawable(context, R.drawable.ic_recommended)

                val recentMediaItem = MediaItem.Builder()
                    .setMediaId(AudioPlayerService.MEDIA_ID_RECENT)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(context.getString(R.string.recently_played_title))
                            .setFolderType(MediaMetadata.FOLDER_TYPE_MIXED)
                            .setArtworkUri(recentlyUri)
                            .setIsPlayable(false)
                            .build()
                    )
                    .build()
                AudioPlayerService.mediaDescListing.add(recentMediaItem)

                // Add podcast
                val podcastUri = getUriToDrawable(context, R.drawable.ic_bottom_news_black)
                val podcastUrMediaItem = MediaItem.Builder()
                    .setMediaId(AudioPlayerService.MEDIA_ID_PODCAST)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(context.getString(R.string.podcast_title))
                            .setFolderType(MediaMetadata.FOLDER_TYPE_MIXED)
                            .setArtworkUri(podcastUri)
                            .setIsPlayable(false)
                            .build()
                    )
                    .build()
                AudioPlayerService.mediaDescListing.add(podcastUrMediaItem)

                // Add library
                val libraryUri = getUriToDrawable(context, R.drawable.ic_bottom_library_black)

                val libraryMediaItem = MediaItem.Builder()
                    .setMediaId(AudioPlayerService.MEDIA_ID_LIBRARY)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(context.getString(R.string.library_title))
                            .setFolderType(MediaMetadata.FOLDER_TYPE_MIXED)
                            .setArtworkUri(libraryUri)
                            .setIsPlayable(false)
                            .build()
                    )
                    .build()
                AudioPlayerService.mediaDescListing.add(libraryMediaItem)

                return@runBlocking Futures.immediateFuture(
                    LibraryResult.ofItemList(
                        AudioPlayerService.mediaDescListing!!,
                        MediaLibraryService.LibraryParams.Builder().build()))
            }




    }



    fun getDiscoverBucketList(
        context: Context,
        parentMediaId: String
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> = runBlocking {

        if (AudioPlayerService?.discoverBucketList?.size!! > 0) {
            return@runBlocking Futures.immediateFuture(
                LibraryResult.ofItemList(
                    AudioPlayerService.discoverBucketList!!,
                    MediaLibraryService.LibraryParams.Builder().build()))
        }else{

            val dataManger = DataManager.getInstance(context)

            var url =
                "https://cpage.api.hungama.com/v1/page/home"
            CommonUtils.setLog("TAG", "ChannelHelper: getHomeListDataLatest url:${url}")



            dataManger?.getVolleyRequestAuto(context, url, JSONObject(), object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    try {
                        AudioPlayerService.discoverBucketRespModel = Gson().fromJson<HomeModel>(
                            response.toString(),
                            HomeModel::class.java
                        ) as HomeModel
                        CommonUtils.setLog(
                            "ChannelHelper",
                            "ChannelHelper: rows size:${AudioPlayerService.discoverBucketRespModel?.data?.body?.rows?.size}"
                        )


                        try {
                            AudioPlayerService?.discoverBucketList = mutableListOf<MediaItem>()
                            AudioPlayerService.discoverBucketRespModel?.data?.body?.rows?.forEach {
                                var model = it
                                if (!model?.id?.equals(
                                        "1",
                                        true
                                    )!! && !model?.items?.isNullOrEmpty()!!
                                ) {


                                    val id =
                                        AudioPlayerService.HUNGAMA_DISCOVER_ROOT + "_" + model?.id


                                    val libraryMediaItem = MediaItem.Builder()
                                        .setMediaId(id)
                                        .setMediaMetadata(
                                            MediaMetadata.Builder()
                                                .setTitle(model?.heading)
                                                .setFolderType(MediaMetadata.FOLDER_TYPE_MIXED)
                                                .setArtworkUri(Uri.parse(model?.image))
                                                .setIsPlayable(false)
                                                .build()
                                        )
                                        .build()
                                    AudioPlayerService.discoverBucketList.add(libraryMediaItem)

                                    CommonUtils.setLog("ChannelHelper", "mediaDesc:${libraryMediaItem}")
                                }
                            }

                        } catch (exp: Exception) {
                            exp.printStackTrace()
                            LibraryResult.ofItemList(
                                AudioPlayerService?.discoverBucketList ?: ImmutableList.of(),
                                MediaLibraryService.LibraryParams.Builder().build()
                            )
                        }

                        CommonUtils.setLog(
                            "ChannelHelper",
                            "podcastBucketListing: size:${AudioPlayerService.discoverBucketList.size}"
                        )

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        LibraryResult.ofItemList(
                            AudioPlayerService?.discoverBucketList ?: ImmutableList.of(),
                            MediaLibraryService.LibraryParams.Builder().build()
                        )
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    CommonUtils.setLog("ChannelHelper", "ChannelHelper: volleyError:${volleyError?.message}")
                    LibraryResult.ofItemList(
                        AudioPlayerService?.discoverBucketList ?: ImmutableList.of(),
                        MediaLibraryService.LibraryParams.Builder().build()
                    )
                }

            })
            return@runBlocking Futures.immediateFuture(
                LibraryResult.ofItemList(
                    AudioPlayerService.discoverBucketList!!,
                    MediaLibraryService.LibraryParams.Builder().build()))

        }
    }

    fun getHeaderWiseListing(context: Context, id: String): MutableList<MediaItem> {
        CommonUtils.setLog("TAG", "getHeaderWiseListing: getPodcastDetail before id:${id}")
        CommonUtils.setLog(
            "TAG",
            "getHeaderWiseListing: getPodcastDetail size :${AudioPlayerService.podcastBucketRespModel?.data?.body?.rows?.size}"
        )


        var updateID = ""
        if (id.contains(AudioPlayerService.HUNGAMA_PODCAST_ROOT)) {
            updateID = id.replace(AudioPlayerService.HUNGAMA_PODCAST_ROOT + "_", "")
            AudioPlayerService.podcastBucketRespModel?.data?.body?.rows?.forEach {
                CommonUtils.setLog(
                    "TAG",
                    "getHeaderWiseListing: getPodcastDetail updateID:${updateID} id:${it?.id}"
                )
                if (updateID.equals(it?.id)) {
                    AudioPlayerService.podcastBucketWiseList = mutableListOf<MediaItem>()
                    it?.items?.forEach {
                        try {
                            val extras = Bundle()
                            extras.putBoolean(AudioPlayerService.CONTENT_STYLE_SUPPORTED, true)
                            extras.putInt(
                                AudioPlayerService.CONTENT_STYLE_BROWSABLE_HINT,
                                AudioPlayerService.CONTENT_STYLE_CATEGORY_LIST_ITEM_VALUE
                            )

                            val id =
                                AudioPlayerService.HUNGAMA_PODCAST_DETAIL_ROOT + "_" + it?.data?.id

                            val libraryMediaItem = MediaItem.Builder()
                                .setMediaId(id)
                                .setMediaMetadata(
                                    MediaMetadata.Builder()
                                        .setTitle(it?.data?.title)
                                        .setDescription(it?.data?.subTitle)
                                        .setFolderType(MediaMetadata.FOLDER_TYPE_PLAYLISTS)
                                        .setArtworkUri(Uri.parse(it?.data?.image))
                                        .setIsPlayable(false)
                                        .build()
                                )
                                .build()
                            AudioPlayerService.podcastBucketWiseList.add(libraryMediaItem)


                            CommonUtils.setLog(
                                "ChannelHelper",
                                "getHeaderWiseListing size:${AudioPlayerService.podcastBucketWiseList?.size} updateID:${updateID} id:${it?.id}"
                            )

                        } catch (exp: Exception) {
                            exp.printStackTrace()
                        }
                    }
                    return AudioPlayerService.podcastBucketWiseList
                }
            }
            return AudioPlayerService.podcastBucketWiseList

        } else if (id.contains(AudioPlayerService.HUNGAMA_DISCOVER_ROOT)) {
            updateID = id.replace(AudioPlayerService.HUNGAMA_DISCOVER_ROOT + "_", "")
            AudioPlayerService.discoverBucketRespModel?.data?.body?.rows?.forEach {
                if (updateID.equals(it?.id)) {
                    AudioPlayerService.discoverBucketWiseList = arrayListOf<MediaItem>()
                    it?.items?.forEach {
                        try {

                            if (it?.data?.type == "21" || it?.data?.type == "34" || it?.data?.type == "77777") {


                                var tmpChannel = Channel(
                                    it?.data?.id!!,
                                    it?.data?.title!!,
                                    it?.data?.image!!,
                                    "",
                                    "" + it?.data?.type,
                                    updateID,
                                    it?.data?.subTitle!!,
                                    it?.data?.misc?.description!!
                                )
//                                serviceScope?.async { callPlayableAPICall(context, tmpChannel) }


                                CommonUtils.setLog("ChannelHelper", "callPlayableAPICall channel:${tmpChannel}")

                                AudioPlayerService.currentChannelList.add(tmpChannel)


                                val libraryMediaItem = MediaItem.Builder()
                                    .setMediaId(it?.data?.id!!)
                                    .setMediaMetadata(
                                        MediaMetadata.Builder()
                                            .setTitle(it?.data?.title)
                                            .setDescription(it?.data?.subTitle)
                                            .setFolderType(MediaMetadata.FOLDER_TYPE_PLAYLISTS)
                                            .setArtworkUri(Uri.parse(it?.data?.image))
                                            .setIsPlayable(true)
                                            .build()
                                    )
                                    .build()
                                AudioPlayerService.discoverBucketWiseList.add(libraryMediaItem)

                            } else {

                                val extras = Bundle()
                                extras.putBoolean(AudioPlayerService.CONTENT_STYLE_SUPPORTED, true)
                                extras.putInt(
                                    AudioPlayerService.CONTENT_STYLE_BROWSABLE_HINT,
                                    AudioPlayerService.CONTENT_STYLE_CATEGORY_LIST_ITEM_VALUE
                                )

                                val id =
                                    AudioPlayerService.HUNGAMA_DISCOVER_DETAIL_ROOT + "_" + it?.data?.id

                                val libraryMediaItem = MediaItem.Builder()
                                    .setMediaId(id)
                                    .setMediaMetadata(
                                        MediaMetadata.Builder()
                                            .setTitle(it?.data?.title)
                                            .setDescription(it?.data?.subTitle)
                                            .setFolderType(MediaMetadata.FOLDER_TYPE_PLAYLISTS)
                                            .setArtworkUri(Uri.parse(it?.data?.image))
                                            .setIsPlayable(false)
                                            .build()
                                    )
                                    .build()
                                AudioPlayerService.discoverBucketWiseList.add(libraryMediaItem)

                                var channel = Channel(
                                    it?.data?.id!!,
                                    it?.data?.title!!,
                                    it?.data?.image!!,
                                    "",
                                    "" + it?.data?.type,
                                    updateID,
                                    it?.data?.subTitle!!,
                                    it?.data?.misc?.description!!
                                )
                                AudioPlayerService.discoverBucketWiseChannelList.add(channel)
                            }


                        } catch (exp: Exception) {
                            exp.printStackTrace()
                        }
                    }
                    return AudioPlayerService.discoverBucketWiseList

                }
            }
            return AudioPlayerService.discoverBucketWiseList
        }


        CommonUtils.setLog("TAG", "ChannelHelper: getPodcastDetail after id:${updateID}")

        return AudioPlayerService.discoverBucketWiseList
    }

    fun getPodcastDetailMain(
        context: Context,
        id: String
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        CommonUtils.setLog("TAG", "ChannelHelper 1: getDiscoverDetail before id:${id}")

        var updateID = findRealID(id)

        CommonUtils.setLog("TAG", "ChannelHelper 2: getDiscoverDetail after id:${updateID}")

        val url = "https://page.api.hungama.com/v2/page/content/${updateID}/podcast/detail/"
        return serviceScope.future {
            getPodcastDetail(context,updateID, url)
        }

    }

    fun getPlaylistDetailMain(
        context: Context,
        id: String
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        CommonUtils.setLog("TAG", "ChannelHelper 1: getDiscoverDetail before id:${id}")

        var updateID = findRealID(id)

        CommonUtils.setLog("TAG", "ChannelHelper 2: getDiscoverDetail after id:${updateID}")

        var url="https://cpage.api.hungama.com/v2/page/content/" + updateID + "/playlist/detail"
        return serviceScope.future {
            getChartAndPlaylistData(context,updateID, url)
        }

    }

    fun getAlbumDetailMain(
        context: Context,
        id: String
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        CommonUtils.setLog("TAG", "ChannelHelper 1: getAlbumDetailMain before id:${id}")

        var updateID = findRealID(id)

        CommonUtils.setLog("TAG", "ChannelHelper 2: getAlbumDetailMain after id:${updateID}")

        var url="https://cpage.api.hungama.com/v2/page/content/" + updateID + "/album/detail"
        return serviceScope.future {
            getAlbumDetailNew(context,updateID, url)
//            getAlbumDetail(context,updateID, url)
        }

    }

    fun getArtistDetailMain(
        context: Context,
        id: String
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        CommonUtils.setLog("TAG", "ChannelHelper 1: getArtistDetailMain before id:${id}")

        var updateID = findRealID(id)

        CommonUtils.setLog("TAG", "ChannelHelper 2: getArtistDetailMain after id:${updateID}")

        var url="https://cpage.api.hungama.com/v2/page/content/" + updateID + "/artist/detail/version-2"
        return serviceScope.future {
            getArtistDetail(context,updateID, url)
        }

    }






    fun getDiscoverDetail(
        context: Context,
        id: String
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        CommonUtils.setLog("TAG", "ChannelHelper 1: getDiscoverDetail before id:${id}")

        var updateID = findRealID(id)

        CommonUtils.setLog("TAG", "ChannelHelper 2: getDiscoverDetail after id:${updateID}")

        var detailMediaItem: Channel? = null

        CommonUtils.setLog(
            "TAG",
            "ChannelHelper 3: getDiscoverDetail discoverBucketList size:${AudioPlayerService.discoverBucketList?.size}"
        )

        AudioPlayerService.discoverBucketWiseChannelList.forEach {
            CommonUtils.setLog(
                "TAG",
                "ChannelHelper 4: getDiscoverDetail id:${updateID} it.mediaId:${it.mediaId}"
            )

            if (updateID.equals(it.mediaId,true)) {
                detailMediaItem = it
            }
        }



        if (detailMediaItem != null) {
            var finalID=""
            var url=""
            CommonUtils.setLog(
                "TAG",
                "ChannelHelper 5: getDiscoverDetail detailMediaItem:${detailMediaItem}"
            )
                if (detailMediaItem?.type.equals(
                        "19",
                        true
                    ) || detailMediaItem?.type.equals("55555", true)
                ) {

                    finalID = findRealID(updateID)
                    url =
                        "https://cpage.api.hungama.com/v2/page/content/" + finalID + "/playlist/detail"

                    return serviceScope.future {
                        getChartAndPlaylistData(context,finalID, url)
                    }

                } else if (detailMediaItem?.type.equals("0", true)) {

                    finalID = findRealID(updateID)
                    url =
                        "https://cpage.api.hungama.com/v2/page/content/" + finalID + "/artist/detail/version-2"
                    return serviceScope.future {
                        getArtistDetail(context,finalID, url)
                    }
                } else if (detailMediaItem?.type.equals("1", true)) {
                    finalID = findRealID(updateID)
                    url = "https://cpage.api.hungama.com/v2/page/content/" + updateID + "/album/detail"
                    return serviceScope.future {
                        getAlbumDetailNew(context,finalID, url)
                    }
                } else if (detailMediaItem?.type.equals(
                        "109",
                        true
                    ) || detailMediaItem?.type.equals("110", true)
                ) {
                    finalID = findRealID(updateID)
                    url = "https://page.api.hungama.com/v2/page/content/${updateID}/podcast/detail/"
                    return serviceScope.future {
                        getPodcastDetail(context,finalID, url)
                    }
                }else{
                    return Futures.immediateFuture(
                        LibraryResult.ofItemList(
                            ImmutableList.of(),
                            MediaLibraryService.LibraryParams.Builder().build()))
                }

        }else{
            return Futures.immediateFuture(
                LibraryResult.ofItemList(
                    ImmutableList.of(),
                    MediaLibraryService.LibraryParams.Builder().build()))
        }
    }

//    fun callPlayableAPICall(context: Context, channel: Channel) {
//
//        serviceScope?.async{
//            val dataManger = DataManager.getInstance(context)
//
//            var url =
//                "https://curls.api.hungama.com/v1/content/${channel?.mediaId}/url/playable?quality=A&contentType=4&certificate=widevine"
//            CommonUtils.setLog("TAG", "ChannelHelper: callPlayableAPICall url:${url} mediaId: ${channel?.mediaId}")
//
//            dataManger?.getVolleyRequest(context, url, JSONObject(), object : DataValues {
//                override fun setJsonDataResponse(response: JSONObject?) {
//                    try {
//                        val playableContentModel = Gson().fromJson<PlayableContentModel>(
//                            response.toString(),
//                            PlayableContentModel::class.java
//                        ) as PlayableContentModel
//                        CommonUtils.setLog(
//                            "ChannelHelper",
//                            "getPodcastDetail: playableContentModel:${playableContentModel}"
//                        )
//
//                        if(!playableContentModel?.data?.body?.data?.url?.playable?.isNullOrEmpty()){
//                            AudioPlayerService.currentChannelList?.forEach {
//                                if (it?.mediaId == channel.mediaId) {
//                                    setEventModelDataAppLevel(playableContentModel,4)
//                                    it?.mediaURL =
//                                        playableContentModel?.data?.body?.data?.url?.playable?.get(0)?.data
//                                    CommonUtils.setLog(
//                                        "ChannelHelper",
//                                        "getPodcastDetail url:${
//                                            playableContentModel?.data?.body?.data?.url?.playable?.get(
//                                                0
//                                            )?.data
//                                        } mediaId:${channel.mediaId} mediaId:${it?.mediaId}"
//                                    )
//                                    return@forEach
//                                }
//                            }
//                        }
//
//
//
//
//
//                    } catch (exp: Exception) {
//                        exp.printStackTrace()
//                    }
//                }
//
//                override fun setVolleyError(volleyError: VolleyError?) {
//                    volleyError?.printStackTrace()
//                    CommonUtils.setLog("ChannelHelper", "getPodcastDetail: volleyError:${volleyError}")
//                }
//
//            })
//        }
//
//    }

    fun getContentTypeDetailName(channel: Channel?): String {
        val contentTypeId=channel?.mediaId
        if (contentTypeId.equals("0", true)) {
            return "Artist"
        } else if (contentTypeId.equals("1", true)) {
            return "Audio Album"
        } else if (contentTypeId.equals("2", true)) {
            return "Music Video Album"
        } else if (contentTypeId.equals("3", true)) {
            return "Image Album"
        } else if (contentTypeId.equals("4", true) || contentTypeId.equals(
                "65",
                true
            ) || contentTypeId.equals("66", true)
        ) {
            return "Movie"
        } else if (contentTypeId.equals("93", true)) {
            return "Short Films"
        } else if (contentTypeId.equals("15", true)) {
            return "Collection"
        } else if (contentTypeId.equals("19", true)) {
            return "Chart"
        } else if (contentTypeId.equals("20", true)) {
            return "Live event"
        } else if (contentTypeId.equals("21", true)) {
            return "Music"
        } else if (contentTypeId.equals("22", true) || contentTypeId.equals(
                "53",
                true
            ) || contentTypeId.equals("88888", true)
        ) {
            return "Music Video"
        } else if (contentTypeId.equals("25", true)) {
            return "Category Video"
        } else if (contentTypeId.equals("51", true)) {
            return "Movie Videos"
        } else if (contentTypeId.equals("52", true)) {
            return "Events and Broadcasts Album"
        } else if (contentTypeId.equals("96", true) || contentTypeId.equals(
                "97",
                true
            ) || contentTypeId.equals("98", true) || contentTypeId.equals(
                "102",
                true
            ) || contentTypeId.equals("107", true)
        ) {
            return "TV Show"
        } else if (contentTypeId.equals("109", true)) {
            return "Podcast"
        } else if (contentTypeId.equals("110", true)) {
            return "Podcast Episode"
        } else if (contentTypeId.equals("55555", true)) {
            return "Playlist"
        } else {
            return ""
        }
    }

    suspend fun getChartAndPlaylistData(context: Context, finalID:String, url: String) =
        withContext(Dispatchers.IO) {
            suspendCancellableCoroutine<MutableList<MediaItem>> { continuation ->
                CommonUtils.setLog("TAG", "ChannelHelper 6: url:${url}")
                AudioPlayerService.songList = mutableListOf<MediaItem>()

                CommonUtils.setLog("TAG", "ChannelHelper 7: url:${url}")


                val dataManger = DataManager.getInstance(context)


                dataManger?.getVolleyRequestAuto(context, url, JSONObject(), object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        try {
                            val homeModel = Gson().fromJson<DetailDynamicModel>(
                                response.toString(),
                                DetailDynamicModel::class.java
                            ) as DetailDynamicModel
                            CommonUtils.setLog(
                                "ChannelHelper",
                                "getChartDetail 8: rows size:${homeModel?.data?.body?.rows?.size}"
                            )



                            homeModel?.data?.body?.rows?.forEach {
                                var model = it

                                var channel = Channel(
                                    model.data.id,
                                    it?.data?.title!!,
                                    model.data.image,
                                    "",
                                    "" + model.data?.type,
                                    finalID,
                                    it.data.subtitle,
                                    it.data.misc.description
                                )

//                                callPlayableAPICall(context, channel)

                                AudioPlayerService.currentChannelList.add(channel)


                                val libraryMediaItem = MediaItem.Builder()
                                    .setMediaId(model.data.id)
                                    .setMimeType(MimeTypes.AUDIO_MPEG)
                                    .setMediaMetadata(
                                        MediaMetadata.Builder()
                                            .setTitle(model.data?.title)
                                            .setDescription(model.data?.subtitle)
                                            .setFolderType(MediaMetadata.FOLDER_TYPE_ALBUMS)
                                            .setIsPlayable(true)
                                            .setArtworkUri(Uri.parse(model.data?.image))
                                            .build()
                                    )
                                    .build()
                                AudioPlayerService.songList.add(libraryMediaItem)



                                CommonUtils.setLog(
                                    "ChannelHelper",
                                    "ChannelHelper 9 getChartDetail model:${model}"
                                )
                            }
                            CommonUtils.setLog(
                                "ChannelHelper",
                                "ChannelHelper 10 getChartDetail songList?.size:${AudioPlayerService.songList?.size}"
                            )
                            if (AudioPlayerService.songList?.size!! > 0) {
                                continuation.resume(AudioPlayerService.songList)

                                CommonUtils.setLog(
                                    "ChannelHelper",
                                    "ChannelHelper 11 getChartDetail songList?.size:${AudioPlayerService.songList?.size}"
                                )

                            }


                        } catch (exp: Exception) {
                            exp.printStackTrace()

                        }
                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
                        CommonUtils.setLog(
                            "ChannelHelper",
                            "getChartDetail: volleyError:${volleyError}"
                        )
                        continuation.resume(AudioPlayerService.songList)
                    }

                })


                continuation?.invokeOnCancellation {
                    CommonUtils.setLog(
                        "TAG",
                        "ChannelHelper 12: getChartDetail before id:${it?.message}"
                    )
                }


            }
            return@withContext LibraryResult.ofItemList(
                AudioPlayerService.songList!!,
                MediaLibraryService.LibraryParams.Builder().build()
            )
        }

    suspend fun getArtistDetail(context: Context, finalID:String, url: String) =
        withContext(Dispatchers.IO) {
            suspendCancellableCoroutine<MutableList<MediaItem>> { continuation ->
                CommonUtils.setLog("TAG", "ChannelHelper 6: url:${url}")
                AudioPlayerService.songList = mutableListOf<MediaItem>()

                CommonUtils.setLog("TAG", "ChannelHelper 7: url:${url}")


                val dataManger = DataManager.getInstance(context)


                dataManger?.getVolleyRequestAuto(context, url, JSONObject(), object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        try {
                            val homeModel = Gson().fromJson<DetailDynamicModel>(
                                response.toString(),
                                DetailDynamicModel::class.java
                            ) as DetailDynamicModel
                            CommonUtils.setLog(
                                "ChannelHelper",
                                "getChartDetail 8: rows size:${homeModel?.data?.body?.rows?.size}"
                            )

                            homeModel?.data?.body?.recomendation?.get(0)?.items?.forEach {
                                var model = it

                                var channel = Channel(
                                    model?.data?.id!!,
                                    it?.data?.title!!,
                                    model?.data?.image!!,
                                    "",
                                    "" + model?.data?.type!!,
                                    finalID,
                                    model?.data?.subTitle!!,
                                    it?.data?.misc?.description!!
                                )
//                                callPlayableAPICall(context, channel)

                                CommonUtils.setLog("ChannelHelper", "callPlayableAPICall channel:${channel}")

                                AudioPlayerService.currentChannelList.add(channel)


                                val libraryMediaItem = MediaItem.Builder()
                                    .setMediaId(model?.data?.id!!)
                                    .setMediaMetadata(
                                        MediaMetadata.Builder()
                                            .setTitle(model?.data?.title)
                                            .setDescription(model?.data?.subTitle)
                                            .setArtworkUri(Uri.parse(model?.data?.image))
                                            .setFolderType(MediaMetadata.FOLDER_TYPE_ALBUMS)
                                            .setIsPlayable(true)
                                            .build()
                                    )
                                    .build()

                                AudioPlayerService.songList.add(libraryMediaItem)

                                CommonUtils.setLog("ChannelHelper", "getArtistDetail model:${model}")
                            }
                            CommonUtils.setLog(
                                "ChannelHelper",
                                "ChannelHelper 10 getChartDetail songList?.size:${AudioPlayerService.songList?.size}"
                            )
                            if (AudioPlayerService.songList?.size!! > 0) {
                                continuation.resume(AudioPlayerService.songList)

                                CommonUtils.setLog(
                                    "ChannelHelper",
                                    "ChannelHelper 11 getChartDetail songList?.size:${AudioPlayerService.songList?.size}"
                                )

                            }


                        } catch (exp: Exception) {
                            exp.printStackTrace()

                        }
                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
                        CommonUtils.setLog(
                            "ChannelHelper",
                            "getChartDetail: volleyError:${volleyError}"
                        )
                        continuation.resume(AudioPlayerService.songList)
                    }

                })


                continuation?.invokeOnCancellation {
                    CommonUtils.setLog(
                        "TAG",
                        "ChannelHelper 12: getChartDetail before id:${it?.message}"
                    )
                }


            }
            return@withContext LibraryResult.ofItemList(
                AudioPlayerService.songList!!,
                MediaLibraryService.LibraryParams.Builder().build()
            )
        }

    suspend fun getAlbumDetailNew(context: Context, finalID:String, url: String) =
        withContext(Dispatchers.IO) {
            suspendCancellableCoroutine<MutableList<MediaItem>> { continuation ->
                CommonUtils.setLog("TAG", "ChannelHelper 6: url:${url}")
                AudioPlayerService.songList = mutableListOf<MediaItem>()

                CommonUtils.setLog("TAG", "ChannelHelper 7: url:${url}")


                val dataManger = DataManager.getInstance(context)


                dataManger?.getVolleyRequestAuto(context, url, JSONObject(), object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        try {
                            val homeModel = Gson().fromJson<DetailDynamicModel>(
                                response.toString(),
                                DetailDynamicModel::class.java
                            ) as DetailDynamicModel
                            CommonUtils.setLog(
                                "ChannelHelper",
                                "getChartDetail 8: rows size:${homeModel?.data?.body?.rows?.size}"
                            )



                            homeModel?.data?.body?.rows?.forEach {
                                var model = it

                                var channel = Channel(
                                    model.data.id,
                                    it?.data?.title!!,
                                    model.data.image,
                                    "",
                                    "" + model.data.type,
                                    finalID,
                                    it.data.subtitle,
                                    it.data.misc.description
                                )

//                                callPlayableAPICall(context, channel)

                                AudioPlayerService.currentChannelList.add(channel)


                                val libraryMediaItem = MediaItem.Builder()
                                    .setMediaId(model.data.id)
                                    .setMimeType(MimeTypes.AUDIO_MPEG)
                                    .setMediaMetadata(
                                        MediaMetadata.Builder()
                                            .setTitle(model?.data?.title)
                                            .setDisplayTitle(model?.data?.title)
                                            .setArtist(model?.data?.subtitle)
                                            .setDescription(model?.data?.subtitle)
                                            .setFolderType(MediaMetadata.FOLDER_TYPE_NONE)
                                            .setIsPlayable(true)
                                            .setArtworkUri(Uri.parse(model?.data?.image))
                                            .build()
                                    )
                                    .build()
                                AudioPlayerService.songList.add(libraryMediaItem)



                                CommonUtils.setLog(
                                    "ChannelHelper",
                                    "ChannelHelper 9 getChartDetail model:${model}"
                                )
                            }
                            CommonUtils.setLog(
                                "ChannelHelper",
                                "ChannelHelper 10 getChartDetail songList?.size:${AudioPlayerService.songList?.size}"
                            )
                            if (AudioPlayerService.songList?.size!! > 0) {
                                continuation.resume(AudioPlayerService.songList)

                                CommonUtils.setLog(
                                    "ChannelHelper",
                                    "ChannelHelper 11 getChartDetail songList?.size:${AudioPlayerService.songList?.size}"
                                )

                            }


                        } catch (exp: Exception) {
                            exp.printStackTrace()

                        }
                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
                        CommonUtils.setLog(
                            "ChannelHelper",
                            "getChartDetail: volleyError:${volleyError}"
                        )
                        continuation.resume(AudioPlayerService.songList)
                    }

                })


                continuation?.invokeOnCancellation {
                    CommonUtils.setLog(
                        "TAG",
                        "ChannelHelper 12: getChartDetail before id:${it?.message}"
                    )
                }


            }
            return@withContext LibraryResult.ofItemList(
                AudioPlayerService.songList!!,
                MediaLibraryService.LibraryParams.Builder().build()
            )
        }


    suspend fun getPodcastDetail(context: Context, finalID:String, url: String) =
        withContext(Dispatchers.IO) {
            suspendCancellableCoroutine<MutableList<MediaItem>> { continuation ->
                CommonUtils.setLog("TAG", "ChannelHelper 6: url:${url}")
                AudioPlayerService.songList = mutableListOf<MediaItem>()

                CommonUtils.setLog("TAG", "ChannelHelper 7: url:${url}")


                val dataManger = DataManager.getInstance(context)


                dataManger?.getVolleyRequestAuto(context, url, JSONObject(), object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        try {
                            val homeModel = Gson().fromJson<DetailDynamicModel>(
                                response.toString(),
                                DetailDynamicModel::class.java
                            ) as DetailDynamicModel
                            CommonUtils.setLog(
                                "ChannelHelper",
                                "getChartDetail 8: rows size:${homeModel?.data?.body?.rows?.size}"
                            )

                            homeModel.data.body.rows.get(0).data.misc.tracks.forEach {
                                var model = it

                                var channel = Channel(model.data.id, it.data.title, model.data.image, "",""+model.data.type,finalID,it.data.subtitle,it.data.misc.description)
//                                callPlayableAPICall(context, channel)

                                CommonUtils.setLog("ChannelHelper", "callPlayableAPICall channel:${channel}")

                                AudioPlayerService.currentChannelList.add(channel)

                                val podcastTrackMediaItem = MediaItem.Builder()
                                    .setMediaId(model.data.id)
                                    .setMediaMetadata(
                                        MediaMetadata.Builder()
                                            .setTitle(model.data.title)
                                            .setDescription(model.data.subtitle)
                                            .setArtworkUri(Uri.parse(model.data.image))
                                            .setFolderType(MediaMetadata.FOLDER_TYPE_NONE)
                                            .setIsPlayable(true)
                                            .build()
                                    )
                                    .build()
                                AudioPlayerService.songList.add(
                                    podcastTrackMediaItem
                                )
                                CommonUtils.setLog("ChannelHelper", "mediaDesc:${podcastTrackMediaItem}")


                                CommonUtils.setLog("ChannelHelper", "getPodcastDetail model:${model}")
                            }
                            CommonUtils.setLog(
                                "ChannelHelper",
                                "ChannelHelper 10 getChartDetail songList?.size:${AudioPlayerService.songList?.size}"
                            )
                            if (AudioPlayerService.songList?.size!! > 0) {
                                continuation.resume(AudioPlayerService.songList)

                                CommonUtils.setLog(
                                    "ChannelHelper",
                                    "ChannelHelper 11 getChartDetail songList?.size:${AudioPlayerService.songList?.size}"
                                )

                            }


                        } catch (exp: Exception) {
                            exp.printStackTrace()

                        }
                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
                        CommonUtils.setLog(
                            "ChannelHelper",
                            "getChartDetail: volleyError:${volleyError}"
                        )
                        continuation.resume(AudioPlayerService.songList)

                    }

                })


                continuation?.invokeOnCancellation {
                    CommonUtils.setLog(
                        "TAG",
                        "ChannelHelper 12: getChartDetail before id:${it?.message}"
                    )
                }


            }
            return@withContext LibraryResult.ofItemList(
                AudioPlayerService.songList!!,
                MediaLibraryService.LibraryParams.Builder().build()
            )
        }
    fun findRealID(str:String):String {
        var updateID = str
        if (str.contains(AudioPlayerService.HUNGAMA_DISCOVER_DETAIL_ROOT)) {
            updateID = str.replace(AudioPlayerService.HUNGAMA_DISCOVER_DETAIL_ROOT + "_", "")
        } else if (str.contains(AudioPlayerService.HUNGAMA_PODCAST_DETAIL_ROOT)) {
            updateID = str.replace(AudioPlayerService.HUNGAMA_PODCAST_DETAIL_ROOT + "_", "")
        } else if (str.contains(AudioPlayerService.HUNGAMA_PODCAST_ROOT)) {
            updateID = str.replace(AudioPlayerService.HUNGAMA_PODCAST_ROOT + "_", "")
        } else if (str.contains(AudioPlayerService.HUNGAMA_DISCOVER_ROOT)) {
            updateID = str.replace(AudioPlayerService.HUNGAMA_DISCOVER_ROOT + "_", "")
        } else if (str.contains(AudioPlayerService.HUNGAMA_LIBRARY_ALBUM_DETAIL_ROOT)) {
            updateID = str.replace(AudioPlayerService.HUNGAMA_LIBRARY_ALBUM_DETAIL_ROOT + "_", "")
        } else if (str.contains(AudioPlayerService.HUNGAMA_LIBRARY_PLAYLIST_DETAIL_ROOT)) {
            updateID =
                str.replace(AudioPlayerService.HUNGAMA_LIBRARY_PLAYLIST_DETAIL_ROOT + "_", "")
        } else if (str.contains(AudioPlayerService.HUNGAMA_LIBRARY_ARTIST_DETAIL_ROOT)) {
            updateID = str.replace(AudioPlayerService.HUNGAMA_LIBRARY_ARTIST_DETAIL_ROOT + "_", "")
        }
        return updateID
    }



    fun createLibraryListing(context: Context): List<MediaItem> =runBlocking {
        AudioPlayerService.libListing = mutableListOf<MediaItem>()

        if (AudioPlayerService.libListing?.size!! > 0) {
            return@runBlocking AudioPlayerService.libListing
        }
        // Add all
//            val allMediaItem = MediaItem.Builder()
//                .setMediaId(AudioPlayerService.MEDIA_ID_LIB_ALL)
//                .setMediaMetadata(
//                    MediaMetadata.Builder()
//                        .setTitle(context.getString(R.string.title_all))
//                        .setFolderType(MediaMetadata.FOLDER_TYPE_MIXED)
//                        .setArtworkUri(Uri.parse(""))
//                        .setIsPlayable(false)
//                        .build()
//                )
//                .build()
//            AudioPlayerService.libListing.add(allMediaItem)

//        // Add downloaded song
//            val downloadUri = getUriToDrawable(context, R.drawable.download_img)
//            val downloadMediaItem = MediaItem.Builder()
//                .setMediaId(AudioPlayerService.MEDIA_ID_LIB_DOWNLOAD)
//                .setMediaMetadata(
//                    MediaMetadata.Builder()
//                        .setTitle(context.getString(R.string.title_downloaded))
//                        .setFolderType(MediaMetadata.FOLDER_TYPE_MIXED)
//                        .setArtworkUri(downloadUri)
//                        .setIsPlayable(false)
//                        .build()
//                )
//                .build()
//            AudioPlayerService.libListing.add(downloadMediaItem)

        // Add favroited song

        val favUri = getUriToDrawable(context, R.drawable.heart)
        val favMediaItem = MediaItem.Builder()
            .setMediaId(AudioPlayerService.MEDIA_ID_LIB_FAV)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(context.getString(R.string.title_favorited))
                    .setFolderType(MediaMetadata.FOLDER_TYPE_MIXED)
                    .setArtworkUri(favUri)
                    .setIsPlayable(false)
                    .build()
            )
            .build()
        AudioPlayerService.libListing.add(favMediaItem)

        // Add playlist
        val playlistUri = getUriToDrawable(context, R.drawable.ic_queue_music)
        val playlistMediaItem = MediaItem.Builder()
            .setMediaId(AudioPlayerService.MEDIA_ID_LIB_PLAYLIST)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(context.getString(R.string.title_playlist))
                    .setFolderType(MediaMetadata.FOLDER_TYPE_MIXED)
                    .setArtworkUri(playlistUri)
                    .setIsPlayable(false)
                    .build()
            )
            .build()
        AudioPlayerService.libListing.add(playlistMediaItem)

        // Add album
        val albumUri = getUriToDrawable(context, R.drawable.ic_new_album)
        val albumMediaItem = MediaItem.Builder()
            .setMediaId(AudioPlayerService.MEDIA_ID_LIB_ALBUM)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(context.getString(R.string.title_album))
                    .setFolderType(MediaMetadata.FOLDER_TYPE_MIXED)
                    .setArtworkUri(albumUri)
                    .setIsPlayable(false)
                    .build()
            )
            .build()
        AudioPlayerService.libListing.add(albumMediaItem)

        // Add podcast
        val podcastUri = getUriToDrawable(context, R.drawable.ic_bottom_news_black)
        val podcastMediaItem = MediaItem.Builder()
            .setMediaId(AudioPlayerService.MEDIA_ID_LIB_PODCAST)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(context.getString(R.string.title_podcasts))
                    .setFolderType(MediaMetadata.FOLDER_TYPE_MIXED)
                    .setArtworkUri(podcastUri)
                    .setIsPlayable(false)
                    .build()
            )
            .build()
        AudioPlayerService.libListing.add(podcastMediaItem)

        // Add artist
        val artistUri = getUriToDrawable(context, R.drawable.ic_new_artist)
        val artistMediaItem = MediaItem.Builder()
            .setMediaId(AudioPlayerService.MEDIA_ID_LIB_ARTIST)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(context.getString(R.string.title_artist))
                    .setFolderType(MediaMetadata.FOLDER_TYPE_MIXED)
                    .setArtworkUri(artistUri)
                    .setIsPlayable(false)
                    .build()
            )
            .build()
        AudioPlayerService.libListing.add(artistMediaItem)

        // Add radio
        val radioUri = getUriToDrawable(context, R.drawable.ic_bottom_news_black)
        val radioMediaItem = MediaItem.Builder()
            .setMediaId(AudioPlayerService.MEDIA_ID_LIB_RADIO)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(context.getString(R.string.title_radio))
                    .setFolderType(MediaMetadata.FOLDER_TYPE_MIXED)
                    .setArtworkUri(radioUri)
                    .setIsPlayable(false)
                    .build()
            )
            .build()
        AudioPlayerService.libListing.add(radioMediaItem)

        AudioPlayerService.libListing
    }


    fun createFavoriteListing(context: Context, favoriteChannels: List<Channel>): List<MediaItem> {
        AudioPlayerService.currentChannelList = favoriteChannels as ArrayList<Channel>

        val mediaDescListing = mutableListOf<MediaItem>()

        favoriteChannels.forEach { channel ->

            val favMediaItem = MediaItem.Builder()
                .setMediaId(channel.mediaId)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(channel.title)
                        .setArtworkUri(Uri.parse(channel.imageRes))
                        .setIsPlayable(true)
                        .build()
                )
                .build()
            mediaDescListing.add(favMediaItem)

        }

        return mediaDescListing
    }

    fun getChannelForId(mediaId: String): Channel {

        if (!AudioPlayerService.currentChannelList.isNullOrEmpty()) {
            AudioPlayerService.currentChannelList.forEach {
                if (mediaId.equals(it.mediaId)) {
                    return it
                }
            }
        }


        var tmpChannel = Channel("","","","","","","","",0)
        return tmpChannel



    }

    fun getNextMediaId(currentMediaId: String): String {
        CommonUtils.setLog("TAG", "getNextMediaId: currentMediaId:${currentMediaId}")
        AudioPlayerService.currentChannelList.indexOf(getChannelForId(currentMediaId)).let {
            return if (it == AudioPlayerService.currentChannelList.lastIndex) {
                AudioPlayerService.currentChannelList[0].mediaId
            } else {
                AudioPlayerService.currentChannelList[it + 1].mediaId
            }
        }
    }

    fun getPreviousMediaId(currentMediaId: String): String {
        CommonUtils.setLog("TAG", "getNextMediaId: getPreviousMediaId:${currentMediaId}")
        AudioPlayerService.currentChannelList.indexOf(getChannelForId(currentMediaId)).let {
            return if (it == 0) {
                AudioPlayerService.currentChannelList[AudioPlayerService.currentChannelList.lastIndex].mediaId
            } else {
                AudioPlayerService.currentChannelList[it - 1].mediaId
            }
        }
    }

    fun searchForChannelMediaId(query: String): String? {

        AudioPlayerService.currentChannelList?.forEach {
            if (it.title == query) {
                return it.mediaId
            }
        }

        return null
    }

    fun getUserRecentPlay(
        context: Context,
        parentMediaId: String
    ): MutableList<MediaItem> = runBlocking {

        if (AudioPlayerService?.recentPlayedList?.size!! > 0) {
            LibraryResult.ofItemList(
                AudioPlayerService?.recentPlayedList ?: ImmutableList.of(),
                MediaLibraryService.LibraryParams.Builder().build()
            )
            return@runBlocking AudioPlayerService?.recentPlayedList!!
        }

        val dataManger = DataManager.getInstance(context)

        var url = "https://cugc.api.hungama.com/v1/user/"+userID+"/stream"

        CommonUtils.setLog("TAG", "ChannelHelper: getUserRecentPlay url:${url}")

        dataManger?.getVolleyRequestArrayAuto(context, url, object : DataValues {
            override fun setJsonArrayDataResponse(jsonArray: JSONArray?) {
                try {
                    val continueWhereLeftModel = Gson().fromJson<ContinueWhereLeftModel>(
                        jsonArray.toString(),
                        ContinueWhereLeftModel::class.java
                    ) as ContinueWhereLeftModel




                    if(!continueWhereLeftModel?.isNullOrEmpty()!!){
                        Log.d(
                            "ChannelHelper",
                            "getUserRecentPlaySong: rows size:${continueWhereLeftModel?.size}"
                        )

                        continueWhereLeftModel?.forEach {
                            var model = it

                            var channel = Channel(model?.data?.id!!, it?.data?.title!!, model?.data?.image!!, "",""+model?.data?.type!!,"",model?.data?.subTitle!!,it?.data?.misc?.description!!)
//                            callPlayableAPICall(context, channel)

                            Log.d("ChannelHelper", "callPlayableAPICall channel:${channel}")

                            AudioPlayerService.currentChannelList.add(channel)

                            val libraryMediaItem = MediaItem.Builder()
                                .setMediaId(model?.data?.id!!)
                                .setMediaMetadata(
                                    MediaMetadata.Builder()
                                        .setTitle(model?.data?.title)
                                        .setDescription(model?.data?.subTitle)
                                        .setFolderType(MediaMetadata.FOLDER_TYPE_NONE)
                                        .setArtworkUri(Uri.parse(model?.data?.image))
                                        .setIsPlayable(true)
                                        .build()
                                )
                                .build()

                            AudioPlayerService.recentPlayedList.add(libraryMediaItem)



                            Log.d("ChannelHelper", "getUserRecentPlaySong model:${model}")
                        }

                    }



                } catch (exp: Exception) {
                    exp.printStackTrace()

                }
            }

            override fun setJsonDataResponse(response: JSONObject?) {

            }

            override fun setVolleyError(volleyError: VolleyError?) {
                volleyError?.printStackTrace()
                Log.d("ChannelHelper", "downloadJson: volleyError:${volleyError}")

            }

        })

        Log.d("ChannelHelper", "getUserRecentPlaySong recentPlayedList size:${AudioPlayerService?.recentPlayedList?.size}")

        AudioPlayerService?.recentPlayedList!!
    }

    fun getPodcastBucketList(
        context: Context,
        parentMediaId: String
    ): MutableList<MediaItem> = runBlocking {

        if (AudioPlayerService?.podcastBucketList?.size!! > 0) {
            LibraryResult.ofItemList(
                AudioPlayerService?.podcastBucketList ?: ImmutableList.of(),
                MediaLibraryService.LibraryParams.Builder().build()
            )
            return@runBlocking AudioPlayerService?.podcastBucketList!!
        }

        val dataManger = DataManager.getInstance(context)

        var url =
            "https://cpage.api.hungama.com/v1/page/podcast"
        CommonUtils.setLog("TAG", "ChannelHelper: getPodcastBucketList url:${url}")



        dataManger?.getVolleyRequestAuto(context, url, JSONObject(), object : DataValues {
            override fun setJsonDataResponse(response: JSONObject?) {
                try {
                    AudioPlayerService.podcastBucketRespModel = Gson().fromJson<HomeModel>(
                        response.toString(),
                        HomeModel::class.java
                    ) as HomeModel
                    CommonUtils.setLog(
                        "ChannelHelper",
                        "ChannelHelper: rows size:${AudioPlayerService.podcastBucketRespModel?.data?.body?.rows?.size}"
                    )


                    try {
                        AudioPlayerService?.podcastBucketList = mutableListOf<MediaItem>()
                        AudioPlayerService.podcastBucketRespModel?.data?.body?.rows?.forEach {
                            var model = it
                            if (!model?.id?.equals(
                                    "1",
                                    true
                                )!! && !model?.items?.isNullOrEmpty()!!
                            ) {


                                val id =
                                    AudioPlayerService.HUNGAMA_PODCAST_ROOT + "_" + model?.id


                                val libraryMediaItem = MediaItem.Builder()
                                    .setMediaId(id)
                                    .setMediaMetadata(
                                        MediaMetadata.Builder()
                                            .setTitle(model?.heading)
                                            .setFolderType(MediaMetadata.FOLDER_TYPE_MIXED)
                                            .setArtworkUri(Uri.parse(model?.image))
                                            .setIsPlayable(false)
                                            .build()
                                    )
                                    .build()
                                AudioPlayerService.podcastBucketList.add(libraryMediaItem)

                                CommonUtils.setLog("ChannelHelper", "mediaDesc:${libraryMediaItem}")
                            }
                        }

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        LibraryResult.ofItemList(
                            AudioPlayerService?.podcastBucketList ?: ImmutableList.of(),
                            MediaLibraryService.LibraryParams.Builder().build()
                        )
                    }

                    CommonUtils.setLog(
                        "ChannelHelper",
                        "podcastBucketListing: size:${AudioPlayerService.podcastBucketList.size}"
                    )

                } catch (exp: Exception) {
                    exp.printStackTrace()
                    LibraryResult.ofItemList(
                        AudioPlayerService?.podcastBucketList ?: ImmutableList.of(),
                        MediaLibraryService.LibraryParams.Builder().build()
                    )
                }
            }

            override fun setVolleyError(volleyError: VolleyError?) {
                volleyError?.printStackTrace()
                CommonUtils.setLog("ChannelHelper", "ChannelHelper: volleyError:${volleyError?.message}")
                LibraryResult.ofItemList(
                    AudioPlayerService?.podcastBucketList ?: ImmutableList.of(),
                    MediaLibraryService.LibraryParams.Builder().build()
                )
            }

        })
        AudioPlayerService?.podcastBucketList!!
    }

    fun getRadioList(
        context: Context,
        parentMediaId: String
    ): MutableList<MediaItem> = runBlocking {

        if (AudioPlayerService?.radioLibList?.size!! > 0) {
            LibraryResult.ofItemList(
                AudioPlayerService?.radioLibList ?: ImmutableList.of(),
                MediaLibraryService.LibraryParams.Builder().build()
            )
            return@runBlocking AudioPlayerService?.radioLibList!!
        }

        val dataManger = DataManager.getInstance(context)

        var url="https://cugc.api.hungama.com/v1/user/${userID}/1/bookmark/type?typeId=77777,34"

        CommonUtils.setLog("TAG", "ChannelHelper: getUserRecentPlay url:${url}")

        dataManger?.getVolleyRequestAuto(context, url, JSONObject(), object : DataValues {
            override fun setJsonDataResponse(response: JSONObject?) {
                try {
                    val homeModel = Gson().fromJson<DetailDynamicModel>(
                        response.toString(),
                        DetailDynamicModel::class.java
                    ) as DetailDynamicModel


                        if(!homeModel?.data?.body?.rows?.isNullOrEmpty()!!){
                            Log.d(
                                "ChannelHelper",
                                "getRadioListLibrary: rows size:${homeModel?.data?.body?.rows?.size}"
                            )

                            AudioPlayerService.radioLibList = mutableListOf<MediaItem>()

                            homeModel?.data?.body?.rows?.forEach {
                                var model = it

                                var channel = Channel(model?.data?.id!!, it?.data?.title!!, model?.data?.image!!, "",""+model?.data?.type!!,"",model?.data?.subtitle!!,it?.data?.misc?.description!!)
//                                callPlayableAPICall(context, channel)

                                Log.d("ChannelHelper", "callPlayableAPICall channel:${channel}")

                                AudioPlayerService.currentChannelList.add(channel)

                                val libraryMediaItem = MediaItem.Builder()
                                    .setMediaId(model?.data?.id!!)
                                    .setMediaMetadata(
                                        MediaMetadata.Builder()
                                            .setTitle(model?.data?.title)
                                            .setDescription(model?.data?.subtitle)
                                            .setFolderType(MediaMetadata.FOLDER_TYPE_NONE)
                                            .setArtworkUri(Uri.parse(model?.data?.image))
                                            .setIsPlayable(true)
                                            .build()
                                    )
                                    .build()

                                AudioPlayerService.radioLibList.add(libraryMediaItem)
                                Log.d("ChannelHelper", "getRadioListLibrary model:${model}")
                            }
                        }



                } catch (exp: Exception) {
                    exp.printStackTrace()

                }
            }

            override fun setVolleyError(volleyError: VolleyError?) {
                volleyError?.printStackTrace()
                Log.d("ChannelHelper", "getRadioListLibrary: volleyError:${volleyError}")
            }

        })

        Log.d("ChannelHelper", "getUserRecentPlaySong recentPlayedList size:${AudioPlayerService?.recentPlayedList?.size}")

        AudioPlayerService.radioLibList!!
    }

    fun getFavSongList(
        context: Context,
        parentMediaId: String
    ): MutableList<MediaItem> = runBlocking {

        if (AudioPlayerService?.favSongLibList?.size!! > 0) {
            LibraryResult.ofItemList(
                AudioPlayerService?.favSongLibList ?: ImmutableList.of(),
                MediaLibraryService.LibraryParams.Builder().build()
            )
            CommonUtils.setLog("TAG", "ChannelHelper: if favSongLibList size :  url:${AudioPlayerService?.favSongLibList?.size}")

            AudioPlayerService?.favSongLibList!!
        }else{

            serviceScope?.future {
                val dataManger = DataManager.getInstance(context)

                val url="https://cugc.api.hungama.com/v1/user/"+userID+"/1/bookmark/type?typeId=21,36"

                CommonUtils.setLog("TAG", "ChannelHelper: getUserRecentPlay url:${url}")

                dataManger?.getVolleyRequestAuto(context, url, JSONObject(), object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        try {
                            val homeModel = Gson().fromJson<DetailDynamicModel>(
                                response.toString(),
                                DetailDynamicModel::class.java
                            ) as DetailDynamicModel


                            if(!homeModel?.data?.body?.rows?.isNullOrEmpty()!!){
                                Log.d(
                                    "ChannelHelper",
                                    "getRadioListLibrary: rows size:${homeModel?.data?.body?.rows?.size}"
                                )

                                AudioPlayerService.favSongLibList = mutableListOf<MediaItem>()

                                homeModel?.data?.body?.rows?.forEach {
                                    var model = it

                                    var channel = Channel(model?.data?.id!!, it?.data?.title!!, model?.data?.image!!, "",""+model?.data?.type!!,"",model?.data?.subtitle!!,it?.data?.misc?.description!!)
//                                    callPlayableAPICall(context, channel)

                                    Log.d("ChannelHelper", "callPlayableAPICall channel:${channel}")

                                    AudioPlayerService.currentChannelList.add(channel)

                                    val libraryMediaItem = MediaItem.Builder()
                                        .setMediaId(model?.data?.id!!)
                                        .setMediaMetadata(
                                            MediaMetadata.Builder()
                                                .setTitle(model?.data?.title)
                                                .setDescription(model?.data?.subtitle)
                                                .setFolderType(MediaMetadata.FOLDER_TYPE_NONE)
                                                .setArtworkUri(Uri.parse(model?.data?.image))
                                                .setIsPlayable(true)
                                                .build()
                                        )
                                        .build()

                                    AudioPlayerService.favSongLibList.add(libraryMediaItem)
                                    Log.d("ChannelHelper", "getRadioListLibrary model:${model}")
                                }
                            }



                        } catch (exp: Exception) {
                            exp.printStackTrace()

                        }
                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
                        Log.d("ChannelHelper", "getRadioListLibrary: volleyError:${volleyError}")
                    }

                })

            }

            Log.d("ChannelHelper", "getUserRecentPlaySong recentPlayedList size:${AudioPlayerService?.recentPlayedList?.size}")

            AudioPlayerService.favSongLibList!!
        }

    }

    fun getFavPodcastList(
        context: Context,
        parentMediaId: String
    ): MutableList<MediaItem> = runBlocking {

        if (AudioPlayerService?.podcastLibraryList?.size!! > 0) {
            LibraryResult.ofItemList(
                AudioPlayerService?.podcastLibraryList ?: ImmutableList.of(),
                MediaLibraryService.LibraryParams.Builder().build()
            )
            AudioPlayerService?.podcastLibraryList!!
        }else{
            serviceScope?.future {
                val dataManger = DataManager.getInstance(context)
                var url =
                    "https://cugc.api.hungama.com/v1/user/" + userID + "/5/content/follow/type?typeId=109"
                CommonUtils.setLog("TAG", "ChannelHelper: getPodcastBucketList url:${url}")



                dataManger?.getVolleyRequestAuto(context, url, JSONObject(), object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        try {
                            val podcastBucketRespModel = Gson().fromJson<HomeModel>(
                                response.toString(),
                                HomeModel::class.java
                            ) as HomeModel
                            CommonUtils.setLog(
                                "ChannelHelper",
                                "ChannelHelper: rows size:${podcastBucketRespModel?.data?.body?.rows?.size}"
                            )


                            try {
                                AudioPlayerService?.podcastLibraryList = mutableListOf<MediaItem>()
                                podcastBucketRespModel?.data?.body?.rows?.forEach {
                                    var model = it
                                    val id = HUNGAMA_PODCAST_DETAIL_ROOT + "_" + model?.data?.id

                                    val libraryMediaItem = MediaItem.Builder()
                                        .setMediaId(id)
                                        .setMediaMetadata(
                                            MediaMetadata.Builder()
                                                .setTitle(model?.data?.title)
                                                .setSubtitle(model?.data?.subTitle)
                                                .setDescription(model?.data?.misc?.description)
                                                .setFolderType(MediaMetadata.FOLDER_TYPE_MIXED)
                                                .setArtworkUri(Uri.parse(model?.data?.image))
                                                .setIsPlayable(false)
                                                .build()
                                        )
                                        .build()
                                    AudioPlayerService.podcastLibraryList.add(libraryMediaItem)

                                    CommonUtils.setLog("ChannelHelper", "mediaDesc:${libraryMediaItem}")
                                }

                            } catch (exp: Exception) {
                                exp.printStackTrace()
                                LibraryResult.ofItemList(
                                    AudioPlayerService?.podcastLibraryList ?: ImmutableList.of(),
                                    MediaLibraryService.LibraryParams.Builder().build()
                                )
                            }

                            CommonUtils.setLog(
                                "ChannelHelper",
                                "podcastBucketListing: size:${AudioPlayerService.podcastLibraryList.size}"
                            )

                        } catch (exp: Exception) {
                            exp.printStackTrace()
                            LibraryResult.ofItemList(
                                AudioPlayerService?.podcastLibraryList ?: ImmutableList.of(),
                                MediaLibraryService.LibraryParams.Builder().build()
                            )
                        }
                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
                        CommonUtils.setLog("ChannelHelper", "ChannelHelper: volleyError:${volleyError?.message}")
                        LibraryResult.ofItemList(
                            AudioPlayerService?.podcastLibraryList ?: ImmutableList.of(),
                            MediaLibraryService.LibraryParams.Builder().build()
                        )
                    }

                })
            }
            AudioPlayerService?.podcastLibraryList!!
        }
    }


    fun getFavPlaylistList(
        context: Context,
        parentMediaId: String
    ): MutableList<MediaItem> = runBlocking {

        if (AudioPlayerService?.playListLibraryList?.size!! > 0) {
            LibraryResult.ofItemList(
                AudioPlayerService?.playListLibraryList ?: ImmutableList.of(),
                MediaLibraryService.LibraryParams.Builder().build()
            )
            CommonUtils.setLog("TAG", "ChannelHelper: if favSongLibList size :  url:${AudioPlayerService?.playListLibraryList?.size}")
            AudioPlayerService?.playListLibraryList!!
        }else{
            serviceScope?.future {

                val dataManger = DataManager.getInstance(context)
                var url = "https://cugc.api.hungama.com/v1/user/"+userID+"/1/bookmark/type?typeId=55555,99999"
                CommonUtils.setLog("TAG", "ChannelHelper: getFavPlaylistList url:${url}")



                dataManger?.getVolleyRequestAuto(context, url, JSONObject(), object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        try {
                            val homeModel = Gson().fromJson<HomeModel>(
                                response.toString(),
                                HomeModel::class.java
                            ) as HomeModel
                            CommonUtils.setLog(
                                "ChannelHelper",
                                "ChannelHelper: rows size:${homeModel?.data?.body?.rows?.size}"
                            )


                            try {
                                AudioPlayerService?.playListLibraryList = mutableListOf<MediaItem>()
                                homeModel?.data?.body?.rows?.forEach {
                                    var model = it
                                    val id = HUNGAMA_LIBRARY_PLAYLIST_DETAIL_ROOT + "_" + model?.data?.id



                                    val libraryMediaItem = MediaItem.Builder()
                                        .setMediaId(id)
                                        .setMediaMetadata(
                                            MediaMetadata.Builder()
                                                .setTitle(model?.data?.title)
                                                .setSubtitle(model?.data?.subTitle)
                                                .setDescription(model?.data?.misc?.description)
                                                .setFolderType(MediaMetadata.FOLDER_TYPE_MIXED)
                                                .setArtworkUri(Uri.parse(model?.data?.image))
                                                .setIsPlayable(false)
                                                .build()
                                        )
                                        .build()
                                    AudioPlayerService.playListLibraryList.add(libraryMediaItem)

                                    CommonUtils.setLog("ChannelHelper", "mediaDesc:${libraryMediaItem}")
                                }

                            } catch (exp: Exception) {
                                exp.printStackTrace()
                                LibraryResult.ofItemList(
                                    AudioPlayerService?.playListLibraryList ?: ImmutableList.of(),
                                    MediaLibraryService.LibraryParams.Builder().build()
                                )
                            }

                            CommonUtils.setLog(
                                "ChannelHelper",
                                "podcastBucketListing: size:${AudioPlayerService.playListLibraryList.size}"
                            )

                        } catch (exp: Exception) {
                            exp.printStackTrace()
                            LibraryResult.ofItemList(
                                AudioPlayerService?.playListLibraryList ?: ImmutableList.of(),
                                MediaLibraryService.LibraryParams.Builder().build()
                            )
                        }
                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
                        CommonUtils.setLog("ChannelHelper", "ChannelHelper: volleyError:${volleyError?.message}")
                        LibraryResult.ofItemList(
                            AudioPlayerService?.playListLibraryList ?: ImmutableList.of(),
                            MediaLibraryService.LibraryParams.Builder().build()
                        )
                    }

                })
            }
            AudioPlayerService?.playListLibraryList!!
        }


    }

    fun getFavAlbumList(
        context: Context,
        parentMediaId: String
    ): MutableList<MediaItem> = runBlocking {

        if (AudioPlayerService?.albumLibraryList?.size!! > 0) {
            LibraryResult.ofItemList(
                AudioPlayerService?.albumLibraryList ?: ImmutableList.of(),
                MediaLibraryService.LibraryParams.Builder().build()
            )
            AudioPlayerService?.albumLibraryList!!
        }else{
            serviceScope?.future {
                val dataManger = DataManager.getInstance(context)

                var url = "https://cugc.api.hungama.com/v1/user/${userID}/1/bookmark/type?typeId=1"
                CommonUtils.setLog("TAG", "ChannelHelper: getFavAlbumList url:${url}")



                dataManger?.getVolleyRequestAuto(context, url, JSONObject(), object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        try {
                            val homeModel = Gson().fromJson<HomeModel>(
                                response.toString(),
                                HomeModel::class.java
                            ) as HomeModel
                            CommonUtils.setLog(
                                "ChannelHelper",
                                "ChannelHelper: rows size:${homeModel?.data?.body?.rows?.size}"
                            )


                            try {
                                AudioPlayerService?.albumLibraryList = mutableListOf<MediaItem>()
                                homeModel?.data?.body?.rows?.forEach {
                                    var model = it
                                    val id = HUNGAMA_LIBRARY_ALBUM_DETAIL_ROOT + "_" + model?.data?.id

                                    val libraryMediaItem = MediaItem.Builder()
                                        .setMediaId(id)
                                        .setMediaMetadata(
                                            MediaMetadata.Builder()
                                                .setTitle(model?.data?.title)
                                                .setSubtitle(model?.data?.subTitle)
                                                .setDescription(model?.data?.misc?.description)
                                                .setFolderType(MediaMetadata.FOLDER_TYPE_MIXED)
                                                .setArtworkUri(Uri.parse(model?.data?.image))
                                                .setIsPlayable(false)
                                                .build()
                                        )
                                        .build()
                                    AudioPlayerService.albumLibraryList.add(libraryMediaItem)

                                    CommonUtils.setLog("ChannelHelper", "mediaDesc:${libraryMediaItem}")
                                }

                            } catch (exp: Exception) {
                                exp.printStackTrace()
                                LibraryResult.ofItemList(
                                    AudioPlayerService?.albumLibraryList ?: ImmutableList.of(),
                                    MediaLibraryService.LibraryParams.Builder().build()
                                )
                            }

                            CommonUtils.setLog(
                                "ChannelHelper",
                                "podcastBucketListing: size:${AudioPlayerService.albumLibraryList.size}"
                            )

                        } catch (exp: Exception) {
                            exp.printStackTrace()
                            LibraryResult.ofItemList(
                                AudioPlayerService?.albumLibraryList ?: ImmutableList.of(),
                                MediaLibraryService.LibraryParams.Builder().build()
                            )
                        }
                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
                        CommonUtils.setLog("ChannelHelper", "ChannelHelper: volleyError:${volleyError?.message}")
                        LibraryResult.ofItemList(
                            AudioPlayerService?.albumLibraryList ?: ImmutableList.of(),
                            MediaLibraryService.LibraryParams.Builder().build()
                        )
                    }

                })
            }
            AudioPlayerService?.albumLibraryList!!
        }


    }

    fun getFavArtistList(
        context: Context,
        parentMediaId: String
    ): MutableList<MediaItem> = runBlocking {

        if (AudioPlayerService?.artistLibraryList?.size!! > 0) {
            LibraryResult.ofItemList(
                AudioPlayerService?.artistLibraryList ?: ImmutableList.of(),
                MediaLibraryService.LibraryParams.Builder().build()
            )
            AudioPlayerService?.artistLibraryList!!
        }else{
            serviceScope?.future {

                val dataManger = DataManager.getInstance(context)
                var url = "https://cugc.api.hungama.com/v1/user/"+userID+"/5/content/follow/type?typeId=0"
                CommonUtils.setLog("TAG", "ChannelHelper: getFavArtistList url:${url}")



                dataManger?.getVolleyRequestAuto(context, url, JSONObject(), object : DataValues {
                    override fun setJsonDataResponse(response: JSONObject?) {
                        try {
                            val homeModel = Gson().fromJson<HomeModel>(
                                response.toString(),
                                HomeModel::class.java
                            ) as HomeModel
                            CommonUtils.setLog(
                                "ChannelHelper",
                                "ChannelHelper: rows size:${homeModel?.data?.body?.rows?.size}"
                            )


                            try {
                                AudioPlayerService?.artistLibraryList = mutableListOf<MediaItem>()
                                homeModel?.data?.body?.rows?.forEach {
                                    var model = it
                                    val id = HUNGAMA_LIBRARY_ARTIST_DETAIL_ROOT + "_" + model?.data?.id



                                    val libraryMediaItem = MediaItem.Builder()
                                        .setMediaId(id)
                                        .setMediaMetadata(
                                            MediaMetadata.Builder()
                                                .setTitle(model?.data?.title)
                                                .setSubtitle(model?.data?.subTitle)
                                                .setDescription(model?.data?.misc?.description)
                                                .setFolderType(MediaMetadata.FOLDER_TYPE_MIXED)
                                                .setArtworkUri(Uri.parse(model?.data?.image))
                                                .setIsPlayable(false)
                                                .build()
                                        )
                                        .build()
                                    AudioPlayerService.artistLibraryList.add(libraryMediaItem)

                                    CommonUtils.setLog("ChannelHelper", "mediaDesc:${libraryMediaItem}")
                                }

                            } catch (exp: Exception) {
                                exp.printStackTrace()
                                LibraryResult.ofItemList(
                                    AudioPlayerService?.artistLibraryList ?: ImmutableList.of(),
                                    MediaLibraryService.LibraryParams.Builder().build()
                                )
                            }

                            CommonUtils.setLog(
                                "ChannelHelper",
                                "podcastBucketListing: size:${AudioPlayerService.artistLibraryList.size}"
                            )

                        } catch (exp: Exception) {
                            exp.printStackTrace()
                            LibraryResult.ofItemList(
                                AudioPlayerService?.artistLibraryList ?: ImmutableList.of(),
                                MediaLibraryService.LibraryParams.Builder().build()
                            )
                        }
                    }

                    override fun setVolleyError(volleyError: VolleyError?) {
                        volleyError?.printStackTrace()
                        CommonUtils.setLog("ChannelHelper", "ChannelHelper: volleyError:${volleyError?.message}")
                        LibraryResult.ofItemList(
                            AudioPlayerService?.artistLibraryList ?: ImmutableList.of(),
                            MediaLibraryService.LibraryParams.Builder().build()
                        )
                    }

                })

            }
            AudioPlayerService?.artistLibraryList!!
        }

    }

    public fun setEventModelDataAppLevel(playableContentModel: PlayableContentModel, contentType: Int) {

        CoroutineScope(Dispatchers.IO).launch {
            var eventModel: EventModel?= HungamaMusicApp?.getInstance()?.getEventData(playableContentModel.data.head.headData.id)

            if(eventModel==null || TextUtils.isEmpty(eventModel?.contentID)){
                eventModel= EventModel()
            }

            if(TextUtils.isEmpty(eventModel?.contentID)){
                eventModel?.contentID=playableContentModel.data.head.headData.id
            }

            if(TextUtils.isEmpty(eventModel?.duration)){
                eventModel?.duration=""+playableContentModel?.data?.head?.headData?.duration
            }

            if(TextUtils.isEmpty(eventModel?.songName)){
                eventModel?.songName=""+playableContentModel?.data?.head?.headData?.title
            }

            if(playableContentModel?.data?.head?.headData?.misc?.actorf!=null&&playableContentModel.data.head.headData.misc.actorf.size>0){
//                eventModel?.actor=""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.misc?.actorf)
                eventModel?.actor=playableContentModel.data.head.headData.misc.actorf.toString()
            }
            CommonUtils.setLog(
                TAG,
                "setEventModelDataAppLevel: eventModel?.actor:${eventModel?.actor}"
            )

            if(playableContentModel?.data?.head?.headData?.misc?.pid!=null&&playableContentModel.data.head.headData.misc.pid.size>0){
//                eventModel?.album_ID=""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.misc?.pid)
                eventModel?.album_ID=playableContentModel.data.head.headData.misc.pid.toString()
            }

            if(playableContentModel?.data?.head?.headData?.misc?.pName!=null&&playableContentModel.data.head.headData.misc.pName.size>0){
//                eventModel?.album_name=""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.misc?.pName)
                eventModel?.album_name=playableContentModel.data.head.headData.misc.pName.toString()
            }


            if(playableContentModel.data?.head?.headData?.misc?.pName!=null&&playableContentModel.data.head.headData.misc.pName.size>0){
//                eventModel?.originalAlbumName=""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.misc?.pName)
                eventModel?.originalAlbumName=playableContentModel.data.head.headData.misc.pName.toString()
            }

            if(playableContentModel?.data?.head?.headData?.misc?.keywords!=null&&playableContentModel.data.head.headData.misc.keywords.size>0){
//                eventModel?.keywords=""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.misc?.keywords)
                eventModel?.keywords=playableContentModel.data.head.headData.misc.keywords.toString()
            }

            if(playableContentModel?.data?.head?.headData?.category!=null&&playableContentModel.data.head.headData.category.size>0){
//                eventModel?.category=""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.category)
                eventModel?.category=playableContentModel.data.head.headData.category.toString()
            }
            eventModel?.rating=""+playableContentModel?.data?.head?.headData?.misc?.ratingCritic
            eventModel?.userRating=""+playableContentModel?.data?.head?.headData?.misc?.rating_user

            if(playableContentModel?.data?.head?.headData?.misc?.movierights!=null&&playableContentModel.data.head.headData.misc.movierights.size>0){
//                eventModel?.content_Pay_Type=""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.misc?.movierights)
                eventModel?.content_Pay_Type=playableContentModel.data.head.headData.misc.movierights.toString()
            }

            eventModel?.release_Date=""+playableContentModel?.data?.head?.headData?.releasedate

            if(playableContentModel?.data?.head?.headData?.genre!=null&&playableContentModel.data.head.headData.genre.size>0){
//                eventModel?.genre=""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.genre)
                eventModel?.genre=playableContentModel.data.head.headData.genre.toString()
            }

            if(playableContentModel?.data?.head?.headData?.subgenre_name!=null&&playableContentModel.data.head.headData.subgenre_name.size>0){
//                eventModel?.genre=""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.subgenre_name)
                eventModel.subGenre=playableContentModel.data.head.headData.subgenre_name.toString()
            }

            if(playableContentModel?.data?.head?.headData?.misc?.attributeCensorRating!=null&&playableContentModel.data.head.headData.misc.attributeCensorRating.size>0){
//                eventModel?.age_rating=""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.misc?.attributeCensorRating)
                eventModel.age_rating=playableContentModel.data.head.headData.misc.attributeCensorRating.toString()
            }

            if(playableContentModel?.data?.head?.headData?.misc?.lyricistf!=null&&playableContentModel.data.head.headData.misc.lyricistf.size>0){
//                eventModel?.lyricist=""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.misc?.lyricistf)
                eventModel.lyricist=playableContentModel.data.head.headData.misc.lyricistf.toString()
            }

            if(playableContentModel?.data?.head?.headData?.misc?.musicdirectorf!=null&&playableContentModel.data.head.headData.misc.musicdirectorf.size>0){
//                eventModel?.musicDirectorComposer=""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.misc?.musicdirectorf)
                eventModel?.musicDirectorComposer=playableContentModel.data.head.headData.misc.musicdirectorf.toString()
            }

            if(playableContentModel?.data?.head?.headData?.misc?.singerf!=null&&playableContentModel.data.head.headData.misc.singerf.size>0){
//                eventModel?.singer=""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.misc?.singerf)
                eventModel?.singer=playableContentModel.data.head.headData.misc.singerf.toString()
            }
            eventModel?.mood=""+playableContentModel?.data?.head?.headData?.misc?.mood
            if(playableContentModel?.data?.head?.headData?.misc?.tempo!=null&&playableContentModel.data.head.headData.misc.tempo.size>0){
//                eventModel?.tempo=""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.misc?.tempo)
                eventModel?.tempo=playableContentModel.data.head.headData.misc.tempo.toString()
            }

            if(playableContentModel?.data?.head?.headData?.misc?.lang!=null&&playableContentModel.data.head.headData.misc.lang.size>0){
//                eventModel?.language=""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.misc?.lang)
                eventModel?.language=playableContentModel.data.head.headData.misc.lang.toString()
            }

            if(playableContentModel?.data?.head?.headData?.misc?.sl!=null&&playableContentModel.data.head.headData.misc.sl.lyric!=null && !TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.sl?.lyric?.link!!)){
                val someFilepath = playableContentModel?.data?.head?.headData?.misc?.sl?.lyric?.link
                val extension = someFilepath?.substring(someFilepath.lastIndexOf("."))
                eventModel?.lyrics_type=""+extension
            }
            eventModel?.label=""+playableContentModel?.data?.head?.headData?.misc?.vendor
            eventModel?.label_id=""+playableContentModel?.data?.head?.headData?.misc?.vendorid
            eventModel?.share=""+playableContentModel?.data?.head?.headData?.misc?.share
            eventModel?.favCount=""+playableContentModel?.data?.head?.headData?.misc?.f_fav_count
            eventModel?.f_fav_count=""+playableContentModel?.data?.head?.headData?.misc?.f_fav_count

            if(playableContentModel?.data?.head?.headData?.misc?.pName!=null&&playableContentModel.data.head.headData.misc.pName.size>0){
//                eventModel?.ptype=""+""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.misc?.pName)
                eventModel?.ptype=playableContentModel.data.head.headData.misc.pName.toString()
            }

            if(playableContentModel?.data?.head?.headData?.misc?.pid!=null&&playableContentModel.data.head.headData.misc.pid.size>0){
//                eventModel?.pid=""+""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.misc?.pid)
                eventModel?.pid=playableContentModel.data.head.headData.misc.pid.toString()
            }

            if(playableContentModel?.data?.head?.headData?.misc?.pName!=null&&playableContentModel.data.head.headData.misc.pName.size>0){
//                eventModel?.pName=""+""+TextUtils.join(",",playableContentModel?.data?.head?.headData?.misc?.pName)
                eventModel?.pName=playableContentModel.data.head.headData.misc.pName.toString()
                eventModel.podcast_album_name = playableContentModel.data.head.headData.misc.pName.toString()
            }

            val userSubscriptionDetail= SharedPrefHelper.getInstance().getPayUserDetail(PrefConstant.USER_PAY_DATA)
            if(userSubscriptionDetail!=null){
                eventModel?.subscriptionStatus=""+userSubscriptionDetail?.data?.user?.userMembershipType
            }
            CommonUtils.setLog(TAG, "setEventModelDataAppLevel contentType: " + contentType)

            if(contentType==4){
                val userSettingRespModel= SharedPrefHelper.getInstance().getUserPlayBackSetting(
                    Constant.TYPE_MUSICPLAYBACK_SETTING)
                CommonUtils.setLog(
                    TAG,
                    "setEventModelDataAppLevel userSettingRespModel:${userSettingRespModel} playableContentModel:${playableContentModel?.data?.head?.headData?.title}"
                )
                if(userSettingRespModel!=null){
                    if(userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.streaming_quality!=null && !TextUtils.isEmpty(userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.streaming_quality)){
                        eventModel?.audioQuality= userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.streaming_quality!!
                        CommonUtils.setLog(
                            TAG,
                            "setEventModelDataAppLevel userSettingRespModel: " + userSettingRespModel?.data?.data?.get(
                                0
                            )?.preference?.get(0)?.streaming_quality!!
                        )
                    }
                }
                if(TextUtils.isEmpty(eventModel?.audioQuality)){
                    eventModel?.audioQuality="Auto"
                }

                val userDataMap= java.util.HashMap<String, String>()
                userDataMap.put(EventConstant.AUDIO_QUALITY, ""+ eventModel.audioQuality)
                EventManager.getInstance().sendUserAttribute(UserAttributeEvent(userDataMap))
            }else if(contentType==5){
                val userSettingVideoModel= SharedPrefHelper.getInstance().getUserPlayBackSetting(
                    Constant.TYPE_VIDEOPLAYBACK_SETTING)
                if(userSettingVideoModel!=null){
                    if(userSettingVideoModel?.data?.data?.get(0)?.preference?.get(0)?.streaming_quality!=null && !TextUtils.isEmpty(userSettingVideoModel?.data?.data?.get(0)?.preference?.get(0)?.streaming_quality)){
                        eventModel?.videoQuality= userSettingVideoModel?.data?.data?.get(0)?.preference?.get(0)?.streaming_quality!!
                    }
                }

                if(TextUtils.isEmpty(eventModel?.videoQuality)){
                    eventModel?.audioQuality="Auto"
                }

                val userDataMap= java.util.HashMap<String, String>()
                userDataMap.put(EventConstant.VIDEO_DOWNLOAD_QUALITY, ""+ eventModel.videoQuality)
                EventManager.getInstance().sendUserAttribute(UserAttributeEvent(userDataMap))
            }

            HungamaMusicApp.getInstance().setEventData(eventModel.contentID,eventModel)

            CommonUtils.setLog(
                TAG,
                "callOfflineSongEventAnalytics eventModel sourceName:${eventModel?.sourceName}  bucketName: ${eventModel?.bucketName} downloadQueue.audioQuality:${eventModel.audioQuality}"
            )
        }


    }
}
