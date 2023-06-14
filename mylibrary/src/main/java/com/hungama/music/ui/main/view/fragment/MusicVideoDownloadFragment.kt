package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import com.hungama.music.utils.customview.downloadmanager.model.DownloadQueue
import com.hungama.music.utils.customview.downloadmanager.model.DownloadedAudio
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.data.model.ContentTypes
import com.hungama.music.data.model.PlanNames
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.adapter.MusicVideoDownloadAdapter
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.utils.CommonUtils.removeDownloadedContent
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant
import com.hungama.music.utils.Utils
import kotlinx.android.synthetic.main.common_header_action_bar.*
import kotlinx.android.synthetic.main.fragment_music_video_download.*
import kotlinx.coroutines.*


class MusicVideoDownloadFragment : BaseFragment(), BaseActivity.OnDownloadVideoQueueItemChanged,
    BaseFragment.OnMenuItemClicked, DeleteDownloadedContentDialog.addPauseListener,MoviePauseDialog.openDeletePopupListener,
    BaseActivity.OnLocalBroadcastEventCallBack , CommonUtils.OnFileDeleted,
    MusicVideoThreeDotFragment.addPauseListener{
    var musicVideoDownloadAdapter : MusicVideoDownloadAdapter? = null
    var isEdit = false
    var deletableDownloadList = ArrayList<DownloadedAudio>()
    var contentType = ContentTypes.VIDEO.value
    companion object{
        var musicvideoList = ArrayList<DownloadedAudio>()
        var playableItemPosition = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_music_video_download, container, false)
    }

    override fun initializeComponent(view: View) {
        if (arguments != null){
            contentType = requireArguments().getInt(Constant.CONTENT_TYPE)
        }
        if (contentType == ContentTypes.SHORT_VIDEO.value){
            tvActionBarHeading.text = getString(R.string.library_video_str_19)
        }else{
            tvActionBarHeading.text = getString(R.string.music_video_str_3)
        }
        ivBack?.setOnClickListener { view -> backPress() }
        rlMenu?.visibility = View.VISIBLE
        rlMenu?.setOnClickListener(this)
        setBottomView(0, status = false, isDeleted = false)
        rvMusicVideo.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        musicVideoDownloadAdapter = MusicVideoDownloadAdapter(requireContext(),object : MusicVideoDownloadAdapter.OnItemClick{
            override fun onMusicItemVideoClick(childPosition: Int) {
                playableItemPosition = childPosition
                if (CommonUtils.isUserHasGoldSubscription()){
                    if (activity != null && activity is MainActivity){
                        (activity as MainActivity).setPauseMusicPlayerOnVideoPlay()
                    }
                    val bundle = Bundle()
                    bundle.putString(Constant.defaultContentId, musicvideoList?.get(childPosition)?.contentId)
                    bundle.putSerializable(Constant.EXTRA_LIST, musicvideoList)
                    bundle.putInt(Constant.CHILD_POSITION, childPosition)
                    val videoDetailsFragment = MusicVideoDetailsFragment()
                    videoDetailsFragment.arguments = bundle
                    addFragment(R.id.fl_container, this@MusicVideoDownloadFragment, videoDetailsFragment, false)
                }else{
                    Constant.screen_name ="Music Video Download Screen"
                    CommonUtils.openSubscriptionDialogPopup(
                        requireContext(),
                        PlanNames.SVOD.name,
                        "",
                        true,
                        null,
                        "",
                        null,Constant.drawer_svod_purchase
                    )
                }
            }

            override fun pauseOrResumeDownload(datalist: DownloadedAudio, position: Int) {
                playableItemPosition = position
                if (datalist.downloadStatus == Status.PAUSED.value){
                    //DemoUtil.getDownloadManager(requireContext()).resumeDownloads()
                    setLog(TAG, "pauseOrResumeDownload: Status.PAUSED")
                    //(activity as BaseActivity).resumeAllVideoDownloads()
                    (activity as BaseActivity).pauseAllVideoDownloads(false, datalist.downloadUrl)
                }else if(datalist.downloadStatus == Status.QUEUED.value){
                    //DemoUtil.getDownloadManager(requireContext()).pauseDownloads()
                    setLog(TAG, "pauseOrResumeDownload: Status.QUEUED")
                    (activity as BaseActivity).pauseAllVideoDownloads(true, datalist.downloadUrl)
                    //(requireActivity() as MainActivity).addOrUpdateDownloadVideoQueue(ArrayList(), this@MusicVideoDownloadFragment, true)
                }else if (datalist.downloadStatus == Status.DOWNLOADING.value){
                    //DemoUtil.getDownloadManager(requireContext()).pauseDownloads()
                    setLog(TAG, "pauseOrResumeDownload: Status.DOWNLOADING")
                    (activity as BaseActivity).pauseAllVideoDownloads(true, datalist.downloadUrl)
                    //(requireActivity() as MainActivity).addOrUpdateDownloadVideoQueue(ArrayList(), this@MusicVideoDownloadFragment, true)
                }
                updateDownloadAdapter()
            }

            override fun onMusicVideoMenuClick(childPosition: Int) {
                playableItemPosition = childPosition
//                commonThreeDotMenuItemSetup(DOWNLOADED_MUSIC_VIDEO_ADAPTER_PAGE, this@MusicVideoDownloadFragment)
                setLog("musicvideoList","musicvideoList${musicvideoList}")
                deletableDownloadList = ArrayList<DownloadedAudio>()
                if (!musicvideoList.isNullOrEmpty() && musicvideoList.size > playableItemPosition){
                    deletableDownloadList.add(musicvideoList.get(playableItemPosition))
                    val moviePauseDialog = MoviePauseDialog(musicvideoList.get(playableItemPosition), this@MusicVideoDownloadFragment,false)
                    moviePauseDialog.show(activity?.supportFragmentManager!!, "movie pause dialog show")
                }
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
        rvMusicVideo.adapter = musicVideoDownloadAdapter
        updateDownloadAdapter()
        CommonUtils.setPageBottomSpacing(rvMusicVideo, requireContext(),
            resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
            resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
        val marginBotom = rvMusicVideo.paddingBottom - resources.getDimensionPixelSize(R.dimen.dimen_10)
        if (marginBotom >= 0){
            Utils.setMarginsBottom(clBottomView, marginBotom)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    private fun updateDownloadAdapter(){
        setLog("MusicVideoDownloadFragment", "updateDownloadAdapter-contentType-$contentType")
        musicvideoList = AppDatabase.getInstance()?.downloadedAudio()
            ?.getDownloadQueueItemsByContentType(contentType) as ArrayList<DownloadedAudio>
        val downloadQueueList = AppDatabase.getInstance()?.downloadQueue()
            ?.getDownloadQueueItemsByContentType(contentType) as ArrayList<DownloadQueue>
        for (downloadQueue in downloadQueueList.iterator()){
            musicvideoList.add(CommonUtils.getDownloadedAudioModel(downloadQueue, false))
        }
        if (musicVideoDownloadAdapter != null) {
            setProgressBarVisible(false)
            musicVideoDownloadAdapter?.setMusicVideoList(musicvideoList)
        }else{

        }
    }

    override fun onResume() {
        super.onResume()
        setLocalBroadcast()
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
        baseMainScope.launch {
            updateDownloadAdapter()
        }
    }

    override fun onDownloadProgress(
        downloads: List<Download?>?,
        progress: Int,
        currentExoDownloadPosition: Int
    ) {
        baseMainScope?.launch {
            setLog("MusicVideoDownloadFragment", "onDownloadProgress-progress-$progress")
            updateDownloadAdapter()
        }

    }

    override fun onDownloadsPausedChanged(
        downloadManager: DownloadManager,
        downloadsPaused: Boolean?
    ) {
        baseMainScope?.launch {
            setLog("MusicVideoDownloadFragment", "onDownloadsPausedChanged-downloadsPaused-$downloadsPaused")
            updateDownloadAdapter()
        }

    }

    override fun onContentRemovedFromDownload(isRemoved: Boolean, content: DownloadedAudio) {
        super.onContentRemovedFromDownload(isRemoved, content)
        if (isRemoved){
            if (musicVideoDownloadAdapter != null) {
                updateDownloadAdapter()
            }
        }
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
            deletableDownloadList.addAll(musicvideoList)
            setBottomView(2, status = true, isDeleted = false)
        }else if (v == tvRemove){
            if (!deletableDownloadList.isNullOrEmpty()){
                val pauseAllDialog = DeleteDownloadedContentDialog(this, deletableDownloadList.size, getString(R.string.library_str_2))
                if(!pauseAllDialog.isVisible){
                    pauseAllDialog.show(activity?.supportFragmentManager!!, "open logout dialog")
                }else{
                    pauseAllDialog.dismiss()
                }
            }
        }
    }

    private fun setBottomView(select: Int, status: Boolean, isDeleted:Boolean){

        if (!musicvideoList.isNullOrEmpty() && !isDeleted){
            for (item in musicvideoList){
                item.isSelected = select
            }
            if (musicVideoDownloadAdapter != null) {
                musicVideoDownloadAdapter?.setMusicVideoList(musicvideoList)
            }
        }else{
            updateDownloadAdapter()
        }
        setLog("MusicVideoDownloadFragment", "setBottomViewstatus-${status} isDeleted:${isDeleted}")

        if (status){
            isEdit = true
            ivMenu?.setImageDrawable(requireContext().faDrawable(R.string.icon_delete, R.color.colorWhite))
            (requireActivity() as MainActivity).hideMiniPlayer()
            clBottomView?.visibility = View.VISIBLE
            clBottomView?.invalidate()
            tvSelectAll?.setOnClickListener(this)
            tvRemove?.setOnClickListener(this)
            setLog("MusicVideoDownloadFragment", "clBottomView visiable -${status} isDeleted:${isDeleted}")

        }else{
            isEdit = false
            ivMenu?.setImageDrawable(requireContext().faDrawable(R.string.icon_edit, R.color.colorWhite))
            (requireActivity() as MainActivity).showMiniPlayer()
            clBottomView?.visibility = View.GONE
            tvSelectAll?.setOnClickListener(null)
            tvRemove?.setOnClickListener(null)
            deletableDownloadList.clear()
        }

        CommonUtils.setPageBottomSpacing(rvMusicVideo, requireContext(),
            resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
            resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
        val marginBotom = rvMusicVideo.paddingBottom - resources.getDimensionPixelSize(R.dimen.dimen_10)
        if (marginBotom >= 0){
            Utils.setMarginsBottom(clBottomView, marginBotom)
        }
    }

    override fun deleteDownloadedContent(status: Boolean) {
        if (status){
            GlobalScope.launch {
                withContext(Dispatchers.IO){
                    if (!deletableDownloadList.isNullOrEmpty()){
                        for (content in deletableDownloadList.iterator()){
                            GlobalScope.launch(Dispatchers.IO) {
                                CommonUtils.setLog(
                                    "videoDeleted",
                                    "DownloadedEpsiodesFragment-deleteDownloadedContent-delete file"
                                )
                                (activity as BaseActivity).removeVideoContentFromDownload(Uri.parse(content.downloadUrl))
                                removeDownloadedContent(content, requireContext(), this@MusicVideoDownloadFragment)
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

    private fun setLocalBroadcast(){
        (requireActivity() as MainActivity).setLocalBroadcastEventCall(this, Constant.AUDIO_PLAYER_EVENT)
    }

    override fun onLocalBroadcastEventCallBack(context: Context?, intent: Intent) {
        if (isAdded){
            val event = intent.getIntExtra("EVENT", 0)
            if (event == Constant.AUDIO_PLAYER_RESULT_CODE) {
                CommonUtils.setPageBottomSpacing(rvMusicVideo, requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
                val marginBotom = rvMusicVideo.paddingBottom - resources.getDimensionPixelSize(R.dimen.dimen_10)
                if (marginBotom >= 0){
                    Utils.setMarginsBottom(clBottomView, marginBotom)
                }
            }
        }
    }

    override fun onFileDeleted(isDeleted: Boolean) {
        CommonUtils.setLog("videoDeleted", "DownloadedEpsiodesFragment-onFileDeleted-$isDeleted")
        CommonUtils.setLog(
            "videoDeleted",
            "DownloadedEpsiodesFragment-deleteDownloadedContent-after delete file"
        )
        for (content in deletableDownloadList.iterator()){
            val iterator: MutableIterator<DownloadedAudio> = musicvideoList.iterator() // it will return iterator
            while (iterator.hasNext()) {
                val downloadedAudio = iterator.next()
                if (downloadedAudio.aId == content.aId){
                    iterator.remove()
                }
            }
        }
        CommonUtils.setLog(
            "videoDeleted",
            "DownloadedEpsiodesFragment-deleteDownloadedContent-after remove from list"
        )
        GlobalScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.Main){
                //delay(1000)
                CommonUtils.setLog(
                    "videoDeleted",
                    "DownloadedEpsiodesFragment-deleteDownloadedContent-call update view"
                )
                setBottomView(0, status = false, isDeleted = true)
            }
        }
    }

    override fun openDeletePopup(status: Boolean) {
        if (status){
            if (!deletableDownloadList.isNullOrEmpty()){
                val pauseAllDialog = MusicVideoThreeDotFragment(musicvideoList.get(playableItemPosition).contentShareLink,this,musicvideoList?.get(playableItemPosition)?.title.toString())
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
        Utils.shareItem(requireContext(), musicvideoList.get(playableItemPosition).contentShareLink)
        eventThreeDotsMenuClick(musicvideoList?.get(playableItemPosition)?.title.toString(), getString(R.string.popup_str_101))
    }

    override fun postAsStory() {

    }

    override fun RemoveMusicVideo(status: Boolean) {
        if (status){
            GlobalScope.launch {
                withContext(Dispatchers.IO){
                    if (!deletableDownloadList.isNullOrEmpty()){
                        for (content in deletableDownloadList.iterator()){
                            GlobalScope.launch(Dispatchers.IO) {
                                CommonUtils.setLog(
                                    "videoDeleted",
                                    "DownloadedEpsiodesFragment-deleteDownloadedContent-delete file"
                                )
                                (activity as BaseActivity).removeVideoContentFromDownload(Uri.parse(content.downloadUrl))
                                removeDownloadedContent(content, requireContext(), this@MusicVideoDownloadFragment)
                            }
                        }
                    }
                }
            }
        }
    }
}