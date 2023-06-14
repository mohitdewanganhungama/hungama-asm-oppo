package com.hungama.music.eventanalytic.util.callbacks.inapp

import android.content.Context
import com.moengage.core.listeners.AppBackgroundListener

/**
 * @author Umang Chamaria
 * Date: 2019-05-31
 */
class ApplicationBackgroundListener : AppBackgroundListener {

    override fun onAppBackground(context: Context) {
        //Timber.v(" goingToBackground(): Application going to background callback received.")
        // application going to background, add your logic here.
    }
}