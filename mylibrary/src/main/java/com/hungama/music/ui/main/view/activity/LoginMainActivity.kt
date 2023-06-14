package com.hungama.music.ui.main.view.activity

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import com.google.gson.Gson
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Base64
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.animation.Animation
import android.webkit.*
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.appsflyer.AppsFlyerLib
import com.appsflyer.AppsFlyerProperties
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.hungama.music.data.model.HERespModel
import com.hungama.music.data.model.LoginPlatformSequenceModel
import com.hungama.music.utils.preference.PrefConstant
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import com.hungama.music.data.model.MessageModel
import com.hungama.music.data.model.MessageType
import com.hungama.music.utils.customview.applelogin.*
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.eventreporter.SkipEvent
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.home.eventreporter.LoginEvent
import com.hungama.music.home.eventreporter.TappedTCContinueEvent
import com.hungama.music.home.eventreporter.UserAttributeEvent
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.eventanalytic.eventreporter.LoginErrorEvent
import com.hungama.music.eventanalytic.eventreporter.LoginSuccessEvent
import com.hungama.music.ui.main.adapter.LoginPlatformSequenceAdapter
import com.hungama.music.ui.main.adapter.LoginSliderAdapter
import com.hungama.music.ui.main.viewmodel.LoginSliderModel
import com.hungama.music.ui.main.viewmodel.UserSubscriptionViewModel
import com.hungama.music.utils.Constant.SIGNIN_WITH_ALL
import com.hungama.music.utils.Constant.SIGNIN_WITH_APPLE
import com.hungama.music.utils.Constant.SIGNIN_WITH_EMAIL
import com.hungama.music.utils.Constant.SIGNIN_WITH_FACEBOOK
import com.hungama.music.utils.Constant.SIGNIN_WITH_GOOGLE
import com.hungama.music.utils.Constant.SIGNIN_WITH_MOBILE
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.hideKeyboard
import com.hungama.music.utils.CommonUtils.isPackageInstalled
import com.hungama.music.utils.CommonUtils.setAppButton1
import com.hungama.music.utils.CommonUtils.setLog
import kotlinx.android.synthetic.main.act_login_main.*
import kotlinx.android.synthetic.main.act_login_main.llEmail
import kotlinx.android.synthetic.main.act_login_main.llFacebook
import kotlinx.android.synthetic.main.act_login_main.llGoogle
import kotlinx.android.synthetic.main.activity_otp_varify.*
import kotlinx.android.synthetic.main.activity_phone_number.*
import kotlinx.android.synthetic.main.layout_progress.*
import kotlinx.coroutines.*
import org.json.JSONObject
import org.json.JSONTokener
import java.io.OutputStreamWriter
import java.net.URL
import java.util.*
import javax.net.ssl.HttpsURLConnection
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class LoginMainActivity() : AppCompatActivity() {

    // get reference of the firebase auth
    var userViewModel: UserViewModel? = null
    val RC_SIGN_IN = 100
    val TAG = javaClass.name.toString()
    lateinit var auth: FirebaseAuth
    lateinit var googleSignInClient: GoogleSignInClient
    var callbackManager: CallbackManager?=null
//    val provider = OAuthProvider.newBuilder("apple.com")
    var SIGNUPMETHOD="EMAIL"
    var action = 0

    lateinit var viewPager2: ViewPager2
    private val sliderHandler = Handler()
    private var sliderItems = mutableListOf<LoginSliderModel>()
    private lateinit var animation : Animation
    private lateinit var loginSliderAdapter : LoginSliderAdapter
    private var loginPlatformSequenceList:ArrayList<LoginPlatformSequenceModel> = ArrayList()
    
    object AppleConstants {

        val CLIENT_ID = "com.hungama.myplay.staging.signin"
        val REDIRECT_URI = "https://user.api.hungama.com/v1/apple-callback"
        val SCOPE = "email"

        val AUTHURL = "https://appleid.apple.com/auth/authorize"
        val TOKENURL = "https://appleid.apple.com/auth/token"

    }

    lateinit var appleAuthURLFull: String
    lateinit var appledialog: Dialog
    lateinit var appleAuthCode: String
    lateinit var appleClientSecret: String
    var dotscount : Int = 0
    var url = ""


    var appleId = ""
    var appleFirstName = ""
    var appleMiddleName = ""
    var appleLastName = ""
    var appleEmail = ""
    var appleAccessToken = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_login_main)
        //tvTermsOfServices.text = Html.fromHtml(getString(R.string.login_str_61))
        loginPlatformSequenceList = CommonUtils.getLoginPlateformSequence()
        sliderItems.add(LoginSliderModel(R.drawable.login_movie))
        sliderItems.add(LoginSliderModel(R.drawable.login_music))
        sliderItems.add(LoginSliderModel(R.drawable.login_podcast))


        if (intent.hasExtra(Constant.DeepLink_Payment))
        url = intent.getStringExtra(Constant.DeepLink_Payment).toString()

        setLog("PrintUrl", " Login oncreate url " + url)

        viewPager2 = findViewById(R.id.viewpager)
        val sliderDotspanel = findViewById<LinearLayout>(R.id.ivImageThreeDots)
        loginSliderAdapter = LoginSliderAdapter()
        loginSliderAdapter.setdata(sliderItems,this)
        viewPager2.adapter = loginSliderAdapter
        dotscount = loginSliderAdapter.itemCount
        setLog(TAG, "onCreate: dotscount "+dotscount)
        val dots: Array<ImageView?> = arrayOfNulls(dotscount)

        for (i in 0 until dotscount) {
            dots[i] = ImageView(this)
            dots[i]!!.setImageDrawable(getDrawable(R.drawable.non_active_dot))
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(8, 0, 8, 0)
            sliderDotspanel.addView(dots[i], params)
        }

        viewPager2.clipToPadding = false
        viewPager2.clipChildren = false
        viewPager2.offscreenPageLimit = 3
        viewPager2.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.addTransformer { page, position ->
            val r = 1 - Math.abs(position)
            page.scaleY = 0.85f + r * 0.15f
        }
        viewPager2.setPageTransformer(compositePageTransformer)

        viewPager2.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)

            }
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                sliderHandler.removeCallbacks(sliderRunnable)
                sliderHandler.postDelayed(sliderRunnable, 5000)
                setLog(TAG, "onPageSelected: position "+position)
                if (position == 0){
                    textView2.text = getString(R.string.login_slider_str_1)
                    textView3.text = getString(R.string.login_slider_str_2)
                }
                else if (position ==1){
                    textView2.text = getString(R.string.login_slider_str_7)
                    textView3.text = getString(R.string.login_slider_str_8)
                }
                else if (position == 2){
                    textView2.text = getString(R.string.login_slider_str_5)
                    textView3.text = getString(R.string.login_slider_str_6)
                }
                for (i in 0 until dotscount) {
                    dots[i]?.setImageDrawable(ContextCompat.getDrawable(this@LoginMainActivity, R.drawable.non_active_dot))
                }

                dots[position]?.setImageDrawable(ContextCompat.getDrawable(this@LoginMainActivity, R.drawable.active_dot))
            }
        })
        action = intent.getIntExtra("action", 0)
        configureGoogleClient()
        configureFacebook()

        setUpViewModel()
        auth = FirebaseAuth.getInstance()

        tvTermCondtion?.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val hashMap = HashMap<String,String>()
                hashMap.put(EventConstant.SOURCE_EPROPERTY,"Tapped TnC Continue")
                EventManager.getInstance().sendEvent(TappedTCContinueEvent(hashMap))
            }

        }
        ll_Skip.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val hashMap = HashMap<String,String>()
                hashMap.put(EventConstant.SOURCE_EPROPERTY,"Skip")
                setLog("TAG","login${hashMap}")
                EventManager.getInstance().sendEvent(SkipEvent(hashMap))
            }
            url = ""
          redirectToHome()
        }
        setAppButton1(this, ll_LoginMobile)
        ll_LoginMobile.setOnClickListener {
           callLoginWithMobileActivity()
        }

        // fill otp and call the on click on button
        llEmail.setOnClickListener {
            signInWithEmail()
        }

        // click on Google button
        llGoogle.setOnClickListener {
            signInToGoogle()
        }


        // click on facebook button
        llFacebook.setOnClickListener {
            signInToFacebook()
        }

        tvPrivacyPolicy.setOnClickListener{
            CommonUtils.openCommonWebView(Constant.privacyPolicy,this)
        }
        tvTermsOfServices.setOnClickListener {
            CommonUtils.openCommonWebView(Constant.termAndConditionLink,this)
        }

        //click on apple button
        /*llAppleLogin.setOnClickListener {
            signInToApple()
        }*/



