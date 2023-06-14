package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
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
import com.appsflyer.CreateOneLinkHttpTask
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.UserProfileDescoverPeopleAdapter
import com.hungama.music.data.model.ContactListModel
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
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant
import kotlinx.android.synthetic.main.common_header_action_bar.*
import kotlinx.android.synthetic.main.fragment_user_profile_descover_people.*
import org.json.JSONObject


class UserProfileDiscoverPeopleFragment(val contactListModel: ContactListModel) : BaseFragment(),
    BaseActivity.OnLocalBroadcastEventCallBack {
    var userViewModel:UserViewModel? = null
    var userSocialData: UserSocialData? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_profile_descover_people, container, false)
    }

    override fun initializeComponent(view: View) {
        ivBack?.setOnClickListener { v -> backPress() }
        tvActionBarHeading?.text = getString(R.string.profile_str_14)
        if (contactListModel != null && contactListModel.data != null && contactListModel.data?.size!! > 0){
            /*val gson = Gson()

            val listString: String = gson.toJson(contacts,
                object : TypeToken<ArrayList<ContactDetailModel?>?>() {}.getType()
            )

            val jsonArray = JSONArray(listString)
            setLog("ContactLists:-", jsonArray.toString())*/
            setupUserViewModel()

        }

        CommonUtils.setPageBottomSpacing(rvDiscoverPeople, requireContext(),
            resources.getDimensionPixelSize(R.dimen.dimen_18), resources.getDimensionPixelSize(R.dimen.dimen_64),
            resources.getDimensionPixelSize(R.dimen.dimen_18), 0)

        CommonUtils.PageViewEvent("","","","",
            MainActivity.lastItemClicked,"user profile_discover people","")
    }

    private fun setUpFollower(userSocialData: UserSocialData, data: List<List<ContactListModel.Data?>?>) {
        val contactList: ArrayList<List<ContactListModel.Data?>?> = ArrayList()
        val addedList: ArrayList<List<ContactListModel.Data?>?> = ArrayList()
        val notAddedList: ArrayList<List<ContactListModel.Data?>?> = ArrayList()
        val inviteList: ArrayList<List<ContactListModel.Data?>?> = ArrayList()
        if (data != null && data.size > 0){
            if (userSocialData != null && userSocialData?.following != null && userSocialData?.following?.size!! > 0) {
                this.userSocialData = userSocialData
                if (data.size!! > 0){
                    data.forEachIndexed { i, item ->
                        if (userSocialData?.following?.size!! > 0) {
                            userSocialData?.following?.forEachIndexed { k, following ->
                                if (item?.size!! > 0 && item?.get(0)?.uId.equals(following?.uId)) {
                                    item?.get(0)?.isAdded = true
                                }
                            }
                            if (item?.get(0)?.isAdded == true){
                                addedList.add(item)
                            }else if (!TextUtils.isEmpty(item?.get(0)?.uId)){
                                notAddedList.add(item)
                            }else{
                                inviteList.add(item)
                            }
                        }
                    }
                }
            }
            if (!addedList.isNullOrEmpty()){
                contactList.addAll(addedList)
            }
            if (!notAddedList.isNullOrEmpty()){
                contactList.addAll(notAddedList)
            }
            if (!inviteList.isNullOrEmpty()){
                contactList.addAll(inviteList)
            }

            rvDiscoverPeople.apply {
                layoutManager =
                    GridLayoutManager(context, 1)
                adapter = UserProfileDescoverPeopleAdapter(context, contactList,
                    object : UserProfileDescoverPeopleAdapter.OnChildItemClick {
                        override fun onUserClick(childPosition: Int, isAddedClick: Boolean) {
                            if (isAddedClick){
                                setFollowUnFollow(contactList.get(childPosition)?.get(0)?.uId!!, contactList.get(childPosition)?.get(0)?.isAdded!!)
                            }else{
                                addFragment(R.id.fl_container, this@UserProfileDiscoverPeopleFragment, UserProfileOtherUserProfileFragment(contactList.get(childPosition)?.get(0)?.uId!!), false)
                            }
                        }

                        override fun onInviteClick(childPosition: Int) {
                            if (isShareAppOnClick()){
                                setLog(TAG, "initializeComponent: if condition working")
                                generateInviteLink()
                            }
                            else{
                                setLog(TAG, "initializeComponent: else condition working")
                            }
                        }
                    })
                setRecycledViewPool(RecyclerView.RecycledViewPool())
                setHasFixedSize(true)
            }
        }


    }

    private fun generateInviteLink(){
        CommonUtils.generateAppsFlyerInviteLink(requireActivity(), linkGeneratorListener,
            "mobile_share", "50", "coin", "www.hungama.com")
    }
    var errorCount = 0
    val linkGeneratorListener = object : CreateOneLinkHttpTask.ResponseListener{
        override fun onResponse(string: String?) {

            CommonUtils.setLog("inviteLink", "onResponse " + string.toString())
            CommonUtils.shareLink(requireContext(), string.toString())
        }

        override fun onResponseError(string: String?) {
            CommonUtils.setLog("inviteLink", "onResponseError " + string.toString())
            if (errorCount < 2){
                generateInviteLink()
                errorCount++
            }
        }
    }

    var lastShareAppClickedTime: Long = 0
    open fun isShareAppOnClick(): Boolean {
        /*
          Prevents the Launch of the component multiple times
          on clicks encountered in quick succession.
         */
        if (SystemClock.elapsedRealtime() - lastShareAppClickedTime < Constant.MAX_CLICK_INTERVAL) {
            lastShareAppClickedTime = SystemClock.elapsedRealtime()
            return false
        }
        lastShareAppClickedTime = SystemClock.elapsedRealtime()
        return true
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
                                setUpFollower(it?.data!!, contactListModel?.data!!)
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
                CommonUtils.setPageBottomSpacing(rvDiscoverPeople, requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_18), resources.getDimensionPixelSize(R.dimen.dimen_64),
                    resources.getDimensionPixelSize(R.dimen.dimen_18), 0)
            }
        }
    }
}