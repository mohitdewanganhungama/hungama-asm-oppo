package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.data.model.BookmarkDataModel
import com.hungama.music.data.model.MessageModel
import com.hungama.music.data.model.MessageType
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.home.eventreporter.UserAttributeEvent
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.main.adapter.RadioAdapter
import com.hungama.music.utils.ConnectionUtil
import com.hungama.music.utils.Constant
import com.hungama.music.utils.Utils
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.CommonUtils
import kotlinx.android.synthetic.main.fr_library_artist.*
import kotlinx.coroutines.launch


class ArtistFragment : BaseFragment(), BaseActivity.OnLocalBroadcastEventCallBack {


    var userViewModel: UserViewModel? = null
    var userAlbumsList: ArrayList<BookmarkDataModel.Data.Body.Row> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fr_library_artist, container, false)
    }

    override fun onResume() {
        super.onResume()
        setUpViewModel()
        setLocalBroadcast()
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
            val bundle = Bundle()
            bundle.putBoolean(Constant.isTabSelection, true)
            bundle.putString(Constant.tabName, "")
            (activity as MainActivity).applyScreen(0,bundle)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return true
    }



    private fun getData() {
        baseMainScope.launch {
            if (ConnectionUtil(requireContext()).isOnline) {
                userViewModel?.getFollwingWithFilter(
                    requireContext(),
                    Constant.MODULE_FOLLOW,
                    "0"
                )?.observe(this@ArtistFragment,
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
                            }
                        }
                    })
            } else {
                setEmptyVisible(false)
                setProgressBarVisible(false)
                val messageModel = MessageModel(getString(R.string.toast_message_5), getString(R.string.toast_message_5),
                    MessageType.NEGATIVE, true)
                CommonUtils.showToast(requireContext(), messageModel)
            }
        }

    }
    private fun setUpViewModel() {
        userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)

        getData()
    }





    private fun setUpLists(userPlaylistData: BookmarkDataModel) {
        if (userPlaylistData.data.body.rows.size > 0) {
            userAlbumsList = userPlaylistData.data.body.rows

            callFavArtistEvent(userAlbumsList)


            rvMusicPlaylist.apply {
                layoutManager =
                    GridLayoutManager(context, 1)
                adapter = RadioAdapter(
                    context, userAlbumsList,
                    object : RadioAdapter.OnChildItemClick {
                        override fun onUserClick(childPosition: Int) {

                            val bundle = Bundle()
                            bundle.putString("id",""+ userAlbumsList[childPosition].data.id)
                            bundle.putString("image", ""+ userAlbumsList[childPosition].data.image)
                            bundle.putString("playerType", ""+ userAlbumsList[childPosition].data.type)
                            bundle.putBoolean("varient", false)
                            val albumDetailFragment = ArtistDetailsFragment()
                            albumDetailFragment.arguments = bundle
                            addFragment(
                                R.id.fl_container,
                                this@ArtistFragment,
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
        } else {
            rvMusicPlaylist.visibility = View.GONE
            clExplore.visibility = View.VISIBLE
        }

    }

    private fun callFavArtistEvent(arrayList: java.util.ArrayList<BookmarkDataModel.Data.Body.Row>) {
        val artistName=ArrayList<String>()
        arrayList.forEach {
            artistName.add(it.data.title!!)
        }

        val userDataMap= java.util.HashMap<String, String>()
        userDataMap[EventConstant.FAVOURITED_ARTIST] =  Utils.arrayToString(artistName)
        userDataMap[EventConstant.NUMBER_OF_FAVORITED_ARTISTS] = ""+ artistName.size
        EventManager.getInstance().sendUserAttribute(UserAttributeEvent(userDataMap))
    }

    private fun setLocalBroadcast(){
        (requireActivity() as MainActivity).setLocalBroadcastEventCall(this, Constant.AUDIO_PLAYER_EVENT)
    }

    override fun onLocalBroadcastEventCallBack(context: Context?, intent: Intent) {
        if (isAdded){
            val event = intent.getIntExtra("EVENT", 0)
            if (event == Constant.AUDIO_PLAYER_RESULT_CODE) {
                CommonUtils.setPageBottomSpacing(rvMusicPlaylist, requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_180),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
            }
        }
    }

}