package com.hungama.music.ui.main.view.activity

import android.graphics.Bitmap
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.hungama.music.R
import com.hungama.music.data.model.MessageModel
import com.hungama.music.data.model.MessageType
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant
import kotlinx.android.synthetic.main.activity_payment_web_view.*
import kotlinx.android.synthetic.main.header_back.*

class CommonWebViewActivity : AppCompatActivity() {
    private val TAG = CommonWebViewActivity::class.java.name
    private var webSettings: WebSettings? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_web_view)
        val url = intent.getStringExtra(Constant.EXTRA_URL)
        if (!TextUtils.isEmpty(url)){
            loadUrl(url)

        }else{
            val messageModel = MessageModel(getString(R.string.discover_str_2), MessageType.NEUTRAL, true)
            CommonUtils.showToast(this, messageModel)
        }
    }


    /**
     * Loads given url in web view
     *
     */
    private fun loadUrl(url: String?) {
        if (!TextUtils.isEmpty(url)) {
            webView.loadUrl(url!!)
            setLog("url", url!!)
        }else{
            val messageModel = MessageModel(getString(R.string.discover_str_2), MessageType.NEUTRAL, true)
            CommonUtils.showToast(this, messageModel)
        }
        webSettings = webView?.settings
        webView?.settings?.javaScriptEnabled = true
        webView?.settings?.pluginState = WebSettings.PluginState.ON

        // Set web view client
        webView?.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {

                setLog(TAG, "URL:" + request?.url.toString())

                view?.loadUrl(request?.url.toString())

                return true

            }

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {

            }

            override fun onPageFinished(view: WebView, url: String) {
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val hashMap = HashMap<String,String>()
    }
}