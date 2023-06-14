package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.hungama.music.HungamaMusicApp
import com.hungama.music.R
import com.hungama.music.data.model.*
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.ThreeDotsClickedEvent
import com.hungama.music.player.audioplayer.TracksContract
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.viewmodel.MoodRadioViewModel
import com.hungama.music.ui.main.viewmodel.PlayableContentViewModel
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.CommonUtils.addFragment
import com.hungama.music.utils.CommonUtils.getCommaSeparatedStringToArray
import com.hungama.music.utils.Constant.SLEEP_TIMER_MENU_ITEM
import com.hungama.music.utils.customview.bottomsheet.SuperBottomSheetFragment
import com.hungama.music.utils.preference.SharedPrefHelper
import kotlinx.android.synthetic.main.music_player_three_dots_bottom_sheet.*
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import androidx.lifecycle.Observer
import androidx.media3.common.Player
import androidx.media3.common.util.Util
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.player.audioplayer.Injection
import com.hungama.music.player.audioplayer.services.AudioPlayerService
import com.hungama.music.player.audioplayer.viewmodel.TracksViewModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant.PLAYER_AUDIO_SONG
import com.hungama.music.utils.Constant.isFromVerticalPlayer
import kotlinx.android.synthetic.main.mood_radio_filter_main.*
import kotlinx.android.synthetic.main.music_player_three_dots_bottom_sheet.progress


