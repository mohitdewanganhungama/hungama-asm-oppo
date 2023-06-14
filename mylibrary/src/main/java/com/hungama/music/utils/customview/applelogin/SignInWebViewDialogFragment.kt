//package com.hungama.music.utils.customview.applelogin
//
//import android.annotation.SuppressLint
//import android.content.DialogInterface
//import android.net.Uri
//import android.os.Bundle
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.view.ViewGroup.LayoutParams.MATCH_PARENT
//import android.webkit.WebResourceRequest
//import android.webkit.WebView
//import android.webkit.WebViewClient
//import androidx.fragment.app.DialogFragment
//import com.hungama.music.utils.CommonUtils.setLog
//import com.hungama.music.utils.customview.applelogin.SignInWithAppleButton.Companion.SIGN_IN_WITH_APPLE_LOG_TAG
//
//
//@SuppressLint("SetJavaScriptEnabled")
//internal class SignInWebViewDialogFragment : DialogFragment() {
//
//    companion object {
//        private const val AUTHENTICATION_ATTEMPT_KEY = "authenticationAttempt"
//        private const val WEB_VIEW_KEY = "webView"
//
//        fun newInstance(authenticationAttempt: SignInWithAppleService.AuthenticationAttempt): SignInWebViewDialogFragment {
//            val fragment = SignInWebViewDialogFragment()
//            fragment.arguments = Bundle().apply {
//                putParcelable(AUTHENTICATION_ATTEMPT_KEY, authenticationAttempt)
//            }
//            return fragment
//        }
//    }
//
////    private lateinit var authenticationAttempt: SignInWithAppleService.AuthenticationAttempt
//    private var callback: ((SignInWithAppleResult) -> Unit)? = null
//
//    private val webViewIfCreated: WebView?
//        get() = view as? WebView
//
//    fun configure(callback: (SignInWithAppleResult) -> Unit) {
//        this.callback = callback
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        authenticationAttempt = arguments?.getParcelable(AUTHENTICATION_ATTEMPT_KEY)!!
//        //setStyle(STYLE_NORMAL, R.style.sign_in_with_apple_button_DialogTheme)
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        super.onCreateView(inflater, container, savedInstanceState)
//
//        val webView = WebView(requireContext()).apply {
//            settings.apply {
//                javaScriptEnabled = true
//                allowFileAccessFromFileURLs = true
//                javaScriptCanOpenWindowsAutomatically = true
//                allowContentAccess = true
//                domStorageEnabled = true
//                allowFileAccess = true
//                loadsImagesAutomatically = true
//            }
//        }
//
//        webView.clearHistory()
//        webView.clearCache(true)
//
//        webView.webViewClient = SignInWebViewClient(authenticationAttempt, ::onCallback)
//
//
//        webView.isVerticalScrollBarEnabled = false
//        webView.isHorizontalScrollBarEnabled = false
//        if (savedInstanceState != null) {
//            savedInstanceState.getBundle(WEB_VIEW_KEY)?.run {
//                webView.restoreState(this)
//            }
//        } else {
//            webView.loadUrl("https://appleid.apple.com/auth/authorize?client_id=com.hungama.myplay.staging.signin&redirect_uri=https%3A%2F%2Fuser.api.hungama.com%2Fv1%2Fapple-callback&response_type=code%20id_token&state=123456&scope=email&response_mode=form_post&frame_id=a3a2fc4d-8624-49d2-b74c-a9ba303d9c5f&m=12&v=1.5.4")
////            webView.loadUrl(authenticationAttempt.authenticationUri)
//        }
//
//
//        return webView
//    }
//
//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        outState.putBundle(
//            WEB_VIEW_KEY,
//            Bundle().apply {
//                webViewIfCreated?.saveState(this)
//            }
//        )
//    }
//
//    override fun onStart() {
//        super.onStart()
//
//        dialog?.window?.setLayout(MATCH_PARENT, MATCH_PARENT)
//    }
//
//    override fun onCancel(dialog: DialogInterface) {
//        super.onCancel(dialog)
//        onCallback(SignInWithAppleResult.Cancel)
//    }
//
//    // SignInWithAppleCallback
//
//    private fun onCallback(result: SignInWithAppleResult) {
//        dialog?.dismiss()
//        val callback = callback
//        if (callback == null) {
//            setLog(SIGN_IN_WITH_APPLE_LOG_TAG, "Callback is not configured")
//            return
//        }
//        callback(result)
//    }
//}