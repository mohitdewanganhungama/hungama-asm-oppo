package com.hungama.music.ui.main.view.activity

import android.app.Activity
import android.content.*
import android.os.Bundle
import android.os.CountDownTimer
import android.text.TextUtils
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.appcompat.app.AppCompatActivity
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
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.hungama.music.data.model.MessageModel
import com.hungama.music.data.model.MessageType
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.LoginErrorEvent
import com.hungama.music.eventanalytic.eventreporter.LoginOTPPageLoadSuccessEvent
import com.hungama.music.eventanalytic.eventreporter.LoginSuccessEvent
import com.hungama.music.home.eventreporter.UserAttributeEvent
import com.hungama.music.ui.main.viewmodel.UserSubscriptionViewModel
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.utils.CommonUtils.hideKeyboard
import com.hungama.music.utils.CommonUtils.setAppButton1
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.preference.PrefConstant
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import kotlinx.android.synthetic.main.act_login_main.*
import kotlinx.android.synthetic.main.activity__mobile_otp_varify.*
import kotlinx.android.synthetic.main.activity__mobile_otp_varify.llEmail
import kotlinx.android.synthetic.main.activity__mobile_otp_varify.llFacebook
import kotlinx.android.synthetic.main.activity__mobile_otp_varify.llGoogle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.concurrent.TimeUnit

class MobileOTPVarifyActivity : AppCompatActivity() {

    // get reference of the firebase auth
    var userViewModel: UserViewModel? = null
    lateinit var auth: FirebaseAuth
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var storedVerificationId: String
    var number: String = ""
    var countryCode: String = ""
    var isOTPResend: Boolean = false;
    var isOTPVarify: Boolean = false;

    val RC_SIGN_IN = 100
    lateinit var googleSignInClient: GoogleSignInClient
    var callbackManager: CallbackManager? = null
    var SIGNUPMETHOD = "EMAIL"
    var action = 0

    private lateinit var newToken :  PhoneAuthProvider.ForceResendingToken

    lateinit var smsBroadcastReceiver: SmsBroadcastReceiver

    companion object {
        val REQ_USER_CONSENT = 2  // Set to an unused request code
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity__mobile_otp_varify)
        action = intent.getIntExtra("action", 0)
        setLog("IAMHere","Here")

        CoroutineScope(Dispatchers.IO).launch {
            val hashMap = HashMap<String,String>()
            //As per jira bug - https://hungama.atlassian.net/browse/HU-3739
            // We are removing this(Success) property
            //hashMap.put("Success","LOGIN_OTP_PAGE_LOAD_SUCCESS")
            setLog("LOGIN","LOGIN_OTP_PAGE_LOAD_SUCCESS${hashMap}")
            EventManager.getInstance().sendEvent(LoginOTPPageLoadSuccessEvent(hashMap))
        }



        Utils.setWindowProperty(this@MobileOTPVarifyActivity)

        auth = FirebaseAuth.getInstance()
        // Force reCAPTCHA flow
        //auth.firebaseAuthSettings.forceRecaptchaFlowForTesting(true)
        startTimer()

        // get storedVerificationId from the intent
        storedVerificationId = intent.getStringExtra("storedVerificationId").toString()
        number = intent.getStringExtra("mobile").toString()
        countryCode = intent.getStringExtra("countryCode").toString()

        if (number.isNotEmpty()) {
            txtTitle.text = getString(R.string.login_str_64)+" $countryCode $number"
        }

