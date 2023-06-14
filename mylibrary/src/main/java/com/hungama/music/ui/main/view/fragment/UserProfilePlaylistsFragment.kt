package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.UserProfileOtherUserProfilePlaylistsAdapter
import com.hungama.music.data.model.PlaylistRespModel
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.Constant
import kotlinx.android.synthetic.main.common_header_action_bar.*
import kotlinx.android.synthetic.main.fragment_user_profile_playlists.*


class UserProfilePlaylistsFragment(val userPlaylist: List<PlaylistRespModel.Data>) :
    BaseFragment(), BaseActivity.OnLocalBroadcastEventCallBack {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_profile_playlists, container, false)
    }

    override fun initializeComponent(view: View) {
        ivBack?.setOnClickListener { v -> backPress() }
        tvActionBarHeading?.text = getString(R.string.profile_str_4)
        setUpPlaylist()
        CommonUtils.setPageBottomSpacing(rvPlaylists, requireContext(),
            resources.getDimensionPixelSize(R.dimen.dimen_18), resources.getDimensionPixelSize(R.dimen.dimen_64),
            resources.getDimensionPixelSize(R.dimen.dimen_18), 0)
    }

    private fun setUpPlaylist() {
        if (userPlaylist != null && userPlaylist.isNotEmpty()){
            rvPlaylists.visibility = View.VISIBLE
            rvPlaylists.apply {
                layoutManager =
                    GridLayoutManager(context, 1)
                adapter = UserProfileOtherUserProfilePlaylistsAdapter(context, userPlaylist,
                    object : UserProfileOtherUserProfilePlaylistsAdapter.OnChildItemClick {
                        override fun onUserClick(childPosition: Int) {
                            val varient = 1

                            val playlistDetailFragment = MyPlaylistDetailFragment(varient,object :MyPlaylistDetailFragment.onBackPreesHendel{
                                override fun backPressItem(status: Boolean) {

                                }

                            })
                            val bundle = Bundle()
                            bundle.putString("image", userPlaylist?.get(childPosition)?.data?.image)
                            bundle.putString("id", userPlaylist?.get(childPosition)?.data?.id)
                            bundle.putString("playerType", Constant.MUSIC_PLAYER)
                            playlistDetailFragment.arguments = bundle
                            addFragment(R.id.fl_container, this@UserProfilePlaylistsFragment, playlistDetailFragment, false)
                        }
                    }, true)
                setRecycledViewPool(RecyclerView.RecycledViewPool())
                setHasFixedSize(true)
            }
        }else{
            rvPlaylists.visibility = View.GONE
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return false
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
                CommonUtils.setPageBottomSpacing(rvPlaylists, requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_18), resources.getDimensionPixelSize(R.dimen.dimen_64),
                    resources.getDimensionPixelSize(R.dimen.dimen_18), 0)
            }
        }
    }
}