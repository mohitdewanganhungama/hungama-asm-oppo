package com.hungama.music.ui.base

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.exoplayer.SimpleExoPlayer
import com.hungama.music.player.audioplayer.queue.NowPlayingQueue
import com.hungama.music.player.audioplayer.services.AudioPlayerService
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("Registered")
open class BaseServiceBoundedActivity : AppCompatActivity() {
    private val TAG = AudioPlayerService::class.java.name
    protected var boundToService: Boolean = false
    private var mService:AudioPlayerService? = null

    override fun onStart() {
        super.onStart()
        boundToService = bindService(
            Intent(this@BaseServiceBoundedActivity, AudioPlayerService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )

    }

    fun reBindService(){
        /*CommonUtils.setLog("BaseActivityLifecycleMethods-1", "reBindService")
        boundToService = bindService(
            Intent(this, AudioPlayerService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )*/
    }

    fun unBindService(){
        CommonUtils.setLog("BaseActivityLifecycleMethods-1", "unBindService")
        if (boundToService) {
            if (mService != null){
                mService?.onUnbindService()
                unbindService(serviceConnection)
                boundToService = false
            }
        }
        /*if (boundToService) {
            setLog("Service Unbinded", "true 2")
            //stopService(intent)
            unbindService(serviceConnection)
            boundToService = false
        }*/
    }
    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            setLog(TAG, "onServiceDisconnected")
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            setLog(TAG, "onServiceConnected")
            if (service is AudioPlayerService.AudioServiceBinder) {
                val binder = service as AudioPlayerService.AudioServiceBinder
                mService = binder.service
                CoroutineScope(Dispatchers.Main).launch{
                    onConnectedToService(service.getPlayerInstance(), service.getNowPlayingQueue(), service)
                }
            }
        }
    }

    open fun onConnectedToService(
        audioPlayerInstance: SimpleExoPlayer,
        nowPlayingQueueInstance: NowPlayingQueue,
        service: AudioPlayerService.AudioServiceBinder
    ) {
        // client can override to take any action on service bind
    }

    override fun onStop() {
        super.onStop()
        CommonUtils.setLog("BaseActivityLifecycleMethods-1", "onStop")
        setLog("Service Unbinded", "true 1")
        if (boundToService) {
            setLog("Service Unbinded", "true 2")
            unbindService(serviceConnection)
            boundToService = false
        }
    }
}