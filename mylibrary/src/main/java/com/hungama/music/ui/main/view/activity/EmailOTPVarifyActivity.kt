package com.hungama.music.ui.main.view.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.TextUtils
import android.view.HapticFeedbackConstants.CONTEXT_CLICK
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
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
import com.hungama.music.utils.preference.PrefConstant
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import com.hungama.music.data.model.MessageModel
import com.hungama.music.data.model.MessageType
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.eventreporter.*
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.home.eventreporter.UserAttributeEvent
import com.hungama.music.ui.main.viewmodel.UserSubscriptionViewModel
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.utils.CommonUtils.hideKeyboard
import com.hungama.music.utils.CommonUtils.setAppButton1
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.customview.pinview.Pinview
import kotlinx.android.synthetic.main.activity__mobile_otp_varify.*
import kotlinx.android.synthetic.main.activity_otp_varify.*
import kotlinx.android.synthetic.main.activity_otp_varify.btnVarify
import kotlinx.android.synthetic.main.activity_otp_varify.imageBack
import kotlinx.android.synthetic.main.activity_otp_varify.imageSuccess
import kotlinx.android.synthetic.main.activity_otp_varify.llFacebook
import kotlinx.android.synthetic.main.activity_otp_varify.llGoogle
import kotlinx.android.synthetic.main.activity_otp_varify.pinView
import kotlinx.android.synthetic.main.activity_otp_varify.progress
import kotlinx.android.synthetic.main.activity_otp_varify.progressBar
import kotlinx.android.synthetic.main.activity_otp_varify.tv_varify
import kotlinx.android.synthetic.main.activity_otp_varify.txtResend
import kotlinx.android.synthetic.main.activity_otp_varify.txtResend2
import kotlinx.android.synthetic.main.activity_otp_varify.txtTitle
import kotlinx.coroutines.*
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.NumberFormat

class EmailOTPVarifyActivity : AppCompatActivity() {

