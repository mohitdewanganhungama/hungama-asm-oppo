package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.HungamaMusicApp
import com.hungama.music.data.model.*
import com.hungama.music.data.webservice.WSConstants
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.AddedToPlaylistEvent
import com.hungama.music.player.audioplayer.Injection
import com.hungama.music.player.audioplayer.TracksContract
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.viewmodel.TracksViewModel
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.PlaylistAddSongSearchAdapter
import com.hungama.music.ui.main.adapter.RecommendedSongsAdapter
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.viewmodel.PlayableContentViewModel
import com.hungama.music.ui.main.viewmodel.PlaylistViewModel
import com.hungama.music.ui.main.viewmodel.SearchViewModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import kotlinx.android.synthetic.main.fr_add_song_playlist.*
import kotlinx.android.synthetic.main.fragment_more_bucket_list.*
import kotlinx.coroutines.launch
import org.json.JSONObject


/**
 * A simple [Fragment] subclass.
 * Use the [AddSongInPlayListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddSongInPlayListFragment : BaseFragment(), TracksContract.View,
    ViewTreeObserver.OnScrollChangedListener, BaseActivity.OnLocalBroadcastEventCallBack,
    TextWatcher, TextView.OnEditorActionListener {
    var selectedContentId: String? = null
    var playerType: String? = null
    var playlistName = ""
    var addSongItemPosition = 0
    var playableItemPosition = 0

    var isLastPage: Boolean = false
    var isLoading: Boolean = false
    var page = 1
    var limit = 10
    var addSongCount =0
    val arrayName :ArrayList<String> = ArrayList()

    var recommendedSongList = java.util.ArrayList<RecommendedSongListRespModel.Data.Body.Similar>()
    var filterSongList = java.util.ArrayList<RecommendedSongListRespModel.Data.Body.Similar>()

    var playableContentViewModel: PlayableContentViewModel = PlayableContentViewModel()
    var playlistListViewModel: PlaylistViewModel? = null

    private lateinit var tracksViewModel: TracksContract.Presenter


    private var recommendedSongsAdapter: RecommendedSongsAdapter? = null
    var searchViewModel:SearchViewModel? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fr_add_song_playlist, container, false)
    }

    override fun initializeComponent(view: View) {
        if (arguments != null){
            if (requireArguments().containsKey("playlistName")){
                playlistName = requireArguments().getString("playlistName").toString()
            }
            selectedContentId = requireArguments().getString("id").toString()
            playerType = requireArguments().getString("playerType").toString()
        }

        arrayName.add("")
        arrayName.add("")
        arrayName.add("")

        tvActionBarHeading.text = getString(R.string.playlist_str_6)

        tracksViewModel = TracksViewModel(Injection.provideTrackRepository(), this)
        setUpPlaylistDetailListViewModel()
        CommonUtils.setPageBottomSpacing(rlMain, requireContext(),
            resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
            resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
        et_Search?.addTextChangedListener(this)
        et_Search?.setOnEditorActionListener(this)
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

    private fun setUpPlaylistDetailListViewModel() {
        playlistListViewModel = ViewModelProvider(
            this
        ).get(PlaylistViewModel::class.java)


        callRecommendedSongAPI(page)


        et_Search?.doOnTextChanged { text, start, before, count ->
            baseMainScope.launch {
                if (isAdded && context != null){
                    try {
                        if (!TextUtils.isEmpty(text?.toString())){
                            val filterList: java.util.ArrayList<RecommendedSongListRespModel.Data.Body.Similar> =
                                performFiltering(text?.toString()!!)
                            if (recommendedSongsAdapter != null) {
                                rvRecomandedSong?.show()
                                rvTab?.hide()
                                recommendedSongsAdapter?.refreshList(filterList)
                            }
                        }
                    }catch (e:Exception){

                    }
                }
            }
        }

    }

    var recommendedListViewModel: PlaylistViewModel? = null
    fun getRecommendedContentList(id1:String, id2:String, id3:String) {
        if (ConnectionUtil(requireContext()).isOnline) {
            recommendedListViewModel = ViewModelProvider(this)[PlaylistViewModel::class.java]
            recommendedListViewModel?.getRecommendedContentListMyPlayList(requireContext(), id1,id2,id3)?.observe(this)
            {
                when (it.status) {
                    Status.SUCCESS -> {
                        if (it?.data != null) {
                            if (recommendedSongList != null)
                                recommendedSongList!!.clear()

                            for (item in it.data.data.body.rows) {

                                val misc = RecommendedSongListRespModel.Data.Body.Similar.Data.Misc()
                                misc.artist = item.data.misc.artist
                                misc.actorf = item.data.misc.actorf
                                misc.singerf = item.data.misc.singerf
                                misc.attributeCensorRating = item.data.misc.attributeCensorRating
//                                    misc.cast = item.data.misc.ca
//                                    misc.countEraFrom = item.data.misc.cou
//                                    misc.countEraTo = item.data.misc.cou
                                misc.description = item.data.misc.description
//                                    misc.dl = item.data.misc.dl
                                misc.explicit = item.data.misc.explicit
                                misc.favCount = item.data.misc.favCount
                                misc.lang = item.data.misc.lang
                                misc.musicdirectorf = item.data.misc.musicdirectorf
                                misc.lyricist = item.data.misc.lyricistf
                                misc.mood = item.data.misc.mood
                                misc.movierights = item.data.misc.movierights
                                misc.nudity = item.data.misc.nudity
                                misc.pid = item.data.misc.pid
                                misc.pName = item.data.misc.p_name
                                misc.tempo = item.data.misc.tempo
                                misc.playcount = item.data.misc.playcount
                                misc.ratingCritic = item.data.misc.ratingCritic.toInt()
                                misc.sArtist = item.data.misc.sArtist
//                                    misc.skipIntro = item.data.misc.sk
//                                    misc.sl = item.data.misc.sl
                                misc.synopsis = item.data.misc.synopsis
//                                    misc.url = item.data.misc.u
                                misc.vendor = item.data.misc.vendor
                                misc.restricted_download = item.data.misc.restricted_download

                                val data = RecommendedSongListRespModel.Data.Body.Similar.Data()
                                data.duration = item.data.duration
                                data.genre = item.data.genre
                                data.id = item.data.id
                                data.image = item.data.image
                                data.playble_image = item.data.playble_image
                                data.misc = misc
                                data.releasedate = item.data.releasedate
                                data.subtitle = item.data.subtitle
                                data.title = item.data.title
                                data.type = item.data.type

                                val similar = RecommendedSongListRespModel.Data.Body.Similar()
                                similar.data = data
                                similar.itype = item.itype

                                recommendedSongList.add(similar)

                            }
                            recommendedSongsAdapter?.refreshList(recommendedSongList)
                            recommendedSongsAdapter?.notifyDataSetChanged()

                        }
                    }

                    Status.LOADING -> {

                    }

                    Status.ERROR -> {
                        setEmptyVisible(false)
                        setProgressBarVisible(false)
                        val messageModel = MessageModel(getString(R.string.toast_str_35), it.message!!, MessageType.NEGATIVE, true)
                        CommonUtils.showToast(requireContext(), messageModel)
                    }
                }
            }
        }
    }

    private fun callRecommendedSongAPI(page: Int) {
        baseMainScope.launch {
            try {
                if (isAdded && context != null){
                    if (ConnectionUtil(context).isOnline) {
                        playlistListViewModel?.getRecommendedSongList(requireContext(), ""+page)?.observe(this@AddSongInPlayListFragment,
                            Observer {
                                when(it.status){
                                    Status.SUCCESS->{
                                        baseMainScope.launch {
                                            try {
                                                setProgressBarVisible(false)
                                                if (it?.data != null) {

                                                    if (it?.data?.data?.body?.similar != null && it?.data?.data?.body?.similar?.size!! > 0) {
                                                        rvRecomandedSong.visibility = View.VISIBLE
//                                    recommendedSongList?.addAll(it?.data?.data?.body?.similar)


                                                        setRecommendedAdapter(it.data.data.body.similar)

                                                    }else{
                                                        isLastPage=true
                                                    }
                                                }
                                            }catch (e:Exception){

                                            }
                                        }
                                    }

                                    Status.LOADING ->{
                                        setProgressBarVisible(true)
                                    }

                                    Status.ERROR ->{
                                        setEmptyVisible(false)
                                        setProgressBarVisible(false)
                                        val messageModel = MessageModel(getString(R.string.toast_str_35), it.message!!, MessageType.NEGATIVE, true)
                                        CommonUtils.showToast(requireContext(), messageModel)
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
            }catch (e:Exception){

            }
        }
    }

    private fun setRecommendedAdapter(songList: java.util.ArrayList<RecommendedSongListRespModel.Data.Body.Similar>) {
        baseMainScope.launch {
            try {
                if (isAdded && context != null){
                    if(recommendedSongsAdapter==null){
                        recommendedSongList=songList
                        setLog("setRecommendedAdapter","setRecommendedAdapter 1 size:${recommendedSongList?.size}")
                        recommendedSongsAdapter =
                            RecommendedSongsAdapter(requireContext(), recommendedSongList,
                                object : RecommendedSongsAdapter.OnChildItemClick {
                                    override fun onPlaySongClick(childPosition: Int) {
                                        playableItemPosition = childPosition
                                        setUpPlayableContentListViewModel(
                                            recommendedSongList?.get(
                                                childPosition
                                            )?.data?.id!!
                                        )
                                        setEventModelDataAppLevel(recommendedSongList?.get(
                                            childPosition
                                        )?.data?.id!!,recommendedSongList?.get(
                                            childPosition
                                        )?.data?.title!!,selectedContentId!!)
                                    }

                                    override fun onAddSongClick(childPosition: Int) {
                                        if (SharedPrefHelper.getInstance().isUserLoggedIn()) {
                                            baseMainScope.launch {
                                                try {
                                                    if (isAdded && context != null) {
                                                        addSongItemPosition = childPosition
                                                        if (!recommendedSongList.isNullOrEmpty() && recommendedSongList.size > addSongItemPosition) {
                                                            callAddSong(
                                                                recommendedSongList.get(
                                                                    addSongItemPosition
                                                                ).data.id, false
                                                            )
                                                        }
                                                        addSongCount += 1
                                                        arrayName.add(
                                                            0,
                                                            recommendedSongList[addSongItemPosition].data.id
                                                        )
                                                        arrayName.removeLast()
                                                        if (addSongCount >= 3) {
                                                            getRecommendedContentList(
                                                                arrayName[arrayName.size - 3],
                                                                arrayName[arrayName.size - 2],
                                                                arrayName[arrayName.size - 1]
                                                            )
                                                            WSConstants.IS_SEARCH_RECOMMENDED_DISPLAY =
                                                                false
                                                        }
                                                    }
                                                } catch (e: Exception) {

                                                }
                                            }
                                        }
                                    }
                                })

//        val layoutMmgr = GridLayoutManager(context, 1, GridLayoutManager.VERTICAL, false)
                        val layoutMmgr = LinearLayoutManager(requireContext())
                        rvRecomandedSong.apply {
                            layoutManager =
                                layoutMmgr
                            adapter = recommendedSongsAdapter
                            setRecycledViewPool(RecyclerView.RecycledViewPool())
                            setHasFixedSize(true)
                        }
                        rvRecomandedSong.setPadding(0, 0, 0, 0)

                        if(WSConstants.IS_SEARCH_RECOMMENDED_DISPLAY){
                            rvRecomandedSong?.addOnScrollListener(object : PaginationScrollListener(layoutMmgr) {
                                override fun isLastPage(): Boolean {
                                    return isLastPage
                                }

                                override fun isLoading(): Boolean {
                                    return isLoading
                                }

                                override fun loadMoreItems() {
                                    isLoading = true
                                    //you have to call loadmore items to get more data
                                    page++
                                    callRecommendedSongAPI(page)

                                }
                            })
                        }

                    }else{
                        setLog("setRecommendedAdapter","setRecommendedAdapter 2 size:${songList?.size}")

                        val size = songList.size
                        recommendedSongList.addAll(songList)
                        val sizeNew = recommendedSongList.size
                        recommendedSongsAdapter?.notifyItemRangeChanged(size, sizeNew)
                    }

                    isLoading = false
                }
            }catch (e:Exception){

            }
        }
    }


    private fun callAddSong(contentId:String, isSearchedSong:Boolean) {
        baseMainScope.launch {
            try {
                if (isAdded && context != null){
                    val jsonObject = JSONObject()
                    jsonObject.put("contentid", contentId)
                    playlistListViewModel?.addSong(requireContext(), jsonObject, selectedContentId!!)?.observe(this@AddSongInPlayListFragment,
                        Observer {
                            when(it.status){
                                Status.SUCCESS->{
                                    setProgressBarVisible(false)
                                    if (it?.data != null) {
                                        if (isSearchedSong){
                                            if (addSongItemPosition < rowList.size) {
                                                baseMainScope.launch {
                                                    try {
                                                        if (!rowList.isNullOrEmpty() && rowList.size > addSongItemPosition){
                                                            val hashMap = HashMap<String,String>()
                                                            val model = HungamaMusicApp.getInstance().getEventData(rowList?.get(addSongItemPosition)?.data?.id.toString())
                                                            hashMap.put(EventConstant.ACTOR_EPROPERTY,Utils.arrayToString(rowList?.get(addSongItemPosition)?.data?.misc?.actorf))
                                                            var newContentId=rowList?.get(addSongItemPosition)?.data?.id.toString()
                                                            var contentIdData=newContentId.replace("playlist-","")
                                                            hashMap.put(EventConstant.CONTENTID_EPROPERTY,contentIdData)
                                                            hashMap.put(EventConstant.CONTENTNAME_EPROPERTY,rowList?.get(addSongItemPosition)?.data?.title.toString())
                                                            if (rowList?.get(addSongItemPosition)?.data?.type.toString().equals("21")){
                                                                hashMap.put(EventConstant.CONTENTTYPE_EPROPERTY,"Song")
                                                            }else{
                                                                hashMap.put(EventConstant.CONTENTTYPE_EPROPERTY,""+Utils.getContentTypeName(""+rowList?.get(addSongItemPosition)?.data?.type))
                                                            }

                                                            hashMap.put(EventConstant.GENRE_EPROPERTY,Utils.arrayToString(rowList?.get(addSongItemPosition)?.data?.genre))
                                                            hashMap.put(EventConstant.LYRICIST_EPROPERTY,Utils.arrayToString(rowList?.get(addSongItemPosition)?.data?.misc?.lyricist))
                                                            hashMap.put(EventConstant.MOOD_EPROPERTY,""+rowList?.get(addSongItemPosition)?.data?.misc?.mood)
                                                            if(rowList?.get(addSongItemPosition)?.data?.misc?.musicdirectorf!=null&& rowList.get(addSongItemPosition).data?.misc?.musicdirectorf?.size!!>0){
                                                                hashMap.put(EventConstant.MUSICDIRECTOR_EPROPERTY,Utils.arrayToString(rowList.get(addSongItemPosition).data?.misc?.musicdirectorf))
                                                            }else{
                                                                if(!TextUtils.isEmpty(model?.musicDirectorComposer)){
                                                                    hashMap.put(EventConstant.MUSICDIRECTOR_EPROPERTY,""+model?.musicDirectorComposer)
                                                                }

                                                            }

                                                            hashMap.put(EventConstant.PLAYLISTNAME_EPROPERTY,""+playlistName)
                                                            hashMap.put(EventConstant.PODCASTNAME_EPROPERTY,"")
                                                            hashMap.put(EventConstant.PODCASTHOST_EPROPERTY,""+model?.podcast_host)
                                                            hashMap.put(EventConstant.SINGER_EPROPERTY,Utils.arrayToString(rowList?.get(addSongItemPosition)?.data?.misc?.singerf))

                                                            if(rowList?.get(addSongItemPosition)?.data?.misc?.tempo!=null&&rowList.get(addSongItemPosition).data?.misc?.tempo?.size!!>0){
                                                                hashMap.put(EventConstant.TEMPO_EPROPERTY,Utils.arrayToString(rowList.get(addSongItemPosition).data?.misc?.tempo))
                                                            }else{
                                                                if(!TextUtils.isEmpty(model?.tempo)){
                                                                    hashMap.put(EventConstant.TEMPO_EPROPERTY,""+model?.tempo)
                                                                }

                                                            }

                                                            if(rowList?.get(addSongItemPosition)?.data?.releasedate!=null&&!TextUtils.isEmpty(rowList?.get(addSongItemPosition)?.data?.releasedate)){
                                                                hashMap.put(EventConstant.YEAROFRELEASE_EPROPERTY,""+DateUtils.convertDate(DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS,DateUtils.DATE_YYYY,rowList.get(addSongItemPosition).data?.releasedate))
                                                            }else{
                                                                if(!TextUtils.isEmpty(model?.release_Date)){
                                                                    hashMap.put(EventConstant.YEAROFRELEASE_EPROPERTY,""+model?.release_Date)
                                                                }

                                                            }
                                                            EventManager.getInstance().sendEvent(AddedToPlaylistEvent(hashMap))
                                                        }
                                                    }catch (e:Exception){

                                                    }
                                                }

                                                baseMainScope.launch {
                                                    try {
                                                        if(rowList!=null&&rowList.size>0){
                                                            val item=rowList?.get(addSongItemPosition)
                                                            rowList?.remove(item)
                                                        }else{
                                                            rowList?.removeAt(addSongItemPosition)
                                                        }


                                                        rvTab?.adapter?.notifyItemRemoved(addSongItemPosition)
                                                        val messageModel = MessageModel(getString(R.string.toast_str_42), getString(R.string.toast_str_43),
                                                            MessageType.NEUTRAL, true)
                                                        CommonUtils.showToast(requireContext(), messageModel)
                                                    }catch (e:Exception){

                                                    }
                                                }
                                            }
                                        }else{
                                            if (addSongItemPosition < recommendedSongList.size) {

                                                baseMainScope.launch {
                                                    try {
                                                        if (!recommendedSongList.isNullOrEmpty() && recommendedSongList.size > addSongItemPosition){
                                                            val hashMap = HashMap<String,String>()
                                                            val model = HungamaMusicApp.getInstance().getEventData(recommendedSongList.get(addSongItemPosition).data.id)

                                                            hashMap.put(EventConstant.ACTOR_EPROPERTY,Utils.arrayToString(recommendedSongList?.get(addSongItemPosition)?.data?.misc?.actorf))
                                                            val newContentId=recommendedSongList.get(addSongItemPosition).data.id
                                                            val contentIdData=newContentId.replace("playlist-","")

                                                            hashMap.put(EventConstant.CONTENTID_EPROPERTY,contentIdData)
                                                            hashMap.put(EventConstant.CONTENTNAME_EPROPERTY,recommendedSongList.get(addSongItemPosition).data.title)
                                                            if (recommendedSongList?.get(addSongItemPosition)?.data?.type.toString().equals("21")){
                                                                hashMap.put(EventConstant.CONTENTTYPE_EPROPERTY,"Song")
                                                            }else{
                                                                hashMap.put(EventConstant.CONTENTTYPE_EPROPERTY,""+Utils.getContentTypeName(""+recommendedSongList?.get(addSongItemPosition)?.data?.type))
                                                            }

                                                            hashMap.put(EventConstant.GENRE_EPROPERTY,Utils.arrayToString(recommendedSongList?.get(addSongItemPosition)?.data?.genre))
                                                            hashMap.put(EventConstant.LANGUAGE_EPROPERTY,Utils.arrayToString(recommendedSongList?.get(addSongItemPosition)?.data?.misc?.lang))
                                                            hashMap.put(EventConstant.LYRICIST_EPROPERTY,Utils.arrayToString(recommendedSongList?.get(addSongItemPosition)?.data?.misc?.lyricist))
                                                            hashMap.put(EventConstant.MOOD_EPROPERTY,""+recommendedSongList?.get(addSongItemPosition)?.data?.misc?.mood)
                                                            if(recommendedSongList?.get(addSongItemPosition)?.data?.misc?.musicdirectorf!=null&&recommendedSongList.get(addSongItemPosition).data.misc.musicdirectorf.size>0){
                                                                hashMap.put(EventConstant.MUSICDIRECTOR_EPROPERTY,Utils.arrayToString(recommendedSongList.get(addSongItemPosition).data?.misc?.musicdirectorf))
                                                            }else if(model!=null){
                                                                if(!TextUtils.isEmpty(model?.musicDirectorComposer)){
                                                                    hashMap.put(EventConstant.MUSICDIRECTOR_EPROPERTY,""+model?.musicDirectorComposer)
                                                                }
                                                                hashMap.put(EventConstant.PODCASTHOST_EPROPERTY,""+model?.podcast_host)
                                                            }

                                                            hashMap.put(EventConstant.PLAYLISTNAME_EPROPERTY,""+playlistName)
                                                            hashMap.put(EventConstant.PODCASTNAME_EPROPERTY,"")

                                                            hashMap.put(EventConstant.SINGER_EPROPERTY,Utils.arrayToString(recommendedSongList?.get(addSongItemPosition)?.data?.misc?.singerf))

                                                            if(recommendedSongList?.get(addSongItemPosition)?.data?.misc?.tempo!=null&&recommendedSongList.get(addSongItemPosition).data.misc.tempo.size>0){
                                                                hashMap.put(EventConstant.TEMPO_EPROPERTY,Utils.arrayToString(recommendedSongList.get(addSongItemPosition).data?.misc?.tempo))
                                                            }else{
                                                                if(!TextUtils.isEmpty(model?.tempo)){
                                                                    hashMap.put(EventConstant.TEMPO_EPROPERTY,""+model?.tempo)
                                                                }

                                                            }

                                                            if(recommendedSongList?.get(addSongItemPosition)?.data?.releasedate!=null&&!TextUtils.isEmpty(recommendedSongList.get(addSongItemPosition).data?.releasedate)){
                                                                hashMap.put(EventConstant.YEAROFRELEASE_EPROPERTY,""+DateUtils.convertDate(DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS,DateUtils.DATE_YYYY,recommendedSongList.get(addSongItemPosition).data?.releasedate))
                                                            }else{
                                                                if(!TextUtils.isEmpty(model?.release_Date)){
                                                                    hashMap.put(EventConstant.YEAROFRELEASE_EPROPERTY,""+model?.release_Date)
                                                                }

                                                            }
                                                            EventManager.getInstance().sendEvent(AddedToPlaylistEvent(hashMap))
                                                        }
                                                    }catch (e:Exception){

                                                    }
                                                }
                                            }

                                            baseMainScope.launch {
                                                try {
                                                    if(!filterSongList.isNullOrEmpty() && filterSongList.size > addSongItemPosition){
                                                        val item=filterSongList?.get(addSongItemPosition)
                                                        filterSongList?.removeAt(addSongItemPosition)
                                                        recommendedSongList?.remove(item)
                                                    }else{
                                                        recommendedSongList?.removeAt(addSongItemPosition)
                                                    }
                                                    recommendedSongsAdapter?.notifyItemRemoved(addSongItemPosition)
                                                    val messageModel = MessageModel(getString(R.string.toast_str_42), getString(R.string.toast_str_43),
                                                        MessageType.NEUTRAL, true)
                                                    CommonUtils.showToast(requireContext(), messageModel)
                                                }catch (e:Exception){

                                                }
                                            }

                                        }
                                    }
                                }



                                Status.LOADING ->{
                                    setProgressBarVisible(true)
                                }

                                Status.ERROR ->{
                                    setEmptyVisible(false)
                                    setProgressBarVisible(false)
                                    val messageModel = MessageModel(it.message.toString(),
                                        MessageType.NEGATIVE, true)
                                    CommonUtils.showToast(requireContext(), messageModel)
                                }
                            }
                        })
                }
            }catch (e:Exception){

            }
        }
    }

    private fun setUpPlayableContentListViewModel(id: String) {
        playableContentViewModel = ViewModelProvider(
            this
        ).get(PlayableContentViewModel::class.java)


        if (ConnectionUtil(context).isOnline) {
            playableContentViewModel?.getPlayableContentList(requireContext(), id)?.observe(this,
                Observer {
                    when(it.status){
                        com.hungama.music.data.webservice.utils.Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                if (!TextUtils.isEmpty(it?.data?.data?.head?.headData?.misc?.url)) {
                                    setPlayableContentListData(it?.data)
                                } else {
                                    playableItemPosition = MyPlaylistDetailFragment.playableItemPosition + 1
                                    if (playableItemPosition < recommendedSongList?.size!!) {
                                        setUpPlayableContentListViewModel(
                                            recommendedSongList?.get(
                                                playableItemPosition
                                            )?.data?.id!!
                                        )
                                        setEventModelDataAppLevel(recommendedSongList?.get(
                                            playableItemPosition
                                        )?.data?.id!!,recommendedSongList?.get(
                                            playableItemPosition
                                        )?.data?.title!!,selectedContentId!!)
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
                            val messageModel = MessageModel(getString(R.string.toast_str_35), it.message!!,
                                MessageType.NEGATIVE, true)
                            CommonUtils.showToast(requireContext(), messageModel)
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
            songDataList = arrayListOf()

            for (i in recommendedSongList?.indices!!) {
                if (playableContentModel?.data?.head?.headData?.id == recommendedSongList?.get(i)?.data?.id) {
                    setRecomendedSongList(
                        playableContentModel,
                        recommendedSongList,
                        MyPlaylistDetailFragment.playableItemPosition
                    )
                } else if (i > MyPlaylistDetailFragment.playableItemPosition) {
                    setRecomendedSongList(null, recommendedSongList, i)
                }
            }


            BaseActivity.setTrackListData(songDataList)
            tracksViewModel.prepareTrackPlayback(0)


        }
    }

    var songDataList: ArrayList<Track> = arrayListOf()


    fun setRecomendedSongList(
        playableContentModel: PlayableContentModel?,
        playableItem: java.util.ArrayList<RecommendedSongListRespModel.Data.Body.Similar>,
        position: Int
    ): ArrayList<Track> {
        val track: Track = Track()
        if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.id)) {
            track.id = playableItem?.get(position)?.data?.id!!.toLong()
        } else {
            track.id = 0
        }
        if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.title)) {
            track.title = playableItem?.get(position)?.data?.title
        } else {
            track.title = ""
        }

        if (!TextUtils.isEmpty(MyPlaylistDetailFragment.playlistRespModel?.data?.head?.data?.title)) {
            track.subTitle = MyPlaylistDetailFragment.playlistRespModel?.data?.head?.data?.title
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

        if (!TextUtils.isEmpty(playerType)) {
            track.playerType = playerType
        } else {
            track.playerType = Constant.MUSIC_PLAYER
        }
        if (!TextUtils.isEmpty(MyPlaylistDetailFragment.playlistRespModel?.data?.head?.data?.title)) {
            track.heading = MyPlaylistDetailFragment.playlistRespModel?.data?.head?.data?.title
        } else {
            track.heading = ""
        }
        if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.playble_image)) {
            track.image = playableItem?.get(position)?.data?.playble_image
        }else if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.image)) {
            track.image = playableItem?.get(position)?.data?.image
        } else {
            track.image = ""
        }
        if (!playableContentModel?.data?.head?.headData?.misc?.movierights.isNullOrEmpty()){
            track.movierights = playableContentModel?.data?.head?.headData?.misc?.movierights.toString()
        }else{
            track.movierights = ""
        }

        if (!TextUtils.isEmpty(MyPlaylistDetailFragment.playlistRespModel?.data?.head?.data?.id!!)) {
            track.parentId = MyPlaylistDetailFragment.playlistRespModel?.data?.head?.data?.id!!
        }
        if (!TextUtils.isEmpty(MyPlaylistDetailFragment.playlistRespModel?.data?.head?.data?.title!!)) {
            track.pName = MyPlaylistDetailFragment.playlistRespModel?.data?.head?.data?.title
        }

        if (!TextUtils.isEmpty(MyPlaylistDetailFragment.playlistRespModel?.data?.head?.data?.subtitle!!)) {
            track.pSubName = MyPlaylistDetailFragment.playlistRespModel?.data?.head?.data?.subtitle
        }

        if (!TextUtils.isEmpty(MyPlaylistDetailFragment.playlistRespModel?.data?.head?.data?.image!!)) {
            track.pImage = MyPlaylistDetailFragment.playlistRespModel?.data?.head?.data?.image
        }

        track.pType = DetailPages.PLAYLIST_DETAIL_PAGE.value
        track.contentType = ContentTypes.AUDIO.value

        track.explicit = playableItem.get(position).data.misc.explicit
        track.restrictedDownload = playableItem.get(position).data.misc.restricted_download
        track.attributeCensorRating =
            playableItem.get(position).data.misc.attributeCensorRating.toString()
        songDataList.add(track)
        return songDataList
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

    override fun onScrollChanged() {

    }

    override fun onDestroy() {
        super.onDestroy()
        tracksViewModel.onCleanup()
    }

    protected fun performFiltering(constraint: String): java.util.ArrayList<RecommendedSongListRespModel.Data.Body.Similar> {
        filterSongList= ArrayList()
        if (constraint.length == 0 && TextUtils.isEmpty(constraint)) {
            return recommendedSongList
        } else {
            if (recommendedSongList != null && !TextUtils.isEmpty(constraint)) {
                val filterPattern = constraint.toString().toLowerCase().trim { it <= ' ' }
                for (model in recommendedSongList) {
                    if (model != null && model?.data?.title != null) {
                        if (model.data.title.lowercase().trim()
                                .contains(filterPattern)
                        ) {
                            filterSongList.add(model)
                        }
                    }
                }
            }
        }
        return filterSongList
    }

    override fun onResume() {
        super.onResume()
        setLocalBroadcast()
    }

    private fun setLocalBroadcast(){
        (requireActivity() as MainActivity).setLocalBroadcastEventCall(this, Constant.AUDIO_PLAYER_EVENT)
    }

    override fun onLocalBroadcastEventCallBack(context: Context?, intent: Intent) {
        if (isAdded){
            val event = intent.getIntExtra("EVENT", 0)
            if (event == Constant.AUDIO_PLAYER_RESULT_CODE) {
                CommonUtils.setPageBottomSpacing(rlMain, requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
            }
        }
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_SEARCH){
            callSearchApi()
            activity?.let { view?.let { it1 -> Utils.hideSoftKeyBoard(it, it1) } }
            return true
        }
        return false
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun afterTextChanged(s: Editable?) {
        if (s?.length == 0) {

        }else{
            if (!TextUtils.isEmpty(et_Search?.text.toString().trim())){

            }
        }
    }

    private fun callSearchApi(){
        if (isAdded && context != null){
            try {
                callSearchApi(et_Search?.text.toString().trim())
            }catch (e:Exception){
            }
        }
    }
    var rowList:ArrayList<BodyRowsItemsItem> = ArrayList()
    private fun callSearchApi(searchText: String) {
        baseMainScope.launch {
            if (isAdded && context != null){
                try {
                    searchViewModel = ViewModelProvider(this@AddSongInPlayListFragment).get(SearchViewModel::class.java)

                    if (ConnectionUtil(activity).isOnline) {
                        searchViewModel?.getSearchData(requireActivity(), searchText.trim())?.observe(this@AddSongInPlayListFragment,
                            Observer {
                                when (it.status) {
                                    Status.SUCCESS -> {
                                        baseMainScope.launch {
                                            if (isAdded && context != null){
                                                try {
                                                    setProgressBarVisible(false)
                                                    if (it?.data != null && it?.data?.data != null
                                                        && it?.data?.data?.body != null
                                                        && !it?.data?.data?.body?.song.isNullOrEmpty()
                                                    ) {

                                                        rvRecomandedSong?.hide()
                                                        rvTab?.show()
                                                        rowList?.addAll(it?.data?.data?.body?.song!!)
                                                        val playlistAddSongSearchAdapter =
                                                            PlaylistAddSongSearchAdapter(requireContext(), rowList,
                                                                object : PlaylistAddSongSearchAdapter.OnChildItemClick {
                                                                    override fun onPlaySongClick(childPosition: Int) {

                                                                    }

                                                                    override fun onAddSongClick(childPosition: Int) {
                                                                        baseMainScope.launch {
                                                                            if (isAdded && context != null){
                                                                                try {
                                                                                    if (!rowList.isNullOrEmpty() && rowList.size > addSongItemPosition){
                                                                                        addSongItemPosition = childPosition
                                                                                        callAddSong(rowList?.get(addSongItemPosition)?.data?.id.toString(), true)
                                                                                    }
                                                                                }catch (e:Exception){

                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                })

                                                        rvTab.apply {
                                                            layoutManager =
                                                                GridLayoutManager(
                                                                    context,
                                                                    1,
                                                                    GridLayoutManager.VERTICAL,
                                                                    false
                                                                )
                                                            adapter = playlistAddSongSearchAdapter
                                                            setRecycledViewPool(RecyclerView.RecycledViewPool())
                                                            setHasFixedSize(true)
                                                        }
                                                        rvTab.setPadding(0, 0, 0, 0)
                                                    }else{
                                                        val messageModel = MessageModel(
                                                            "No search result found", "No search result found",
                                                            MessageType.NEUTRAL, true
                                                        )
                                                        CommonUtils.showToast(requireContext(), messageModel)
                                                    }
                                                }catch (e:Exception){

                                                }
                                            }
                                        }
                                    }

                                    Status.LOADING -> {
                                        setProgressBarVisible(true)
                                    }

                                    Status.ERROR -> {
                                        setEmptyVisible(false)
                                        setProgressBarVisible(false)
//                            Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                                    }
                                }
                            })
                    } else {
                        val messageModel = MessageModel(
                            getString(R.string.toast_message_5), getString(R.string.toast_message_5),
                            MessageType.NEGATIVE, true
                        )
                        CommonUtils.showToast(requireContext(), messageModel)
                    }
                }catch (e:Exception){

                }
            }
        }
    }
}