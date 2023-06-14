package com.hungama.music.player.audioplayer.model

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.hungama.music.R
import com.hungama.music.data.model.ContentTypes
import com.hungama.music.data.model.DetailPages
import java.io.Serializable

enum class Track_State(val value: Int) {
    //////
//Do not change track state value. Its used for sorting queue list and shuffle list.
    //////
    PLAYING(2),
    PAUSED(3),
    PLAYED(1),
    IN_QUEUE(4)
}

//static fields
private val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

private const val all_tracks_selection =
    MediaStore.Audio.Media.IS_MUSIC + "=1 OR " + MediaStore.Audio.Media.IS_PODCAST + "=1"

private const val track_with_id_selection =
    "(" + all_tracks_selection + ") AND " + MediaStore.Audio.Media._ID + "=?"

private const val track_with_name_selection =
    "(" + all_tracks_selection + ") AND " + MediaStore.Audio.Media.TITLE + " LIKE ?"


private val projection = arrayOf(
    MediaStore.Audio.Media._ID,
    MediaStore.Audio.Media.DATA,
    MediaStore.Audio.Media.TITLE,
    MediaStore.Audio.Media.ARTIST_ID,
    MediaStore.Audio.Media.ARTIST,
    MediaStore.Audio.Media.ALBUM_ID,
    MediaStore.Audio.Media.ALBUM,
    MediaStore.Audio.Media.DURATION,
    MediaStore.Audio.Media.YEAR,
    MediaStore.Audio.Media.TRACK,
    MediaStore.Audio.Media.DATE_ADDED,
    MediaStore.Audio.Media.IS_PODCAST
)

//static method
fun getAllLocalDeviceTracksQuery(context: Context): Cursor? {

    return context.contentResolver.query(
        uri,
        projection,
        all_tracks_selection,
        null,
        MediaStore.Audio.Media.TRACK
    )
}

@Entity(tableName = "track")
open class Track(
    @PrimaryKey(autoGenerate = true)
    var uniquePosition:Long = 0,
    var id: Long = 0,
    var title: String? = "",
    var subTitle: String? = "",
    var image: String? = "",
    var url: String? = "",
    var drmlicence:String? = "",
    @Ignore
    @Transient
    var albumArtBitmap: Bitmap? = null,
    @Ignore
    var statusBarColor: Int? = 0,
    var defaultAlbumArtRes: Int = R.drawable.ic_audiotrack,
    var state: Track_State = Track_State.IN_QUEUE,
    var songLyricsUrl:String? = "",
    var heading:String? = "",
    var artistName:String? = "",
    var playerType:String? = null,
    var isLiked: Boolean = false,
    var isDownloaded: Boolean = false,
    var parentId: String? = null,
    var pName:String? = "",
    var pSubName:String? = "",
    var pImage:String? = "",
    var pType:Int = DetailPages.EMPTY_PAGE.value,
    var contentType:Int = ContentTypes.NONE.value,
    var explicit:Int = 0,
    var restrictedDownload:Int = 0,
    var attributeCensorRating:String = "",
    var pid: String = "",
    var shareUrl:String = "",
    var favCount: String = "",
    var urlKey:String = "",
    var onErrorPlayableUrl:String = "",
    var movierights: String = ""
) : Serializable {
    override fun toString(): String {
        return "Track(uniquePosition=$uniquePosition, id=$id, title=$title, subTitle=$subTitle, image=$image, url=$url, drmlicence=$drmlicence, albumArtBitmap=$albumArtBitmap, statusBarColor=$statusBarColor, defaultAlbumArtRes=$defaultAlbumArtRes, state=$state, songLyricsUrl=$songLyricsUrl, heading=$heading, artistName=$artistName, playerType=$playerType, isLiked=$isLiked, isDownloaded=$isDownloaded, parentId=$parentId, pName=$pName, pSubName=$pSubName, pImage=$pImage, pType=$pType, contentType=$contentType, explicit=$explicit, restrictedDownload=$restrictedDownload, attributeCensorRating='$attributeCensorRating')"
    }
}
