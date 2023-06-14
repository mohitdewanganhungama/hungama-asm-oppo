package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.hungama.fetch2.Download
import com.hungama.fetch2core.Reason
import com.hungama.music.R
import com.hungama.music.player.audioplayer.TracksContract
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.viewmodel.TracksViewModel
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.utils.customview.downloadmanager.model.DownloadQueue
import com.hungama.music.data.model.ContentTypes
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.player.audioplayer.Injection

import com.hungama.music.ui.main.adapter.DownloadQueueAdapter
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant
import kotlinx.android.synthetic.main.common_header_action_bar.*
import kotlinx.android.synthetic.main.fr_downloadin_progress.*
import kotlinx.android.synthetic.main.header_main.*
import kotlinx.coroutines.*


/**
 * A simple [Fragment] subclass.
 * Use the [DownloadingProgressFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DownloadingProgressFragment : BaseFragment(), TracksContract.View,BaseActivity.OnDownloadQueueItemChanged,
    PauseAllDialog.addPauseListener, BaseActivity.OnLocalBroadcastEventCallBack {

    var addSongItemPosition = 0
    var playableItemPosition = 0

    var downloadQueuetList: ArrayList<DownloadQueue>? = null

    var downloadQueueAdapter: DownloadQueueAdapter? = null
    private lateinit var tracksViewModel: TracksContract.Presenter
    var isDownloadActive = false
    companion object{
        val pauseAllDialog = 1
        val resumeAllDialog = 2
        val cancelAllDialog = 3
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fr_downloadin_progress, container, false)
    }

    override fun initializeComponent(view: View) {

        tvActionBarHeading.setText(getString(R.string.library_all_str_4))

        tracksViewModel = TracksViewModel(Injection.provideTrackRepository(), this)
        getDownloadQueueSongList()

        isDownloadActive = (activity as BaseActivity).isAudioDownloadActive()
        changeButtonText()


        pauseAll?.setOnClickListener(this)
        cancel?.setOnClickListener(this)
        btnExplore?.setOnClickListener(this)
        CommonUtils.applyButtonTheme(requireContext(), btnExplore)
        CommonUtils.applyButtonTheme(requireContext(), llPlayAll)
        CommonUtils.setPageBottomSpacing(rvPlaylist, requireContext(),
            resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
            resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
    }

    override fun onClick(v: View) {
        super.onClick(v)
        if(v==pauseAll){
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), pauseAll!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            val pauseAllDialog = PauseAllDialog(this,if (isDownloadActive) pauseAllDialog else resumeAllDialog)
            if(!pauseAllDialog.isVisible){
                pauseAllDialog.show(activity?.supportFragmentManager!!, "open logout dialog")
            }else{
                pauseAllDialog.dismiss()
            }

        }else if(v==cancel){
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), cancel!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            val pauseAllDialog = PauseAllDialog(this, cancelAllDialog)
            if(!pauseAllDialog.isVisible){
                pauseAllDialog.show(activity?.supportFragmentManager!!, "open logout dialog")
            }else{
                pauseAllDialog.dismiss()
            }
        }
        else if(v == btnExplore){
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), cancel!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            (activity as MainActivity).applyScreen(5)
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden && activity != null) {
            (activity as BaseActivity).showBottomNavigationBar()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    private fun getDownloadQueueSongList() {
        try {
            if(isAdded){
                    downloadQueuetList= ArrayList<DownloadQueue>()
                    downloadQueuetList = AppDatabase?.getInstance()?.downloadQueue()
                        ?.getDownloadQueueItemsByAudioContentType(ContentTypes.AUDIO.value, ContentTypes.PODCAST.value) as ArrayList<DownloadQueue>?
                    if (downloadQueuetList != null && downloadQueuetList!!.isNotEmpty()) {
                        setDetails(downloadQueuetList!!)

                        rvPlaylist?.visibility=View.VISIBLE
                        llDownload?.visibility=View.VISIBLE
                        clExplore?.visibility=View.GONE
                    }else{
                        rvPlaylist?.visibility=View.GONE
                        llDownload?.visibility=View.GONE
                        clExplore?.visibility=View.VISIBLE
                    }
            }
        }catch (e:Exception){

        }


    }

    private fun setDetails(downloadQueuetList: java.util.ArrayList<DownloadQueue>) {
        downloadQueueAdapter = DownloadQueueAdapter(
            requireActivity(), downloadQueuetList,
            object : DownloadQueueAdapter.OnChildItemClick {
                override fun onUserClick(
                    childPosition: Int,
                    isMenuClick: Boolean,
                    isDownloadClick: Boolean
                ) {

                }
            })
        if (isAdded)
        rvPlaylist.adapter = downloadQueueAdapter
    }

    override fun startTrackPlayback(
        selectedTrackPosition: Int,
        tracksList: MutableList<Track>,
        trackPlayStartPosition: Long
    ) {

    }

    override fun getViewActivity(): AppCompatActivity {
        return activity as AppCompatActivity
    }

    override fun getApplicationContext(): Context {
        return (activity as AppCompatActivity).applicationContext
    }


    override fun onResume() {
        super.onResume()
        setLocalBroadcast()
        (activity as BaseActivity).addOrUpdateDownloadMusicQueue(
            ArrayList(),
            this,
            null,
            true,
            false
        )
    }

    override fun onDownloadQueueItemChanged(data: Download, reason: Reason) {

        baseIOScope?.launch {
            setLog("DWProgrss-onChangedid", data.id.toString())
            setLog("DWProgrss-onChanged", reason.toString())

            when(reason){
                Reason.DOWNLOAD_ADDED -> {
                    setLog("DWProgrss-ADDED", data.id.toString())
                }
                Reason.DOWNLOAD_QUEUED ->{
                    setLog("DWProgrss-QUEUED", data.id.toString())

                }
                Reason.DOWNLOAD_STARTED->{
                    withContext(Dispatchers.Main){
                        setLog("DWProgrss-STARTED", data.id.toString())
                        isDownloadActive = true
                        changeButtonText()
                        getDownloadQueueSongList()
                    }

                }
                Reason.DOWNLOAD_PROGRESS_CHANGED->{
                    setLog("DWProgrss-CHANGED", data.id.toString())
                }
                Reason.DOWNLOAD_RESUMED->{
                    setLog("DWProgrss-RESUMED", data.id.toString())
                }
                Reason.DOWNLOAD_PAUSED->{
                    setLog("DWProgrss-PAUSED", data.id.toString())
                }
                Reason.DOWNLOAD_COMPLETED->{
                    withContext(Dispatchers.Main){
                        setLog("DWProgrss-COMPLETED", data.id.toString())
                        getDownloadQueueSongList()
                    }


                }
                Reason.DOWNLOAD_CANCELLED->{
                    setLog("DWProgrss-CANCELLED", data.id.toString())
                }
                Reason.DOWNLOAD_REMOVED->{
                    setLog("DWProgrss-REMOVED", data.id.toString())
                }
                Reason.DOWNLOAD_DELETED->{
                    setLog("DWProgrss-DELETED", data.id.toString())
                }
                Reason.DOWNLOAD_ERROR->{
                    setLog("DWProgrss-ERROR", data.id.toString())
                }
                Reason.DOWNLOAD_BLOCK_UPDATED->{
                    setLog("DWProgrss-UPDATED", data.id.toString())
                }
                Reason.DOWNLOAD_WAITING_ON_NETWORK->{
                    setLog("DWProgrss-NETWORK", data.id.toString())
                }
                else -> {}
            }
        }

    }

    override fun pauseAll() {
        /*(activity as BaseActivity).fetch?.pauseAll()
        AppDatabase?.getInstance()?.downloadQueue()?.pauseAllAudioDownloads(Status.PAUSED.value, ContentTypes.AUDIO.value)
        getDownloadQueueSongList()*/
        isDownloadActive = false
        changeButtonText()
        (activity as BaseActivity).pauseAllAudioDownloads(true)
        getDownloadQueueSongList()
    }

    override fun resumeAll() {
        isDownloadActive = true
        changeButtonText()
        (activity as BaseActivity).resumeAllAudioDownloads()
        getDownloadQueueSongList()
    }

    override fun cancelAll() {
        /*(activity as BaseActivity).fetch?.cancelAll()
        AppDatabase?.getInstance()?.downloadQueue()?.deleteAll()
        activity?.onBackPressed()*/
        isDownloadActive = false
        (activity as BaseActivity).cancelAllAudioDownloads()
        getDownloadQueueSongList()
    }

    fun changeButtonText(){
        CoroutineScope(Dispatchers.Main).launch {
            if (isDownloadActive){
                pauseAll?.text = getString(R.string.download_str_4)
            }else{
                pauseAll?.text = getString(R.string.download_str_5)
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
                CommonUtils.setPageBottomSpacing(rvPlaylist, requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
            }
        }
    }
}