package com.hungama.music.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.hungama.music.utils.DateUtils
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import java.util.*
import kotlin.collections.ArrayList


@Parcelize
data class HomeModel(

    @field:SerializedName("data")
    val data: Data? = null,

    @field:SerializedName("message")
    val message: String? = "",

    @field:SerializedName("status")
    val status: Int? = null
) : Parcelable

@Parcelize
data class Data(

    @field:SerializedName("head")
    val head: Head? = null,

    @field:SerializedName("body")
    val body: Body? = null,

    @field:SerializedName("subtitle")
    val subtitle: String? = "",

    @field:SerializedName("id")
    val id: String? = "",

    @field:SerializedName("type")
    val type: String? = "",

    @field:SerializedName("misc")
    val misc: Misc? = null
) : Parcelable

@Parcelize
data class Head(

    @field:SerializedName("hscroll")
    val hscroll: Int? = null,

    @field:SerializedName("id")
    val id: String? = "",

    @field:SerializedName("type")
    val type: Int? = null,

    @field:SerializedName("items")
    val items: List<HeadItemsItem?>? = null,

    @field:SerializedName("data")
    val data: HeadData? = null
) : Parcelable

@Parcelize
data class HeadItemsItem(

    @field:SerializedName("id")
    val id: String? = "",

    @field:SerializedName("page")
    val page: String? = "",

    @field:SerializedName("title")
    val title: String? = null
) : Parcelable

@Parcelize
data class Body(

    @field:SerializedName("rows")
    var rows: ArrayList<RowsItem?>? = null
) : Parcelable


@Parcelize
data class RowsItem(

    @field:SerializedName("heading")
    var heading: String? = "",

    @field:SerializedName("pro_user")
    val pro_user: String? = "2",

    @field:SerializedName("more")
    val more: Int? = null,

    @field:SerializedName("hscroll")
    val hscroll: Int? = null,

    @field:SerializedName("id")
    val id: String? = "",

    @field:SerializedName("image")
    val image: String? = "",

    @field:SerializedName("videoUrl")
    val videoUrl: String? = "",

    @field:SerializedName("description")
    val description: String? = "",

    @field:SerializedName("type")
    val type: Int? = null,

    @field:SerializedName("numrow")
    var numrow: Int? = null,

    @field:SerializedName("items")
    var items: ArrayList<BodyRowsItemsItem?>? = null,

    @field:SerializedName("subhead")
    val subhead: String? = "",
    @field:SerializedName("itype")
    var itype: Int? = null,

    @field:SerializedName("orignal_season")
    var orignalItems: ArrayList<OrignalSeason?>? = null,

    @field:SerializedName("public")
    var public: Int? = null,

    @field:SerializedName("bucketQuery")
    var bucketQuery:String = "",

    @field:SerializedName("identifier")
    var identifier:Int? = null,

    @field:SerializedName("keywords")
    var keywords: List<String>? = null
) : Parcelable


