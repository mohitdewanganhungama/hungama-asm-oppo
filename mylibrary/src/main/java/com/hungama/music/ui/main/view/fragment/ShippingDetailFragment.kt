package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.hungama.music.R
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.data.model.ProductRespModel
import com.hungama.music.data.model.ShippingDetailModel
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant
import com.hungama.music.utils.Constant.shippingDetailsKey
import com.hungama.music.utils.customview.customspinnerview.SpinnerObserver
import com.hungama.music.utils.preference.SharedPrefHelper
import kotlinx.android.synthetic.main.common_header_action_bar.*
import kotlinx.android.synthetic.main.fragment_shipping_detail.*


class ShippingDetailFragment(val product: ProductRespModel.Product) : BaseFragment(),
    SpinnerObserver, BaseActivity.OnLocalBroadcastEventCallBack {
    var minChar: Int = 6
    var maxChar: Int = 20
    var MobilePattern: Int = 10
    var PinCode: Int = 6
    var maxWord: Int = 200
    val TAGs = this.javaClass.simpleName
    var shippingDetailModel = ShippingDetailModel()
    override fun initializeComponent(view: View) {
        CommonUtils.setAppButton3(requireContext(), btnsSave)
        //CommonUtils.applyButtonTheme(requireContext(), btnsSave)
        ivBack?.setOnClickListener { v -> backPress() }
        tvActionBarHeading?.text = getString(R.string.reward_str_15)
        shippingDetailModel = SharedPrefHelper.getInstance().getUserLastShippingDetails()
        FNameValidation()
        LNameValidation()
        MobileNValidation()
        PinCodeValidation()
        StateSpinner()
        AddressValidation()
        AddressValidation2()
        cityValidation()
    }

    fun StateSpinner() {
        if (spState != null) {
            spState.setOnSpinnerItemSelectedListener<String> { _, _, index, text ->
                //Toast.makeText(requireContext(), index.toString() + "-" + text, Toast.LENGTH_SHORT).show()
                blurViewState.setTopLeftRadius(resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat())
                blurViewState.setTopRightRadius(resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat())
                blurViewState.setBottomLeftRadius(resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat())
                blurViewState.setBottomRightRadius(resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat())
                if (text.equals("Dadra and Nagar Haveli and Daman and Diu")){
                    spState?.setPadding(resources.getDimensionPixelSize(R.dimen.dimen_22),
                        resources.getDimensionPixelSize(R.dimen.dimen_0),
                        resources.getDimensionPixelSize(R.dimen.dimen_12),
                        resources.getDimensionPixelSize(R.dimen.dimen_0))
                }else{
                    spState?.setPadding(resources.getDimensionPixelSize(R.dimen.dimen_22),
                        resources.getDimensionPixelSize(R.dimen.dimen_12),
                        resources.getDimensionPixelSize(R.dimen.dimen_12),
                        resources.getDimensionPixelSize(R.dimen.dimen_12))
                }
                validateAllField()
            }
            spState.attachSpinnerObserver(this)


            spState.setIsFocusable(true)
            spState.selectItemByIndex(0)
        }
    }

    fun FNameValidation() {
        val fname = etFName.text.toString()
        if (TextUtils.isEmpty(fname)) {
            clFName.setBackgroundResource(R.drawable.normal_bg_edittext)
            ivfNamecheck.visibility = View.GONE
        }


        etFName.doOnTextChanged { text, start, before, count ->
            if (etFName.text.length > 0) {
                ivfNamecheck.visibility = View.GONE
            }
            if (1 < etFName.text.length) {
                ivfNamecheck.visibility = View.VISIBLE
                clFName.setBackgroundResource(R.drawable.bg_edittext_shipping_detail)
                tvFNameError.visibility = View.GONE
            } else {
                tvFNameError.visibility = View.VISIBLE
                clFName.setBackgroundResource(R.drawable.bg_edittext_error)
                tvFNameError.text = getString(R.string.reward_str_104) + " 2 " + getString(R.string.reward_str_105)
            }
            if (etFName.text.length > maxChar) {
                ivfNamecheck.visibility = View.GONE
                clFName.setBackgroundResource(R.drawable.bg_edittext_error)
                tvFNameError.text = getString(R.string.reward_str_106) + " $maxChar " + getString(R.string.reward_str_107)
                tvFNameError.visibility = View.VISIBLE
            }
            validateAllField()
        }


    }

    fun LNameValidation() {
        val lname = etLName.text.toString()
        if (TextUtils.isEmpty(lname)) {
            clLName.setBackgroundResource(R.drawable.normal_bg_edittext)
            ivLNameCheck.visibility = View.GONE
        }
        etLName.doOnTextChanged { text, start, before, count ->
            if (etLName.text.length > 0) {
                ivLNameCheck.visibility = View.GONE
            }
            if (1 < etLName.text.length) {
                ivLNameCheck.visibility = View.VISIBLE
                clLName.setBackgroundResource(R.drawable.bg_edittext_shipping_detail)
                tvLNameError.visibility = View.GONE
            } else {
                tvLNameError.visibility = View.VISIBLE
                clLName.setBackgroundResource(R.drawable.bg_edittext_error)
                tvLNameError.text = getString(R.string.reward_str_104) + " 2 " + getString(R.string.reward_str_105)
            }
            if (etLName.text.length > maxChar) {
                ivLNameCheck.visibility = View.GONE
                clLName.setBackgroundResource(R.drawable.bg_edittext_error)
                tvLNameError.text = getString(R.string.reward_str_106) + " 20 " + getString(R.string.reward_str_107)
                tvLNameError.visibility = View.VISIBLE
            }
            validateAllField()
        }

    }

    fun MobileNValidation() {
        val mobileNo = etMobileNo.text.toString()
        if (TextUtils.isEmpty(mobileNo)) {
            clMobileNumber.setBackgroundResource(R.drawable.normal_bg_edittext)
            ivMobileNumberCheck.visibility = View.GONE
        }
        etMobileNo.doOnTextChanged { text, start, before, count ->
            if (etMobileNo.text.length > 0) {
                ivMobileNumberCheck.visibility = View.GONE
            }
            if (MobilePattern <= etMobileNo.text.length) {
                ivMobileNumberCheck.visibility = View.VISIBLE
                clMobileNumber.setBackgroundResource(R.drawable.bg_edittext_shipping_detail)
                tvMobileNoError.visibility = View.GONE
            } else {
                tvMobileNoError.visibility = View.VISIBLE
                clMobileNumber.setBackgroundResource(R.drawable.bg_edittext_error)
            }
            if (etMobileNo.text.length > MobilePattern) {
                ivMobileNumberCheck.visibility = View.GONE
                clMobileNumber.setBackgroundResource(R.drawable.bg_edittext_error)
                tvMobileNoError.visibility = View.VISIBLE
            }
            validateAllField()
        }

    }

    fun PinCodeValidation() {
        val mobileNo = etPinCode.text.toString()
        if (TextUtils.isEmpty(mobileNo)) {
            clPincode.setBackgroundResource(R.drawable.normal_bg_edittext)
            ivPinCodeCheck.visibility = View.GONE
        }
        etPinCode.doOnTextChanged { text, start, before, count ->
            if (etPinCode.text.length > 0) {
                ivPinCodeCheck.visibility = View.GONE
            }
            if (PinCode <= etPinCode.text.length) {
                ivPinCodeCheck.visibility = View.VISIBLE
                clPincode.setBackgroundResource(R.drawable.bg_edittext_shipping_detail)
                tvPincodeError.visibility = View.GONE
            } else {
                tvPincodeError.visibility = View.VISIBLE
                clPincode.setBackgroundResource(R.drawable.bg_edittext_error)

            }
            if (etPinCode.text.length > PinCode) {
                ivPinCodeCheck.visibility = View.GONE
                clPincode.setBackgroundResource(R.drawable.bg_edittext_error)
                tvPincodeError.visibility = View.VISIBLE
            }
            validateAllField()
        }
        if (shippingDetailModel.isPhisicalProduct && !TextUtils.isEmpty(shippingDetailModel.pincode)){
            etPinCode?.setText(shippingDetailModel.pincode)
        }
    }

    fun AddressValidation() {
        val address = etAddress.text.toString()
        if (TextUtils.isEmpty(address)) {
            clAddressOne.setBackgroundResource(R.drawable.normal_bg_edittext)
            ivAddressCheck.visibility = View.GONE
        }
        etAddress.doOnTextChanged { text, start, before, count ->
            if (etAddress.text.length > 0) {
                ivAddressCheck.visibility = View.GONE
            }
            if (minChar < etAddress.text.length) {
                ivAddressCheck.visibility = View.VISIBLE
                clAddressOne.setBackgroundResource(R.drawable.bg_edittext_shipping_detail)
                tvAddressError.visibility = View.GONE
            } else {
                tvAddressError.visibility = View.VISIBLE
                clAddressOne.setBackgroundResource(R.drawable.bg_edittext_error)
                tvAddressError.text = getString(R.string.reward_str_104) + " $minChar " + getString(R.string.reward_str_105)
            }
            if (etAddress.text.length > maxWord) {
                ivAddressCheck.visibility = View.GONE
                clAddressOne.setBackgroundResource(R.drawable.bg_edittext_error)
                tvAddressError.text = getString(R.string.reward_str_106) + " $maxWord " + getString(R.string.reward_str_107)
                tvAddressError.visibility = View.VISIBLE
            }
            validateAllField()
        }
        if (shippingDetailModel.isPhisicalProduct && !TextUtils.isEmpty(shippingDetailModel.address1)){
            etAddress?.setText(shippingDetailModel.address1)
        }
    }

    fun AddressValidation2() {
        val conformAddress = etConformAddress.text.toString()
        if (TextUtils.isEmpty(conformAddress)) {
            clAddressTwo.setBackgroundResource(R.drawable.normal_bg_edittext)
            ivAddressCheck.visibility = View.GONE
        }
        etConformAddress.doOnTextChanged { text, start, before, count ->
            if (etConformAddress.text.length > 0) {
                ivAddressCheck2.visibility = View.GONE
            }
            if (minChar < etConformAddress.text.length) {
                ivAddressCheck2.visibility = View.VISIBLE
                clAddressTwo.setBackgroundResource(R.drawable.bg_edittext_shipping_detail)
                tvConformAddressError.visibility = View.GONE
            } else {
                tvConformAddressError.visibility = View.VISIBLE
                clAddressTwo.setBackgroundResource(R.drawable.bg_edittext_error)
                tvConformAddressError.text = getString(R.string.reward_str_104) + " $minChar " + getString(R.string.reward_str_105)
            }
            if (etConformAddress.text.length > maxWord) {
                ivAddressCheck2.visibility = View.GONE
                clAddressTwo.setBackgroundResource(R.drawable.bg_edittext_error)
                tvConformAddressError.text = getString(R.string.reward_str_106) + " $maxWord " + getString(R.string.reward_str_107)
                tvConformAddressError.visibility = View.VISIBLE
            }
            validateAllField()
        }
        if (shippingDetailModel.isPhisicalProduct && !TextUtils.isEmpty(shippingDetailModel.address2)){
            etConformAddress?.setText(shippingDetailModel.address2)
        }
    }

    fun cityValidation() {
        val city = etCity.text.toString()
        if (TextUtils.isEmpty(city)) {
            clCity.setBackgroundResource(R.drawable.normal_bg_edittext)
            ivCityCheck.visibility = View.GONE
        }


        etCity.doOnTextChanged { text, start, before, count ->
            if (etCity.text.length > 0) {
                ivCityCheck.visibility = View.GONE
            }

            if (3 < etCity.text.length) {
                ivCityCheck.visibility = View.VISIBLE
                clCity.setBackgroundResource(R.drawable.bg_edittext_shipping_detail)
                tvCityError.visibility = View.GONE
            } else {
                tvCityError.visibility = View.VISIBLE
                clCity.setBackgroundResource(R.drawable.bg_edittext_error)
                tvCityError.text = getString(R.string.reward_str_104) + " 3 " + getString(R.string.reward_str_105)
            }
            validateAllField()
        }
        if (shippingDetailModel.isPhisicalProduct && !TextUtils.isEmpty(shippingDetailModel.city)){
            etCity?.setText(shippingDetailModel.city)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }catch (e:Exception){

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shipping_detail, container, false)
    }

    override fun onSpinnerObserver(isShown: Boolean) {
        if (isShown){
            blurViewState.setTopLeftRadius(resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat())
            blurViewState.setTopRightRadius(resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat())
            blurViewState.setBottomLeftRadius(resources.getDimensionPixelSize(R.dimen.dimen_0).toFloat())
            blurViewState.setBottomRightRadius(resources.getDimensionPixelSize(R.dimen.dimen_0).toFloat())
            spState.spinnerPopupBackgroundColor = ContextCompat.getColor(requireContext(), R.color.home_bg_color)
        }else{
            blurViewState.setTopLeftRadius(resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat())
            blurViewState.setTopRightRadius(resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat())
            blurViewState.setBottomLeftRadius(resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat())
            blurViewState.setBottomRightRadius(resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat())
            spState.spinnerPopupBackgroundColor = ContextCompat.getColor(requireContext(), R.color.blur_one_half_opacity_white_color)
        }
    }

    private fun validateAllField(){
        val firstName = etFName?.text.toString()
        val lastName = etLName?.text.toString()
        val mobile = etMobileNo?.text.toString()
        val pincode = etPinCode?.text.toString()
        val address1 = etAddress?.text.toString()
        val address2 = etConformAddress?.text.toString()
        val city = etCity?.text.toString()
        val state = spState?.text.toString()

        if ((3 < firstName.length && firstName.length < maxChar)
            && (3 < lastName.length && lastName.length < maxChar)
            && (MobilePattern == mobile.length)
            && (PinCode == pincode.length)
            && (address1.length > minChar && address1.length < maxWord)
            && (address2.length > minChar && address2.length < maxWord)
            && (!TextUtils.isEmpty(city) && 3 < city.length)
            && (!TextUtils.isEmpty(state))){
            CommonUtils.applyButtonTheme(requireContext(), btnsSave)
            val shippingDetails = ShippingDetailModel()
            shippingDetails.firstName = firstName
            shippingDetails.lastName = lastName
            shippingDetails.mobile = mobile
            shippingDetails.pincode = pincode
            shippingDetails.address1 = address1
            shippingDetails.address2 = address2
            shippingDetails.city = city
            shippingDetails.state = state
            shippingDetails.isPhisicalProduct = true
            val reviewOrderFragment = ReviewOrderFragment(product)
            val bundle = Bundle()
            bundle.putSerializable(shippingDetailsKey, shippingDetails)
            reviewOrderFragment.arguments = bundle

            btnsSave.setOnClickListener {
                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        CommonUtils.hapticVibration(requireContext(), btnsSave!!,
                            HapticFeedbackConstants.CONTEXT_CLICK
                        )
                    }
                }catch (e:Exception){

                }
                setLocalBroadcast()
                addFragment(R.id.fl_container, this, reviewOrderFragment, false)
            }
        }else{
            btnsSave.setOnClickListener(null)
            CommonUtils.setAppButton3(requireContext(), btnsSave)
        }

    }

    override fun onResume() {
        super.onResume()
        setLog(TAGs, "onResume()")
    }

    override fun onDestroy() {
        try {
            requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        }catch (e:Exception){

        }
        super.onDestroy()
        setLog(TAGs, "onDestroy()")

    }
    private fun setLocalBroadcast(){
        setLog(TAGs, "setLocalBroadcast()")
        (requireActivity() as MainActivity).setLocalBroadcastEventCall(this, Constant.SHIPPING_DETAILS_EVENT)
    }

    override fun onLocalBroadcastEventCallBack(context: Context?, intent: Intent) {
        val event = intent.getIntExtra("EVENT", 0)
        setLog(TAGs, "$event")
        if (event == Constant.SHIPPING_DETAILS_EVENT_RESULT_CODE) {
            val error = intent.getStringExtra("error")
            setLog(TAGs, "$error")
            if (!TextUtils.isEmpty(error) && error.equals("UnAvailable zip code", true)){
                //Toast.makeText(requireContext(), "$error-1", Toast.LENGTH_LONG).show()
                etPinCode.setText("")
                ivPinCodeCheck.visibility = View.GONE
                clPincode.setBackgroundResource(R.drawable.bg_edittext_error)
                tvPincodeError.visibility = View.VISIBLE
            }
        }
    }
}

