package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.data.model.*
import com.hungama.music.utils.preference.PrefConstant
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.UserProfileOtherUserProfileMostHeardSongsAdapter
import com.hungama.music.ui.main.adapter.UserProfileOtherUserProfileMostListenedArtistsAdapter
import com.hungama.music.ui.main.adapter.UserProfileOtherUserProfilePlaylistsAdapter
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.applyButtonTheme
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.utils.CommonUtils.setLog
import kotlinx.android.synthetic.main.common_header_action_bar.*
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.fragment_user_profile_other_user_profile.*
import kotlinx.android.synthetic.main.fragment_user_profile_other_user_profile.btnFollow
import kotlinx.android.synthetic.main.fragment_user_profile_other_user_profile.ivUser
import kotlinx.android.synthetic.main.fragment_user_profile_other_user_profile.llEditProfile
import kotlinx.android.synthetic.main.fragment_user_profile_other_user_profile.llExploreNow
import kotlinx.android.synthetic.main.fragment_user_profile_other_user_profile.llFollowersCount
import kotlinx.android.synthetic.main.fragment_user_profile_other_user_profile.llFollowingCount
import kotlinx.android.synthetic.main.fragment_user_profile_other_user_profile.llHeaderPlaylist
import kotlinx.android.synthetic.main.fragment_user_profile_other_user_profile.llNoActivity
import kotlinx.android.synthetic.main.fragment_user_profile_other_user_profile.llPlaylist
import kotlinx.android.synthetic.main.fragment_user_profile_other_user_profile.llPlaylistCount
import kotlinx.android.synthetic.main.fragment_user_profile_other_user_profile.llSeeAll
import kotlinx.android.synthetic.main.fragment_user_profile_other_user_profile.rlShare
import kotlinx.android.synthetic.main.fragment_user_profile_other_user_profile.rvMostHeardSongs
import kotlinx.android.synthetic.main.fragment_user_profile_other_user_profile.rvMostListenedArtist
import kotlinx.android.synthetic.main.fragment_user_profile_other_user_profile.rvPlaylist
import kotlinx.android.synthetic.main.fragment_user_profile_other_user_profile.scrollView
import kotlinx.android.synthetic.main.fragment_user_profile_other_user_profile.tvFollowersCount
import kotlinx.android.synthetic.main.fragment_user_profile_other_user_profile.tvFollowingCount
import kotlinx.android.synthetic.main.fragment_user_profile_other_user_profile.tvPlayListCount
import org.json.JSONObject

