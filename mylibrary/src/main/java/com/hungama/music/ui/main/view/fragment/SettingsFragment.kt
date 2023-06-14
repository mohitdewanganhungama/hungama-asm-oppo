package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import com.hungama.music.data.model.*
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.UserProfileOtherUserProfileMostHeardSongsAdapter
import com.hungama.music.ui.main.adapter.UserProfileOtherUserProfileMostListenedArtistsAdapter
import com.hungama.music.ui.main.adapter.UserProfileOtherUserProfilePlaylistsAdapter
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.utils.*
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.CommonUtils.applyButtonTheme
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.utils.CommonUtils.hideKeyboard
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.CommonUtils.showKeyboard
import com.hungama.music.utils.preference.PrefConstant
import kotlinx.android.synthetic.main.common_header_action_bar.*
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.fragment_settings.ivUser
import kotlinx.android.synthetic.main.fragment_settings.llEditProfile
import kotlinx.android.synthetic.main.fragment_settings.llExploreNow
import kotlinx.android.synthetic.main.fragment_settings.llFollowersCount
import kotlinx.android.synthetic.main.fragment_settings.llFollowingCount
import kotlinx.android.synthetic.main.fragment_settings.llHeaderPlaylist
import kotlinx.android.synthetic.main.fragment_settings.llMain
import kotlinx.android.synthetic.main.fragment_settings.llNoActivity
import kotlinx.android.synthetic.main.fragment_settings.llPlaylist
import kotlinx.android.synthetic.main.fragment_settings.llPlaylistCount
import kotlinx.android.synthetic.main.fragment_settings.llSecond
import kotlinx.android.synthetic.main.fragment_settings.llSeeAll
import kotlinx.android.synthetic.main.fragment_settings.rlShare
import kotlinx.android.synthetic.main.fragment_settings.rvMostHeardSongs
import kotlinx.android.synthetic.main.fragment_settings.rvMostListenedArtist
import kotlinx.android.synthetic.main.fragment_settings.rvPlaylist
import kotlinx.android.synthetic.main.fragment_settings.scrollView
import kotlinx.android.synthetic.main.fragment_settings.tvFollowersCount
import kotlinx.android.synthetic.main.fragment_settings.tvFollowingCount
import kotlinx.android.synthetic.main.fragment_settings.tvPlayListCount
import kotlinx.android.synthetic.main.fragment_user_profile_other_user_profile.*
import kotlinx.coroutines.launch
import org.json.JSONObject

