//package com.hungama.music.utils.customview.applelogin
//
//import android.net.Uri
//import android.net.http.SslError
//import android.os.Build
//import android.text.TextUtils
//import android.util.Log
//import android.webkit.SslErrorHandler
//import android.webkit.WebResourceRequest
//import android.webkit.WebView
//import android.webkit.WebViewClient
//import androidx.annotation.RequiresApi
//import com.hungama.music.utils.CommonUtils.setLog
//import com.hungama.music.utils.customview.applelogin.SignInWithAppleButton.Companion.SIGN_IN_WITH_APPLE_LOG_TAG
//
//class SignInWebViewClient(
//    private val attempt: SignInWithAppleService.AuthenticationAttempt,
//    private val callback: (SignInWithAppleResult) -> Unit
//) : WebViewClient() {
//
//    override fun onLoadResource(view: WebView?, url: String?) {
//        super.onLoadResource(view, url)
//        setLog("AppleLogin", "onLoadResource: url:${url}")
//    }
//
//    // for API levels < 24
//    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
//        setLog("AppleLogin","shouldOverrideUrlLoading:${url}")
//        return isUrlOverridden(view, Uri.parse(url))
//    }
//
//    @RequiresApi(Build.VERSION_CODES.N)
//    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
//        setLog("AppleLogin","shouldOverrideUrlLoading:${request?.url}")
//        return isUrlOverridden(view, request?.url)
//    }
//
//    private fun isUrlOverridden(view: WebView?, url: Uri?): Boolean {
//        setLog("AppleLogin","isUrlOverridden:${url}")
//        return when {
//            url.toString().contains("appleid.apple.com") -> {
//                view?.loadUrl(url.toString())
//                true
//            }
//            url.toString().contains("hungama.com") -> {
//                setLog(SIGN_IN_WITH_APPLE_LOG_TAG, "Web view was forwarded to redirect URI")
//
//                val email = url?.getQueryParameter("email")
//                val provider = url?.getQueryParameter("provider")
//
//                if(TextUtils.isEmpty(email)){
//                    callback(SignInWithAppleResult.Failure(IllegalArgumentException("code not returned")))
//                }else{
//                    callback(SignInWithAppleResult.Success(email!!))
//                }
//
//
//                true
//            }
//            else -> {
//                false
//            }
//        }
//    }
//
//}