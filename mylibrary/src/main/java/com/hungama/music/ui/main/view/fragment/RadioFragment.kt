package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.media3.common.util.Util
import com.hungama.music.HungamaMusicApp
import com.hungama.music.R
import com.hungama.music.data.model.*
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.home.eventreporter.UserAttributeEvent
import com.hungama.music.player.audioplayer.Injection
import com.hungama.music.player.audioplayer.TracksContract
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.services.AudioPlayerService
import com.hungama.music.player.audioplayer.viewmodel.TracksViewModel
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.RadioAdapter
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.viewmodel.PlayableContentViewModel
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.ConnectionUtil
import com.hungama.music.utils.Constant
import com.hungama.music.utils.Utils
import kotlinx.android.synthetic.main.fr_library_albums.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


class RadioFragment : BaseFragment(), TracksContract.View,
    BaseActivity.OnLocalBroadcastEventCallBack {

    var playableItem = BookmarkDataModel.Data.Body.Row.Data()
    var playableItemPosition = 0
    var CONTENT_TYPE = 0
    var RADIO_TYPE = 2225

    private var isLastDurationPlay = false

    var playableContentViewModel: PlayableContentViewModel? = null
    var userViewModel: UserViewModel? = null
    var userAlbumsList: ArrayList<BookmarkDataModel.Data.Body.Row> = ArrayList()
    private lateinit var tracksViewModel: TracksContract.Presenter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fr_library_radio, container, false)
    }

    override fun onResume() {
        super.onResume()
        setLocalBroadcast()
        setUpViewModel()
    }

    override fun initializeComponent(view: View) {
        CommonUtils.applyButtonTheme(requireContext(), btnExplore)
        btnExplore?.setOnClickListener(this)

        tracksViewModel = TracksViewModel(Injection.provideTrackRepository(), this)

    }

    override fun onClick(v: View) {
        super.onClick(v)
        if (v == btnExplore) {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), btnExplore!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            val bundle = Bundle()
            bundle.putBoolean(Constant.isTabSelection, true)
            bundle.putString(Constant.tabName, "radio")
            (activity as MainActivity).applyScreen(1,bundle)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return true
    }

    /**
     * get Playable url for song : 33, 35
     *
     * @param id
     */
    private fun getPlayableMoodRadioList(id: String, type: Int) {
        if (type == Constant.CONTENT_MOOD_RADIO) {
            playableContentViewModel?.getMoodRadioList(requireContext(), id)?.observe(this,
                Observer {
                    when (it.status) {
                        com.hungama.music.data.webservice.utils.Status.SUCCESS -> {
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                moodRadioListRespModel = it?.data
                                if (it?.data?.size!! > 0) {
                                    getPlayableContentUrl(it?.data?.get(0)?.data?.id!!)
                                }

                            }
                        }

                        com.hungama.music.data.webservice.utils.Status.LOADING -> {
                            setProgressBarVisible(true)
                        }

                        com.hungama.music.data.webservice.utils.Status.ERROR -> {
                            setProgressBarVisible(false)
                            Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                        }
                    }
                })
        } else if (type == Constant.CONTENT_ARTIST_RADIO) {
            playableContentViewModel?.getArtistRadioList(requireContext(), id)?.observe(this,
                Observer {
                    when (it.status) {
                        com.hungama.music.data.webservice.utils.Status.SUCCESS -> {
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                moodRadioListRespModel = it?.data
                                if (it?.data?.size!! > 0) {
                                    getPlayableContentUrl(it?.data?.get(0)?.data?.id!!)
                                }

                            }
                        }

                        com.hungama.music.data.webservice.utils.Status.LOADING -> {
                            setProgressBarVisible(true)
                        }

                        com.hungama.music.data.webservice.utils.Status.ERROR -> {
                            setProgressBarVisible(false)
                            Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                        }
                    }
                })
        } else {
            playableContentViewModel?.getOnDemandRadioList(requireContext(), id)?.observe(this,
                Observer {
                    when (it.status) {
                        com.hungama.music.data.webservice.utils.Status.SUCCESS -> {
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                moodRadioListRespModel = it?.data
                                if (it?.data?.size!! > 0) {
                                    getPlayableContentUrl(it?.data?.get(0)?.data?.id!!)
                                }

                            }
                        }

                        com.hungama.music.data.webservice.utils.Status.LOADING -> {
                            setProgressBarVisible(true)
                        }

                        com.hungama.music.data.webservice.utils.Status.ERROR -> {
                            setProgressBarVisible(false)
                            Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                        }
                    }
                })
        }
    }

    /**
     * get Playable url for song : 21
     *
     * @param id
     */
    fun getPlayableContentUrl(id: String) {
        if (ConnectionUtil(context).isOnline) {
            playableContentViewModel?.getPlayableContentList(requireContext(), id)?.observe(this,
                Observer {
                    when (it.status) {
                        com.hungama.music.data.webservice.utils.Status.SUCCESS -> {
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                //setLog(TAG, "isViewLoading $it")

                                if (!TextUtils.isEmpty(it?.data?.data?.head?.headData?.misc?.url)) {
                                    setPlayableContentListData(it?.data)
                                }
                            }
                        }

                        com.hungama.music.data.webservice.utils.Status.LOADING -> {
                            setProgressBarVisible(true)
                        }

                        com.hungama.music.data.webservice.utils.Status.ERROR -> {
                            setEmptyVisible(false)
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

    private fun getUserAlbumsData() {
        baseMainScope.launch {
            if (ConnectionUtil(requireContext()).isOnline) {
                userViewModel?.getUserBookmarkedDataWithFilter(
                    requireContext(),
                    Constant.MODULE_FAVORITE,
                    "77777,34"
                )?.observe(this@RadioFragment,
                    Observer {
                        when (it.status) {
                            Status.SUCCESS -> {
                                setProgressBarVisible(false)
                                if (it?.data != null) {
                                    setUpLists(it?.data)
                                }

                            }

                            Status.LOADING -> {
                                setProgressBarVisible(true)
                            }

                            Status.ERROR -> {
                                setEmptyVisible(false)
                                setProgressBarVisible(false)
                                Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                            }
                        }
                    })
            } else {
                setEmptyVisible(false)
                setProgressBarVisible(false)
                val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                    MessageType.NEGATIVE, true)
                CommonUtils.showToast(requireContext(), messageModel)
            }
        }

    }

    private fun setUpViewModel() {
        userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)

        playableContentViewModel = ViewModelProvider(
            this
        ).get(PlayableContentViewModel::class.java)

        getUserAlbumsData()
    }


    private fun callFavRadioEvent(arrayList: java.util.ArrayList<BookmarkDataModel.Data.Body.Row>) {
        val list = ArrayList<String>()
        arrayList.forEach {
            list.add(it?.data?.title!!)
        }

        val userDataMap = java.util.HashMap<String, String>()
        userDataMap.put(
            EventConstant.FAVOURITED_ON_DEMAND_RADIO,
             Utils.arrayToString(list)
        )
        EventManager.getInstance().sendUserAttribute(UserAttributeEvent(userDataMap))
    }

    private fun setUpLists(userPlaylistData: BookmarkDataModel) {
        if (userPlaylistData?.data != null && userPlaylistData?.data?.body?.rows?.size!! > 0) {
            userAlbumsList = userPlaylistData?.data?.body?.rows!!

            callFavRadioEvent(userAlbumsList)

            rvMusicPlaylist.apply {
                layoutManager =
                    GridLayoutManager(context, 1)
                adapter = RadioAdapter(
                    context, userAlbumsList,
                    object : RadioAdapter.OnChildItemClick {
                        override fun onUserClick(childPosition: Int) {
                            var parent = userAlbumsList?.get(childPosition)?.data

                            if (userAlbumsList?.get(childPosition)?.data?.type!!.equals(
                                    "15",
                                    true
                                ) || userAlbumsList?.get(childPosition)?.data?.type!!.equals(
                                    "21",
                                    true
                                ) || userAlbumsList?.get(childPosition)?.data?.type!!.equals(
                                    "34",
                                    true
                                )
                            ) {
                                playableItem = parent!!
                                playableItemPosition = childPosition
                                getPlayableContentUrl(parent?.id!!)
                            } else if (userAlbumsList?.get(childPosition)?.data?.type!!.equals(
                                    "33",
                                    true
                                )
                            ) {
//                                playableItem = parent
//                                playableItemPosition = childPosition
//                                CONTENT_TYPE = Constant.CONTENT_MOOD_RADIO
//                                getPlayableMoodRadioList(parent.items?.get(childPosition)?.data?.moodid!!,
//                                    Constant.CONTENT_MOOD_RADIO
//                                )

                            } else if (userAlbumsList?.get(childPosition)?.data?.type!!.equals(
                                    "35",
                                    true
                                )
                            ) {
                                playableItem = parent!!
                                playableItemPosition = childPosition
                                CONTENT_TYPE = Constant.CONTENT_ON_DEMAND_RADIO
                                getPlayableMoodRadioList(
                                    parent?.id!!,
                                    Constant.CONTENT_ON_DEMAND_RADIO
                                )
                            } else if (userAlbumsList?.get(childPosition)?.data?.type!!.equals(
                                    "36",
                                    true
                                )
                            ) {
                                playableItem = parent!!
                                playableItemPosition = childPosition
                                CONTENT_TYPE = Constant.CONTENT_ARTIST_RADIO
                                getPlayableMoodRadioList(
                                    parent?.id!!,
                                    Constant.CONTENT_ARTIST_RADIO
                                )
                            }

                        }
                    }
                )
                setRecycledViewPool(RecyclerView.RecycledViewPool())
                setHasFixedSize(true)
            }
            rvMusicPlaylist.visibility = View.VISIBLE
            clExplore.visibility = View.GONE
        } else {
            rvMusicPlaylist.visibility = View.GONE
            clExplore.visibility = View.VISIBLE
        }

    }

    var moodRadioListRespModel: MoodRadioListRespModel? = null


    fun setPlayableContentListData(playableContentModel: PlayableContentModel) {

        if (playableContentModel != null) {
            setLog("PlayableItem", playableContentModel?.data?.head?.headData?.id.toString())
            songDataList = arrayListOf()
            isLastDurationPlay = false

            if (playableItem?.type!!.equals(
                    Constant.PLAYER_MOOD_RADIO,
                    true
                ) || playableItem?.type!!.equals(
                    Constant.PLAYER_ON_DEMAND_RADIO,
                    true
                ) || playableItem?.type!!.equals(Constant.PLAYER_ARTIST_RADIO, true)
            ) {
                for (i in moodRadioListRespModel?.indices!!) {
                    if (playableContentModel?.data?.head?.headData?.id == moodRadioListRespModel?.get(
                            i
                        )?.data?.id
                    ) {
                        setRadioDataList(
                            playableContentModel,
                            moodRadioListRespModel?.get(i),
                            playableItemPosition,
                            playableItem
                        )
                    }
                    if (i > playableItemPosition) {
                        setRadioDataList(
                            null,
                            moodRadioListRespModel?.get(i),
                            playableItemPosition,
                            playableItem
                        )
                    }

                }
                BaseActivity.setTrackListData(songDataList)

                if (HungamaMusicApp?.getInstance()
                        ?.getContinueWhereLeftData() != null && HungamaMusicApp?.getInstance()
                        ?.getContinueWhereLeftData()?.size!! > 0
                ) {

                    HungamaMusicApp?.getInstance()?.getContinueWhereLeftData()?.forEach {
                        if (it?.data?.id == playableContentModel?.data?.head?.headData?.id) {
                            if (it?.data?.durationPlay != null && it?.data?.durationPlay?.toLong()!! > 0) {
                                //tracksViewModel.prepareTrackPlayback(playableItemPosition, TimeUnit.SECONDS.toMillis(it?.data?.durationPlay!!))
                                tracksViewModel.prepareTrackPlayback(
                                    0,
                                    TimeUnit.SECONDS.toMillis(it?.data?.durationPlay?.toLong()!!)
                                )

                            } else {
                                //tracksViewModel.prepareTrackPlayback(playableItemPosition)
                                tracksViewModel.prepareTrackPlayback(0)

                            }
                            isLastDurationPlay = true
                            return@forEach
                        }
                    }

                    if (!isLastDurationPlay) {
                        tracksViewModel.prepareTrackPlayback(0)

                    }
                } else {
                    tracksViewModel.prepareTrackPlayback(0)

                }
            } else {
                for (i in userAlbumsList!!.indices) {

                    /*if (playableContentModel.id == playableItem.items?.get(i)?.data?.id){
                        setPlayableDataList(playableContentModel, playableItem, playableItemPosition)
                    }else{
                        setPlayableDataList(null, playableItem, i)
                    }*/
                    if (playableContentModel?.data?.head?.headData?.id == userAlbumsList?.get(i)?.data?.id) {
                        setPlayableDataList(
                            playableContentModel,
                            playableItem,
                            playableItemPosition
                        )
                    } else if (i > playableItemPosition) {
                        setPlayableDataList(null, playableItem, i)
                    }

                }
                BaseActivity.setTrackListData(songDataList)
                if (HungamaMusicApp?.getInstance()
                        ?.getContinueWhereLeftData() != null && HungamaMusicApp?.getInstance()
                        ?.getContinueWhereLeftData()?.size!! > 0
                ) {

                    HungamaMusicApp?.getInstance()?.getContinueWhereLeftData()?.forEach {
                        if (it?.data?.id == playableContentModel?.data?.head?.headData?.id) {
                            if (it?.data?.durationPlay != null && it?.data?.durationPlay?.toLong()!! > 0) {
                                //tracksViewModel.prepareTrackPlayback(playableItemPosition, TimeUnit.SECONDS.toMillis(it?.data?.durationPlay!!))
                                tracksViewModel.prepareTrackPlayback(
                                    0,
                                    TimeUnit.SECONDS.toMillis(it?.data?.durationPlay?.toLong()!!)
                                )

                            } else {
                                //tracksViewModel.prepareTrackPlayback(playableItemPosition)
                                tracksViewModel.prepareTrackPlayback(0)

                            }

                            isLastDurationPlay = true
                            return@forEach
                        }
                    }

                    if (!isLastDurationPlay) {
                        //tracksViewModel.prepareTrackPlayback(playableItemPosition)
                        tracksViewModel.prepareTrackPlayback(0)

                    }
                } else {
                    //tracksViewModel.prepareTrackPlayback(playableItemPosition)
                    tracksViewModel.prepareTrackPlayback(0)

                }


            }


        }
    }

    var songDataList: ArrayList<Track> = arrayListOf()
    fun setPlayableDataList(
        playableContentModel: PlayableContentModel?,
        playableItem: BookmarkDataModel.Data.Body.Row.Data,
        position: Int
    ) {
        val track: Track = Track()

        if (!TextUtils.isEmpty(playableItem?.id)) {
            track.id = playableItem?.id!!.toLong()
        } else {
            track.id = 0
        }
        if (!TextUtils.isEmpty(playableItem?.title)) {
            track.title = playableItem?.title
        } else {
            track.title = ""
        }

        if (!TextUtils.isEmpty(playableItem?.subtitle)) {
            track.subTitle = playableItem?.subtitle
        } else {
            track.subTitle = ""
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.url)) {
            track.url = playableContentModel?.data?.head?.headData?.misc?.url
        } else {
            track.url = ""
        }
        if (!playableContentModel?.data?.head?.headData?.misc?.movierights.isNullOrEmpty()){
            track.movierights = playableContentModel?.data?.head?.headData?.misc?.movierights.toString()
        }else{
            track.movierights = ""
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.downloadLink?.drm?.token)) {
            track.drmlicence =
                playableContentModel?.data?.head?.headData?.misc?.downloadLink?.drm?.token
        } else {
            track.drmlicence = ""
        }

        setLog(TAG, "setPlayableDataList playerType:${playableItem?.type}")
        if (!TextUtils.isEmpty(playableItem?.type)) {
            track.playerType = Constant.PLAYER_LIVE_RADIO
        } else {
            track.playerType = ""
        }
        if (!TextUtils.isEmpty(playableItem.title)) {
            track.heading = playableItem.title
        } else {
            track.heading = ""
        }
        if (!TextUtils.isEmpty(playableItem?.playble_image)) {
            track.image = playableItem?.playble_image
        }else if (!TextUtils.isEmpty(playableItem?.image)) {
            track.image = playableItem?.image
        } else {
            track.image = ""
        }

        track.explicit = playableItem.misc.explicit
        track.restrictedDownload = playableItem.misc.restricted_download
        if (playableItem.misc.attributeCensorRating != null) {
            track.attributeCensorRating =
                playableItem.misc.attributeCensorRating.toString()
        }

        if (playableContentModel != null){
            track.urlKey = playableContentModel.data.head.headData.misc.urlKey
        }
        songDataList.add(track)
    }

    fun setRadioDataList(
        playableContentModel: PlayableContentModel?,
        playableItem: MoodRadioListRespModel.MoodRadioListRespModelItem?,
        position: Int,
        playableItem1: BookmarkDataModel.Data.Body.Row.Data
    ) {
        val track: Track = Track()


        if (!TextUtils.isEmpty(playableItem?.data?.id)) {
            track.id = playableItem?.data?.id!!.toLong()
        } else {
            track.id = 0
        }
        if (!TextUtils.isEmpty(playableItem?.data?.title)) {
            track.title = playableItem?.data?.title
        } else {
            track.title = ""
        }

        if (!TextUtils.isEmpty(playableItem?.data?.subtitle)) {
            track.subTitle = playableItem?.data?.subtitle
        } else {
            track.subTitle = ""
        }

        if (!playableContentModel?.data?.head?.headData?.misc?.movierights.isNullOrEmpty()){
            track.movierights = playableContentModel?.data?.head?.headData?.misc?.movierights.toString()
        }else{
            track.movierights = ""
        }

        if (playableItem?.data?.misc != null && playableItem?.data?.misc?.artist != null) {
            track.artistName = TextUtils.join(",", playableItem?.data?.misc?.artist!!)
        } else {
            track.artistName = ""
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.url)) {
            track.url = playableContentModel?.data?.head?.headData?.misc?.url
        } else {
            track.url = ""
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.downloadLink?.drm?.token)) {
            track.drmlicence =
                playableContentModel?.data?.head?.headData?.misc?.downloadLink?.drm?.token
        } else {
            track.drmlicence = ""
        }

        if (!TextUtils.isEmpty(playableItem1?.type)) {
            track.playerType = playableItem1?.type
        } else {
            track.playerType = Constant.PLAYER_RADIO
        }
        if (!TextUtils.isEmpty(playableItem1.title)) {
            track.heading = playableItem1.title
        } else {
            track.heading = ""
        }
        if (!TextUtils.isEmpty(playableItem?.data?.playble_image)) {
            track.image = playableItem?.data?.playble_image
        }else if (!TextUtils.isEmpty(playableItem?.data?.image)) {
            track.image = playableItem?.data?.image
        } else {
            track.image = ""
        }
        track.explicit = playableItem1.misc.explicit
        track.restrictedDownload = playableItem1.misc.restricted_download
        if (playableItem1.misc.attributeCensorRating != null) {
            track.attributeCensorRating =
                playableItem1.misc.attributeCensorRating.toString()
        }

        if (playableContentModel != null){
            track.urlKey = playableContentModel.data.head.headData.misc.urlKey
        }
        songDataList.add(track)
    }

    override fun startTrackPlayback(
        selectedTrackPosition: Int,
        tracksList: MutableList<Track>,
        trackPlayStartPosition: Long
    ) {
        if(isAdded()&&activity!=null){
            val intent = Intent(getViewActivity(), AudioPlayerService::class.java)
            intent.action = AudioPlayerService.PlaybackControls.PLAY.name
            intent.putExtra(Constant.SELECTED_TRACK_POSITION, selectedTrackPosition)
            if (trackPlayStartPosition > 0) {
                intent.putExtra(Constant.SELECTED_TRACK_PLAY_START_POSITION, trackPlayStartPosition)
            }
            intent.putExtra(Constant.PLAY_CONTEXT_TYPE, Constant.PLAY_CONTEXT.LIBRARY_TRACKS)
            Util.startForegroundService(getViewActivity(), intent)
            (activity as MainActivity).reBindService()
        }


    }

    override fun getViewActivity(): AppCompatActivity {
        return activity as AppCompatActivity
    }

    override fun getApplicationContext(): Context {
        return (activity as AppCompatActivity).applicationContext
    }

    private fun setLocalBroadcast(){
        (requireActivity() as MainActivity).setLocalBroadcastEventCall(this, Constant.AUDIO_PLAYER_EVENT)
    }

    override fun onLocalBroadcastEventCallBack(context: Context?, intent: Intent) {
        if (isAdded){
            val event = intent.getIntExtra("EVENT", 0)
            if (event == Constant.AUDIO_PLAYER_RESULT_CODE) {
                CommonUtils.setPageBottomSpacing(rvMusicPlaylist, requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_180),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
            }
        }
    }
}