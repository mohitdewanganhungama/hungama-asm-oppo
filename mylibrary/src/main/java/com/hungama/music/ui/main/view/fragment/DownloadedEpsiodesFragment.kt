package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import com.hungama.fetch2.Status
import com.hungama.music.R
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.utils.customview.downloadmanager.model.DownloadQueue
import com.hungama.music.utils.customview.downloadmanager.model.DownloadedAudio
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.data.model.*
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.adapter.DownloadedEpsiodesAdapter
import com.hungama.music.player.videoplayer.VideoPlayerActivity
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.preference.SharedPrefHelper
import kotlinx.android.synthetic.main.common_header_action_bar.*
import kotlinx.android.synthetic.main.fragment_downloaded_epsiodes.*
import kotlinx.coroutines.*


class DownloadedEpsiodesFragment : BaseFragment(), BaseActivity.OnDownloadVideoQueueItemChanged,
    OnUserSubscriptionUpdate, BaseFragment.OnMenuItemClicked,
    MoviePauseDialog.openDeletePopupListener, DeleteDownloadedContentDialog.addPauseListener,
    BaseActivity.OnLocalBroadcastEventCallBack, CommonUtils.OnFileDeleted {
    private var epsiodeAdapter: DownloadedEpsiodesAdapter? = null
    var isEdit = false
    var deletableDownloadList = ArrayList<DownloadedAudio>()
    var tvShowEpisodeList = ArrayList<DownloadedAudio>()
    var playableItemPosition = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_downloaded_epsiodes, container, false)
    }


    override fun initializeComponent(view: View) {
        tvActionBarHeading.text = getString(R.string.download_str_26)
        ivBack?.setOnClickListener {
                view -> backPress()
        }
        rlMenu?.visibility = View.VISIBLE
        rlMenu?.setOnClickListener(this)
        setBottomView(0, status = false, isDeleted = false)

        rvTVShows.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        epsiodeAdapter = DownloadedEpsiodesAdapter(requireContext(),object : DownloadedEpsiodesAdapter.OnItemClick{
            override fun showMovieDeleteDialog(adapterPosition: Int) {
                if (!tvShowEpisodeList.isNullOrEmpty() && tvShowEpisodeList.size > adapterPosition){
                    deletableDownloadList = ArrayList<DownloadedAudio>()
                    deletableDownloadList.add(tvShowEpisodeList.get(adapterPosition))
                    var moviePauseDialog = MoviePauseDialog(tvShowEpisodeList.get(adapterPosition), this@DownloadedEpsiodesFragment,false)
                    moviePauseDialog.show(
                        activity?.supportFragmentManager!!,
                        "TV Show pause dialog show"
                    )
                }
            }
            override fun onEpisodeClick(childPosition: Int) {
                if (!tvShowEpisodeList.isNullOrEmpty() && tvShowEpisodeList.size > childPosition) {
                    val dpm = DownloadPlayCheckModel()
                    dpm.contentId = tvShowEpisodeList.get(childPosition).contentId?.toString()!!
                    dpm.contentTitle = tvShowEpisodeList.get(childPosition).title?.toString()!!

                    if (tvShowEpisodeList.get(childPosition).movierights.toString()
                            .contains(PlanNames.FVOD.name, true)
                    ) {
                        dpm.planName = "[SVOD]"
                    } else {
                        dpm.planName = tvShowEpisodeList.get(childPosition).movierights.toString()
                    }
                    dpm.isAudio = false
                    dpm.isDownloadAction = false
                    dpm.isDirectPaymentAction = false
                    dpm.queryParam = ""
                    dpm.isShowSubscriptionPopup = true
                    dpm.clickAction = ClickAction.FOR_SINGLE_CONTENT
                    dpm.restrictedDownload = RestrictedDownload.NONE_DOWNLOAD_CONTENT
                    Constant.screen_name ="Downloaded Episodes Screen"
                    if (CommonUtils.userCanDownloadContent(
                            requireContext(),
                            downloadedEpisodesRoot,
                            dpm,
                            this@DownloadedEpsiodesFragment,Constant.drawer_svod_purchase
                        )
                    ) {
                        val songsList =
                            CommonUtils.getVideoDummyData2("https://hunstream.hungama.com/c/5/481/3d4/48090348/48090348_,100,400,750,1000,1600,.mp4.m3u8?rtLFaR4wQhnQIwZj-gbvlKvXi6fnpm8zqQD_AVZHY1bwN0aPUIi99NRWCgtfsYx_4rANuyEvwF6-l4O1vfy8khCL2v6l-9IL1Knc0y-Oc_WoL5hQeTmyi3HxvwLA")
                        val intent = Intent(requireContext(), VideoPlayerActivity::class.java)
                        val serviceBundle = Bundle()
                        serviceBundle.putParcelableArrayList(Constant.ITEM_KEY, songsList)
                        serviceBundle.putString(Constant.LIST_TYPE, Constant.VIDEO_LIST)
                        serviceBundle.putString(
                            Constant.SELECTED_CONTENT_ID,
                            tvShowEpisodeList.get(childPosition).contentId
                        )
                        serviceBundle.putInt(
                            Constant.TYPE_ID,
                            tvShowEpisodeList.get(childPosition).type!!
                        )
                        intent.putExtra(Constant.BUNDLE_KEY, serviceBundle)
                        intent.putExtra("thumbnailImg", tvShowEpisodeList.get(childPosition).image)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                        if (activity != null) {
                            val status = (activity as MainActivity).getAudioPlayerPlayingStatus()
                            if (status == Constant.pause) {
                                SharedPrefHelper.getInstance()
                                    .setLastAudioContentPlayingStatus(true)
                            } else {
                                SharedPrefHelper.getInstance()
                                    .setLastAudioContentPlayingStatus(false)
                            }
                            (activity as MainActivity).pausePlayer()
                        }
                        startActivity(intent)

                        /*val bundle = Bundle()
                        bundle.putString("image", tvShowEpisodeList.get(childPosition).image)
                        bundle.putString("id", tvShowEpisodeList.get(childPosition).contentId)
                        if (childPosition % 2 == 0)
                            bundle.putBoolean("varient", true)
                        else
                            bundle.putBoolean("varient", false)
                        var varient = 1
                        *//*if (!TextUtils.isEmpty(varientType)) {
                    if (varientType.equals("v2", true)) {
                        varient = 2
                    }
                }*//*
                bundle.putString("playerType", tvShowEpisodeList.get(childPosition).type.toString())
                val tvShowDetailsFragment = TvShowDetailsFragment(varient)
                tvShowDetailsFragment.arguments = bundle
                addFragment(R.id.fl_container, this@DownloadedEpsiodesFragment, tvShowDetailsFragment, false)*/
                    }
                }
            }
            override fun pauseOrResumeDownload(datalist: DownloadedAudio, position: Int) {
                if (datalist.downloadStatus == Status.PAUSED.value){
                    //DemoUtil.getDownloadManager(requireContext()).resumeDownloads()
                    //(activity as BaseActivity).resumeAllVideoDownloads()
                    (activity as BaseActivity).pauseAllVideoDownloads(false, datalist.downloadUrl)
                }else if(datalist.downloadStatus == Status.QUEUED.value){
                    /*DemoUtil.getDownloadManager(requireContext()).pauseDownloads()
                    (requireActivity() as MainActivity).addOrUpdateDownloadVideoQueue(
                        ArrayList(),
                        this@DownloadedEpsiodesFragment,
                        true,
                        true
                    )*/
                    (activity as BaseActivity).pauseAllVideoDownloads(true, datalist.downloadUrl)
                }else if (datalist.downloadStatus == Status.DOWNLOADING.value){
                   /* DemoUtil.getDownloadManager(requireContext()).pauseDownloads()
                    (requireActivity() as MainActivity).addOrUpdateDownloadVideoQueue(
                        ArrayList(),
                        this@DownloadedEpsiodesFragment,
                        true,
                        true
                    )*/
                    (activity as BaseActivity).pauseAllVideoDownloads(true, datalist.downloadUrl)
                }
                updateDownloadAdapter()
            }

            override fun onItemSelection(
                data: DownloadedAudio,
                childPosition: Int,
                isSelected: Int
            ) {
                if (isSelected == 2){
                    deletableDownloadList.add(data)
                }else{
                    if (!deletableDownloadList.isNullOrEmpty()){
                        val iterator: MutableIterator<DownloadedAudio> = deletableDownloadList.iterator() // it will return iterator
                        while (iterator.hasNext()) {
                            val downloadedAudio = iterator.next()
                            if (downloadedAudio.aId == data.aId){
                                iterator.remove()
                            }
                        }
                    }
                }
            }
        })
        rvTVShows.adapter = epsiodeAdapter
        updateDownloadAdapter()
        /*epsiodesList.add(
            DownloadedEpsiodesModel(
                "Ready to Mingle",
                "S01 ‧ EP01 ‧ 52 Min",
                R.drawable.image_epsiodes_01
            )
        )
        epsiodesList.add(
            DownloadedEpsiodesModel(
                "Love Poisoning",
                "S01 ‧ EP02 ‧ 52 Min",
                R.drawable.image_epsiodes_02
            )
        )
        epsiodesList.add(
            DownloadedEpsiodesModel(
                "Love True",
                "S01 ‧ EP01 ‧ 52 Min",
                R.drawable.image_epsiodes_03
            )
        )
        epsiodeAdapter.setEpsiodes(epsiodesList)*/

        CommonUtils.setPageBottomSpacing(rvTVShows, requireContext(),
            resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
            resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
        setBottomSpace()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onResume() {
        super.onResume()
        setLocalBroadcast()
        setLog("tvShowEpisodeList","tvShowEpisodeList${tvShowEpisodeList}")
        (requireActivity() as MainActivity).addOrUpdateDownloadVideoQueue(
            ArrayList(),
            this,
            true,
            false
        )
    }

    override fun onDownloadVideoQueueItemChanged(
        downloadManager: DownloadManager,
        download: Download
    ) {
        baseMainScope?.launch {
            updateDownloadAdapter()
        }

    }

    override fun onDownloadProgress(
        downloads: List<Download?>?,
        progress: Int,
        currentExoDownloadPosition: Int
    ) {
        //updateDownloadAdapter()
    }

    override fun onDownloadsPausedChanged(
        downloadManager: DownloadManager,
        downloadsPaused: Boolean?
    ) {
        baseMainScope.launch {
            updateDownloadAdapter()
        }

    }

    private fun updateDownloadAdapter(){
        try {
            tvShowEpisodeList = AppDatabase.getInstance()?.downloadedAudio()
                ?.getDownloadQueueItemsByContentType(ContentTypes.TV_SHOWS.value) as ArrayList<DownloadedAudio>
            val downloadQueueList = AppDatabase.getInstance()?.downloadQueue()
                ?.getDownloadQueueItemsByContentType(ContentTypes.TV_SHOWS.value) as ArrayList<DownloadQueue>
            setLog("videoDeleted", "DownloadedEpsiodesFragment-updateDownloadAdapter-downloadQueueList.size-${downloadQueueList.size}")
            for (downloadQueue in downloadQueueList.iterator()){
                setLog("downloadQueue","downloadQueue${downloadQueue}")
                tvShowEpisodeList.add(CommonUtils.getDownloadedAudioModel(downloadQueue, false))
            }
            if (epsiodeAdapter != null) {
                setProgressBarVisible(false)
                setLog("videoDeleted", "DownloadedEpsiodesFragment-updateDownloadAdapter-tvShowEpisodeList.size-${tvShowEpisodeList.size}")
                if (!tvShowEpisodeList.isNullOrEmpty()){
                    tvShowEpisodeList.forEachIndexed { index, downloadedAudio ->
                        //setLog("videoDeleted", "DownloadedEpsiodesFragment-updateDownloadAdapter-isEdit-$isEdit")
                        if (isEdit){
                            downloadedAudio.isSelected = 1
                        }
                        if (!deletableDownloadList.isNullOrEmpty()){
                            deletableDownloadList.forEachIndexed { i, downloadedAudio2 ->
                                setLog("videoDeleted", "DownloadedEpsiodesFragment-updateDownloadAdapter-downloadedAudio.contentId-${downloadedAudio.contentId}-downloadedAudio2.contentId-${downloadedAudio2.contentId}")
                                if (downloadedAudio.contentId.equals(downloadedAudio2.contentId)){
                                    downloadedAudio.isSelected = 2
                                }
                            }
                        }
                    }
                }
                epsiodeAdapter?.setEpsiodes(tvShowEpisodeList)
            }
        }catch (e:Exception){

        }
    }

    override fun onUserSubscriptionUpdateCall(status: Int, contentId: String) {

    }

    override fun onClick(v: View) {
        super.onClick(v)
        if (v == rlMenu){
            if (isEdit){
                setBottomView(0, status = false, isDeleted = false)
            }else{
                setBottomView(1, status = true, isDeleted = false)
            }
        }else if (v == tvSelectAll){
            deletableDownloadList = ArrayList<DownloadedAudio>()
            deletableDownloadList.addAll(tvShowEpisodeList)
            setBottomView(2, status = true, isDeleted = false)
        }else if (v == tvRemove){
            openDeletePopup(true)
        }
    }

    override fun onContentRemovedFromDownload(isRemoved: Boolean, content: DownloadedAudio) {
        super.onContentRemovedFromDownload(isRemoved, content)
        if (isRemoved){
            if (epsiodeAdapter != null) {
                updateDownloadAdapter()
            }
        }
    }

    private fun setBottomView(select: Int, status: Boolean, isDeleted:Boolean){
        setLog("videoDeleted", "DownloadedEpsiodesFragment-setBottomView-tvShowEpisodeList.size="+tvShowEpisodeList.size)
        if (!tvShowEpisodeList.isNullOrEmpty() && !isDeleted){
            setLog("videoDeleted", "DownloadedEpsiodesFragment-setBottomView-if-1")
            for (item in tvShowEpisodeList){
                item.isSelected = select
            }
            if (epsiodeAdapter != null) {
                setLog("videoDeleted", "DownloadedEpsiodesFragment-setBottomView-epsiodeAdapter update")
                epsiodeAdapter?.setEpsiodes(tvShowEpisodeList)
                epsiodeAdapter?.notifyDataSetChanged()
            }
        }else{
            setLog("videoDeleted", "DownloadedEpsiodesFragment-setBottomView-else-1")
            updateDownloadAdapter()
        }
        if (status){
            setLog("videoDeleted", "DownloadedEpsiodesFragment-setBottomView-if-2")
            isEdit = true
            ivMenu?.setImageDrawable(requireContext().faDrawable(R.string.icon_delete, R.color.colorWhite))
            (requireActivity() as MainActivity).hideMiniPlayer()
            setBottomSpace()
            clBottomView?.show()
            tvSelectAll?.setOnClickListener(this)
            tvRemove?.setOnClickListener(this)
        }else{
            setLog("videoDeleted", "DownloadedEpsiodesFragment-setBottomView-else-1")
            isEdit = false
            ivMenu?.setImageDrawable(requireContext().faDrawable(R.string.icon_edit, R.color.colorWhite))
            (requireActivity() as MainActivity).showMiniPlayer()
            clBottomView?.hide()
            tvSelectAll?.setOnClickListener(null)
            tvRemove?.setOnClickListener(null)
            deletableDownloadList.clear()
        }
    }

    override fun deleteDownloadedContent(status:Boolean) {
        if (status){
            GlobalScope.launch {
                withContext(Dispatchers.IO){
                    setLog("videoDeleted", "DownloadedEpsiodesFragment-deleteDownloadedContent-deletableDownloadList.size-${deletableDownloadList.size}")
                    if (!deletableDownloadList.isNullOrEmpty()){
                        for (content in deletableDownloadList.iterator()){
                            GlobalScope.launch(Dispatchers.IO) {
                                setLog("videoDeleted", "DownloadedEpsiodesFragment-deleteDownloadedContent-delete file")
                                (activity as BaseActivity).removeVideoContentFromDownload(Uri.parse(content.downloadUrl))
                                CommonUtils.removeDownloadedContent(content, requireContext(), this@DownloadedEpsiodesFragment)
                            }
                        }
                    }
                }
            }
        }else{
            if (!isEdit){
                setBottomView(0, status = false, isDeleted = false)
            }
        }

    }

    override fun openDeletePopup(status: Boolean) {
        if (status){
            setLog("videoDeleted", "DownloadedEpsiodesFragment-openDeletePopup-deletableDownloadList.size-${deletableDownloadList.size}")
            if (!deletableDownloadList.isNullOrEmpty()){
                val pauseAllDialog = DeleteDownloadedContentDialog(this, deletableDownloadList.size, getString(R.string.podcast_str_9))
                if(!pauseAllDialog.isVisible){
                    pauseAllDialog.show(activity?.supportFragmentManager!!, "open logout dialog")
                }else{
                    pauseAllDialog.dismiss()
                }
            }
        }else{
            deletableDownloadList.clear()
        }
    }

    override fun share() {
        setLog(TAG, "share: click")
        var shareurl =
            getString(R.string.music_player_str_18) + " " + tvShowEpisodeList?.get(playableItemPosition)?.contentShareLink
        Utils.shareItem(requireActivity(), shareurl)
        eventThreeDotsMenuClick(tvShowEpisodeList?.get(playableItemPosition)?.title.toString(), getString(R.string.popup_str_101))
    }

    override fun postAsStory() {
        eventThreeDotsMenuClick(tvShowEpisodeList?.get(playableItemPosition)?.title.toString(), getString(R.string.general_str_7))
    }

    private fun setLocalBroadcast(){
        (requireActivity() as MainActivity).setLocalBroadcastEventCall(this, Constant.AUDIO_PLAYER_EVENT)
    }

    override fun onLocalBroadcastEventCallBack(context: Context?, intent: Intent) {
        if (isAdded){
            val event = intent.getIntExtra("EVENT", 0)
            if (event == Constant.AUDIO_PLAYER_RESULT_CODE) {
                CommonUtils.setPageBottomSpacing(rvTVShows, requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
                val marginBotom = rvTVShows.paddingBottom - resources.getDimensionPixelSize(R.dimen.dimen_10)
                if (marginBotom >= 0){
                    Utils.setMarginsBottom(clBottomView, marginBotom)
                }
            }
        }
    }

    override fun onFileDeleted(isDeleted: Boolean) {
        setLog("videoDeleted", "DownloadedEpsiodesFragment-onFileDeleted-$isDeleted")
        setLog("videoDeleted", "DownloadedEpsiodesFragment-deleteDownloadedContent-after delete file")
        setLog("videoDeleted", "DownloadedEpsiodesFragment-deleteDownloadedContent-deletableDownloadList.size-${deletableDownloadList.size}")
        for (content in deletableDownloadList.iterator()){
            val iterator: MutableIterator<DownloadedAudio> = tvShowEpisodeList.iterator() // it will return iterator
            while (iterator.hasNext()) {
                val downloadedAudio = iterator.next()
                if (downloadedAudio.contentId.equals(content.contentId)){
                    iterator.remove()
                }
            }
        }
        setLog("videoDeleted", "DownloadedEpsiodesFragment-deleteDownloadedContent-after remove from list")
        setLog("videoDeleted", "DownloadedEpsiodesFragment-deleteDownloadedContent-tvShowEpisodeList.size-${tvShowEpisodeList.size}")
        GlobalScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.Main){
                //delay(1000)
                setLog("videoDeleted", "DownloadedEpsiodesFragment-deleteDownloadedContent-call update view")
                val messageModel = MessageModel(getString(R.string.tv_show_deleted_successfully), getString(R.string.tv_show_deleted_successfully),
                    MessageType.NEUTRAL, true)
                if (isAdded && activity != null){
                    CommonUtils.showToast(requireContext(), messageModel)
                }
                setBottomView(0, status = false, isDeleted = true)
            }
        }
    }

    private fun setBottomSpace(){
        val marginBottom = rvTVShows.paddingBottom - resources.getDimensionPixelSize(R.dimen.dimen_10)
        if (marginBottom >= 0){
            Utils.setMarginsBottom(clBottomView, marginBottom)
        }
    }
}