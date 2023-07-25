package com.hungama.music.ui.main.view.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.utils.customview.downloadmanager.model.DownloadedAudio
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.data.database.oldappdata.DBOHandler
import com.hungama.music.data.model.*
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.home.eventreporter.UserAttributeEvent
import com.hungama.music.ui.main.adapter.UserProfilePlaylistsAdapter
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.utils.ConnectionUtil
import com.hungama.music.utils.Constant
import com.hungama.music.utils.Utils
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import kotlinx.android.synthetic.main.fr_library_playlist.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.stream.Collectors


class PlaylistFragment : BaseFragment(), CreatePlaylistDialog.createPlayListListener,
    BaseActivity.OnLocalBroadcastEventCallBack {

    private val CREATE_PLAYLIST_ID = "5001"
    var userViewModel: UserViewModel? = null
    var userPlaylist: ArrayList<PlaylistRespModel.Data> = ArrayList()
    var defaultPlaylistName = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fr_library_playlist, container, false)
    }


    override fun onResume() {
        super.onResume()
        setUpViewModel()
        setLocalBroadcast()
        setLog(TAG, "onResume: work")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        setLog(TAG, "onDestroyView: work")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLog(TAG, "onViewCreated: work")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setLog(TAG, "onAttach: work")
    }

    override fun onStart() {
        super.onStart()
        setLog(TAG, "onStart: work")
    }


    override fun initializeComponent(view: View) {
        defaultPlaylistName = getString(R.string.hungama_playlist) + " 1"
        createPlaylist?.setOnClickListener(this)
        CommonUtils.applyButtonTheme(requireContext(), btnExplore)
        btnExplore?.setOnClickListener(this)
        CommonUtils.setPageBottomSpacing(rvMusicPlaylist, requireContext(), resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_100), resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
    }

    override fun onClick(v: View) {
        super.onClick(v)
        if (v == createPlaylist) {
            if (SharedPrefHelper.getInstance().isUserLoggedIn()) {
                val createPlaylistDialog =
                    CreatePlaylistDialog(this@PlaylistFragment, defaultPlaylistName)
                createPlaylistDialog.show(
                    activity?.supportFragmentManager!!,
                    "open createplaylist dialog"
                )
                CommonUtils.PageViewEvent(
                    "", "", "", "",
                    MainActivity.lastItemClicked + "_" + MainActivity.headerItemName + "_Playlist",
                    "popup_create playlist", ""
                )
            }
            } else if (v == btnExplore) {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), btnExplore!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            (activity as MainActivity).applyScreen(1)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return true
    }


    override fun playListCreatedSuccessfull(id: String) {
        var varient = 1

        val playlistDetailFragment = MyPlaylistDetailFragment(varient,object :MyPlaylistDetailFragment.onBackPreesHendel{
            override fun backPressItem(status: Boolean) {
                setUpViewModel()
            }

        })
        val bundle = Bundle()
        bundle.putString("id", id)
        bundle.putString("playerType", Constant.MUSIC_PLAYER)

        playlistDetailFragment.arguments = bundle
        setLocalBroadcast()
        addFragment(R.id.fl_container, this@PlaylistFragment, playlistDetailFragment, false)
        setUpViewModel()
    }

    private fun setUpViewModel() {
        userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)
        getUserPlaylistData()
    }

    var favPlayList = ArrayList<BookmarkDataModel.Data.Body.Row>()


    private fun getUserPlaylistData() {
        baseMainScope.launch {
            setLog("isGotoDownloadClicked", "PlaylistFragment-getUserPlaylistData-isGotoDownloadClicked-${Constant.ISGOTODOWNLOADCLICKED}")
            if (ConnectionUtil(requireContext()).isOnline) {
                userViewModel?.getUserBookmarkedData(
                    requireContext(),
                    Constant.MODULE_FAVORITE,
                    "55555,99999"
                )?.observe(this@PlaylistFragment,
                    Observer {
                        when (it.status) {
                            Status.SUCCESS -> {
                                if (it?.data != null && it?.data?.data?.body != null && it?.data.data?.body?.rows!!.size > 0) {
                                    favPlayList = it?.data?.data?.body?.rows!!
                                }
                                setProgressBarVisible(false)
                                callUserPlaylistAPI()
                            }

                            Status.LOADING -> {
                                setProgressBarVisible(true)
                            }

                            Status.ERROR -> {
                                setEmptyVisible(false)
                                setProgressBarVisible(false)
                                Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                                callUserPlaylistAPI()
                            }
                        }
                    })

            }
            else {
                setEmptyVisible(false)
                setProgressBarVisible(false)
                val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                    MessageType.NEGATIVE, true)
                CommonUtils.showToast(requireContext(), messageModel)

                val userPlaylistData = PlaylistRespModel()

                if(SharedPrefHelper.getInstance().getPlayList("PlayList") != null && SharedPrefHelper.getInstance().getPlayList("PlayList")!!.isNotEmpty()){
                    val libraryAllModel = SharedPrefHelper.getInstance().getPlayList("PlayList")!!


                    for (item in libraryAllModel){
                        val misc = PlaylistRespModel.Data.Data.Misc()

                        misc.artist = item.data.misc.artist
                        if (item.data.misc.attributeCensorRating.isNotEmpty())
                            misc.attributeCensorRating =item.data.misc.attributeCensorRating
                        misc.description = item.data.misc.description!!
                        misc.favCount = item.data.misc.favCount
                        if (item.data.misc.lang.isNotEmpty())
                            misc.lang = item.data.misc.lang
                            if (item.data.misc.movierights!!.isNotEmpty())
                                misc.movierights = item.data.misc.movierights
                        misc.playcount = item.data.misc.playcount.toString()
                        misc.ratingCritic = if (item.data.misc.ratingCritic.toString()
                                .isNotEmpty()) item.data.misc.ratingCritic.toString().toInt() else 0
                        if (item.data.misc.artist!!.isNotEmpty())
                            misc.sArtist = item.data.misc.artist
                        misc.synopsis = item.data.misc.synopsis!!
                        val data = PlaylistRespModel.Data.Data()
                        data.misc = misc
                        data.id = item.data.id
                        data.duration = item.data.duration
                        data.image = item.data.image
                        data.releasedate = item.data.releasedate
                        data.subtitle = item.data.subtitle
                        data.title = item.data.title
                        data.type = item.data.type
                        val rows = PlaylistRespModel.Data()
                        rows.data = data
                        rows.isOwnerPlaylist = true
                        rows.itype = item.itype
                        rows.modified = item.modified
                        rows.public = item.public
                        userPlaylistData.data.add(rows)
                    }
//                    setUpLists(userPlaylistData)
                }
//                else{

                val downloadedSongTotal = AppDatabase?.getInstance()?.downloadedAudio()?.getDownloadQueueItemsByContentType(ContentTypes.AUDIO.value)
                val dbDataold = DBOHandler.getAllCachedTracks(requireContext())


                Log.e("alkghlah", Gson().toJson(downloadedSongTotal) + "\n")
                Log.e("alkghlah", Gson().toJson(dbDataold))

                var count = 1
                if (downloadedSongTotal!!.isNotEmpty() && dbDataold.isNotEmpty()) {

                    for (item in downloadedSongTotal) {
                        for (i in dbDataold) {
                            if (item.contentId == i.trackId) {
                                count += 1
                                val jsonObject = JSONObject(i.jsonDetails)

                                val misc = PlaylistRespModel.Data.Data.Misc()
                                if (item.attribute_censor_rating!!.isNotEmpty())
                                    misc.attributeCensorRating = stringToWords(item.attribute_censor_rating!!) as ArrayList<String>
                                misc.description = item.description!!
                                misc.favCount = item.f_fav_count
                                if (item.language!!.isNotEmpty())
                                    if (item.movierights!!.isNotEmpty())
                                        misc.movierights = stringToWords(item.movierights.toString()) as ArrayList<String>
                                misc.playcount = item.playcount.toString()
                                misc.ratingCritic = if (item.criticRating.toString()
                                        .isNotEmpty()) item.criticRating.toString().toInt() else 0
                                if (item.artist!!.isNotEmpty())
                                misc.synopsis = item.synopsis!!

                                val data = PlaylistRespModel.Data.Data()
                                data.duration = 0
                                data.id = if (jsonObject.has("myPlaylistID")) jsonObject.getString("myPlaylistID") else ""
                                data.image = item.thumbnailPath.toString()
                                data.misc = misc
                                data.releasedate = item.releaseDate!!
                                data.subtitle = item.subTitle!!
                                data.title = if(jsonObject.has("playlistname")) jsonObject.getString("playlistname") else "Offline_playlist$count"
                                data.type = 99999
                                val rows = PlaylistRespModel.Data()
                                rows.data = data

                                var count = 0
                                if (userPlaylistData.data.size>0){

                                    for (libray in userPlaylistData.data){
                                        if(libray.data.id == data.id){
                                            count +=1
                                        }
                                    }
                                }

                                if(count == 0)
                                    userPlaylistData.data.add(rows)
                            }
                        }
                    }
                }
                val libraryAllModel1 : ArrayList<PlaylistRespModel.Data> = ArrayList()
                for (item in userPlaylistData.data){
                    if (item.data.id.isNotEmpty()){
                        libraryAllModel1.add(item)
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    userPlaylistData.data = libraryAllModel1.stream().distinct().collect(Collectors.toList()) as ArrayList<PlaylistRespModel.Data>
                }
                setLog("playlistData", Gson().toJson(userPlaylistData))
                setUpLists(userPlaylistData)
//            }
            }
        }

    }

    fun stringToWords(s : String) = s.trim().splitToSequence(' ')
        .filter { it.isNotEmpty() } // or: .filter { it.isNotBlank() }
        .toList()

    private fun callUserPlaylistAPI() {
        baseMainScope.launch {
            userViewModel?.getUserPlaylistData(
                requireContext(),
                SharedPrefHelper.getInstance().getUserId()!!
            )?.observe(this@PlaylistFragment,
                Observer {
                    when (it.status) {
                        Status.SUCCESS -> {
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                it?.data?.data?.forEach {
                                    it?.data?.cp_subtitle=SharedPrefHelper.getInstance().getHandleName()!!
                                }
                                setUpLists(it?.data)
                            }

                        }

                        Status.LOADING -> {
                            setProgressBarVisible(true)
                        }

                        Status.ERROR -> {
                            setProgressBarVisible(false)
                            Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                            setUpLists(null)
                        }
                    }
                })


        }
    }

    private fun callMYPlaylistEvent(
        arrayList: ArrayList<PlaylistRespModel.Data>,
        favPlayList: ArrayList<BookmarkDataModel.Data.Body.Row>) {
        val list = ArrayList<String>()
        val favlist = ArrayList<String>()
        val userDataMap = java.util.HashMap<String, String>()

        arrayList.forEach {
            if(!it.data.title.contains(getString(R.string.library_playlist_str_5),true)){
                list.add(it?.data?.title!!)
            }

        }

        if (favPlayList != null && favPlayList.size > 0) {
            favPlayList.forEach {
                favlist.add(it?.data?.title!!)
            }

            userDataMap.put(
                EventConstant.FAVOURITED_PLAYLIST,
                 Utils.arrayToString(favlist)
            )
            userDataMap.put(EventConstant.NUMBER_OF_FAVORITED_PLAYLISTS, "" + favlist?.size)
        }

        userDataMap.put(EventConstant.MY_AUDIO_PLAYLIST,  Utils.arrayToString(list))
        userDataMap.put(EventConstant.NUMBER_OF_USER_PLAYLISTS, "" + list?.size)

        EventManager.getInstance().sendUserAttribute(UserAttributeEvent(userDataMap))

    }

    private fun setUpLists(userPlaylistData: PlaylistRespModel?) {
        userPlaylist = ArrayList()
        addOfflinePlayList(userPlaylistData)
        if (userPlaylistData != null && !userPlaylistData?.data.isNullOrEmpty()) {
            userPlaylist.addAll(userPlaylistData?.data!!)
            callMYPlaylistEvent(userPlaylist,favPlayList)
        }
        addFavPlayList()
        if (!userPlaylist.isNullOrEmpty()){
            rvMusicPlaylist.apply {
                layoutManager = GridLayoutManager(context, 1)
                adapter = UserProfilePlaylistsAdapter(
                    context, userPlaylist,
                    object : UserProfilePlaylistsAdapter.OnChildItemClick {
                        override fun onUserClick(childPosition: Int) {

                            if (userPlaylist?.get(childPosition)?.data?.id!!.equals(
                                    CREATE_PLAYLIST_ID,
                                    true
                                )
                            ) {
                                var lastNum = 1
                                userPlaylist?.forEachIndexed { index, item ->
                                    if (item?.data?.type == 99999) {
                                        if (item?.data?.title?.contains("Hungama Playlist ") == true) {
                                            val count = item.data.title.filter { it.isDigit() }
                                            if (!TextUtils.isEmpty(count)) {
                                                try {
                                                    val num = count.toInt() + 1
                                                    if (lastNum < num) {
                                                        lastNum = num
                                                        defaultPlaylistName =
                                                            getString(R.string.hungama_playlist) + " " + lastNum
                                                    }
                                                } catch (e: Exception) {

                                                }
                                            }
                                        }
                                    }
                                }
                                if (SharedPrefHelper.getInstance().isUserLoggedIn()) {
                                    val createPlaylistDialog = CreatePlaylistDialog(
                                        this@PlaylistFragment,
                                        defaultPlaylistName
                                    )
                                    createPlaylistDialog.show(
                                        activity?.supportFragmentManager!!,
                                        "open createplaylist dialog"
                                    )
                                }
                            } else {
                                if (userPlaylist?.get(childPosition)?.isOwnerPlaylist!! || userPlaylist?.get(childPosition)?.data?.type==99999) {
                                    setLog("playlist","MyPlaylistDetailFragment called")
                                    var varient = 1
                                    val playlistDetailFragment = MyPlaylistDetailFragment(varient,object :MyPlaylistDetailFragment.onBackPreesHendel{
                                        override fun backPressItem(status: Boolean) {
                                            setUpViewModel()
                                        }

                                    })
                                    val bundle = Bundle()
                                    bundle.putString(
                                        "image",
                                        userPlaylist?.get(childPosition)?.data?.image
                                    )
                                    bundle.putString(
                                        "id",
                                        userPlaylist?.get(childPosition)?.data?.id
                                    )
                                    bundle.putString("playerType", Constant.MUSIC_PLAYER)
                                    playlistDetailFragment.arguments = bundle
                                    setLocalBroadcast()
                                    addFragment(
                                        R.id.fl_container,
                                        this@PlaylistFragment,
                                        playlistDetailFragment,
                                        false
                                    )

                                    CommonUtils.PageViewEvent("","","","",
                                        MainActivity.lastItemClicked + "_" +MainActivity.headerItemName + "_Playlist",
                                        MainActivity.lastItemClicked + "_" +MainActivity.headerItemName + "_Playlist_"+ "Add to Playlist","")

                                } else {

                                    setLog("playlist","PlaylistDetailFragmentDynamic called")

                                    val bundle = Bundle()
                                    bundle.putString(
                                        "image",
                                        userPlaylist?.get(childPosition)?.data?.image
                                    )
                                    bundle.putString(
                                        "id",
                                        userPlaylist?.get(childPosition)?.data?.id
                                    )
                                    bundle.putString("playerType", Constant.MUSIC_PLAYER)
                                    var varient = 1
                                    val playlistDetailFragment = PlaylistDetailFragmentDynamic.newInstance(varient)
                                    playlistDetailFragment.arguments = bundle

                                    addFragment(
                                        R.id.fl_container,
                                        this@PlaylistFragment,
                                        playlistDetailFragment,
                                        false
                                    )

                                }

                            }
                        }
                    }
                )
                setRecycledViewPool(RecyclerView.RecycledViewPool())
                setHasFixedSize(true)
            }
            if (userPlaylist.size > 1){
                rvMusicPlaylist.visibility = View.VISIBLE
                clExplore.visibility = View.GONE
            }else{
                rvMusicPlaylist.visibility = View.GONE
                clExplore.visibility = View.VISIBLE
            }
        }else{
            rvMusicPlaylist.visibility = View.GONE
            clExplore.visibility = View.VISIBLE
        }

    }

    private fun addOfflinePlayList(userPlaylistData: PlaylistRespModel?) {
        try {
            val createPlaylistData = PlaylistRespModel.Data()
            createPlaylistData.data.id = CREATE_PLAYLIST_ID
            createPlaylistData.data.title = getString(R.string.library_playlist_str_5)
            userPlaylist.add(0, createPlaylistData)

            val playListOffline = AppDatabase.getInstance()?.downloadedAudio()
                ?.getPlayList() as ArrayList<DownloadedAudio>
            if (!playListOffline.isNullOrEmpty()) {
                playListOffline.forEach {
                    val playlistData = PlaylistRespModel.Data()
                    playlistData.data.id = "" + it.parentId
                    playlistData.data.title = "" + it.pName
                    playlistData.public = false
                    playlistData.isOwnerPlaylist = false
                    playlistData.data.image = "" + it.parentThumbnailPath
                    playlistData.data.type = it.type!!
                    playlistData.data.subtitle = "" + it.f_fav_count
                    playlistData.data.misc.favCount = it.f_fav_count
                    userPlaylist.add(playlistData)
                }
            }

            val userPlayListOffline = AppDatabase.getInstance()?.downloadedAudio()
                ?.getUserPlayList() as ArrayList<DownloadedAudio>
            /*if (!userPlayListOffline.isNullOrEmpty()) {
                userPlayListOffline.forEach {
                    val playlistData = PlaylistRespModel.Data()
                    playlistData.data.id = "" + it.parentId
                    playlistData.data.title = "" + it.pName
                    playlistData.public = false
                    playlistData.isOwnerPlaylist = true
                    playlistData.data.image = "" + it.parentThumbnailPath
                    playlistData.data.subtitle = "" + it.fav_count
                    playlistData.data.misc.favCount = it.fav_count!!
                    userPlaylist.add(playlistData)

                }
            }*/

            if (!userPlayListOffline.isNullOrEmpty()) {
                userPlayListOffline.forEach {
                    var isUserPlaylistAvailable = false
                    if (userPlaylistData != null && !userPlaylistData.data.isNullOrEmpty()) {
                        for (item in userPlaylistData.data){
                            if (item.data.id.equals(it.parentId)){
                                isUserPlaylistAvailable = true
                            }
                        }
                    }
                    if (!isUserPlaylistAvailable){
                        setLog(TAG, "firstTimeAddData: $it")
                        val playlistData = PlaylistRespModel.Data()
                        playlistData.data.id = "" + it.parentId
                        playlistData.data.title = "" + it.pName
                        playlistData.public = false
                        playlistData.isOwnerPlaylist = true
                        playlistData.data.type = 99999
                        playlistData.data.image = "" + it.parentThumbnailPath
                        playlistData.data.subtitle = "" + it.f_fav_count
                        playlistData.data.misc.favCount = it.f_fav_count!!
                        userPlaylist.add(playlistData)
                    }
                }
            }
        }catch (e:Exception){

        }

    }

    private fun addFavPlayList() {
        setLog(
            TAG,
            "addFavPlayList: 1 userPlaylist size:${userPlaylist?.size} favPlayList size:${favPlayList?.size}"
        )
        if (favPlayList != null && favPlayList?.size!! > 0) {
            favPlayList?.forEach {
                var playlistData = PlaylistRespModel.Data()
                playlistData?.data?.id = it?.data?.id!!
                playlistData?.data?.title = it?.data?.title!!
                if(!it?.data?.type?.isNullOrBlank()!!){
                    playlistData?.data?.type = it?.data?.type?.toInt()!!
                }
                playlistData?.public = false
                playlistData?.isOwnerPlaylist = false
                playlistData?.data?.image = "" + it?.data?.image
                playlistData?.data?.subtitle = "" + it?.data?.subtitle

                if(!it.data.misc.song_count.isNullOrBlank()){
                    playlistData?.data?.misc?.song_count = it.data.misc.song_count
                }else if(!it.data.songCount.isNullOrBlank()){
                    playlistData?.data?.misc?.song_count = it.data.songCount
                }

                userPlaylist.add(playlistData)
            }
            setLog(
                TAG,
                "addFavPlayList: 2 userPlaylist size:${userPlaylist?.size} favPlayList size:${favPlayList?.size}"
            )
        }
    }

    private fun setLocalBroadcast() {
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(mMessageReceiver, IntentFilter(Constant.AUDIO_PLAYER_EVENT))
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(mMessageReceiver, IntentFilter(Constant.MYPLAYLIST_EVENT))
    }

    override fun onLocalBroadcastEventCallBack(context: Context?, intent: Intent) {
        if (isAdded){
            val event = intent.getIntExtra("EVENT", 0)
            if (event == Constant.MYPLAYLIST_RESULT_CODE) {
                setUpViewModel()
            }
            if (event == Constant.AUDIO_PLAYER_RESULT_CODE) {
                CommonUtils.setPageBottomSpacing(rvMusicPlaylist, requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_100),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
            }
        }
    }

    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            CommonUtils.setLog(
                "BroadcastReceiver-1",
                "LibraryMusicAllFragment-mMessageReceiver-----" + intent
            )
            if (intent != null) {
                if (intent.hasExtra("EVENT")) {
                    CommonUtils.setLog(
                        "BroadcastReceiver-1",
                        "LibraryMusicAllFragment-mMessageReceiver------" + intent.getIntExtra("EVENT", 0)
                    )
                    onLocalBroadcastEventCallBack(context, intent)
                }
            }
        }
    }


}