    // get reference of the firebase auth
    var userViewModel: UserViewModel? = null
    lateinit var auth: FirebaseAuth
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var storedVerificationId: String
    var email: String = ""
    var isOTPResend: Boolean = false;
    var isOTPVarify: Boolean = false;
    val RC_SIGN_IN = 100
    lateinit var googleSignInClient: GoogleSignInClient
    var callbackManager: CallbackManager?=null
    var SIGNUPMETHOD="EMAIL"
    var action = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_varify)
        action = intent.getIntExtra("action", 0)
        configureGoogleClient()
        configureFacebook()

        setUpViewModel()
        val hashMap = HashMap<String,String>()
        //As per jira bug - https://hungama.atlassian.net/browse/HU-3739
        // We are removing this(Success) property
        hashMap.put("Success","LOGIN_OTP_PAGE_LOAD_SUCCESS")
        setLog("LOGIN","LOGIN_OTP_PAGE_LOAD_SUCCESS${hashMap}")
        EventManager.getInstance().sendEvent(LoginOTPPageLoadSuccessEvent(hashMap))

        Utils.setWindowProperty(this@EmailOTPVarifyActivity)

        auth = FirebaseAuth.getInstance()
        startTimer()

        // get storedVerificationId from the intent
        storedVerificationId = intent.getStringExtra("storedVerificationId").toString()
        email = intent.getStringExtra("email").toString()

        if (email.isNotEmpty()) {
            txtTitle.text = getString(R.string.login_str_31) + " $email"
        }

        // fill otp and call the on click on button
        llMobile.setOnClickListener {
            val intent: Intent
            intent = Intent(this@EmailOTPVarifyActivity, EnterMobileNumberActivity::class.java)
            intent.putExtra("action", action)
            startActivityForResult(intent, Constant.SIGNIN_WITH_MOBILE)
            overridePendingTransition(R.anim.enter, R.anim.exit)
        }

        // click on Google button
        llGoogle.setOnClickListener {
            signInToGoogle()
        }

        // click on facebook button
        llFacebook.setOnClickListener {
            signInToFacebook()
        }
        // fill otp and call the on click on button
        btnVarify.setOnClickListener {
            //Toast.makeText(this, getString(R.string.enter_valid_otp), Toast.LENGTH_SHORT).show()
            try {
                  if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                CommonUtils.hapticVibration(this, btnVarify!!, CONTEXT_CLICK)
            }
            }catch (e:Exception){

            }
            Utils?.hideSoftKeyBoard(this@EmailOTPVarifyActivity,pinView)
            val otp = pinView.value.trim()
            if (otp.isNotEmpty()) {
                //API Call
                if (otp.length < 4) {
                    isOTPVarify = false;

                } else {
                    isOTPVarify = true;
                    progress.visibility = View.VISIBLE
                    imageSuccess?.visibility = View.GONE
                    tv_varify.text = getString(R.string.login_str_40)
                    //validateOtp(number, otp)
                    makeVerifyOtpCall(email, otp)
                }
            } else {
                isOTPVarify = false;
                val messageModel = MessageModel(getString(R.string.toast_str_34), getString(R.string.toast_str_3),MessageType.NEGATIVE, true)
                CommonUtils.showToast(this, messageModel)
            }
        }
        imageBack.setOnClickListener {
            hideKeyboard()
            finish()
        }
        txtResend2.setOnClickListener {
            //sendVerificationCode(number)
            if (isOTPResend) {

                //btnVarify.setBackgroundResource(R.drawable.corner_radius_18_bg_blue)
                setAppButton1(this, btnVarify)
                imageSuccess?.visibility = View.GONE
                tv_varify.text = getString(R.string.login_str_17)
                setProgressBarVisible(true)
                pinView.value = ""
                //generateOtp(number)
                startTimer()
                makeReSendOtpCall(email)
            } else {
                val messageModel = MessageModel("Wait for few time", MessageType.NEUTRAL, true)
                CommonUtils.showToast(this, messageModel)
            }
        }

        pinView.setPinViewEventListener(object : Pinview.PinViewEventListener {
            override fun onDataEntered(pinview: Pinview?, fromUser: Boolean) {
                val messageModel = MessageModel(pinview!!.value, MessageType.NEUTRAL, true)
                //CommonUtils.showToast(this@EmailOTPVarifyActivity, messageModel)
            }
        })
        val typeface = ResourcesCompat.getFont(
            this,
            R.font.sf_pro_text
        )
        pinView.apply {
            setTextColor(ContextCompat.getColor(this@EmailOTPVarifyActivity, R.color.colorWhite))
            if (typeface != null) {
                setTextTypeFace(typeface)
            }
            setTextSize(20)
        }
    }

    override fun onResume() {
        super.onResume()
        hideKeyboard()
    }

    override fun onDestroy() {
        super.onDestroy()
        hideKeyboard()
    }

    private fun callSocialLogin(
        uid: String,
        providerUid: String,
        email: String,
        userName:String,
        userImage: String,
        provider: String,
        firstname:String,
        lastname:String
    ) {
        if (ConnectionUtil(this@EmailOTPVarifyActivity).isOnline) {

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
                    this@EmailOTPVarifyActivity,
                    mainJson.toString()
                )?.observe(this,
                    Observer {
                        when(it.status){
                            Status.SUCCESS->{
                                setProgressBarVisible(false)
                                if (it?.data != null) {
                                    setLog("TAG", "socialLoginRespObserver:: req mainJson:${mainJson} respo: $it?.data")



                                    if(SharedPrefHelper.getInstance().isUserLoggedIn()){
                                        val userDataMap= java.util.HashMap<String, String>()
                                        userDataMap.put(EventConstant.LOG_IN_SOURCE, "onboarding")
                                        userDataMap.put(EventConstant.LOG_IN_METHOD, provider)
                                        EventManager.getInstance().sendUserAttribute(
                                            UserAttributeEvent(userDataMap)
                                        )
                                    }

                                    if (SharedPrefHelper.getInstance().isUserLoggedIn()){
                                        CommonUtils.setLog(
                                            "LoginSubscription",
                                            "EmailOTPVarifyActivity-socialLogin-isUserLogdIn-true"
                                        )
                                        getUserSubscriptionStatus()
                                    }else{
                                        CommonUtils.setLog(
                                            "LoginSubscription",
                                            "EmailOTPVarifyActivity-socialLogin-isUserLogdIn-false"
                                        )
                                        redirectToHome()
                                    }

                                    if(it?.data?.result?.data?.newUser!=null&&!TextUtils.isEmpty(it?.data?.result?.data?.newUser) && it.data.result.data.newUser.contains("true",true)){
                                        setLog(TAG, "callSocialLogin socialLogin : ${provider}")
                                        Utils.registerUserMethod_AF(
                                            provider
                                        )
                                    }else{
                                        CoroutineScope(Dispatchers.IO).launch {
                                            val hashMap = HashMap<String,String>()
                                            hashMap.put(EventConstant.METHOD_EPROPERTY,provider)
                                            setLog("LOGIN","Success${hashMap}")
                                            EventManager.getInstance().sendEvent(LoginSuccessEvent(hashMap))
                                        }

                                    }


                                }

                            }

                            Status.LOADING ->{
                                setProgressBarVisible(true)
                            }

                            Status.ERROR ->{
                                setProgressBarVisible(false)
                                onError(it?.message!!)
                            }
                        }
                    })

            } catch (e: Exception) {
                e.printStackTrace()
                Utils.showSnakbar(this,
                    emailOtpRoot!!,
                    false,
                    getString(R.string.discover_str_2)
                )
            }


        } else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),MessageType.NEGATIVE, true)
            CommonUtils.showToast(this, messageModel)
        }
    }

    private fun configureGoogleClient() {
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    /*private fun signInToGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
        showLoader()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                setLog("TAG", "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!, account.id!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                setLog("TAG", "Google sign in failed", e)
            }
        }
    }
*/


    private fun signInToFacebook() {
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
                    setLog("TAG", "uid-:"+user!!.uid + " provider_uid-:"+token.token + " name-:"+ user.displayName + " email-:"+ user.email + "name-:"+ user.photoUrl)
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
        if (user != null){
            callSocialLogin(user.uid, providerToken, user.email!!, user.displayName!!, user.photoUrl!!.toString(), provider, user.displayName!!, user.displayName!!)
        }

    }

    fun redirectToHome() {

        val userId = SharedPrefHelper.getInstance().getUserId().toString()
        if (userId.isNotEmpty()){
            AppsFlyerLib.getInstance().setCustomerIdAndLogSession(userId, this)
        }
//        val appsFlyerUserId = AppsFlyerProperties.getInstance().getString(AppsFlyerProperties.APP_USER_ID)
//        setLog("appsFlyerUserId", appsFlyerUserId.toString())

        if (action == Constant.SIGNIN_WITH_GOOGLE
            || action == Constant.SIGNIN_WITH_FACEBOOK
            || action == Constant.SIGNIN_WITH_MOBILE
            || action == Constant.SIGNIN_WITH_EMAIL
            || action == Constant.SIGNIN_WITH_APPLE
            || action == Constant.SIGNIN_WITH_ALL)
        {

           CoroutineScope(Dispatchers.Main).launch {
               delay(2000)
//               GamificationSDK.refreshPoints()
               overridePendingTransition(R.anim.enter, R.anim.exit)
               val returnIntent = Intent()
               returnIntent.putExtra("result", action)
               setResult(action, returnIntent)
               finish()

               setLog("GM-SDK-APP","redirectToHome called")
           }

        }
        else
        {
            val url = if (intent.hasExtra(Constant.DeepLink_Payment)) intent.getStringExtra(Constant.DeepLink_Payment) else ""
            val intent = Intent(this@EmailOTPVarifyActivity, MainActivity::class.java)
            if (url!!.isNotEmpty())
                intent.putExtra(Constant.DeepLink_Payment, url)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            overridePendingTransition(R.anim.enter, R.anim.exit)
            finish()
        }
        val intent = Intent(Constant.SONG_DURATION_BROADCAST)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    /*fun redirectToHome(){
        startActivity(Intent(applicationContext, MainActivity::class.java))
        *//*intent.putExtra("user", user)*//*
        finish()
    }*/

    fun showLoader() {
        if(!this.isFinishing){
            DisplayDialog.getInstance(this).showProgressDialog(this@EmailOTPVarifyActivity, false)
        }
    }

    fun dismissLoader() {
        if(!this.isFinishing) {
            DisplayDialog.getInstance(this).dismissProgressDialog()
        }
    }

    private fun startTimer() {
        val timer = object : CountDownTimer(3 * 60 * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                var diff = millisUntilFinished
                val secondsInMilli: Long = 1000
                val minutesInMilli = secondsInMilli * 60
                val hoursInMilli = minutesInMilli * 60
                val daysInMilli = hoursInMilli * 24

                val elapsedDays = diff / daysInMilli
                diff %= daysInMilli

                val elapsedHours = diff / hoursInMilli
                diff %= hoursInMilli

                val elapsedMinutes = diff / minutesInMilli
                diff %= minutesInMilli
                val elapsedSeconds = diff / secondsInMilli
                diff %= secondsInMilli
                val f: NumberFormat = DecimalFormat("00")

                isOTPResend = false
                txtResend.text = getString(R.string.login_str_44)
                txtResend2.text = getString(R.string.login_str_33)+" ${f.format(elapsedMinutes)} : ${f.format(elapsedSeconds)}"
                txtResend2.setTextColor(ContextCompat.getColor(this@EmailOTPVarifyActivity, R.color.item_sub_title_color))
                val typeface = ResourcesCompat.getFont(
                    this@EmailOTPVarifyActivity,
                    R.font.sf_pro_text_light
                )
                txtResend2.typeface = typeface
            }

            override fun onFinish() {
                isOTPResend = true
                txtResend.text = getString(R.string.login_str_44)
                txtResend2.text = getString(R.string.login_str_43)
                txtResend2.setTextColor(ContextCompat.getColor(this@EmailOTPVarifyActivity, R.color.colorWhite))
                val typeface = ResourcesCompat.getFont(
                    this@EmailOTPVarifyActivity,
                    R.font.sf_pro_text
                )
                txtResend2.typeface = typeface
            }
        }
        timer.start()
    }


    private fun setUpViewModel() {
        userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)
        setProgressBarVisible(false)
    }


    private fun setProgressBarVisible(it: Boolean) {
        if (it) {
            if(!isOTPVarify){
                progressBar?.visibility = View.VISIBLE
            }
        } else {
            progressBar?.visibility = View.GONE
        }
    }

    fun onError(msg:String){
        if (msg != null) {
            if (isOTPVarify){
                isOTPVarify=false
                btnVarify.setBackgroundResource(R.drawable.corner_radius_18_bg_red)
                progress.visibility = View.GONE
                imageSuccess?.setImageDrawable(this.faDrawable(R.string.icon_error, R.color.colorWhite))
                imageSuccess.visibility = View.VISIBLE
                tv_varify.text = getString(R.string.login_str_54)

            }
            setLog("TAG", "onErrorObserver $msg")
            val messageModel = MessageModel(msg, MessageType.NEUTRAL, true)
            CommonUtils.showToast(this, messageModel)

            CoroutineScope(Dispatchers.IO).launch {
                val hashMap = HashMap<String,String>()
                hashMap.put(EventConstant.ERRORTYPE_EPROPERTY,msg)
                hashMap.put(EventConstant.SOURCE_EPROPERTY,"Email")
                setLog("LOGIN","Success${hashMap}")
                EventManager.getInstance().sendEvent(LoginErrorEvent(hashMap))
            }

        }
    }


    private fun makeVerifyOtpCall(email: String, otp: String) {
        userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)

        if (ConnectionUtil(this).isOnline) {
            userViewModel?.verifyOTPEmail(this, email, otp)?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                val mainJson = JSONObject()
                                mainJson.put("emailId", email)
                                userViewModel?.SSOmobileLogin(this, mainJson.toString())?.observe(this,
                                    Observer {
                                        when(it.status){
                                            com.hungama.music.data.webservice.utils.Status.SUCCESS->{
                                                setProgressBarVisible(false)
                                                if (it?.data!=null){
                                                    if (isOTPResend) {
                                                        isOTPResend = false
                                                        startTimer()
                                                    }
                                                    setLog( "makeVerifyOtpCallEmail"," "+ it?.data?.result?.data?.newUser.toString())

                                                    if(it?.data?.result?.data?.newUser!=null&&
                                                        !TextUtils.isEmpty(it?.data?.result?.data?.newUser) &&
                                                        it.data.result.data.newUser.contains("true",true))
                                                    {
                                                        setLog( "makeVerifyOtpCallEmail"," "+
                                                                it?.data?.result?.data?.newUser.toString() + " "+
                                                                SharedPrefHelper.getInstance().get(PrefConstant.AF_COMPLETE_REGISTRATION, true))

                                                            Utils.registerUserMethod_AF("Email")


                                                    }else{
                                                        CoroutineScope(Dispatchers.IO).launch {
                                                            val hashMap1 =
                                                                java.util.HashMap<String, String>()
                                                            hashMap1.put(EventConstant.METHOD_EPROPERTY,"Email")
                                                            setLog("LOGIN","Success${hashMap1}")
                                                            EventManager.getInstance().sendEvent(LoginSuccessEvent(hashMap1))
                                                        }

                                                    }

                                                    if (isOTPVarify) {
                                                        isOTPVarify=false
                                                        tv_varify.text = getString(R.string.login_str_41)
                                                        progress.visibility = View.GONE
                                                        imageSuccess?.setImageDrawable(this.faDrawable(R.string.icon_success, R.color.colorWhite))
                                                        imageSuccess.visibility = View.VISIBLE
                                                        btnVarify.setBackgroundResource(R.drawable.corner_radius_18_bg_green)
                                                        if (SharedPrefHelper.getInstance().isUserLoggedIn()){
                                                            CommonUtils.setLog(
                                                                "LoginSubscription",
                                                                "EmailOTPVarifyActivity-SSOmobileLogin-isUserLogdIn-true"
                                                            )
                                                            getUserSubscriptionStatus()
                                                        }else{
                                                            CommonUtils.setLog(
                                                                "LoginSubscription",
                                                                "EmailOTPVarifyActivity-SSOmobileLogin-isUserLogdIn-false"
                                                            )
                                                            redirectToHome()
                                                        }
                                                    }


                                                }
                                            }

                                            com.hungama.music.data.webservice.utils.Status.LOADING ->{
                                                setProgressBarVisible(true)
                                            }

                                            com.hungama.music.data.webservice.utils.Status.ERROR ->{
                                                setProgressBarVisible(false)
                                            }
                                        }
                                    })
                            }

                        }

                        Status.LOADING ->{
                            setProgressBarVisible(true)
                        }

                        Status.ERROR ->{
                            val messageModel = MessageModel(it.message.toString(), MessageType.NEGATIVE, true)
                            CommonUtils.showToast(this, messageModel)
                            setProgressBarVisible(false)
                            isOTPVarify=false
                            btnVarify.setBackgroundResource(R.drawable.corner_radius_18_bg_red)
                            progress.visibility = View.GONE
                            imageSuccess?.setImageDrawable(this.faDrawable(R.string.icon_error, R.color.colorWhite))
                            imageSuccess.visibility = View.VISIBLE
                            tv_varify.text = getString(R.string.login_str_54)

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
        } else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),MessageType.NEGATIVE, true)
            CommonUtils.showToast(this, messageModel)
        }
        setProgressBarVisible(true)
    }



    private fun makeReSendOtpCall(email: String) {
        userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)

        if (ConnectionUtil(this).isOnline) {
            CoroutineScope(Dispatchers.IO).launch {
                val hashMap  = HashMap<String,String>()
                hashMap.put(EventConstant.SOURCE_EPROPERTY,""+email)
                setLog("LOGIN","RESEND OTP${hashMap}")
                EventManager.getInstance().sendEvent(LoginOTPResendEvent(hashMap))
            }

            userViewModel?.generateOTPEmail(this, email)?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                setLog("TAG", "generateOTPCallRespObserver:: $it")
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
        } else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),MessageType.NEGATIVE, true)
            CommonUtils.showToast(this, messageModel)
        }

        setProgressBarVisible(true)
    }



    private fun signInToGoogle() {
        showLoader()
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Pass the activity result back to the Facebook SDK
        callbackManager?.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)


        if (requestCode == Constant.SIGNIN_WITH_GOOGLE || requestCode == Constant.SIGNIN_WITH_FACEBOOK
            || requestCode == Constant.SIGNIN_WITH_MOBILE || requestCode == Constant.SIGNIN_WITH_EMAIL
            || requestCode == Constant.SIGNIN_WITH_APPLE || requestCode == Constant.SIGNIN_WITH_ALL
        ){
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
            }catch (e:Exception){

            }
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
                val ids = graphJson.optString(LoginMainActivity.ID)
                val names = graphJson.optString(LoginMainActivity.NAME)
                val gender = graphJson.optString(LoginMainActivity.GENDER)
                val emails = graphJson.optString(LoginMainActivity.EMAIL)
                val first_name = graphJson.optString(LoginMainActivity.FIRST_NAME)
                val last_name = graphJson.optString(LoginMainActivity.LAST_NAME)
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
}
