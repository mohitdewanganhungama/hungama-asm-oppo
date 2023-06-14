package com.hungama.music.ui.main.view.fragment

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hungama.music.data.model.*
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.UserProfileFollowingAdapter
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.viewmodel.ContactsViewModel
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.ConnectionUtil
import com.hungama.music.utils.Constant
import com.hungama.music.utils.Utils
import com.hungama.music.R
import kotlinx.android.synthetic.main.common_header_action_bar.*
import kotlinx.android.synthetic.main.fragment_user_profile_following.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject


class UserProfileFollowingFragment(val userId: String, val isCurrentUser:Boolean) : BaseFragment(), LoaderManager.LoaderCallbacks<Cursor>,
    BaseActivity.OnLocalBroadcastEventCallBack {
    var userViewModel:UserViewModel? = null
    var userSocialData: UserSocialData? = null

    var PROJECTION_NUMBERS = arrayOf<String>(
        ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
        ContactsContract.CommonDataKinds.Phone.NUMBER
    )
    var PROJECTION_DETAILS = arrayOf<String>(
        ContactsContract.Contacts._ID,
        ContactsContract.Contacts.DISPLAY_NAME,
        ContactsContract.CommonDataKinds.Phone.PHOTO_URI
    )
    protected var phones: Map<Long, List<String?>> = HashMap()
    protected var contacts: ArrayList<ContactDetailModel> = ArrayList()
    val PERMISSIONS_REQUEST_READ_CONTACTS = 100
    lateinit var callbackManager2: CallbackManager
    var isCallFragment = false

    private val contactsViewModel by viewModels<ContactsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        callbackManager2 = CallbackManager.Factory.create()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_profile_following, container, false)
    }

    override fun initializeComponent(view: View) {
        baseMainScope.launch { 
            try {
                if (isAdded && context != null){
                    CommonUtils.applyButtonTheme(requireContext(), btnConnectFacebook)
                    CommonUtils.applyButtonTheme(requireContext(), btnConnectContact)
                    if (isCurrentUser){
                        coinView?.visibility = View.VISIBLE
                    }else{
                        coinView?.visibility = View.GONE
                    }
                    shimmerLayout.visibility = View.VISIBLE
                    shimmerLayout.startShimmer()
                    ivBack?.setOnClickListener { v -> backPress() }
                    tvActionBarHeading?.text = getString(R.string.profile_str_5)
                    btnConnectFacebook.setOnClickListener {
                        try {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                CommonUtils.hapticVibration(requireContext(), btnConnectFacebook!!,
                                    HapticFeedbackConstants.CONTEXT_CLICK
                                )
                            }
                        }catch (e:Exception){

                        }
                        //addFragment(R.id.fl_container, this, UserProfileFindFriendsFragment(), false)

                        /*val request = GraphRequest.newGraphPathRequest(
                            AccessToken.getCurrentAccessToken(),
                            SharedPrefHelper.getInstance().getUserId()+"/friends"
                        ) {
                            setLog(TAG, "initializeComponent: newGraphPathRequest"+it)
                        }

                        request.executeAsync()*/
                        callFacebook()

                    }
                    btnConnectContact.setOnClickListener{

//            if(isOnClick()){

//                isCallFragment = false

                        try {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                CommonUtils.hapticVibration(requireContext(), btnConnectContact!!,
                                    HapticFeedbackConstants.CONTEXT_CLICK
                                )
                            }
                        }catch (e:Exception){

                        }
                        //addFragment(R.id.fl_container, this, UserProfileDiscoverPeopleFragment(), false)
                        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(
                                requireActivity(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS),
                                PERMISSIONS_REQUEST_READ_CONTACTS)
                        } else {
                            //LoaderManager.getInstance(this).initLoader(0, null, this)

                        }*/
                        init()
//            }


                    }
                    setupUserViewModel()
                    CommonUtils.setPageBottomSpacing(scrollView, requireContext(),
                        resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
                        resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
                }
            }catch (e:Exception){
                
            }
        }

        CommonUtils.PageViewEvent("","","","",
            "profile_viewprofile","user profile_following","")
    }

    private fun setUpFollowing(userSocialData: UserSocialData) {

        if (userSocialData != null && userSocialData?.following != null && userSocialData?.following?.size!! > 0) {
            this.userSocialData = userSocialData
            rvFollowing.apply {
                layoutManager =
                    GridLayoutManager(context, 1)
                adapter = UserProfileFollowingAdapter(context, userSocialData?.following,
                    object : UserProfileFollowingAdapter.OnChildItemClick {
                        override fun onUserClick(childPosition: Int, isAddedClick: Boolean) {
                            if (isAddedClick){
                                setFollowUnFollow(userSocialData?.following?.get(childPosition)?.uId!!, userSocialData?.following?.get(childPosition)?.isAdded!!)
                            }else{
                                addFragment(R.id.fl_container, this@UserProfileFollowingFragment, UserProfileOtherUserProfileFragment(userSocialData?.following?.get(childPosition)?.uId!!), false)
                            }
                        }
                    })
                setRecycledViewPool(RecyclerView.RecycledViewPool())
                setHasFixedSize(true)
            }
        }
        shimmerLayout.visibility = View.GONE
        shimmerLayout.stopShimmer()
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
                userId
            )?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                setUpFollowing(it?.data)
                            }

                        }

                        Status.LOADING ->{
                            setProgressBarVisible(false)
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

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        setProgressBarVisible(true)
        return when (id) {
            0 -> CursorLoader(
                requireActivity(),
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                PROJECTION_NUMBERS,
                null,
                null,
                null
            )
            else -> CursorLoader(
                requireActivity(),
                ContactsContract.Contacts.CONTENT_URI,
                PROJECTION_DETAILS,
                null,
                null,
                null
            )
        }
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        when (loader.getId()) {
            0 -> {
                phones = HashMap()
                if (data != null) {
                    while (!data.isClosed && data.moveToNext()) {
                        val contactId = data.getLong(0)
                        val phone = data.getString(1)
                        var list: ArrayList<String?>
                        if (phones.containsKey(contactId)) {
                            list = ArrayList()
                            list.addAll(phones.get(contactId)!!)
                        } else {
                            list = ArrayList()
                            (phones as HashMap<Long, List<String?>>)?.put(contactId, list)
                        }
                        list.add(phone)
                    }
                    data.close()
                }
                LoaderManager.getInstance(this)
                    .initLoader(1, null, this)
            }
            1 -> if (data != null) {
                while (!data.isClosed && data.moveToNext()) {
                    val contactId = data.getLong(0)
                    val name = data.getString(1)
                    val photo = data.getString(2)
                    val contactPhones: List<String?>? = phones.get(contactId)
                    if (contactPhones != null) {
                        var contact: ContactDetailModel? = null
                        for (phone in contactPhones) {
                            //contact = Contact(contactId, name, phone.toString(), photo)
                            contact = ContactDetailModel(name, phone.toString())
                            if (contact != null){
                                addContact(contact)
                            }
                        }
                    }
                }
                data.close()
                setProgressBarVisible(false)
                if (contacts != null && contacts.size > 0){
                    //addFragment(R.id.fl_container, this, UserProfileDiscoverPeopleFragment(contacts), false)
                }

            }
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {

    }

    protected fun addContact(contact: ContactDetailModel?) {
        contacts.add(contact!!)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {

        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults.isNotEmpty()) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //LoaderManager.getInstance(this).initLoader(0, null, this)
                    contactsViewModel.fetchContacts()
                } else {
                    //  toast("Permission must be granted in order to display contacts information")
                }
            }
        }
    }

    private fun init() {
        contactsViewModel.contactsLiveData.observe(this, Observer {
            val gson = Gson()

            val listString: String = gson.toJson(it,
                object : TypeToken<ArrayList<Contact?>?>() {}.getType()
            )
            val jsonArray = JSONArray(listString)
            val jsonObject = JSONObject()
            jsonObject.put("contact", jsonArray)
            setLog("ContactData123", jsonObject.toString())
            syncUserContactList(jsonObject)

//            setLog(TAG, "CONTINUE.......", )
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(
                requireActivity(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS),
                PERMISSIONS_REQUEST_READ_CONTACTS)
        } else {
            contactsViewModel.fetchContacts()
        }
    }

    private fun syncUserContactList(jsonObject: JSONObject) {
        if (ConnectionUtil(requireContext()).isOnline) {
            userViewModel?.syncContactList(
                requireContext(),
                jsonObject
            )?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null) {
//                                if (!isCallFragment){
                                    addFragment(R.id.fl_container, this, UserProfileDiscoverPeopleFragment(it?.data), false)
//                                    isCallFragment = true
//                                }
//                                else{
//                                    setLog(TAG, "ELSE IS CALL FRAGMENT IS TRUE" )
//                                    isCallFragment = false
//                                }
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
        }else{
            val messageModel = MessageModel(getString(R.string.toast_message_5), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }

    private fun callFacebook(){
        if (AccessToken.isCurrentAccessTokenActive()){

            val request = GraphRequest.newGraphPathRequest(
                AccessToken.getCurrentAccessToken(),
                AccessToken.getCurrentAccessToken()?.userId+"/friends"
            ) {
                //setLog("FBResp", it.rawResponse.toString())
                try {
                    //setLog("FBResp3", it.jsonObject.toString())
                    /*val fbFriendsList = Gson().fromJson<FacebookFriends>(
                        it.jsonObject.toString(),
                        FacebookFriends::class.java
                    ) as FacebookFriends*/
                    it.jsonObject?.let { it1 -> syncUserFbList(it1) }
                }catch (e:Exception){
                    val messageModel = MessageModel(getString(R.string.discover_str_2), MessageType.NEUTRAL, true)
                    CommonUtils.showToast(requireContext(), messageModel)
                }

            }

            request.executeAsync()
        }else{
            LoginManager.getInstance().logInWithReadPermissions(this, callbackManager2, listOf("public_profile", "user_friends","email"))
            LoginManager.getInstance().registerCallback(callbackManager2, object : FacebookCallback<LoginResult>{
                override fun onCancel() {
                    val messageModel = MessageModel(getString(R.string.profile_str_121), MessageType.NEUTRAL, true)
                    CommonUtils.showToast(requireContext(), messageModel)
                }

                override fun onError(error: FacebookException) {
                    val messageModel = MessageModel(error.message.toString(), MessageType.NEUTRAL, true)
                    CommonUtils.showToast(requireContext(), messageModel)
                }

                override fun onSuccess(result: LoginResult) {
                    setLog("TAG", "Success Login")


                    val request = GraphRequest.newGraphPathRequest(
                        AccessToken.getCurrentAccessToken(),
                        result.accessToken.userId +"/friends",
                    ) {
                        //setLog("FBResp", it.rawResponse.toString())
                        try {
                            it.jsonObject?.let { it1 -> syncUserFbList(it1) }
                        }catch (e:Exception){
                            val messageModel = MessageModel(getString(R.string.discover_str_2), MessageType.NEUTRAL, true)
                            CommonUtils.showToast(requireContext(), messageModel)
                        }

                    }

                    request.executeAsync()
                    // Get User's Info
                }

            })
        }

    }

    private fun syncUserFbList(jsonObject: JSONObject) {
        if (ConnectionUtil(requireContext()).isOnline) {
            userViewModel?.syncFbList(
                requireContext(),
                jsonObject
            )?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                addFragment(R.id.fl_container, this, UserProfileFindFriendsFragment(it?.data), false)
                                shimmerLayout.visibility = View.GONE
                                shimmerLayout.stopShimmer()
                            }

                        }

                        Status.LOADING ->{
                            setProgressBarVisible(false)
                        }

                        Status.ERROR ->{
                            setEmptyVisible(false)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager2.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
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