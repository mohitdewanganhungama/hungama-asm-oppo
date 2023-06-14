//package com.hungama.music.auto.media.library
//
//import android.content.Context
//import android.graphics.Bitmap
//import android.net.Uri
//import android.provider.MediaStore
//import android.support.v4.media.MediaBrowserCompat
//import android.support.v4.media.MediaBrowserCompat.MediaItem
//import android.support.v4.media.MediaMetadataCompat
//import android.util.Log
//import com.bumptech.glide.Glide
//import com.hungama.music.auto.NOTIFICATION_LARGE_ICON_SIZE
//import com.hungama.music.auto.media.extensions.albumArtUri
//import com.hungama.music.auto.channel.ChannelHelper
//import com.hungama.music.auto.glideOptions
//import com.hungama.music.auto.media.extensions.*
//import com.hungama.music.R
//import kotlinx.coroutines.*
//import java.io.IOException
//
//
///**
// * Represents a tree of media that's used by [MusicService.onLoadChildren].
// *
// * [BrowseTree] maps a media id (see: [MediaMetadataCompat.METADATA_KEY_MEDIA_ID]) to one (or
// * more) [MediaMetadataCompat] objects, which are children of that media id.
// *
// * For example, given the following conceptual tree:
// * root
// *  +-- Albums
// *  |    +-- Album_A
// *  |    |    +-- Song_1
// *  |    |    +-- Song_2
// *  ...
// *  +-- Artists
// *  ...
// *
// *  Requesting `browseTree["root"]` would return a list that included "Albums", "Artists", and
// *  any other direct children. Taking the media ID of "Albums" ("Albums" in this example),
// *  `browseTree["Albums"]` would return a single item list "Album_A", and, finally,
// *  `browseTree["Album_A"]` would return "Song_1" and "Song_2". Since those are leaf nodes,
// *  requesting `browseTree["Song_1"]` would return null (there aren't any children of it).
// */
//class BrowseTree(
//    val context: Context,
//    musicSource: MusicSource,
//    val recentMediaId: String? = null
//) {
//    val TAG="BrowseTree"
//    private val mediaIdToChildren = mutableMapOf<String, MutableList<MediaMetadataCompat>>()
//
//    /**
//     * Whether to allow clients which are unknown (not on the allowed list) to use search on this
//     * [BrowseTree].
//     */
//    val searchableByUnknownCaller = true
//
//    /**
//     * In this example, there's a single root node (identified by the constant
//     * [UAMP_BROWSABLE_ROOT]). The root's children are each album included in the
//     * [MusicSource], and the children of each album are the songs on that album.
//     * (See [BrowseTree.buildAlbumRoot] for more details.)
//     *
//     * TODO: Expand to allow more browsing types.
//     */
//    init {
//        val rootList = mediaIdToChildren[HUNGAMA_BROWSABLE_ROOT] ?: mutableListOf()
//
//        val discoverMetadata = MediaMetadataCompat.Builder().apply {
//            id = HUNGAMA_DISCOVER_ROOT
//            title = context.getString(R.string.discover_title)
//            albumArtUri = RESOURCE_ROOT_URI +
//                    context.resources.getResourceEntryName(R.drawable.ic_recommended)
//            flag = MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
//        }.build()
//
//        val recentlyPlayedMetadata = MediaMetadataCompat.Builder().apply {
//            id = HUNGAMA_RECOMMENDED_ROOT
//            title = context.getString(R.string.recently_played_title)
//            albumArtUri = RESOURCE_ROOT_URI +
//                    context.resources.getResourceEntryName(R.drawable.ic_album)
//            flag = MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
//        }.build()
//
//        val podcastMetadata = MediaMetadataCompat.Builder().apply {
//            id = HUNGAMA_PODCAST_ROOT
//            title = context.getString(R.string.podcast_title)
//            albumArtUri = RESOURCE_ROOT_URI +
//                    context.resources.getResourceEntryName(R.drawable.ic_recommended)
//            flag = MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
//        }.build()
//
//        val libraryMetadata = MediaMetadataCompat.Builder().apply {
//            id = HUNGAMA_LIBRARY_ROOT
//            title = context.getString(R.string.library_title)
//            albumArtUri = RESOURCE_ROOT_URI +
//                    context.resources.getResourceEntryName(R.drawable.ic_album)
//            flag = MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
//        }.build()
//
//        Log.d(TAG, "BrowseTree:rootList:${rootList}")
//
//        rootList += discoverMetadata
//        rootList += recentlyPlayedMetadata
//        rootList += podcastMetadata
//        rootList += libraryMetadata
//        mediaIdToChildren[HUNGAMA_BROWSABLE_ROOT] = rootList
//
//        ChannelHelper.getBucketListing(context, HUNGAMA_PODCAST_ROOT)
//        ChannelHelper.getBucketListing(context, HUNGAMA_DISCOVER_ROOT)
//        Log.d(TAG, "BrowseTree:rootList++:${mediaIdToChildren[HUNGAMA_BROWSABLE_ROOT]}")
//
//        Log.d(TAG, "BrowseTree:musicSource:${musicSource}")
//
//        musicSource.forEach { mediaItem ->
//
//            val albumMediaId = mediaItem.album.urlEncoded
//            val albumChildren = mediaIdToChildren[albumMediaId] ?: buildAlbumRoot(mediaItem)
//            albumChildren += mediaItem
//
//            Log.d(TAG, "BrowseTree: albumMediaId:${albumMediaId} trackNumber:${mediaItem.trackNumber}  mediaItem.id:${mediaItem.id}")
//
//            // Add the first track of each album to the 'Recommended' category
//            if (mediaItem.trackNumber == 1L) {
//                val recommendedChildren = mediaIdToChildren[HUNGAMA_RECOMMENDED_ROOT]
//                    ?: mutableListOf()
//                recommendedChildren += mediaItem
//                mediaIdToChildren[HUNGAMA_RECOMMENDED_ROOT] = recommendedChildren
//            }
//
//            // If this was recently played, add it to the recent root.
//            if (mediaItem.id == recentMediaId) {
//                mediaIdToChildren[HUNGAMA_RECENT_ROOT] = mutableListOf(mediaItem)
//            }
//
//            Log.d(TAG, "BrowseTree:mediaIdToChildren:${mediaIdToChildren}")
//        }
//
//        Log.d(TAG, "BrowseTree:mediaIdToChildren:${mediaIdToChildren}")
//
//
//    }
//
//    /**
//     * Provide access to the list of children with the `get` operator.
//     * i.e.: `browseTree\[UAMP_BROWSABLE_ROOT\]`
//     */
//    operator fun get(mediaId: String) = mediaIdToChildren[mediaId]
//
//    /**
//     * Builds a node, under the root, that represents an album, given
//     * a [MediaMetadataCompat] object that's one of the songs on that album,
//     * marking the item as [MediaItem.FLAG_BROWSABLE], since it will have child
//     * node(s) AKA at least 1 song.
//     */
//    private fun buildAlbumRoot(mediaItem: MediaMetadataCompat): MutableList<MediaMetadataCompat> {
//        Log.d("TAG", "buildAlbumRoot: albumArtUri:${mediaItem.albumArtUri}")
//        Log.d("TAG", "buildAlbumRoot: albumArt:${mediaItem.albumArt}")
//
////        if(mediaItem.albumArt==null){
////            loadBitmap(mediaItem.albumArtUri)
////        }
//
//        val albumMetadata = MediaMetadataCompat.Builder().apply {
//            id = mediaItem.album.urlEncoded
//            title = mediaItem.album
//            artist = mediaItem.artist
////            if(mediaItem.albumArt==null&&currentBitmap!=null){
////                albumArt = currentBitmap
////            }else{
////                albumArt = mediaItem.albumArt
////            }
//
//            albumArtUri = mediaItem.albumArtUri.toString()
//            flag = MediaItem.FLAG_BROWSABLE
//        }.build()
//
////        // Adds this album to the 'Albums' category.
////        val rootList = mediaIdToChildren[HUNGAMA_LIBRARY_ROOT] ?: mutableListOf()
////        rootList += albumMetadata
////        mediaIdToChildren[HUNGAMA_LIBRARY_ROOT] = rootList
////
////        // Adds this album to the 'Albums' category.
////        val rootList1 = mediaIdToChildren[HUNGAMA_DISCOVER_ROOT] ?: mutableListOf()
////        rootList1 += albumMetadata
////        mediaIdToChildren[HUNGAMA_DISCOVER_ROOT] = rootList1
//
//        // Adds this album to the 'Albums' category.
//        val rootList2 = mediaIdToChildren[HUNGAMA_PODCAST_ROOT] ?: mutableListOf()
//        rootList2 += albumMetadata
//        mediaIdToChildren[HUNGAMA_PODCAST_ROOT] = rootList2
//
//        Log.d("TAG", "buildAlbumRoot: albumMetadata:${albumMetadata?.albumArt} mediaIdToChildren:${mediaIdToChildren}")
//
//        // Insert the album's root with an empty list for its children, and return the list.
//        return mutableListOf<MediaMetadataCompat>().also {
//            mediaIdToChildren[albumMetadata.id!!] = it
//        }
//    }
//    var currentBitmap: Bitmap? = null
//    private fun loadBitmap(url: Uri?) {
//        Log.d("TAG", "buildAlbumRoot: loadBitmap url:${url}")
//        val serviceJob = SupervisorJob()
//        val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
//        serviceScope.launch {
//            if(url!=null){
//                try {
//                    try {
//                        currentBitmap = MediaStore.Images.Media.getBitmap(
//                            context.getContentResolver(),
//                            url
//                        )
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                    }
//
//
//                    Log.d("TAG", "buildAlbumRoot: currentBitmap:${currentBitmap}")
//
//                } catch (e: IOException) {
//                    e.printStackTrace()
//                    currentBitmap= resolveUriAsBitmap(url)
//                }
//                if(currentBitmap==null){
//                    currentBitmap= resolveUriAsBitmap(url)
//                }
//            }
//
//        }
//    }
//
//    private suspend fun resolveUriAsBitmap(uri: Uri): Bitmap? {
//        return withContext(Dispatchers.IO) {
//            Log.d("TAG", "buildAlbumRoot: resolveUriAsBitmap:${uri}")
//            // Block on downloading artwork.
//            Glide.with(context).applyDefaultRequestOptions(glideOptions)
//                .asBitmap()
//                .load(uri)
//                .submit(NOTIFICATION_LARGE_ICON_SIZE, NOTIFICATION_LARGE_ICON_SIZE)
//                .get()
//        }
//    }
//}
//
//const val HUNGAMA_BROWSABLE_ROOT = "/"
//const val HUNGAMA_EMPTY_ROOT = "@empty@"
//const val HUNGAMA_RECOMMENDED_ROOT = "__RECOMMENDED__"
//const val HUNGAMA_LIBRARY_ROOT = "__LIBRARY__"
//const val HUNGAMA_RECENT_ROOT = "__RECENT__"
//const val HUNGAMA_DISCOVER_ROOT = "__DISCOVER__"
//const val HUNGAMA_PODCAST_ROOT = "__PODCAST__"
//const val HUNGAMA_PODCAST_DETAIL_ROOT = "__PODCAST__DETAIL__"
//const val HUNGAMA_DISCOVER_DETAIL_ROOT = "__DISCOVER__DETAIL__"
//
//const val MEDIA_SEARCH_SUPPORTED = "android.media.browse.SEARCH_SUPPORTED"
//
//const val RESOURCE_ROOT_URI = "android.resource://com.hungama.myplay.activity/drawable/"
