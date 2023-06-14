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
import com.hungama.music.HungamaMusicApp
import com.hungama.music.R
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.utils.customview.downloadmanager.model.DownloadQueue
import com.hungama.music.utils.customview.downloadmanager.model.DownloadedAudio
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.data.model.*
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.adapter.MovieDownloadAdapter
import com.hungama.music.player.download.DemoUtil
import com.hungama.music.player.videoplayer.VideoPlayerActivity
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.utils.CommonUtils.getDownloadedAudioModel
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant
import com.hungama.music.utils.Utils
import kotlinx.android.synthetic.main.common_header_action_bar.*
import kotlinx.android.synthetic.main.fragment_movie_download.*
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class MovieDownloadFragment : BaseFragment(), BaseActivity.OnDownloadVideoQueueItemChanged,
    OnUserSubscriptionUpdate, BaseFragment.OnMenuItemClicked,
    MoviePauseDialog.openDeletePopupListener, DeleteDownloadedContentDialog.addPauseListener,
    BaseActivity.OnLocalBroadcastEventCallBack,CommonUtils.OnFileDeleted {
    var movieAdater: MovieDownloadAdapter? = null
    var isEdit = false
    var deletableDownloadList = ArrayList<DownloadedAudio>()
    var contentType = ContentTypes.MOVIES.value
    companion object{
        var movielist = ArrayList<DownloadedAudio>()
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
        return inflater.inflate(R.layout.fragment_movie_download, container, false)
    }

    override fun initializeComponent(view: View) {
        setLog(TAG, "initializeComponent: ")

        if (arguments != null){
            contentType = requireArguments().getInt(Constant.CONTENT_TYPE)
        }
        if (contentType == ContentTypes.SHORT_FILMS.value){
            tvActionBarHeading.text = getString(R.string.library_video_str_17)
        }else{
            tvActionBarHeading.text = getString(R.string.download_str_25)
        }
        ivBack?.setOnClickListener { view -> backPress() }
        rlMenu?.visibility = View.VISIBLE
        rlMenu?.setOnClickListener(this)
        setBottomView(0, status = false, isDeleted = false)

        rvMoviesData.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        movieAdater = MovieDownloadAdapter(requireContext(),contentType, object : MovieDownloadAdapter.MovieItemListener {

                override fun showMovieDeleteDialog(adapterPosition: Int) {
                    setBottomView(0, status = false, isDeleted = false)
                    deletableDownloadList = ArrayList<DownloadedAudio>()
                    if (!movielist.isNullOrEmpty() && movielist.size > adapterPosition){
                        deletableDownloadList.add(movielist.get(adapterPosition))
                        val moviePauseDialog = MoviePauseDialog(movielist.get(adapterPosition), this@MovieDownloadFragment,true)
                        moviePauseDialog.show(activity?.supportFragmentManager!!, "movie pause dialog show")
                    }
                }

                override fun showDetail(datalist: DownloadedAudio) {
                    val bundle = Bundle()
                    /*bundle.putString("image", datalist?.image)
                    bundle.putString("id", datalist?.contentId)
                    bundle.putString("playerType", "" + datalist?.pType!!)
                    bundle.putBoolean("varient", true)

                    val variant = 1
                    val movieDetailsFragment = MovieV1Fragment(variant)
                    movieDetailsFragment.arguments = bundle

                    addFragment(R.id.fl_container, this@MovieDownloadFragment, movieDetailsFragment, false)*/
                    if (datalist != null) {
                        val dpm = DownloadPlayCheckModel()
                        dpm.contentId = datalist?.contentId?.toString()!!
                        dpm.contentTitle = datalist?.title?.toString()!!
                        if (datalist.movierights.toString().contains(PlanNames.FVOD.name, true)){
                            dpm.planName = "[SVOD]"
                        }else{
                            dpm.planName = datalist.movierights.toString()
                        }
                        dpm.isAudio = false
                        dpm.isDownloadAction = false
                        dpm.isDirectPaymentAction = false
                        dpm.queryParam = ""
                        dpm.isShowSubscriptionPopup = true
                        dpm.clickAction = ClickAction.FOR_SINGLE_CONTENT
                        dpm.restrictedDownload = RestrictedDownload.NONE_DOWNLOAD_CONTENT
                        Constant.screen_name ="Movie Download"
                        if (CommonUtils.userCanDownloadContent(requireContext(), movieDetailroot, dpm, this@MovieDownloadFragment,Constant.drawer_svod_purchase)) {
                            playAllMovies(dpm.contentId)
                        }
                    }
                }

                override fun pauseOrResumeDownload(datalist: DownloadedAudio, position: Int) {
                    setLog(TAG, "pauseOrResumeDownload: datalist : ${datalist}")
                    setLog(TAG, "pauseOrResumeDownload: downloadIndex : ${DemoUtil.getDownloadManager(requireContext()).downloadIndex}")
                    setLog(TAG, "pauseOrResumeDownload: currentDownloads : ${DemoUtil.getDownloadManager(requireContext()).currentDownloads}")
                    if (datalist.downloadStatus == Status.PAUSED.value){
                        //DemoUtil.getDownloadManager(requireContext()).resumeDownloads()
                        //(requireActivity() as BaseActivity).resumeAllVideoDownloads()
                        (activity as BaseActivity).pauseAllVideoDownloads(false, datalist.downloadUrl)
                    }else if(datalist.downloadStatus == Status.QUEUED.value || datalist.downloadStatus == Status.DOWNLOADING.value){
//                            DemoUtil.getDownloadManager(requireContext()).setStopReason(datalist?.downloadUrl,Download.STOP_REASON_NONE)
//                        DemoUtil.getDownloadManager(requireContext()).pauseDownloads()
                        (requireActivity() as BaseActivity).pauseAllVideoDownloads(true, datalist.downloadUrl)
                        //(requireActivity() as MainActivity).addOrUpdateDownloadVideoQueue(ArrayList(), this@MovieDownloadFragment, true)
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
        rvMoviesData.adapter = movieAdater

        updateDownloadAdapter()
        CommonUtils.setPageBottomSpacing(rvMoviesData, requireContext(),
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
        baseMainScope?.launch {
            updateDownloadAdapter()
        }

    }

    override fun onDownloadsPausedChanged(
        downloadManager: DownloadManager,
        downloadsPaused: Boolean?
    ) {
        baseMainScope?.launch {
            updateDownloadAdapter()
        }

    }

    private fun updateDownloadAdapter(){
        setLog("MovieDownloadFragment", "updateDownloadAdapter-contentType-$contentType")
        //val contentTypes:Array<Int> = arrayOf(contentType)
        movielist = AppDatabase?.getInstance()?.downloadedAudio()
            ?.getDownloadQueueItemsByContentType(contentType) as ArrayList<DownloadedAudio>
        val downloadQueueList = AppDatabase?.getInstance()?.downloadQueue()
            ?.getDownloadQueueItemsByContentType(contentType) as ArrayList<DownloadQueue>

        for (downloadQueue in downloadQueueList.iterator()){
            movielist.add(getDownloadedAudioModel(downloadQueue, false))
        }
        if (movieAdater != null) {
            setProgressBarVisible(false)
            if (!movielist.isNullOrEmpty()){
                movielist.forEachIndexed { index, downloadedAudio ->
                    if (isEdit){
                        downloadedAudio.isSelected = 1
                    }
                    if (!deletableDownloadList.isNullOrEmpty()){
                        deletableDownloadList.forEachIndexed { i, downloadedAudio2 ->
                            if (downloadedAudio.contentId.equals(downloadedAudio2.contentId)){
                                downloadedAudio.isSelected = 2
                            }
                        }
                    }
                }
            }
            movieAdater?.addData(movielist)
        }
    }

    override fun onUserSubscriptionUpdateCall(status: Int, contentId: String) {

    }

    private fun playAllMovies(selectedContentId:String){

        CoroutineScope(Dispatchers.IO).launch {
            CommonUtils.setDownloadEventModelDataAppLevel(selectedContentId)
        }

        val songsList = CommonUtils.getVideoDummyData2("https://hunstream.hungama.com/c/5/481/3d4/48090348/48090348_,100,400,750,1000,1600,.mp4.m3u8?rtLFaR4wQhnQIwZj-gbvlKvXi6fnpm8zqQD_AVZHY1bwN0aPUIi99NRWCgtfsYx_4rANuyEvwF6-l4O1vfy8khCL2v6l-9IL1Knc0y-Oc_WoL5hQeTmyi3HxvwLA")
        val intent = Intent(requireContext(), VideoPlayerActivity::class.java)
        val serviceBundle = Bundle()
        serviceBundle.putParcelableArrayList(Constant.ITEM_KEY, songsList)
        serviceBundle.putString(Constant.LIST_TYPE, Constant.VIDEO_LIST)
        serviceBundle.putString(Constant.SELECTED_CONTENT_ID,selectedContentId)
        serviceBundle.putInt(Constant.CONTENT_TYPE, Constant.CONTENT_MOVIE)
        serviceBundle.putInt(Constant.TYPE_ID, Constant.CONTENT_MOVIE)
        intent.putExtra(Constant.BUNDLE_KEY, serviceBundle)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        serviceBundle.putLong(
            Constant.VIDEO_START_POSITION, TimeUnit.SECONDS.toMillis(
                HungamaMusicApp.getInstance().getContentDuration(selectedContentId!!)!!))
        //(requireActivity() as MainActivity).setLocalBroadcastEventCall(this, Constant.VIDEO_PLAYER_EVENT)
        startActivity(intent)
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
            deletableDownloadList.addAll(movielist)
            setBottomView(2, status = true, isDeleted = false)
        }else if (v == tvRemove){
            setLog(TAG, "onClick: this is working tvRemove")
            openDeletePopup(true)
        }
    }

    override fun onContentRemovedFromDownload(isRemoved: Boolean, content: DownloadedAudio) {
        super.onContentRemovedFromDownload(isRemoved, content)
        if (isRemoved){
            if (movieAdater != null) {
                updateDownloadAdapter()
            }
        }
    }

    private fun setBottomView(select: Int, status: Boolean, isDeleted:Boolean){

        if (!movielist.isNullOrEmpty() && !isDeleted){
            for (item in movielist){
                item.isSelected = select
            }
            if (movieAdater != null) {
                movieAdater?.addData(movielist)
            }
        }else{
            updateDownloadAdapter()
        }
        if (status){
            isEdit = true
            ivMenu?.setImageDrawable(requireContext().faDrawable(R.string.icon_delete, R.color.colorWhite))
            (requireActivity() as MainActivity).hideMiniPlayer()
            setBottomSpace()
            clBottomView?.visibility = View.VISIBLE
            tvSelectAll?.setOnClickListener(this)
            tvRemove?.setOnClickListener(this)
        }else{
            isEdit = false
            ivMenu?.setImageDrawable(requireContext().faDrawable(R.string.icon_edit, R.color.colorWhite))
            (requireActivity() as MainActivity).showMiniPlayer()
            clBottomView?.visibility = View.GONE
            tvSelectAll?.setOnClickListener(null)
            tvRemove?.setOnClickListener(null)
            deletableDownloadList.clear()
        }
    }

    override fun deleteDownloadedContent(status:Boolean) {
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
                                CommonUtils.removeDownloadedContent(content, requireContext(), this@MovieDownloadFragment)
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
            if (!deletableDownloadList.isNullOrEmpty()){
                val pauseAllDialog = DeleteDownloadedContentDialog(this, deletableDownloadList.size, getString(R.string.download_str_18))
                setLog(TAG, "openDeletePopup:pauseAllDialog "+pauseAllDialog)
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
        Utils.shareItem(requireContext(), movielist.get(playableItemPosition).contentShareLink)
        eventThreeDotsMenuClick(movielist?.get(playableItemPosition)?.title.toString(), getString(R.string.popup_str_101))
    }

    override fun postAsStory() {
        eventThreeDotsMenuClick(movielist?.get(playableItemPosition)?.title.toString(), getString(R.string.general_str_7))
        val title = movielist?.get(playableItemPosition)?.title
        val subtitle = movielist?.get(playableItemPosition)?.subTitle
        val contentURL = movielist?.get(playableItemPosition)?.contentShareLink
        val imageURL = movielist?.get(playableItemPosition)?.image

        val track = Track()
        track.title = title
        track.subTitle = subtitle
        track.image = imageURL
        track.url = contentURL

        val sheet = ShareStoryPlatformDialog((activity as BaseActivity)!!, track)
        sheet.show(activity?.supportFragmentManager!!, "openStoryPlatformDialog")
    }

    private fun setLocalBroadcast(){
        (requireActivity() as MainActivity).setLocalBroadcastEventCall(this, Constant.AUDIO_PLAYER_EVENT)
    }

    override fun onLocalBroadcastEventCallBack(context: Context?, intent: Intent) {
        if (isAdded){
            val event = intent.getIntExtra("EVENT", 0)
            if (event == Constant.AUDIO_PLAYER_RESULT_CODE) {
                CommonUtils.setPageBottomSpacing(rvMoviesData, requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
                val marginBotom = rvMoviesData.paddingBottom - resources.getDimensionPixelSize(R.dimen.dimen_10)
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
            val iterator: MutableIterator<DownloadedAudio> = movielist.iterator() // it will return iterator
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
                val messageModel = MessageModel(getString(R.string.movie_deleted_successfully), getString(R.string.movie_deleted_successfully),
                    MessageType.NEUTRAL, true)
                if (isAdded && activity != null){
                    CommonUtils.showToast(requireContext(), messageModel)
                }
                setBottomView(0, status = false, isDeleted = true)
            }
        }
    }

    private fun setBottomSpace(){
        val marginBotom = rvMoviesData.paddingBottom - resources.getDimensionPixelSize(R.dimen.dimen_10)
        if (marginBotom >= 0){
            Utils.setMarginsBottom(clBottomView, marginBotom)
        }
    }
}