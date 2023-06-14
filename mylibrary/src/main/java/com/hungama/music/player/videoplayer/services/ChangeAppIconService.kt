//package com.hungama.music.player.videoplayer.services
//
//import android.app.Service
//import android.content.ComponentName
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.os.IBinder
//import android.util.Log
//import com.hungama.music.utils.CommonUtils
//import com.hungama.music.utils.CommonUtils.setLog
//import com.hungama.music.utils.preference.SharedPrefHelper
//import com.hungama.music.BuildConfig
//
//class ChangeAppIconService : Service() {
//    private val aliases = arrayOf(".FreeAlias", ".GoldAlias")
//    private var aliasName = aliases.get(0)
//
//    override fun onBind(intent: Intent?): IBinder? = null
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        setLog("ChangeAppIconService", "Service Started")
//        return START_NOT_STICKY
//    }
//    override fun onTaskRemoved(rootIntent: Intent?) {
//        setLog("ChangeAppIconService", "onTaskRemoved: called")
//        changeAppIcon()
//        stopSelf()
//    }
//
//    fun changeAppIcon() {
//        if (CommonUtils.isUserHasGoldSubscription()) {
//            aliasName = aliases.get(1)
//        } else {
//            aliasName = aliases.get(0)
//        }
//        SharedPrefHelper.getInstance().get("activeActivityAlias", aliasName).let { aliasName ->
//            if (!isAliasEnabled(aliasName!!)) {
//                setAliasEnabled(aliasName)
//            }
//        }
//
//        setLog("ChangeAppIconService", "onTaskRemoved: changeAppIcon called aliasName:${aliasName}")
//    }
//
//    private fun isAliasEnabled(aliasName: String): Boolean {
//        return packageManager.getComponentEnabledSetting(
//            ComponentName(
//                this,
//                "${BuildConfig.APPLICATION_ID}$aliasName"
//            )
//        ) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
//    }
//
//    private fun setAliasEnabled(aliasName: String) {
//        if (aliasName?.equals(aliases.get(1))) {
//            packageManager?.setComponentEnabledSetting(
//                ComponentName(
//                    applicationContext.packageName,
//                    applicationContext.packageName + ".FreeAlias"
//                ),
//                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
//            )
//
//            packageManager?.setComponentEnabledSetting(
//                ComponentName(
//                    applicationContext.packageName,
//                    applicationContext.packageName + ".GoldAlias"
//                ),
//                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
//            )
//        } else {
//            packageManager?.setComponentEnabledSetting(
//                ComponentName(
//                    applicationContext.packageName,
//                    applicationContext.packageName + ".FreeAlias"
//                ),
//                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
//            )
//
//            packageManager?.setComponentEnabledSetting(
//                ComponentName(
//                    applicationContext.packageName,
//                    applicationContext.packageName + ".GoldAlias"
//                ),
//                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
//            )
//        }
//
//        setLog("ChangeAppIconService", "onTaskRemoved: setAliasEnabled called aliasName:${aliasName}")
//
//    }
//}