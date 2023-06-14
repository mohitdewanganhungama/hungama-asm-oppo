package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.Util
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import com.hungama.music.data.model.*
import com.hungama.music.player.audioplayer.TracksContract
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.services.AudioPlayerService
import com.hungama.music.player.audioplayer.viewmodel.TracksViewModel
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.utils.customview.bottomsheet.SuperBottomSheetFragment
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.player.audioplayer.Injection
import com.hungama.music.utils.ConnectionUtil
import com.hungama.music.utils.Constant
import com.hungama.music.utils.Constant.RADIO_ERA_LIST
import com.hungama.music.utils.Constant.RADIO_LANGUAGE_LIST
import com.hungama.music.utils.Constant.RADIO_MOOD_LIST
import com.hungama.music.utils.Constant.RADIO_TEMPO_LIST
import com.hungama.music.utils.Utils
import com.hungama.music.ui.main.viewmodel.MoodRadioViewModel
import com.hungama.music.ui.main.viewmodel.PlayableContentViewModel
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import kotlinx.android.synthetic.main.mood_radio_filter_main.*
import kotlinx.android.synthetic.main.mood_radio_filter_main.progress

class MoodRadioFilterBottomSheetFragment() : SuperBottomSheetFragment(),
    MoodRadioFilterSelectMood.OnItemClick, TracksContract.View {

    var moodRadioViewModel = MoodRadioViewModel()
    var moodRadioPopupMoodModel = MoodRadioFilterModel()
    var moodRadioPopupTempoModel = MoodRadioFilterModel()
    var moodRadioPopupLangModel = MoodRadioFilterModel()
    var moodRadioContentListModel = MoodRadioContentList()
    var playableContentViewModel: PlayableContentViewModel = PlayableContentViewModel()
    var songDataList:ArrayList<Track> = arrayListOf()
    private lateinit var tracksViewModel: TracksContract.Presenter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        //hideSystemNavigationBar()
        return inflater.inflate(R.layout.mood_radio_filter_main, container, false)
    }

    override fun getCornerRadius() =
        requireContext().resources.getDimension(R.dimen.common_popup_round_corner)

    override fun getStatusBarColor() = Color.RED

    override fun isSheetAlwaysExpanded(): Boolean = true
    override fun getExpandedHeight(): Int =
        requireContext().resources.getDimension(R.dimen.dimen_360).toInt()

    /*override fun getPeekHeight(): Int = requireContext().resources.getDimension(R.dimen.dimen_540).toInt()*/

    override fun getBackgroundColor(): Int =
        requireContext().resources.getColor(R.color.transparent)

    override fun isSheetCancelableOnTouchOutside(): Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpMoodRadioPopupMoodViewModel()
        setUpMoodRadioPopupTempoViewModel()
        setUpMoodRadioPopupLanguageViewModel()
        tvEraTitle.text = SharedPrefHelper.getInstance().getMoodRadioEraFilterMinRange().toString() + " - " + SharedPrefHelper.getInstance().getMoodRadioEraFilterMaxRange().toString()
        tvMoodTitle?.text = SharedPrefHelper.getInstance().getMoodRadioMoodFilterTitle()
        tvTempoTitle.text = SharedPrefHelper.getInstance().getMoodRadioTempoFilterTitle()
        tvLanguageTitle.text = SharedPrefHelper.getInstance().getMoodRadioLanguageFilterTitle()
        three_dot_menu_close?.setOnClickListener {
            close()
        }

        llEra?.setOnClickListener {
            val sheet = MoodRadioFilterEra(this)
            sheet.show(activity?.supportFragmentManager!!, "MoodRadioFilterSelectTempo")
        }

        tracksViewModel = TracksViewModel(Injection.provideTrackRepository(), this)
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
            llMood?.setOnClickListener {
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
            llTempo?.setOnClickListener {
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
            llLang?.setOnClickListener {
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
        if (type == RADIO_MOOD_LIST){
            //setLog("Type-1", type.toString())
            //setLog("Type-1", moodRadioPopupMoodModel.data?.body?.rows?.get(0)?.items?.get(position)?.title.toString())
            tvMoodTitle?.text = moodRadioPopupMoodModel.data?.body?.rows?.get(0)?.items?.get(position)?.title.toString()
            SharedPrefHelper.getInstance().setMoodRadioMoodFilterId(moodRadioPopupMoodModel.data?.body?.rows?.get(0)?.items?.get(position)?.moodid!!)
            SharedPrefHelper.getInstance().setMoodRadioMoodFilterTitle(moodRadioPopupMoodModel.data?.body?.rows?.get(0)?.items?.get(position)?.title.toString())
        }else if (type == RADIO_TEMPO_LIST){
            //setLog("Type-2", type.toString())
            //setLog("Type-2", moodRadioPopupTempoModel.data?.body?.rows?.get(0)?.items?.get(position)?.title.toString())
            tvTempoTitle.text = moodRadioPopupTempoModel.data?.body?.rows?.get(0)?.items?.get(position)?.title.toString()
            SharedPrefHelper.getInstance().setMoodRadioTempoFilterId(moodRadioPopupTempoModel.data?.body?.rows?.get(0)?.items?.get(position)?.tempoid!!)
            SharedPrefHelper.getInstance().setMoodRadioTempoFilterTitle(moodRadioPopupTempoModel.data?.body?.rows?.get(0)?.items?.get(position)?.title.toString())
        }else if (type == RADIO_LANGUAGE_LIST){
            //setLog("Type-3", type.toString())
            //setLog("Type-3", moodRadioPopupLangModel.data?.body?.rows?.get(0)?.items?.get(position)?.title.toString())
            tvLanguageTitle.text = moodRadioPopupLangModel.data?.body?.rows?.get(0)?.items?.get(position)?.title.toString()
            SharedPrefHelper.getInstance().setMoodRadioLanguageFilterId(moodRadioPopupLangModel.data?.body?.rows?.get(0)?.items?.get(position)?.code!!.toString())
            SharedPrefHelper.getInstance().setMoodRadioLanguageFilterTitle(moodRadioPopupLangModel.data?.body?.rows?.get(0)?.items?.get(position)?.title.toString())
        }else if (type == RADIO_ERA_LIST){
            tvEraTitle.text = SharedPrefHelper.getInstance().getMoodRadioEraFilterMinRange().toString() + " - " + SharedPrefHelper.getInstance().getMoodRadioEraFilterMaxRange().toString()
        }
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
        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.image)){
            track.image = playableContentModel?.data?.head?.headData?.image
        }else if (!TextUtils.isEmpty(playableItem?.data?.playble_image)){
            track.image = playableItem?.data?.playble_image
        }else if (!TextUtils.isEmpty(playableItem?.data?.image)){
            track.image = playableItem?.data?.image
        }else{
            track.image = ""
        }
        songDataList.add(track)
    }

    override fun onDestroy() {
        super.onDestroy()
        tracksViewModel.onCleanup()
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
}