class SettingsFragment : BaseFragment(), ViewTreeObserver.OnScrollChangedListener,
    TextView.OnEditorActionListener, BaseActivity.OnLocalBroadcastEventCallBack {
    var userViewModel: UserViewModel? = null
    var userPlaylist: List<PlaylistRespModel.Data> = ArrayList()
    var isGoToProfileEditPage = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewSettingsFragment: View =
            inflater.inflate(R.layout.fragment_settings, container, false)

        return viewSettingsFragment
    }

    override fun initializeComponent(view: View) {
        if (arguments != null){
            if (requireArguments().containsKey(Constant.isProfileEditPage)){
                isGoToProfileEditPage = requireArguments().getBoolean(Constant.isProfileEditPage, false)
            }
        }

        shimmerLayout.visibility = View.GONE
        shimmerLayout.stopShimmer()
        applyButtonTheme(requireContext(), llEditProfile)
        applyButtonTheme(requireContext(), llExploreNow)
        applyButtonTheme(requireContext(), llEditProfile2)
        /*if (!TextUtils.isEmpty(SharedPrefHelper.getInstance().getUsername())){
            tvUserName.text = SharedPrefHelper.getInstance().getUsername()
            //tvActionBarHeading?.text = SharedPrefHelper.getInstance().getUsername()
        }else{
            tvUserName.text = "guest01"
            tvActionBarHeading?.text = "guest01"
        }*/

    /*    CommonUtils.PageViewEvent(""
        "","")*/

        ivBack?.setOnClickListener { v -> backPress() }
        ivBack2?.setOnClickListener { v -> backPress() }
        ivMenu?.setImageDrawable(requireContext().faDrawable(R.string.icon_tick, R.color.colorWhite))
        rlMenu?.setOnClickListener { callEditProfile() }


        rlHeading2.visibility = View.INVISIBLE
        scrollView.viewTreeObserver.addOnScrollChangedListener(this)
        //tvActionBarHeading?.text = "peaches02"
        //tvUserName?.text = "peaches02"
        llFollowingCount?.setOnClickListener(this)

        etName?.doOnTextChanged { text, start, before, count ->
            if(text?.length!!>0){
                //rlMenu?.visibility = View.VISIBLE
            }else{
                //rlMenu?.visibility = View.VISIBLE
            }
        }

        llSeeAll.setOnClickListener(this)
        llHeaderPlaylist.setOnClickListener(this)
        llEditProfile?.setOnClickListener(this)
        llEditProfile2?.setOnClickListener(this)
        llExploreNow?.setOnClickListener(this)
        etName?.setOnEditorActionListener(this)
        ivEditUsername?.setOnClickListener(this)
        rlShare?.setOnClickListener(this)

        if (isGoToProfileEditPage){
            (requireActivity() as MainActivity).setLocalBroadcastEventCall(this, Constant.EDIT_PROFILE_EVENT)
            addFragment(R.id.fl_container, this, UserProfileEditProfileFragment(), false)
        }

        CommonUtils.setPageBottomSpacing(scrollView, requireContext(),
            resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
            resources.getDimensionPixelSize(R.dimen.dimen_0), 0)

        CommonUtils.PageViewEvent("","","","",
            "Profile","profile_viewprofile","")
    }

    override fun onResume() {
        super.onResume()
        setUpViewModel()
        setLocalBroadcast()
    }

    override fun onClick(v: View) {
        super.onClick(v)
        if(v==llFollowersCount){
            addFragment(R.id.fl_container, this, UserProfileFollowersFragment(SharedPrefHelper.getInstance().getUserId()!!), false)
        }else if (v==llPlaylistCount){
            addFragment(R.id.fl_container, this, UserProfilePlaylistsFragment(userPlaylist), false)
        }else if (v==llFollowingCount){
            addFragment(R.id.fl_container, this, UserProfileFollowingFragment(SharedPrefHelper.getInstance().getUserId()!!, true), false)
        }else if (v==llEditProfile || v==llEditProfile2){
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), llEditProfile!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            (requireActivity() as MainActivity).setLocalBroadcastEventCall(this, Constant.EDIT_PROFILE_EVENT)
            addFragment(R.id.fl_container, this, UserProfileEditProfileFragment(), false)
        }else if (v==llHeaderPlaylist || v==llSeeAll){
            if (userPlaylist != null){
                addFragment(R.id.fl_container, this, UserProfilePlaylistsFragment(userPlaylist), false)
            }
        }else if (v==llExploreNow){
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), llExploreNow!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            //requireActivity().supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            //replaceFragment(R.id.fl_container, DiscoverMainTabFragment(), false, 0)

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
        }else if (v == ivEditUsername){
            etName?.isEnabled = true
            etName?.requestFocus()
            etName?.setSelection(etName?.text.toString().length)
            requireContext()?.showKeyboard(etName)
        }else if (v == rlShare){
            setLog(TAG, "onClick: isShareUserProfile:${isShareUserProfile}")
            if(isShareUserProfile){
                Utils.shareItem(requireActivity(), SharedPrefHelper.getInstance().get(PrefConstant.USER_SHARE,""))
            }

        }
    }

    private fun setUpLists(userPlaylistData: PlaylistRespModel) {
        if (userPlaylistData?.data != null && userPlaylistData?.data?.size!! > 0){
            userPlaylist = userPlaylistData?.data!!
            tvPlayListCount.text = userPlaylistData?.count.toString()
            llPlaylistCount?.setOnClickListener(this)
            llPlaylist.visibility = View.VISIBLE
            var seeMore = true
            if (userPlaylist.size > 5){
                seeMore = false
                llSeeAll.visibility = View.VISIBLE
            }else{
                llSeeAll.visibility = View.GONE
            }
            rvPlaylist.apply {
                layoutManager =
                    GridLayoutManager(context, 1)
                adapter = UserProfileOtherUserProfilePlaylistsAdapter(context, userPlaylistData.data,
                    object : UserProfileOtherUserProfilePlaylistsAdapter.OnChildItemClick {
                        override fun onUserClick(childPosition: Int) {
                            var varient = 1

                            val playlistDetailFragment = MyPlaylistDetailFragment(varient,object :MyPlaylistDetailFragment.onBackPreesHendel{
                                override fun backPressItem(status: Boolean) {

                                }

                            })
                            val bundle = Bundle()
                            bundle.putString("image", userPlaylistData?.data?.get(childPosition)?.data?.image)
                            bundle.putString("id", userPlaylistData?.data?.get(childPosition)?.data?.id)
                            bundle.putString("playerType", Constant.MUSIC_PLAYER)
                            playlistDetailFragment.arguments = bundle
                            addFragment(R.id.fl_container, this@SettingsFragment, playlistDetailFragment, false)
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onScrollChanged() {
        if (isAdded) {

            /* get the maximum height which we have scroll before performing any action */
            var maxDistance = 0

            maxDistance += llMain.marginTop + llMain.marginBottom + llMain.height + clUsername.marginTop + clUsername.marginBottom + clUsername.height + llSecond.marginTop + llSecond.marginBottom + llSecond.height + resources.getDimensionPixelSize(R.dimen.dimen_10)
            /* how much we have scrolled */
            val movement = scrollView.scrollY

            maxDistance = maxDistance + resources.getDimensionPixelSize(R.dimen.dimen_60)
            if (movement >= maxDistance){
                //setLog("OnNestedScroll-m", movement.toString())
                //setLog("OnNestedScroll-d", maxDistance.toString())
                headBlur.visibility = View.VISIBLE
                rlHeading2.visibility = View.VISIBLE
                //rlHeading.setBackgroundColor(artworkProminentColor)
            }else{
                //setLog("OnNestedScroll-m--", movement.toString())
                //setLog("OnNestedScroll-d--", maxDistance.toString())
                rlHeading2.visibility = View.INVISIBLE
                headBlur.visibility = View.INVISIBLE
                /*rlHeading.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.transparent
                    )
                )*/
            }
        }
    }

    private fun setUpViewModel() {
        /*if (!TextUtils.isEmpty(SharedPrefHelper.getInstance().get(PrefConstant.USER_IMAGE,""))){
            ImageLoader.loadImage(requireContext(),ivUser,SharedPrefHelper.getInstance().get(
                PrefConstant.USER_IMAGE,""),R.drawable.ic_no_user_img)
        }*/
        userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)


        getUserProfile()
        getUserPlaylistData()
    }



    private fun getUserSocialData(){
        if (ConnectionUtil(requireContext()).isOnline) {
            userViewModel?.getUserSocialData(
                requireContext(),
                SharedPrefHelper.getInstance().getUserId()!!
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
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }


    private fun fillUI(userSocialData: UserSocialData) {
        tvFollowersCount.text = userSocialData.followerCount.toString()
        tvFollowingCount.text = userSocialData.followingCount.toString()
        if (userSocialData.followerCount!! != null && userSocialData.followerCount!! > 0){
            llFollowersCount?.setOnClickListener(this)
        }
    }

    private fun getUserPlaylistData(){
        if (ConnectionUtil(requireContext()).isOnline) {
            userViewModel?.getUserPlaylistData(
                requireContext(),
                SharedPrefHelper.getInstance().getUserId()!!
            )?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            getUserSocialData()
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
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }



    private fun getUserProfile(){
        if (ConnectionUtil(requireContext()).isOnline!!) {
            userViewModel?.getUserProfileData(
                requireContext(), SharedPrefHelper.getInstance().getUserId()!!
            )?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                CommonUtils.saveUserProfileDetails(it?.data)
                                (activity as BaseActivity).callEventUserProperty(it?.data)
                                if (it?.data.statusCode == 200 && it?.data.result != null && it?.data?.result?.size!! > 0){

                                    if(isShareUserProfile){
                                        rlShare?.visibility=View.VISIBLE
                                    }else{
                                        rlShare?.visibility=View.GONE
                                    }

                                    if (!TextUtils.isEmpty(it?.data?.result?.get(0)?.handleName)){
                                        tvActionBarHeading?.text = it?.data?.result?.get(0)?.handleName
                                    }else{
                                        tvActionBarHeading?.text = ""
                                    }

                                    if (it?.data?.result?.get(0)?.profileImage!! != null && !TextUtils.isEmpty(it?.data?.result?.get(0)?.profileImage!!.toString())){
                                        ImageLoader.loadImage(requireContext(),ivUser,it?.data?.result?.get(0)?.profileImage!!.toString(),R.drawable.ic_no_user_img)
                                    }else if (it?.data?.result?.get(0)?.alternateProfileImage!! != null && !TextUtils.isEmpty(it?.data?.result?.get(0)?.alternateProfileImage!!.toString())){
                                        ImageLoader.loadImage(requireContext(),ivUser,it?.data?.result?.get(0)?.alternateProfileImage!!.toString(),R.drawable.ic_no_user_img)
                                    }
                                    var username = ""
                                    if (!TextUtils.isEmpty(it?.data?.result?.get(0)?.firstName)){
                                        username += it?.data?.result?.get(0)?.firstName + " "
                                    }
                                    if (!TextUtils.isEmpty(it?.data?.result?.get(0)?.lastName)){
                                        username += it?.data?.result?.get(0)?.lastName?.trim()
                                    }

                                    if (!TextUtils.isEmpty(username)){
                                        etName?.setText(username)
                                    }else{
                                        if (!TextUtils.isEmpty(it?.data?.result?.get(0)?.handleName)){
                                            etName?.setText(it?.data?.result?.get(0)?.handleName)
                                        }else{
                                            etName?.setText("")
                                        }

                                    }

                                    if (!TextUtils.isEmpty(""+it?.data?.result?.get(0)?.uId)){
                                        tvUID?.setText(""+it?.data?.result?.get(0)?.uId)
                                    }else{
                                        if (!TextUtils.isEmpty(SharedPrefHelper.getInstance().getUserId())){
                                            tvUID?.setText(""+SharedPrefHelper.getInstance().getUserId())
                                        }else{
                                            tvUID?.setText("")
                                        }

                                    }
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
        }else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }



    private fun callEditProfile() {
        this.hideKeyboard()
        etName?.clearFocus()
        try {
            val mainJson = JSONObject()
            val displayName = etName?.text
            val parts  = displayName?.split(" ")?.toMutableList()
            val firstName = parts?.firstOrNull()
            parts?.removeAt(0)
            val lastName = parts?.joinToString(" ")
            mainJson.put("firstName", firstName)
            mainJson.put("lastName", lastName)
            userViewModel?.editProfile(
                requireContext(),
                mainJson
            )

        } catch (e: Exception) {
            e.printStackTrace()
            Utils.showSnakbar(requireContext(),
                requireView(),
                false,
                getString(R.string.discover_str_2)
            )
        }
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        setLog("actionSave", "On keyboard done click11")
        if (actionId == EditorInfo.IME_ACTION_DONE){
            callEditProfile()
            setLog("actionSave", "On keyboard done click")
            return true
        }
        return false
    }

    private fun setLocalBroadcast(){
        (requireActivity() as MainActivity).setLocalBroadcastEventCall(this, Constant.AUDIO_PLAYER_EVENT)
    }
    override fun onLocalBroadcastEventCallBack(context: Context?, intent: Intent) {
        if (isAdded){
            val event = intent.getIntExtra("EVENT", 0)
            if (event == Constant.EDIT_PROFILE_RESULT_CODE) {
                getUserProfile()
            }
            if (event == Constant.AUDIO_PLAYER_RESULT_CODE) {
                CommonUtils.setPageBottomSpacing(scrollView, requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
            }
        }

    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        setLog(TAG, "onHiddenChanged: hidden ${hidden}")
        if (!hidden){
            setLog(TAG, "onHiddenChanged: hidden if ${hidden}")
            getUserSocialData()
        }
    }
}