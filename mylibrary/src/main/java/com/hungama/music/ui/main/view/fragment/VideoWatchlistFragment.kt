package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.hungama.music.R
import com.hungama.music.data.model.BookmarkCountRespModel
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.data.model.LibraryMusicModel
import com.hungama.music.data.model.MessageModel
import com.hungama.music.data.model.MessageType
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.main.adapter.LibraryVideoAdapter
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.*
import kotlinx.android.synthetic.main.common_header_action_bar.*
import kotlinx.android.synthetic.main.fr_video_download_all.*
import kotlinx.android.synthetic.main.fr_video_watchlist.*
import kotlinx.android.synthetic.main.fr_video_watchlist.clExplore
import kotlinx.android.synthetic.main.fr_video_watchlist.downloadSearch
import kotlinx.android.synthetic.main.fr_video_watchlist.rvMusicPlaylist
import kotlinx.coroutines.launch

class VideoWatchlistFragment : BaseFragment(), VideoWatchlistItemFragment.WatchlisItemRefresh,
    BaseActivity.OnLocalBroadcastEventCallBack, TextWatcher {

    companion object{
        val MOVIE_ID=101
        val TV_SHOW=102
        val SHORT_VIDEO=103
        val SHORT_FILMS = 104
    }

    private lateinit var musicLibarayAdapter: LibraryVideoAdapter
    private var musicplayList = ArrayList<LibraryMusicModel>()
    var userViewModel: UserViewModel? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fr_video_watchlist, container, false)
    }

    override fun initializeComponent(view: View) {
        CommonUtils.applyButtonTheme(requireContext(), btnExplore)
        rvMusicPlaylist.layoutManager =
            LinearLayoutManager(requireContext()!!, LinearLayoutManager.VERTICAL, false)

        musicLibarayAdapter = LibraryVideoAdapter(requireContext()!!, musicplayList,object : LibraryVideoAdapter.PlayListItemClick{
            override fun libraryItemOnClick(musicData: LibraryMusicModel) {
                val fragment= musicData?.id?.toInt()
                    ?.let { VideoWatchlistItemFragment.newInstance(it) }
                fragment?.addRefreshItemListiner(this@VideoWatchlistFragment)
                fragment?.let {
                    addFragment(R.id.fl_container,this@VideoWatchlistFragment,
                        it,false)
                }
            }
        })
        rvMusicPlaylist.adapter = musicLibarayAdapter


        btnExplore?.setOnClickListener {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), btnExplore!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            (activity as MainActivity).applyScreen(2)
        }

        CommonUtils.setPageBottomSpacing(rvMusicPlaylist, requireContext(),
            resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
            resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
        downloadSearch?.addTextChangedListener(this)
    }

    private fun setUpBookmarkModel() {
        baseMainScope.launch {
            CommonUtils.setLog(
                "isGotoDownloadClicked",
                "VideoWatchlistFragment-setUpBookmarkModel-isGotoDownloadClicked-${Constant.ISGOTODOWNLOADCLICKED}"
            )

            if (ConnectionUtil(requireContext()).isOnline) {
                userViewModel = ViewModelProvider(
                    this@VideoWatchlistFragment
                ).get(UserViewModel::class.java)
                if (isAdded){
                    getWatchlistCall()
                }
            }else{
                setEmptyVisible(false)
                setProgressBarVisible(false)
                val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                    MessageType.NEGATIVE, true)
                CommonUtils.showToast(requireContext(), messageModel)
            }
        }

    }

    fun getWatchlistCall(){
        baseMainScope.launch {
            userViewModel?.getBookmarkCountTypeWise(requireActivity(), Constant.MODULE_WATCHLIST)?.observe(this@VideoWatchlistFragment,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                updateList(it.data)
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
        }

    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return true
    }


    override fun onReloadItem() {
        if (isAdded){
            getWatchlistCall()
        }
    }

    private fun updateList(data: BookmarkCountRespModel?) {
        try {
            var tvShowCount=0
            var movieCount = 0
            var shortFilmCount = 0
            var movieSubTitle="0 " + getString(R.string.download_str_18)
            var tvShowSubTitle="0 " + getString(R.string.download_str_19)
            var shortFilmSubTitle="0 " + getString(R.string.library_video_str_6)
            musicplayList = ArrayList()
            data?.data?.forEach {
                if(it.type ==4){
                    movieCount = it.count
                }else if(it.type ==93){
                    shortFilmCount = it.count
                }else if(it.type ==96 || it.type ==97 || it.type ==98){
                    tvShowCount += it.count
                }

            }
            if (movieCount > 0){
                movieSubTitle= "$movieCount " + getString(R.string.library_video_str_4)
                if (!musicplayList.isNullOrEmpty() && musicplayList.size > 0){
                    musicplayList.get(0).SubTitle = movieSubTitle
                }else{
                    musicplayList.add(
                        LibraryMusicModel(
                            ""+
                                    MOVIE_ID,
                            getString(R.string.library_video_str_4),
                            movieSubTitle, "", ""
                        )
                    )
                }

            }else{
                if (!musicplayList.isNullOrEmpty() && musicplayList.size > 0){
                    musicplayList.removeAt(0)
                }
            }

            if(tvShowCount>0){
                tvShowSubTitle= "$tvShowCount " + getString(R.string.library_video_str_5)
                if (!musicplayList.isNullOrEmpty() && musicplayList.size > 1){
                    musicplayList.get(1).SubTitle = tvShowSubTitle
                }else{
                    musicplayList.add(
                        LibraryMusicModel(
                            ""+
                                    TV_SHOW,
                            getString(R.string.library_video_str_5),
                            tvShowSubTitle, "", ""
                        )
                    )
                }
            }else{
                if (!musicplayList.isNullOrEmpty() && musicplayList.size > 1){
                    musicplayList.removeAt(1)
                }
            }


            if (shortFilmCount > 0){
                shortFilmSubTitle= "$shortFilmCount " + getString(R.string.library_video_str_6)
                if (!musicplayList.isNullOrEmpty() && musicplayList.size > 2){
                    musicplayList.get(2).SubTitle = shortFilmSubTitle
                }else{
                    musicplayList.add(
                        LibraryMusicModel(
                            ""+
                                    SHORT_FILMS,
                            getString(R.string.library_video_str_6),
                            shortFilmSubTitle, "", ""
                        )
                    )
                }
            }else{
                if (!musicplayList.isNullOrEmpty() && musicplayList.size > 2){
                    musicplayList.removeAt(2)
                }
            }
            if (!musicplayList.isNullOrEmpty()){
                clExplore.hide()
                clWhatchlist.show()
                musicLibarayAdapter.refreshData(musicplayList)
            }else{
                clExplore.show()
                clWhatchlist.hide()
            }
        }catch (e:Exception){

        }
    }

    override fun onResume() {
        super.onResume()
        setLocalBroadcast()
        setUpBookmarkModel()
    }
    private fun setLocalBroadcast(){
        (requireActivity() as MainActivity).setLocalBroadcastEventCall(this, Constant.AUDIO_PLAYER_EVENT)
    }

    override fun onLocalBroadcastEventCallBack(context: Context?, intent: Intent) {
        if (isAdded){
            val event = intent.getIntExtra("EVENT", 0)
            if (event == Constant.AUDIO_PLAYER_RESULT_CODE) {
                CommonUtils.setPageBottomSpacing(rvMusicPlaylist, requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
            }
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }

    override fun afterTextChanged(s: Editable?) {
        val searchText = downloadSearch?.text.toString().trim()
        musicLibarayAdapter?.filter?.filter(searchText)
    }
}