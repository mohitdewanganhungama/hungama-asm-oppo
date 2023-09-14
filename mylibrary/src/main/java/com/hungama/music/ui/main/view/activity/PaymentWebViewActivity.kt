package com.hungama.music.ui.main.view.activity

import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.KeyEvent
import android.webkit.*
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.*
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import com.amplitude.api.Amplitude
import com.appsflyer.AFInAppEventParameterName
import com.appsflyer.AppsFlyerLib
import com.hungama.music.HungamaMusicApp
import com.hungama.music.data.model.MessageModel
import com.hungama.music.data.model.MessageType
import com.hungama.music.data.model.PlanNames
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.*
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.main.viewmodel.UserSubscriptionViewModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant.PAY_API_RESPONSE_FAILED_CODE
import com.hungama.music.utils.Constant.PAY_API_RESPONSE_SUCCESS_CODE
import com.hungama.music.utils.payment.goolgewallet.BillingProcessor
import com.hungama.music.utils.payment.goolgewallet.PurchaseInfo
import com.hungama.music.utils.preference.SharedPrefHelper

import com.hungama.music.R
import kotlinx.android.synthetic.main.activity_payment_web_view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.security.AccessController.getContext
import java.util.*
import java.util.concurrent.TimeUnit


class PaymentWebViewActivity : AppCompatActivity() {
    private val TAG = PaymentWebViewActivity::class.java.name
    private var webSettings: WebSettings? = null

//
    private var isGoogleBillingPayCall = false
    private var isPaymentSuccess = false

    private var googleBillingPaysplitArray= HashMap<String,String>()
    private var gpOrdersplitArray= HashMap<String,String>()
    private var paySplitArray= HashMap<String,String>()
    private var productIdsList = HashMap<String,String>()

    private var currentWebURL=""
    private var eventlastWebURL=""
    private var mClient: CustomTabsClient? = null
    private var isCustomChromeTabClick = false
    var mConnection: CustomTabsServiceConnection? = null
    private var mPackageNameToBind = ""
    private var isFromSplash = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_web_view)
        val url = intent.getStringExtra("url")
        isFromSplash = intent.getBooleanExtra("fromSplash", false)
        setLog("PaymentUrl" + " " + url)
        if (url != null && !TextUtils.isEmpty(url)) {
            loadUrl(url)
        } else {
            val messageModel = MessageModel(getString(R.string.discover_str_2), MessageType.NEUTRAL, true)
            CommonUtils.showToast(this@PaymentWebViewActivity, messageModel)
        }

        isGoogleBillingPayCall = false
        productIdsList=HashMap()

        mConnection = object : CustomTabsServiceConnection() {
            override fun onCustomTabsServiceConnected(
                componentName: ComponentName,
                customTabsClient: CustomTabsClient
            ) {
                mClient = customTabsClient
                mClient?.warmup(0)
                setLog(TAG, "CustomTabsServiceConnection ServiceConnected")
            }

            override fun onServiceDisconnected(componentName: ComponentName) {
                mClient = null
                setLog(TAG, "CustomTabsServiceConnection ServiceDisconnected")
            }

            override fun onBindingDied(name: ComponentName) {
                setLog(TAG, "CustomTabsServiceConnection onBindingDied")
            }

            override fun onNullBinding(name: ComponentName) {
                setLog(TAG, "CustomTabsServiceConnection onNullBinding")
            }
        }
        mPackageNameToBind = "com.android.chrome"
        val ok =
            CustomTabsClient.bindCustomTabsService(this@PaymentWebViewActivity, mPackageNameToBind, mConnection!!)
        setLog(TAG, "bindCustomTabsService: mPackageNameToBind:${mPackageNameToBind} state:${ok}")

