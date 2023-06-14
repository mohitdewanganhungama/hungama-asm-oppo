package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.data.model.*
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.home.eventreporter.UserAttributeEvent
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.VideoWatchlistAdapter
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.ConnectionUtil
import com.hungama.music.utils.Constant
import com.hungama.music.utils.Constant.VIDEO_WATCHLIST_DETAIL_ADAPTER
import com.hungama.music.utils.Utils
import com.hungama.music.utils.customview.downloadmanager.model.DownloadQueue
import com.hungama.music.R
import kotlinx.android.synthetic.main.common_header_action_bar.*
import kotlinx.android.synthetic.main.fragment_music_video_download.*
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception


class VideoWatchlistItemFragment : BaseFragment(),
    BaseActivity.OnLocalBroadcastEventCallBack, DeleteWatchlistItemDialog.addPauseListener,
    BaseActivity.OnDownloadVideoQueueItemChanged, OnUserSubscriptionUpdate,
    BaseFragment.OnUserContentOrderStatus, BaseFragment.OnMenuItemClicked {
    private var videoWatchlistAdapter: VideoWatchlistAdapter? = null
    var userViewModel: UserViewModel? = null
    var isEdit = false
    private var contentIds: String? = ""
    private lateinit var types: String
    private var selectionItem:Int = 0

    var contentId: Int = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_music_video_download, container, false)
    }
    companion object{
        var musicvideoList = ArrayList<BookmarkDataModel.Data.Body.Row>()
        var movielist = ArrayList<BookmarkDataModel.Data.Body.Row>()
        var playableItemPosition = 0
        fun newInstance(contentId: Int): VideoWatchlistItemFragment{
            val fragment = VideoWatchlistItemFragment()
            val bundle = Bundle()
            bundle.putInt(Constant.defaultContentId, contentId)
            fragment.arguments = bundle
            return fragment
        }
    }
    override fun initializeComponent(view: View) {
        if (arguments != null){
            if (arguments?.containsKey(Constant.defaultContentId)!!){
                contentId = arguments?.getInt(Constant.defaultContentId)!!
            }
        }
        rlMenu?.visibility = View.VISIBLE
        rlMenu?.setOnClickListener(this)
        setBottomView(0, status = false, isDeleted = false);
        setLog(TAG, "initializeComponent: movielist"+ movielist)

        ivMenu?.setImageDrawable(
            requireContext().faDrawable(
                R.string.icon_edit,
                R.color.colorWhite
            )
        )
        rvMusicVideo.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        videoWatchlistAdapter = VideoWatchlistAdapter(
            requireContext(),
            contentId,
            object : VideoWatchlistAdapter.OnItemClick {
                override fun onMusicItemVideoClick(childPosition: Int) {
                    playableItemPosition = childPosition
                    try {
                        val parent = RowsItem()
                        val bodyRowsItemsItem = BodyRowsItemsItem()
                        val bodyDataItem = BodyDataItem()
                        val bodyRowsItemsItemList:ArrayList<BodyRowsItemsItem?> = ArrayList()
                        bodyDataItem.id = musicvideoList.get(childPosition).data.id
                        bodyDataItem.type = musicvideoList.get(childPosition).data.type
                        bodyDataItem.title = musicvideoList.get(childPosition).data.title
                        bodyRowsItemsItem.data = bodyDataItem
                        bodyRowsItemsItemList.add(bodyRowsItemsItem)
                        parent.items = bodyRowsItemsItemList
                        parent.heading = tvActionBarHeading.text.toString()
                        sendArtworkTappedEvent(parent,0,0,null)
                    }catch (e:Exception){

                    }

                    if (musicvideoList?.get(childPosition)?.data?.type.equals("4")) {
                        val bundle = Bundle()
                        bundle.putString("image", musicvideoList?.get(childPosition)?.data?.image)
                        bundle.putString("id", musicvideoList?.get(childPosition)?.data?.id)
                        bundle.putString(
                            "playerType",
                            musicvideoList?.get(childPosition)?.data?.type
                        )
                        bundle.putBoolean("varient", true)
                        var varient = 1
                        val movieDetailsFragment = MovieV1Fragment(varient)
                        movieDetailsFragment.arguments = bundle

                        addFragment(
                            R.id.fl_container,
                            this@VideoWatchlistItemFragment,
                            movieDetailsFragment,
                            false
                        )
                    } else if (musicvideoList?.get(childPosition)?.data?.type.equals("93")) {
                        val bundle = Bundle()
                        bundle.putString("image", musicvideoList?.get(childPosition)?.data?.image)
                        bundle.putString("id", musicvideoList?.get(childPosition)?.data?.id)
                        bundle.putString(
                            "playerType",
                            musicvideoList?.get(childPosition)?.data?.type
                        )
                        bundle.putBoolean("varient", true)

                        var varient = 1

                        val movieDetailsFragment = MovieV1Fragment(varient)
                        movieDetailsFragment.arguments = bundle

                        addFragment(
                            R.id.fl_container,
                            this@VideoWatchlistItemFragment,
                            movieDetailsFragment,
                            false
                        )
                    } else if (musicvideoList?.get(childPosition)?.data?.type.equals("96") || musicvideoList?.get(
                            childPosition
                        )?.data?.type.equals("97") || musicvideoList?.get(childPosition)?.data?.type.equals(
                            "98"
                        )
                    ) {
                        val bundle = Bundle()
                        bundle.putString("image", musicvideoList?.get(childPosition)?.data?.image)
                        bundle.putString("id", musicvideoList?.get(childPosition)?.data?.id)
                        bundle.putString(
                            "playerType",
                            musicvideoList?.get(childPosition)?.data?.type
                        )
                        bundle.putBoolean("varient", true)
                        val item = BodyRowsItemsItem()
                        item.itemId = musicvideoList?.get(childPosition)?.data?.id?.toInt()!!
                        item.type = musicvideoList?.get(childPosition)?.data?.type
                        bundle.putParcelable("child_item", item)
                        if (childPosition % 2 == 0)
                            bundle.putBoolean("varient", true)
                        else
                            bundle.putBoolean("varient", false)
                        var varient = 1

                        val tvShowDetailsFragment = TvShowDetailsFragment(varient)
                        tvShowDetailsFragment.arguments = bundle
                        addFragment(
                            R.id.fl_container,
                            this@VideoWatchlistItemFragment,
                            tvShowDetailsFragment,
                            false
                        )

                    }
                }

                override fun onMusicItemThreeDotClick(childPosition: Int) {
                    playableItemPosition = childPosition
                    commonThreeDotMenuItemSetup(VIDEO_WATCHLIST_DETAIL_ADAPTER, this@VideoWatchlistItemFragment)
                    /*var moviePauseDialog = WatchlistThreeDotDialog(
                        childPosition,
                        musicvideoList?.get(childPosition),
                        object : WatchlistThreeDotDialog.WatchlistThreeDotListener {
                            override fun removeFromMyList(
                                modelItem: BookmarkDataModel.Data.Body.Row?,
                                position: Int
                            ) {
                                setAddOrRemoveWatchlist(
                                    modelItem?.data?.id,
                                    "" + modelItem?.data?.type,
                                    false,
                                    Constant.MODULE_WATCHLIST
                                )
                                musicvideoList?.remove(modelItem)
                                videoWatchlistAdapter?.notifyItemRemoved(position)
                            }

                            override fun download(
                                modelItem: BookmarkDataModel.Data.Body.Row?,
                                position: Int
                            ) {
                                *//**
                                 * add code for download movie
                                 *//*
                                startMovieDownload(modelItem)

                            }

                            override fun share(
                                modelItem: BookmarkDataModel.Data.Body.Row?,
                                position: Int
                            ) {
                                Utils.shareItem(context!!, modelItem!!.data!!.misc!!.share)

                            }

                            override fun postAsStory() {
                                val title = musicvideoList?.get(childPosition)?.data?.title
                                val subtitle = musicvideoList?.get(childPosition)?.data?.subtitle
                                val contentURL = musicvideoList?.get(childPosition)?.data?.misc?.share
                                val imageURL = musicvideoList?.get(childPosition)?.data?.image

                                val track = Track()
                                track.title = title
                                track.subTitle = subtitle
                                track.image = imageURL
                                track.url = contentURL

                                val sheet = ShareStoryPlatformDialog((activity as BaseActivity)!!, track)
                                sheet.show(activity?.supportFragmentManager!!, "openStoryPlatformDialog")
                            }

                            override fun cancel() {

                            }
                        },
                        0
                    )
                    moviePauseDialog.show(
                        activity?.supportFragmentManager!!,
                        "movie pause dialog show"
                    )*/
                }

                override fun onItemSelection(
                    data: BookmarkDataModel.Data.Body.Row,
                    childPosition: Int,
                    isSelected: Int
                ){
                    if (data.data.isSelected == 2){
                        movielist.add(data)
                        setLog(TAG, "onItemSelection: "+ movielist)
                    }else{
                        if (!movielist.isNullOrEmpty()){
                            val iterator: MutableIterator<BookmarkDataModel.Data.Body.Row> = movielist.iterator() // it will return iterator
                            while (iterator.hasNext()) {
                                val downloadedAudio = iterator.next()
                                setLog(TAG, "onItemSelection: "+downloadedAudio.data.id)
                                setLog(TAG, "onItemSelection: "+downloadedAudio.data.title)
                                if (downloadedAudio.data.isSelected == 2){
                                    setLog(TAG, "onItemSelection: "+downloadedAudio.data.title)
                                    setLog(TAG, "onItemSelection: "+downloadedAudio.data.id)
                                    iterator.remove()
                                }
                            }
                        }
                    }
                }

            })
        rvMusicVideo.adapter = videoWatchlistAdapter
        setUpBookmarkModel()

        if (contentId == VideoWatchlistFragment.MOVIE_ID) {
            tvActionBarHeading.setText(getString(R.string.library_video_str_10))
        } else if (contentId == VideoWatchlistFragment.SHORT_FILMS) {
            tvActionBarHeading.setText(getString(R.string.library_video_str_18))
        } else if (contentId == VideoWatchlistFragment.TV_SHOW) {
            tvActionBarHeading.setText(getString(R.string.library_video_str_22))
        }
        CommonUtils.setPageBottomSpacing(
            rvMusicVideo,
            requireContext(),
            resources.getDimensionPixelSize(R.dimen.dimen_0),
            resources.getDimensionPixelSize(R.dimen.dimen_0),
            resources.getDimensionPixelSize(R.dimen.dimen_0),
            0
        )
        setBottomSpace()

    }

    private fun startContentDownload(modelItem: BookmarkDataModel.Data.Body.Row?) {
        val dpm = DownloadPlayCheckModel()
        dpm.contentId = modelItem?.data?.id?.toString()!!
        dpm.contentTitle = modelItem?.data?.title?.toString()!!
        dpm.planName = modelItem?.data?.misc?.movierights.toString()
        dpm.isAudio = false
        dpm.isDownloadAction = true
        dpm.isShowSubscriptionPopup = true
        dpm.clickAction = ClickAction.FOR_SINGLE_CONTENT
        dpm.restrictedDownload =
            RestrictedDownload.valueOf(modelItem?.data?.misc?.restricted_download!!)
        if (modelItem != null && modelItem?.data != null) {
            var attributeCensorRating = ""
            if (!modelItem?.data?.misc?.attributeCensorRating.isNullOrEmpty()) {
                attributeCensorRating =
                    modelItem?.data?.misc?.attributeCensorRating?.get(0).toString()
            }
            Constant.screen_name ="Video Watch List"
            if (CommonUtils.userCanDownloadContent(
                    requireContext(),
                    requireView(),
                    dpm,
                    this,Constant.drawer_svod_download
                )
            ) {
                if (!CommonUtils.checkUserCensorRating(
                        requireContext(),
                        attributeCensorRating
                    )
                ) {
                    setLog("onDwClick", "Clicked")
                    // self download
                    /*startDRMDownloadSong()
                    downloadIconStates(Download.STATE_QUEUED, ivDownload)
                    tvDownload?.text = getString(R.string.in_queue)*/
                    val downloadQueueList: ArrayList<DownloadQueue> = ArrayList()
                    var dq = DownloadQueue()
                    //for (item in playlistSongList?.iterator()!!){

                    dq = DownloadQueue()
                    if (modelItem != null) {
                        if (!TextUtils.isEmpty(modelItem?.data?.id!!)) {
                            dq.parentId = modelItem?.data?.id!!
                            dq.contentId = modelItem?.data?.id!!
                        }
                        if (!TextUtils.isEmpty(modelItem?.data?.title!!)) {
                            dq.pName = modelItem?.data?.title
                            dq.title = modelItem?.data?.title
                        }

                        if (!TextUtils.isEmpty(modelItem?.data?.subtitle!!)) {
                            dq.pSubName = modelItem?.data?.subtitle
                            dq.subTitle = modelItem?.data?.subtitle
                        }

                        if (!TextUtils.isEmpty(modelItem?.data?.releasedate!!)) {
                            dq.pReleaseDate =
                                modelItem?.data?.releasedate
                        }

                        if (!TextUtils.isEmpty(modelItem?.data?.image!!)) {
                            dq.pImage = modelItem?.data?.image
                            dq.image = modelItem?.data?.image
                        }


                        if (!TextUtils.isEmpty(modelItem?.data?.misc?.movierights.toString()!!)) {
                            dq.planName =
                                modelItem?.data?.misc?.movierights.toString()
                            dq.planType = CommonUtils.getContentPlanType(dq.planName)
                        }

                        if (contentId == VideoWatchlistFragment.MOVIE_ID) {
                            dq.pType = DetailPages.VIDEO_WATCHLIST_DETAIL_ADAPTER.value
                            dq.contentType = ContentTypes.MOVIES.value
                        } else if (contentId == VideoWatchlistFragment.SHORT_FILMS) {
                            dq.pType = DetailPages.VIDEO_WATCHLIST_DETAIL_ADAPTER.value
                            dq.contentType = ContentTypes.SHORT_FILMS.value
                        } else if (contentId == VideoWatchlistFragment.TV_SHOW) {
                            dq.pType = DetailPages.VIDEO_WATCHLIST_DETAIL_ADAPTER.value
                            dq.contentType = ContentTypes.TV_SHOWS.value
                        }

                        val downloadQueue =
                            AppDatabase.getInstance()?.downloadQueue()?.findByContentId(
                                modelItem?.data?.id!!.toString()
                            )
                        val downloadedAudio =
                            AppDatabase.getInstance()?.downloadedAudio()
                                ?.findByContentId(
                                    modelItem?.data?.id!!.toString()
                                )
                        if ((!downloadQueue?.contentId.equals(modelItem?.data?.id!!.toString()))
                            && (!downloadedAudio?.contentId.equals(modelItem?.data?.id!!.toString()))
                        ) {
                            downloadQueueList.add(dq)
                        }

                        (requireActivity() as MainActivity).addOrUpdateDownloadVideoQueue(
                            downloadQueueList,
                            this,
                            false,
                            true
                        )

//                        downloadIconStates(Status.DOWNLOADING.value, ivDownload)
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return true
    }

    private fun setUpBookmarkModel() {
        musicvideoList = ArrayList<BookmarkDataModel.Data.Body.Row>()
        if (isAdded && context != null){
            try {
                setBottomView(0, status = false, isDeleted = false);
        if (ConnectionUtil(requireContext()).isOnline) {
            userViewModel = ViewModelProvider(
                this
            ).get(UserViewModel::class.java)


            setLog(TAG, "setUpBookmarkModel contentId:$contentId")
            if (contentId == VideoWatchlistFragment.MOVIE_ID) {
                userViewModel?.getUserBookmarkedData(
                    requireActivity(),
                    Constant.MODULE_WATCHLIST,
                    "4"
                )?.observe(this,
                    Observer {
                        when (it.status) {
                            com.hungama.music.data.webservice.utils.Status.SUCCESS -> {
                                setProgressBarVisible(false)
                                if (it?.data != null) {
                                    musicvideoList = it?.data?.data?.body?.rows!!
                                    if (musicvideoList != null && musicvideoList.size > 0) {
                                        callFavVideoEvent(musicvideoList)
                                        videoWatchlistAdapter?.setMusicVideoList(musicvideoList)
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
            } else if (contentId == VideoWatchlistFragment.TV_SHOW) {
                userViewModel?.getUserBookmarkedData(
                    requireActivity(),
                    Constant.MODULE_WATCHLIST,
                    "96,97,98"
                )?.observe(this,
                    Observer {
                        when (it.status) {
                            com.hungama.music.data.webservice.utils.Status.SUCCESS -> {
                                setProgressBarVisible(false)
                                if (it?.data != null) {
                                    musicvideoList = it?.data?.data?.body?.rows!!
                                    if (musicvideoList != null && musicvideoList.size > 0) {
                                        callFavVideoEvent(musicvideoList)
                                        videoWatchlistAdapter?.setMusicVideoList(musicvideoList)
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
            } else if (contentId == VideoWatchlistFragment.SHORT_FILMS) {
                userViewModel?.getUserBookmarkedData(
                    requireActivity(),
                    Constant.MODULE_WATCHLIST,
                    "93"
                )?.observe(this,
                    Observer {
                        when (it.status) {
                            com.hungama.music.data.webservice.utils.Status.SUCCESS -> {
                                setProgressBarVisible(false)
                                if (it?.data != null) {
                                    musicvideoList = it?.data?.data?.body?.rows!!
                                    if (musicvideoList != null && musicvideoList.size > 0) {
                                        callFavVideoEvent(musicvideoList)
                                        videoWatchlistAdapter?.setMusicVideoList(musicvideoList)
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
            }

        } else {
            val messageModel = MessageModel(
                getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true
            )
            CommonUtils.showToast(requireContext(), messageModel)
        }
            }catch (e:Exception){

            }
        }
    }





    private fun callFavVideoEvent(arrayList: ArrayList<BookmarkDataModel.Data.Body.Row>) {
        val list = ArrayList<String>()
        arrayList.forEach {
            list.add(it?.data?.title!!)
        }
        val userDataMap = java.util.HashMap<String, String>()
        userDataMap.put(EventConstant.FAVOURITED_VIDEO,  Utils.arrayToString(list))
        userDataMap.put(EventConstant.NUMBER_OF_FAVORITED_VIDEOS, "" + list?.size)
        EventManager.getInstance().sendUserAttribute(UserAttributeEvent(userDataMap))
    }

    override fun onResume() {
        super.onResume()
        setLog(TAG, "onResume: ")
        setLocalBroadcast()
    }

    override fun onPause() {
        super.onPause()
        setLog(TAG, "onPause: ")

    }

    override fun onDetach() {
        super.onDetach()
        setLog(TAG, "onDetach: ")
        if (watchlisItemRefresh != null) {
            watchlisItemRefresh?.onReloadItem()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setLog(TAG, "onAttach: ")
    }

    var watchlisItemRefresh: WatchlisItemRefresh? = null
    fun addRefreshItemListiner(watchlisItemRefresh: WatchlisItemRefresh) {
        this.watchlisItemRefresh = watchlisItemRefresh
    }

    interface WatchlisItemRefresh {
        fun onReloadItem()
    }

    private fun setLocalBroadcast() {
        (requireActivity() as MainActivity).setLocalBroadcastEventCall(
            this,
            Constant.AUDIO_PLAYER_EVENT
        )
    }

    override fun onLocalBroadcastEventCallBack(context: Context?, intent: Intent) {
        if (isAdded) {
            val event = intent.getIntExtra("EVENT", 0)
            if (event == Constant.AUDIO_PLAYER_RESULT_CODE) {
                CommonUtils.setPageBottomSpacing(
                    rvMusicVideo,
                    requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_0),
                    resources.getDimensionPixelSize(R.dimen.dimen_0),
                    resources.getDimensionPixelSize(R.dimen.dimen_0),
                    0
                )
                val marginBotom = rvMusicVideo.paddingBottom - resources.getDimensionPixelSize(R.dimen.dimen_10)
                if (marginBotom >= 0){
                    Utils.setMarginsBottom(clBottomView, marginBotom)
                }
            }
        }
    }

    override fun onClick(v: View) {
        super.onClick(v)
        if (v == rlMenu) {
            if (isEdit) {
                setBottomView(0, status = false, isDeleted = false)
            } else {
                setBottomView(1, status = true, isDeleted = false)
            }
        } else if (v == tvSelectAll) {
            movielist.addAll(musicvideoList)
            setBottomView(2, status = true, isDeleted = false)
        } else if (v == tvRemove) {
            setLog(TAG, "deleteWatchlist: selectionItem "+selectionItem)
            deleteWatchlist(true)
//            setLog(TAG, "onClick: this is working")
//            if (!TextUtils.isEmpty(contentIds)){
//                setAddOrRemoveWatchlist(contentIds,types,false,Constant.MODULE_WATCHLIST)
//            }
        }
    }
    private fun deleteWatchlist(status: Boolean){
        setLog(TAG, "deleteWatchlist: selectionItem  delete"+selectionItem)
        if (status){
            if (!movielist.isNullOrEmpty()){
               val pauseAllDialog = DeleteWatchlistItemDialog(this, movielist.size, getString(R.string.download_str_18))
               if(!pauseAllDialog.isVisible){
                   pauseAllDialog.show(activity?.supportFragmentManager!!, "open logout dialog")
               }else{
                   pauseAllDialog.dismiss()
               }
           }
        }else{
            musicvideoList.clear()
        }
    }

    private fun setBottomView(select: Int, status: Boolean, isDeleted: Boolean) {
        if (isAdded && context != null){
            setLog(TAG, "setBottomView: select " + select)
            if (!musicvideoList.isNullOrEmpty() && !isDeleted) {
                for (item in musicvideoList) {
                    item.data.isSelected = select
                    selectionItem = item.data.isSelected
                    setLog(TAG, "setBottomView: select " + item.data.isSelected)
                }
                if (videoWatchlistAdapter != null){
                    videoWatchlistAdapter?.setMusicVideoList(musicvideoList)
                }
            }
            if (status) {
                isEdit = true
                ivMenu?.setImageDrawable(
                    requireContext().faDrawable(
                        R.string.icon_delete,
                        R.color.colorWhite
                    )
                )
                (requireActivity() as MainActivity).hideMiniPlayer()
                setBottomSpace()
                clBottomView?.visibility = View.VISIBLE
                tvSelectAll?.setOnClickListener(this)
                tvRemove?.setOnClickListener(this)
            } else {
                isEdit = false
                ivMenu?.setImageDrawable(
                    requireContext().faDrawable(
                        R.string.icon_edit,
                        R.color.colorWhite
                    )
                )
                (requireActivity() as MainActivity).showMiniPlayer()
                clBottomView?.visibility = View.GONE
                tvSelectAll?.setOnClickListener(null)
                tvRemove?.setOnClickListener(null)
                movielist.clear()
            }
        }
    }

    override fun deleteWatchlistItem(status: Boolean) {
        if (status){
            GlobalScope.launch {
                withContext(Dispatchers.IO){
                    if (!musicvideoList.isNullOrEmpty()){
                        val favArrayType= JSONArray()
                        val list : ArrayList<String> = ArrayList()
                        for (content in musicvideoList.iterator()){
                            if(content.data.isSelected == 2){
//                                list.add(content.data.id.toString())
                                favArrayType.put(content.data.id)
                                setLog(TAG, "deleteWatchlistItem: list"+list)
                            }
                        }
                        GlobalScope.launch(Dispatchers.IO) {
                            setLog(TAG, "deleteWatchlistItem: list"+list)
                            setLog(TAG, "deleteWatchlistItem: "+contentId)
                            val jsonObject = JSONObject()
                            jsonObject.put("contentId",favArrayType)
                            setLog(TAG, "deleteWatchlistItem: "+favArrayType)
                            jsonObject.put("action",false)
                            jsonObject.put("module",Constant.MODULE_WATCHLIST)
                            if(contentId == VideoWatchlistFragment.MOVIE_ID) {
                                jsonObject.put("typeId", 4)
                            }else if (contentId == VideoWatchlistFragment.TV_SHOW){
                                jsonObject.put("typeId", 96)
                            }
                            else if (contentId == VideoWatchlistFragment.SHORT_FILMS){
                                jsonObject.put("typeId", 93)
                            }
                            userViewModel?.multipleBookmarkApi(requireContext(),""+jsonObject.toString())
                        }

                    }
                    for (content in movielist.iterator()){
                        val iterator: MutableIterator<BookmarkDataModel.Data.Body.Row> = musicvideoList.iterator() // it will return iterator
                        while (iterator.hasNext()) {
                            val musiclist = iterator.next()
                            setLog(TAG, "deleteWatchlistItem: .title"+musiclist.data.title)
                            setLog(TAG, "deleteWatchlistItem: isSelected"+musiclist.data.isSelected)
                            if (musiclist.data.id.equals(content.data.id)){
                                setLog(TAG, "deleteWatchlistItem: "+musiclist.data.id)
                                setLog(TAG, "deleteWatchlistItem: "+musiclist.data.isSelected)
                                setLog(TAG, "deleteWatchlistItem: "+musiclist.data.title)
                                iterator.remove()
                            }
                        }
                    }

                    withContext(Dispatchers.Main){
                        delay(1000)
                        if (!movielist.isNullOrEmpty()) {
//                            setBottomView(0, status = false, isDeleted = true)
                            setUpBookmarkModel()
                        }else{
                            backPress()
                        }
                    }
                }
            }
        } else {
            if (!isEdit) {
                setBottomView(0, status = false, isDeleted = false)
            }
        }
    }

    override fun onUserSubscriptionUpdateCall(status: Int, contentId: String) {
        if (isAdded) {
            setLog("contentOrder-1", status.toString() + "--" + contentId)
            getContentOrderStatus(this, contentId)
            if (status == Constant.CONTENT_ORDER_STATUS_NA) {

            } else if (status == Constant.CONTENT_ORDER_STATUS_FAIL) {

            } else if (status == Constant.CONTENT_ORDER_STATUS_IN_PROCESS || status == Constant.CONTENT_ORDER_STATUS_PENDING) {

            } else if (status == Constant.CONTENT_ORDER_STATUS_SUCCESS) {

            }
        }
    }

    override fun onDownloadVideoQueueItemChanged(
        downloadManager: DownloadManager,
        download: Download
    ) {

    }

    override fun onDownloadProgress(
        downloads: List<Download?>?,
        progress: Int,
        currentExoDownloadPosition: Int
    ) {

    }

    override fun onDownloadsPausedChanged(
        downloadManager: DownloadManager,
        downloadsPaused: Boolean?
    ) {
        if (downloadsPaused!!){
            setLog("VideoDownloadLog:9", downloadsPaused.toString())
        }else{
            setLog("VideoDownloadLog:10", downloadsPaused.toString())
        }
    }

    override fun onUserContentOrderStatusCheck(status: Int) {

    }

    override fun onContentLikedFromThreeDotMenu(isFavorite: Boolean, position: Int) {
        super.onContentLikedFromThreeDotMenu(isFavorite, position)
        if (!isFavorite){
            musicvideoList?.remove(musicvideoList?.get(position))
            videoWatchlistAdapter?.notifyItemRemoved(position)
        }
    }

    override fun onContentDownloadFromThreeDotMenu(position: Int) {
        super.onContentDownloadFromThreeDotMenu(position)
        /**
         * add code for download movie
         */
        if (!musicvideoList.isNullOrEmpty() && musicvideoList.size > position){
            startContentDownload(musicvideoList?.get(position))
        }
    }

    private fun setBottomSpace(){
        val marginBottom = rvMusicVideo.paddingBottom - resources.getDimensionPixelSize(R.dimen.dimen_10)
        if (marginBottom >= 0){
            Utils.setMarginsBottom(clBottomView, marginBottom)
        }
    }
}
