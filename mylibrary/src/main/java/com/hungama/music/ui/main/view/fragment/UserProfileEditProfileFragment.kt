package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import com.hungama.music.data.model.MessageModel
import com.hungama.music.data.model.MessageType
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.utils.customview.customspinnerview.SpinnerObserver
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.data.model.UserProfileModel
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.applyButtonTheme
import com.hungama.music.utils.CommonUtils.hideKeyboard
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.CommonUtils.showKeyboard
import com.ozcanalasalvar.library.utils.DateUtils
import com.ozcanalasalvar.library.view.datePicker.DatePicker
import com.ozcanalasalvar.library.view.popup.DatePickerPopup
import kotlinx.android.synthetic.main.common_header_action_bar.*
import kotlinx.android.synthetic.main.fragment_user_profile_edit_profile.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.*


class UserProfileEditProfileFragment : BaseFragment(), SpinnerObserver,
    TextView.OnEditorActionListener, BaseActivity.OnLocalBroadcastEventCallBack {
    var popupWindow = PopupWindow()
    var isSpGenderShowing = false
    var userViewModel: UserViewModel? = null
    private var datePickerPopup: DatePickerPopup? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideBottomNavigationAndMiniplayer()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_profile_edit_profile, container, false)
    }


    override fun initializeComponent(view: View) {
        applyButtonTheme(requireContext(), llSaveButton)
        getUserProfile()
        ivBack?.setOnClickListener { v -> backPress() }
        tvActionBarHeading?.text = getString(R.string.profile_str_20)
        //tvActionBarHeading?.text = "dfkjdajsfkljsajkfhdskjruiehfjkdshkf"
        tvActionBarHeading?.setPadding(70,0,0,0)


        val gender = resources.getStringArray(R.array.Gender)
        if (spGender != null) {

            llDob.visibility = View.GONE
            spGender.setOnSpinnerItemSelectedListener<String> { _, _, index, text ->
                //Toast.makeText(requireContext(), index.toString() + "-" + text, Toast.LENGTH_SHORT).show()
                blurViewGender.setTopLeftRadius(resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat())
                blurViewGender.setTopRightRadius(resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat())
                blurViewGender.setBottomLeftRadius(resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat())
                blurViewGender.setBottomRightRadius(resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat())
            }
            spGender.attachSpinnerObserver(this)


            spGender.setIsFocusable(true)
            //spGender.selectItemByIndex(0)
            text_date.setOnClickListener(this)
            ivDob.setOnClickListener(this)
            llSaveButton?.setOnClickListener(this)
            etName?.setOnEditorActionListener(this)
            ivEditUsername.setOnClickListener(this)
            ivUsernameEdit.setOnClickListener(this)

        }

        /*val calendar = Calendar.getInstance()
        calendar.timeInMillis = DateUtils.getCurrentTime()
        val day = calendar[Calendar.DAY_OF_MONTH]
        val month = calendar[Calendar.MONTH]
        val year = calendar[Calendar.YEAR]
        text_date.text = ""+year+"-" + (month + 1) + "-" + day*/
        val typeface = ResourcesCompat.getFont(
            requireContext(),
            R.font.sf_pro_text_semibold
        )
        datepicker.offset = 2
        datepicker.setTextFont(typeface)
        datepicker.maxDate = DateUtils.getCurrentTime()
        datepicker.date = DateUtils.getCurrentTime()
        datepicker.minDate = DateUtils.getTimeMiles(1948, 8,15)
        datepicker.setPickerMode(DatePicker.MONTH_ON_FIRST)
        datepicker.setDataSelectListener { date, day, month, year ->
            /*Toast.makeText(
                requireContext(),
                "" + day + "/" + (month + 1) + "/" + year,
                Toast.LENGTH_SHORT
            ).show()*/
        if(text_date!=null && year !=null && month !=null &&day !=null)
            text_date.text = ""+year+"-" + (month + 1) + "-" + day
        }

        CommonUtils.setPageBottomSpacing(scrollView, requireContext(),
            resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
            resources.getDimensionPixelSize(R.dimen.dimen_0), 0)

        CommonUtils.PageViewEvent("","","","",
            "viewprofile","user profile_edit profile","")
    }

    override fun onClick(v: View) {
        super.onClick(v)
        if (v==ivEditUsername){
            etName.isEnabled = true
            etName.requestFocus()
            etName.setSelection(etName.text.toString().length)
            requireContext()?.showKeyboard(etName)
        }else if(v==text_date || v==ivDob){
            llDob.visibility = View.VISIBLE
        }else if(v==llSaveButton){
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), llSaveButton!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            llDob.visibility = View.GONE
           if(userViewModel!=null){
               callEditProfile()
           }
        }else if (v == ivUsernameEdit){
            edtUsername?.isEnabled = true
            edtUsername.requestFocus()
            edtUsername.setSelection(edtUsername.text.toString().length)
            requireContext()?.showKeyboard(edtUsername)
        }
    }

    private fun callEditProfile() {
        hideKeyboard()
        etName?.clearFocus()
        try {
            val mainJson = JSONObject()


            mainJson.put("handleName", edtUsername?.text?.trim()?.toString())
            if(spGender?.text?.equals("Male")!!){
                mainJson.put("gender", "M")
            }else if(spGender?.text?.equals("Female")!!){
                mainJson.put("gender", "F")
            }else if(spGender?.text?.equals("Prefer not to say")!!){
                mainJson.put("gender", "U")
            }else{
                val messageModel = MessageModel(getString(R.string.please_select_gender),
                    MessageType.NEUTRAL, true)
                CommonUtils.showToast(requireContext(), messageModel)
                return
            }

            if (!TextUtils.isEmpty(text_date?.text)){
                mainJson.put("dob", text_date?.text)
            }else{
                val messageModel = MessageModel(getString(R.string.please_select_date_of_birth),
                    MessageType.NEUTRAL, true)
                CommonUtils.showToast(requireContext(), messageModel)
                return
            }


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
            )?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it != null) {
                                if (!TextUtils.isEmpty(it?.data?.message)){
                                    val messageModel = MessageModel(it?.data?.message.toString(), MessageType.NEUTRAL, true)
                                    CommonUtils.showToast(requireContext(), messageModel)
                                }
                                val intent = Intent(Constant.EDIT_PROFILE_EVENT)
                                intent.putExtra("EVENT", Constant.EDIT_PROFILE_RESULT_CODE)
                                LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
                                backPress()
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

        } catch (e: Exception) {
            e.printStackTrace()
            Utils.showSnakbar(requireContext(),
                requireView(),
                false,
                getString(R.string.discover_str_2)
            )
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onDestroy() {
        showBottomNavigationAndMiniplayer()
        hideKeyboard()
        super.onDestroy()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden){
            showBottomNavigationAndMiniplayer()
        }else{
            hideBottomNavigationAndMiniplayer()
        }
    }

    private fun getContextColor(@ColorRes resource: Int): Int {
        return ContextCompat.getColor(requireActivity(), resource)
    }

    override fun onSpinnerObserver(isShown: Boolean) {
        setLog("datePickerCustom", "isShown-$isShown")
        if (isShown){
            blurViewGender.setTopLeftRadius(resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat())
            blurViewGender.setTopRightRadius(resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat())
            blurViewGender.setBottomLeftRadius(resources.getDimensionPixelSize(R.dimen.dimen_0).toFloat())
            blurViewGender.setBottomRightRadius(resources.getDimensionPixelSize(R.dimen.dimen_0).toFloat())
            spGender.spinnerPopupBackgroundColor = ContextCompat.getColor(requireContext(), R.color.home_bg_color)
        }else{
            blurViewGender.setTopLeftRadius(resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat())
            blurViewGender.setTopRightRadius(resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat())
            blurViewGender.setBottomLeftRadius(resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat())
            blurViewGender.setBottomRightRadius(resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat())
            spGender.spinnerPopupBackgroundColor = ContextCompat.getColor(requireContext(), R.color.blur_one_half_opacity_white_color)
        }
    }


    private fun getUserProfile(){
        userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)

        if (ConnectionUtil(requireContext()).isOnline) {

            userViewModel?.getUserProfileData(
                requireContext(), SharedPrefHelper.getInstance().getUserId()!!
            )?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                fillUserDetail(it?.data)
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



    private fun fillUserDetail(it: UserProfileModel?) {
        if (it != null) {
            CommonUtils.saveUserProfileDetails(it)
            if (it.statusCode == 200 && it.result != null && it?.result?.size!! > 0){
                if (!TextUtils.isEmpty(it?.result?.get(0)?.handleName)){
                    edtUsername.setText(it?.result?.get(0)?.handleName)
                }else{
                    edtUsername.setText("")
                }

                if (it?.result?.get(0)?.profileImage!! != null && !TextUtils.isEmpty(it?.result?.get(0)?.profileImage!!.toString())){
                    ImageLoader.loadImage(requireContext(),ivUserImg,it?.result?.get(0)?.profileImage!!.toString(),R.drawable.ic_no_user_img)
                }else if (it?.result?.get(0)?.alternateProfileImage!! != null && !TextUtils.isEmpty(it?.result?.get(0)?.alternateProfileImage!!.toString())){
                    ImageLoader.loadImage(requireContext(),ivUserImg,it?.result?.get(0)?.alternateProfileImage!!.toString(),R.drawable.ic_no_user_img)
                }
                var username = ""
                if (!TextUtils.isEmpty(it?.result?.get(0)?.firstName)){
                    username += it?.result?.get(0)?.firstName + " "
                }
                if (!TextUtils.isEmpty(it?.result?.get(0)?.lastName)){
                    username += it?.result?.get(0)?.lastName?.trim()
                }

                if (!TextUtils.isEmpty(username)){
                    etName?.setText(username)
                }else{
                    if (!TextUtils.isEmpty(it?.result?.get(0)?.handleName)){
                        etName?.setText(it?.result?.get(0)?.handleName)
                    }else{
                        etName?.setText("")
                    }

                }

                if (!TextUtils.isEmpty(it?.result?.get(0)?.email)){
                    edtUserEmail.setText(it?.result?.get(0)?.email)
                }else{
                    edtUserEmail.setText("")
                }
                if (!TextUtils.isEmpty(it?.result?.get(0)?.phone)){
                    edtUserMobile.setText(it?.result?.get(0)?.phone)
                }else{
                    edtUserMobile.setText("")
                }

                if (!TextUtils.isEmpty(it?.result?.get(0)?.gender)){
                    if(it?.result?.get(0)?.gender?.equals("M",true)!!){
                        spGender.text="Male"
                    }else if(it?.result?.get(0)?.gender?.equals("F",true)!!){
                        spGender.text="Female"
                    }else if(it?.result?.get(0)?.gender?.equals("U",true)!!){
                        spGender.text="Prefer not to say"
                    }else{
                        spGender.text="Select gender"
                    }
                }else{
                    spGender.text="Select gender"
                }

                if (!TextUtils.isEmpty(it.result?.get(0)?.dob)){
                    text_date.text =  com.hungama.music.utils.DateUtils.convertDate(
                        if (it.result?.get(0)?.dob!!.contains("T"))
                        com.hungama.music.utils.DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_WITH_T
                        else
                            com.hungama.music.utils.DateUtils.DATE_FORMAT_YYYY_MM_DD,
                        com.hungama.music.utils.DateUtils.DATE_FORMAT_YYYY_MM_DD,it?.result?.get(0)?.dob)!!
                }else{
                    text_date.text = ""
                }

                //etName.requestFocus()
                etName.setSelection(etName.text.toString().length)
                //Utils.showSoftKeyBoard(requireActivity(),etName)
            }
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