//follow = 1 is current following this user
//follow = 0 is current not following this user
class UserProfileOtherUserProfileFragment(val otherUserId: String) : BaseFragment(),
    BaseActivity.OnLocalBroadcastEventCallBack {
    var userViewModel: UserViewModel? = null
    var userPlaylist: List<PlaylistRespModel.Data> = ArrayList()
    var isFollowing = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_profile_other_user_profile, container, false)
    }

    override fun initializeComponent(view: View) {
        applyButtonTheme(requireContext(), llEditProfile)
        applyButtonTheme(requireContext(), llExploreNow)
        ivBack?.setOnClickListener { v -> backPress() }
        llHeaderPlaylist.setOnClickListener(this)
        llSeeAll.setOnClickListener(this)
        llEditProfile.setOnClickListener(this)
        llExploreNow.setOnClickListener(this)
        rlShare.setOnClickListener(this)
        CommonUtils.setPageBottomSpacing(scrollView, requireContext(),
            resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
            resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
        CommonUtils.PageViewEvent("","","","",
            MainActivity.lastItemClicked,"user profile_other user profile","")
    }

    override fun onResume() {
        super.onResume()
        setUpViewModel(otherUserId)
        setLocalBroadcast()
    }

    private fun setUpLists(userPlaylistData: PlaylistRespModel) {
        if (userPlaylistData?.data != null && userPlaylistData?.data?.size!! > 0) {
            userPlaylist = userPlaylistData?.data!!
            tvPlayListCount.text = userPlaylistData?.count.toString()
            llPlaylistCount?.setOnClickListener(this)
            llPlaylist.visibility = View.VISIBLE
            var seeMore = true
            if (userPlaylist.size > 5) {
                seeMore = false
                llSeeAll.visibility = View.VISIBLE
            } else {
                llSeeAll.visibility = View.GONE
            }
            rvPlaylist.apply {
                layoutManager =
                    GridLayoutManager(context, 1)
                adapter =
                    UserProfileOtherUserProfilePlaylistsAdapter(context, userPlaylistData.data,
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
                                addFragment(R.id.fl_container, this@UserProfileOtherUserProfileFragment, playlistDetailFragment, false)
                            }
                        }, seeMore)
                setRecycledViewPool(RecyclerView.RecycledViewPool())
                setHasFixedSize(true)
            }
        }else{
            tvPlayListCount.text = "0"
            llPlaylist.visibility = View.GONE
            llNoActivity.visibility = View.VISIBLE
        }

        //llMostHeardSongs.visibility = View.VISIBLE
        rvMostHeardSongs.apply {
            layoutManager =
                GridLayoutManager(context, 5, GridLayoutManager.HORIZONTAL, false)
            adapter = UserProfileOtherUserProfileMostHeardSongsAdapter(context, ArrayList(),
                object : UserProfileOtherUserProfileMostHeardSongsAdapter.OnChildItemClick {
                    override fun onUserClick(childPosition: Int) {

                    }
                })
            setRecycledViewPool(RecyclerView.RecycledViewPool())
            setHasFixedSize(true)
        }

        //llMostListenedArtist.visibility = View.VISIBLE
        rvMostListenedArtist.apply {
            layoutManager =
                GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false)
            adapter = UserProfileOtherUserProfileMostListenedArtistsAdapter(context, ArrayList(),
                object : UserProfileOtherUserProfileMostListenedArtistsAdapter.OnChildItemClick {
                    override fun onUserClick(childPosition: Int) {

                    }
                })
            setRecycledViewPool(RecyclerView.RecycledViewPool())
            setHasFixedSize(true)
        }
    }

    override fun onClick(v: View) {
        super.onClick(v)
        if(v==llFollowersCount){
            //addFragment(R.id.fl_container, this, UserProfileFollowersFragment(otherUserId), false)
        }else if (v==llPlaylistCount){
            addFragment(R.id.fl_container, this, UserProfilePlaylistsFragment(userPlaylist), false)
        }else if (v==llFollowingCount){
            //addFragment(R.id.fl_container, this, UserProfileFollowingFragment(otherUserId, false), false)
        }else if (v==llHeaderPlaylist || v==llSeeAll){
            if (userPlaylist != null && userPlaylist.size > 5){
                addFragment(R.id.fl_container, this, UserProfilePlaylistsFragment(userPlaylist), false)
            }
        }else if (v==llEditProfile){
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), llEditProfile!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            setFollowUnFollow(otherUserId)
        }else if (v==llExploreNow){
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), llExploreNow!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            val backStackCount: Int = requireActivity().supportFragmentManager.getBackStackEntryCount()
            for (i in 0 until backStackCount) {

                // Get the back stack fragment id.
                val backStackId: Int = requireActivity().supportFragmentManager.getBackStackEntryAt(i).getId()
                requireActivity().supportFragmentManager.popBackStack(
                    backStackId,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
            }
            replaceFragment(R.id.fl_container, DiscoverMainTabFragment.newInstance(context, Bundle()), false, 0)
        }else if (v == rlShare){
            setLog(TAG, "onClick: isShareUserProfile:${isShareUserProfile} userProfileModel:${userProfileModel}")
            if(isShareUserProfile&&userProfileModel!=null&&!TextUtils.isEmpty(userProfileModel?.result?.get(0)?.share)){
                Utils.shareItem(requireActivity(),userProfileModel?.result?.get(0)?.share!!)
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    private fun setUpViewModel(otherUserId: String) {
        if (!TextUtils.isEmpty(SharedPrefHelper.getInstance().get(PrefConstant.USER_IMAGE,""))){
            ImageLoader.loadImage(requireContext(),ivUser, SharedPrefHelper.getInstance().get(
                PrefConstant.USER_IMAGE,""),R.drawable.profile_icon)
        }
        userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)


        getOtherUserProfile(otherUserId)

    }


    private fun getUserSocialData(otherUserId: String) {
        if (ConnectionUtil(requireContext()).isOnline) {
            userViewModel?.getUserSocialData(
                requireContext(), otherUserId
            )?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                fillUI(it?.data)
                            }

                        }

                        Status.LOADING ->{
                            setProgressBarVisible(true)
                        }

                        Status.ERROR ->{
                            setProgressBarVisible(false)
                            Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                        }
                    }
                })
        }else {
            val messageModel = MessageModel(getString(R.string.toast_message_5), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }

    private fun fillUI(userSocialData: UserSocialData) {
        if (userSocialData.followerCount!! > 0){
            tvFollowersCount.text = userSocialData.followerCount.toString()
            llFollowersCount?.setOnClickListener(this)
        }else{
            tvFollowersCount.text = "0"
        }

        if (userSocialData.followingCount!! > 0){
            tvFollowingCount.text = userSocialData.followingCount.toString()
            llFollowingCount?.setOnClickListener(this)
        }else{
            tvFollowingCount.text = "0"
        }

        for (follow in userSocialData.follower?.iterator()!!){
            if (SharedPrefHelper.getInstance().getUserId().equals(follow?.uId)){
                btnFollow?.text = getString(R.string.profile_str_5)
                ivFollow.setImageDrawable(requireContext().faDrawable(R.string.icon_tick, R.color.colorWhite))
                isFollowing = true
            }
        }
    }

    private fun getUserPlaylistData(otherUserId: String) {
        if (ConnectionUtil(requireContext()).isOnline) {
            userViewModel?.getUserPlaylistData(
                requireContext(), otherUserId
            )?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            getUserSocialData(otherUserId)
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
                        }
                    }
                })
        }else {
            val messageModel = MessageModel(getString(R.string.toast_message_5), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }


    private var userProfileModel: UserProfileModel?=null
    private fun getOtherUserProfile(otherUserId: String){
        if (ConnectionUtil(requireContext()).isOnline) {
            userViewModel?.getUserProfileData(
                requireContext(), otherUserId
            )?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                if (it?.data.statusCode == 200 && it?.data.result != null && it?.data?.result?.size!! > 0){

                                    userProfileModel=it?.data

                                    loadUserData(userProfileModel!!)
                                    getUserPlaylistData(userProfileModel?.result?.get(0)?.uId!!)

                                }

                            }
                        }

                        Status.LOADING ->{
                            setProgressBarVisible(true)
                        }

                        Status.ERROR ->{
                            setProgressBarVisible(false)
                        }
                    }
                })
        }else {
            val messageModel = MessageModel(getString(R.string.toast_message_5), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }

    private fun loadUserData(userProfileModel: UserProfileModel) {
        if(isShareUserProfile){
            rlShare?.visibility=View.VISIBLE
        }else{
            rlShare?.visibility=View.GONE
        }
        if (!TextUtils.isEmpty(userProfileModel?.result?.get(0)?.handleName)){
            tvActionBarHeading?.text = userProfileModel?.result?.get(0)?.handleName
        }else{
            tvActionBarHeading?.text = ""
        }

        if (userProfileModel?.result?.get(0)?.profileImage!! != null && !TextUtils.isEmpty(userProfileModel?.result?.get(0)?.profileImage!!.toString())){
            ImageLoader.loadImage(requireContext(),ivUser,userProfileModel?.result?.get(0)?.profileImage!!.toString(),R.drawable.ic_no_user_img)
        }else if (userProfileModel?.result?.get(0)?.alternateProfileImage!! != null && !TextUtils.isEmpty(userProfileModel?.result?.get(0)?.alternateProfileImage!!.toString())){
            ImageLoader.loadImage(requireContext(),ivUser,userProfileModel?.result?.get(0)?.alternateProfileImage!!.toString(),R.drawable.ic_no_user_img)
        }
        var username = ""
        if (!TextUtils.isEmpty(userProfileModel?.result?.get(0)?.firstName)){
            username += userProfileModel?.result?.get(0)?.firstName
        }
        if (!TextUtils.isEmpty(userProfileModel?.result?.get(0)?.lastName)){
            username += userProfileModel?.result?.get(0)?.lastName
        }

        if (!TextUtils.isEmpty(username)){
            tvUserName?.text = username
        }else{
            if (!TextUtils.isEmpty(userProfileModel?.result?.get(0)?.handleName)){
                tvUserName?.text = userProfileModel?.result?.get(0)?.handleName
            }else{
                tvUserName?.text = ""
            }
        }
    }


    private fun setFollowUnFollow(followingId: String) {
        if (ConnectionUtil(context).isOnline) {
            isFollowing = !isFollowing
            val jsonObject = JSONObject()
            jsonObject.put("followingId", followingId)
            jsonObject.put("follow", isFollowing)
            userViewModel?.followUnfollowSocial(requireContext(), jsonObject.toString())
            setFollowingStatus()
        } else {
            val messageModel = MessageModel(getString(R.string.toast_message_5), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }

    fun setFollowingStatus() {
        if (isFollowing) {
            btnFollow?.text = getString(R.string.profile_str_5)
            ivFollow.setImageDrawable(requireContext().faDrawable(R.string.icon_tick, R.color.colorWhite))
        }else{
            btnFollow?.text = getString(R.string.profile_str_2)
            ivFollow.setImageDrawable(requireContext().faDrawable(R.string.icon_follow, R.color.colorWhite))
        }
    }

    private fun setLocalBroadcast(){
        (requireActivity() as MainActivity).setLocalBroadcastEventCall(this, Constant.AUDIO_PLAYER_EVENT)
    }

    override fun onLocalBroadcastEventCallBack(context: Context?, intent: Intent) {
        if (isAdded){
            val event = intent.getIntExtra("EVENT", 0)
            if (event == Constant.AUDIO_PLAYER_RESULT_CODE) {
                CommonUtils.setPageBottomSpacing(scrollView, requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
            }
        }
    }
}