//        appleAuthURLFull = "https://appleid.apple.com/auth/authorize?client_id=com.hungama.myplay.staging.signin&redirect_uri=https://www.hungama.com/apple_redirect&response_type=code%20id_token&state=123456&scope=name%20email&response_mode=form_post"

//        val configuration = SignInWithAppleConfiguration(
//            clientId = "com.hungama.myplay.staging.signin",
//            redirectUri = "https://www.hungama.com/apple_redirect",
//            scope = "email"
//        )

        //            redirectUri = "https://user.api.hungama.com:8081/v1/apple-callback",

        val configuration = SignInWithAppleConfiguration(
            clientId = "com.hungama.myplay.staging.signin",
            redirectUri = "https://user.api.hungama.com/v1/apple-callback",
            scope = "email",
        )

//        val configuration = SignInWithAppleConfiguration(
//            clientId = "com.hungama.web.signin",
//            redirectUri = "https://www.hungama.com/apple_redirect",
//            scope = "email"
//        )
        val callback: (SignInWithAppleResult) -> Unit = { result ->
            when (result) {
                is SignInWithAppleResult.Success -> {
                    setLog("SAMPLE_APP", "User Success Apple Sign In:${result.authorizationCode}")
                    val messageModel = MessageModel(result.authorizationCode, MessageType.NEUTRAL, true)
                    CommonUtils.showToast(this, messageModel)

                    callSocialLogin(
                        "",
                        "",
                        result.authorizationCode,
                        "",
                        "",
                        "apple",
                        "",
                        ""
                    )
                }
                is SignInWithAppleResult.Failure -> {
                    setLog("SAMPLE_APP", "Received error from Apple Sign In ${result.error.message}")
                }
                is SignInWithAppleResult.Cancel -> {
                    setLog("SAMPLE_APP", "User canceled Apple Sign In")
                }
            }
        }
        llAppleLogin.setOnClickListener {
            signInToApple()
        }


        if (action == SIGNIN_WITH_GOOGLE){
            signInToGoogle()
        }else if (action == SIGNIN_WITH_FACEBOOK){
            signInToFacebook()
        }else if (action == SIGNIN_WITH_APPLE){
            signInToApple()
        }else if (action == SIGNIN_WITH_EMAIL){
            callLoginEvent("Email")
            val intent: Intent
            intent = Intent(this@LoginMainActivity, EnterEmailActivity::class.java)
            intent.putExtra("action", action)
            startActivity(intent)
            overridePendingTransition(R.anim.enter, R.anim.exit)
        }else if (action == SIGNIN_WITH_MOBILE){
            callLoginWithMobileActivity()
            callLoginEvent("Phone")
            val intent = Intent(this@LoginMainActivity, EnterMobileNumberActivity::class.java)
            intent.putExtra("action", action)
            startActivityForResult(intent, SIGNIN_WITH_MOBILE)
            overridePendingTransition(R.anim.enter, R.anim.exit)
        }



        if (Constant.DEFAULT_COUNTRY_CODE.equals("IN", true)){
            clLanguage?.show()
        }else{
            clLanguage?.hide()
        }
        clLanguage?.setOnClickListener {
            hideKeyboard()
            val intent = Intent(this@LoginMainActivity, ChooseLanguageActivity::class.java)
            intent.putExtra("action", action)
            startActivityForResult(intent, SIGNIN_WITH_MOBILE)
            overridePendingTransition(R.anim.enter, R.anim.exit)
        }
        rvLoginPlateformSequence?.apply {
            layoutManager =
                GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false)
            adapter = LoginPlatformSequenceAdapter(context, loginPlatformSequenceList, object : LoginPlatformSequenceAdapter.OnLoginPlatformClickListener{
                override fun onLoginPlatformClickListener(position: Int) {
                    if (!loginPlatformSequenceList.isNullOrEmpty() && loginPlatformSequenceList.size > position){
                        when(loginPlatformSequenceList.get(position).id){
                            SIGNIN_WITH_EMAIL -> {
                                setLog("UserLogin", "LoginMainActivity-OnCreate()-onLoginPlatformClickListene()-SIGNIN_WITH_EMAIL")
                                signInWithEmail()
                            }
                            SIGNIN_WITH_GOOGLE -> {
                                setLog("UserLogin", "LoginMainActivity-OnCreate()-onLoginPlatformClickListene()-SIGNIN_WITH_GOOGLE")
                                signInToGoogle()
                            }
                            SIGNIN_WITH_FACEBOOK -> {
                                setLog("UserLogin", "LoginMainActivity-OnCreate()-onLoginPlatformClickListene()-SIGNIN_WITH_FACEBOOK")
                                signInToFacebook()
                            }
                            SIGNIN_WITH_APPLE -> {
                                setLog("UserLogin", "LoginMainActivity-OnCreate()-onLoginPlatformClickListene()-SIGNIN_WITH_APPLE")
                                //setUpSignInWithAppleOnClick(supportFragmentManager, configuration, callback)
                                //openCCT()
                                signInToApple()
                            }
                        }
                    }
                }
            })
            setRecycledViewPool(RecyclerView.RecycledViewPool())
            setHasFixedSize(true)
        }

        if (url.isNotEmpty())
            clLanguage.hide()
    }

    private fun callLoginWithMobileActivity() {
        callLoginEvent("Phone")
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                CommonUtils.hapticVibration(this, ll_LoginMobile!!,
                    HapticFeedbackConstants.CONTEXT_CLICK
                )
            }
        }catch (e:Exception){

        }
        val intent = Intent(this@LoginMainActivity, EnterMobileNumberActivity::class.java)
        intent.putExtra("action", action)
        intent.putExtra("mainLogin", true)
        if (url.isNotEmpty()) {
            intent.putExtra(Constant.DeepLink_Payment, url)
        }
        startActivityForResult(intent, SIGNIN_WITH_MOBILE)
        overridePendingTransition(R.anim.enter, R.anim.exit)
    }

    override fun onResume() {
        super.onResume()
        hideKeyboard()
        setLog("displayDiscover", "he_api-${CommonUtils.getFirebaseConfigHEAPIData()}")
    }

    override fun onDestroy() {
        super.onDestroy()
        hideKeyboard()
    }
    private fun setUpViewModel() {
        userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)

        setProgressBarVisible(false)
    }

    private fun callSocialLogin(
        uid: String,
        providerUid: String,
        email: String,
        userName: String,
        userImage: String,
        provider: String,
        firstname:String,
        lastname:String
    ) {
        dismissLoader()
        if (ConnectionUtil(this@LoginMainActivity).isOnline) {

            try {
                val mainJson = JSONObject()
                val clientDataJson = JSONObject()
                val login_provider_uidJson = JSONObject()
                login_provider_uidJson.put(
                    "value",
                    uid
                )
                clientDataJson.put("login_provider_uid", login_provider_uidJson)

                val silentUserIdJson = JSONObject()
                silentUserIdJson.put(
                    "value",
                    SharedPrefHelper.getInstance().get(PrefConstant.SILENT_USER_ID,"")
                )
                clientDataJson.put("silent_user_id", silentUserIdJson)
                val uidJsonObject = JSONObject()
                uidJsonObject.put("value", SharedPrefHelper.getInstance().getUserId())
                clientDataJson.put("uid", uidJsonObject)

                val usernameJson = JSONObject()
                if(!TextUtils.isEmpty(email)){
                    usernameJson.put(
                        "value",
                        email
                    )
                }else{
                    usernameJson.put(
                        "value",
                        uid
                    )
                }

                clientDataJson.put("username", usernameJson)

                val emailJson = JSONObject()
                emailJson.put(
                    "value",
                    email
                )
                clientDataJson.put("email", emailJson)

                val nameJson = JSONObject()
                nameJson.put(
                    "value",
                    firstname + " " + lastname
                )
                clientDataJson.put("name", nameJson)

                val imageJson = JSONObject()
                imageJson.put(
                    "value",
                    userImage
                )
                //clientDataJson.put("image", imageJson)

                val uidJson = JSONObject()
                uidJson.put(
                    "value",
                    SharedPrefHelper.getInstance().get(PrefConstant.SILENT_USER_ID,"")
                )
                clientDataJson.put("uid", uidJson)

                val login_providerJson = JSONObject()
                //facebook //googleplus
                login_providerJson.put(
                    "value",
                    provider
                )
                clientDataJson.put("login_provider", login_providerJson)

                val is_site_uidJson = JSONObject()
                is_site_uidJson.put(
                    "value",
                    false
                )
                clientDataJson.put("is_site_uid", is_site_uidJson)

                mainJson.put("process", "gigya_login")
                mainJson.put("method", "signup_login")
                mainJson.put("client_data", clientDataJson)

                SharedPrefHelper.getInstance().save(PrefConstant.USER_NAME,userName)
                //SharedPrefHelper.getInstance().save(PrefConstant.USER_IMAGE,userImage)
                userViewModel?.socialLogin(
                    this@LoginMainActivity,
                    mainJson.toString()
                )?.observe(this,
                    Observer {
                        when(it.status){
                            Status.SUCCESS->{
                                setProgressBarVisible(false)
                                dismissLoader()
                                if (it?.data != null) {
                                    setLog(TAG, "Registration:: provider :${provider} isNewUser:${it?.data?.result?.data?.newUser}")
                                    if(it?.data?.result?.data?.newUser!=null&&!TextUtils.isEmpty(it?.data?.result?.data?.newUser) && it?.data?.result?.data?.newUser.contains("true",true)){
                                        setLog(TAG, "Registration provider:${provider}")
                                        Utils.registerUserMethod_AF(provider)
                                    }else{
                                        CoroutineScope(Dispatchers.IO).launch {
                                            val hashMap1 =
                                                java.util.HashMap<String, String>()
                                            hashMap1.put(EventConstant.METHOD_EPROPERTY,provider)
                                            setLog("LOGIN","Success${hashMap1}")
                                            EventManager.getInstance().sendEvent(LoginSuccessEvent(hashMap1))
                                        }

                                    }
                                    if (SharedPrefHelper.getInstance().isUserLoggedIn()){
                                        setLog("LoginSubscription", "LoginMainActivity-socialLogin-isUserLogdIn-true")
                                        getUserSubscriptionStatus()
                                    }else{
                                        setLog("LoginSubscription", "LoginMainActivity-socialLogin-isUserLogdIn-false")
                                        redirectToHome()
                                    }
                                }
                            }

                            Status.LOADING ->{
                                setProgressBarVisible(true)
                            }

                            Status.ERROR ->{
                                setProgressBarVisible(false)
                                dismissLoader()
                                val messageModel = MessageModel(it?.message.toString(), MessageType.NEUTRAL, true)
                                CommonUtils.showToast(this, messageModel)

                                CoroutineScope(Dispatchers.IO).launch {
                                    val hashMap = HashMap<String,String>()
                                    hashMap.put(EventConstant.ERRORTYPE_EPROPERTY,it.message!!)
                                    hashMap.put(EventConstant.SOURCE_EPROPERTY,"Email")
                                    setLog("LOGIN","Success${hashMap}")
                                    EventManager.getInstance().sendEvent(LoginErrorEvent(hashMap))
                                }

                            }
                        }
                    })

            } catch (e: Exception) {
                e.printStackTrace()
                val messageModel = MessageModel(getString(R.string.discover_str_2), MessageType.NEUTRAL, true)
                CommonUtils.showToast(this, messageModel)
            }


        } else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),MessageType.NEGATIVE, true)
            CommonUtils.showToast(this, messageModel)
        }
    }

    private fun signInWithEmail(){
        callLoginEvent("Email")
        setLog("PrintUrl", " Login Email " + url.toString())
        val intent: Intent
        intent = Intent(this@LoginMainActivity, EnterEmailActivity::class.java)
        intent.putExtra("action", action)
        if (url.isNotEmpty())
            intent.putExtra(Constant.DeepLink_Payment,  url)
        startActivityForResult(intent, SIGNIN_WITH_EMAIL)
        overridePendingTransition(R.anim.enter, R.anim.exit)
    }

    private fun configureGoogleClient() {
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun signInToGoogle() {
        callLoginEvent("Gmail")
        showLoader()
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Pass the activity result back to the Facebook SDK
        callbackManager?.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SIGNIN_WITH_GOOGLE || requestCode == SIGNIN_WITH_FACEBOOK
            || requestCode == SIGNIN_WITH_MOBILE || requestCode == SIGNIN_WITH_EMAIL
            || requestCode == SIGNIN_WITH_APPLE || requestCode == SIGNIN_WITH_ALL){
                if (resultCode == Activity.RESULT_OK){

                }else if (resultCode == Activity.RESULT_CANCELED){

                }else{
                    action = requestCode
                    redirectToHome()
                }
        }
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                //setLog("TAG", "firebaseAuthWithGoogle:" + account.id)
                //firebaseAuthWithGoogle(account.idToken!!, account.id!!)
                parseGoogleData(task)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                setLog("Google sign in failed", e.message.toString())
            } catch (e:Exception){

            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String, id: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                dismissLoader()
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    setLog("TAG", "signInWithCredential:success")
                    val user = auth.currentUser
                    setLog(
                        "TAG",
                        "uid-:" + user!!.uid + " provider_uid-:" + idToken + " name-:" + user.displayName + " email-:" + user.email + "name-:" + user.photoUrl
                    )
                    loginSuccess(user, "googleplus", idToken)
                } else {
                    // If sign in fails, display a message to the user.
                    setLog("signInWithCredential:failure", task.exception.toString())
                }
            }
    }

    private fun signInToFacebook() {
        callLoginEvent("Facebook")
        showLoader()
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile", "user_friends","email"))
    }

    fun configureFacebook() {
// Initialize Facebook Login button
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                setLog("TAG", "facebook:onSuccess:$loginResult")
                //handleFacebookAccessToken(loginResult.accessToken)
                getFacebookProfileData(loginResult)
            }

            override fun onCancel() {
                setLog("TAG", "facebook:onCancel")
                val messageModel = MessageModel(getString(R.string.discover_str_2), MessageType.NEUTRAL, true)
                CommonUtils.showToast(applicationContext, messageModel)
            }

            override fun onError(error: FacebookException) {
                setLog("facebook:onError", error.message.toString())
                val messageModel = MessageModel(getString(R.string.discover_str_2), MessageType.NEUTRAL, true)
                CommonUtils.showToast(applicationContext, messageModel)
            }
        })
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        setLog("TAG", "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                dismissLoader()
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    setLog("TAG", "signInWithCredential:success")
                    val user = auth.currentUser
                    setLog(
                        "TAG",
                        "uid-:" + user!!.uid + " provider_uid-:" + token.token + " name-:" + user.displayName + " email-:" + user.email + "name-:" + user.photoUrl
                    )
                    loginSuccess(user, "facebook", token.token)
                } else {
                    // If sign in fails, display a message to the user.
                    setLog("signInWithCredential:failure", task.exception?.message.toString())
                    val messageModel = MessageModel("Authentication failed.", MessageType.NEUTRAL, true)
                    CommonUtils.showToast(applicationContext, messageModel)
                }
            }
    }


    fun loginSuccess(user: FirebaseUser?, provider: String, providerToken: String) {
        if (user != null) {
            setLog("loginSuccess", "provider-$provider - user.uid-${user.uid} - providerToken-${providerToken} - user.email-${user.email} - user.displayName-${user.displayName} - user.photoUrl-${user.photoUrl}")
            callSocialLogin(
                user.uid.toString(),
                providerToken.toString(),
                user.email.toString(),
                user.displayName.toString(),
                user.photoUrl.toString(),
                provider.toString(),
                user.displayName.toString(),
                user.displayName.toString()
            )
        }

    }

    fun redirectToHome() {
        hideKeyboard()

        val userId = SharedPrefHelper.getInstance().getUserId().toString()
        if (userId.isNotEmpty()){
            AppsFlyerLib.getInstance().setCustomerIdAndLogSession(userId, this)
        }

/*        val appsFlyerUserId = AppsFlyerProperties.getInstance().getString(AppsFlyerProperties.APP_USER_ID)
        setLog("appsFlyerUserId", appsFlyerUserId.toString())*/

        if (action == Constant.SIGNIN_WITH_GOOGLE
            || action == Constant.SIGNIN_WITH_FACEBOOK
            || action == Constant.SIGNIN_WITH_MOBILE
            || action == Constant.SIGNIN_WITH_EMAIL
            || action == Constant.SIGNIN_WITH_APPLE
            || action == Constant.SIGNIN_WITH_ALL){
            overridePendingTransition(R.anim.enter, R.anim.exit)
            val returnIntent = Intent()
            returnIntent.putExtra("result", action)
            setResult(action, returnIntent)
            finish()
        }else{
                intent = Intent(this@LoginMainActivity, MainActivity::class.java)
                if (url.isNotEmpty())
                    intent.putExtra(Constant.DeepLink_Payment, url)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                overridePendingTransition(R.anim.enter, R.anim.exit)
                finish()

        }

        if(SharedPrefHelper.getInstance().isUserLoggedIn()){
            val userDataMap= java.util.HashMap<String, String>()
            userDataMap.put(EventConstant.LOG_IN_SOURCE, "onboarding")
            userDataMap.put(EventConstant.LOG_IN_METHOD, SIGNUPMETHOD)
            EventManager.getInstance().sendUserAttribute(UserAttributeEvent(userDataMap))
        }
        val intent = Intent(Constant.SONG_DURATION_BROADCAST)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    fun showLoader() {
        if (!this.isFinishing) {
            DisplayDialog.getInstance(this).showProgressDialog(this@LoginMainActivity, false)
        }
    }

    fun dismissLoader() {
        if (!this.isFinishing) {
            DisplayDialog.getInstance(this).dismissProgressDialog()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setProgressBarVisible(false)
        dismissLoader()

    }
    private fun setProgressBarVisible(it: Boolean) {
        if (it) {
//            showLoader()
            pb_progress?.visibility = View.VISIBLE
        } else {
//            dismissLoader()
            pb_progress?.visibility = View.GONE
        }
    }


    fun signInToApple(){
        setLog("UserLogin", "LoginMainActivity-signInToApple()")
        // Localize the Apple authentication screen in French.
//        provider.addCustomParameter("locale", "en")
        val pending = auth.pendingAuthResult
        if (pending != null) {
            pending.addOnSuccessListener { authResult ->
                setLog("UserLogin", "LoginMainActivity-checkPending:onSuccess:$authResult")
                // Get the user profile with authResult.getUser() and
                // authResult.getAdditionalUserInfo(), and the ID
                // token from Apple with authResult.getCredential().
                val user = authResult.user
            }.addOnFailureListener { e ->
                setLog("UserLogin", "LoginMainActivity-checkPending:onFailure-${e.message.toString()}")
            }
        } else {
/*            auth.startActivityForSignInWithProvider(this, provider.build())
                .addOnSuccessListener { authResult ->
                    // Sign-in successful!
                    setLog("UserLogin", "LoginMainActivity-activitySignIn:onSuccess:${authResult.user}")
                    val user = authResult.user
                    loginSuccess(user, "apple", "")
                    // ...
                }
                .addOnFailureListener { e ->
                    setLog("UserLogin", "LoginMainActivity-activitySignIn:onFailure-${e.message.toString()}")
                    val messageModel = MessageModel("Authentication failed.", MessageType.NEUTRAL, true)
                    CommonUtils.showToast(this, messageModel)
                }*/
        }
    }

    /**
     * Get user's basic profile data from facebook.
     */
    private fun getFacebookProfileData(loginResult: LoginResult) {
        val request = GraphRequest.newMeRequest(
            loginResult.accessToken
        ) { `object`, response ->
            parseFacebookData(`object`, AccessToken.getCurrentAccessToken()?.token!!)
            setLog("TAG", "FACEBOOK TOKEN : " + AccessToken.getCurrentAccessToken()?.token!!)
//            try {
//                LoginManager.getInstance().logOut()
//            } catch (ignore: Exception) {
//            }
        }
        val parameters = Bundle()
        val REQUIRED_FIELDS = "fields"
        parameters.putString(REQUIRED_FIELDS, permissions)
        request.parameters = parameters
        request.executeAsync()
    }

    private fun parseFacebookData(graphJson: JSONObject?, token: String) {
        try {
            if (graphJson != null) {
                val ids = graphJson.optString(ID)
                val names = graphJson.optString(NAME)
                val gender = graphJson.optString(GENDER)
                val emails = graphJson.optString(EMAIL)
                val first_name = graphJson.optString(FIRST_NAME)
                val last_name = graphJson.optString(LAST_NAME)
                val image_url = "http://graph.facebook.com/$ids/picture?type=large"
                setLog(
                    "Tag",
                    "FBDATA" + "-->\nId " + ids + "\n-->Name " + names + "\n-->email " + emails + names + "\n-->email " + emails + "\n-->FirstName " + first_name + "\n-->LAstName " + last_name
                )
                SIGNUPMETHOD="Facebook"

                var id = ""
                var idToken = ""
                var email = ""
                var displayName = ""
                var image = ""
                var name = ""
                var fullName = ""

                if (!TextUtils.isEmpty(ids)){
                    id = ids.toString()
                }
                if (!TextUtils.isEmpty(token)){
                    idToken = token.toString()
                }
                if (!TextUtils.isEmpty(emails)){
                    email = emails.toString()
                }
                if (!TextUtils.isEmpty("$first_name$last_name")){
                    displayName = "$first_name $last_name"
                }
                if (image_url != null && !TextUtils.isEmpty(""+image_url)){
                    image = image_url.toString()
                }
                if (!TextUtils.isEmpty(first_name)){
                    name = first_name.toString()
                }
                if (!TextUtils.isEmpty(last_name)){
                    fullName = last_name.toString()
                }
                callSocialLogin(
                    id,
                    idToken,
                    email,
                    displayName,
                    image,
                    "facebook",
                    name,
                    fullName
                )
            }
        }catch (e:Exception){

        }

    }

    /*
     * Facebook JSON fields for user's basic profile.
     */
    companion object FacebookFields {
        internal val ID = "id"
        internal val NAME = "name"
        internal val GENDER = "gender"
        internal val EMAIL = "email"
        internal val FIRST_NAME = "first_name"
        internal val LAST_NAME = "last_name"
        internal val PICTURE = "picture"
//        internal val USER_FRIENDS = "user_friends"

        internal val permissions: String
            get() = ID + "," +
                    NAME + "," +
                    GENDER + "," +
                    EMAIL + "," +
                    FIRST_NAME + "," +
                    LAST_NAME + "," +
                    PICTURE
    }

    private fun parseGoogleData(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            setLog(
                "Tag",
                "GoogleDATA" + "-->\nId " + account!!.id + "\n-->FirstName " + account.givenName + "\n-->LastName " + account.familyName + "\n-->email " + account.email + "\n-->Photo " + account.photoUrl
            )
            //googleSignInClient!!.signOut()
            SIGNUPMETHOD="Google"
            var id = ""
            var idToken = ""
            var email = ""
            var displayName = ""
            var image = ""
            var name = ""
            var fullName = ""

            if (!TextUtils.isEmpty(account.id)){
                id = account.id.toString()
            }
            if (!TextUtils.isEmpty(account.idToken)){
                idToken = account.idToken.toString()
            }
            if (!TextUtils.isEmpty(account.email)){
                email = account.email.toString()
            }
            if (!TextUtils.isEmpty(account.displayName)){
                displayName = account.displayName.toString()
            }
            if (account.photoUrl != null && !TextUtils.isEmpty(""+account.photoUrl)){
                image = account.photoUrl.toString()
            }
            if (!TextUtils.isEmpty(account.givenName)){
                name = account.givenName.toString()
            }
            if (!TextUtils.isEmpty(account.familyName)){
                fullName = account.familyName.toString()
            }
            callSocialLogin(
                id,
                idToken,
                email,
                displayName,
                image,
                "googleplus",
                name,
                fullName
            )


        } catch (e: ApiException) {
            setLog("TAG", " signInResult:failed code=" + e.statusCode)
        } catch (e:Exception){
            setLog("TAG", " signInResult:Exceptione=" + e.message)
        }

    }

    suspend fun isAppleLoggedIn(): Boolean {
        val expireTime = SharedPrefHelper.getInstance().getAppleVerifyRefreshToken()

        val currentTime = System.currentTimeMillis() / 1000L // Check the current Unix Time

        return if (currentTime >= expireTime!!) {
            // After 24 hours validate the Refresh Token and generate new Access Token
            val untilUnixTime =
                currentTime + (60 * 60 * 24) // Execute the method after 24 hours again
            SharedPrefHelper.getInstance().setAppleVerifyRefreshToken(untilUnixTime)
            verifyRefreshToken()
        } else {
            true
        }
    }


    // Show 'Sign in with Apple' login page in a dialog
    @SuppressLint("SetJavaScriptEnabled")
    fun setupAppleWebViewDialog(url: String) {
        appledialog = Dialog(this)
        val webView = WebView(this)
        webView.isVerticalScrollBarEnabled = false
        webView.isHorizontalScrollBarEnabled = false
        webView.webViewClient = AppleWebViewClient()
        webView.settings.javaScriptEnabled = true
//        webView.loadUrl(url)
        webView.loadUrl("https://appleid.apple.com/auth/authorize?client_id=com.hungama.myplay.staging.signin&redirect_uri=https%3A%2F%2Fuser.api.hungama.com%2Fv1%2Fapple-callback&response_type=code%20id_token&state=123456&scope=email&response_mode=form_post&frame_id=a3a2fc4d-8624-49d2-b74c-a9ba303d9c5f&m=12&v=1.5.4")
        appledialog.setContentView(webView)
        appledialog.show()
    }

    // A client to know about WebView navigation
    // For API 21 and above
    @Suppress("OverridingDeprecatedMember")
    inner class AppleWebViewClient : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            setLog("AppleLogin", "onPageStarted: request:${url}")
        }

        override fun onLoadResource(view: WebView?, url: String?) {
            super.onLoadResource(view, url)
            setLog("AppleLogin", "onLoadResource: request:${url}")
        }
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            setLog("AppleLogin", "shouldOverrideUrlLoading: request:${request?.url}")

            if (request?.url.toString().startsWith("https://www.hungama.com/")) {

                handleUrl(request?.url.toString())

                // Close the dialog after getting the authorization code
                if (request?.url.toString().contains("success=")) {
                    appledialog.dismiss()
                }
                return true
            }
            return true
        }

        // For API 19 and below
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            setLog("AppleLogin", "shouldOverrideUrlLoading: request:${url}")
            if (url.startsWith("https://www.hungama.com/")) {

                handleUrl(url)

                // Close the dialog after getting the authorization code
                if (url.contains("success=")) {
                    appledialog.dismiss()
                }
                return true
            }
            return false
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)

            setLog("AppleLogin", "onPageFinished: request:${url}")

            // retrieve display dimensions
            val displayRectangle = Rect()
            val window = this@LoginMainActivity.window
            window.decorView.getWindowVisibleDisplayFrame(displayRectangle)

            // Set height of the Dialog to 90% of the screen
            val layoutParams = view?.layoutParams
            layoutParams?.height = (displayRectangle.height() * 0.9f).toInt()
            view?.layoutParams = layoutParams
        }

        override fun shouldInterceptRequest(
            view: WebView?,
            request: WebResourceRequest?
        ): WebResourceResponse? {
            //setLog("requestoo", request?.url.toString() + " ===== " + request?.method)
            //val uri = Uri.parse(request?.url.toString())
            //val args = uri.queryParameterNames
            //setLog("QeuryPara", args.toString())
            //setLog("QeuryPara1", request?.requestHeaders.toString())
            return super.shouldInterceptRequest(view, request)
        }

        // Check WebView url for access token code or error
        @SuppressLint("LongLogTag")
        private fun handleUrl(url: String) {

            val uri = Uri.parse(url)
           // val server = uri.authority
            //val path = uri.path
            //val protocol = uri.scheme
            //val args = uri.queryParameterNames
            //setLog("QeuryParam", args.toString())
            val success = uri.getQueryParameter("success")
            if (success == "true") {

                // Get the Authorization Code from the URL
                appleAuthCode = uri.getQueryParameter("code") ?: ""
                setLog("Apple Code: ", appleAuthCode)

                // Get the Client Secret from the URL
                appleClientSecret = uri.getQueryParameter("client_secret") ?: ""
                setLog("Apple Client Secret: ", appleClientSecret)

                // Save the Client Secret (appleClientSecret) using SharedPreferences
                // This will allow us to verify if refresh Token is valid every time they open the app after cold start.
                SharedPrefHelper.getInstance().setAppleClientSecret(appleClientSecret)

                //Check if user gave access to the app for the first time by checking if the url contains their email
                if (url.contains("email")) {

                    //Get user's First Name
                    val firstName = uri.getQueryParameter("first_name")
                    setLog("Apple User First Name: ", firstName ?: "")
                    appleFirstName = firstName ?: "Not exists"

                    //Get user's Middle Name
                    val middleName = uri.getQueryParameter("middle_name")
                    setLog("Apple User Middle Name: ", middleName ?: "")
                    appleMiddleName = middleName ?: "Not exists"

                    //Get user's Last Name
                    val lastName = uri.getQueryParameter("last_name")
                    setLog("Apple User Last Name: ", lastName ?: "")
                    appleLastName = lastName ?: "Not exists"

                    //Get user's email
                    val email = uri.getQueryParameter("email")
                    setLog("Apple User Email: ", email ?: "Not exists")
                    appleEmail = email ?: ""
                }

                // Exchange the Auth Code for Access Token
                requestForAccessToken(appleAuthCode, appleClientSecret)
            } else if (success == "false") {
                setLog("ERROR", "We couldn't get the Auth Code")
            }
        }
    }

    private fun requestForAccessToken(code: String, clientSecret: String) {

        val grantType = "authorization_code"

        val postParamsForAuth =
            "grant_type=" + grantType + "&code=" + code + "&redirect_uri=" + AppleConstants.REDIRECT_URI + "&client_id=" + AppleConstants.CLIENT_ID + "&client_secret=" + clientSecret

        CoroutineScope(Dispatchers.Default).launch {
            val httpsURLConnection =
                withContext(Dispatchers.IO) { URL(AppleConstants.TOKENURL).openConnection() as HttpsURLConnection }
            httpsURLConnection.requestMethod = "POST"
            httpsURLConnection.setRequestProperty(
                "Content-Type",
                "application/x-www-form-urlencoded"
            )
            httpsURLConnection.doInput = true
            httpsURLConnection.doOutput = true
            withContext(Dispatchers.IO) {
                val outputStreamWriter = OutputStreamWriter(httpsURLConnection.outputStream)
                outputStreamWriter.write(postParamsForAuth)
                outputStreamWriter.flush()
            }
            val response = httpsURLConnection.inputStream.bufferedReader()
                .use { it.readText() }  // defaults to UTF-8

            val jsonObject = JSONTokener(response).nextValue() as JSONObject

            val accessToken = jsonObject.getString("access_token") // Here is the access token
            setLog("Apple Access Token is: ", accessToken)
            appleAccessToken = accessToken

            val expiresIn = jsonObject.getInt("expires_in") // When the access token expires
            setLog("expires in: ", expiresIn.toString())

            val refreshToken =
                jsonObject.getString("refresh_token") // The refresh token used to regenerate new access tokens. Store this token securely on your server.
            setLog("refresh token: ", refreshToken)

            // Save the RefreshToken Token (refreshToken) using SharedPreferences
            // This will allow us to verify if refresh Token is valid every time they open the app after cold start.
            SharedPrefHelper.getInstance().setAppleRefreshToken(refreshToken)


            val idToken =
                jsonObject.getString("id_token") // A JSON Web Token that contains the user’s identity information.
            setLog("ID Token: ", idToken)

            // Get encoded user id by splitting idToken and taking the 2nd piece
            val encodedUserID = idToken.split(".")[1]

            //Decode encodedUserID to JSON
            val decodedUserData = String(Base64.decode(encodedUserID, Base64.DEFAULT))
            val userDataJsonObject = JSONObject(decodedUserData)
            // Get User's ID
            val userId = userDataJsonObject.getString("sub")
            setLog("Apple User ID :", userId)
            appleId = userId

            withContext(Dispatchers.Main) {
                //openDetailsActivity()
                /*callSocialLogin(
                    appleId,
                    appleAccessToken,
                    appleEmail,
                    appleFirstName + " " + appleLastName,
                    "",
                    "apple"
                )*/
            }
        }
    }

    private fun openDetailsActivity() {
        val myIntent = Intent(baseContext, MainActivity::class.java)
        myIntent.putExtra("apple_id", appleId)
        myIntent.putExtra("apple_first_name", appleFirstName)
        myIntent.putExtra("apple_middle_name", appleMiddleName)
        myIntent.putExtra("apple_last_name", appleLastName)
        myIntent.putExtra("apple_email", appleEmail)
        myIntent.putExtra("apple_access_token", appleAccessToken)
        startActivity(myIntent)
    }

    private suspend fun verifyRefreshToken(): Boolean {

        // Verify Refresh Token only once a day
        val refreshToken = SharedPrefHelper.getInstance().getAppleRefreshToken()
        val clientSecret = SharedPrefHelper.getInstance().getAppleClientSecret()

        val postParamsForAuth =
            "grant_type=refresh_token" + "&client_id=" + AppleConstants.CLIENT_ID + "&client_secret=" + clientSecret + "&refresh_token=" + refreshToken

        val httpsURLConnection =
            withContext(Dispatchers.IO) { URL(AppleConstants.TOKENURL).openConnection() as HttpsURLConnection }
        httpsURLConnection.requestMethod = "POST"
        httpsURLConnection.setRequestProperty(
            "Content-Type",
            "application/x-www-form-urlencoded"
        )
        httpsURLConnection.doInput = true
        httpsURLConnection.doOutput = true
        withContext(Dispatchers.IO) {
            val outputStreamWriter = OutputStreamWriter(httpsURLConnection.outputStream)
            outputStreamWriter.write(postParamsForAuth)
            outputStreamWriter.flush()

        }
        return try {
            val response = httpsURLConnection.inputStream.bufferedReader()
                .use { it.readText() }  // defaults to UTF-8
            val jsonObject = JSONTokener(response).nextValue() as JSONObject
            val newAccessToken = jsonObject.getString("access_token")
            //Replace the Access Token on your server with the new one
            setLog("New Access Token: ", newAccessToken)
            true
        } catch (e: Exception) {
            setLog(
                "ERROR: ",
                "Refresh Token has expired or user revoked app credentials"
            )
            false
        }
    }

