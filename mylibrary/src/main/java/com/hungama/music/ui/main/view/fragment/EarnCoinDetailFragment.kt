package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.hungama.music.data.model.EarnCoinModel
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.Utils
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import kotlinx.android.synthetic.main.common_header_action_bar.*
import kotlinx.android.synthetic.main.fr_coin_earn.*
import kotlinx.android.synthetic.main.fr_coin_earn.shimmerLayout
import kotlinx.android.synthetic.main.fragment_chart_detail_v2.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EarnCoinDetailFragment : BaseFragment() {
    var earnCoinList = mutableListOf<EarnCoinModel>()
    var loadURL="http://34.117.248.225?alang=${SharedPrefHelper.getInstance().getLanguage()}"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fr_coin_earn, container, false)
    }

    override fun initializeComponent(view: View) {

        shimmerLayout?.visibility = View.VISIBLE
        shimmerLayout?.startShimmer()

        ivBack?.setOnClickListener{ view -> backPress() }
        tvActionBarHeading.text = getString(R.string.profile_str_36)
        CommonUtils.PageViewEvent("",
            "",
            "","",
            "UserProfile" + "_" + getString(R.string.profile_str_36),
            getString(R.string.profile_str_36),
            "")

//        loadUrl("https://storage.googleapis.com/media-content-hungama-com/default-image-icons/Earn-coins_mobile_PWA.jpg")
        loadUrl(loadURL)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return true
    }


    /**
     * Loads given url in web view
     *
     */
    private fun loadUrl(url: String?) {
        webView?.settings?.javaScriptEnabled = true
        webView?.settings?.javaScriptCanOpenWindowsAutomatically = true
        webView?.settings?.allowContentAccess = true
        webView?.settings?.domStorageEnabled = true
        webView?.settings?.allowFileAccess = true
        webView?.settings?.setSupportZoom(true)
        webView?.settings?.loadsImagesAutomatically = true
        webView?.isClickable = true
        webView?.clearHistory()
        webView?.clearCache(true)


        webView?.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                webView?.invalidate()



                shimmerLayout?.stopShimmer()
                shimmerLayout?.visibility = View.GONE
                webView?.visibility=View.VISIBLE

            }

        }

//        webView?.loadUrl("file:///android_asset/earn_coins_page_app.html")
        webView?.loadUrl(url!!)

        setPageBottomMarginSpacing(
            webView,
            requireContext(),
            resources.getDimensionPixelSize(R.dimen.dimen_0),
            resources.getDimensionPixelSize(R.dimen.dimen_0),
            resources.getDimensionPixelSize(R.dimen.dimen_0),
            resources.getDimensionPixelSize(R.dimen.dimen_0)
        )
    }


    fun setPageBottomMarginSpacing(view: View?, context: Context,
                             startPadding: Int, topPadding: Int, endPadding: Int, bottomPadding: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val mStartPadding = startPadding
                val mTopPadding = topPadding
                CommonUtils.setLog("setPageBottomSpacing", "mTopPadding: $mTopPadding")
                val mEndPadding = endPadding
                var mBottomPadding = bottomPadding
                val miniplayerHeight = (context as MainActivity).miniplayerHeight
                val bottomNavigationHeight = (context as MainActivity).bottomNavigationHeight
                mBottomPadding += miniplayerHeight + bottomNavigationHeight
                CommonUtils.setLog("setPageBottomSpacing", "miniplayerHeight: $miniplayerHeight")
                CommonUtils.setLog(
                    "setPageBottomSpacing",
                    "bottomNavigationHeight: $bottomNavigationHeight"
                )
                CommonUtils.setLog(
                    "setPageBottomSpacing",
                    "isBottomStickyAdLoaded: ${(context as MainActivity).isBottomStickyAdLoaded}"
                )
                if (CommonUtils.isHomeScreenBannerAds() && (context as MainActivity).isBottomNavigationVisible && (context as MainActivity).isBottomStickyAdLoaded){
                    mBottomPadding += context.resources.getDimensionPixelSize(R.dimen.dimen_70)
                    CommonUtils.setLog("setPageBottomSpacing", "stickyAdHeight: 70")
                }
                val extraSpace = context.resources.getDimensionPixelSize(R.dimen.dimen_10)
                mBottomPadding += extraSpace
                CommonUtils.setLog("setPageBottomSpacing", "totalBottomSpace: $mBottomPadding")
//                view?.set(mStartPadding, mTopPadding, mEndPadding, mBottomPadding)
                Utils.setMarginsBottom(webView!!,mBottomPadding)
                view?.requestLayout()
            }catch (e:Exception){

            }
        }
    }

}