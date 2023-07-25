package com.hungama.music.ui.main.view.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.media3.common.util.Util
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.hungama.fetch2.Download
import com.hungama.fetch2core.Reason
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.data.database.oldappdata.DBOHandler
import com.hungama.music.data.model.*
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.CreatedPlaylistEvent
import com.hungama.music.home.eventreporter.UserAttributeEvent
import com.hungama.music.player.audioplayer.Injection
import com.hungama.music.player.audioplayer.TracksContract
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.services.AudioPlayerService
import com.hungama.music.player.audioplayer.viewmodel.TracksViewModel
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.LibraryMusicAdapter
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.viewmodel.PlayableContentViewModel
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.ConnectionUtil
import com.hungama.music.utils.Constant
import com.hungama.music.utils.Utils
import com.hungama.music.utils.customview.downloadmanager.model.DownloadedAudio
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import kotlinx.android.synthetic.main.dialog_create_playlist.*
import kotlinx.android.synthetic.main.fragment_library_music_all.*
import kotlinx.android.synthetic.main.fragment_library_music_all.btnExplore
import kotlinx.android.synthetic.main.fragment_library_music_all.clExplore
import kotlinx.android.synthetic.main.fragment_library_music_all.createPlaylist
import kotlinx.android.synthetic.main.fragment_library_music_all.rvMusicPlaylist
import kotlinx.coroutines.*
import org.json.JSONObject
import java.util.*
import java.util.stream.Collectors