//    fun setUpSignInWithAppleOnClick(
//        fragmentManager: FragmentManager,
//        configuration: SignInWithAppleConfiguration,
//        callback: (SignInWithAppleResult) -> Unit
//    ) {
//        callLoginEvent("Apple")
//        val fragmentTag = "SignInWithAppleButton-SignInWebViewDialogFragment"
//        val service = SignInWithAppleService(fragmentManager, fragmentTag, configuration, callback)
//        service.show()
//    }

//    fun setUpSignInWithAppleOnClick(
//        fragmentManager: FragmentManager,
//        configuration: SignInWithAppleConfiguration,
//        callback: SignInWithAppleCallback
//    ) {
//        setUpSignInWithAppleOnClick(fragmentManager, configuration, callback.toFunction())
//    }
    private val sliderRunnable =
        java.lang.Runnable { viewPager2.currentItem = viewPager2.currentItem + 1 }

    fun getUserSubscriptionStatus() {
        val userSubscriptionViewModel = ViewModelProvider(
            this
        ).get(UserSubscriptionViewModel::class.java)
        if (ConnectionUtil(this).isOnline) {
            userSubscriptionViewModel?.getUserSubscriptionStatusDetail(this)?.observe(this,
                Observer {
                    when (it.status) {
                        Status.SUCCESS -> {
                            setProgressBarVisible(false)
                            redirectToHome()
                        }

                        Status.LOADING -> {
                            setProgressBarVisible(true)
                        }

                        Status.ERROR -> {
                            redirectToHome()
                            setProgressBarVisible(false)
                        }
                    }
                })
        }
    }



    private fun loadHEdata(data: HERespModel.Data?) {
        if(!data?.msisdn?.isNullOrEmpty()!! &&!data?.msisdn?.equals("9999999999")!!){
            val alertDialog = AlertDialog.Builder(this@LoginMainActivity)

            alertDialog.apply {
                //setIcon(R.drawable.hungama_text_icon)
                //setTitle("Hello")
                setMessage("Are you sure to login with ${data?.msisdnWithIsdCode}")
                setPositiveButton(getString(R.string.discover_str_13)){ dialogInterface: DialogInterface, i: Int ->
                    callHELogin(data!!)
                }
                setNegativeButton(getString(R.string.popup_str_75)){ dialogInterface: DialogInterface, i: Int ->
                    dialogInterface?.dismiss()
                }


            }.create().show()
        }
    }

    private fun callHELogin(data: HERespModel.Data) {

        if (ConnectionUtil(this@LoginMainActivity).isOnline) {
            showLoader()
            try {
                val jsonObject: JSONObject=JSONObject()

                jsonObject.put("silent_user_id",SharedPrefHelper.getInstance().get(PrefConstant.SILENT_USER_ID,""))


                val mainJson = JSONObject()
                val clientDataJson = JSONObject()
                val username = JSONObject()
                username.put(
                    "value",
                    "+"+data?.msisdnWithIsdCode
                )
                mainJson.put("process", "mobile_login")
                mainJson.put("HE", true)
                clientDataJson.put("username", username)



                val silent_user_id = JSONObject()
                silent_user_id.put(
                    "value",
                    SharedPrefHelper.getInstance().get(PrefConstant.SILENT_USER_ID,"")
                )
                clientDataJson.put("silent_user_id", silent_user_id)



                mainJson.put("method", "signup_login")
                mainJson.put("client_data", clientDataJson)

                setLog("TAG", "SSOmobileLogin HE:: ${mainJson}")
                userViewModel?.SSOmobileLogin(
                    this@LoginMainActivity,
                    mainJson.toString()
                )?.observe(this,
                    Observer {
                        when(it.status){
                            Status.SUCCESS->{
                                setProgressBarVisible(false)
                                dismissLoader()
                                if (it?.data != null) {
                                    setLog("TAG", "socialLoginRespObserver:: $it?.data")
                                    if(it?.data?.result?.data?.newUser!=null&&!TextUtils.isEmpty(it?.data?.result?.data?.newUser) && it?.data?.result?.data?.newUser.contains("true",true)){
                                        Utils.registerUserMethod_AF(
                                            "HE"
                                        )
                                    }else{
                                        val hashMap1 =
                                            java.util.HashMap<String, String>()
                                        hashMap1.put(EventConstant.METHOD_EPROPERTY,"HE")
                                        setLog("LOGIN","Success${hashMap1}")
                                        EventManager.getInstance().sendEvent(LoginSuccessEvent(hashMap1))
                                    }
                                    if (SharedPrefHelper.getInstance().isUserLoggedIn()){
                                        setLog("LoginSubscription", "LoginMainActivity-socialLogin-isUserLogdIn-true")
                                        getUserSubscriptionStatus()
                                    }else{
                                        setLog("LoginSubscription", "LoginMainActivity-socialLogin-isUserLogdIn-false")
                                        redirectToHome()
                                    }
                                }

                            }

                            Status.LOADING ->{
                                setProgressBarVisible(true)
                            }

                            Status.ERROR ->{
                                setProgressBarVisible(false)
                                dismissLoader()
                                val messageModel = MessageModel(it?.message.toString(), MessageType.NEUTRAL, true)
                                CommonUtils.showToast(this, messageModel)

                                val hashMap = HashMap<String,String>()
                                hashMap.put(EventConstant.ERRORTYPE_EPROPERTY,it.message!!)
                                hashMap.put(EventConstant.SOURCE_EPROPERTY,"HE")
                                setLog("LOGIN","Success${hashMap}")
                                EventManager.getInstance().sendEvent(LoginErrorEvent(hashMap))
                            }
                        }
                    })

            } catch (e: Exception) {
                e.printStackTrace()
                val messageModel = MessageModel(getString(R.string.discover_str_2), MessageType.NEUTRAL, true)
                CommonUtils.showToast(this, messageModel)
            }


        } else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),MessageType.NEGATIVE, true)
            CommonUtils.showToast(this, messageModel)
        }
    }

    private fun callLoginEvent(loginWith: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val dataMap=HashMap<String,String>()
            dataMap.put(EventConstant.LOGIN_EPROPERTY,"false")
            dataMap.put(EventConstant.METHOD_EPROPERTY,loginWith)
            dataMap.put(EventConstant.SCREEN_NAME_EPROPERTY,EventConstant.SOURCE_ONBOARDING)
            dataMap.put(EventConstant.SOURCE_EPROPERTY,EventConstant.SOURCE_ONBOARDING)
            EventManager.getInstance().sendEvent(LoginEvent(dataMap))
        }
    }

    private var GFG_URI = "https://appleid.apple.com/auth/authorize?client_id=com.hungama.myplay.staging.signin&redirect_uri=https%3A%2F%2Fuser.api.hungama.com%2Fv1%2Fapple-callback&response_type=code%20id_token&state=123456&scope=email&response_mode=form_post&frame_id=a3a2fc4d-8624-49d2-b74c-a9ba303d9c5f&m=12&v=1.5.4"
    private var package_name = "com.android.chrome";
    fun openCCT(){
        val builder = CustomTabsIntent.Builder()

        // to set the toolbar color use CustomTabColorSchemeParams
        // since CustomTabsIntent.Builder().setToolBarColor() is deprecated

        val params = CustomTabColorSchemeParams.Builder()
        params.setToolbarColor(ContextCompat.getColor(this@LoginMainActivity, R.color.colorPrimary))
        builder.setDefaultColorSchemeParams(params.build())

        // shows the title of web-page in toolbar
        builder.setShowTitle(true)

        // setShareState(CustomTabsIntent.SHARE_STATE_ON) will add a menu to share the web-page
        builder.setShareState(CustomTabsIntent.SHARE_STATE_OFF)

        // To modify the close button, use
        // builder.setCloseButtonIcon(bitmap)

        // to set weather instant apps is enabled for the custom tab or not, use
        builder.setInstantAppsEnabled(true)

        //  To use animations use -
        //  builder.setStartAnimations(this, android.R.anim.start_in_anim, android.R.anim.start_out_anim)
        //  builder.setExitAnimations(this, android.R.anim.exit_in_anim, android.R.anim.exit_out_anim)
        val customBuilder = builder.build()

        if (isPackageInstalled(package_name)) {
            // if chrome is available use chrome custom tabs
            customBuilder.intent.setPackage(package_name)
            customBuilder.launchUrl(this, Uri.parse(GFG_URI))
        } else {
            // if not available use WebView to launch the url
        }
    }
}