//        googleInAPPSetup()
    }

    override fun onResume() {
        super.onResume()
        setLog(TAG, "============onResume()======");
        isCustomChromeTabClick = false;
    }

    /**
     * Loads given url in web view
     *
     */
    private fun loadUrl(url: String?) {
        webSettings = webView?.settings
        webView?.settings?.javaScriptEnabled = true
        webView?.settings?.javaScriptCanOpenWindowsAutomatically=true
        webView?.settings?.allowContentAccess=true
        webView?.settings?.domStorageEnabled = true
        webView?.settings?.allowFileAccess = true
        webView?.settings?.setSupportZoom(true)
        webView?.settings?.loadsImagesAutomatically = true
        webView?.isClickable = true

        webView?.clearHistory()
        webView?.clearCache(true)


        // Set web view client
        var requestTime:Date?=null
        webView?.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {

                setLog(TAG, "=====shouldOverrideUrlLoading=========" + request?.url);

                val tab = request?.url?.getQueryParameter("tab")
                setLog(TAG, "=====shouldOverrideUrlLoading========= tab " + tab)
                if (!tab.isNullOrEmpty() && tab.equals("cct")){
                    loadUrlInChromeService(request?.url!!)
                    return true;
                } else if (request?.url.toString().contains("upi://")) {

                    val uri = Uri.parse(request?.url.toString())
                    val UPI_APP_PACKAGE_NAME = uri.getQueryParameter("package")
                    setLog(TAG, "shouldOverrideUrlLoading: "+UPI_APP_PACKAGE_NAME)
                    setLog("UpiAppPackage", UPI_APP_PACKAGE_NAME.toString())
                    val upiPayIntent = Intent(Intent.ACTION_VIEW)
//                    if(request?.url.toString().contains("com.phonepe.app")){
                    upiPayIntent.data = SplitPhonepeUrl(request?.url.toString())
//                    }else upiPayIntent.data = request?.url


                    //phonepe
                    upiPayIntent.setPackage(UPI_APP_PACKAGE_NAME)
                    val UPI_PAYMENT_REQUEST_CODE = 0


                    // will always show a dialog to user to choose an app
                    val intent = Intent.createChooser(upiPayIntent, "Pay with")

                    // check if intent resolves
                    if (null != intent.resolveActivity(packageManager)) {
                        startActivityForResult(intent, UPI_PAYMENT_REQUEST_CODE)
                    } else {
                        val messageModel = MessageModel(getString(R.string.payment_str_5), MessageType.NEUTRAL, true)
                        CommonUtils.showToast(this@PaymentWebViewActivity, messageModel)
                    }
                    return true
                }
                else if (request?.url.toString().contains("https://payapihmopftd.hungama.com/payment_success") && !isPaymentSuccess) {
                    getUserSubscriptionStatus()
                    setLog("PaymentSuccessFullURL", request?.url.toString())
                    isPaymentSuccess = true
                }
//                else if (request?.url.toString().contains("https://payapihmopftd.hungama.com/payment_success")) {
//
//                    val returnIntent = Intent()
//                    returnIntent.putExtra("result", "success")
//                    returnIntent.putExtra("isButtonNotClicked", true)
//                    setResult(PAY_API_RESPONSE_SUCCESS_CODE, returnIntent)
//                    setLog("payapiUrl-req-1", request?.url.toString())
////                    finish()
//
//                }
                else if (request?.url.toString().contains("/payment/success/") || request?.url.toString()
                        .contains("http://pac.hungama.com/pages/response/success.php") || request?.url.toString()
                        .contains(
                            "http://hungama.com/payment/success/"
                        )
                ) {


                    setLog("payapiUrl-req-1", request?.url.toString()!!)
                    if (isFromSplash){
                        startActivity(Intent(this@PaymentWebViewActivity, MainActivity::class.java))
                    }
                    else{
                        val returnIntent = Intent()
                        returnIntent.putExtra("result", "success")
                        setResult(PAY_API_RESPONSE_SUCCESS_CODE, returnIntent)
                        }
                    finish()
                } else if (request?.url.toString()
                        .contains("http://pac.hungama.com/pages/response/fail.php") || request?.url.toString()
                        .contains(
                            "http://hungama.com/payment/failed/"
                        )
                ) {
                    /*request?.url.toString().contains("https://payapihmopftd.hungama.com/payment_fail")
                    11-07-2021 - Above condition is removed as per discussed with @sushant shinde*/
                    if (isFromSplash){
                        startActivity(Intent(this@PaymentWebViewActivity, MainActivity::class.java))
                    }
                    else {
                        val returnIntent = Intent()
                        returnIntent.putExtra("result", "failed")
                        setResult(PAY_API_RESPONSE_FAILED_CODE, returnIntent)
                        setLog("payapiUrl-req-2", request?.url.toString()!!)
                    }
                    finish()
                }
                else if (request?.url.toString().contains("/billing/pay.php")) {
                    isGoogleBillingPayCall = true

                    val args = request?.url?.getQueryParameterNames()
                    if(args!=null&&args.size>0){
                        args?.forEach {
                            if(request?.url != null  && it !=null && request?.url?.getQueryParameter(it) !=null)
                            {
                                googleBillingPaysplitArray.put(it,request?.url?.getQueryParameter(it)!!)
                            }
                        }
                    }
                    if (request?.url != null)
                    {
                        view?.loadUrl(request?.url.toString())
                    }
                    setLog(TAG, "GoogleBillingPayCall step 1: isGoogleBillingPayCall->${isGoogleBillingPayCall} googleBillingPaysplitArray:${googleBillingPaysplitArray}")

                }
                else if (request?.url.toString()
                        .contains("https://pac.hungama.com/wvclose.php") || request?.url.toString()
                        .contains(
                            "http://pac.hungama.com/wvclose.php"
                        )
                ) {
                    setLog(TAG, "GoogleBillingPayCall step 2: isGoogleBillingPayCall->${isGoogleBillingPayCall} googleBillingPaysplitArray:${googleBillingPaysplitArray}")
                    if (isGoogleBillingPayCall) {
                        val args = request?.url?.getQueryParameterNames()
                        if(args!=null&&args.size>0){
                            args?.forEach {
                                if(request?.url !=null &&it !=null && request?.url?.getQueryParameter(it) !=null)
                                {
                                    gpOrdersplitArray.put(it,request?.url?.getQueryParameter(it)!!)
                                }
                                if(it?.contains("store_payment_id")!!){
                                    if(request?.url !=null &&it !=null && request?.url?.getQueryParameter(it) !=null)
                                    {
                                        productIdsList.put("store_payment_id",request?.url?.getQueryParameter(it)!!)
                                    }
                                }
                            }

                            setLog(TAG, "GoogleBillingPayCall gpOrdersplitArray->"+gpOrdersplitArray)
                            if(googleBillingPaysplitArray!=null&&productIdsList!=null&&gpOrdersplitArray?.size!!>0){
                                googleInAPPSetup()
                            }else{
                                onBackPressed()
                            }
                        }else{
                            onBackPressed()
                        }
                    }else{
                        onBackPressed()
                    }


                }else {
                    if (request?.url != null)
                    {
                        view?.loadUrl(request?.url.toString())
                    }
                }


                return true

            }

            override fun onLoadResource(view: WebView?, url: String?) {
                super.onLoadResource(view, url)

                val domainUrl = CommonUtils.getUrlWithoutParameters(view?.url!!)!!
                setLog(TAG, "onLoadResource: view url:${domainUrl} progress:${view?.progress}")


                if(domainUrl.contains("hungama.com/payment") || domainUrl.contains("hungama.com/plan")){
                    if(!eventlastWebURL.equals(domainUrl) && !currentWebURL.equals(domainUrl) ){
                        currentWebURL= domainUrl!!
                        requestTime = DateUtils.getCurrentDateTime()
                    }

                    if(view?.progress==100 && !eventlastWebURL.equals(domainUrl)){
                        setLog(TAG, "onLoadResource: hungama.com/payment callWebLoadEvent :${domainUrl} progress:${view?.progress}")
                        callWebLoadEvent(domainUrl, 0, "")
                    }
                }
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                currentWebURL=CommonUtils.getUrlWithoutParameters(view?.url!!)!!
                requestTime = DateUtils.getCurrentDateTime()
                setLog(TAG, "onPageStarted: view url:${view?.url} progress:${view?.progress}")
            }


            @RequiresApi(Build.VERSION_CODES.M)
            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?)
            {
                super.onReceivedError(view, request, error)
                val domainUrl=CommonUtils.getUrlWithoutParameters(view?.url!!)!!
                if(view?.progress==100 && !eventlastWebURL.equals(domainUrl)){
                    setLog(TAG, "onPageFinished: lastWebURL:${domainUrl} progress:${view?.progress}")
                    callWebLoadEvent(domainUrl, error!!.errorCode, error.description.toString())
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                val domainUrl=CommonUtils.getUrlWithoutParameters(view?.url!!)!!
                if(view.progress==100 && !eventlastWebURL.equals(domainUrl)){
                    setLog(TAG, "onPageFinished: lastWebURL:${domainUrl} progress:${view?.progress}")
                    callWebLoadEvent(domainUrl, 0, "")
                }
            }

            override fun shouldInterceptRequest(
                view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                setLog("shouldInterceptRequest", request?.url.toString() + " ===== " + request?.method)
                return super.shouldInterceptRequest(view, request)
            }

            fun callWebLoadEvent(url:String, errorCode:Int, description :String){
                CoroutineScope(Dispatchers.IO).launch {
                    /**
                     * event property start
                     */
                    val responseTime = DateUtils.getCurrentDateTime()
                    val diffInMillies: Long = Math.abs(requestTime?.getTime()!! - responseTime?.getTime()!!)

                    val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)


                    val hashMap = HashMap<String, String>()
                    if(paySplitArray!=null && paySplitArray.containsKey("content_id")){
                        if(!TextUtils.isEmpty(HungamaMusicApp.getInstance().getEventData(paySplitArray?.get("content_id")!!).sourceName)){
                            hashMap.put(EventConstant.SOURCE_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData(paySplitArray?.get("content_id")!!).sourceName)
                        }
                    }

                    hashMap.put(EventConstant.LOAD_TIME,""+diff)
                    hashMap.put(EventConstant.ACTION_EPROPERTY,""+paySplitArray?.get("content_id"))

                    if (errorCode != 0){
                        hashMap.put(EventConstant.RESPONSECODE_EPROPERTY, errorCode.toString())
                        hashMap.put(EventConstant.ERRORCODE_EPROPERTY, errorCode.toString())
                        hashMap.put(EventConstant.NAME, description)
                    }
                    else
                    {
                        hashMap.put(EventConstant.RESPONSECODE_EPROPERTY,"200")
                    }
                    hashMap.put(EventConstant.NETWORKTYPE_EPROPERTY, "" + ConnectionUtil(this@PaymentWebViewActivity).networkType)
                    hashMap.put(EventConstant.URL_EPROPERTY, url)

                    EventManager.getInstance().sendEvent(WebViewPerformanceEvent(hashMap))
                    eventlastWebURL=url

                }

            }

        }

        if (!TextUtils.isEmpty(url)) {
            val args = url?.toUri()?.queryParameterNames
            if(args!=null&&args.size>0){
                args.forEach {
                    if(url?.toUri() != null  && it !=null && url?.toUri()?.getQueryParameter(it) !=null)
                    paySplitArray.put(it,url?.toUri()?.getQueryParameter(it)!!)
                }
            }

            if (url != null)
            webView.loadUrl(url!!)
            setLog("payapiUrl", url!!)

            setLog("paySplitArray", "paySplitArray->"+paySplitArray)

            CoroutineScope(Dispatchers.IO).launch {
                val hashMap = java.util.HashMap<String, String>()
                hashMap.put(EventConstant.PROMO, "false")
                if(paySplitArray!=null && paySplitArray.containsKey("content_id")){
                    if(!TextUtils.isEmpty(HungamaMusicApp.getInstance().getEventData(paySplitArray?.get("content_id")!!).sourceName)){
                        hashMap.put(EventConstant.SOURCE_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData(paySplitArray?.get("content_id")!!).sourceName)
                    }
                }
                EventManager.getInstance().sendEvent(SubscriptionPlanPageOpenEvent(hashMap))
            }


        }else{
            val messageModel = MessageModel(getString(R.string.discover_str_2), MessageType.NEUTRAL, true)
            CommonUtils.showToast(this@PaymentWebViewActivity, messageModel)
        }
    }


    override fun onDestroy() {

        super.onDestroy()
        val hashMap = HashMap<String, String>()
        EventManager.getInstance().sendEvent(RentBackFromRentPageEvent(hashMap))
        if(mConnection!=null&&!isFinishing){
            unbindService(mConnection!!)
        }

        mConnection = null
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.action == KeyEvent.ACTION_DOWN) {
            when (keyCode) {
                KeyEvent.KEYCODE_BACK -> {
                    if (webView.canGoBack()) {
                        webView.goBack()
                    } else {
                        onBackPressed()
                    }
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun loadUrlInChromeService(url: Uri) {
        setLog(TAG, "loadUrlInChromeService: "+url)
        if (mClient == null) {
            if (getContext() != null && !isFinishing()) {
                Toast.makeText(this@PaymentWebViewActivity, "Error in service connect", Toast.LENGTH_SHORT).show()
            }
            return
        }
        val mCustomTabsSession: CustomTabsSession? =
            mClient!!.newSession(object : CustomTabsCallback() {
                override fun onNavigationEvent(navigationEvent: Int, extras: Bundle?) {
                    super.onNavigationEvent(navigationEvent, extras)
                    System.out.println("CustomTabsSession :: navigationEvent = [" + navigationEvent + "], extras = [" + extras + "]");
                }

                override fun extraCallback(callbackName: String, args: Bundle?) {
                    super.extraCallback(callbackName, args)
                    System.out.println("CustomTabsSession :: callbackName = [" + callbackName + "], args = [" + args + "]");
                }

                override fun onMessageChannelReady(extras: Bundle?) {
                    super.onMessageChannelReady(extras)
                    System.out.println("CustomTabsSession :: extras = [" + extras + "]");
                }

                override fun onPostMessage(message: String, extras: Bundle?) {
                    super.onPostMessage(message, extras)
                    System.out.println("CustomTabsSession :: message = [" + message + "], extras = [" + extras + "]");
                }

                override fun onRelationshipValidationResult(
                    relation: Int,
                    requestedOrigin: Uri,
                    result: Boolean,
                    extras: Bundle?
                ) {
                    super.onRelationshipValidationResult(relation, requestedOrigin, result, extras)
                    System.out.println("CustomTabsSession :: relation = [" + relation + "], requestedOrigin = [" + requestedOrigin + "], result = [" + result + "], extras = [" + extras + "]");
                }
            })
        mCustomTabsSession?.mayLaunchUrl(url!!, null, null)
        val builder: CustomTabsIntent.Builder =CustomTabsIntent.Builder()
        val customTabsIntent: CustomTabsIntent = builder.build()
        customTabsIntent.intent.setPackage(mPackageNameToBind)
        customTabsIntent.launchUrl(this@PaymentWebViewActivity, url)
        isCustomChromeTabClick = true
            finish()
    }


    private var bp: BillingProcessor? = null

    /**
     * use for hungama un project
     */
//    private val LICENSE_KEY: String? = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtmDSG/fsqCK82h7HDXsWJwjaDcusXdHDxMeAGB5sanTZDnjFrTMnG3bt45jogqBvhrYen/SBD95Jt93cXM/uhEeQUzinL3/gfe48Y6blkXzeBkDMeGlk9D8Qvl4uYEb2FHS95OOb8WmIHcYvVC+zZmXlOuGDVw5AdFhBPQQIswTeJ3Pmt4NDcA/fEraYrgsJ9b8/viFSrj1+tF6nMWkAABFZ36jgNZJWY6hp4SJwS9H4GaB/i22nMIjBXAvV3Y8dXpaT7zJlUPdx1T6JbNeWgiQFgO3iQbHKWGNHqkCsaPZMdPXIzplQC+eXS/Y/mp5MyQf9b+VamjrKg1HQDk+9CQIDAQAB"
    /**
     * use for hungama music project
     */
    private val LICENSE_KEY: String = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApfPPciZRDbMMQs4I3HUflV+ntCBrvlKKrrbXkzH4G5m+lRugOPO/S38ABaLsKrDdosKrlL+/TFpNOXsWdgPzarQX84p58jXod48I0iSJtjHH+gTgvUnM5lBiZJrdzfwc+xCDMNVe1YIqSfTQ1SXgDcLdd1tNNGpRN7tLP5RFiwFrpfDevT29NNArH+vtXkC4cNcttSx7DwVfIdE0fSmZN1CtH+T2MKVTRzZgty3ZecGBoaUzbJejCPWwFUrOZCvo5FmL4gxYxjgrs4jhZ2wyn2cJLE2YImfMNrrnKSP6RMZQwyWnsOZd1hvEOgIQP9YXdHVGYxDQpSbbxTBpROM61QIDAQAB"
    private val MERCHANT_ID: String = "07070533480270152344"

    private fun googleInAPPSetup() {
        bp = BillingProcessor(
            this@PaymentWebViewActivity,
            LICENSE_KEY,
            MERCHANT_ID,
            object : BillingProcessor.IBillingHandler {
                override fun onProductPurchased(
                    @NonNull productId: String,
                    @Nullable purchaseInfo: PurchaseInfo?
                ) {
                    showToast("onProductPurchased productId: $productId --> purchaseInfo:${purchaseInfo}")
                    updatePurchaseItem(purchaseInfo)

                }

                override fun onBillingError(errorCode: Int, @Nullable error: Throwable?) {
                    showToast("onBillingError: errorCode${errorCode} error:${error}")
//                    val messageModel = MessageModel("onBillingError code:${errorCode}:${error?.message}", MessageType.NEGATIVE, true)
//                    CommonUtils.showToast(this@PaymentWebViewActivity, messageModel)

                }

                override fun onBillingInitialized() {
                    callInApp()
                }

                override fun onPurchaseHistoryRestored() {
                    showToast("onPurchaseHistoryRestored bp connected:${bp?.isConnected} productIdsList:${productIdsList} googleBillingPaysplitArray:${googleBillingPaysplitArray}")

                    try{
                        if (bp != null && bp?.isConnected!! && productIdsList.size > 0) {

                            var details: PurchaseInfo? = null
                            if (googleBillingPaysplitArray.containsKey("plan_type")) {

                                if (googleBillingPaysplitArray.get("plan_type")
                                        ?.contains("subscription", true)!!
                                ) {
                                    details = bp?.getSubscriptionPurchaseInfo(productIdsList?.get("store_payment_id"))!!
                                } else {
                                    details = bp?.getPurchaseInfo(productIdsList?.get("store_payment_id"))!!
                                }
                            }

                            if (details != null) {
                                updatePurchaseItem(details)
                            }
                        }
                    }catch (exp:Exception){
                        exp.printStackTrace()
                    }



                }
            })
    }

    private fun callInApp(){
        if (bp != null && bp?.isConnected!! && productIdsList.containsKey("store_payment_id") && googleBillingPaysplitArray.containsKey("plan_type")) {
            showToast(
                "onBillingInitialized plan_type:${googleBillingPaysplitArray.get("plan_type")} store_payment_id:${
                    productIdsList?.get(
                        "store_payment_id"
                    )
                }"
            )
            if (googleBillingPaysplitArray.get("plan_type")
                    ?.contains("subscription", true)!!
            ) {
                bp?.subscribe(
                    this@PaymentWebViewActivity,
                    productIdsList?.get("store_payment_id")
                )
            } else {
                bp?.purchase(
                    this@PaymentWebViewActivity,
                    productIdsList?.get("store_payment_id")
                )
            }
        }else{
            showToast("callInApp is null")
        }
    }

    fun showToast(message: String) {
        setLog(TAG, "showToast: ${message}")
    }

    private fun updatePurchaseItem(purchaseInfo: PurchaseInfo?) {
        showToast("onProductPurchased purchaseToken final: ${purchaseInfo?.toString()}")
        showToast("onProductPurchased store_payment_id: ${gpOrdersplitArray?.containsKey("store_payment_id")}")
        showToast("onProductPurchased plan_type: ${googleBillingPaysplitArray.containsKey("plan_type")}")

        if (purchaseInfo?.purchaseData?.purchaseToken != null && gpOrdersplitArray.containsKey("store_payment_id") && googleBillingPaysplitArray.containsKey("plan_type")) {
            var jsonObject = JSONObject()
            jsonObject.put("platform_id", "1") //For android platform_id = 1 For ios platform_id = 4
            jsonObject.put("payment_id", "11") //for Google payment_id - 11 for Ios  payment_id - 10
            if (gpOrdersplitArray.containsKey("product_id")) {
                jsonObject.put("product_id", gpOrdersplitArray?.get("product_id"))
            }

            if (gpOrdersplitArray.containsKey("identity")) {
                jsonObject.put("identity", gpOrdersplitArray?.get("identity"))
            }

            if (gpOrdersplitArray.containsKey("store_payment_id")) {
                jsonObject.put("store_payment_id", gpOrdersplitArray?.get("store_payment_id"))
            }

            if (!TextUtils.isEmpty(purchaseInfo?.purchaseData?.purchaseToken)) {
                jsonObject.put("purchase_token", purchaseInfo?.purchaseData?.purchaseToken)
            }

            if (gpOrdersplitArray.containsKey("hardware_id")) {
                jsonObject.put("hardware_id", gpOrdersplitArray?.get("hardware_id"))
            }

            jsonObject.put("store_email_id", "")
            if (paySplitArray != null && paySplitArray.containsKey("content_id")) {
                jsonObject.put("content_id", paySplitArray?.get("content_id"))
            }

            if (gpOrdersplitArray.containsKey("plan_id")) {
                jsonObject.put("plan_id", gpOrdersplitArray?.get("plan_id"))
            }

            if (gpOrdersplitArray.containsKey("order_id")) {
                jsonObject.put("order_id", gpOrdersplitArray?.get("order_id"))
            }

            if (gpOrdersplitArray.containsKey("payment_id")) {
                jsonObject.put("payment_id", gpOrdersplitArray?.get("payment_id"))
            }

            showToast("GoogleBillingPayCall callGoogleNotifyBillingApi"+jsonObject)


            if (googleBillingPaysplitArray.get("plan_type")?.contains("subscription", true)!!
            ) {
                bp?.loadOwnedPurchasesFromGoogleAsync(object : BillingProcessor.IPurchasesResponseListener {
                    override fun onPurchasesSuccess() {
                        showToast("Subscriptions updated.")
                        callGoogleNotifyBillingApi(jsonObject)
                    }

                    override fun onPurchasesError() {
                        showToast("Subscriptions update eroor.")
                        callGoogleNotifyBillingApi(jsonObject)
                    }
                })
            } else {
                setLog(TAG, "updatePurchaseItem: product_id:${gpOrdersplitArray?.get("store_payment_id")}")
                bp?.consumePurchaseAsync(
                    gpOrdersplitArray?.get("store_payment_id"),
                    object : BillingProcessor.IPurchasesResponseListener{
                        override fun onPurchasesSuccess() {
                            showToast("Successfully consumed")
                            callGoogleNotifyBillingApi(jsonObject)
                        }

                        override fun onPurchasesError() {
                            showToast("Not consumed")
                            callGoogleNotifyBillingApi(jsonObject)
                        }
                    })
            }
        } else {
            showToast("callGoogleNotifyBillingApi purchase purchaseToken null")
        }
    }

    private fun callGoogleNotifyBillingApi(jsonObject: JSONObject){

        Handler(Looper.getMainLooper()).post {
                showToast("callGoogleNotifyBillingApi call API" + jsonObject)
                val userSubscriptionViewModel = ViewModelProvider(
                    this@PaymentWebViewActivity
                ).get(UserSubscriptionViewModel::class.java)

                if (ConnectionUtil(this@PaymentWebViewActivity).isOnline) {
                    userSubscriptionViewModel?.googleStoreNotifyBilling(
                        this@PaymentWebViewActivity,
                        jsonObject
                    )?.observe(
                        this@PaymentWebViewActivity,
                        androidx.lifecycle.Observer {
                            if(bp!=null){
                                bp?.release()
                            }
                            if(it.data!=null) {
                                if (gpOrdersplitArray.containsKey("order_id")) {
                                    val mURL =
                                        "https://payapihmopftd.hungama.com/order/" + gpOrdersplitArray?.get(
                                            "order_id"
                                        ) //+"/"+ SharedPrefHelper.getInstance().getLanguage()
                                    showToast("callGoogleNotifyBillingApi: mURL:${mURL}")
                                    if (mURL != null) {
                                        webView?.loadUrl(mURL)
                                    }

                                    isGoogleBillingPayCall = false
                                    gpOrdersplitArray.clear()
                                    paySplitArray.clear()
                                    googleBillingPaysplitArray.clear()
                                }
                            }
                        })
                }
        }
    }

    fun getUserSubscriptionStatus() {
            val userSubscriptionViewModel = ViewModelProvider(this)[UserSubscriptionViewModel::class.java]

            if (ConnectionUtil(this).isOnline) {
                var requestTime: Date? = DateUtils.getCurrentDateTime()
                userSubscriptionViewModel?.getUserSubscriptionStatusDetail(this)?.
                observe(this){
                    when(it.status){
                        Status.SUCCESS->{

                            if (it?.data != null && it?.data?.success!!) {

                                val returnIntent = Intent()
                                returnIntent.putExtra("result", "success")
                                returnIntent.putExtra("isButtonNotClicked", true)
                                setResult(PAY_API_RESPONSE_SUCCESS_CODE, returnIntent)

                                val responseTime = DateUtils.getCurrentDateTime()
                                val diffInMillies: Long = Math.abs(requestTime?.getTime()!! - responseTime?.getTime()!!)

                                val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                                val planName = if (intent.hasExtra("planName")) intent.getStringExtra("planName") else ""
                                val contentId = if (intent.hasExtra("contentId")) intent.getStringExtra("contentId") else ""

                                CoroutineScope(Dispatchers.IO).launch {
                                    if(!TextUtils.isEmpty(planName)){
                                        if (planName!!.contains(PlanNames.TVOD.name, true) || planName.contains(
                                                PlanNames.PTVOD.name, true)|| planName.contains(
                                                PlanNames.LE.name, true)){
                                            /* Track Events in real time */

                                            val eventValue: MutableMap<String, Any> = HashMap()

                                            if(it?.data?.data?.tvod!=null && it?.data?.data?.tvod?.size!!>0){
                                                it?.data?.data?.tvod?.forEach {
                                                    if(it?.contentId?.contains(contentId!!,true)!!){
                                                        eventValue.put(AFInAppEventParameterName.CONTENT_ID,it?.contentId!!)
                                                        eventValue.put(AFInAppEventParameterName.REVENUE,it?.planPrice!!)
                                                        eventValue.put(AFInAppEventParameterName.CURRENCY,it?.currency!!)
                                                        eventValue.put(EventConstant.AF_PURCHASE_METHOD_PROPERTY,it?.paymentSource!!)
                                                        eventValue.put(EventConstant.CUID, SharedPrefHelper.getInstance().getUserId().toString())

                                                        val hashMapCheck = HashMap<String,String>()
                                                        hashMapCheck.put(AFInAppEventParameterName.CONTENT_ID,it?.contentId!!)
                                                        hashMapCheck.put(AFInAppEventParameterName.REVENUE,it?.planPrice!!)
                                                        hashMapCheck.put(AFInAppEventParameterName.CURRENCY,it?.currency!!)
                                                        hashMapCheck.put(EventConstant.AF_PURCHASE_METHOD_PROPERTY,it?.paymentSource!!)
                                                        hashMapCheck.put(EventConstant.CUID, SharedPrefHelper.getInstance().getUserId().toString())
                                                        EventManager.getInstance().sendEvent(AF_Subscribe(hashMapCheck))

                                                        val hashMap = HashMap<String,String>()
                                                        hashMap.put(EventConstant.PAYMENT_MODE,it?.paymentSource!!)
                                                        hashMap.put(EventConstant.NETWORKTYPE_EPROPERTY,"" + ConnectionUtil(this@PaymentWebViewActivity).networkType)
                                                        hashMap.put(EventConstant.PLAN_SELECTED,it?.planName.toString())
                                                        hashMap.put(EventConstant.PROMO,it?.couponCode.toString())
                                                        hashMap.put(EventConstant.RESPONSECODE_EPROPERTY,"200")
                                                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, diff.toString())
                                                        EventManager.getInstance().sendEvent(SubscriptionSuccessfulPageEvent(hashMap))

                                                        val hashMap1 = HashMap<String,String>()
                                                        hashMap1.put(EventConstant.MODE_EPROPERTY,it?.paymentSource!!)
                                                        hashMap1.put(EventConstant.METHOD_EPROPERTY,it?.paymentSourceDetails!!)
                                                        hashMap1.put(EventConstant.SOURCE_NAME_EPROPERTY, ""+ MainActivity.lastItemClicked+"_"+ MainActivity.headerItemName)
                                                        EventManager.getInstance().sendEvent(RentPaymentOptionEvent(hashMap1))
                                                    }
                                                }
                                                AppsFlyerLib.getInstance().logEvent(HungamaMusicApp.getInstance().applicationContext!!,EventConstant.AF_PURCHASE_ENAME, eventValue)
                                            }

                                            setLog("TAG", "AppsFlyerLib eventName:${EventConstant.AF_PURCHASE_ENAME} eventProperties:${eventValue}")
                                        }else{

                                            /* Track Events in real time */

                                            if(it?.data?.data?.subscription!=null){
                                                val eventValue: MutableMap<String, Any> = HashMap()
                                                eventValue.put(AFInAppEventParameterName.REVENUE,it?.data?.data?.subscription?.planPrice!!)
                                                eventValue.put(AFInAppEventParameterName.CURRENCY,it?.data?.data?.subscription?.currency!!)
                                                eventValue.put(EventConstant.AF_START_DATE_PROPERTY,it?.data?.data?.subscription?.subscriptionStartDate!!)
                                                eventValue.put(EventConstant.AF_EXPIRATION_DATE_PROPERTY,it?.data?.data?.subscription?.subscriptionEndDate!!)
                                                eventValue.put(EventConstant.AF_SUBSCRIPTION_PERIOD_PROPERTY,it?.data?.data?.subscription?.planValidityName!!)
                                                eventValue.put(EventConstant.AF_SUBSCRIPTION_METHOD_PROPERTY,it?.data?.data?.subscription?.paymentSource!!)
                                                eventValue.put(EventConstant.CUID, SharedPrefHelper.getInstance().getUserId().toString())
                                                AppsFlyerLib.getInstance().logEvent(HungamaMusicApp.getInstance().applicationContext!!,EventConstant.AF_SUBSCRIBE_ENAME, eventValue)

                                                val hashMapCheck = HashMap<String,String>()
                                                hashMapCheck[AFInAppEventParameterName.REVENUE] = it?.data?.data?.subscription?.planPrice!!
                                                hashMapCheck[AFInAppEventParameterName.CURRENCY] = it?.data?.data?.subscription?.currency!!
                                                hashMapCheck.put(EventConstant.AF_START_DATE_PROPERTY,it?.data?.data?.subscription?.subscriptionStartDate!!)
                                                hashMapCheck.put(EventConstant.AF_EXPIRATION_DATE_PROPERTY,it?.data?.data?.subscription?.subscriptionEndDate!!)
                                                hashMapCheck.put(EventConstant.AF_SUBSCRIPTION_PERIOD_PROPERTY,it?.data?.data?.subscription?.planValidityName!!)
                                                hashMapCheck.put(EventConstant.AF_SUBSCRIPTION_METHOD_PROPERTY,it?.data?.data?.subscription?.paymentSource!!)
                                                hashMapCheck.put(EventConstant.CUID, SharedPrefHelper.getInstance().getUserId().toString())


                                                EventManager.getInstance().sendEvent(AF_Subscribe(hashMapCheck))
                                                val hashMap = HashMap<String,String>()

                                                hashMap.put(EventConstant.PAYMENT_MODE,it?.data?.data?.subscription?.paymentSource!!)
                                                hashMap.put(EventConstant.NETWORKTYPE_EPROPERTY,"" + ConnectionUtil(this@PaymentWebViewActivity).networkType)
                                                hashMap.put(EventConstant.PLAN_SELECTED,it?.data?.data?.subscription?.planName.toString())
                                                hashMap.put(EventConstant.PROMO,it?.data?.data?.subscription?.couponCode.toString())

                                                hashMap.put(EventConstant.RESPONSECODE_EPROPERTY,"200")
                                                hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, diff.toString())

                                                EventManager.getInstance().sendEvent(SubscriptionSuccessfulPageEvent(hashMap))

                                                setLog("TAG", "AppsFlyerLib eventName:${EventConstant.AF_SUBSCRIBE_ENAME} eventProperties:${eventValue}")
                                            }



                                        }
                                    }
                                }

                            }
                        }

                        Status.LOADING ->{

                        }

                        Status.ERROR ->{
                        }
                    }
                }
            } else {
                val messageModel = MessageModel(getString(R.string.discover_str_3), getString(R.string.toast_message_5),
                    MessageType.NEGATIVE, true)
                CommonUtils.showToast(this, messageModel)
            }

    }

    override fun onBackPressed() {
        if (intent.getBooleanExtra("fromSplash", false)){
            startActivity(Intent(this, MainActivity::class.java))
        }
        super.onBackPressed()
    }

    fun SplitPhonepeUrl(url: String): Uri {

        if(url.contains("autopay=true")) {
            val split = url.split("&package=com.phonepe.app").toTypedArray()
            return split[0].toUri()
        }else return url.toUri()

    }
}