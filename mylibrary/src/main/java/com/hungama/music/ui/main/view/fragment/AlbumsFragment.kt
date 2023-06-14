package com.hungama.music.ui.main.view.fragment

import android.os.Bundle
import android.view.*
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.R
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.data.model.BookmarkDataModel
import com.hungama.music.data.model.MessageModel
import com.hungama.music.data.model.MessageType
import com.hungama.music.data.model.PlaylistRespModel
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.home.eventreporter.UserAttributeEvent
import com.hungama.music.ui.main.adapter.AlbumsAdapter
import com.hungama.music.utils.ConnectionUtil
import com.hungama.music.utils.Constant
import com.hungama.music.utils.Utils
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.customview.downloadmanager.model.DownloadedAudio
import kotlinx.android.synthetic.main.fr_library_albums.*
import kotlinx.coroutines.launch


class AlbumsFragment : BaseFragment() {

    var userViewModel: UserViewModel? = null
    var userAlbumsList: ArrayList<BookmarkDataModel.Data.Body.Row> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fr_library_albums, container, false)
    }

    override fun onResume() {
        super.onResume()
        setUpViewModel()
    }

    override fun initializeComponent(view: View) {
        CommonUtils.applyButtonTheme(requireContext(), btnExplore)
        btnExplore?.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        super.onClick(v)
        if (v == btnExplore) {
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


    private fun setUpViewModel() {
        userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)
        getUserAlbumsData()
    }

    private fun getUserAlbumsData() {
        baseMainScope.launch {
            CommonUtils.setLog(
                "isGotoDownloadClicked",
                "AlbumsFragment-getUserAlbumsData-isGotoDownloadClicked-${Constant.ISGOTODOWNLOADCLICKED}"
            )
            if (ConnectionUtil(requireContext()).isOnline) {
                userViewModel?.getUserBookmarkedDataWithFilter(
                    requireContext(),
                    Constant.MODULE_FAVORITE,
                    "1"
                )?.observe(this@AlbumsFragment,
                    {
                        when(it.status){
                            Status.SUCCESS->{
                                setProgressBarVisible(false)
                                if (it?.data != null) {
                                    setUpLists(it.data)
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
                setUpLists(null)
            }
        }

    }

    private fun addOfflineAlbum() {
        baseMainScope.launch {
            val albumListoffline = AppDatabase.getInstance()?.downloadedAudio()
                ?.getAlbumList() as ArrayList<DownloadedAudio>
            if (!albumListoffline.isNullOrEmpty()) {
                albumListoffline.forEach {
                    val albumData = BookmarkDataModel.Data.Body.Row()
                    albumData.data.id = "" + it.parentId
                    albumData.data.title = "" + it.pName
                    albumData.data.image = "" + it.pImage
                    albumData.data.subtitle = "" + it.f_fav_count
                    albumData.data.misc.favCount = it.f_fav_count!!
                    userAlbumsList.add(albumData)
                }
            }
        }

    }

    private fun setUpLists(userPlaylistData: BookmarkDataModel?) {
        userAlbumsList = ArrayList()
        addOfflineAlbum()
        if (userPlaylistData != null && !userPlaylistData.data.body.rows.isNullOrEmpty()) {
            userAlbumsList.addAll(userPlaylistData.data.body.rows)
            callFavAlbumEvent(userAlbumsList)
        }

        if (!userAlbumsList.isNullOrEmpty()){
            rvMusicPlaylist.apply {
                layoutManager =
                    GridLayoutManager(context, 1)
                adapter = AlbumsAdapter(
                    context, userAlbumsList,
                    object : AlbumsAdapter.OnChildItemClick {
                        override fun onUserClick(childPosition: Int) {
                            val bundle = Bundle()
                            bundle.putString("id",""+ userAlbumsList[childPosition].data.id)
                            bundle.putString("image", ""+ userAlbumsList[childPosition].data.image)
                            bundle.putString("playerType", ""+ userAlbumsList[childPosition].data.type)
                            val albumDetailFragment = AlbumDetailFragment()
                            albumDetailFragment.arguments = bundle
                            addFragment(
                                R.id.fl_container,
                                this@AlbumsFragment,
                                albumDetailFragment,
                                false
                            )

                        }
                    }
                )
                setRecycledViewPool(RecyclerView.RecycledViewPool())
                setHasFixedSize(true)
            }
            rvMusicPlaylist.visibility = View.VISIBLE
            clExplore.visibility = View.GONE
        }else {
            rvMusicPlaylist.visibility = View.GONE
            clExplore.visibility = View.VISIBLE
        }

    }

    private fun callFavAlbumEvent(arrayList: java.util.ArrayList<BookmarkDataModel.Data.Body.Row>) {
        val list=ArrayList<String>()
        arrayList.forEach {
            list.add(it.data.title!!)
        }

        val userDataMap= java.util.HashMap<String, String>()
        userDataMap.put(EventConstant.FAVOURITED_ALBUM,Utils.arrayToString(list))
        userDataMap.put(EventConstant.NUMBER_OF_FAVORITED_ALBUMS,""+ list.size)
        EventManager.getInstance().sendUserAttribute(UserAttributeEvent(userDataMap))
    }

}