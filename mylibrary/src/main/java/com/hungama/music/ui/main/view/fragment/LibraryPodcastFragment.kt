package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.*
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.LibraryMusicAdapter
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.data.model.*
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.main.adapter.LibraryPodcastsAdapter
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.utils.Constant
import com.hungama.music.utils.ConnectionUtil
import com.hungama.music.utils.Utils
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.customview.downloadmanager.model.DownloadedAudio
import kotlinx.android.synthetic.main.fr_library_main.*
import kotlinx.android.synthetic.main.fragment_library_podcast_all.*
import kotlinx.android.synthetic.main.layout_progress.*
import kotlinx.coroutines.launch


class LibraryPodcastFragment : BaseFragment(), CreatePlaylistDialog.createPlayListListener,
    BaseActivity.OnLocalBroadcastEventCallBack {

    companion object{
        val FOLLOW_MORE_PODCAST_ID=6001
        val DOWNLOADING_PROGRESS=6002
        val DOWNLOADED_PODCAST=6003
    }
    private lateinit var btnExplore: LinearLayoutCompat
    private lateinit var rvMusicLibrary: RecyclerView
    private lateinit var musicLibarayAdapter: LibraryMusicAdapter
    private var musicplayList = ArrayList<LibraryMusicModel>()
    var userViewModel: UserViewModel = UserViewModel()
    var bookmarkDataModel: BookmarkDataModel? = null
    var userPodcastList: ArrayList<BookmarkDataModel.Data.Body.Row> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_library_podcast_all, container, false)
    }

    override fun initializeComponent(view: View) {
        baseMainScope.launch {
            btnExplore = view.findViewById(R.id.btnExplore)
            btnExplore.setOnClickListener {
                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        CommonUtils.hapticVibration(requireContext(), btnExplore!!,
                            HapticFeedbackConstants.CONTEXT_CLICK
                        )
                    }
                }catch (e:Exception){

                }
                val bundle = Bundle()
                bundle.putBoolean(Constant.isTabSelection, true)
                bundle.putString(Constant.tabName, "podcast")
                (activity as MainActivity).applyScreen(1,bundle)
            }
            CommonUtils.applyButtonTheme(requireContext(), btnExplore)
            rvMusicLibrary = view.findViewById(R.id.rvMusicPlaylist)

            musicplayList = ArrayList<LibraryMusicModel>()
            musicplayList.add(
                LibraryMusicModel(
                    ""+
                            FOLLOW_MORE_PODCAST_ID,
                    getString(R.string.podcast_str_13),
                    "", "", ""
                )
            )

            val downloadingPodcastTotal = AppDatabase?.getInstance()?.downloadQueue()?.getDownloadQueueItemsByContentType(
                ContentTypes.PODCAST.value)
            var downloadingSubtitle = ""
            var totalPadcast = 0

            if (downloadingPodcastTotal != null && downloadingPodcastTotal.isNotEmpty()){
                totalPadcast = downloadingPodcastTotal.size
                downloadingSubtitle += totalPadcast.toString() + " " + getString(R.string.podcast_str_9)
            }
            if (totalPadcast != 0){
                musicplayList.add(
                    LibraryMusicModel(
                        ""+
                                DOWNLOADING_PROGRESS,
                        getString(R.string.library_all_str_4),
                        downloadingSubtitle, "", ""
                    )
                )
            }

            val downloadedPodcastTotal = AppDatabase?.getInstance()?.downloadedAudio()?.getDownloadQueueItemsByContentType(
                ContentTypes.PODCAST.value)
            var downloadedPodcastSubtitle = ""
            var totalDownloadedPodcast = 0
            if (downloadedPodcastTotal != null && downloadedPodcastTotal.isNotEmpty()){
                totalDownloadedPodcast = downloadedPodcastTotal.size
                downloadedPodcastSubtitle += totalDownloadedPodcast.toString() + " " + getString(R.string.podcast_str_9)
            }
            if (totalDownloadedPodcast != 0){
                musicplayList.add(
                    LibraryMusicModel(
                        ""+
                                DOWNLOADED_PODCAST,
                        getString(R.string.download_str_10),
                        downloadedPodcastSubtitle, "", ""
                    )
                )
            }

            rvMusicLibrary.layoutManager =
                LinearLayoutManager(requireContext()!!, LinearLayoutManager.VERTICAL, false)
            musicLibarayAdapter = LibraryMusicAdapter(requireContext()!!, musicplayList,object :
                LibraryMusicAdapter.PlayListItemClick{
                override fun libraryItemOnClick(musicData: LibraryMusicModel) {
                    if(musicData?.id?.equals(""+ FOLLOW_MORE_PODCAST_ID) == true){
                        val bundle = Bundle()
                        bundle.putString("playerType", Constant.PLAYER_PODCAST_AUDIO_TRACK)
                        val followMorePodcastFragment = FollowMorePodcastFragment()
                        followMorePodcastFragment.arguments = bundle
                        addFragment(R.id.fl_container, this@LibraryPodcastFragment, followMorePodcastFragment, false)


                        callPageViewEventForTab(MainActivity.lastItemClickedForBTab + "_" + MainActivity.headerItemNameForBTab + MainActivity.subHeaderItemNameForBTab,
                            "library_music_podcasts_followpodcasts",
                            "0","listing")



                    }else if (musicData?.id?.equals(""+ DOWNLOADING_PROGRESS) == true){
                        //val downloadedContentDetailFragment = DownloadedContentDetailFragment(1)
                        //addFragment(R.id.fl_container, this@LibraryPodcastFragment, downloadedContentDetailFragment, false)
                    }else if (musicData?.id?.equals(""+ DOWNLOADED_PODCAST) == true){
                        val downloadedContentDetailFragment = DownloadedContentDetailFragment(ContentTypes.PODCAST.value)
                        addFragment(R.id.fl_container, this@LibraryPodcastFragment, downloadedContentDetailFragment, false)
                    }
                }
            })

