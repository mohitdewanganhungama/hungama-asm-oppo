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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import com.hungama.music.data.model.MessageModel
import com.hungama.music.data.model.MessageType
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.UserProfileFollowersAdapter
import com.hungama.music.data.model.UserSocialData
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.utils.ConnectionUtil
import com.hungama.music.utils.Utils
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.Constant
import kotlinx.android.synthetic.main.fragment_user_profile_followers.*
import kotlinx.android.synthetic.main.common_header_action_bar.tvActionBarHeading
import org.json.JSONObject


class UserProfileFollowersFragment(val userId: String) : BaseFragment(),
    BaseActivity.OnLocalBroadcastEventCallBack {
    var userViewModel:UserViewModel? = null
    var userSocialData: UserSocialData? = null
    var otherUserSocialData: UserSocialData? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_profile_followers, container, false)
    }

    override fun initializeComponent(view: View) {
        ivBack?.setOnClickListener { v -> backPress() }
        tvActionBarHeading?.text = getString(R.string.profile_str_3)
        shimmerLayout.visibility = View.VISIBLE
        shimmerLayout.startShimmer()
        setupUserViewModel()
        CommonUtils.setPageBottomSpacing(rvFollowers, requireContext(),
            resources.getDimensionPixelSize(R.dimen.dimen_18), resources.getDimensionPixelSize(R.dimen.dimen_64),
            resources.getDimensionPixelSize(R.dimen.dimen_6), 0)

        CommonUtils.PageViewEvent("","","","",
            "viewprofile","user profile_followers","")
    }

    private fun setUpOtherUserFollower(userSocialData1: UserSocialData) {
        if (userSocialData1 != null && userSocialData1?.follower != null && userSocialData1?.follower?.size!! > 0) {
            otherUserSocialData = userSocialData1
            if (otherUserSocialData?.follower?.size!! > 0){
                otherUserSocialData?.follower?.forEachIndexed { i, follower ->
                    if (userSocialData?.follower?.size!! > 0) {
                        userSocialData?.following?.forEachIndexed { k, following ->
                            if (follower?.uId.equals(following?.uId)) {
                                follower?.isAdded = true
                            }
                        }
                    }
                }
            }
            /*if (otherUserSocialData?.follower?.size!! > 0){
                for (i in otherUserSocialData?.follower?.indices!!){
                    if (userSocialData?.follower?.size!! > 0) {
                        for (k in userSocialData?.following?.indices!!){
                            if (otherUserSocialData?.follower?.get(i)?.uId.equals(userSocialData?.following?.get(k)?.uId)) {
                                otherUserSocialData?.follower?.get(i)?.isAdded = true
                            }
                        }
                    }
                }
            }*/

            rvFollowers?.visibility = View.VISIBLE
            rvFollowers.apply {
                layoutManager =
                    GridLayoutManager(context, 1)
                adapter = UserProfileFollowersAdapter(context, otherUserSocialData?.follower,
                    object : UserProfileFollowersAdapter.OnChildItemClick {
                        override fun onUserClick(childPosition: Int, isAddedClick: Boolean) {
                            if (isAddedClick){
                                setFollowUnFollow(otherUserSocialData?.follower?.get(childPosition)?.uId!!, otherUserSocialData?.follower?.get(childPosition)?.isAdded!!)
                            }else{
                                addFragment(R.id.fl_container, this@UserProfileFollowersFragment, UserProfileOtherUserProfileFragment(otherUserSocialData?.follower?.get(childPosition)?.uId!!), false)
                            }
                        }
                    })
                setRecycledViewPool(RecyclerView.RecycledViewPool())
                setHasFixedSize(true)
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return false
    }



    private fun setupUserViewModel() {
        userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)


        getUserSocialData()
    }

    private fun getUserSocialData() {
        if (ConnectionUtil(requireContext()).isOnline) {
            userViewModel?.getUserSocialData(
                requireContext(),
                SharedPrefHelper?.getInstance()?.getUserId()!!
            )?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                setUpFollower(it?.data!!)
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
        }else{
            val messageModel = MessageModel(getString(R.string.toast_message_5), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }

    private fun getOtherUserSocialData() {
        if (ConnectionUtil(requireContext()).isOnline) {

            userViewModel?.getUserSocialData(
                requireContext(),
                userId
            )?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                setUpOtherUserFollower(it?.data)
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
        }else{
            val messageModel = MessageModel(getString(R.string.toast_message_5), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }


    private fun setUpFollower(userSocialData1: UserSocialData) {
        if (userSocialData1 != null && userSocialData1?.follower != null && userSocialData1?.follower?.size!! > 0) {
            userSocialData = userSocialData1
            getOtherUserSocialData()
            shimmerLayout.visibility = View.GONE
            shimmerLayout.stopShimmer()
        }

    }
    private fun setFollowUnFollow(followingId: String, isFollowing:Boolean){
        if (ConnectionUtil(context).isOnline) {
            val jsonObject = JSONObject()
            jsonObject.put("followingId",followingId)
            jsonObject.put("follow",isFollowing)
            userViewModel?.followUnfollowSocial(requireContext(), jsonObject.toString())
        } else {
            val messageModel = MessageModel(getString(R.string.toast_message_5), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
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
                CommonUtils.setPageBottomSpacing(rvFollowers, requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_18), resources.getDimensionPixelSize(R.dimen.dimen_64),
                    resources.getDimensionPixelSize(R.dimen.dimen_6), 0)
            }
        }
    }
}