class MusicPlayerThreeDotsBottomSheetFragment() : SuperBottomSheetFragment(), MusicPlayBackSettingStreamQuality.OnItemClick,
    MoodRadioFilterSelectMood.OnItemClick, TracksContract.View, View.OnClickListener {
    var onMusicMenuItemClick: OnMusicMenuItemClick? = null
    var mContext: BaseActivity? = null

    var moodRadioViewModel = MoodRadioViewModel()
    var moodRadioPopupMoodModel = MoodRadioFilterModel()
    var moodRadioPopupTempoModel = MoodRadioFilterModel()
    var moodRadioPopupLangModel = MoodRadioFilterModel()
    var moodRadioContentListModel = MoodRadioContentList()
    var playableContentViewModel: PlayableContentViewModel = PlayableContentViewModel()
    var songDataList:ArrayList<Track> = arrayListOf()
    var isSleepTimerAllow = true
    private lateinit var tracksViewModel: TracksContract.Presenter

    companion object{
        var onMusicMenuItemClicked: BaseActivity? = null
        var currentTrack: Track? = null
        fun newInstance(onMusicMenuItemClicked: BaseActivity, currentTrack: Track?): MusicPlayerThreeDotsBottomSheetFragment{
            val fragment = MusicPlayerThreeDotsBottomSheetFragment()
            this.onMusicMenuItemClicked = onMusicMenuItemClicked
            this.currentTrack = currentTrack
            return fragment
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.music_player_three_dots_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.onMusicMenuItemClick = onMusicMenuItemClicked
        this.mContext = onMusicMenuItemClicked

        setTopMenus()
        val userSettingRespModel=SharedPrefHelper.getInstance().getUserPlayBackSetting(Constant.TYPE_MUSICPLAYBACK_SETTING)

        if (userSettingRespModel != null && userSettingRespModel?.data != null){
            isSleepTimerAllow = userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)?.sleep_timer!!
        }
        setLog("TAG", "onViewCreated: currentTrack:${currentTrack?.title} player time:${currentTrack?.playerType}")

        if (currentTrack?.playerType.equals(Constant.PLAYER_PODCAST_AUDIO_ALBUM) || currentTrack?.playerType.equals(Constant.PLAYER_PODCAST_AUDIO_TRACK)){
            txt_music_player_menu_show_similar_song?.text = getString(R.string.menu_str_26)
            txt_music_player_menu_song_details?.text = getString(R.string.menu_str_27)
            txt_music_player_menu_audio_quality?.text = getString(R.string.popup_str_103)

            rl_music_player_menu_show_similar_song?.visibility = View.GONE
            rl_music_player_menu_share?.visibility = View.GONE
            rl_music_player_menu_playlist?.visibility = View.GONE
            rl_music_player_menu_go_to_album_playlist?.visibility = View.GONE
            rl_music_player_menu_view_album?.visibility = View.GONE
            rl_music_player_menu_show_similar_song?.visibility = View.GONE
            rl_music_player_menu_song_details?.visibility = View.GONE
            rl_music_player_menu_mood?.visibility = View.GONE
            rl_music_player_menu_era?.visibility = View.GONE
            rl_music_player_menu_tempo?.visibility = View.GONE
            rl_music_player_menu_language_preference?.visibility = View.GONE
            if (BaseActivity.isDisplayDiscover){
                rl_music_player_menu_sleep_timer?.visibility = View.GONE
            }else{
                if (isSleepTimerAllow){
                    rl_music_player_menu_sleep_timer?.visibility = View.VISIBLE
                }else{
                    rl_music_player_menu_sleep_timer?.visibility = View.GONE
                }
            }
        }else if (currentTrack?.playerType.equals(Constant.PLAYER_RADIO)
            || currentTrack?.playerType.equals(Constant.PLAYER_LIVE_RADIO)
            || currentTrack?.playerType.equals(Constant.PLAYER_ARTIST_RADIO)
            || currentTrack?.playerType.equals(Constant.PLAYER_ON_DEMAND_RADIO)
            || currentTrack?.playerType.equals(Constant.PLAYER_MOOD_RADIO)){

            rl_music_player_menu_view_album?.visibility = View.GONE
            rl_music_player_menu_playlist?.visibility = View.GONE
            rl_music_player_menu_go_to_queue?.visibility = View.GONE
            rl_music_player_menu_go_to_album_playlist?.visibility = View.GONE
            rl_music_player_menu_view_album?.visibility = View.GONE
            rl_music_player_menu_show_similar_song?.visibility = View.GONE
            rl_music_player_menu_song_details?.visibility = View.GONE
            rl_music_player_menu_playback_speed?.visibility = View.GONE
            rl_music_player_menu_go_to_podcast?.visibility = View.GONE
            rl_music_player_menu_sleep_timer?.visibility = View.GONE
            rl_music_player_menu_audio_quality?.visibility = View.GONE

            if (currentTrack?.playerType.equals(Constant.PLAYER_MOOD_RADIO)){
                if (BaseActivity.isDisplayDiscover){
                    devider8?.visibility = View.GONE
                    rl_music_player_menu_mood?.visibility = View.GONE
                    rl_music_player_menu_era?.visibility = View.GONE
                    rl_music_player_menu_tempo?.visibility = View.GONE
                    rl_music_player_menu_language_preference?.visibility = View.GONE
                }else{
                    setUpMoodRadioPopupMoodViewModel()
                    setUpMoodRadioPopupTempoViewModel()
                    setUpMoodRadioPopupLanguageViewModel()

                    devider15?.visibility = View.GONE
                }
            }else{
                rl_music_player_menu_mood?.visibility = View.GONE
                rl_music_player_menu_era?.visibility = View.GONE
                rl_music_player_menu_tempo?.visibility = View.GONE
                rl_music_player_menu_language_preference?.visibility = View.GONE
                if (BaseActivity.isDisplayDiscover){
                    devider8?.visibility = View.GONE
                }else{
                    devider0?.visibility = View.GONE
                }
            }
        }else{
            if (currentTrack?.pType == DetailPages.PLAYLIST_DETAIL_PAGE.value || currentTrack?.pType == DetailPages.PLAYLIST_DETAIL_ADAPTER.value){
                txt_music_player_menu_go_to_album_playlist?.text = getString(R.string.go_to_playlist)
            }else if (currentTrack?.pType == DetailPages.ALBUM_DETAIL_PAGE.value || currentTrack?.pType == DetailPages.ALBUM_DETAIL_ADAPTER.value){
                txt_music_player_menu_go_to_album_playlist?.text = getString(R.string.menu_str_13)
            }else{
                rl_music_player_menu_go_to_album_playlist?.visibility = View.GONE
            }
            rl_music_player_menu_view_album.visibility = View.GONE
            txt_music_player_menu_show_similar_song?.text = getString(R.string.menu_str_25)
            txt_music_player_menu_song_details?.text = getString(R.string.menu_str_14)
            devider7?.visibility = View.VISIBLE
            rl_music_player_menu_show_similar_song?.visibility = View.GONE
            rl_music_player_menu_playback_speed?.visibility = View.GONE
            rl_music_player_menu_go_to_podcast?.visibility = View.GONE
            rl_music_player_menu_mood?.visibility = View.GONE
            rl_music_player_menu_era?.visibility = View.GONE
            rl_music_player_menu_tempo?.visibility = View.GONE
            rl_music_player_menu_language_preference?.visibility = View.GONE
            if (isSleepTimerAllow){
                rl_music_player_menu_sleep_timer?.visibility = View.VISIBLE
            }else{
                rl_music_player_menu_sleep_timer?.visibility = View.GONE
            }
        }
        if (BaseActivity.isDisplayDiscover){
            rl_music_player_menu_share?.visibility = View.VISIBLE
            rl_music_player_menu_playback_speed?.visibility = View.GONE
        }else{
            rl_music_player_menu_share?.visibility = View.GONE
        }

        music_player_menu_close.setOnClickListener {
            if (currentTrack?.playerType.equals(Constant.PLAYER_MOOD_RADIO)){
                //close()
            }
            closeDialog()

            CoroutineScope(Dispatchers.IO).launch {
                var hashMap = HashMap<String,String>()
                hashMap.put(EventConstant.SOURCE_EPROPERTY, "Music Player Three Dots")
                hashMap.put(EventConstant.ACTION_EPROPERTY,getString(R.string.music_player_str_15))
                EventManager.getInstance().sendEvent(ThreeDotsClickedEvent(hashMap))
            }

        }
        /*rl_music_player_menu_download.setOnClickListener {
            closeDialog()
            var hashMap = HashMap<String,String>()
            hashMap.put(EventConstant.ACTION_EPROPERTY,getString(R.string.music_player_menu_download))
            EventManager.getInstance().sendEvent(ThreeDotsClickedEvent(hashMap))
        }*/

        rl_music_player_menu_playlist.setOnClickListener {
            if (SharedPrefHelper.getInstance().isUserLoggedIn()) {
                val hashMap = HashMap<String, String>()
                hashMap.put(EventConstant.SOURCE_EPROPERTY, "Music Player Three Dots")
                hashMap.put(EventConstant.ACTION_EPROPERTY, getString(R.string.menu_str_3))
                EventManager.getInstance().sendEvent(ThreeDotsClickedEvent(hashMap))
                val addToPlaylistMenuFragment: AddToPlaylistMenuFragment? =
                    AddToPlaylistMenuFragment(
                        currentTrack?.id.toString(),
                        currentTrack?.title.toString(),
                        currentTrack?.playerType.toString()
                    )
                addToPlaylistMenuFragment?.show(
                    activity?.supportFragmentManager!!,
                    "AddToPlaylistMenuFragment"
                )
            }
        }

        /*rl_music_player_menu_queue.setOnClickListener {
            var hashMap = HashMap<String,String>()
            hashMap.put(EventConstant.ACTION_EPROPERTY,getString(R.string.music_player_menu_add_to_queue))
            EventManager.getInstance().sendEvent(ThreeDotsClickedEvent(hashMap))
        }*/

        rl_music_player_menu_view_album.setOnClickListener {
            if (activity is BaseActivity) {
                val playableContentModel=HungamaMusicApp.getInstance().getEventData(""+currentTrack?.id.toString())
                val bundle = Bundle()
                val pidList = getCommaSeparatedStringToArray(playableContentModel.pid)
                if (!pidList.isNullOrEmpty()){

                    val hashMap = HashMap<String,String>()
                    hashMap.put(EventConstant.SOURCE_EPROPERTY, "Music Player Three Dots")
                    hashMap.put(EventConstant.ACTION_EPROPERTY,getString(R.string.menu_str_23))
                    EventManager.getInstance().sendEvent(ThreeDotsClickedEvent(hashMap))

                    val pid = pidList[0]
                    bundle.putString("image", currentTrack?.image)
                    bundle.putString("id", pid)
                    bundle.putString("playerType", currentTrack?.playerType)

                    val albumDetailFragment = AlbumDetailFragment()
                    albumDetailFragment.arguments = bundle

                    closeDialog()
                    if (BaseActivity.isDisplayDiscover){
                        (activity as BaseActivity).toggleSheetBehavior()
                    }
                    (activity as BaseActivity).addFragment(
                        R.id.fl_container,
                        this,
                        albumDetailFragment,
                        false
                    )
                }else{
                    val messageModel = MessageModel(getString(R.string.no_album_found), MessageType.NEUTRAL, true)
                    CommonUtils.showToast(requireContext(), messageModel)
                }
            }

        }

        /*rl_music_player_menu_view_artist.setOnClickListener {
            var hashMap = HashMap<String,String>()
            hashMap.put(EventConstant.ACTION_EPROPERTY,getString(R.string.music_player_menu_view_artist))
            EventManager.getInstance().sendEvent(ThreeDotsClickedEvent(hashMap))
        }*/

        rl_music_player_menu_audio_quality.setOnClickListener {
            try {
              /*  val sheet = MusicPlayBackSettingStreamQuality(CommonUtils.getStreamQualityDummyData(
                    QualityAction.MUSIC_PLAYBACK_STREAM_QUALITY,, "music_player_screen"),""+currentTrack?.playerType!!, this@MusicPlayerThreeDotsBottomSheetFragment)*/



                val sheet = MusicPlayBackSettingStreamQuality(CommonUtils.getStreamQualityDummyData(
                    QualityAction.MUSIC_PLAYBACK_STREAM_QUALITY, "music_player_screen"),"",this@MusicPlayerThreeDotsBottomSheetFragment)
                sheet.show(activity?.supportFragmentManager!!, "MusicPlayBackSettingStreamQuality")
                      closeDialog()

                val hashMap = HashMap<String,String>()
                hashMap.put(EventConstant.SOURCE_EPROPERTY, "Music Player Three Dots")
                hashMap.put(EventConstant.ACTION_EPROPERTY,getString(R.string.menu_str_28))
                EventManager.getInstance().sendEvent(ThreeDotsClickedEvent(hashMap))
            }catch (e:Exception){

            }

        }

        rl_music_player_menu_share.setOnClickListener {
            closeDialog()
            val hashMap = HashMap<String,String>()
            hashMap.put(EventConstant.SOURCE_EPROPERTY, "Music Player Three Dots")
            hashMap.put(EventConstant.ACTION_EPROPERTY,getString(R.string.menu_str_1))
            EventManager.getInstance().sendEvent(ThreeDotsClickedEvent(hashMap))

            val eventData=HungamaMusicApp.getInstance().getEventData(""+currentTrack?.id)
            if(eventData!=null&&!TextUtils.isEmpty(eventData?.share)){
                setLog("TAG", "rl_music_player_menu_share share url:${eventData?.share}")
            var shareurl=getString(R.string.music_player_str_18)+" "+ eventData?.share
                shareurl += "play/"
            Utils.shareItem(requireActivity(),shareurl)
            }else{
                setLog("TAG", "rl_music_player_menu_share share is empty")
            }


        }

        rl_music_player_menu_share_story.setOnClickListener {
            var hashMap = HashMap<String, String>()
            hashMap.put(EventConstant.SOURCE_EPROPERTY, "Music Player Three Dots")
            hashMap.put(EventConstant.ACTION_EPROPERTY, getString(R.string.general_str_7))
            EventManager.getInstance().sendEvent(ThreeDotsClickedEvent(hashMap))
            if (onMusicMenuItemClick != null) {
                closeDialog()
                setLog("TAG", "onViewCreated: woking")
                onMusicMenuItemClick?.onMusicMenuItemClick(Constant.SHARE_STORY_MENU_ITEM)
            }





        }

        rl_music_player_menu_song_details.setOnClickListener {
            closeDialog()
            if (BaseActivity.isDisplayDiscover){
                (activity as BaseActivity).toggleSheetBehavior()
            }

            if (activity is BaseActivity) {

                val hashMap = HashMap<String,String>()
                hashMap.put(EventConstant.SOURCE_EPROPERTY, "Music Player Three Dots")
                hashMap.put(EventConstant.ACTION_EPROPERTY,getString(R.string.menu_str_14))
                EventManager.getInstance().sendEvent(ThreeDotsClickedEvent(hashMap))


                if (currentTrack?.playerType.equals(Constant.PLAYER_PODCAST_AUDIO_ALBUM) || currentTrack?.playerType.equals(Constant.PLAYER_PODCAST_AUDIO_TRACK)){
                    val bundle = Bundle()
                    bundle.putString("id",""+currentTrack?.parentId)
                    bundle.putString("image", ""+currentTrack?.pImage)
                    bundle.putString("playerType", ""+currentTrack?.playerType)
                    val podcastDetailFragment = PodcastDetailsFragment()
                    podcastDetailFragment.arguments = bundle
                    if (activity != null && activity is MainActivity){
                        (activity as MainActivity).unSelectAllTab()
                    }
                    if (BaseActivity.isDisplayDiscover){
                        (activity as BaseActivity).toggleSheetBehavior()
                    }
                    (activity as BaseActivity).addFragment(
                        R.id.fl_container,
                        this,
                        podcastDetailFragment,
                        false
                    )
                }else{
                    val bundle = Bundle()
                    bundle.putString("id",""+currentTrack?.id)
                    bundle.putString("image", ""+currentTrack?.image)
                    bundle.putString("playerType", ""+currentTrack?.playerType)
                    if (!BaseActivity.isDisplayDiscover){
                        bundle.putBoolean(isFromVerticalPlayer, true)
                    }
                    if (activity != null && activity is MainActivity){
                        (activity as MainActivity).unSelectAllTab()
                    }
                    val songDetailFragment = SongDetailFragment()
                    songDetailFragment.arguments = bundle

                    (activity as BaseActivity).addFragment(
                        R.id.fl_container,
                        this,
                        songDetailFragment,
                        false
                    )
                }

            }
        }

        rl_music_player_menu_show_similar_song.setOnClickListener {

            val hashMap = HashMap<String,String>()
            hashMap.put(EventConstant.SOURCE_EPROPERTY, "Music Player Three Dots")
            hashMap.put(EventConstant.ACTION_EPROPERTY,getString(R.string.menu_str_25))
            EventManager.getInstance().sendEvent(ThreeDotsClickedEvent(hashMap))

            closeDialog()
            if (BaseActivity.isDisplayDiscover){
                (activity as BaseActivity).toggleSheetBehavior()
            }
            val bundle = Bundle()
            bundle.putString("id",""+currentTrack?.id)
            bundle.putString("image", ""+currentTrack?.image)
            bundle.putString("playerType", ""+currentTrack?.playerType)
            val similarSongFragment = SimilarSongsFragment()
            similarSongFragment.arguments = bundle

            (activity as BaseActivity).addFragment(
                R.id.fl_container,
                this,
                similarSongFragment,
                false
            )
        }

        rl_music_player_menu_sleep_timer.setOnClickListener {
            if (onMusicMenuItemClick != null) {

                val hashMap = HashMap<String,String>()
                hashMap.put(EventConstant.SOURCE_EPROPERTY, "Music Player Three Dots")
                hashMap.put(EventConstant.ACTION_EPROPERTY,getString(R.string.menu_str_29))
                EventManager.getInstance().sendEvent(ThreeDotsClickedEvent(hashMap))

                closeDialog()
                onMusicMenuItemClick?.onMusicMenuItemClick(SLEEP_TIMER_MENU_ITEM)
            }
        }

        rl_music_player_menu_go_to_podcast.setOnClickListener {
            if (currentTrack?.playerType.equals(Constant.PLAYER_PODCAST_AUDIO_ALBUM) || currentTrack?.playerType.equals(Constant.PLAYER_PODCAST_AUDIO_TRACK)){

                val hashMap = HashMap<String,String>()
                hashMap.put(EventConstant.SOURCE_EPROPERTY, "Music Player Three Dots")
                hashMap.put(EventConstant.ACTION_EPROPERTY,getString(R.string.menu_str_27))
                EventManager.getInstance().sendEvent(ThreeDotsClickedEvent(hashMap))
                if (activity is MainActivity) {
                    (activity as MainActivity)?.unSelectAllTab()
                }
                setLog("TAG", "rl_music_player_menu_go_to_podcast: currentTrack:${currentTrack}")
                val bundle = Bundle()
                bundle.putString(Constant.defaultContentId,""+currentTrack?.parentId)
                bundle.putString("image", ""+currentTrack?.pImage)
                bundle.putString(Constant.defaultContentPlayerType, ""+currentTrack?.playerType)
                if (!BaseActivity.isDisplayDiscover){
                    bundle.putBoolean(isFromVerticalPlayer, true)
                }
                val podcastDetailFragment = PodcastDetailsFragment()
                podcastDetailFragment.arguments = bundle
                closeDialog()
                (activity as BaseActivity).addFragment(
                    R.id.fl_container,
                    this,
                    podcastDetailFragment,
                    false
                )
            }
        }

        txt_music_player_menu_go_to_queue.setOnClickListener {

            val hashMap = HashMap<String,String>()
            hashMap.put(EventConstant.SOURCE_EPROPERTY, "Music Player Three Dots")
            hashMap.put(EventConstant.ACTION_EPROPERTY,getString(R.string.popup_str_100))
            EventManager.getInstance().sendEvent(ThreeDotsClickedEvent(hashMap))

            closeDialog()
            if (BaseActivity.isDisplayDiscover){
                (activity as MainActivity).openQueueFragment()
            }else{
                if(activity != null){
                    BaseActivity.isNewSwipablePlayerOpen = false
                    addFragment(requireContext(),R.id.fl_container, this, QueueFragment(), false)
                    (activity as MainActivity).changeMiniPlayerState(BottomSheetBehavior.STATE_COLLAPSED)
                }
            }
        }

        rl_music_player_menu_go_to_album_playlist.setOnClickListener {

            val hashMap = HashMap<String,String>()
            hashMap.put(EventConstant.SOURCE_EPROPERTY, "Music Player Three Dots")
            hashMap.put(EventConstant.ACTION_EPROPERTY,getString(R.string.go_to_playlist))
            EventManager.getInstance().sendEvent(ThreeDotsClickedEvent(hashMap))
            if (activity is MainActivity) {
                (activity as MainActivity)?.unSelectAllTab()
            }
            if (currentTrack?.pType == DetailPages.PLAYLIST_DETAIL_PAGE.value || currentTrack?.pType == DetailPages.PLAYLIST_DETAIL_ADAPTER.value){
                if (activity is BaseActivity) {



                    val playableContentModel=HungamaMusicApp.getInstance().getEventData(""+currentTrack?.id.toString())
                    val bundle = Bundle()
                    if (currentTrack != null && !TextUtils.isEmpty(currentTrack?.parentId)){
                        val hashMap = HashMap<String,String>()
                        hashMap.put(EventConstant.SOURCE_EPROPERTY, "Music Player Three Dots")
                        hashMap.put(EventConstant.ACTION_EPROPERTY,getString(R.string.go_to_playlist))
                        EventManager.getInstance().sendEvent(ThreeDotsClickedEvent(hashMap))

                        bundle.putString("image", currentTrack?.image)
                        bundle.putString("id", currentTrack?.parentId)
                        bundle.putString("playerType", currentTrack?.playerType)

                        val playlistDetailFragment = PlaylistDetailFragmentDynamic.newInstance(0)
                        playlistDetailFragment.arguments = bundle

                        closeDialog()
                        if (BaseActivity.isDisplayDiscover){
                            (activity as BaseActivity).toggleSheetBehavior()
                        }
                        (activity as BaseActivity).addFragment(
                            R.id.fl_container,
                            this,
                            playlistDetailFragment,
                            false
                        )
                    }else{
                        val messageModel = MessageModel(getString(R.string.no_playlist_found), MessageType.NEUTRAL, true)
                        CommonUtils.showToast(requireContext(), messageModel)
                    }
                }
            }else if (currentTrack?.pType == DetailPages.ALBUM_DETAIL_PAGE.value || currentTrack?.pType == DetailPages.ALBUM_DETAIL_ADAPTER.value
                || currentTrack?.pType == DetailPages.RECOMMENDED_SONG_LIST_PAGE.value){
                if (activity is BaseActivity) {
                    val playableContentModel=HungamaMusicApp.getInstance().getEventData(""+currentTrack?.id.toString())
                    val bundle = Bundle()
                    val pidList = getCommaSeparatedStringToArray(playableContentModel.pid)
                    if (!pidList.isNullOrEmpty()){

                        val hashMap = HashMap<String,String>()
                        hashMap.put(EventConstant.SOURCE_EPROPERTY, "Music Player Three Dots")
                        hashMap.put(EventConstant.ACTION_EPROPERTY,getString(R.string.menu_str_13))
                        EventManager.getInstance().sendEvent(ThreeDotsClickedEvent(hashMap))

                        val pid = pidList[0]
                        bundle.putString("image", currentTrack?.image)
                        bundle.putString("id", pid)
                        bundle.putString("playerType", currentTrack?.playerType)

                        val albumDetailFragment = AlbumDetailFragment()
                        albumDetailFragment.arguments = bundle

                        closeDialog()
                        if (BaseActivity.isDisplayDiscover){
                            (activity as BaseActivity).toggleSheetBehavior()
                        }
                        (activity as BaseActivity).addFragment(
                            R.id.fl_container,
                            this,
                            albumDetailFragment,
                            false
                        )
                    }else{
                        val messageModel = MessageModel(getString(R.string.no_album_found), MessageType.NEUTRAL, true)
                        CommonUtils.showToast(requireContext(), messageModel)
                    }
                }
            }
        }

        rl_music_player_menu_playback_speed.setOnClickListener {
            if (activity != null){
                val hashMap = HashMap<String,String>()
                hashMap.put(EventConstant.SOURCE_EPROPERTY, "Music Player Three Dots")
                hashMap.put(EventConstant.ACTION_EPROPERTY,getString(R.string.popup_str_102))
                EventManager.getInstance().sendEvent(ThreeDotsClickedEvent(hashMap))

                val sheet = SpeedChangeDialog((activity as BaseActivity), BaseActivity.songDataList?.get(BaseActivity.nowPlayingCurrentIndex()))
                sheet.show(activity?.supportFragmentManager!!, "SpeedChangeDialog")
            }
        }

        rl_music_player_menu_era.setOnClickListener {

            val hashMap = HashMap<String,String>()
            hashMap.put(EventConstant.SOURCE_EPROPERTY, "Music Player Three Dots")
            hashMap.put(EventConstant.ACTION_EPROPERTY,getString(R.string.music_player_str_43))
            EventManager.getInstance().sendEvent(ThreeDotsClickedEvent(hashMap))

            val sheet = MoodRadioFilterEra(this)
            sheet.show(activity?.supportFragmentManager!!, "MoodRadioFilterSelectTempo")
        }

        rl_music_player_menu_cast?.setOnClickListener {
            val hashMap = HashMap<String, String>()
            hashMap.put(EventConstant.SOURCE_EPROPERTY, "Music Player Three Dots")
            hashMap.put(EventConstant.ACTION_EPROPERTY, getString(R.string.cast_screen))
            EventManager.getInstance().sendEvent(ThreeDotsClickedEvent(hashMap))
            if (onMusicMenuItemClick != null) {
                closeDialog()
                setLog("TAG", "onViewCreated: CAST_SCREEN_MENU_ITEM")
                onMusicMenuItemClick?.onMusicMenuItemClick(Constant.CAST_SCREEN_MENU_ITEM)
            }
        }
        tracksViewModel = TracksViewModel(Injection.provideTrackRepository(), this)
    }

    // extension function to get / download bitmap from url
    fun URL.toBitmap(): Bitmap? {
        return try {
            BitmapFactory.decodeStream(openStream())
        } catch (e: IOException) {
            null
        }
    }


    // extension function to save an image to internal storage
    fun Bitmap.saveToInternalStorage(): File? {
        setLog("TAG", " share fb story : saveToInternalStorage")


        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir: File = mContext?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        val fileInFolder = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )

//        val fileInFolder=File.createTempFile("${currentTrack?.id}", ".jpg", context?.cacheDir)


        setLog("TAG", "share fb story :  fileInFolder : ${fileInFolder.absolutePath}");


        return try {

            // get the file output stream
            val stream: OutputStream = FileOutputStream(fileInFolder)

            // compress bitmap
            compress(Bitmap.CompressFormat.JPEG, 90, stream)

            // flush the stream
            stream.flush()

            // close stream
            stream.close()



            setLog("TAG", " share fb story : saveToInternalStorage file" + fileInFolder.absolutePath)

            // return the saved image uri
            fileInFolder
        } catch (e: IOException) { // catch the exception
            e.printStackTrace()
            null
        }
    }

