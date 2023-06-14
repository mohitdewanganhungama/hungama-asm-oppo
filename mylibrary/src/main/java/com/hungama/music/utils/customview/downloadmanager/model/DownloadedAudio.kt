package com.hungama.music.utils.customview.downloadmanager.model

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.hungama.fetch2.Status
import com.hungama.music.data.model.ContentTypes
import com.hungama.music.data.model.DetailPages
import com.hungama.music.data.model.PlanNames
import com.hungama.music.data.model.PlanTypes
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
@Entity(tableName = "downloaded_audio")
data class DownloadedAudio(
    @PrimaryKey(autoGenerate = true)
    var aId: Long? = null,
    var contentId:String? = null,
    /*var albumId:String? = null,
    var chartId:String? = null,
    var playlistId:String? = null,
    var podcastId:String? = null,
    var artistId:String? = null,
    var collectionId:String? = null,*/
    var title: String? = "",
    var subTitle: String? = "",
    var playableUrl: String? = "",
    var downloadUrl: String? = "",
    var lyricsUrl: String? = "",
    var downloadManagerId: Int = 0,
    var downloadedFilePath: String = "",
    var totalDownloadBytes: Long = 0,
    var downloadedBytes: Long = 0,
    var downloadStatus: Int = Status.NONE.value,
    var downloadNetworkType: Int = 0,
    var createdDT: Long = 0,

    var parentId: String? = null,
    var pName:String? = "",
    var pType:Int = DetailPages.EMPTY_PAGE.value,
    var contentType:Int = ContentTypes.NONE.value,
    var originalAlbumName:String? = "",
    var podcastAlbumName:String? = "",
    var releaseDate:String? = "",
    var actor:String? = "",
    var singer:String? = "",
    var lyricist:String? = "",
    var genre:String? = "",
    var subGenre:String? = "",
    var mood:String? = "",
    var tempo:String? = "",
    var language:String? = "",
    var musicDirectorComposer:String? = "",
    var releaseYear:String? = "",
    var category:String? = "",
    var rating:String? = "",
    var cast_enabled:Int? = 0,
    var ageRating:String? = "",
    var criticRating:String? = "",
    var keywords:String? = "",
    var episodeNumber:String? = "",
    var seasonNumber:String? = "",
    var subtitleEnabled:Int? = 0,
    var selectedSubtitleLanguage:String? = "",
    var lyricsType:String? = "",
    var userRating:String? = "",
    var videoQuality:String? = "",
    var audioQuality:String? = "",
    var label:String? = "",
    var labelId:String? = "",
    var isOriginal:String? = "",
    var contentPayType:String? = "",

    var itype:Int? = 0,
    var type:Int? = 0,
    var image:String? = "",
    var duration:Long? = 0,
    var cast:String? = "",
    var explicit:Int? = 0,
    var pid:String? = "",
    var movierights:String? = "",
    var attribute_censor_rating:String? = "",
    var nudity:String? = "",
    var playcount:Int? = 0,
    var s_artist:String? = "",
    var artist:String? = "",
    var lyricsLanguage:String? = "",
    var lyricsLanguageId:String? = "",
    var lyricsFilePath:String? = "",
    var fav_count:Int? = 0,
    var synopsis:String? = "",
    var description:String? = "",
    var vendor:String? = "",
    var countEraFrom:String? = "",
    var countEraTo:String? = "",
    var skipCreditET:Int? = 0,
    var skipCreditST:Int? = 0,
    var skipIntroET:Int? = 0,
    var skipIntroST:Int? = 0,
    var userId:String? = "",
    var thumbnailPath:String? = "",

    var pSubName:String? = "",
    var pReleaseDate:String? = "",
    var pDescription:String? = "",
    var pNudity:String? = "",
    var pRatingCritics:String? = "",
    var pMovieRights:String? = "",
    var pGenre:String? = "",
    var pLanguage:String? = "",
    var pImage:String? = "",
    var heading:String? = "",

    var downloadAll:Int = 0,
    var parentThumbnailPath:String? = "",
    var downloadRetry:Int = 0,
    var isFavorite:Int = 0,

    var planName:String = PlanNames.NONE.name,
    var planType:Int = PlanTypes.SUBSCRIPTION.value, // 0 - Subscription plan and 1 - rental plan

    var contentStreamDate:Long = 0,
    var contentStreamDuration: Long = 0,
    var percentDownloaded:Int = 0,

    var contentStartDate:String = "",
    var contentExpiryDate:String = "",
    var contentPlayValidity:Int = 0, //validity in days for play the content
    var drmLicense:String = "",
    var contentShareLink:String = "",
    var isSelected:Int = 0,
    var isDeleted:Int = 0,

    var restrictedDownload: Int = 0,
    var downloadManagerExoPlayerId:String = "",

    var source:String = "",
    var f_playcount:String = "",
    var f_fav_count:String = ""
    ) : Parcelable
