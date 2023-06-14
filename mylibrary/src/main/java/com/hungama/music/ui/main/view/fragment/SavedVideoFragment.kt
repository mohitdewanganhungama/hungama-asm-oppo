package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.hungama.music.R
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.adapter.SavedVideoAdapter
import com.hungama.music.data.model.BookmarkDataModel
import com.hungama.music.data.model.MessageModel
import com.hungama.music.data.model.MessageType
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.utils.ConnectionUtil
import com.hungama.music.utils.Constant
import com.hungama.music.utils.Utils
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.preference.SharedPrefHelper
import kotlinx.android.synthetic.main.fr_saved_video.*
import kotlinx.coroutines.launch

class SavedVideoFragment : BaseFragment(), BaseActivity.OnLocalBroadcastEventCallBack {

    private var musicLibarayAdapter: SavedVideoAdapter?=null
    private var musicvideoList = ArrayList<BookmarkDataModel.Data.Body.Row>()
    var userViewModel: UserViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fr_saved_video, container, false)
    }

    override fun initializeComponent(view: View) {
        CommonUtils.applyButtonTheme(requireContext(), btnExplore)
        btnExplore.setOnClickListener{
            val bundle = Bundle()
            bundle.putBoolean(Constant.isTabSelection, true)
            bundle.putString(Constant.tabName, "shows")
            (activity as MainActivity).applyScreen(2, bundle)
        }
        setUpBookmarkModel()
        CommonUtils.setPageBottomSpacing(rvMusicPlaylist, requireContext(),
            resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_5),
            resources.getDimensionPixelSize(R.dimen.dimen_0), 0)

        }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return true
    }

    private fun setUpBookmarkModel() {
        baseMainScope.launch {
            CommonUtils.setLog(
                "isGotoDownloadClicked",
                "SavedVideoFragment-setUpBookmarkModel-isGotoDownloadClicked-${Constant.ISGOTODOWNLOADCLICKED}"
            )
            if (ConnectionUtil(requireContext()).isOnline) {
                userViewModel = ViewModelProvider(
                    this@SavedVideoFragment
                ).get(UserViewModel::class.java)

                baseMainScope.launch {
                    userViewModel?.getUserBookmarkedDataWithFilter(requireContext(),
                        Constant.MODULE_WATCHLIST,
                        "88888,22,53")?.observe(this@SavedVideoFragment,
                        Observer {
                            when(it.status){
                                Status.SUCCESS->{
                                    setProgressBarVisible(false)
                                    if (it?.data != null) {
                                        musicvideoList=it?.data?.data?.body?.rows!!
                                        setLog(TAG, "RespObserver musicvideoList: ${musicvideoList?.size}")
                                        if(musicvideoList.size>0){
                                            rvMusicPlaylist.layoutManager =
                                                LinearLayoutManager(requireContext()!!, LinearLayoutManager.VERTICAL, false)
                                            musicLibarayAdapter = SavedVideoAdapter(
                                                requireContext()!!,
                                                musicvideoList,
                                                object : SavedVideoAdapter.onItemClickListener {
                                                    override fun OnTvShowItemClick(adapterPosition: Int) {

                                                        if (activity != null && activity is MainActivity){
                                                            (activity as MainActivity).setPauseMusicPlayerOnVideoPlay()
                                                        }
                                                        val bundle = Bundle()
                                                        bundle.putString("id", musicvideoList?.get(adapterPosition)?.data?.id)
                                                        val videoDetailsFragment = MusicVideoDetailsFragment()
                                                        videoDetailsFragment.arguments = bundle
                                                        addFragment(R.id.fl_container, this@SavedVideoFragment, videoDetailsFragment, false)
                                                    }

                                                    override fun OnLibraryItemThreeDotClick(adapterPosition: Int) {
                                                        var moviePauseDialog = WatchlistThreeDotDialog(adapterPosition,musicvideoList?.get(adapterPosition),object : WatchlistThreeDotDialog.WatchlistThreeDotListener{
                                                            override fun removeFromMyList(
                                                                modelItem: BookmarkDataModel.Data.Body.Row?,
                                                                position: Int
                                                            ) {
                                                                setAddOrRemoveWatchlist(modelItem?.data?.id,""+ modelItem?.data?.type,false,Constant.MODULE_WATCHLIST)
                                                                musicvideoList?.remove(modelItem)
                                                                musicLibarayAdapter?.notifyItemRemoved(position)
                                                            }

                                                            override fun download(
                                                                modelItem: BookmarkDataModel.Data.Body.Row?,
                                                                position: Int
                                                            ) {

                                                            }

                                                            override fun share(
                                                                modelItem: BookmarkDataModel.Data.Body.Row?,
                                                                position: Int
                                                            ) {
                                                                Utils.shareItem(context!!,modelItem!!.data!!.misc!!.share)
                                                            }

                                                            override fun postAsStory() {

                                                            }

                                                            override fun cancel() {

                                                            }

                                                        },2)
                                                        moviePauseDialog.show(
                                                            activity?.supportFragmentManager!!,
                                                            "movie pause dialog show"
                                                        )
                                                    }

                                                })
                                            rvMusicPlaylist?.adapter=musicLibarayAdapter
                                            musicLibarayAdapter?.refreshData(musicvideoList)
                                            setEmptyView(false)
                                        }else{
                                            setEmptyView(true)
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
                }


            }else{
                setEmptyVisible(false)
                setProgressBarVisible(false)
                val messageModel = MessageModel(getString(R.string.toast_message_5), getString(R.string.toast_message_5),
                    MessageType.NEGATIVE, true)
                CommonUtils.showToast(requireContext(), messageModel)
            }
        }

    }


    fun setEmptyView(isEmptyShow:Boolean){
        if(isEmptyShow){
            clExplore?.visibility=View.VISIBLE
            clSaved?.visibility=View.GONE
        }else{
            clSaved?.visibility=View.VISIBLE
            clExplore?.visibility=View.GONE
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
                CommonUtils.setPageBottomSpacing(rvMusicPlaylist, requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_5),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
            }
        }
    }
}