class LibraryMusicAllFragment(tabName1:String) : BaseFragment(), CreatePlaylistDialog.createPlayListListener,
    BaseActivity.OnDownloadQueueItemChanged, TracksContract.View,
    BaseActivity.OnLocalBroadcastEventCallBack {

    var tabName = tabName1
    companion object{
        val CREATE_PLAYLIST_ID=5001
        val DOWNLOADING_PROGRESS=5002
        val DOWNLOADED_SONGS=5003
        val FAVORITED_SONGS=5004
        val DOWNLOADED_PODCAST=5005
        val MY_DEVICE=5006
    }


    private lateinit var rvMusicLibrary: RecyclerView
    private lateinit var musicLibarayAdapter: LibraryMusicAdapter
    private var musicplayList = ArrayList<LibraryMusicModel>()
    var userViewModel: UserViewModel? = null
    var bookmarkDataModel: BookmarkDataModel? = null

    var downloadedSongSubtitle = ""
    var downloadedPodcastSubtitle = ""
    var downloadingSubtitle = ""
    var favorited_songs_subtitle = ""

    var isDownloadinProgressItemShow=true

    var userPlaylist: ArrayList<PlaylistRespModel.Data> = ArrayList()

    private lateinit var tracksViewModel: TracksContract.Presenter
    var musicData: LibraryMusicModel?=null
    var playableContentViewModel: PlayableContentViewModel? = null
    var defaultPlaylistName = ""
    var libraryAllModel: ArrayList<LibraryAllRespModel.Row> = ArrayList()
    lateinit var playlistListener: CreatePlaylistDialog.createPlayListListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_library_music_all, container, false)
    }

    override fun initializeComponent(view: View) {
        CommonUtils.applyButtonTheme(requireContext(), btnExplore)
        defaultPlaylistName = getString(R.string.hungama_playlist) + " 1"
        favorited_songs_subtitle = "0 " + getString(R.string.library_playlist_str_8)
        rvMusicLibrary = view.findViewById(R.id.rvMusicPlaylist)

        playlistListener = this

        createPlaylist?.setOnClickListener {
            createPlaylist()
        }

        ivMyDevice?.setOnClickListener {
            val localDeviceDetailFragment = LocalDeviceSongsDetailFragment()
            addFragment(R.id.fl_container, this@LibraryMusicAllFragment, localDeviceDetailFragment, false)

            CommonUtils.PageViewEvent("","","","",
                MainActivity.lastItemClicked + "_" + MainActivity.headerItemName + "_All",
                "my_device","")
        }
        btnExplore?.setOnClickListener {
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

        rvMusicLibrary.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        musicLibarayAdapter = LibraryMusicAdapter(requireContext()!!, musicplayList,object :
            LibraryMusicAdapter.PlayListItemClick{
            override fun libraryItemOnClick(musicData: LibraryMusicModel) {
                setLog(TAG, "libraryItemOnClick musicData id:${musicData.id} musicData containId:${musicData.containId}")
                if (musicData?.id?.equals("" + CREATE_PLAYLIST_ID) == true) {
                    createPlaylist()

                    CommonUtils.PageViewEvent("","","","",
                        MainActivity.lastItemClicked + "_" +MainActivity.headerItemName + "_All",
                        "popup_create playlist","")

                }
                else if (musicData?.id?.equals("" + MY_DEVICE) == true) {
                    val localDeviceDetailFragment = LocalDeviceSongsDetailFragment()
                    addFragment(R.id.fl_container, this@LibraryMusicAllFragment, localDeviceDetailFragment, false)

                    CommonUtils.PageViewEvent("","","","",
                        MainActivity.lastItemClicked + "_" + MainActivity.headerItemName + "_All",
                        "my_device","")

                }
                else if (musicData?.id?.equals("" + DOWNLOADING_PROGRESS) == true) {
                    val downloadedContentDetailFragment = DownloadingProgressFragment()
                    addFragment(
                        R.id.fl_container,
                        this@LibraryMusicAllFragment,
                        downloadedContentDetailFragment,
                        false
                    )
                } else if (musicData?.id?.equals("" + DOWNLOADED_SONGS) == true) {

                        val downloadedContentDetailFragment =
                            DownloadedContentDetailFragment(ContentTypes.AUDIO.value)

                        addFragment(
                            R.id.fl_container,
                            this@LibraryMusicAllFragment,
                            downloadedContentDetailFragment,
                            false
                        )
                } else if (musicData?.id?.equals("" + DOWNLOADED_PODCAST) == true) {
                    val downloadedContentDetailFragment = DownloadedContentDetailFragment(ContentTypes.PODCAST.value)
                    addFragment(
                        R.id.fl_container,
                        this@LibraryMusicAllFragment,
                        downloadedContentDetailFragment,
                        false
                    )
                } else if (musicData?.id?.equals("" + FAVORITED_SONGS) == true) {
                    val favoritedSongsDetailFragment = FavoritedSongsDetailFragment()
                    addFragment(
                        R.id.fl_container,
                        this@LibraryMusicAllFragment,
                        favoritedSongsDetailFragment,
                        false
                    )
                } else if (musicData?.id?.equals("" + 0) == true) {
                    val bundle = Bundle()
                    bundle.putString("image", musicData?.image)
                    bundle.putString("id", musicData?.containId)
                    bundle.putString("playerType", "" + musicData?.id)
                    bundle.putBoolean("varient", false)
                    val artistDetailsFragment = ArtistDetailsFragment()
                    artistDetailsFragment.arguments = bundle
                    addFragment(
                        R.id.fl_container,
                        this@LibraryMusicAllFragment,
                        artistDetailsFragment,
                        false
                    )

                } else if (musicData?.id?.equals("" + 1) == true) {
                    val bundle = Bundle()
                    bundle.putString("image", musicData?.image)
                    bundle.putString("id", musicData?.containId)
                    bundle.putString("playerType", "" + musicData?.id)
                    bundle.putBoolean("varient", false)
                    val albumDetailFragment = AlbumDetailFragment()
                    albumDetailFragment.arguments = bundle
                    addFragment(
                        R.id.fl_container,
                        this@LibraryMusicAllFragment,
                        albumDetailFragment,
                        false
                    )

                } else if (musicData?.id?.equals("" + 109) == true) {
                    val bundle = Bundle()
                    bundle.putString("image", musicData?.image)
                    bundle.putString("id", musicData?.containId)
                    bundle.putString("playerType", "" + musicData?.id)
                    bundle.putBoolean("varient", false)

                    val playlistDetailFragment = PodcastDetailsFragment()
                    playlistDetailFragment.arguments = bundle

                    addFragment(
                        R.id.fl_container,
                        this@LibraryMusicAllFragment,
                        playlistDetailFragment,
                        false
                    )

                } else if (musicData?.id?.equals("" + 55555) == true) {
                    val bundle = Bundle()
                    bundle.putString("image", musicData?.image)
                    bundle.putString("id", musicData?.containId)
                    bundle.putString("playerType", "" + musicData?.id)
                    var varient = 1

                    val playlistDetailFragment = PlaylistDetailFragmentDynamic.newInstance(varient)
                    playlistDetailFragment.arguments = bundle

                    addFragment(
                        R.id.fl_container,
                        this@LibraryMusicAllFragment,
                        playlistDetailFragment,
                        false
                    )

                } else if (musicData?.id?.equals("" + 34) == true || musicData?.id?.equals("" + 77777) == true) {
                    playRadio(musicData)
                } else if (musicData?.containId != null) {
                    val varient = 1
                    val playlistDetailFragment = MyPlaylistDetailFragment(varient,object :MyPlaylistDetailFragment.onBackPreesHendel{
                        override fun backPressItem(status: Boolean) {
                        }

                    })
                    val bundle = Bundle()
                    bundle.putString("image", musicData?.image)
                    bundle.putString("id", musicData?.containId)
                    bundle.putString("playerType", Constant.MUSIC_PLAYER)
                    playlistDetailFragment.arguments = bundle
                    setLocalBroadcast()
                    addFragment(
                        R.id.fl_container,
                        this@LibraryMusicAllFragment,
                        playlistDetailFragment,
                        false
                    )

/*                    CommonUtils.PageViewEvent("","","","",
                        MainActivity.lastItemClicked + "_" +MainActivity.headerItemName + "_All",
                        MainActivity.lastItemClicked + "_" +MainActivity.headerItemName + "_All_"+ "Add to Playlist","")*/
                }
            }

        })
        rvMusicLibrary.adapter = musicLibarayAdapter
        musicLibarayAdapter.notifyDataSetChanged()

        val fragment=LibraryMainTabFragment()
        fragment?.addReloadListener(object :LibraryMainTabFragment.onReloadListener{
            override fun onRefresh() {
                if (isAdded) {
                    if (requireView() != null) {
                        setLog(TAG, "onHiddenChanged: initializeComponent called")
                        initializeComponent(requireView())
                    }
                }
            }

        })


        baseIOScope.launch {
            firstTimeAddData(null)
        }

        setupUserViewModel()

        tracksViewModel = TracksViewModel(Injection.provideTrackRepository(), this)

        CommonUtils.setPageBottomSpacing(rvMusicPlaylist, requireContext(),
            resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_110),
            resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
    }

    private fun createPlaylist(isDeepLink:Boolean = false){
        if (SharedPrefHelper.getInstance().isUserLoggedIn()) {
            var lastNum = 1
            if (!musicplayList.isNullOrEmpty()) {
                musicplayList?.forEachIndexed { index, item ->
                    if (item?.id?.equals("99999", true) == true) {
                        if (item?.Title?.contains("Hungama Playlist ") == true) {
                            var count = item.Title.filter { it.isDigit() }
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
            }

            if (isDeepLink) {
                callCreatePlaylist(defaultPlaylistName)
            } else {
                val createPlaylistDialog = CreatePlaylistDialog(this@LibraryMusicAllFragment, defaultPlaylistName)
                createPlaylistDialog.show(
                    activity?.supportFragmentManager!!,
                    "open createplaylist dialog"
                )
            }
        }
    }

    private fun playRadio(musicData: LibraryMusicModel) {

        if (musicData?.id?.equals("" + 34) == true || musicData?.id?.equals("" + 77777) == true) {
            this.musicData = musicData;
            getPlayableContentUrl(musicData?.containId!!)
        }
    }

    /**
     * get Playable url for song : 21
     *
     * @param id
     */
    fun getPlayableContentUrl(id: String) {
        setLog("isGotoDownloadClicked", "LibraryMusicAllFragment-getPlayableContentUrl-isGotoDownloadClicked-${Constant.ISGOTODOWNLOADCLICKED}")
        if (ConnectionUtil(requireContext()).isOnline(!Constant.ISGOTODOWNLOADCLICKED)) {
            playableContentViewModel = ViewModelProvider(
                this
            ).get(PlayableContentViewModel::class.java)

            playableContentViewModel?.getPlayableContentList(requireContext(), id)?.observe(this,
                Observer {
                    when(it.status){
                        com.hungama.music.data.webservice.utils.Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                if (!TextUtils.isEmpty(it?.data?.data?.head?.headData?.misc?.url)) {
                                    setPlayableContentListData(it?.data)
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
            setEmptyVisible(false)
            setProgressBarVisible(false)
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }



    fun setPlayableContentListData(playableContentModel: PlayableContentModel) {

        if (playableContentModel != null) {
            setLog("PlayableItem", playableContentModel?.data?.head?.headData?.id.toString())
            songDataList = arrayListOf()


            if (playableContentModel?.data?.head?.headData?.id == musicData?.containId) {
                setPlayableDataList(
                    playableContentModel,
                    musicData!!
                )
            }
            BaseActivity.setTrackListData(songDataList)
            tracksViewModel.prepareTrackPlayback(0)


        }
    }

    var songDataList: ArrayList<Track> = arrayListOf()
    fun setPlayableDataList(
        playableContentModel: PlayableContentModel?,
        playableItem: LibraryMusicModel
    ) {
        val track: Track = Track()

        if (!TextUtils.isEmpty(playableItem?.containId)) {
            track.id = playableItem?.id!!.toLong()
        } else {
            track.id = 0
        }
        if (!TextUtils.isEmpty(playableItem?.Title)) {
            track.title = playableItem?.Title
        } else {
            track.title = ""
        }

        if (!TextUtils.isEmpty(playableItem?.SubTitle)) {
            track.subTitle = playableItem?.SubTitle
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

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.type.toString())) {
            track.playerType = playableContentModel?.data?.head?.headData?.type.toString()
        } else {
            track.playerType = ""
        }
        if (!TextUtils.isEmpty(playableItem.Title)) {
            track.heading = playableItem.Title
        } else {
            track.heading = ""
        }
        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.image.toString())) {
            track.image = playableContentModel?.data?.head?.headData?.image
        }else if (!TextUtils.isEmpty(playableItem?.image)) {
            track.image = playableItem?.image
        } else {
            track.image = ""
        }

        if (playableContentModel?.data?.head?.headData?.misc?.explicit != null){
            track.explicit = playableContentModel?.data?.head?.headData?.misc?.explicit!!
        }
        if (playableContentModel?.data?.head?.headData?.misc?.restricted_download != null){
            track.restrictedDownload = playableContentModel?.data?.head?.headData?.misc?.restricted_download!!
        }
        if (playableContentModel?.data?.head?.headData?.misc?.attributeCensorRating != null){
            track.attributeCensorRating =
                playableContentModel?.data?.head?.headData?.misc?.attributeCensorRating.toString()
        }
        if (!playableContentModel?.data?.head?.headData?.misc?.movierights.isNullOrEmpty()){
            track.movierights = playableContentModel?.data?.head?.headData?.misc?.movierights.toString()
        }else{
            track.movierights = ""
        }

        if (playableContentModel != null){
            track.urlKey = playableContentModel.data.head.headData.misc.urlKey
        }
        songDataList.add(track)

        CommonUtils
    }



//    override fun onHiddenChanged(hidden: Boolean) {
//        super.onHiddenChanged(hidden)
//        if (!hidden) {
//            firstTimeAddData()
//        }
//    }

    private fun firstTimeAddData(rows: ArrayList<LibraryAllRespModel.Row>?) {
        try {
            if (isAdded && context != null) {
                downloadingSubtitle = ""
                downloadedSongSubtitle = ""
                downloadedPodcastSubtitle = ""

                musicplayList = ArrayList<LibraryMusicModel>()

                musicplayList.add(
                    LibraryMusicModel(
                        "" + CREATE_PLAYLIST_ID,
                        getString(R.string.library_playlist_str_5),
                        "", "", ""
                    )
                )

//                musicplayList.add(
//                    LibraryMusicModel(
//                        "" + MY_DEVICE,
//                        getString(R.string.library_str_4),
//                        "", "", ""
//                    )
//                )


                val downloadingSongTotal = AppDatabase?.getInstance()?.downloadQueue()
                    ?.getDownloadQueueItemsByContentType(ContentTypes.AUDIO.value)
                setLog(TAG, "firstTimeAddData: downloadingSongTotal "+downloadingSongTotal)
                val downloadingPodcastTotal = AppDatabase?.getInstance()?.downloadQueue()
                    ?.getDownloadQueueItemsByContentType(ContentTypes.PODCAST.value)


                var totalSongs = 0
                var totalPadcast = 0

                if (downloadingSongTotal != null && downloadingSongTotal.isNotEmpty()) {
                    totalSongs = downloadingSongTotal.size
                    setLog(TAG, "firstTimeAddData: "+totalSongs)
                    downloadingSubtitle += totalSongs.toString() + " " + getString(R.string.library_playlist_str_8)

                    val userDataMap= java.util.HashMap<String, String>()
                    userDataMap.put(EventConstant.NUMBER_OF_DOWNLOADED_SONGS, ""+ totalSongs)
                    EventManager.getInstance().sendUserAttribute(UserAttributeEvent(userDataMap))
                }

                if (downloadingPodcastTotal != null && downloadingPodcastTotal.isNotEmpty()) {
                    totalPadcast = downloadingPodcastTotal.size
                    if (totalSongs > 0) {
                        downloadingSubtitle += ", " + totalPadcast.toString() + " " + getString(R.string.podcast_str_9)
                    } else {
                        downloadingSubtitle += totalPadcast.toString() + " " + getString(R.string.podcast_str_9)
                    }
                }
                if (totalSongs != 0 || totalPadcast != 0) {
                    musicplayList.add(
                        LibraryMusicModel(
                            "" +
                                    DOWNLOADING_PROGRESS,
                            getString(R.string.library_all_str_4),
                            downloadingSubtitle,
                            "",
                            ""
                        )
                    )
                }

                val downloadedSongTotal = AppDatabase?.getInstance()?.downloadedAudio()
                    ?.getDownloadQueueItemsByContentType(ContentTypes.AUDIO.value)

                setLog(TAG, "downloadedSongTotal: ${downloadedSongTotal?.size}")

                var totalDownloadedSong = 0
                if (!downloadedSongTotal.isNullOrEmpty()) {
                    totalDownloadedSong = downloadedSongTotal.size
                    //setLog(TAG, "downloadedSongTotal2: $totalDownloadedSong")
                    downloadedSongSubtitle += totalDownloadedSong.toString() + " " + getString(R.string.library_playlist_str_8)
                }
                if (totalDownloadedSong > 0) {
                    musicplayList.add(
                        LibraryMusicModel(
                            "" +
                                    DOWNLOADED_SONGS,
                            getString(R.string.library_all_str_1),
                            downloadedSongSubtitle, "", ""
                        )
                    )
                }

                if (bookmarkDataModel != null && bookmarkDataModel?.data != null && bookmarkDataModel?.data?.body != null && !bookmarkDataModel?.data?.body?.rows.isNullOrEmpty()){
                    val totalFav = bookmarkDataModel?.data?.body?.rows?.size.toString()
                    favorited_songs_subtitle = "$totalFav " + getString(R.string.library_playlist_str_8)
                    musicplayList.add(
                        LibraryMusicModel(
                            "" +
                                    FAVORITED_SONGS,
                            getString(R.string.favrited_str_1),
                            favorited_songs_subtitle, "", ""
                        )
                    )
                }

                val downloadedPodcastTotal = AppDatabase?.getInstance()?.downloadedAudio()
                    ?.getDownloadQueueItemsByContentType(ContentTypes.PODCAST.value)

                var totalDownloadedPodcast = 0
                if (downloadedPodcastTotal != null && downloadedPodcastTotal.isNotEmpty()) {
                    totalDownloadedPodcast = downloadedPodcastTotal.size
                    downloadedPodcastSubtitle += totalDownloadedPodcast.toString() + " " + getString(R.string.podcast_str_9)
                }


                if (totalDownloadedPodcast != 0) {
                    musicplayList.add(
                        LibraryMusicModel(
                            "" +
                                    DOWNLOADED_PODCAST,
                            getString(R.string.download_str_10),
                            downloadedPodcastSubtitle, "", ""
                        )
                    )
                }

                val playListoffline = AppDatabase.getInstance()?.downloadedAudio()
                    ?.getPlayList() as ArrayList<DownloadedAudio>
                if (!playListoffline.isNullOrEmpty()) {
                    playListoffline.forEach {
                        setLog(TAG, "firstTimeAddData: $it")
                        musicplayList.add(
                            LibraryMusicModel(
                                "55555",
                                it.pName!!,
                                ""+getString(R.string.library_music_str_9),
                                it.parentThumbnailPath.toString(),
                                it.parentId!!,
                            )
                        )

                    }
                }

                val userPlayListOffline = AppDatabase.getInstance()?.downloadedAudio()
                    ?.getUserPlayList() as ArrayList<DownloadedAudio>
                if (!userPlayListOffline.isNullOrEmpty()) {
                    userPlayListOffline.forEach {
                        var isUserPlaylistAvailable = false
                        if (!rows.isNullOrEmpty()){
                            for (item in rows){
                                if (item.data.id.equals(it.parentId)){
                                    isUserPlaylistAvailable = true
                                }
                            }
                        }
                        if (!isUserPlaylistAvailable){
                            setLog(TAG, "firstTimeAddData: $it")
                            musicplayList.add(
                                LibraryMusicModel(
                                    "99999",
                                    it.pName!!,
                                    ""+getString(R.string.library_music_str_9),
                                    it.parentThumbnailPath.toString(),
                                    it.parentId!!,
                                )
                            )
                        }
                    }
                }

                val albumoffline = AppDatabase.getInstance()?.downloadedAudio()
                    ?.getAlbumList() as ArrayList<DownloadedAudio>
                if (!albumoffline.isNullOrEmpty()) {
                    albumoffline.forEach {
                        setLog(TAG, "firstTimeAddData: $it")
                        musicplayList.add(
                            LibraryMusicModel(
                                "1",
                                it?.pName!!,
                                ""+it?.f_playcount!!+" discover_str_24",
                                it?.pImage!!,
                                it?.parentId!!,
                            )
                        )

                    }
                }
                setLog(TAG, "downloadedSongTotal3: ${musicplayList?.size}")
                if(musicplayList.size > 1){
                    setUpUi(true)
                }else{
                    setUpUi(false)
                }


            }
        }catch (e:Exception){

        }


    }

    private fun setUpUi(status: Boolean){
        if(isAdded()){
            baseMainScope.launch {
                setLog(TAG, "downloadedSongTotal4: $status")
                if (status) {
                    rvMusicPlaylist.visibility = View.VISIBLE
                    clExplore.visibility = View.GONE
                    //musicLibarayAdapter?.notifyDataSetChanged()
                    musicLibarayAdapter?.refreshData(musicplayList)
                } else {
                    clExplore.visibility = View.VISIBLE
                    rvMusicPlaylist.visibility = View.GONE

                }
            }
        }


    }

    private fun fetchDataFromDB(isUpdate: Boolean) {
        try {
            if (isAdded()) {
                downloadingSubtitle = ""
                downloadedSongSubtitle = ""
                downloadedPodcastSubtitle = ""

                val downloadingSongTotal = AppDatabase?.getInstance()?.downloadQueue()
                    ?.getDownloadQueueItemsByContentType(ContentTypes.AUDIO.value)
                val downloadingPodcastTotal = AppDatabase?.getInstance()?.downloadQueue()
                    ?.getDownloadQueueItemsByContentType(ContentTypes.PODCAST.value)


                var totalSongs = 0
                var totalPadcast = 0



                if (downloadingSongTotal != null && downloadingSongTotal.isNotEmpty()) {
                    totalSongs = downloadingSongTotal.size
                    setLog(TAG, "fetchDataFromDB: "+totalSongs)
                    downloadingSubtitle += totalSongs.toString() + " " + getString(R.string.library_playlist_str_8)
                }

                if (downloadingPodcastTotal != null && downloadingPodcastTotal.isNotEmpty()) {
                    totalPadcast = downloadingPodcastTotal.size
                    if (totalSongs > 0) {
                        downloadingSubtitle += ", " + totalPadcast.toString() + " " + getString(R.string.podcast_str_9)
                    } else {
                        downloadingSubtitle += totalPadcast.toString() + " " + getString(R.string.podcast_str_9)
                    }
                }



                val downloadedSongTotal = AppDatabase?.getInstance()?.downloadedAudio()
                    ?.getDownloadQueueItemsByContentType(ContentTypes.AUDIO.value)

                var totalDownloadedSong = 0
                if (downloadedSongTotal != null && downloadedSongTotal.isNotEmpty()) {
                    totalDownloadedSong = downloadedSongTotal.size
                    setLog(TAG, "fetchDataFromDB: "+totalDownloadedSong)
                    downloadedSongSubtitle += totalDownloadedSong.toString() + " " + getString(R.string.library_playlist_str_8)
                }

                if (totalDownloadedSong != 0 && musicplayList.size > 1) {
                    var DOWNLOADED_SONGS_POSITION=2
                    if(!isDownloadinProgressItemShow){
                        DOWNLOADED_SONGS_POSITION=DOWNLOADED_SONGS_POSITION-1
                    }
                    musicplayList.set(
                        DOWNLOADED_SONGS_POSITION,
                        LibraryMusicModel(
                            ""+ DOWNLOADED_SONGS,
                            getString(R.string.library_all_str_1),
                            downloadedSongSubtitle, "", ""
                        )
                    )
                    musicLibarayAdapter?.notifyItemChanged(DOWNLOADED_SONGS_POSITION)

                }

                val downloadedPodcastTotal = AppDatabase?.getInstance()?.downloadedAudio()
                    ?.getDownloadQueueItemsByContentType(ContentTypes.PODCAST.value)

                var totalDownloadedPodcast = 0
                if (downloadedPodcastTotal != null && downloadedPodcastTotal.isNotEmpty()) {
                    totalDownloadedPodcast = downloadedPodcastTotal.size
                    downloadedPodcastSubtitle += totalDownloadedPodcast.toString() + " " + getString(R.string.podcast_str_9)
                }

                if (totalDownloadedPodcast != 0) {
                    var DOWNLOADED_PODCAST_POSITION=3
                    if(!isDownloadinProgressItemShow){
                        DOWNLOADED_PODCAST_POSITION=DOWNLOADED_PODCAST_POSITION-1
                    }
                    musicplayList.set(
                        DOWNLOADED_PODCAST_POSITION,
                        LibraryMusicModel(
                            ""+ DOWNLOADED_PODCAST,
                            getString(R.string.download_str_10),
                            downloadedPodcastSubtitle, "", ""
                        )
                    )
                    musicLibarayAdapter?.notifyItemChanged(DOWNLOADED_PODCAST_POSITION)

                }

                setLog(TAG, "fetchDataFromDB --2 totalSongs:$totalSongs totalPadcast:$totalPadcast size:${musicplayList.size} DOWNLOADING_PROGRESS:${DOWNLOADING_PROGRESS}")
                if ((totalSongs != 0 || totalPadcast != 0) && musicplayList.size > 1) {
                    musicplayList.set(1,
                        LibraryMusicModel(
                            ""+ DOWNLOADING_PROGRESS,
                            getString(R.string.library_all_str_4),
                            downloadingSubtitle, "", ""
                        )
                    )
                    musicLibarayAdapter?.notifyItemChanged(1)

                    setLog(TAG, "fetchDataFromDB --2-1 totalSongs:$totalSongs totalPadcast:$totalPadcast size:${musicplayList.size} DOWNLOADING_PROGRESS:${DOWNLOADING_PROGRESS}")
                } else {
                    musicplayList?.forEachIndexed { index, libraryMusicModel ->
                        setLog(TAG, "fetchDataFromDB --2 libraryMusicModel size:${libraryMusicModel.id}")

                        if (libraryMusicModel?.id.equals("" + DOWNLOADING_PROGRESS)) {
                            musicplayList?.remove(libraryMusicModel)
                            musicLibarayAdapter?.notifyItemRemoved(index)
                            isDownloadinProgressItemShow=false
                            setLog(TAG, "fetchDataFromDB after removeAt: musicplayList size:${musicplayList.size}")
                            return
                        }else
                        {
                            setLog(TAG, "fetchDataFromDB --2 musicplayList size:${musicplayList.size}")
                        }
                    }
                }

                setLog(TAG, "fetchDataFromDB: musicplayList size:${musicplayList.size}")

                if(musicplayList.size > 1){
                    setUpUi(true)
                }else{
                    setUpUi(false)
                }
            }
        }catch (e:Exception){
            e.printStackTrace()
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

        val playlistDetailFragment = MyPlaylistDetailFragment(varient,object : MyPlaylistDetailFragment.onBackPreesHendel{
            override fun backPressItem(status: Boolean) {
                setupUserViewModel()
            }

        })
        val bundle = Bundle()
        bundle.putString("id", id)
        bundle.putString("playerType", Constant.MUSIC_PLAYER)
        playlistDetailFragment.arguments = bundle
        setLocalBroadcast()
        addFragment(R.id.fl_container, this@LibraryMusicAllFragment, playlistDetailFragment, false)
//        firstTimeAddData()
        //setupUserViewModel()
    }


    private fun setupUserViewModel() {
        baseMainScope.launch {
            userViewModel = ViewModelProvider(
                this@LibraryMusicAllFragment
            ).get(UserViewModel::class.java)
            setLog("isGotoDownloadClicked", "LibraryMusicAllFragment-setupUserViewModel-isGotoDownloadClicked-${Constant.ISGOTODOWNLOADCLICKED}")
            if (ConnectionUtil(requireContext()).isOnline(!Constant.ISGOTODOWNLOADCLICKED)) {
                userViewModel?.getLibraryAllData(requireContext())?.observe(this@LibraryMusicAllFragment,
                    Observer {
                        when(it.status){
                            Status.SUCCESS->{
                                setProgressBarVisible(false)
                                setLog(TAG, "setupUserViewModel: it?.data "+it?.data)
    //                            if (libraryAllModel.isNullOrEmpty()){
    //                                setupAllList(libraryAllModel)
    //                            }
                                if (it?.data != null) {
                                    libraryAllModel = it.data.rows

                                    val rowsData : ArrayList<LibraryAllRespModel.Row> = ArrayList()
//                                    rowsData.addAll(libraryAllModel)
                                    for (itemOf in libraryAllModel){
                                        val playListoffline = AppDatabase.getInstance()?.downloadedAudio()?.getUserPlayList(itemOf.data.id) as ArrayList<DownloadedAudio>

                                        for(playL in playListoffline){
                                            if(itemOf.data.id == playL.parentId){
                                                rowsData.add(itemOf)
                                            }
                                        }

                                    }
                                    val libraryAllModel1 = rowsData


   /*                                 var libraryAllModel1 : ArrayList<LibraryAllRespModel.Row> = ArrayList()
                                    if(rowsData.isNotEmpty())
                                        libraryAllModel1 = rowsData
                                    else if (libraryAllModel.isNotEmpty())
                                        libraryAllModel1.addAll(libraryAllModel)*/

                                    SharedPrefHelper.getInstance().savePlayList("PlayList", rowsData)
/*                                    CoroutineScope(Dispatchers.IO).launch {
                                        SharedPrefHelper.getInstance().getPlayList("PlayList")
                                    }*/
                                    bookmarkDataModel = null
                                    baseMainScope.launch {
                                        // 21,36  added typeId
                                        userViewModel?.getUserBookmarkedData(requireContext(), Constant.MODULE_FAVORITE, "")?.observe(this@LibraryMusicAllFragment
                                        ) {
                                            when (it.status) {
                                                Status.SUCCESS -> {
                                                    setProgressBarVisible(false)
                                                    setLog(TAG, "setupUserViewModel: it?.data" + it?.data)

                                                    if ((it?.data != null && it.data.data.body.rows.isNotEmpty()) && libraryAllModel1 != null) {

                                                        for(i in it.data.data.body.rows.indices){
                                                            for (j in libraryAllModel1.indices){
                                                                if (j>= libraryAllModel1.size)
                                                                    break
                                                                if (it.data.data.body.rows[i].data.id == libraryAllModel1[j].data.id){
                                                                    rowsData.removeAt(j)
                                                                }
                                                            }
                                                        }

                                               SharedPrefHelper.getInstance().savePlayList("PlayList", rowsData)

                                                        bookmarkDataModel = it.data
                                                        if (!libraryAllModel.isNullOrEmpty()) {
                                                            setLog("lahlghla", "865" + Gson().toJson(libraryAllModel))
                                                            setupAllList(libraryAllModel)

/*                                                            for(item in it.data.data.body.rows){
                                                                for (lib in libraryAllModel){
                                                                    if (item.data.id == lib.data.id){
                                                                        setLog("bookmarkedData", " " + item.data.title)
                                                                    }
                                                                }
                                                            }*/
                                                        }
                                                    } else {
                                                        setLog("lahlghla","869" + Gson().toJson(libraryAllModel))

                                                        setupAllList(libraryAllModel)
                                                    }

                                                }

                                                Status.LOADING -> {
                                                    setProgressBarVisible(true)
                                                }

                                                Status.ERROR -> {
                                                    if (!libraryAllModel.isNullOrEmpty()) {
                                                        setLog("lahlghla","882" + Gson().toJson(libraryAllModel))
                                                        setupAllList(libraryAllModel)
                                                    }
                                                    setEmptyVisible(false)
                                                    setProgressBarVisible(false)
                                                    Utils.showSnakbar(
                                                        requireContext(),
                                                        requireView(),
                                                        true,
                                                        it.message!!
                                                    )
                                                }
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

                if(SharedPrefHelper.getInstance().getPlayList("PlayList") != null && SharedPrefHelper.getInstance().getPlayList("PlayList")!!.isNotEmpty()){
                    libraryAllModel = SharedPrefHelper.getInstance().getPlayList("PlayList")!!
//                    setupAllList(libraryAllModel)
                }
//                else{
                    val downloadedSongTotal = AppDatabase?.getInstance()?.downloadedAudio()?.getDownloadQueueItemsByContentType(ContentTypes.AUDIO.value)
                    val dbDataold = DBOHandler.getAllCachedTracks(requireContext())

//                    libraryAllModel.clear()

//                    Log.e("alkghlah", Gson().toJson(downloadedSongTotal) + "\n")
                    Log.e("alkghlah", Gson().toJson(dbDataold))

                    var count = 1
                    if (downloadedSongTotal!!.isNotEmpty() && dbDataold.isNotEmpty()) {

                        for (item in downloadedSongTotal) {
                            for (i in dbDataold) {
                                if (item.contentId == i.trackId) {
                                    count += 1
                                    val jsonObject = JSONObject(i.jsonDetails)

                                    val misc = LibraryAllRespModel.Row.Data.Misc()
//                                    misc.artist = stringToWords(item.artist!!) as ArrayList<String>
                                    if (item.attribute_censor_rating!!.isNotEmpty())
                                        misc.attributeCensorRating =
                                            stringToWords(item.attribute_censor_rating!!) as ArrayList<String>
                                    misc.description = item.description!!
                                    misc.favCount = item.f_fav_count
                                    if (item.language!!.isNotEmpty())
                                        if (item.movierights!!.isNotEmpty())
                                            misc.movierights = stringToWords(item.movierights.toString()) as ArrayList<String>
                                    misc.nudity = item.nudity!!
                                    misc.playcount = item.playcount.toString()
                                    misc.ratingCritic = if (item.criticRating.toString()
                                            .isNotEmpty()) item.criticRating.toString().toInt() else 0
                                    if (item.artist!!.isNotEmpty())
//                                        misc.sArtist = stringToWords(item.artist.toString()) as ArrayList<String>
                                    misc.synopsis = item.synopsis!!

                                    val data = LibraryAllRespModel.Row.Data()

                                    setLog("alhgolahoighoa", " " + Gson().toJson(jsonObject))

                                    data.duration = 0
                                    data.id = if (jsonObject.has("myPlaylistID")) jsonObject.getString("myPlaylistID") else ""
                                    data.image = item.thumbnailPath.toString()
                                    data.misc = misc
                                    data.releasedate = item.releaseDate!!
                                    data.subtitle = item.subTitle!!
                                    data.title = if(jsonObject.has("playlistname")) jsonObject.getString("playlistname") else "Offline_playlist$count"
//                                    data.type = DOWNLOADED_SONGS
                                    data.type = 99999

                                    setLog("playlistData", Gson().toJson(data))

                                    val rows = LibraryAllRespModel.Row()
                                    rows.data = data
                                    rows.itype = 2
                                    rows.public = true

                                    var count = 0
                                    if (libraryAllModel.size>0){

                                        for (libray in libraryAllModel){
                                            if(libray.data.id == data.id){
                                                count +=1
                                            }
                                        }
                                    }

                                    if(count == 0)
                                    libraryAllModel.add(rows)
                                }
                            }
                        }

                        var libraryAllModel1 : ArrayList<LibraryAllRespModel.Row> = ArrayList()
                        for (item in libraryAllModel){
                            if (item.data.id.isNotEmpty()){
                                libraryAllModel1.add(item)
                            }
                        }

                        libraryAllModel1 = libraryAllModel1.stream().distinct().collect(Collectors.toList()) as ArrayList<LibraryAllRespModel.Row>
                        setLog("playlistData", Gson().toJson(libraryAllModel1))
                        setupAllList(libraryAllModel1)
                    }
//                }
            }
        }

    }

    fun stringToWords(s : String) = s.trim().splitToSequence(' ')
        .filter { it.isNotEmpty() } // or: .filter { it.isNotBlank() }
        .toList()

    override fun onResume() {
        super.onResume()
        setLocalBroadcast()
        (requireActivity() as MainActivity).addOrUpdateDownloadMusicQueue(
            ArrayList(),
            this,
            null,
            true,
            false
        )

        setLog(TAG, "onResume:............................")
    }

    override fun onDownloadQueueItemChanged(data: Download, reason: Reason) {
        baseIOScope?.launch {
            if(isAdded()){
                setLog("DWProgrss-onChangedid", data.id.toString())
                setLog("DWProgrss-onChanged", reason.toString())

                when (reason) {
                    Reason.DOWNLOAD_ADDED -> {
                        setLog("DWProgrss-ADDED", data.id.toString())
                    }
                    Reason.DOWNLOAD_QUEUED -> {
                        setLog("DWProgrss-QUEUED", data.id.toString())

                    }
                    Reason.DOWNLOAD_STARTED -> {
                        setLog("DWProgrss-STARTED", data.id.toString())

                    }
                    Reason.DOWNLOAD_PROGRESS_CHANGED -> {
                        setLog("DWProgrss-CHANGED", data.id.toString())
                    }
                    Reason.DOWNLOAD_RESUMED -> {
                        setLog("DWProgrss-RESUMED", data.id.toString())
                    }
                    Reason.DOWNLOAD_PAUSED -> {
                        setLog("DWProgrss-PAUSED", data.id.toString())
                    }
                    Reason.DOWNLOAD_COMPLETED -> {
                        setLog("DWProgrss-COMPLETED", data.id.toString())
                        CoroutineScope(Dispatchers.IO).launch {
                            delay(2000)
                            fetchDataFromDB(true)
                        }
                    }
                    Reason.DOWNLOAD_CANCELLED -> {
                        setLog("DWProgrss-CANCELLED", data.id.toString())
                    }
                    Reason.DOWNLOAD_REMOVED -> {
                        setLog("DWProgrss-REMOVED", data.id.toString())
                    }
                    Reason.DOWNLOAD_DELETED -> {
                        setLog("DWProgrss-DELETED", data.id.toString())
                    }
                    Reason.DOWNLOAD_ERROR -> {
                        setLog("DWProgrss-ERROR", data.id.toString())
                    }
                    Reason.DOWNLOAD_BLOCK_UPDATED -> {
                        setLog("DWProgrss-UPDATED", data.id.toString())
                    }
                    Reason.DOWNLOAD_WAITING_ON_NETWORK -> {
                        setLog("DWProgrss-NETWORK", data.id.toString())
                    }
                    else -> {}
                }
            }
        }


    }

    override fun startTrackPlayback(
        selectedTrackPosition: Int,
        tracksList: MutableList<Track>,
        trackPlayStartPosition: Long
    ) {
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

    override fun getViewActivity(): AppCompatActivity {
        return activity as AppCompatActivity
    }

    override fun getApplicationContext(): Context {
        return (activity as AppCompatActivity).applicationContext
    }

    private fun setLocalBroadcast(){
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(mMessageReceiverLibrary)
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(mMessageReceiverLibrary, IntentFilter(Constant.AUDIO_PLAYER_EVENT))
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(mMessageReceiverLibrary, IntentFilter(Constant.MYPLAYLIST_EVENT))
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(mMessageReceiverLibrary, IntentFilter(Constant.DOWNLOADED_CONTENT_EVENT))
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(mMessageReceiverLibrary, IntentFilter(Constant.FAVORITE_CONTENT_EVENT))
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(mMessageReceiverLibrary, IntentFilter(Constant.LIBRARY_CONTENT_EVENT))
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(mMessageReceiverLibrary, IntentFilter(Constant.STICKY_ADS_VISIBILITY_CHANGE_EVENT))
    }

    override fun onLocalBroadcastEventCallBack(context: Context?, intent: Intent) {
        if (isAdded){
            val event = intent.getIntExtra("EVENT", 0)
            if (event == Constant.MYPLAYLIST_RESULT_CODE || event == Constant.DOWNLOADED_CONTENT_RESULT_CODE
                || event == Constant.FAVORITE_CONTENT_RESULT_CODE || event == Constant.LIBRARY_CONTENT_RESULT_CODE) {
//                firstTimeAddData()
                setupUserViewModel()
            }
            if (event == Constant.AUDIO_PLAYER_RESULT_CODE) {
                CommonUtils.setPageBottomSpacing(rvMusicPlaylist, requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_150),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
            }else if (context != null && event == Constant.STICKY_ADS_VISIBILITY_CHANGE_RESULT_CODE){
                CommonUtils.setPageBottomSpacing(rvMusicPlaylist, requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_150),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
            }
        }
    }

    private fun setupAllList(rows: ArrayList<LibraryAllRespModel.Row>) {
        baseMainScope?.launch {
            firstTimeAddData(rows)
            rows?.forEachIndexed { index, libraryAllRespModelItem ->
                var subtitle = ""
                if (libraryAllRespModelItem?.data?.type == 99999){
                    //subtitle = getString(R.string.library_music_str_9)
                }else if (libraryAllRespModelItem?.data?.type == 0){
                    subtitle =getString(R.string.library_music_str_7)
                }else if (libraryAllRespModelItem?.data?.type == 1){
                    subtitle = getString(R.string.library_all_str_9)
                }else if (libraryAllRespModelItem?.data?.type == 109){
                    subtitle = getString(R.string.search_str_5)
                }else if (libraryAllRespModelItem?.data?.type == 55555){
                    subtitle = getString(R.string.library_music_str_9)
                }else if (libraryAllRespModelItem?.data?.type == 34 && libraryAllRespModelItem?.data?.type == 77777){
                    subtitle = getString(R.string.search_str_9)
                }
                else{
                    setLog("alhglhal", "OfflinePlayList")
                }
                if (!TextUtils.isEmpty(libraryAllRespModelItem?.data?.subtitle)){
                    if (!TextUtils.isEmpty(subtitle)){
                        subtitle += " • " + libraryAllRespModelItem?.data?.subtitle
                    }else{
                        subtitle +=libraryAllRespModelItem?.data?.subtitle
                    }
                }
                musicplayList.add(
                    LibraryMusicModel(
                        "" +
                                libraryAllRespModelItem?.data?.type,
                        libraryAllRespModelItem.data.title,
                        subtitle,
                        libraryAllRespModelItem.data.image,
                        libraryAllRespModelItem.data.id,
                    )
                )
                setLog(TAG, "libraryMusicAllRespObserver musicData title:${libraryAllRespModelItem?.data?.title} musicData id:${libraryAllRespModelItem?.data?.type} musicData containId:${libraryAllRespModelItem?.data?.id}")
            }
            withContext(Dispatchers.Main){
                if(musicplayList.size > 1){
                    setUpUi(true)
                }else{
                    setUpUi(false)
                }

                setLog(TAG, "libraryMusic " + Gson().toJson(musicplayList))

                musicLibarayAdapter?.refreshData(musicplayList)


                if (tabName == "create-playlist")
                    createPlaylist(true)
            }

        }

    }

    private fun callCreatePlaylist(playListName:String) {
        try {
            val mainJson = JSONObject()

            mainJson.put("name", playListName)
            mainJson.put("public", switchicon?.isChecked == true)

            userViewModel?.createPlayList(requireContext(), mainJson)?.observe(this,
                Observer {
                    when(it.status){
                        com.hungama.music.data.webservice.utils.Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null && !TextUtils.isEmpty(it?.data?.data?.id)) {

                                setLog("TAG", "callCreatePlaylist: it?.data:${it?.data}")

                                CoroutineScope(Dispatchers.IO).launch {
                                    val hashMap = HashMap<String,String>()
                                    hashMap[EventConstant.CONTENTTYPE_EPROPERTY] = "Audio"
                                    hashMap[EventConstant.PLAYLISTNAME_EPROPERTY] = it.data.data.title
                                    if(MainActivity.lastBottomItemPosClicked==4){
                                        hashMap[EventConstant.SOURCE_NAME_EPROPERTY] = "Library"
                                    }else {
                                        hashMap[EventConstant.SOURCE_NAME_EPROPERTY] = "Player_3 Dot Menu"
                                    }

                                    EventManager.getInstance().sendEvent(CreatedPlaylistEvent(hashMap))
                                }


                                if(playlistListener!=null){
                                    playlistListener?.playListCreatedSuccessfull(it.data.data.id)
                                    tabName = ""
                                }
                            }else if (!TextUtils.isEmpty(it?.data?.message)){
                                val messageModel = MessageModel(it?.data?.message.toString(), MessageType.NEUTRAL, true)
                                CommonUtils.showToast(requireContext(), messageModel)
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

        } catch (e: Exception) {
            e.printStackTrace()
            Utils.showSnakbar(requireContext(),
                requireView(),
                false,
                getString(R.string.discover_str_2)
            )
        }
    }

    private val mMessageReceiverLibrary: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            CommonUtils.setLog(
                "BroadcastReceiver-1",
                "LibraryMusicAllFragment-mMessageReceiver-" + intent
            )
            if (intent != null) {
                if (intent.hasExtra("EVENT")) {
                    CommonUtils.setLog(
                        "BroadcastReceiver-1",
                        "LibraryMusicAllFragment-mMessageReceiver-" + intent.getIntExtra("EVENT", 0)
                    )
                    onLocalBroadcastEventCallBack(context, intent)
                }
            }
        }
    }


}