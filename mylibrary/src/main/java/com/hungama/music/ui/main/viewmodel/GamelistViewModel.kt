package com.hungama.music.ui.main.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.text.TextUtils
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hungama.music.HungamaMusicApp
import com.hungama.music.data.model.*
import com.hungama.music.data.webservice.repositories.GameDetailRepos
import com.hungama.music.data.webservice.utils.Resource
import com.hungama.music.utils.CommonUtils
import com.hungama.music.R
import kotlinx.android.synthetic.main.activity_toolbar_web_view.*

class GamelistViewModel : ViewModel() {

/*    var FLAG= MutableLiveData<String>()
     var orderId = MutableLiveData<String>()
    var MODE = MutableLiveData<String>()
     var defaultContentId= MutableLiveData<String>()
    var EXTRA_PAGE_DETAIL_NAME= MutableLiveData<String>()
    var EXTRA_URL= MutableLiveData<String>()  */

    private var gamelistRepos: GameDetailRepos? = null

    fun getGamelistDetailList(
        context: Context,
        id: String
    ): MutableLiveData<Resource<GamelistModel>>? {
        gamelistRepos = GameDetailRepos()

        return gamelistRepos?.getgameDetail(context, id)
    }

     fun loadUrl(url: String?,webView: WebView):WebView {
        if (!TextUtils.isEmpty(url)) {
            webView.loadUrl(url!!)
            CommonUtils.setLog("url", url)
        } else {
            val messageModel =
                MessageModel(HungamaMusicApp.hungamaMusicApp?.applicationContext!!.getString(R.string.discover_str_2), MessageType.NEUTRAL, true)
            CommonUtils.showToast(HungamaMusicApp.hungamaMusicApp!!.applicationContext, messageModel)
        }
         return webView
    }



}

