package com.hungama.music.data.model

import android.os.Parcelable
import com.hungama.music.eventanalytic.EventConstant
import kotlinx.android.parcel.Parcelize

/**
 * Created by Chetan(chetan@saeculumsolutions.com) on 9/3/2021.
 * Purpose:
 */
@Parcelize
open class EventModel(
    var actor: String = "",
    var album_ID: String = "",
    var album_name: String = "",
    var audioQuality: String = "",
    var bucketName: String = "",
    var connectionType: String = "",
    var consumptionType: String = EventConstant.CONSUMPTIONTYPE_ONLINE,
    var contentID: String = "",
    var contentType: String = "",
    var deviceModel: String = "",
    var duration: String = "",
    var duration_bg: String = "",
    var duration_fg: String = "",
    var genre: String = "",
    var label: String = "",
    var label_id: String = "",
    var language: String = "",
    var lastSource: String = "",
    var loginStatus: String = "",
    var lyricist: String = "",
    var mood: String = "",
    var musicDirectorComposer: String = "",
    var name: String = "",
    var nid: String = "",
    var originalAlbumName: String = "",
    var percentageCompletion: String = "",
    var playlistID: String = "",
    var playlistName: String = "",
    var podcast_album_name: String = "",
    var podcast_host: String = "",
    var singer: String = "",
    var songName: String = "",
    var sourceName: String = "",
    var subGenre: String = "",
    var lyrics_type: String = "",
    var subscriptionStatus: String = "",
    var tempo: String = "",
    var yearofrelease: String = "",
    var rating: String = "",
    var is_original: String = "",
    var category: String = "",
    var cast_enabled: String = "",
    var age_rating: String = "",
    var content_Pay_Type: String = "",
    var critic_Rating: String = "",
    var keywords: String = "",
    var episodeNumber: String = "",
    var pid: String = "",
    var pName: String = "",
    var ptype: String = "",
    var release_Date: String = "",
    var season_Number: String = "",
    var status: String = "",
    var subtitleEnable: String = "",
    var subtitleLanguageSelected: String = "",
    var userRating: String = "",
    var share: String = "",
    var videoQuality: String = "",
    var favCount: String = "",
    var f_fav_count: String = "",
    var urlKey: String = ""
): Parcelable {
    override fun toString(): String {
        return "EventModel(actor='$actor', album_ID='$album_ID', album_name='$album_name', audioQuality='$audioQuality', bucketName='$bucketName', connectionType='$connectionType', consumptionType='$consumptionType', contentID='$contentID', contentType='$contentType', deviceModel='$deviceModel', duration='$duration', duration_bg='$duration_bg', duration_fg='$duration_fg', genre='$genre', label='$label', label_id='$label_id', language='$language', lastSource='$lastSource', loginStatus='$loginStatus', lyricist='$lyricist', mood='$mood', musicDirectorComposer='$musicDirectorComposer', name='$name', nid='$nid', originalAlbumName='$originalAlbumName', percentageCompletion='$percentageCompletion', playlistID='$playlistID', playlistName='$playlistName', podcast_album_name='$podcast_album_name', podcast_host='$podcast_host', singer='$singer', songName='$songName', sourceName='$sourceName', subGenre='$subGenre', lyrics_type='$lyrics_type', subscriptionStatus='$subscriptionStatus', tempo='$tempo', yearofrelease='$yearofrelease', rating='$rating', is_original='$is_original', category='$category', cast_enabled='$cast_enabled', age_rating='$age_rating', content_Pay_Type='$content_Pay_Type', critic_Rating='$critic_Rating', keywords='$keywords', episodeNumber='$episodeNumber', pid='$pid', pName='$pName', ptype='$ptype', release_Date='$release_Date', season_Number='$season_Number', status='$status', subtitleEnable='$subtitleEnable', subtitleLanguageSelected='$subtitleLanguageSelected', userRating='$userRating', share='$share', videoQuality='$videoQuality', favCount='$favCount', f_fav_count='$f_fav_count')"
    }
}