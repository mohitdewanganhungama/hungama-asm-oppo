package com.hungama.music.ui.main.view.activity
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.Credentials
import com.google.android.gms.auth.api.credentials.HintRequest
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.hungama.music.data.model.*
import com.hungama.music.R
import com.hungama.music.utils.customview.bottomsheet.CornerRadiusFrameLayout
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.eventreporter.*
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.ui.main.adapter.CountryListAdapter
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.home.eventreporter.UserAttributeEvent
import com.hungama.music.ui.main.viewmodel.UserSubscriptionViewModel
import com.hungama.music.utils.CommonUtils.hideKeyboard
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.setAppButton1
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.preference.PrefConstant
import com.hungama.music.utils.preference.SharedPrefHelper
import kotlinx.android.synthetic.main.activity_enter_mobile.*
import kotlinx.android.synthetic.main.layout_progress.*
import kotlinx.android.synthetic.main.list_bottom_sheet.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class EnterMobileNumberActivity : AppCompatActivity() {

    // get reference of the firebase auth
    var userViewModel: UserViewModel? = null
    lateinit var auth: FirebaseAuth
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var storedVerificationId:String
    var number : String =""
    var selectedCountry : String ="India"
    var selectedCountryCode : String ="+91"
    var layoutManger: LinearLayoutManager? = null
    var listOfCountry = mutableListOf<CountryModel>()
    var listOfCountrySearch = mutableListOf<CountryModel>()
    var countryDataList = CountryDataModel()
    private lateinit var sheetBehavior: BottomSheetBehavior<CornerRadiusFrameLayout>
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    var action = 0
    var mainLogin = false

    private val CREDENTIAL_PICKER_REQUEST = 1  // Set to an unused request code
    var isHEDataLoaded = false
    var heCountryCode = ""
    var heCountryName = ""
    var heMobileNumber = ""
    var heMsisdnWithIsdCode = ""
    var isCountryDataLoaded = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val hashMap = HashMap<String,String>()
        hashMap.put(EventConstant.ACTION_EPROPERTY,"1")
        setLog("LOGIN","MobileClicked${hashMap}")
        EventManager.getInstance().sendEvent(LoginMobileClickedEvent(hashMap))
        setContentView(R.layout.activity_enter_mobile)
        action = intent.getIntExtra("action", 0)
        if(intent?.hasExtra("mainLogin")!!){
            mainLogin = intent.getBooleanExtra("mainLogin", false)
        }

        setUpViewModel()
        setUpCountryData()

        Utils.setWindowProperty(this@EnterMobileNumberActivity)

        auth = FirebaseAuth.getInstance()
        // Force reCAPTCHA flow
        //auth.firebaseAuthSettings.forceRecaptchaFlowForTesting(true)
        // Callback function for Phone Auth
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            // This method is called when the verification is completed
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                setLog("MobileOtpLogin", "EnterMobileNumerActivity-onVerificationCompleted")
                signInWithPhoneAuthCredential(credential)
                setLog("GFG", "onVerificationCompleted Success")
                val messageModel = MessageModel("onVerificationCompleted Success",MessageType.POSITIVE, true)
                CommonUtils.showToast(this@EnterMobileNumberActivity, messageModel)
            }

            // Called when verification is failed add log statement to see the exception
            override fun onVerificationFailed(e: FirebaseException) {
                setLog("GFG", "onVerificationFailed $e")
                setLog("MobileOtpLogin", "EnterMobileNumerActivity-onVerificationFailed-${e.message}")
                try {
                    if (e is FirebaseAuthInvalidCredentialsException) {
                        // Invalid request
                        val messageModel = MessageModel(getString(R.string.please_enter_a_valid_phone_number),MessageType.NEGATIVE, true)
                        CommonUtils.showToast(this@EnterMobileNumberActivity, messageModel)
                    } else if (e is FirebaseTooManyRequestsException) {
                        // The SMS quota for the project has been exceeded
                        val messageModel = MessageModel("onVerificationFailed "+e.message.toString(),MessageType.NEGATIVE, true)
                        CommonUtils.showToast(this@EnterMobileNumberActivity, messageModel)
                    }
                }catch (e:Exception){
                    setLog("MobileOtpLogin", "EnterMobileNumerActivity-error-${e.message}")
                }

            }

            // On code is sent by the firebase this method is called
            // in here we start a new activity where user can enter the OTP
            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                setLog("MobileOtpLogin", "EnterMobileNumerActivity-onCodeSent")
                setLog("GFG", "onCodeSent: $verificationId")
                storedVerificationId = verificationId
                resendToken = token

                // Start a new activity using intent
                // also send the storedVerificationId using intent
                // we will use this id to send the otp back to firebase
                /*val intent = Intent(applicationContext, MobileOTPVarifyActivity::class.java)
                intent.putExtra("storedVerificationId", storedVerificationId)
                intent.putExtra("mobile", number)
                startActivity(intent)
                finish()*/
                val url = if (intent.hasExtra(Constant.DeepLink_Payment)) intent.getStringExtra(Constant.DeepLink_Payment) else ""
                val intent = Intent(this@EnterMobileNumberActivity, MobileOTPVarifyActivity::class.java)
                intent.putExtra("storedVerificationId", storedVerificationId)
                intent.putExtra("mobile",number)
                intent.putExtra("countryCode",selectedCountryCode)
                intent.putExtra("action", action)
                if (url!!.isNotEmpty()) {
                    intent.putExtra(Constant.DeepLink_Payment, url)
                }
                startActivityForResult(intent, Constant.SIGNIN_WITH_MOBILE)
                overridePendingTransition(R.anim.enter, R.anim.exit)

                CoroutineScope(Dispatchers.IO).launch {
                    val hashMap = HashMap<String, String>()
                    hashMap.put(EventConstant.SOURCE_EPROPERTY, "Mobile")
                    setLog("LOGIN", "emailfilled${hashMap}")
                    EventManager.getInstance().sendEvent(LoginOTPEvent(hashMap))
                }

            }
        }

        // get storedVerificationId from the intent
        storedVerificationId = intent.getStringExtra("storedVerificationId").toString()
        number = intent.getStringExtra("mobile").toString()
        etMobileNumber?.setBackgroundColor(Color.TRANSPARENT)
        //etMobileNumber.requestFocus()

        val privacy = findViewById(R.id.tvPrivacyPolicy)as TextView
        val terms = findViewById(R.id.tvTermsOfServices)as TextView
        privacy.setOnClickListener {
            CommonUtils.openCommonWebView(Constant.privacyPolicy,this)
        }
        terms.setOnClickListener {
            CommonUtils.openCommonWebView(Constant.termAndConditionLink,this)

        }

        setAppButton1(this, btnSendOTP)
        var isClickEnable = true
        // fill otp and call the on click on button
        btnSendOTP.setOnClickListener {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(this, btnSendOTP!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            val number = etMobileNumber.text.trim().toString()
            if (number.isNotEmpty()) {
                setLog("EnterMobileNumber", "btnSendOTPClick - $selectedCountryCode")
                //API Call
                if((selectedCountryCode.equals("+91", true) && number.length < 10) || number.length < 5){
                    val messageModel = MessageModel(getString(R.string.toast_str_2),MessageType.NEGATIVE, true)
                    CommonUtils.showToast(this, messageModel)
                }else{
                    //setProgressBarVisible(true)
                    if (isClickEnable) {
                        isClickEnable = false
                        setLog("setCountryAndHE", "btnSendOTP-click-isCountryDataLoaded-$isCountryDataLoaded-heMsisdnWithIsdCode-${heMsisdnWithIsdCode}-heMobileNumber-$heMobileNumber-number-$number")

                        if (isHEDataLoaded && heMobileNumber.equals(number, true) && !heMsisdnWithIsdCode.isNullOrBlank()){
                            setLog("setCountryAndHE", "btnSendOTP-click-callHELogin caled")
                            callHELogin(heMsisdnWithIsdCode)
                        }else{
                            generateOtp(number)
                        }
                        Handler(Looper.getMainLooper()).postDelayed({
                            isClickEnable = true
                        }, 5000)
                    }
                }
            } else {
                //val messageModel = MessageModel(getString(R.string.login_str_28), MessageType.NEUTRAL, true)
                val messageModel = MessageModel(getString(R.string.toast_str_2),MessageType.NEGATIVE, true)
                CommonUtils.showToast(this, messageModel)
            }
        }



        imageBack.setOnClickListener {
            finish()
        }
        coordinator.bottomSheetLL.setCornerRadius(resources.getDimensionPixelSize(R.dimen.common_popup_round_corner).toFloat())
        sheetBehavior = BottomSheetBehavior.from<CornerRadiusFrameLayout>(coordinator.bottomSheetLL)
        sheetBehavior?.setState(BottomSheetBehavior.STATE_HIDDEN) // will hide bottom sheet
        //initRecyclerView()

        sheetBehavior?.setPeekHeight(0)

        ImageLoader.loadImage(
            this,
            imageFlag,
            "https://storage.googleapis.com/static-media-hungama-com/NewFlags/in.png",
            R.drawable.bg_gradient_placeholder
        )
        imageFlag.setOnClickListener {
            //Open Bottom sheet
            hideKeyboard()
            sheetBehavior?.setState(BottomSheetBehavior.STATE_EXPANDED) // Will show the bottom sheet
            sheetBehavior?.setPeekHeight(resources.getDimensionPixelSize(R.dimen.dimen_400))
        }

        etCountryCode.setOnClickListener {

            //Open Bottom sheet
            hideKeyboard()
            sheetBehavior?.setState(BottomSheetBehavior.STATE_EXPANDED) // Will show the bottom sheet
            sheetBehavior?.setPeekHeight(resources.getDimensionPixelSize(R.dimen.dimen_400))
        }

        coordinator.llBtnClose.setOnClickListener{
            sheetBehavior?.setState(BottomSheetBehavior.STATE_HIDDEN)
        }

        coordinator.bottomSheetLL.setOnTouchListener(View.OnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (sheetBehavior?.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    sheetBehavior?.setState(BottomSheetBehavior.STATE_HIDDEN) // will hide bottom sheet
                    sheetBehavior?.setPeekHeight(0)
                }
                return@OnTouchListener false
            }
            true
        })

        /**
         * bottom sheet state change listener
         * we are changing button text when sheet changed state
         */
        sheetBehavior?.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        backdrop?.hide()
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        backdrop?.show()
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        sheetBehavior?.setState(BottomSheetBehavior.STATE_HIDDEN) // will hide bottom sheet
                        sheetBehavior?.setPeekHeight(0)
                        backdrop?.hide()
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED ->{
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })


    }



    // Construct a request for phone numbers and show the picker
    private fun requestHint() {
        val hintRequest = HintRequest.Builder()
            .setPhoneNumberIdentifierSupported(true)
            .build()
        val credentialsClient = Credentials.getClient(this)
        val intent = credentialsClient.getHintPickerIntent(hintRequest)
        startIntentSenderForResult(
            intent.intentSender,
            CREDENTIAL_PICKER_REQUEST,
            null, 0, 0, 0
        )
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    setLog("SignIn", "signInWithCredential:success")
                    setLog("MobileOtpLogin", "EnterMobileNumerActivity-signInWithPhoneAuthCredential-task.isSuccessful-true")
                    val user = task.result?.user
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                    intent.putExtra("user", user)
                    finish()
                } else {
                    // Sign in failed, display a message and update the UI
                    setLog("signInWithCredential:failure", task.exception?.message.toString())
                    setLog("MobileOtpLogin", "EnterMobileNumerActivity-signInWithPhoneAuthCredential-task.isSuccessful-false")
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI
                }
            }
    }

    override fun onResume() {
        super.onResume()
        hideKeyboard()

        var heApi_url=""
        if(!TextUtils.isEmpty(Constant.HE_API_URL)){
            heApi_url=Constant.HE_API_URL
        }else{
            heApi_url=CommonUtils.getFirebaseConfigAdsData().he_api.url
        }

        setLog("Login","HEAPI_URL:${heApi_url}")
        if(!TextUtils.isEmpty(heApi_url)){
            getHEApiCall(heApi_url)
        }else{
            setLog("Login","HEAPI_URLnot valid url :${heApi_url}}")
        }
    }

    override fun onStop() {
        super.onStop()
        hideKeyboard()
    }

    private fun initRecyclerView() {
        layoutManger = LinearLayoutManager(this)
        coordinator.rvCounrtyList.setLayoutManager(layoutManger)

        val countryListAdapter = CountryListAdapter(this).apply {
            itemClick = { countryTitle ->
                setLog(">>>>>>>>>",countryTitle.country)
                selectedCountry=countryTitle.country
                selectedCountryCode = countryTitle.code
                setLog("TAG", "initRecyclerView: "+countryTitle.country)
                setLog("EnterMobileNumber", "initRecyclerView - $selectedCountryCode")
                CoroutineScope(Dispatchers.IO).launch {
                    val hashMap = HashMap<String,String>()
                    hashMap.put(EventConstant.ACTION_EPROPERTY,""+selectedCountryCode)
                    setLog("LOGIN","MobileCountrycode${hashMap}")
                    EventManager.getInstance().sendEvent(LoginMobileNumberCountryCodeEvent(hashMap))
                }


                etCountryCode.setText("("+selectedCountryCode+")")
                if (!TextUtils.isEmpty(countryTitle.imageURL)){
                    ImageLoader.loadImage(
                        context,
                        imageFlag,
                        countryTitle.imageURL,
                        R.drawable.bg_gradient_placeholder
                    )
                }

                listOfCountry.forEach{it ->
                    setLog("TAG", "initRecyclerView: "+it.country)

                    setLog("TAG", "initRecyclerView: it.country without"+it.country)
                    setLog("TAG", "initRecyclerView: it.code withous"+it.code)
                    setLog("TAG", "initRecyclerView: selectedCountry with"+selectedCountry)
                    setLog("TAG", "initRecyclerView: selectedCountryCode"+selectedCountryCode)
                    if(it.country.equals(selectedCountry)){
                        setLog("TAG", "initRecyclerView: selectedCountry"+selectedCountry)
                        setLog("TAG", "initRecyclerView: it.country"+it.country)
                        it.isSelected=true
                    }else{
                        it.isSelected=false
                    }
                    Handler(Looper.getMainLooper()).post {
                        notifyDataSetChanged()
                    }
                }
                hideKeyboard()
                sheetBehavior?.setState(BottomSheetBehavior.STATE_HIDDEN)
                coordinator.etSearch.setText("")
            }
        }
        coordinator.rvCounrtyList.adapter = countryListAdapter
        countryListAdapter.setCountryList(setCountryCodeListData())

        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(s.toString().length > 0){
                    listOfCountrySearch = mutableListOf<CountryModel>()
                    setLog("listOfCountrySearch", "text-${s}-listOfCountrySearch.size-${listOfCountrySearch.size}")
                    listOfCountry?.forEachIndexed { index, it ->
                        if (it.dialCode.equals("+92")){
                            setLog("listOfCountrySearch","pk-true-it-$it-index-$index")
                        }
                        if(it.country.contains(s.toString(), true)){
                            setLog("listOfCountrySearch", "IF-text-${s}-it-$it-index-$index-listOfCountrySearch.size-${listOfCountrySearch.size}-listOfCountry.size-${listOfCountry.size}")
                            listOfCountrySearch.addAll(listOf(it))
                        }
                        else if(it.code.equals("+"+s.toString(),true)){
                            setLog("listOfCountrySearch", "ElseIf-text-${s}-listOfCountrySearch.size-${listOfCountrySearch.size}-listOfCountry.size-${listOfCountry.size}")
                            listOfCountrySearch.addAll(listOf(it))
                        }
                    }
                    /*listOfCountry.forEach{it ->
                        if (it.code.equals("pk")){
                            setLog("listOfCountrySearch","pk-true-it-$it")
                        }
                        if(it.country.contains(s.toString(), true)){
                            setLog("listOfCountrySearch", "IF-text-${s}-it-$it-listOfCountrySearch.size-${listOfCountrySearch.size}-listOfCountry.size-${listOfCountry.size}")
                            listOfCountrySearch.addAll(listOf(it))
                        }
                        else if(it.code.equals("+"+s.toString(),true)){
                            setLog("listOfCountrySearch", "ElseIf-text-${s}-listOfCountrySearch.size-${listOfCountrySearch.size}-listOfCountry.size-${listOfCountry.size}")
                            listOfCountrySearch.addAll(listOf(it))
                        }
                    }*/
                    countryListAdapter.setCountryList(listOfCountrySearch)
                }else{
                    listOfCountry.clear()
                    listOfCountrySearch.clear()
                    countryListAdapter.setCountryList(setCountryCodeListData())
                }
                /*Handler(Looper.getMainLooper()).post {
                    countryListAdapter.notifyDataSetChanged()
                }*/

            }
        }
        coordinator.etSearch.addTextChangedListener(textWatcher)

    }

    private fun setCountryCodeListData(): List<CountryModel> {
        listOfCountry = mutableListOf<CountryModel>()
        for(country in countryDataList){
            setLog("TAG", "generateDummyData: "+country.name)
            setLog("TAG", "generateDummyData: "+country.code)
            if (country.name.equals(selectedCountry, false)){
                val countryModel = CountryModel(country.name!!, country.dialCode!!,country.flag!!,true)
                listOfCountry.add(countryModel)
            }else{
                val countryModel = CountryModel(country.name!!, country.dialCode!!,country.flag!!,false)
                listOfCountry.add(countryModel)
            }
        }
        setLog("listOfCountrySearch","generateDummyData-listOfCountry.size-${listOfCountry.size}")
        return listOfCountry
    }

    private fun isValidMobile(phone: String, isIndianNumber:Boolean): Boolean {
        return Pattern.matches("[1-9][0-9]{9}", phone)
    }
    private fun login() {
        number = etMobileNumber.text.trim().toString()

        setLog("EnterMobileNumber", "login - $selectedCountryCode")
        // get the phone number from edit text and append the country cde with it
        if (number.isNotEmpty()) {
            if ((selectedCountryCode.equals("+91", true) && isValidMobile(number, true)) || number.length > 4) {

                //number = "+91$number"
                number = number
                sendVerificationCode(number, selectedCountryCode.trim())
                //makeSendOtpCall(number, selectedCountryCode.trim())

            }else{
                //Utils.showSnakbar(parentMobile, false, "Enter valid mobile number")
                val messageModel = MessageModel(getString(R.string.toast_str_2),MessageType.NEGATIVE, true)
                CommonUtils.showToast(this, messageModel)
            }

        }else{
            val messageModel = MessageModel(getString(R.string.toast_str_33), getString(R.string.toast_str_2),MessageType.NEGATIVE, true)
            CommonUtils.showToast(this, messageModel)
            //Utils.showSnakbar(parentMobile, false, "Enter mobile number")
        }
    }

    private fun sendVerificationCode(number: String, countryCode: String) {
        if (ConnectionUtil(this).isOnline) {
            CoroutineScope(Dispatchers.IO).launch {
                val hashMap = HashMap<String, String>()
                hashMap.put(EventConstant.ACTION_EPROPERTY, ""+number)
                setLog("LOGIN", "Mobilefilled${hashMap}")
                EventManager.getInstance().sendEvent(LoginMobileNumberFilledEvent(hashMap))
            }


            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(countryCode+number) // Phone number to verify
                .setTimeout(0, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this) // Activity (for callback binding)
                .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
            setLog("GFG", "Auth started")
        }else {
            val messageModel = MessageModel(getString(R.string.toast_message_5), getString(R.string.toast_message_5),MessageType.NEGATIVE, true)
            CommonUtils.showToast(this, messageModel)
        }
    }
    private fun generateOtp(number: String) {
        try {

            login()
//            userViewModel?.generateOtp(
//                this@EnterMobileNumberActivity,
//                number)
        } catch (e: Exception) {
            e.printStackTrace()
            val messageModel = MessageModel(getString(R.string.discover_str_2), MessageType.NEGATIVE, true)
            CommonUtils.showToast(this, messageModel)
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
//            showLoader()
            pb_progress?.visibility = View.VISIBLE
        } else {
//            dismissLoader()
            pb_progress?.visibility = View.GONE
        }
    }



    private fun makeSendOtpCall(number: String, countryCode: String) {
        userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)

        if (ConnectionUtil(this).isOnline) {
            CoroutineScope(Dispatchers.IO).launch {
                val hashMap = HashMap<String, String>()
                hashMap.put(EventConstant.ACTION_EPROPERTY, ""+number)
                setLog("LOGIN", "Mobilefilled${hashMap}")
                EventManager.getInstance().sendEvent(LoginMobileNumberFilledEvent(hashMap))
            }


            userViewModel?.generateOTPNumber(this, number, countryCode)?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                setLog("TAG", "generateOTPCallRespObserver:: $it?.data")
                                val intent:Intent
                                intent = Intent(this@EnterMobileNumberActivity, MobileOTPVarifyActivity::class.java)
                                intent.putExtra("mobile",number)
                                intent.putExtra("countryCode",countryCode)
                                intent.putExtra("action", action)
                                startActivityForResult(intent, Constant.SIGNIN_WITH_MOBILE)
                                overridePendingTransition(R.anim.enter, R.anim.exit)

                                CoroutineScope(Dispatchers.IO).launch {
                                    val hashMap = HashMap<String, String>()
                                    hashMap.put(EventConstant.SOURCE_EPROPERTY, "Mobile")
                                    setLog("LOGIN", "emailfilled${hashMap}")
                                    EventManager.getInstance().sendEvent(LoginOTPEvent(hashMap))
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
        } else {
            val messageModel = MessageModel(getString(R.string.toast_message_5), getString(R.string.toast_message_5),MessageType.NEGATIVE, true)
            CommonUtils.showToast(this, messageModel)
        }
        setProgressBarVisible(true)
    }


    private fun setUpCountryData() {
        userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)

        if (ConnectionUtil(this).isOnline) {
            userViewModel?.getCountryData(this)?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null) {

                                setLog("TAG", "generateOTPCallRespObserver:: $it?.data")
                                countryDataList = it?.data
                                setLog("listOfCountrySearch","setUpCountryData-countryDataList.size-${countryDataList.size}")
                                setCountryCodeListData()
                                isCountryDataLoaded = true
                                setLog("setCountryAndHE", "setUpCountryData-isCountryDataLoaded-$isCountryDataLoaded-isHEDataLoaded-$isHEDataLoaded")


                                for(country in countryDataList){
                                    if(isHEDataLoaded){
                                        if (country.code?.trim().equals(heCountryName?.trim(), true)){
                                            setLog("setCountryAndHE", "countryDataList item:${country}")
                                            selectedCountry=country.name!!
                                            selectedCountryCode = country.dialCode!!
                                            etCountryCode.setText("(${selectedCountryCode})")
                                            if (!TextUtils.isEmpty(country.flag)){
                                                ImageLoader.loadImage(
                                                    this,
                                                    imageFlag,
                                                    country.flag!!,
                                                    R.drawable.bg_gradient_placeholder
                                                )
                                            }
                                            etMobileNumber?.setText(heMobileNumber)
                                            break
                                        }
                                    }else{
                                        if (country.code.equals(Constant.DEFAULT_COUNTRY_CODE, true)){
                                            selectedCountry=country.name!!
                                            selectedCountryCode = country.dialCode!!
                                            etCountryCode.setText("(${selectedCountryCode})")
                                            if (!TextUtils.isEmpty(country.flag)){
                                                ImageLoader.loadImage(
                                                    this,
                                                    imageFlag,
                                                    country.flag!!,
                                                    R.drawable.bg_gradient_placeholder
                                                )
                                            }
                                            break
                                        }
                                    }


                                }

//                                setCountryAndHE()
                                initRecyclerView()
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
            val messageModel = MessageModel(getString(R.string.toast_message_5), getString(R.string.toast_message_5),MessageType.NEGATIVE, true)
            CommonUtils.showToast(this, messageModel)
        }
        setProgressBarVisible(true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constant.SIGNIN_WITH_GOOGLE || requestCode == Constant.SIGNIN_WITH_FACEBOOK
            || requestCode == Constant.SIGNIN_WITH_MOBILE || requestCode == Constant.SIGNIN_WITH_EMAIL
            || requestCode == Constant.SIGNIN_WITH_APPLE || requestCode == Constant.SIGNIN_WITH_ALL
        ){
            setLog("TAG", "onActivityResult: requestCode ${requestCode}")
            if (resultCode == Activity.RESULT_OK){

            }else if (resultCode == Activity.RESULT_CANCELED){

            }else{
                action = requestCode

                redirectToHome()
            }
        }else if(requestCode==CREDENTIAL_PICKER_REQUEST){
            if (resultCode == Activity.RESULT_OK){
                val credential = data?.getParcelableExtra<Credential>(Credential.EXTRA_KEY)
                setLog("TAG", "onActivityResult:${credential}")
                etMobileNumber.setText(""+credential?.id)
            }
        }
    }

    fun redirectToHome() {
        setLog("TAG", "redirectToHome action:${action} mainLogin${mainLogin}")
        if (action == Constant.SIGNIN_WITH_GOOGLE
            || action == Constant.SIGNIN_WITH_FACEBOOK
            || action == Constant.SIGNIN_WITH_MOBILE
            || action == Constant.SIGNIN_WITH_HE
            || action == Constant.SIGNIN_WITH_ALL){
                if(action==Constant.SIGNIN_WITH_HE && mainLogin){
                    setLog("TAG", "redirectToHome action:${action} MainActivity called")

                    val intent = Intent(this@EnterMobileNumberActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    overridePendingTransition(R.anim.enter, R.anim.exit)
                }else{
                    setLog("TAG", "redirectToHome action:${action} other returnIntent called")
                    overridePendingTransition(R.anim.enter, R.anim.exit)
                    val returnIntent = Intent()
                    returnIntent.putExtra("result", action)
                    setResult(action, returnIntent)
                    finish()
                }

        }else{
            finish()
        }

        val intent = Intent(Constant.SONG_DURATION_BROADCAST)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }


    fun getHEApiCall(heApi_url: String) {
        if(userViewModel==null){
            userViewModel = ViewModelProvider(
                this
            ).get(UserViewModel::class.java)
        }
        if (ConnectionUtil(this).isOnline) {
            userViewModel?.getHEApiCall(this,heApi_url)?.observe(this,
                Observer {
                    when (it.status) {
                        Status.SUCCESS -> {
                            setProgressBarVisible(false)
                            setLog(TAG, "getHEApiCall resp:${it?.data?.data}")
                            loadHEdata(it?.data?.data)

                        }

                        Status.LOADING -> {
                            setProgressBarVisible(true)
                        }

                        Status.ERROR -> {
                            setProgressBarVisible(false)
                        }
                    }
                })
        }
    }

    private fun loadHEdata(data: HERespModel.Data?) {
        val userDataMap = java.util.HashMap<String, String>()
        if(!data?.msisdn.isNullOrEmpty() &&!data?.msisdn?.equals("9999999999",true)!!){


            isHEDataLoaded = true
            heCountryCode = data.isdCode
            heCountryName = data.country
            heMobileNumber = data.msisdn
            heMsisdnWithIsdCode = data.msisdnWithIsdCode
            tv_send_otp?.text = getString(R.string.download_str_9)
            tv_select_language?.text = getString(R.string.login_via_mobile_number)
            txtTitle?.text = getString(R.string.login_via_mobile_number_subtext)
            setLog("setCountryAndHE", "loadHEdata-isCountryDataLoaded-$isCountryDataLoaded-isHEDataLoaded-$isHEDataLoaded")
            setCountryAndHE()
            userDataMap.put(EventConstant.HE_AVAILABLE, ""+true)
        }else{
            userDataMap.put(EventConstant.HE_AVAILABLE, ""+false)
        }
        EventManager.getInstance().sendUserAttribute(UserAttributeEvent(userDataMap))
    }

    private fun callHELogin(heMsisdnWithIsdCode:String) {
        if(action==0){
            action=Constant.SIGNIN_WITH_HE
        }

        if (ConnectionUtil(this@EnterMobileNumberActivity).isOnline) {
            showLoader()
            try {
                val jsonObject: JSONObject = JSONObject()

                jsonObject.put("silent_user_id",
                    SharedPrefHelper.getInstance().get(PrefConstant.SILENT_USER_ID,""))


                val mainJson = JSONObject()
                val clientDataJson = JSONObject()
                val username = JSONObject()
                username.put(
                    "value",
                    "+"+heMsisdnWithIsdCode
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
                    this@EnterMobileNumberActivity,
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
                                        CoroutineScope(Dispatchers.IO).launch {
                                            val hashMap1 =
                                                java.util.HashMap<String, String>()
                                            hashMap1.put(EventConstant.METHOD_EPROPERTY,"HE")
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
                                    hashMap.put(EventConstant.SOURCE_EPROPERTY,"HE")
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

    fun getUserSubscriptionStatus() {
        val userSubscriptionViewModel = ViewModelProvider(
            this
        ).get(UserSubscriptionViewModel::class.java)
        if (ConnectionUtil(this).isOnline) {
            userSubscriptionViewModel?.getUserSubscriptionStatusDetail(this)?.observe(this,
                Observer {
                    when (it.status) {
                        Status.SUCCESS -> {
                            setLog("getUserSubscriptionStatus", "getUserSubscriptionStatus-socialLogin-isUserLogdIn-true")
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

    fun showLoader() {
        if (!this.isFinishing) {
            DisplayDialog.getInstance(this).showProgressDialog(this@EnterMobileNumberActivity, false)
        }
    }

    fun dismissLoader() {
        if (!this.isFinishing) {
            DisplayDialog.getInstance(this).dismissProgressDialog()
        }
    }

    private fun setCountryAndHE(){
        setLog("setCountryAndHE", "setCountryAndHE-loadHEdata-isCountryDataLoaded-${isCountryDataLoaded}-isHEDataLoaded-${isHEDataLoaded} heCountryName:${heCountryName} heCountryCode${heCountryCode}")

        if (countryDataList?.size!!>0){
            for(country in countryDataList){
                if (isHEDataLoaded&&country.code.equals(heCountryName, true)){
                    setLog("setCountryAndHE", "countryDataList item:${country}")
                    selectedCountry=country.name!!
                    selectedCountryCode = country.dialCode!!
                    etCountryCode.setText("(${selectedCountryCode})")
                    if (!TextUtils.isEmpty(country.flag)){
                        ImageLoader.loadImage(
                            this,
                            imageFlag,
                            country.flag!!,
                            R.drawable.bg_gradient_placeholder
                        )
                    }
                    etMobileNumber?.setText(heMobileNumber)
                    break
                }
            }
        }else if (isHEDataLoaded){
            selectedCountry=heCountryName
            selectedCountryCode = heCountryCode
            etCountryCode.setText("(+${selectedCountryCode})")
            etMobileNumber?.setText(heMobileNumber)
        }
    }
}
