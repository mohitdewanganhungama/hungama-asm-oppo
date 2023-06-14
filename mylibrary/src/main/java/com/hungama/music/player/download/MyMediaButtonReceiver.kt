package com.hungama.music.player.download

import android.content.Context
import android.content.Intent
import androidx.media.session.MediaButtonReceiver
import com.hungama.music.utils.CommonUtils.setLog
import java.lang.IllegalStateException

class MyMediaButtonReceiver : MediaButtonReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        try {
            super.onReceive(context, intent)
        } catch (e: IllegalStateException) {
            setLog(this.javaClass.name, e.message!!)
        }
    }
}