//        rvMusicLibrary?.setRecycledViewPool(RecyclerView.RecycledViewPool())
//        rvMusicLibrary?.setHasFixedSize(true)
            rvMusicLibrary.adapter = musicLibarayAdapter
//        rvMusicLibrary?.isNestedScrollingEnabled=false
            musicLibarayAdapter.notifyDataSetChanged()
            if (!musicplayList.isNullOrEmpty() && musicplayList.size > 1){
                rvMusicPlaylist.visibility = View.VISIBLE
                clExplore.visibility = View.GONE
            }else{
                clExplore.visibility = View.VISIBLE
            }
            setUpViewModel()
            CommonUtils.setPageBottomSpacing(rvPodcastList, requireContext(),
                resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
                resources.getDimensionPixelSize(R.dimen.dimen_0), 0)

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
        addFragment(R.id.fl_container, this@LibraryPodcastFragment, playlistDetailFragment, false)
    }

    private fun setUpViewModel() {
        userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)
        getData()
    }


    private fun getData() {
        baseMainScope.launch {
            if (ConnectionUtil(requireContext()).isOnline) {
                userViewModel?.getFollwingWithFilter(
                    requireContext(),
                    Constant.MODULE_FOLLOW,
                    "109"
                )?.observe(this@LibraryPodcastFragment,
                    Observer {
                        when(it.status){
                            Status.SUCCESS->{
                                setProgressBarVisible(false)
                                if (it?.data != null) {
                                    setUpLists(it?.data)
                                }
                            }
                            Status.LOADING ->{
                                setProgressBarVisible(true)
                            }

                            Status.ERROR ->{
                                setEmptyVisible(false)
                                setProgressBarVisible(false)
                                Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                                setUpLists(null)
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

    }



    private fun setUpLists(userPlaylistData: BookmarkDataModel?) {
        userPodcastList = ArrayList()
        //addOfflinePodcast()
        if (userPlaylistData != null && userPlaylistData?.data != null && userPlaylistData?.data?.body?.rows?.size!! > 0) {
            userPodcastList = userPlaylistData?.data?.body?.rows!!
        }

        if (!userPodcastList.isNullOrEmpty()){
            rvPodcastList.apply {
                layoutManager =
                    LinearLayoutManager(context,  LinearLayoutManager.VERTICAL, false)
                adapter = LibraryPodcastsAdapter(
                    context, userPodcastList,
                    object : LibraryPodcastsAdapter.OnChildItemClick {
                        override fun onUserClick(childPosition: Int) {

                            val bundle = Bundle()
                            bundle.putString("id",""+userPodcastList.get(childPosition)?.data?.id)
                            bundle.putString("image", ""+userPodcastList.get(childPosition)?.data?.image)
                            bundle.putString("playerType", ""+userPodcastList.get(childPosition)?.data?.type)
                            bundle.putBoolean("varient", false)
                            val podcastDetailsFragment = PodcastDetailsFragment()
                            podcastDetailsFragment.arguments = bundle
                            addFragment(
                                R.id.fl_container,
                                this@LibraryPodcastFragment,
                                podcastDetailsFragment,
                                false
                            )

                        }
                    }
                )

                setRecycledViewPool(RecyclerView.RecycledViewPool())
                setHasFixedSize(true)
            }
            rvPodcastList?.isNestedScrollingEnabled=false
            rvPodcastList.visibility = View.VISIBLE
            clExplore.visibility = View.GONE
        }else {
            if (!musicplayList.isNullOrEmpty() && musicplayList.size > 1 && userPodcastList.isNullOrEmpty()){
                rvPodcastList.visibility = View.GONE
                rvMusicPlaylist.visibility = View.VISIBLE
                clExplore.visibility = View.GONE
            }else{
                rvPodcastList.visibility = View.GONE
                rvMusicPlaylist.visibility = View.VISIBLE
                clExplore.visibility = View.VISIBLE
            }

        }

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
                CommonUtils.setPageBottomSpacing(rvPodcastList, requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
            }
        }
    }


}