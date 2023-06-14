package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.*
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.UserProfileFindFriendsAdapter
import com.hungama.music.data.model.FacebookListModel
import com.hungama.music.data.model.MessageModel
import com.hungama.music.data.model.MessageType
import com.hungama.music.data.model.UserSocialData
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.utils.ConnectionUtil
import com.hungama.music.utils.Utils
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.Constant
import kotlinx.android.synthetic.main.common_header_action_bar.*
import kotlinx.android.synthetic.main.fragment_user_profile_find_friends.*
import org.json.JSONArray
import org.json.JSONObject


class UserProfileFindFriendsFragment(val facebookListModel: FacebookListModel) : BaseFragment(),
    BaseActivity.OnLocalBroadcastEventCallBack {
    var userViewModel: UserViewModel? = null
    var userSocialData: UserSocialData? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_profile_find_friends, container, false)
    }

    override fun initializeComponent(view: View) {
        ivBack?.setOnClickListener { v -> backPress() }
        tvActionBarHeading?.text = getString(R.string.profile_str_12)
        if (facebookListModel != null && facebookListModel.data != null && facebookListModel.data?.size!! > 0){
            setupUserViewModel()
        }

        CommonUtils.setPageBottomSpacing(scrollView, requireContext(),
            resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
            resources.getDimensionPixelSize(R.dimen.dimen_0), 0)

        CommonUtils.PageViewEvent("","","","",
            MainActivity.lastItemClicked,"user profile_find friends","")
    }

    private fun setUpFollower(
        userSocialData: UserSocialData,
        data: List<List<FacebookListModel.Data?>?>
    ) {
        if (data != null && data.size > 0){
            if (userSocialData != null && userSocialData?.following != null && userSocialData?.following?.size!! > 0) {
                this.userSocialData = userSocialData

                if (data.size!! > 0){
                    var counts = 0
                    data.forEachIndexed { i, item ->
                        if (userSocialData?.following?.size!! > 0) {
                            userSocialData?.following?.forEachIndexed { k, following ->
                                if (item?.size!! > 0 && item?.get(0)?.uId.equals(following?.uId)) {
                                    item?.get(0)?.isAdded = true

                                }
                            }
                        }
                        if (item?.size!! > 0 && !TextUtils.isEmpty(item?.get(0)?.uId)) {
                            counts++
                            counts = counts
                        }
                    }
                    tvFollowAll.text = getString(R.string.profile_str_42) + " (" + counts + ")"
                    if(counts > 0){
                        followAll.visibility = View.VISIBLE
                        followAll.setOnClickListener(this)
                    }else{
                        followAll.visibility = View.VISIBLE
                    }

                }
            }

            rvFindFriend.apply {
                layoutManager =
                    GridLayoutManager(context, 1)
                adapter = UserProfileFindFriendsAdapter(context, data,
                    object : UserProfileFindFriendsAdapter.OnChildItemClick {
                        override fun onUserClick(childPosition: Int, isAddedClick: Boolean) {
                            if (isAddedClick){
                                setFollowUnFollow(data.get(childPosition)?.get(0)?.uId!!, data.get(childPosition)?.get(0)?.isAdded!!)
                            }else{
                                addFragment(R.id.fl_container, this@UserProfileFindFriendsFragment, UserProfileOtherUserProfileFragment(data.get(childPosition)?.get(0)?.uId!!), false)
                            }
                        }
                    })
                setRecycledViewPool(RecyclerView.RecycledViewPool())
                setHasFixedSize(true)
            }
        }else{
            followAll.visibility = View.VISIBLE
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
                            if (it != null) {
                                setUpFollower(it?.data!!, facebookListModel?.data!!)
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

    override fun onClick(v: View) {
        super.onClick(v)
        if (v==followAll){
            var ids = mutableListOf<String>()
            for (id in facebookListModel?.data?.listIterator()!!){
                if (!TextUtils.isEmpty(id?.get(0)?.uId!!)){
                    ids.add(id?.get(0)?.uId!!.toString())
                    id?.get(0)?.isAdded = true
                }
            }

            //followAll.visibility = View.GONE
            setFollowAll(ids, true)
        }
    }

    private fun setFollowAll(followingId: MutableList<String>, isFollowing:Boolean){
        if (ConnectionUtil(context).isOnline) {
            val jsonObject = JSONObject()
            val jsonArray = JSONArray(followingId)
            jsonObject.put("followingId",jsonArray)
            jsonObject.put("follow",isFollowing)
            userViewModel?.followAll(requireContext(), jsonObject)
            getUserSocialData()
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
                CommonUtils.setPageBottomSpacing(scrollView, requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
            }
        }
    }
}