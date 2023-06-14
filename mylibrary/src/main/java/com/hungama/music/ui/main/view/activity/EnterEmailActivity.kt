package com.hungama.music.ui.main.view.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Patterns
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import com.hungama.music.R
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.eventreporter.LoginEmailClickedEvent
import com.hungama.music.eventanalytic.eventreporter.LoginEmailFilledEvent
import com.hungama.music.eventanalytic.eventreporter.LoginOTPEvent
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.data.model.MessageModel
import com.hungama.music.data.model.MessageType
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.hideKeyboard
import com.hungama.music.utils.CommonUtils.setLog
import kotlinx.android.synthetic.main.activity_choose_language.*
import kotlinx.android.synthetic.main.activity_enter_email.*
import kotlinx.android.synthetic.main.activity_enter_email.imageBack
import kotlinx.android.synthetic.main.activity_enter_email.progress
import kotlinx.android.synthetic.main.activity_otp_varify.*
import kotlinx.android.synthetic.main.list_bottom_sheet.view.*
import java.util.*
import kotlin.collections.HashMap

class EnterEmailActivity : AppCompatActivity() {

    // get reference of the firebase auth
    var userViewModel: UserViewModel? = null
    lateinit var auth: FirebaseAuth
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var storedVerificationId: String
    var email: String = ""
    var layoutManger: LinearLayoutManager? = null
//    private val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    private val emailPattern = "^(?=.{1,64}@)[A-Za-z0-9\\+_-]+(\\.[A-Za-z0-9\\+_-]+)*@[^-][A-Za-z0-9\\+-]+(\\.[A-Za-z0-9\\+-]+)*(\\.[A-Za-z]{2,})$"
    var action = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_email)

        tvPrivacyPolicy.setOnClickListener {
            CommonUtils.openCommonWebView(Constant.privacyPolicy,this)
        }
        tvTermsOfServices.setOnClickListener {
            CommonUtils.openCommonWebView(Constant.termAndConditionLink,this)
        }


        action = intent.getIntExtra("action", 0)
        setUpViewModel()
        val hashMap = HashMap<String,String>()
        hashMap.put(EventConstant.ACTION_EPROPERTY,"1")
        setLog("LOGIN","EmailClicked${hashMap}")
        EventManager.getInstance().sendEvent(LoginEmailClickedEvent(hashMap))

        Utils.setWindowProperty(this@EnterEmailActivity)

        auth = FirebaseAuth.getInstance()

        // get storedVerificationId from the intent
        storedVerificationId = intent.getStringExtra("storedVerificationId").toString()
        //email = intent.getStringExtra("mobile").toString()
        //etMobileNumber.requestFocus()

        CommonUtils.setAppButton1(this, btnSendOTP)
        var isClickEnable = true
        // fill otp and call the on click on button
        btnSendOTP.setOnClickListener {
            try {
                if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(this, btnSendOTP!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            email = etEmail.text.trim().toString()
            if (email.isNotEmpty()) {


                setLog(TAG, "EMAIL_ADDRESS: ${Patterns.EMAIL_ADDRESS.matcher(email).matches()} --> ${email.matches(emailPattern.toRegex())}")


                //API Call
                if (email.matches(emailPattern.toRegex()) ) {
                    //setProgressBarVisible(true)
                    //generateOtp(number)

                    if (isClickEnable) {
                        isClickEnable = false
                        makeSendOtpCall(email)
                        Handler(Looper.getMainLooper()).postDelayed({
                            isClickEnable = true
                        }, 5000)
                    }
                } else {
                    val messageModel = MessageModel(getString(R.string.toast_str_1),MessageType.NEGATIVE, true)
                    CommonUtils.showToast(this, messageModel)
                }
            } else {
                val messageModel = MessageModel( getString(R.string.toast_str_1),MessageType.NEGATIVE, true)
                CommonUtils.showToast(this, messageModel)
            }
        }
        imageBack.setOnClickListener {
            finish()
        }
        setButtonStatus(0)
    }


    private fun setUpViewModel() {
        userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)

        setProgressBarVisible(false)
    }

    override fun onResume() {
        super.onResume()
        hideKeyboard()
    }

    override fun onDestroy() {
        super.onDestroy()
        hideKeyboard()
    }
    private fun setProgressBarVisible(it: Boolean) {
        if (it) {
            progressBar2?.visibility = View.VISIBLE
        } else {
            progressBar2?.visibility = View.GONE
        }
    }





    private fun makeSendOtpCall(email: String) {
        userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)

        if (ConnectionUtil(this).isOnline) {
            setButtonStatus(0)
            val hashMap = HashMap<String, String>()
            hashMap.put(EventConstant.ACTION_EPROPERTY, "1")
            setLog("LOGIN", "emailfilled${hashMap}")
            EventManager.getInstance().sendEvent(LoginEmailFilledEvent(hashMap))
            userViewModel?.generateOTPEmail(this, email)?.observe(this,
                {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                setLog("TAG", "generateOTPCallRespObserver:: $it?.data")
                                val url = if (intent.hasExtra(Constant.DeepLink_Payment)) intent.getStringExtra(Constant.DeepLink_Payment) else ""

                                val otpintent = Intent(this@EnterEmailActivity, EmailOTPVarifyActivity::class.java)
                                otpintent.putExtra("email", email)
                                otpintent.putExtra("action", action)
                                if (url!!.isNotEmpty())
                                    otpintent.putExtra(Constant.DeepLink_Payment, url)
                                startActivityForResult(otpintent, Constant.SIGNIN_WITH_EMAIL)
                                overridePendingTransition(R.anim.enter, R.anim.exit)

                                val hashMap = HashMap<String, String>()
                                hashMap.put(EventConstant.SOURCE_EPROPERTY, "Email")
                                setLog("LOGIN", "emailfilled${hashMap}")
                                EventManager.getInstance().sendEvent(LoginOTPEvent(hashMap))
                            }
                            setButtonStatus(0)
                        }

                        Status.LOADING ->{
                            setProgressBarVisible(true)
                        }

                        Status.ERROR ->{
                            setProgressBarVisible(false)
                            setButtonStatus(0)
                        }
                    }
                })
        } else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),MessageType.NEGATIVE, true)
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
            if (resultCode == Activity.RESULT_OK){

            }else if (resultCode == Activity.RESULT_CANCELED){

            }else{
                action = requestCode
                redirectToHome()
            }
        }
    }

    fun redirectToHome() {
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
            finish()
        }

        val intent = Intent(Constant.SONG_DURATION_BROADCAST)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun setButtonStatus(status:Int){
        when(status){
            0->{
                progress.hide()
               // ivDetailBtnIcon.show()
                tv_send_otp.show()
            }
            1->{
                progress.show()
                //ivDetailBtnIcon.hide()
                tv_send_otp.hide()
            }
        }
    }
}