@Entity(tableName = "BodyRowsItemsItem")
@Parcelize
data class BodyRowsItemsItem(

    @PrimaryKey
    var itemId: Int = 0, // or foodId: Int? = null

    @field:SerializedName("data")
    var data: BodyDataItem? = null,

    var type: String? = "",

    @Ignore
    @Expose
    var id:String = "",

    @Ignore
    @Expose
    var orignalItems: ArrayList<OrignalSeason?>? = null,

    @ColumnInfo(name = "added_date_Time")
    var addedDateTime: Date?=DateUtils.getCurrentDateTime(),

    @field:SerializedName("itype")
    var itype: Int? = null,
    var sr_no: Int? = null, // or foodId: Int? = null
    var adUnitId: String = ""
) : Parcelable
@Entity(tableName = "bodydataitem")
@Parcelize
data class BodyDataItem(
    @field:SerializedName("id")
    var id: String? = "",
    @field:SerializedName("uid")
    var uid: String? = "",
    @field:SerializedName("handleName")
    var handleName: String? = "",
    @field:SerializedName("identifier")
    var identifier: String? = "",
    @field:SerializedName("moodid")
    val moodid: String? = "",
    @field:SerializedName("title")
    var title: String? = "",
    var options: ArrayList<String> = ArrayList(),
    @field:SerializedName("subtitle")
    var subTitle: String? = "",
    @field:SerializedName("type")
    var type: String? = "",
    @field:SerializedName("deeplink_url")
    var deeplink_url: String? = "",
    @field:SerializedName("deeplink_action")
    var deeplink_action: Boolean? = false,
    @field:SerializedName("duration")
    var duration: String? = "",
    @field:SerializedName("misc")
    val misc: Misc? = null,
    @field:SerializedName("image")
    var image: String = "",
    @field:SerializedName("playble_image")
    var playble_image: String = "",
    @field:SerializedName("internal")
    var internal: Boolean = false,
    @field:SerializedName("isBrandHub")
    var isBrandHub: Boolean = false,
    @field:SerializedName("deepLink")
    var deepLink: String = "",
    @field:SerializedName("date")
    var date: String = "",
    @field:SerializedName("endDate")
    var endDate: String = "",
    @field:SerializedName("continueStatus")
    var continueStatus: Boolean = false,
    @SerializedName("movierights")
    var movierights: MutableList<String>? = mutableListOf(),

    var width: Int? = 0,
    var height: Int? = 0,
    var ratio: Float? = 0f,
    var images: MutableList<String> = mutableListOf(),
    var location: String? = "",
    //var viewInex: Int? = 0,
    //var isAllStorySeen: Boolean? = false,
    //var stories: MutableList<HomeStory> = mutableListOf(),
    var userStories: UserStoryModel? = null,
    @SerializedName("genre")
    var genre: List<String>? = listOf(),
    @SerializedName("duration_play")
    var durationPlay: Number? = 0,
    @SerializedName("variant")
    var variant:String = "",
    @field:SerializedName("itype")
    var itype: Int? = null,
    @SerializedName("variant-images")
    var variant_images:List<String>? = null,
    var isVisible:Boolean = true,
    @SerializedName("releasedate")
    var releasedate: String = "",
    @SerializedName("attribute_game_rating")
    var attributeGameRating: List<String>? = listOf(),
    @Expose
    var isCurrentPlaying: Boolean = false,
    @SerializedName("sequence")
    var sequence: Int = 0,
    @Expose
    var playTrailer : Boolean = false,
    var loopTrailer : Boolean = false,
    var playWithSound : Boolean = false,
    var setTime : Boolean = false,
    @SerializedName("primaryCta")
    @Expose
    var primaryCta: PrimaryCta? = null,
    @SerializedName("secondaryCta")
    @Expose
    var secondaryCta : String = "",
    @SerializedName("trailer")
    @Expose
    var trailer : String = "",
    @SerializedName("contentType")
    @Expose
    var contentType : String = "",
    @SerializedName("contentTypeId")
    @Expose
    var contentTypeId : String = "",

    @SerializedName("user")
    var user:String = "",
    @SerializedName("share")
    var share:String = "",

    var isFollow : Boolean = false,
    var isDownloading : String = "",
    var playlistSongList: ArrayList<PlaylistModel.Data.Body.Row> = ArrayList()
) : Parcelable

@Keep
@Parcelize
data class PrimaryCta(
    @SerializedName("id")
    var id: String = "",
    @SerializedName("title")
    var title: @RawValue List<Any>? = null
) : Parcelable

@Keep
@Parcelize
data class Option(
    @SerializedName("op1")
    var op1: String = "",
    @SerializedName("op2")
    var op2: String = "",
    @SerializedName("op3")
    var op3: String = ""
) : Parcelable

@Entity(tableName = "misc")
@Parcelize
data class Misc(

    var languages: MutableList<String>? = mutableListOf(),

    @field:SerializedName("territory_rights")
    val territoryRights: String? = "",

    @field:SerializedName("lang_id")
    val langId: String? = "",

    @field:SerializedName("header_title")
    val headerTitle: String? = "",

    @field:SerializedName("header_subtitle")
    val headerSubTitle: String? = "",

    @field:SerializedName("header_image")
    val headerImage: String? = "",

    @field:SerializedName("releasedate")
    val releasedate: String? = "",

    @field:SerializedName("priority")
    val priority: String? = "",

//	@field:SerializedName("genre_id")
//	val genreId: String? = "",

    @field:SerializedName("duration")
    val duration: String? = "",

    @field:SerializedName("fav_count")
    val favCount: String? = "",

    @SerializedName("f_fav_count")
    var f_FavCount: String = "",

    @SerializedName("movierights")
    var movierights: List<String> = listOf(),

    @field:SerializedName("vendor_id")
    val vendorId: Int? = null,

    @field:SerializedName("rating_critic")
	val rating_critic: String? = "",

    @field:SerializedName("users_rating")
    val usersRating: UsersRating? = null,

    @field:SerializedName("lang")
    val lang: @RawValue Any? = null,

    @field:SerializedName("explicit_i")
    val explicitI: String? = "",

    @field:SerializedName("propertyid")
    val propertyid: String? = "",

    @field:SerializedName("playcount")
    val playcount: String? = "",

    @SerializedName("f_playcount")
    var f_playcount: String = "",

    @field:SerializedName("synopsis")
    val synopsis: String? = null,

    @field:SerializedName("artistid")
    val artistid: String? = null,

    @SerializedName("artist")
    var artist: List<String?>? = listOf(),
    @SerializedName("attribute_censor_rating")
    var attributeCensorRating: @RawValue List<String?>? = listOf(),
    @SerializedName("cast")
    var cast: String? = "",
    @SerializedName("description")
    var description: String? = "",
    @SerializedName("lyricist")
    var lyricist: List<String>? = listOf(),
    @SerializedName("nudity")
    var nudity: String? = "",
    @SerializedName("pid")
    var pid: @RawValue List<String?>? = listOf(),
    @SerializedName("s_artist")
    var sArtist: List<String?>? = listOf(),
    @SerializedName("skipIntro")
    var skipIntro: SkipIntro? = SkipIntro(),
    @SerializedName("url")
    var url: String? = "",
    @SerializedName("vendor")
    var vendor: String? = "",
    @SerializedName("post")
    var post: StoryPost? = null,
    @SerializedName("explicit")
    var explicit: Int = 0,
    @SerializedName("restricted_download")
    var restricted_download: Int = 1,
    @SerializedName("actorf")
    var actorf: List<String> = listOf(),
    @SerializedName("singerf")
    var singerf: List<String> = listOf(),
    @SerializedName("mood")
    var mood: String = "",
    @SerializedName("musicdirectorf")
    var musicdirectorf: List<String> = listOf(),
    @SerializedName("tempo")
    var tempo: List<String> = listOf()
) : Parcelable