/*
    var activityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            run {
                setLog("TAG", "result:${result}")
            }
        }*/

    override fun getCornerRadius() =
        requireContext().resources.getDimension(R.dimen.common_popup_round_corner)

    override fun getStatusBarColor() = Color.RED

    override fun isSheetAlwaysExpanded(): Boolean = true
    override fun getExpandedHeight(): Int {
        if (currentTrack?.playerType.equals(Constant.PLAYER_RADIO)
            || currentTrack?.playerType.equals(Constant.PLAYER_LIVE_RADIO)
            || currentTrack?.playerType.equals(Constant.PLAYER_ARTIST_RADIO)
            || currentTrack?.playerType.equals(Constant.PLAYER_ON_DEMAND_RADIO)
            || currentTrack?.playerType.equals(Constant.PLAYER_MOOD_RADIO)) {
                if (currentTrack?.playerType.equals(Constant.PLAYER_MOOD_RADIO)){
                    if (BaseActivity.isDisplayDiscover){
                        return requireContext().resources.getDimension(R.dimen.dimen_210).toInt()
                    }else{
                        return requireContext().resources.getDimension(R.dimen.dimen_360).toInt()
                    }
                }else{
                    if (BaseActivity.isDisplayDiscover){
                        return requireContext().resources.getDimension(R.dimen.dimen_210).toInt()
                    }else{
                        return requireContext().resources.getDimension(R.dimen.dimen_150).toInt()
                    }
                }
        }else if (currentTrack?.playerType.equals(Constant.PLAYER_PODCAST_AUDIO_ALBUM) || currentTrack?.playerType.equals(Constant.PLAYER_PODCAST_AUDIO_TRACK)){
            if (BaseActivity.isDisplayDiscover){
                return requireContext().resources.getDimension(R.dimen.dimen_360).toInt()
            }else{
                return requireContext().resources.getDimension(R.dimen.dimen_410).toInt()
            }
        } else {
            if (BaseActivity.isDisplayDiscover){
                return requireContext().resources.getDimension(R.dimen.dimen_520).toInt()
            }else{
                if (currentTrack?.playerType.toString() == PLAYER_AUDIO_SONG && (currentTrack?.pType == DetailPages.PLAYLIST_DETAIL_PAGE.value || currentTrack?.pType == DetailPages.PLAYLIST_DETAIL_ADAPTER.value || currentTrack?.pType == DetailPages.ALBUM_DETAIL_PAGE.value || currentTrack?.pType == DetailPages.ALBUM_DETAIL_ADAPTER.value)){
                    if (!isSleepTimerAllow){
                        return requireContext().resources.getDimension(R.dimen.dimen_575).toInt()
                    }
                    return requireContext().resources.getDimension(R.dimen.dimen_625).toInt()
                }else if (currentTrack?.playerType.toString() == PLAYER_AUDIO_SONG){
                    if (!isSleepTimerAllow){
                        return requireContext().resources.getDimension(R.dimen.dimen_525).toInt()
                    }
                    return requireContext().resources.getDimension(R.dimen.dimen_575).toInt()
                }else{
                    return requireContext().resources.getDimension(R.dimen.dimen_475).toInt()
                }
                //return requireContext().resources.getDimension(R.dimen.dimen_475).toInt()
            }
        }

    }

    /*override fun getPeekHeight(): Int = requireContext().resources.getDimension(R.dimen.dimen_540).toInt()*/

    override fun getBackgroundColor(): Int =
        requireContext().resources.getColor(R.color.transparent)

    interface OnMusicMenuItemClick {
        fun onMusicMenuItemClick(menuId: Int)
    }

    override fun onUserClickOnQuality(position: Int, settingType: Int) {
       /* setLog("MYPosition","POSITION${position}")
        saveSetting(CommonUtils.getStreamQualityDummyData().get(position).title)*/
    }

    private fun saveSetting(title: String?) {

        var userViewModel: UserViewModel? = null
        userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)
        if (ConnectionUtil(requireContext()).isOnline) {

            try {

                val userSettingRespModel=SharedPrefHelper.getInstance().getUserPlayBackSetting(Constant.TYPE_MUSICPLAYBACK_SETTING)
                if(userSettingRespModel!=null && userSettingRespModel?.data != null){
                    val mainJson = JSONObject()
                    val prefArrays = JSONArray()

                    val emailSettingJson = JSONObject()
                    val userSettingsData = userSettingRespModel?.data?.data?.get(0)?.preference?.get(0)
                    emailSettingJson.put("autoPlay",userSettingsData?.autoPlay)
                    emailSettingJson.put("streaming_quality",title?.trim().toString())
                    emailSettingJson.put("music_language_preference", userSettingsData?.music_language_preference)
                    emailSettingJson.put("music_language_preference_title", userSettingsData?.music_language_preference_title)
                    emailSettingJson.put("equalizer","equalizer")
                    emailSettingJson.put("sleep_timer",userSettingsData?.sleep_timer)
                    emailSettingJson.put("show_lyrics",userSettingsData?.show_lyrics)
                    emailSettingJson.put("gapless",userSettingsData?.gapless)
                    emailSettingJson.put("crossfade",userSettingsData?.crossfade)
                    emailSettingJson.put("smooth_song_transition",userSettingsData?.smooth_song_transition)

                    mainJson.put("type", Constant.TYPE_MUSICPLAYBACK_SETTING)
                    mainJson.put("preference", prefArrays)

                    prefArrays.put(emailSettingJson)
                    userViewModel?.saveUserPref(
                        requireContext(),
                        mainJson.toString(),
                        Constant.TYPE_MUSICPLAYBACK_SETTING
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }


        } else {
        }
    }
    private fun appInstalledOrNot(uri: String): Boolean {
        val pm: PackageManager = activity?.getPackageManager()!!
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
        }
        return false
    }

    private fun setUpMoodRadioPopupMoodViewModel() {
        if (ConnectionUtil(requireContext()).isOnline) {
            moodRadioViewModel = ViewModelProvider(
                this
            ).get(MoodRadioViewModel::class.java)
            moodRadioViewModel?.getMoodRadioMoodPopupList(requireActivity())?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            setMoodRadioPopupMoodData(it?.data!!)
                        }

                        Status.LOADING ->{
                            setProgressBarVisible(true)
                        }

                        Status.ERROR ->{
                            setProgressBarVisible(false)
                            Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                        }
                    }
                })
        } else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }

    fun setMoodRadioPopupMoodData(moodRadioFilterModel: MoodRadioFilterModel) {

        if (moodRadioFilterModel != null ) {
            moodRadioPopupMoodModel = moodRadioFilterModel
            rl_music_player_menu_mood?.setOnClickListener {

                val hashMap = HashMap<String,String>()
                hashMap.put(EventConstant.SOURCE_EPROPERTY, "Music Player Three Dots")
                hashMap.put(EventConstant.ACTION_EPROPERTY,getString(R.string.music_player_str_21))
                EventManager.getInstance().sendEvent(ThreeDotsClickedEvent(hashMap))

                val sheet = MoodRadioFilterSelectMood(moodRadioFilterModel, this)
                sheet.show(activity?.supportFragmentManager!!, "MoodRadioFilterSelectMood")
            }
        }
    }

    private fun setUpMoodRadioPopupTempoViewModel() {
        if (ConnectionUtil(requireContext()).isOnline) {
            moodRadioViewModel = ViewModelProvider(
                this
            ).get(MoodRadioViewModel::class.java)
            moodRadioViewModel?.getMoodRadioTempoPopupList(requireActivity())?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            setMoodRadioPopupTempoData(it?.data!!)
                        }

                        Status.LOADING ->{
                            setProgressBarVisible(true)
                        }

                        Status.ERROR ->{
                            setProgressBarVisible(false)
                            Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                        }
                    }
                })
        } else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }



    fun setMoodRadioPopupTempoData(moodRadioFilterModel: MoodRadioFilterModel) {

        if (moodRadioFilterModel != null ) {
            moodRadioPopupTempoModel = moodRadioFilterModel
            rl_music_player_menu_tempo?.setOnClickListener {
                val hashMap = HashMap<String,String>()
                hashMap.put(EventConstant.SOURCE_EPROPERTY, "Music Player Three Dots")
                hashMap.put(EventConstant.ACTION_EPROPERTY,getString(R.string.tempo))
                EventManager.getInstance().sendEvent(ThreeDotsClickedEvent(hashMap))

                val sheet = MoodRadioFilterSelectTempo(moodRadioFilterModel, this)
                sheet.show(activity?.supportFragmentManager!!, "MoodRadioFilterSelectTempo")
            }
        }
    }

    private fun setUpMoodRadioPopupLanguageViewModel() {
        moodRadioViewModel = ViewModelProvider(
            this
        ).get(MoodRadioViewModel::class.java)


        if (ConnectionUtil(requireContext()).isOnline) {
            moodRadioViewModel?.getMoodRadioLanguagePopupList(requireActivity())?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            setMoodRadioPopupLanguageData(it?.data!!)
                        }

                        Status.LOADING ->{
                            setProgressBarVisible(true)
                        }

                        Status.ERROR ->{
                            setProgressBarVisible(false)
                            Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                        }
                    }
                })
        } else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }

    }

    fun setMoodRadioPopupLanguageData(moodRadioFilterModel: MoodRadioFilterModel) {

        if (moodRadioFilterModel != null ) {
            moodRadioPopupLangModel = moodRadioFilterModel
            rl_music_player_menu_language_preference?.setOnClickListener {

                val hashMap = HashMap<String,String>()
                hashMap.put(EventConstant.SOURCE_EPROPERTY, "Music Player Three Dots")
                hashMap.put(EventConstant.ACTION_EPROPERTY,getString(R.string.discover_str_31))
                EventManager.getInstance().sendEvent(ThreeDotsClickedEvent(hashMap))

                val sheet = MoodRadioFilterSelectLanguage(moodRadioFilterModel, this)
                sheet.show(activity?.supportFragmentManager!!, "MoodRadioFilterSelectMood")
            }
        }
    }

    private fun setProgressBarVisible(it: Boolean) {
        if (it) {
            progress?.visibility = View.VISIBLE
        } else {
            progress?.visibility = View.GONE
        }
    }

    override fun onUserClick(position: Int, type: Int) {
        if (type == Constant.RADIO_MOOD_LIST){
            //setLog("Type-1", type.toString())
            //setLog("Type-1", moodRadioPopupMoodModel.data?.body?.rows?.get(0)?.items?.get(position)?.title.toString())
            tvMoodTitle?.text = moodRadioPopupMoodModel.data?.body?.rows?.get(0)?.items?.get(position)?.title.toString()
            SharedPrefHelper.getInstance().setMoodRadioMoodFilterId(moodRadioPopupMoodModel.data?.body?.rows?.get(0)?.items?.get(position)?.moodid!!)
            SharedPrefHelper.getInstance().setMoodRadioMoodFilterTitle(moodRadioPopupMoodModel.data?.body?.rows?.get(0)?.items?.get(position)?.title.toString())
        }else if (type == Constant.RADIO_TEMPO_LIST){
            //setLog("Type-2", type.toString())
            setLog("Type-2", moodRadioPopupTempoModel.data?.body?.rows?.get(0)?.items?.get(position)?.title.toString())
//            tvTempoTitle.text = moodRadioPopupTempoModel.data?.body?.rows?.get(0)?.items?.get(position)?.title.toString()
            SharedPrefHelper.getInstance().setMoodRadioTempoFilterId(moodRadioPopupTempoModel.data?.body?.rows?.get(0)?.items?.get(position)?.tempoid!!)
            SharedPrefHelper.getInstance().setMoodRadioTempoFilterTitle(moodRadioPopupTempoModel.data?.body?.rows?.get(0)?.items?.get(position)?.title.toString())
        }else if (type == Constant.RADIO_LANGUAGE_LIST){
            //setLog("Type-3", type.toString())
            //setLog("Type-3", moodRadioPopupLangModel.data?.body?.rows?.get(0)?.items?.get(position)?.title.toString())
         //   tvLanguageTitle.text = moodRadioPopupLangModel.data?.body?.rows?.get(0)?.items?.get(position)?.title.toString()
            SharedPrefHelper.getInstance().setMoodRadioLanguageFilterId(moodRadioPopupLangModel.data?.body?.rows?.get(0)?.items?.get(position)?.code!!.toString())
            SharedPrefHelper.getInstance().setMoodRadioLanguageFilterTitle(moodRadioPopupLangModel.data?.body?.rows?.get(0)?.items?.get(position)?.title.toString())
        }else if (type == Constant.RADIO_ERA_LIST){
         //   tvEraTitle.text = SharedPrefHelper.getInstance().getMoodRadioEraFilterMinRange().toString() + " - " + SharedPrefHelper.getInstance().getMoodRadioEraFilterMaxRange().toString()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tracksViewModel.onCleanup()
        mContext?.removeMusicPlayerThreeDotMenuEventCallBack()
        onMusicMenuItemClicked = null
        currentTrack = null
    }

    private fun close() {
        setUpMoodRadioContentList()
    }

    private fun setUpMoodRadioContentList() {



        if (ConnectionUtil(requireContext()).isOnline) {
            moodRadioViewModel = ViewModelProvider(
                this
            ).get(MoodRadioViewModel::class.java)

            moodRadioViewModel?.getMoodRadioContentList(requireActivity(), SharedPrefHelper.getInstance().getMoodRadioMoodFilterId()!!, 0, 20,
                SharedPrefHelper.getInstance().getMoodRadioLanguageFilterId().toString(),
                (SharedPrefHelper.getInstance().getMoodRadioEraFilterMinRange().toString()+"|" + SharedPrefHelper.getInstance().getMoodRadioEraFilterMaxRange().toString()),
                SharedPrefHelper.getInstance().getMoodRadioTempoFilterTitle()?.first().toString()
            )?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            setMoodRadioContentListData(it?.data!!)
                        }

                        Status.LOADING ->{
                            setProgressBarVisible(true)
                        }

                        Status.ERROR ->{
                            setProgressBarVisible(false)
                            Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                        }
                    }
                })
        } else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }

    fun setMoodRadioContentListData(moodRadioContentList: MoodRadioContentList) {

        if (moodRadioContentList != null ) {
            moodRadioContentListModel = moodRadioContentList
            if(moodRadioContentList?.size!!>0){
                getPlayableContentUrl(moodRadioContentList?.get(0)?.data?.id!!)
            }else{
                dismiss()
            }
        }
    }

    /**
     * get Playable url for song : 21
     *
     * @param id
     */
    fun getPlayableContentUrl(id:String){
        if (ConnectionUtil(context).isOnline) {
            playableContentViewModel = ViewModelProvider(
                this
            ).get(PlayableContentViewModel::class.java)

            playableContentViewModel?.getPlayableContentList(requireContext(), id)?.observe(this,
                Observer {
                    when(it.status){
                        com.hungama.music.data.webservice.utils.Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                //setLog(TAG, "isViewLoading $it")
                                if(!TextUtils.isEmpty(it?.data?.data?.head?.headData?.misc?.url)){
                                    setPlayableContentListData(it?.data)
                                }else {
                                    nextId++
                                    if (nextId < moodRadioContentListModel?.size!!) {
                                        getPlayableContentUrl(moodRadioContentListModel?.get(nextId)?.data?.id!!.toString())
                                    }
                                }
                            }
                        }

                        com.hungama.music.data.webservice.utils.Status.LOADING ->{
                            setProgressBarVisible(true)
                        }

                        com.hungama.music.data.webservice.utils.Status.ERROR ->{
                            setProgressBarVisible(false)
                            Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                        }
                    }
                })
        } else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }

    var nextId = 0

    fun setPlayableContentListData(playableContentModel: PlayableContentModel) {

        if (playableContentModel != null ) {
            setLog("PlayableItem", playableContentModel?.data?.head?.headData?.id.toString())
            songDataList = arrayListOf()

            for (i in moodRadioContentListModel?.indices!!){

                if (playableContentModel?.data?.head?.headData?.id == moodRadioContentListModel?.get(i)?.data?.id){
                    setPodcastDataList(playableContentModel, moodRadioContentListModel.get(i))
                }else{
                    setPodcastDataList(null, moodRadioContentListModel.get(i))
                }

            }
            BaseActivity.setTrackListData(songDataList)
            tracksViewModel.prepareTrackPlayback(0)
            dismiss()
        }
    }

    fun setPodcastDataList(
        playableContentModel: PlayableContentModel?,
        playableItem: MoodRadioContentList.MoodRadioPlayableContentItem
    ) {
        val track:Track = Track()


        if (!TextUtils.isEmpty(playableItem?.data?.id)){
            track.id = playableItem?.data?.id!!.toLong()
        }else{
            track.id = 0
        }
        if (!TextUtils.isEmpty(playableItem?.data?.title)){
            track.title = playableItem?.data?.title
        }else{
            track.title = ""
        }

        if (!TextUtils.isEmpty(playableItem?.data?.subtitle)){
            track.subTitle = playableItem?.data?.subtitle
        }else{
            track.subTitle = ""
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.url)){
            track.url = playableContentModel?.data?.head?.headData?.misc?.url
        }else{
            track.url = ""
        }
        if (!playableContentModel?.data?.head?.headData?.misc?.movierights.isNullOrEmpty()){
            track.movierights = playableContentModel?.data?.head?.headData?.misc?.movierights.toString()
        }else{
            track.movierights = ""
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.downloadLink?.drm?.token)){
            track.drmlicence = playableContentModel?.data?.head?.headData?.misc?.downloadLink?.drm?.token
        }else{
            track.drmlicence = ""
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.sl?.lyric?.link)) {
            track.songLyricsUrl = playableContentModel?.data?.head?.headData?.misc?.sl?.lyric?.link
        } else {
            track.songLyricsUrl = ""
        }

        if (!TextUtils.isEmpty(""+Constant.CONTENT_MOOD_RADIO)){
            track.playerType = ""+Constant.CONTENT_MOOD_RADIO
        }else{
            track.playerType = ""
        }
        /* if (!TextUtils.isEmpty(playableItem1.heading)){
             track.heading = playableItem1.heading
         }else{*/
        track.heading = SharedPrefHelper.getInstance().getMoodRadioMoodFilterTitle()+" Mood Radio"
        //}
        if (!TextUtils.isEmpty(playableItem?.data?.playble_image)){
            track.image = playableItem?.data?.playble_image
        }else if (!TextUtils.isEmpty(playableItem?.data?.image)){
            track.image = playableItem?.data?.image
        }else{
            track.image = ""
        }
        songDataList.add(track)
    }

    override fun startTrackPlayback(
        selectedTrackPosition: Int,
        tracksList: MutableList<Track>,
        trackPlayStartPosition: Long
    ) {
        val intent = Intent(getViewActivity(), AudioPlayerService::class.java)
        intent.action = AudioPlayerService.PlaybackControls.PLAY.name
        intent.putExtra(Constant.SELECTED_TRACK_POSITION, selectedTrackPosition)
        intent.putExtra(Constant.PLAY_CONTEXT_TYPE, Constant.PLAY_CONTEXT.LIBRARY_TRACKS)
        Util.startForegroundService(getViewActivity(), intent)
        (activity as MainActivity).reBindService()
    }

    override fun getViewActivity(): AppCompatActivity {
        return activity as AppCompatActivity
    }

    override fun getApplicationContext(): Context {
        return (activity as AppCompatActivity).applicationContext
    }

    private fun setTopMenus(){
        if (currentTrack?.playerType.toString() == PLAYER_AUDIO_SONG){
            llTopMenu?.show()
            llLoop?.setOnClickListener(this)
            llShuffle?.setOnClickListener(this)
            llFavourite?.setOnClickListener(this)
            if (mContext != null) {
                mContext?.setMusicPlayerThreedotMenuListener(musicPlayerThreedotMenuListener)
                mContext?.setMusicPlayerThreeDotMenuEventCallBack(setMusicPlayerThreeDotMenuListener)
            }
            setTopRepeatMenu()
            setTopSuffleMenu()
            setTopFavouriteMenu()
            setTopDownloadMenu()
            devider9?.show()
            devider16?.hide()
            rl_music_player_menu_cast?.show()
        }else{
            llTopMenu?.hide()
            devider9?.hide()
            rl_music_player_menu_cast?.hide()
        }
    }
    override fun onClick(v: View?) {
        if (v == llLoop){
            if (mContext != null){
                mContext?.changeRepeateModes(mContext?.getPlayerRepeatMode()!!)
            }
        }else if (v == llShuffle){
            if (mContext != null){
                mContext?.changeShuffleMode()
            }
        }else if (v == llFavourite){
            if (mContext != null){
                mContext?.setSongLike()
            }
        }else if (v == llDownload){
            if (mContext != null){
                mContext?.downloadAudioTrack()
            }

        }
    }

    private val musicPlayerThreedotMenuListener = object : BaseActivity.OnMusicPlayerThreedotMenuListener{
        override fun repeatModeChanged(repeatMode: Int) {
            changeRepeatModeIcon(repeatMode)
        }

        override fun shuffleModeChanged(shuffleMode: Boolean) {
            changeShuffleModeIcon(shuffleMode)
        }
    }

    private fun setTopRepeatMenu(){
        if (mContext != null){
            val repeatMode = mContext?.getPlayerRepeatMode()
            repeatMode?.let { changeRepeatModeIcon(it) }
        }
    }

    private fun changeRepeatModeIcon(repeatMode: Int){
        if (mContext != null){
            var repeatIcon = R.string.icon_repeat_loop
            var repeatColor = R.color.colorWhite
            if (repeatMode == Player.REPEAT_MODE_OFF){
                repeatIcon = R.string.icon_repeat_loop
                repeatColor = R.color.colorWhite
                ivLoopMenu?.alpha = 0.4F
                tvRepeat?.text = getString(R.string.repeat)
                tvRepeat?.alpha = 0.4F
            }else if (repeatMode == Player.REPEAT_MODE_ALL){
                repeatIcon = R.string.icon_repeat_active
                repeatColor = R.color.colorWhite
                ivLoopMenu?.alpha = 1F
                tvRepeat?.text = getString(R.string.repeat_all)
                tvRepeat?.alpha = 1F
            }else if (repeatMode == Player.REPEAT_MODE_ONE){
                repeatIcon = R.string.icon_repeat_one
                repeatColor = R.color.colorWhite
                ivLoopMenu?.alpha = 1F
                tvRepeat?.text = getString(R.string.repeat_one)
                tvRepeat?.alpha = 1F
            }

            ivLoopMenu?.setImageDrawable(
                context?.resources?.getDimensionPixelSize(R.dimen.font_18)?.let {
                    requireContext().faDrawable(
                        repeatIcon,
                        repeatColor,
                        it.toFloat()
                    )
                }
            )
        }
    }

    private fun setTopSuffleMenu(){
        if (mContext != null){
            val shuffleMode = mContext?.getShuffleModeStatus()
            shuffleMode?.let { changeShuffleModeIcon(it) }
        }
    }

    private fun changeShuffleModeIcon(shuffleMode: Boolean){
        if (mContext != null){
            var shuffleIcon = R.string.icon_shuffle
            var shuffleColor = R.color.colorWhite
            if (shuffleMode){
                shuffleIcon = R.string.icon_shuffle_active
                shuffleColor = R.color.colorWhite
                ivShuffleMenu?.alpha = 1F
                tvShuffle?.alpha = 1F
            }else{
                shuffleIcon = R.string.icon_shuffle
                shuffleColor = R.color.colorWhite
                ivShuffleMenu?.alpha = 0.4F
                tvShuffle?.alpha = 0.4F
            }

            ivShuffleMenu?.setImageDrawable(
                context?.resources?.getDimensionPixelSize(R.dimen.font_18)?.let {
                    requireContext().faDrawable(
                        shuffleIcon,
                        shuffleColor,
                        it.toFloat()
                    )
                }
            )
        }
    }

    private fun setTopDownloadMenu(){
        val isDownloaded = CommonUtils.isContentDownloaded(
            BaseActivity.songDataList,
            BaseActivity.nowPlayingCurrentIndex()
        )
        if (isDownloaded) {
            if (isAdded && context != null && ivDownloadMenu != null) {
                context?.resources?.getDimensionPixelSize(R.dimen.font_18)?.let {
                    CommonUtils.downloadIconStates(
                        requireContext(),
                        com.hungama.fetch2.Status.COMPLETED.value,
                        ivDownloadMenu,
                        it.toFloat()
                    )
                }
            }
        } else {
            val downloadQueue =
                AppDatabase.getInstance()?.downloadQueue()?.findByContentId(
                    currentTrack?.id.toString()
                )
            if (downloadQueue != null && (!downloadQueue.contentId.equals(
                    currentTrack?.id.toString()
                ))
            ) {
                if (isAdded && context != null && ivDownloadMenu != null) {
                    context?.resources?.getDimensionPixelSize(R.dimen.font_18)?.let {
                        CommonUtils.downloadIconStates(
                            requireContext(),
                            downloadQueue.downloadStatus,
                            ivDownloadMenu,
                            it.toFloat()
                        )
                    }
                }
            } else {
                if (isAdded && context != null && ivDownloadMenu != null) {
                    context?.resources?.getDimensionPixelSize(R.dimen.font_18)?.let {
                        CommonUtils.downloadIconStates(
                            requireContext(),
                            com.hungama.fetch2.Status.NONE.value,
                            ivDownloadMenu,
                            it.toFloat()
                        )
                    }
                }
            }
            if (mContext?.getPlayerType(currentTrack?.playerType) == Constant.CONTENT_RADIO) {
                llDownload?.setOnClickListener(null)
            } else {
                llDownload?.setOnClickListener(this)
            }

        }
    }

    val setMusicPlayerThreeDotMenuListener = object : BaseActivity.OnSwipablePlayerListener{
        override fun onDownloadContentStateChange(status: Int) {
            super.onDownloadContentStateChange(status)
            CoroutineScope(Dispatchers.Main).launch {
                if (isAdded && context != null && ivDownloadMenu != null){
                    context?.resources?.getDimensionPixelSize(R.dimen.font_18)?.let { CommonUtils.downloadIconStates(requireContext(), status, ivDownloadMenu, it.toFloat()) }
                }
            }
        }

        override fun onFavoritedContentStateChange(isFavorite: Boolean) {
            super.onFavoritedContentStateChange(isFavorite)
            updateFavouriteStatus(isFavorite)
        }
    }

    private fun setTopFavouriteMenu(){
        if (!BaseActivity.songDataList.isNullOrEmpty() && BaseActivity.songDataList?.size!! > BaseActivity.nowPlayingCurrentIndex() && BaseActivity.songDataList?.get(
                BaseActivity.nowPlayingCurrentIndex()
            )?.pType != DetailPages.LOCAL_DEVICE_SONG_PAGE.value
        ) {
            currentTrack?.isLiked?.let { updateFavouriteStatus(it) }
        }
    }

    private fun updateFavouriteStatus(isFavorite: Boolean){
        CoroutineScope(Dispatchers.Main).launch {
            if (isAdded && context != null && ivFavoriteMenu != null) {
                var icon = R.string.icon_like
                if (isFavorite) {
                    icon = R.string.icon_liked
                }
                ivFavoriteMenu?.setImageDrawable(
                    context?.resources?.getDimensionPixelSize(R.dimen.font_18)?.let {
                        requireContext().faDrawable(
                            icon,
                            R.color.colorWhite,
                            it.toFloat()
                        )
                    }
                )
            }
        }
    }


}