        // fill otp and call the on click on button
        llEmail.setOnClickListener {
            val intent: Intent
            intent = Intent(this@MobileOTPVarifyActivity, EnterEmailActivity::class.java)
            intent.putExtra("action", action)
            startActivityForResult(intent, Constant.SIGNIN_WITH_EMAIL)
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
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(
                        this, btnVarify!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }

                val otp = pinView.value
                if (otp != null && !TextUtils.isEmpty(otp)) {
                    //API Call
                    if (otp.length < 6) {
                        isOTPVarify = false;
                        val messageModel = MessageModel(
                            getString(R.string.toast_str_34),
                            getString(R.string.toast_str_3),
                            MessageType.NEGATIVE,
                            true
                        )
                        CommonUtils.showToast(this, messageModel)

                    } else {
                        isOTPVarify = true;
                        progress.visibility = View.VISIBLE
                        imageSuccess?.visibility = View.GONE
                        setAppButton1(this,btnVarify)
                        tv_varify.text = getString(R.string.login_str_40)
                        validateOtp(number, otp);

                    }
                } else {
                    isOTPVarify = false;
                    val messageModel = MessageModel(
                        getString(R.string.toast_str_34),
                        getString(R.string.toast_str_3),
                        MessageType.NEGATIVE,
                        true
                    )
                    CommonUtils.showToast(this, messageModel)
                }

            } catch (e: Exception) {
                setLog("MobileOtpLogin", "MobileOTPVarifyActivity-btnVarify-btnVarify.click-error-${e.message}")
            }


        }
        imageBack.setOnClickListener {
            hideKeyboard()
            finish()
        }
        txtResend2.setOnClickListener {
            setLog(TAG, "onCreate: is otp resend ${isOTPResend}")
            //sendVerificationCode(number)
            if (isOTPResend) {
                //btnVarify.setBackgroundResource(R.drawable.corner_radius_18_bg_blue)
                setAppButton1(this, btnVarify)
                imageSuccess?.visibility = View.GONE
                tv_varify.text = getString(R.string.login_str_17)
                setProgressBarVisible(true)
                pinView.value = ""
                setLog(TAG, "onCreate: resend otp contry code ${countryCode+number}")
                val options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(countryCode+number) // Phone number to verify
                    .setTimeout(0, TimeUnit.SECONDS) // Timeout and unit
                    .setActivity(this) // Activity (for callback binding)
                    .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
                    .build()
                PhoneAuthProvider.verifyPhoneNumber(options)
                startTimer()
//                makeReSendOtpCall(number, countryCode)
            } else {
                val messageModel = MessageModel("Wait for few time", MessageType.NEUTRAL, true)
                CommonUtils.showToast(this, messageModel)
            }
        }

        // Callback function for Phone Auth
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            // This method is called when the verification is completed
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(credential)
                setLog("GFG", "onVerificationCompleted Success")
            }

            // Called when verification is failed add log statement to see the exception
            override fun onVerificationFailed(e: FirebaseException) {
                setLog("GFG", "onVerificationFailed $e")
                try {
                    if (e is FirebaseAuthInvalidCredentialsException) {
                        // Invalid request
                        val messageModel = MessageModel(getString(R.string.please_enter_a_valid_phone_number),MessageType.NEGATIVE, true)
                        CommonUtils.showToast(this@MobileOTPVarifyActivity, messageModel)
                    } else if (e is FirebaseTooManyRequestsException) {
                        // The SMS quota for the project has been exceeded
                        val messageModel = MessageModel("onVerificationFailed "+e.message.toString(),MessageType.NEGATIVE, true)
                        CommonUtils.showToast(this@MobileOTPVarifyActivity, messageModel)
                    }
                }catch (e:Exception){
                    setLog("MobileOtpLogin", "MobileOTPVarifyActivity-error-${e.message}")
                }
            }

            // On code is sent by the firebase this method is called
            // in here we start a new activity where user can enter the OTP
            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                setLog("GFG", "onCodeSent: $verificationId")
                storedVerificationId = verificationId
                newToken = token

            }
        }

        val typeface = ResourcesCompat.getFont(
            this,
            R.font.sf_pro_text
        )

        pinView.apply {
            setTextColor(ContextCompat.getColor(this@MobileOTPVarifyActivity, R.color.colorWhite))
            if (typeface != null) {
                setTextTypeFace(typeface)
            }
            setTextSize(20)
        }
        configureGoogleClient()
        configureFacebook()

        setUpViewModel()
        startSmsUserConsent()
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
        if (ConnectionUtil(this@MobileOTPVarifyActivity).isOnline) {

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
                    this@MobileOTPVarifyActivity,
                    mainJson.toString()
                )?.observe(this,
                    Observer {
                        when(it.status){
                            Status.SUCCESS->{
                                setProgressBarVisible(false)
                                if (it != null) {
                                    setLog("TAG", "socialLoginRespObserver:: $it")
                                    if (SharedPrefHelper.getInstance().isUserLoggedIn()){
                                        setLog("LoginSubscription", "MobileOTPVarifyActivity-socialLogin-isUserLogdIn-true")
                                        getUserSubscriptionStatus()
                                    }else{
                                        setLog("LoginSubscription", "MobileOTPVarifyActivity-socialLogin-isUserLogdIn-false")
                                        redirectToHome()
                                    }
                                    if(it?.data?.result?.data?.newUser!=null&&!TextUtils.isEmpty(it?.data?.result?.data?.newUser) && it?.data?.result?.data?.newUser.contains("true",true)){
                                        setLog(TAG, "MobileOTPVarifyActivity socialLogin:: $provider")
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



                                }

                            }

                            Status.LOADING ->{
                                setProgressBarVisible(true)
                            }

                            Status.ERROR ->{
                                setProgressBarVisible(false)
                                if (it != null) {
                                    if (isOTPVarify){
                                        isOTPVarify=false
                                        btnVarify.setBackgroundResource(R.drawable.corner_radius_18_bg_red)
                                        progress.visibility = View.GONE
                                        imageSuccess?.setImageDrawable(this.faDrawable(R.string.icon_error, R.color.colorWhite))
                                        imageSuccess.visibility = View.VISIBLE
                                        tv_varify.text = getString(R.string.login_str_54)

                                    }

                                    setLog("TAG", "onErrorObserver $it")
                                    val messageModel = MessageModel(it?.message.toString(), MessageType.NEGATIVE, true)
                                    CommonUtils.showToast(this, messageModel)

                                    CoroutineScope(Dispatchers.IO).launch {
                                        val hashMap = HashMap<String,String>()
                                        hashMap.put(EventConstant.ERRORTYPE_EPROPERTY,it?.message!!)
                                        hashMap.put(EventConstant.SOURCE_EPROPERTY,"Mobile")
                                        setLog("LOGIN","Success${hashMap}")
                                        EventManager.getInstance().sendEvent(LoginErrorEvent(hashMap))
                                    }

                                }
                            }
                        }
                    })

            } catch (e: Exception) {
                e.printStackTrace()
                val messageModel = MessageModel(getString(R.string.discover_str_2), MessageType.NEGATIVE, true)
                CommonUtils.showToast(this, messageModel)
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



    fun loginSuccess(user: FirebaseUser?, provider: String, providerToken: String) {
        if (user != null){
            callSocialLogin(user.uid, providerToken, user.email!!, user.displayName!!, user.photoUrl!!.toString(), provider, user.displayName!!, user.displayName!!)
        }

    }

    fun redirectToHome() {

        if(SharedPrefHelper.getInstance().isUserLoggedIn()){
            val userId = SharedPrefHelper.getInstance().getUserId().toString()
            if (userId.isNotEmpty()){
                AppsFlyerLib.getInstance().setCustomerIdAndLogSession(userId, this)
            }

/*            val appsFlyerUserId = AppsFlyerProperties.getInstance().getString(AppsFlyerProperties.APP_USER_ID)
            setLog("appsFlyerUserId", appsFlyerUserId.toString())*/

            val userDataMap= java.util.HashMap<String, String>()
            userDataMap.put(EventConstant.LOG_IN_SOURCE, "onboarding")
            userDataMap.put(EventConstant.LOG_IN_METHOD, "Mobile")
            EventManager.getInstance().sendUserAttribute(UserAttributeEvent(userDataMap))

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

                val url = if (intent.hasExtra(Constant.DeepLink_Payment)) intent.getStringExtra(Constant.DeepLink_Payment).toString() else ""
                val intent = Intent(this@MobileOTPVarifyActivity, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                if (url.isNotEmpty()) {
                    intent.putExtra(Constant.DeepLink_Payment, url)
                }
                startActivity(intent)
                overridePendingTransition(R.anim.enter, R.anim.exit)
                finish()


            }
        }
        val intent = Intent(Constant.SONG_DURATION_BROADCAST)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    fun showLoader() {
        if(!this.isFinishing){
            DisplayDialog.getInstance(this).showProgressDialog(this@MobileOTPVarifyActivity, false)
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
                txtResend2.setTextColor(ContextCompat.getColor(this@MobileOTPVarifyActivity, R.color.item_sub_title_color))
                val typeface = ResourcesCompat.getFont(
                    this@MobileOTPVarifyActivity,
                    R.font.sf_pro_text_light
                )
                txtResend2.typeface = typeface
            }

            override fun onFinish() {
                setLog(TAG, "onFinish: work ")
                isOTPResend = true
                txtResend.text = getString(R.string.login_str_44)
                txtResend2.text = getString(R.string.login_str_43)
                txtResend2.setTextColor(ContextCompat.getColor(this@MobileOTPVarifyActivity, R.color.colorWhite))
                val typeface = ResourcesCompat.getFont(
                    this@MobileOTPVarifyActivity,
                    R.font.sf_pro_text
                )
                txtResend2.typeface = typeface
            }
        }
        timer.start()
    }


    private fun validateOtp(number: String, otp: String) {
        try {
            //makeVerifyOtpCall(number, otp, countryCode)
            setLog("GFG", "validateOtp: $otp")
            setLog("MobileOtpLogin", "MobileOTPVarifyActivity-validateOtp-otp-${otp}")
            val credential : PhoneAuthCredential = PhoneAuthProvider.getCredential(
                    storedVerificationId.toString(), otp)
                signInWithPhoneAuthCredential(credential)
        } catch (e: Exception) {
            e.printStackTrace()
            val messageModel = MessageModel(getString(R.string.discover_str_2), MessageType.NEGATIVE, true)
            CommonUtils.showToast(this, messageModel)
        }
    }

    // verifies if the code matches sent by firebase
    // if success start the new activity in our case it is main Activity
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        setLog("GFG", "signInWithPhoneAuthCredential: $credential")
        setLog("MobileOtpLogin", "MobileOTPVarifyActivity-signInWithPhoneAuthCredential-credential-$credential")
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    setLog("MobileOtpLogin", "MobileOTPVarifyActivity-signInWithPhoneAuthCredential-task.isSuccessful-true")
                    setLog("GFG", "signInWithPhoneAuthCredential: task.isSuccessful")
                   /* SharedPrefHelper.getInstance().save(PrefConstant.ISLOGIN,true)
                    val intent = Intent(this , MainActivity::class.java)
                    startActivity(intent)
                    finish()*/

                    val mainJson = JSONObject()
                    mainJson.put("mobNo", countryCode+number)
                    callSSOMobileApi(mainJson)

                } else {
                    setLog("MobileOtpLogin", "MobileOTPVarifyActivity-signInWithPhoneAuthCredential-task.isSuccessful-false")
                    setLog("GFG", "signInWithPhoneAuthCredential: task.Error")
                    isOTPVarify=false
                    btnVarify.setBackgroundResource(R.drawable.corner_radius_18_bg_red)
                    progress.visibility = View.GONE
                    imageSuccess?.setImageDrawable(this.faDrawable(R.string.icon_error, R.color.colorWhite))
                    imageSuccess.visibility = View.VISIBLE
                    tv_varify.text = getString(R.string.login_str_54)
                    val hashMap = HashMap<String,String>()
                    // Sign in failed, display a message and update the UI
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {

                        // The verification code entered was invalid
                        val messageModel = MessageModel(getString(R.string.toast_str_34), getString(R.string.toast_str_3),MessageType.NEGATIVE, true)
                        CommonUtils.showToast(this, messageModel)
                        try {
                            setLog("GFG", "signInWithPhoneAuthCredential: task.Error-"+(task.exception as FirebaseAuthInvalidCredentialsException).errorCode)
                            hashMap.put(EventConstant.ERRORTYPE_EPROPERTY,
                                (task.exception as FirebaseAuthInvalidCredentialsException).errorCode)
                        }catch (e:Exception){
                            setLog("MobileOtpLogin", "MobileOTPVarifyActivity-signInWithPhoneAuthCredential-task.isSuccessful-false-error-${e.message}")
                        }

                    }

                    hashMap.put(EventConstant.SOURCE_EPROPERTY,"Mobile")
                    setLog("LOGIN","SuccessForMobile${hashMap}")
                    EventManager.getInstance().sendEvent(LoginErrorEvent(hashMap))
                }
            }
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



    private fun makeVerifyOtpCall(number: String, otp: String, countryCode: String) {
        userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)

        if (ConnectionUtil(this).isOnline) {
            userViewModel?.verifyOTPNumber(this, number, otp, countryCode)?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it != null) {
                                val mainJson = JSONObject()
                                mainJson.put("mobNo", countryCode+number)
                                callSSOMobileApi(mainJson)
                            }

                        }

                        Status.LOADING ->{
                            setProgressBarVisible(true)
                        }

                        Status.ERROR ->{
//                            val messageModel = MessageModel(it.message.toString(), MessageType.NEGATIVE, true)
//                            CommonUtils.showToast(this, messageModel)
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
                                hashMap.put(EventConstant.SOURCE_EPROPERTY,"Mobile")
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


    private fun makeReSendOtpCall(number: String, countryCode: String) {
        userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)

        if (ConnectionUtil(this).isOnline) {
            userViewModel?.generateOTPNumber(this, number, countryCode)?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it != null) {
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


    /**
     * Get user's basic profile data from facebook.
     */
    private fun getFacebookProfileData(loginResult: LoginResult) {
        val request = GraphRequest.newMeRequest(
            loginResult.accessToken
        ) { `object`, response ->
            parseFacebookData(`object`, AccessToken.getCurrentAccessToken()?.token!!)
            setLog("TAG", "FACEBOOK TOKEN : " + AccessToken.getCurrentAccessToken()?.token!!)

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

    override fun onStart() {
        super.onStart()
        registerToSmsBroadcastReceiver()
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(smsBroadcastReceiver)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Pass the activity result back to the Facebook SDK
        callbackManager?.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
        setLog("TAG", "onActivityResult requestCode:${requestCode}")
        setLog("TAG", "onActivityResult resultCode:${resultCode}")
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
                setLog( "Google sign in failed", e.message.toString())
            }catch (e:Exception){

            }
        } else if (requestCode == REQ_USER_CONSENT) {
            try {
                if ((resultCode == Activity.RESULT_OK) && (data != null)) {
                    //That gives all message to us. We need to get the code from inside with regex
                    val message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
                    val code = message?.let { fetchVerificationCode(it) }

                    hideKeyboard(pinView!!)
                    setLog("TAG", "OTP in code:${code} message:${message}")
                    pinView.value=""+code
                    btnVarify?.performClick()
                }else{
                    unregisterReceiver(smsBroadcastReceiver)
                    registerToSmsBroadcastReceiver()
                }
            }catch (e:Exception){
                setLog("MobileOtpLogin", "MobileOTPVarifyActivity-onActivityResult-REQ_USER_CONSENT-error-${e.message}")
            }

        }
    }

    private fun startSmsUserConsent() {
        SmsRetriever.getClient(this).also {
            //We can add user phone number or leave it blank
            it.startSmsUserConsent(null)
                .addOnSuccessListener {
                    setLog("MobileOtpLogin", "MobileOTPVarifyActivity-startSmsUserConsent-addOnSuccessListener")
                    setLog("TAG", "autp otp LISTENING_SUCCESS")
                }
                .addOnFailureListener {
                    setLog("MobileOtpLogin", "MobileOTPVarifyActivity-startSmsUserConsent-addOnFailureListener")
                    setLog("TAG", "autp otp LISTENING_FAILURE")
                }
        }
    }

    private fun registerToSmsBroadcastReceiver() {
        smsBroadcastReceiver = SmsBroadcastReceiver().also {
            it.smsBroadcastReceiverListener = object : SmsBroadcastReceiver.SmsBroadcastReceiverListener {
                override fun onSuccess(intent: Intent?) {
                    setLog("MobileOtpLogin", "MobileOTPVarifyActivity-registerToSmsBroadcastReceiver-onSuccess")
                    setLog("TAG", "autp otp registerToSmsBroadcastReceiver onSuccess:")
                    intent?.let { context -> startActivityForResult(context, REQ_USER_CONSENT) }
                }

                override fun onFailure() {
                    setLog("MobileOtpLogin", "MobileOTPVarifyActivity-registerToSmsBroadcastReceiver-onFailure")
                    setLog("TAG", "autp otp registerToSmsBroadcastReceiver onFailure:")
                }
            }
        }

        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        registerReceiver(smsBroadcastReceiver, intentFilter)

        setLog("TAG", "autp otp registerToSmsBroadcastReceiver:")
    }

    private fun fetchVerificationCode(message: String): String {
        var otpCode = ""
        try {
            setLog("TAG", "autp otp fetchVerification Code message:${message} ")
            otpCode=Regex("(\\d{6})").find(message)?.value ?: ""
            setLog("MobileOtpLogin", "MobileOTPVarifyActivity-fetchVerificationCode-otpCode-${otpCode}")
            /**
             * for otp received from API
             */
//        val otpCode=Regex("(\\d{4})").find(message)?.value ?: ""
            setLog("TAG", "autp otp fetchVerification otpCode:${otpCode} Code message:${message} ")
        }catch (e:Exception){
            setLog("MobileOtpLogin", "MobileOTPVarifyActivity-fetchVerificationCode-error-${e.message}")
        }

        return otpCode
    }


    class SmsBroadcastReceiver : BroadcastReceiver() {

        lateinit var smsBroadcastReceiverListener: SmsBroadcastReceiverListener

        override fun onReceive(context: Context?, intent: Intent?) {

            if (intent?.action == SmsRetriever.SMS_RETRIEVED_ACTION) {

                val extras = intent.extras
                val smsRetrieverStatus = extras?.get(SmsRetriever.EXTRA_STATUS) as com.google.android.gms.common.api.Status

                when (smsRetrieverStatus.statusCode) {
                    CommonStatusCodes.SUCCESS -> {
                        setLog("MobileOtpLogin", "MobileOTPVarifyActivity-SmsBroadcastReceiver-SUCCESS")
                        extras.getParcelable<Intent>(SmsRetriever.EXTRA_CONSENT_INTENT).also {
                            setLog("TAG", "autp otp onReceive:${it}")
                            smsBroadcastReceiverListener.onSuccess(it)
                        }
                    }

                    CommonStatusCodes.TIMEOUT -> {
                        setLog("MobileOtpLogin", "MobileOTPVarifyActivity-SmsBroadcastReceiver-TIMEOUT")
                        smsBroadcastReceiverListener.onFailure()
                    }
                }
            }
        }

        interface SmsBroadcastReceiverListener {
            fun onSuccess(intent: Intent?)
            fun onFailure()
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

    fun callSSOMobileApi(mainJson: JSONObject) {
        userViewModel?.SSOmobileLogin(this, mainJson.toString())?.observe(this,
            Observer {
                when(it.status){
                    com.hungama.music.data.webservice.utils.Status.SUCCESS->{

                        setProgressBarVisible(false)
                        if (it?.data!=null) {
                            if (isOTPResend) {
                                isOTPResend = false
                                startTimer()
                            }
                            if (isOTPVarify) {
                                isOTPVarify=false
                                tv_varify.text = getString(R.string.login_str_41)
                                progress.visibility = View.GONE
                                imageSuccess?.setImageDrawable(this.faDrawable(R.string.icon_success, R.color.colorWhite))
                                imageSuccess.visibility = View.VISIBLE
                                btnVarify.setBackgroundResource(R.drawable.corner_radius_18_bg_green)
                                SharedPrefHelper.getInstance().save(
                                    PrefConstant.ISLOGIN,true)
                                setLog("LOGIN", "UserId-"+SharedPrefHelper.getInstance().getUserId())



                                if(it?.data?.result?.data?.newUser!=null&&!TextUtils.isEmpty(it?.data?.result?.data?.newUser) && it?.data?.result?.data?.newUser.contains("true",true)){
                                    Utils.registerUserMethod_AF("Mobile")
                                }else{
                                    val hashMap1 =
                                        java.util.HashMap<String, String>()
                                    hashMap1.put(EventConstant.METHOD_EPROPERTY,"Mobile")
                                    setLog("LOGIN","Success${hashMap1}")
                                    EventManager.getInstance().sendEvent(LoginSuccessEvent(hashMap1))
                                }
                                if (SharedPrefHelper.getInstance().isUserLoggedIn()){
                                    CommonUtils.setLog(
                                        "LoginSubscription",
                                        "MobileOTPVarifyActivity-SSOmobileLogin-isUserLogdIn-true"
                                    )
                                    getUserSubscriptionStatus()
                                }else{
                                    CommonUtils.setLog(
                                        "LoginSubscription",
                                        "MobileOTPVarifyActivity-SSOmobileLogin-isUserLogdIn-false"
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
                        isOTPVarify = false;
                        progress.visibility = View.GONE
                        imageSuccess?.visibility = View.GONE
                        tv_varify.text = getString(R.string.login_str_17)


                    }
                }
            })
    }
}