@Entity(tableName = "usersrating")
@Parcelize
data class UsersRating(

    @field:SerializedName("maxrating")
    val maxrating: String? = "",

    @field:SerializedName("avgrating")
    val avgrating: String? = "",

    @field:SerializedName("user_rating")
    val userRating: String? = null
) : Parcelable

@Entity(tableName = "skipintro")
@Parcelize
data class SkipIntro(
    @SerializedName("skipCreditET")
    var skipCreditET: Int? = 0,
    @SerializedName("skipCreditST")
    var skipCreditST: Int? = 0,
    @SerializedName("skipIntroET")
    var skipIntroET: Int? = 0,
    @SerializedName("skipIntroST")
    var skipIntroST: Int? = 0
) : Parcelable

@Parcelize
data class HeadData(
    @SerializedName("image")
    val image: String,
    @SerializedName("title")
    val title: String
) : Parcelable

@Parcelize
data class StoryPost(
    @SerializedName("items")
    var items: List<StoryItems> = listOf(),
    @SerializedName("readCount")
    var readCount: Int = 0,
    @SerializedName("count")
    var count: Int = 0,
    @SerializedName("timestamp")
    var timestamp: Long = 0
) : Parcelable

@Parcelize
data class StoryItems(
    @SerializedName("id")
    var id: String = "",
    @SerializedName("watch")
    var watch: Boolean = false
) : Parcelable


@Keep
@Parcelize
data class OrignalSeason(
    @SerializedName("data")
    var data: OrignalData = OrignalData(),
    var image: String = "",
    var videoUrl: String = "",
    var description: String = "",
    @SerializedName("itype")
    var itype: Int = 0,
    @SerializedName("type")
    var type:Int = 0,
    var isVisible:Boolean = false
) : Parcelable {
    @Keep
    @Parcelize
    data class OrignalData(
        @SerializedName("id")
        var id: String = "",
        @SerializedName("image")
        var image: String = "",
        @SerializedName("misc")
        var misc: OrignalMisc = OrignalMisc(),
        @SerializedName("title")
        var title: String = ""
    ) : Parcelable {
        @Keep
        @Parcelize
        data class OrignalMisc(
            @SerializedName("track")
            var track: List<OrignalMiscTrack> = listOf()
        ) : Parcelable {
            @Keep
            @Parcelize
            data class OrignalMiscTrack(
                @SerializedName("data")
                var data: TrackData = TrackData(),
                @SerializedName("itype")
                var itype: Int = 0
            ) : Parcelable {
                @Keep
                @Parcelize
                data class TrackData(
                    @SerializedName("description")
                    var description: String = "",
                    @SerializedName("id")
                    var id: String = "",
                    @SerializedName("image")
                    var image: String = "",
                    @SerializedName("name")
                    var name: String = "",
                    var movierights: MutableList<String>? = mutableListOf(),
                    @SerializedName("subTitle")
                    var subTitle: String = "",
                    @SerializedName("type")
                    var type: Int = 0,
                    @SerializedName("attribute_censor_rating")
                    var attributeCensorRating: List<String> = listOf()
                ) : Parcelable
            }
        }
    }
}

