package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.data.model.*
import com.hungama.music.player.audioplayer.TracksContract
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.viewmodel.TracksViewModel
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.FollowPodcastAdapter
import com.hungama.music.player.audioplayer.Injection
import com.hungama.music.utils.ConnectionUtil
import com.hungama.music.utils.Constant
import com.hungama.music.utils.Utils
import com.hungama.music.ui.main.viewmodel.PlayableContentViewModel
import com.hungama.music.ui.main.viewmodel.PlaylistViewModel
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import kotlinx.android.synthetic.main.fr_add_song_playlist.*
import kotlinx.android.synthetic.main.fr_add_song_playlist.rvRecomandedSong
import kotlinx.android.synthetic.main.fr_my_playlist_detail.*
import kotlinx.android.synthetic.main.header_main.*
import kotlinx.android.synthetic.main.header_main.etSearch
import org.json.JSONObject


/**
 * A simple [Fragment] subclass.
 * Use the [FollowMorePodcastFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FollowMorePodcastFragment : BaseFragment(), TracksContract.View,
    ViewTreeObserver.OnScrollChangedListener, BaseActivity.OnLocalBroadcastEventCallBack {
    var playerType: String? = null
    var addSongItemPosition = 0
    var playableItemPosition = 0


    var recommendedSongList = java.util.ArrayList<RecommendedSongListRespModel.Data.Body.Similar>()
    var filterSongList = java.util.ArrayList<RecommendedSongListRespModel.Data.Body.Similar>()

    var playableContentViewModel: PlayableContentViewModel = PlayableContentViewModel()
    var playlistListViewModel: PlaylistViewModel? = null
    var userViewModel: UserViewModel? = null

    private lateinit var tracksViewModel: TracksContract.Presenter


    private var recommendedSongsAdapter: FollowPodcastAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fr_add_song_playlist, container, false)
    }

    override fun initializeComponent(view: View) {
        playerType = requireArguments().getString("playerType").toString()

        tvActionBarHeading.setText("Follow Podcasts")

        tracksViewModel = TracksViewModel(Injection.provideTrackRepository(), this)
        setUpPlaylistDetailListViewModel()
        CommonUtils.setPageBottomSpacing(rlMain, requireContext(),
            resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
            resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
    }

    override fun onClick(v: View) {
        super.onClick(v)

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

        userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)

        if (ConnectionUtil(context).isOnline) {
            playlistListViewModel?.getFollowPodcastList(requireContext())?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null) {

                                if (!it.data.data.body.rows.isNullOrEmpty()) {
                                    rvRecomandedSong.visibility = View.VISIBLE
                                    recommendedSongList = it.data.data.body.rows

                                    recommendedSongsAdapter =
                                        FollowPodcastAdapter(requireContext(), recommendedSongList,
                                            object : FollowPodcastAdapter.OnChildItemClick {
                                                override fun onPlaySongClick(childPosition: Int) {

                                                }

                                                override fun onAddSongClick(childPosition: Int) {
                                                    addSongItemPosition = childPosition
                                                    callFollowPodcast(recommendedSongList.get(addSongItemPosition))
                                                }
                                            })

                                    rvRecomandedSong.apply {
                                        layoutManager =
                                            GridLayoutManager(context, 1, GridLayoutManager.VERTICAL, false)
                                        adapter = recommendedSongsAdapter
                                        setRecycledViewPool(RecyclerView.RecycledViewPool())
                                        setHasFixedSize(true)
                                    }
                                    rvRecomandedSong.setPadding(0, 0, 0, 0)
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
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }


        etSearch?.doOnTextChanged { text, start, before, count ->
            val filterList: java.util.ArrayList<RecommendedSongListRespModel.Data.Body.Similar> =
                performFiltering(text?.toString()!!)
            if (recommendedSongsAdapter != null) {
                recommendedSongsAdapter?.refreshList(filterList)
            }
        }
    }



    private fun callFollowPodcast(model: RecommendedSongListRespModel.Data.Body.Similar) {
        if (ConnectionUtil(context).isOnline) {
            val jsonObject1 = JSONObject()
            jsonObject1.put("contentId",model?.data?.id)
            jsonObject1.put("typeId",""+model?.data?.type)
            jsonObject1.put("action",true)
            jsonObject1.put("module",Constant.MODULE_FOLLOW)
            userViewModel?.followUnfollowModule(requireContext(), jsonObject1.toString())


            if (addSongItemPosition <= recommendedSongList.size) {

                if(filterSongList!=null&&filterSongList.size>0){
                    val item=filterSongList?.get(addSongItemPosition)
                    filterSongList?.removeAt(addSongItemPosition)
                    recommendedSongList?.remove(item)
                }else{
                    recommendedSongList?.removeAt(addSongItemPosition)
                }


                rvRecomandedSong?.adapter?.notifyDataSetChanged()
            }
        } else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
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
                                    setPlayableContentListData(it?.data!!)
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
        if (!playableItem?.get(position)?.data?.misc?.movierights.isNullOrEmpty()){
            track.movierights = playableItem?.get(position)?.data?.misc?.movierights.toString()
        }else{
            track.movierights = ""
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

        track.pType = DetailPages.PODCAST_DETAIL_PAGE.value
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
        filterSongList.clear()
        if (constraint.length == 0 && TextUtils.isEmpty(constraint)) {
            return recommendedSongList
        } else {
            if (recommendedSongList != null && !TextUtils.isEmpty(constraint)) {
                val filterPattern = constraint.toString().toLowerCase().trim { it <= ' ' }
                for (model in recommendedSongList) {
                    if (model != null && model?.data?.title != null) {
                        if (model.data.title.toLowerCase().trim()
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
}