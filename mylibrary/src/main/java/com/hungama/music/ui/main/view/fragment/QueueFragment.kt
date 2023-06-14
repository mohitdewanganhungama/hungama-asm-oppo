package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.media3.common.util.Util
import com.hungama.music.R
import com.hungama.music.ui.main.adapter.QueueAdapter
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.queue.NowPlayingQueue
import com.hungama.music.player.audioplayer.services.AudioPlayerService
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.data.model.*
import com.hungama.music.player.audioplayer.model.Track_State
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.utils.CommonUtils.isContentDownloaded
import com.hungama.music.utils.CommonUtils.toBitmap
import com.hungama.music.utils.ConnectionUtil
import com.hungama.music.utils.Constant
import com.hungama.music.utils.Constant.PLAY_CONTEXT_TYPE
import com.hungama.music.utils.Constant.SELECTED_TRACK_POSITION
import com.hungama.music.utils.Utils
import com.hungama.music.ui.main.viewmodel.PlayableContentViewModel
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import kotlinx.android.synthetic.main.fr_queue.*
import kotlinx.android.synthetic.main.header_queue.*
import kotlinx.android.synthetic.main.new_now_playing_bottom_sheet.*
import kotlinx.coroutines.*
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 * Use the [QueueFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class QueueFragment : BaseFragment(), BaseActivity.PlayItemChangeListener,
    BaseFragment.OnMenuItemClicked, DeleteContentFromQueueDialog.deleteListener {
    var currentPlaySongIndex = -1
    var queueSongList: MutableList<Track>? = arrayListOf()
    var adapter: QueueAdapter? = null
    var playableContentViewModel: PlayableContentViewModel = PlayableContentViewModel()
    lateinit var touchHelper: ItemTouchHelper
    var mLayoutManager:LinearLayoutManager? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fr_queue, container, false)
    }

    override fun initializeComponent(view: View) {
        setLog("BaseActivity:-", "hideBottomNavigationBar-queue - hide")
        (activity as MainActivity).hideBottomNavigationBar()
        (activity as MainActivity).showMiniPlayer()
        (activity as MainActivity).changeMiniPlayerProgressAlignment()
        (activity as MainActivity).addPlayItemChangeListener(this)

        ivBackQueue?.setOnClickListener {
            activity?.onBackPressed()
            (activity as MainActivity).toggleSheetBehavior()

        }
        img_queue_menu_dots?.setOnClickListener {
            //showPopup(img_queue_menu_dots)
            commonThreeDotMenuItemSetup(Constant.QUEUE_MANAGER_DETAIL_PAGE, this)
        }

        buildUI(BaseActivity.songDataList!!, true)

        (activity as MainActivity).nowPlayingQueue?.changeQueueItem(object :
            NowPlayingQueue.OnQueueItemChanged {
            override fun onQueueItemChanged(arrayList: MutableList<Track>, isQueueReordered:Boolean) {
                setLog("queue--3", arrayList.toString())
                //setLog("queue--3", "QueueFragment-onQueueItemChanged- track.title-${arrayList.get(BaseActivity.nowPlayingCurrentIndex()).title} - track.state-${arrayList.get(BaseActivity.nowPlayingCurrentIndex()).state}")
                buildUI(arrayList)
                if (isQueueReordered){
                    setLog("queueReordered", "reordered")
                    val intent = Intent(Constant.AUDIO_PLAYER_EVENT)
                    intent.putExtra("EVENT", Constant.AUDIO_PLAYER_SONG_LIST_CHANGED_RESULT_CODE)
                    LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
                }
            }
        })
    }

    private fun buildUI(trackList: MutableList<Track>, isResetAdapter:Boolean=false) {
        //queueSongList = ArrayList()
        if (trackList != null && trackList.size!! > 0) {
            setLog("queue--4", trackList.toString())
            queueSongList = trackList
            //currentPlaySongIndex = (activity as BaseActivity).nowPlayingCurrentIndex()
            GlobalScope.launch {
                setLog("queue--5", trackList.toString())
                if (isResetAdapter){
                    setLog("queue--5.1", trackList.toString())
                    setQueueAdapter(trackList)
                }else{
                    if (adapter != null){
                        setLog("queue--5.2", trackList.toString())
                        //setLog("queue--5.2", "QueueFragment-buildUI- track.title-${trackList.get(BaseActivity.nowPlayingCurrentIndex()).title} - track.state-${trackList.get(BaseActivity.nowPlayingCurrentIndex()).state}")
                        GlobalScope.launch {
                            withContext(Dispatchers.Main){
                                //setLog("queue--5.2.1", "QueueFragment-buildUI- track.title-${trackList.get(BaseActivity.nowPlayingCurrentIndex()).title} - track.state-${trackList.get(BaseActivity.nowPlayingCurrentIndex()).state}")
                                adapter?.addData(trackList as ArrayList<Track>)
                            }
                        }
                    }else{
                        setLog("queue--5.3", trackList.toString())
                        setQueueAdapter(trackList)
                    }
                }
                val currentPlaySongIndex = BaseActivity.nowPlayingCurrentIndex()
                if (trackList.size > currentPlaySongIndex){
                    setArtImageBg(trackList.get(currentPlaySongIndex))
                }
            }
        }else{
            setLog("queue--6", trackList.size.toString())
            queueSongList = ArrayList()
            GlobalScope.launch {
                setQueueAdapter(trackList)
            }
        }

    }

    private fun setUpPlayableContentListViewModel(id: Long) {
        playableContentViewModel = ViewModelProvider(
            this
        ).get(PlayableContentViewModel::class.java)


        if (ConnectionUtil(context).isOnline) {
            playableContentViewModel?.getPlayableContentList(requireContext(), "" + id)?.observe(this,
                Observer {
                    when(it.status){
                        com.hungama.music.data.webservice.utils.Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                if (!TextUtils.isEmpty(it?.data?.data?.head?.headData?.misc?.url)) {
                                    setPlayableContentListData(it?.data!!)
                                } else {
                                    currentPlaySongIndex = currentPlaySongIndex + 1
                                    if (currentPlaySongIndex < queueSongList?.size!!) {
                                        setEventModelDataAppLevel(""+queueSongList?.get(currentPlaySongIndex)?.id!!,""+queueSongList?.get(currentPlaySongIndex)?.title,"")
                                        setUpPlayableContentListViewModel(queueSongList?.get(currentPlaySongIndex)?.id!!)
                                    }
                                }
                            }
                        }

                        com.hungama.music.data.webservice.utils.Status.LOADING ->{
                            setProgressBarVisible(true)
                        }

                        com.hungama.music.data.webservice.utils.Status.ERROR ->{
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

    fun setPlayableContentListData(playableContentModel: PlayableContentModel) {

        if (playableContentModel != null) {
            setLog("PlayableItem", playableContentModel?.data?.head?.headData?.id.toString())

            queueSongList?.forEach {
                /*if(playableContentModel?.id.equals(""+it?.id) && it.uniquePosition == queueSongList.get(currentPlaySongIndex).uniquePosition){
                    setPlaylistSongList(playableContentModel,it)
                }*/
                if (playableContentModel?.data?.head?.headData?.id.equals("" + it?.id)) {
                    setPlaylistSongList(playableContentModel, it)
                }
            }

            //BaseActivity.setTrackListData(queueSongList as ArrayList<Track>)
            if (queueSongList != null && queueSongList?.size!! > 0){
                (activity as MainActivity).nowPlayingQueue?.updateTrack(queueSongList?.get(currentPlaySongIndex)!!)

                val intent = Intent(requireActivity(), AudioPlayerService::class.java)
                intent.action = AudioPlayerService.PlaybackControls.PLAY.name
                intent.putExtra(PLAY_CONTEXT_TYPE, Constant.PLAY_CONTEXT.QUEUE_TRACKS)
                intent.putExtra(Constant.IS_TRACKS_QUEUEITEM, true)
                intent.putExtra(SELECTED_TRACK_POSITION, currentPlaySongIndex)
                Util.startForegroundService(requireActivity(), intent)
                (activity as MainActivity).reBindService()
            }

        }
    }

    fun setPlaylistSongList(
        playableContentModel: PlayableContentModel?,
        playableItem: Track
    ) {
        val track = playableItem
        if (!TextUtils.isEmpty("" + playableItem?.id)) {
            track.id = playableItem.id
        } else {
            track.id = 0
        }
        if (!TextUtils.isEmpty(playableItem?.title)) {
            track.title = playableItem?.title
        } else {
            track.title = ""
        }

        if (!TextUtils.isEmpty(playableItem?.subTitle)) {
            track.subTitle = playableItem?.subTitle
        } else {
            track.subTitle = ""
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.url)) {
            track.url = playableContentModel?.data?.head?.headData?.misc?.url
        } else {
            track.url = ""
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.downloadLink?.drm?.token)) {
            track.drmlicence = playableContentModel?.data?.head?.headData?.misc?.downloadLink?.drm?.token
        } else {
            track.drmlicence = ""
        }
        if (!playableContentModel?.data?.head?.headData?.misc?.movierights.isNullOrEmpty()){
            track.movierights = playableContentModel?.data?.head?.headData?.misc?.movierights.toString()
        }else{
            track.movierights = ""
        }

        if (!TextUtils.isEmpty(playableItem?.playerType)) {
            track.playerType = playableItem?.playerType
        } else {
            track.playerType = ""
        }
        if (!TextUtils.isEmpty(playableItem?.heading)) {
            track.heading = playableItem?.heading
        } else {
            track.heading = ""
        }
       if (!TextUtils.isEmpty(playableItem?.image)) {
            track.image = playableItem?.image
        } else {
            track.image = ""
        }

        track.explicit = playableItem.explicit
        track.restrictedDownload = playableItem.restrictedDownload
        track.attributeCensorRating =
            playableItem.attributeCensorRating
        setArtImageBg(track)
        if (!queueSongList.isNullOrEmpty() && queueSongList?.size!! > currentPlaySongIndex){
            queueSongList?.set(currentPlaySongIndex, track)
        }
    }

    private suspend fun setQueueAdapter(queueSongList: MutableList<Track>) {
        withContext(Dispatchers.Main) {
            if (isAdded){
                adapter = QueueAdapter(
                    requireActivity(), queueSongList as ArrayList<Track>,
                    object : QueueAdapter.OnItemClick {
                        override fun onItemDeleteClick(position: Int) {
                            callDisplayAlertDialog(position)
                        }

                        override fun onItemPlayClick(position: Int) {
                            try {
                                currentPlaySongIndex = position
                                if (!BaseActivity.songDataList.isNullOrEmpty() && BaseActivity.songDataList.size > currentPlaySongIndex){
                                    if (BaseActivity.songDataList.size > BaseActivity.nowPlayingCurrentIndex()){
                                        (activity as MainActivity).callUserStreamUpdate(-1,
                                            BaseActivity.songDataList.get(BaseActivity.nowPlayingCurrentIndex()), BaseActivity.nowPlayingCurrentIndex())
                                    }

                                    val isOfflinePlay = isContentDownloaded(BaseActivity.songDataList, currentPlaySongIndex)
                                    if (isOfflinePlay){
                                        playContentOffline(BaseActivity.songDataList, currentPlaySongIndex, false)
                                    }else{
                                        if (BaseActivity.songDataList.get(currentPlaySongIndex).pType == DetailPages.LOCAL_DEVICE_SONG_PAGE.value){
                                            playContentOfflineDeviceSongs(BaseActivity.songDataList, currentPlaySongIndex, false)
                                        }else{
                                            playContentOnline(BaseActivity.songDataList, currentPlaySongIndex, false)
                                        }
                                    }
                                }
                            }catch (e:Exception){

                            }
                        }

                        override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
                            touchHelper?.startDrag(viewHolder)
                        }

                    })

                // Setup ItemTouchHelper
                val callback = DragManageAdapter(
                    requireActivity(),
                    adapter!!
                )
                touchHelper = ItemTouchHelper(callback)
                touchHelper.attachToRecyclerView(rvQueue)
                mLayoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
                rvQueue?.apply {
                    layoutManager = mLayoutManager
                    setRecycledViewPool(RecyclerView.RecycledViewPool())
                    setHasFixedSize(true)
                }

                rvQueue?.adapter = adapter
                // Jira issue :  https://hungama.atlassian.net/jira/software/c/projects/HU/issues/HU-129
//            rvQueue?.setPadding(
//                0,
//                0,
//                0,
//                getDeviceHeight(requireActivity()) - resources.getDimensionPixelSize(R.dimen.dimen_48) - resources.getDimensionPixelSize(
//                    R.dimen.dimen_48
//                ) - resources.getDimensionPixelSize(R.dimen.dimen_16)
//            )
                try {
                    if (rvQueue != null){
                        setLog("openQueue", "QueueFragment-setQueueAdapter-rvQueueLayout=${rvQueue.layoutManager}")
                        rvQueue?.post(Runnable { // Call smooth scroll
                            //rvQueue.smoothScrollToPosition((activity as BaseActivity).nowPlayingQueue.currentPlayingTrackIndex)
                            /*(rvQueue.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                                currentPlaySongIndex,
                                0
                            )*/
                            setLog("openQueue", "QueueFragment-setQueueAdapter-rvQueueLayout2=${rvQueue.layoutManager}")
                            mLayoutManager?.scrollToPositionWithOffset(
                                ((activity as BaseActivity).nowPlayingQueue?.currentPlayingTrackIndex!!),
                                0
                            )
                        })
                    }
                }catch (e:Exception){

                }
            }
        }
    }

    private fun callDisplayAlertDialog(position: Int) {
        /*MaterialAlertDialogBuilder(requireContext())
            .setTitle("Alert")
            .setMessage("Are you sure you want to remove this song from playlist?")
            .setNeutralButton("No") { dialog, which ->
                dialog?.dismiss()
            }

            .setPositiveButton("Yes") { dialog, which ->
                val trackDeletedItem = queueSongList?.get(position)
                queueSongList?.removeAt(position)
                adapter?.notifyItemRemoved(position)
                AppDatabase?.getInstance()?.trackDao()?.deleteSongByUniquePosition(trackDeletedItem?.uniquePosition!!)
                onSongDeleteUpdateUpcommingNextView(queueSongList as ArrayList<Track>)
                dialog?.dismiss()
            }
            .show()*/
        val trackDeletedItem = queueSongList?.get(position)
        if (trackDeletedItem != null){
            val deleteContentFromQueueDialog = DeleteContentFromQueueDialog(this, trackDeletedItem.title.toString(), trackDeletedItem.contentType, position)
            if(!deleteContentFromQueueDialog.isVisible){
                deleteContentFromQueueDialog.show(activity?.supportFragmentManager!!, "open logout dialog")
            }else{
                deleteContentFromQueueDialog.dismiss()
            }
        }

    }

    override fun onClick(v: View) {
        super.onClick(v)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    fun setArtImageBg(track: Track) {
        try {
            val artImageUrl = track?.image
            var musicPlayerBgArtImageDrawable: LayerDrawable? = null
            if (!TextUtils.isEmpty(artImageUrl)) {
                val bgColor = ColorDrawable(resources.getColor(R.color.home_bg_color))
                val bgImage: Drawable? =
                    ContextCompat.getDrawable(requireActivity(), R.drawable.audio_player_bg_two)
                val gradient: Drawable? = ContextCompat.getDrawable(
                    requireActivity(),
                    R.drawable.audio_player_gradient_drawable
                )

                val result: Deferred<Bitmap?> = GlobalScope.async {
                    val urlImage = URL(artImageUrl)
                    urlImage.toBitmap()
                }

                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        // get the downloaded bitmap
                        val bitmap: Bitmap? = result.await()
                        //val alfaBitmat = CommonUtils.adjustOpacity(bitmap!!, 125)
                        val artImage = BitmapDrawable(resources, bitmap)
                        musicPlayerBgArtImageDrawable =
                            LayerDrawable(arrayOf<Drawable>(bgColor, artImage, gradient!!))
                        MainScope().launch {
                            rlMain?.background = musicPlayerBgArtImageDrawable
                        }
                    } catch (exp: Exception) {
                        exp.printStackTrace()
                    }


                }
            }
        } catch (exp: Exception) {
            exp.printStackTrace()
        }


    }

    class DragManageAdapter(
        val context: Context,
        adapter: QueueAdapter
    ) : ItemTouchHelper.Callback() {
        var queueAdapter = adapter
        var targetPosition = 0
        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            setLog("onDrrag", "MoveFlag")
            return makeMovementFlags(dragFlags, 0)
        }
        override fun isItemViewSwipeEnabled(): Boolean {
            return false
        }
        override fun isLongPressDragEnabled(): Boolean {
            return false
        }
        override fun onMove(
            recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            setLog("onDrrag", "Move-${viewHolder.adapterPosition}-${target.adapterPosition}")
            targetPosition = target.adapterPosition
            return true
        }
        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                if (viewHolder is QueueAdapter.ItemViewHolder) {
                    queueAdapter.onItemSelected(viewHolder)
                }
            }
            super.onSelectedChanged(viewHolder, actionState)
        }
        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            setLog("onDrrag", "clear-${viewHolder.adapterPosition}-${targetPosition}")
            queueAdapter.onItemClear(viewHolder)
        }
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            setLog("onDrrag", "Swiped")
        }

        override fun onMoved(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            fromPos: Int,
            target: RecyclerView.ViewHolder,
            toPos: Int,
            x: Int,
            y: Int
        ) {
            super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)
            setLog("onDrrag", "Movedd-${fromPos}-${toPos}")
            targetPosition = toPos
            queueAdapter.swapItems(fromPos, toPos)
        }

    }



    override fun playItemChange() {
        //buildUI()
        setLog("tag","playItemChange size:${queueSongList?.size} base size:${BaseActivity.songDataList?.size}")
    }

    interface OnTracksReordered {
        fun onTracksReordered(itemsList: MutableList<Track>)
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as BaseActivity).changeMiniPlayerProgressAlignment()
        (activity as MainActivity).nowPlayingQueue?.changeQueueItem(null)
        (activity as MainActivity).showBottomNavigationBar()
    }

    private fun playContentOnline(songDataList: ArrayList<Track>?, index: Int, isPause: Boolean) {
        if (!songDataList.isNullOrEmpty() && songDataList?.size!! > index){
            setUpPlayableContentListViewModel(songDataList?.get(index)?.id!!)
        }

    }

    private fun playContentOffline(songDataList: ArrayList<Track>?, index: Int, isPause: Boolean) {
        val downloadedAudio = AppDatabase?.getInstance()?.downloadedAudio()?.findByContentId(queueSongList?.get(currentPlaySongIndex)?.id!!.toString())
        if (downloadedAudio != null){
            val playableContentModel = PlayableContentModel()
            playableContentModel.data?.head?.headData?.id = downloadedAudio?.contentId!!
            playableContentModel.data?.head?.headData?.misc?.url = downloadedAudio.downloadedFilePath
            playableContentModel.data?.head?.headData?.misc?.downloadLink?.drm?.url = downloadedAudio?.downloadUrl!!
            playableContentModel.data?.head?.headData?.misc?.downloadLink?.drm?.token = downloadedAudio.drmLicense
            playableContentModel.data?.head?.headData?.misc?.sl?.lyric?.link = downloadedAudio?.lyricsUrl
            setPlayableContentListData(playableContentModel)
            if (!songDataList.isNullOrEmpty() && songDataList?.size!! > index) {
                val downloadedAudio = AppDatabase?.getInstance()?.downloadedAudio()
                    ?.findByContentId(queueSongList?.get(currentPlaySongIndex)?.id!!.toString())
                val playableContentModel = PlayableContentModel()
                playableContentModel.data?.head?.headData?.id = downloadedAudio?.contentId!!
                playableContentModel.data?.head?.headData?.misc?.url = downloadedAudio.downloadedFilePath
                playableContentModel.data?.head?.headData?.misc?.downloadLink?.drm?.url= downloadedAudio?.downloadUrl!!
                playableContentModel.data?.head?.headData?.misc?.downloadLink?.drm?.token= downloadedAudio.drmLicense
                playableContentModel.data?.head?.headData?.misc?.sl?.lyric?.link =
                    downloadedAudio?.lyricsUrl
                setPlayableContentListData(playableContentModel)
            }
          }

    }

    private fun playContentOfflineDeviceSongs(songDataList: ArrayList<Track>?, index: Int, isPause: Boolean) {

            if (!songDataList.isNullOrEmpty() && songDataList?.size!! > index) {
                val playableContentModel = PlayableContentModel()
                playableContentModel.data?.head?.headData?.id =
                    songDataList?.get(index)?.id.toString()
                playableContentModel.data?.head?.headData?.misc?.url =
                    songDataList?.get(index)?.url.toString()
                playableContentModel.data?.head?.headData?.misc?.downloadLink?.drm?.url = ""
                playableContentModel.data?.head?.headData?.misc?.downloadLink?.drm?.token = ""
                playableContentModel.data?.head?.headData?.misc?.sl?.lyric?.link = ""
                setPlayableContentListData(playableContentModel)
            }
    }

    private fun showPopup(view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.inflate(R.menu.queue_menu)

        popup.setOnMenuItemClickListener { item: MenuItem? ->

            when (item?.itemId) {
                R.id.clearAllsongs -> {
                    //Toast.makeText(requireContext(), item.title, Toast.LENGTH_SHORT).show()
                    /*val track = BaseActivity.songDataList?.get(BaseActivity.nowPlayingCurrentIndex())
                    val trackList:ArrayList<Track> = ArrayList()
                    if (track != null) {
                        trackList.add(track)
                    }
                    (activity as MainActivity).nowPlayingQueue?.setupQueue(trackList, false)
                    (activity as MainActivity).nowPlayingQueue?.currentPlayingTrackIndex = 0
                    BaseActivity.updateNowPlayingCurrentIndex(0)
                    if (track != null) {
                        (requireActivity() as MainActivity).callSongLyricsApi(track)
                        (requireActivity() as MainActivity).updatePlayerView()
                    }*/
                    clearQueue(true)
                }
            }

            true
        }

        popup.show()
    }

    private fun clearQueue(isClear: Boolean){
        if (isClear && activity != null){
            (activity as MainActivity).nowPlayingQueue?.nowPlayingTracksList = ArrayList()
            (activity as MainActivity).nowPlayingQueue?.currentPlayingTrackIndex = 0
            BaseActivity.setTrackListData(ArrayList())
            (activity as MainActivity).closeMusicMiniplayer()
            BaseActivity.updateNowPlayingCurrentIndex(0)

            val messageModel = MessageModel(getString(R.string.menu_str_30), getString(R.string.popup_str_86),
                MessageType.NEUTRAL, true)
            CommonUtils.showToast(requireContext(), messageModel)
            (activity as MainActivity).openPlayerScreen(5, Bundle(), true)

        }
    }

    override fun onClearQueue(isClear: Boolean) {
        super.onClearQueue(isClear)
        clearQueue(isClear)
    }

    private fun onSongDeleteUpdateUpcommingNextView(queueSongList: ArrayList<Track>) {
        queueSongList?.forEachIndexed { index, track ->
            if(track.state == Track_State.PLAYING){
                BaseActivity.updateNowPlayingCurrentIndex(index)
                setLog("TAG", "nowPlayingPosition:"+BaseActivity.nowPlayingCurrentIndex())
            }
        }
        BaseActivity?.setTrackListData(queueSongList)

        val intent = Intent(context, AudioPlayerService::class.java)
        intent.apply {
            action = AudioPlayerService.PlaybackControls.RE_ORDER.name
            putExtra(Constant.TRACKS_LIST, ArrayList(queueSongList))
        }
        context?.let {
            Util.startForegroundService(it, intent)
        }
        (activity as MainActivity).setSongLyricsData(null, null)
        (activity as MainActivity).setEnableDisableNextPreviousIcons()
    }

    override fun deleteContentFromQueue(status: Boolean, contentPosition: Int) {
        if (status && !queueSongList.isNullOrEmpty() && queueSongList?.size!! > contentPosition){
            val trackDeletedItem = queueSongList?.get(contentPosition)
            queueSongList?.removeAt(contentPosition)
            adapter?.notifyItemRemoved(contentPosition)
            AppDatabase?.getInstance()?.trackDao()?.deleteSongByUniquePosition(trackDeletedItem?.uniquePosition!!)
            onSongDeleteUpdateUpcommingNextView(queueSongList as ArrayList<Track>)
            val messageModel = MessageModel(getString(R.string.toast_str_41), getString(R.string.toast_str_7),
                MessageType.NEUTRAL, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }
}