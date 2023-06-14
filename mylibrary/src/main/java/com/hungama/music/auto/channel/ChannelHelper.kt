//package com.hungama.music.auto.channel
//
//import android.content.Context
//import android.net.Uri
//import android.os.Bundle
//import android.support.v4.media.MediaBrowserCompat
//import android.support.v4.media.MediaDescriptionCompat
//import android.util.Log
//import androidx.media.MediaBrowserServiceCompat
//import com.android.volley.VolleyError
//import com.google.gson.Gson
//import com.hungama.music.auto.MusicService
//import com.hungama.music.auto.api.data.DataManager
//import com.hungama.music.auto.api.data.DataValues
//import com.hungama.music.auto.api.model.DetailDynamicModel
//import com.hungama.music.auto.api.model.HomeModel
//import com.hungama.music.auto.api.model.PlayableContentModel
//import com.hungama.music.auto.api.model.PlaylistModel
//import com.hungama.music.auto.media.library.*
//import com.hungama.music.auto.ui.AutoHungamaMusicService.Companion.discoverBucketRespModel
//import com.hungama.music.auto.ui.AutoHungamaMusicService.Companion.podcastBucketRespModel
//import org.json.JSONObject
//
//val DEVICE="carPlay"
//object ChannelHelper {
//
//
//    fun getBucketListing(context: Context, category: String): List<MediaBrowserCompat.MediaItem> {
//        var list: List<MediaBrowserCompat.MediaItem>? = null
//        if (category == HUNGAMA_PODCAST_ROOT) {
//            list = getPodcastBucketList(context)
//        } else if (category == HUNGAMA_DISCOVER_ROOT) {
//            list = getDiscoverBucketList(context)
//        }
//
//        return list!!
//    }
//
//    fun getPodcastBucketList(context: Context): List<MediaBrowserCompat.MediaItem> {
//        if (!MusicService.podcastBucketListing?.isNullOrEmpty()) {
//            Log.d(
//                "ChannelHelper",
//                "podcastBucketListing ifff not empty: size:${MusicService.podcastBucketListing.size}"
//            )
//            return MusicService.podcastBucketListing
//        }
//        val dataManger = DataManager(context)
//
////        var url="https://page.api.hungama.com/v1/page/home?uid=5&alang=en&mlang=en,&vlang=en&device=carPlay&platform=a&storeId=1"
//        var url =
//            "https://cpage.api.hungama.com/v1/page/podcast?alang=en&vlang=en,hi&mlang=en,hi&platform=a&device="+DEVICE+"&variant=v1&uid=632155051&storeId=1"
//        Log.d("TAG", "downloadJson: getHomeListDataLatest url:${url}")
//
//
//        dataManger?.getVolleyRequest(context, url, JSONObject(), object : DataValues {
//            override fun setJsonDataResponse(response: JSONObject?) {
//                try {
//                    podcastBucketRespModel = Gson().fromJson<HomeModel>(
//                        response.toString(),
//                        HomeModel::class.java
//                    ) as HomeModel
//                    Log.d(
//                        "ChannelHelper",
//                        "downloadJson: rows size:${podcastBucketRespModel?.data?.body?.rows?.size}"
//                    )
//
//                    podcastBucketRespModel?.data?.body?.rows?.forEach {
//                        var model = it
//                        try {
//                            val mediaDesc = MediaDescriptionCompat.Builder()
//                                .setMediaId(HUNGAMA_PODCAST_ROOT+"_"+model?.id)
//                                .setTitle(model?.heading)
//                                .build()
//
//                            MusicService.podcastBucketListing.add(
//                                MediaBrowserCompat.MediaItem(
//                                    mediaDesc,
//                                    MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
//                                )
//                            )
//                            Log.d("ChannelHelper", "mediaDesc:${mediaDesc}")
//                        } catch (exp: Exception) {
//                            exp.printStackTrace()
//                        }
//
//                    }
//
//
//                    Log.d(
//                        "ChannelHelper",
//                        "podcastBucketListing: size:${MusicService.podcastBucketListing.size}"
//                    )
//
//                } catch (exp: Exception) {
//                    exp.printStackTrace()
//                }
//            }
//
//            override fun setVolleyError(volleyError: VolleyError?) {
//                volleyError?.printStackTrace()
//                Log.d("ChannelHelper", "downloadJson: volleyError:${volleyError}")
//            }
//
//        })
//        return MusicService.podcastBucketListing!!
//    }
//
//    fun getDiscoverBucketList(context: Context): List<MediaBrowserCompat.MediaItem> {
//        if (!MusicService.discoverBucketListing?.isNullOrEmpty()) {
//            Log.d(
//                "ChannelHelper",
//                "podcastBucketListing ifff not empty: size:${MusicService.discoverBucketListing.size}"
//            )
//            return MusicService.discoverBucketListing
//        }
//        val dataManger = DataManager(context)
//
////        var url="https://page.api.hungama.com/v1/page/home?uid=5&alang=en&mlang=en,&vlang=en&device=carPlay&platform=a&storeId=1"
//        var url =
//            "https://cpage.api.hungama.com/v1/page/home?alang=en&vlang=hi,en&mlang=hi,en&platform=a&device="+DEVICE+"&variant=v1&uid=896965566&storeId=1"
//        Log.d("TAG", "downloadJson: getHomeListDataLatest url:${url}")
//
//
//        dataManger?.getVolleyRequest(context, url, JSONObject(), object : DataValues {
//            override fun setJsonDataResponse(response: JSONObject?) {
//                try {
//                    discoverBucketRespModel = Gson().fromJson<HomeModel>(
//                        response.toString(),
//                        HomeModel::class.java
//                    ) as HomeModel
//                    Log.d(
//                        "ChannelHelper",
//                        "downloadJson: rows size:${discoverBucketRespModel?.data?.body?.rows?.size}"
//                    )
//
//
//                    try {
//                        discoverBucketRespModel?.data?.body?.rows?.forEach {
//                            var model = it
//                            if (!model?.id?.equals("1", true)!!) {
//                                val mediaDesc = MediaDescriptionCompat.Builder()
//                                    .setMediaId(HUNGAMA_DISCOVER_ROOT+"_"+model?.id)
//                                    .setTitle(model?.heading)
//                                    .build()
//
//                                MusicService.discoverBucketListing.add(
//                                    MediaBrowserCompat.MediaItem(
//                                        mediaDesc,
//                                        MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
//                                    )
//                                )
//                                Log.d("ChannelHelper", "mediaDesc:${mediaDesc}")
//                            }
//                        }
//
//                    } catch (exp: Exception) {
//                        exp.printStackTrace()
//                    }
//
//                    Log.d(
//                        "ChannelHelper",
//                        "podcastBucketListing: size:${MusicService.discoverBucketListing.size}"
//                    )
//
//                } catch (exp: Exception) {
//                    exp.printStackTrace()
//                }
//            }
//
//            override fun setVolleyError(volleyError: VolleyError?) {
//                volleyError?.printStackTrace()
//                Log.d("ChannelHelper", "downloadJson: volleyError:${volleyError}")
//            }
//
//        })
//        return MusicService.discoverBucketListing!!
//    }
//
//    fun getHeaderWiseListing(context: Context, id: String): List<MediaBrowserCompat.MediaItem> {
//        var list: List<MediaBrowserCompat.MediaItem> = arrayListOf<MediaBrowserCompat.MediaItem>()
//        Log.d("TAG", "getHeaderWiseListing: getPodcastDetail before id:${id}")
//        Log.d("TAG", "getHeaderWiseListing: getPodcastDetail size :${podcastBucketRespModel?.data?.body?.rows?.size}")
//
//
//        var updateID=""
//        if(id?.contains(HUNGAMA_PODCAST_ROOT)){
//            updateID=id?.replace(HUNGAMA_PODCAST_ROOT+"_","")
//            podcastBucketRespModel?.data?.body?.rows?.forEach {
//                Log.d("TAG", "getHeaderWiseListing: getPodcastDetail updateID:${updateID} id:${it?.id}")
//                if(updateID?.equals(it?.id)){
//                    MusicService.podcastListing = arrayListOf<MediaBrowserCompat.MediaItem>()
//                    it?.items?.forEach {
//                        try {
//                            val mediaDesc = MediaDescriptionCompat.Builder()
//                                .setMediaId(HUNGAMA_PODCAST_DETAIL_ROOT+"_"+it?.data?.id)
//                                .setTitle(it?.data?.title)
//                                .setDescription(it?.data?.subTitle)
//                                .setIconUri(Uri.parse(it?.data?.image))
//                                .build()
//
//                            MusicService.podcastListing.add(
//                                MediaBrowserCompat.MediaItem(
//                                    mediaDesc,
//                                    MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
//                                )
//                            )
//                            Log.d("ChannelHelper", "getHeaderWiseListing size:${MusicService.podcastListing?.size} updateID:${updateID} id:${it?.id}")
//
//                        } catch (exp: Exception) {
//                            exp.printStackTrace()
//                        }
//                    }
//
//                }
//            }
//            return MusicService.podcastListing
//
//        }else if(id?.contains(HUNGAMA_DISCOVER_ROOT)){
//            updateID=id?.replace(HUNGAMA_DISCOVER_ROOT+"_","")
//            discoverBucketRespModel?.data?.body?.rows?.forEach {
//                if(updateID?.equals(it?.id)){
//                    MusicService.discoverListing = arrayListOf<MediaBrowserCompat.MediaItem>()
//                    it?.items?.forEach {
//                        try {
//                            val mediaDesc = MediaDescriptionCompat.Builder()
//                                .setMediaId(HUNGAMA_DISCOVER_DETAIL_ROOT+"_"+it?.data?.id)
//                                .setTitle(it?.data?.title)
//                                .setDescription(it?.data?.subTitle)
//                                .setIconUri(Uri.parse(it?.data?.image))
//                                .build()
//
//                            MusicService.discoverListing.add(
//                                MediaBrowserCompat.MediaItem(
//                                    mediaDesc,
//                                    MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
//                                )
//                            )
//                            Log.d("ChannelHelper", "getHeaderWiseListing size:${MusicService.discoverListing?.size}")
//                        } catch (exp: Exception) {
//                            exp.printStackTrace()
//                        }
//                    }
//                    return MusicService.discoverListing
//
//                }
//            }
//        }
//
//
//        Log.d("TAG", "downloadJson: getPodcastDetail after id:${updateID}")
//
//        return list
//    }
//
//    @Synchronized
//    fun getPodcastDetail(
//        context: Context,
//        id: String,
//        result: MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>>
//    ) {
//        MusicService.podcastTrackListingListing = arrayListOf<MediaBrowserCompat.MediaItem>()
//
//        Log.d("TAG", "downloadJson: getPodcastDetail before id:${id}")
//
//        val updateID=id?.replace(HUNGAMA_PODCAST_DETAIL_ROOT+"_","")
//
//        Log.d("TAG", "downloadJson: getPodcastDetail after id:${updateID}")
//
//        callPodcastDetailAPI(context,updateID,result)
//    }
//
//    fun callPodcastDetailAPI(context: Context,id: String,result: MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>>){
//        val dataManger = DataManager(context)
//
//        var url =
//            "https://stgpage.api.hungama.com/v2/page/content/"+id+"/podcast/detail/?alang=en&vlang=hi,en&mlang=hi,en&platform=a&device="+DEVICE+"&variant=v2&uid=896965566&storeId=1"
//        Log.d("TAG", "downloadJson: getPodcastDetail url:${url}")
//
//
//        dataManger?.getVolleyRequest(context, url, JSONObject(), object : DataValues {
//            override fun setJsonDataResponse(response: JSONObject?) {
//                try {
//                    val homeModel = Gson().fromJson<DetailDynamicModel>(
//                        response.toString(),
//                        DetailDynamicModel::class.java
//                    ) as DetailDynamicModel
//                    Log.d(
//                        "ChannelHelper",
//                        "getPodcastDetail: rows size:${homeModel?.data?.body?.rows?.get(0).data?.misc?.tracks?.size}"
//                    )
//
//
//                    try {
//                        var trackNumber=1000L
//                        homeModel?.data?.body?.rows?.get(0).data?.misc?.tracks?.forEach {
//                            var model = it
//
//                            val mediaDesc = MediaDescriptionCompat.Builder()
//                                .setMediaId(model?.data?.id)
//                                .setTitle(model?.data?.title)
//                                .setSubtitle(model?.data?.subtitle)
//                                .setDescription(homeModel?.data?.head?.data?.title)
//                                .setIconUri(Uri.parse(model?.data?.image))
//                                .build()
//
//                            MusicService.podcastTrackListingListing.add(
//                                MediaBrowserCompat.MediaItem(
//                                    mediaDesc,
//                                    MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
//                                )
//                            )
//
//
//
//
////                            callPlayableAPICall(context,model)
//
//                            Log.d("ChannelHelper", "getPodcastDetail model:${model}")
//                        }
//                        result.sendResult(MusicService.podcastTrackListingListing)
//
//                    } catch (exp: Exception) {
//                        exp.printStackTrace()
//                    }
//
//                    Log.d(
//                        "ChannelHelper",
//                        "getPodcastDetail podcastBucketListing inside: size:${MusicService.podcastTrackListingListing.size}"
//                    )
//
//                } catch (exp: Exception) {
//                    exp.printStackTrace()
//                }
//            }
//
//            override fun setVolleyError(volleyError: VolleyError?) {
//                volleyError?.printStackTrace()
//                Log.d("ChannelHelper", "getPodcastDetail: volleyError:${volleyError}")
//            }
//
//        })
//
//
//        Log.d(
//            "ChannelHelper",
//            "getPodcastDetail podcastBucketListing after: size:${MusicService.podcastTrackListingListing.size}"
//        )
//    }
//
//    fun callPlayableAPICall(context: Context, model: PlaylistModel.Data.Body.Row.Data.Misc.Track){
//        val dataManger = DataManager(context)
//
//        var url =
//            "https://curls.api.hungama.com/v1/content/"+model?.data?.id+"/url/playable?quality=A&contentType=4&certificate=widevine&alang=en&vlang=hi,en&mlang=hi,en&platform=a&device="+DEVICE+"&variant=v1&uid=896965566&storeId=1"
//        Log.d("TAG", "downloadJson: getPodcastDetail url:${url}")
//
//        dataManger?.getVolleyRequest(context, url, JSONObject(), object : DataValues {
//            override fun setJsonDataResponse(response: JSONObject?) {
//                try {
//                    val playableContentModel = Gson().fromJson<PlayableContentModel>(
//                        response.toString(),
//                        PlayableContentModel::class.java
//                    ) as PlayableContentModel
//                    Log.d(
//                        "ChannelHelper",
//                        "getPodcastDetail: playableContentModel:${playableContentModel}"
//                    )
//
//                    val mediaDesc = MediaDescriptionCompat.Builder()
//                        .setMediaId(model?.data?.id)
//                        .setTitle(model?.data?.title)
//                        .setDescription(model?.data?.subtitle)
////                        .setMediaUri(Uri.parse(playableContentModel?.data?.body?.data?.url?.playable?.get(0)?.data))
////                        .setMediaUri(Uri.parse("https://media.hungama.com/c/4/caa/ea5/86920918/86920918_,96,128,192,.mp4.m3u8?b05wqepTfQkvNQudWS2KLgiUPSAcXwPCQ7fNw6wyl4f5y_lZL07kz84V0rZDlBS42j8PRbOYOPhhHjtEq6t-Di44xtVWAAIqVEgfRzqO8Jm1Q-Mcatd7vGFMHdND"))
//                        .setIconUri(Uri.parse(model?.data?.image))
//                        .build()
//
//                    MusicService.podcastTrackListingListing.add(
//                        MediaBrowserCompat.MediaItem(
//                            mediaDesc,
//                            MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
//                        )
//                    )
//                    Log.d("ChannelHelper", "getPodcastDetail model mediaDesc:${mediaDesc}")
//                    Log.d(
//                        "ChannelHelper",
//                        "getPodcastDetail podcastBucketListing inside: size:${MusicService.podcastTrackListingListing.size}"
//                    )
//
//                } catch (exp: Exception) {
//                    exp.printStackTrace()
//                }
//            }
//
//            override fun setVolleyError(volleyError: VolleyError?) {
//                volleyError?.printStackTrace()
//                Log.d("ChannelHelper", "getPodcastDetail: volleyError:${volleyError}")
//            }
//
//        })
//    }
//}