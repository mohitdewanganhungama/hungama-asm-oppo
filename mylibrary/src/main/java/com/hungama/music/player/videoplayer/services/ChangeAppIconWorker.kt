package com.hungama.music.player.videoplayer.services

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import androidx.work.*
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.BuildConfig


class ChangeAppIconWorker(val context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {
    companion object {
        private val aliases = arrayOf(".FreeAlias", ".GoldAlias")
        private var aliasName = aliases.get(0)
        fun enqueue(context: Context) {
            val workRequestBuilder = OneTimeWorkRequestBuilder<ChangeAppIconWorker>()
            WorkManager.getInstance(context).enqueueUniqueWork(
                ChangeAppIconWorker::class.java.simpleName,
                ExistingWorkPolicy.REPLACE, workRequestBuilder.build()
            )
            setLog("ChangeAppIconWorker","enqueue called")
        }
    }

    override suspend fun doWork(): Result {
        return try {
            changeAppIcon()
            setLog("ChangeAppIconWorker","Success")
            return Result.success()
        } catch (e: Exception) {
            setLog("ChangeAppIconWorker","ExecutionException")
            return Result.retry()
        }
    }

    fun changeAppIcon() {
        if (CommonUtils.isUserHasGoldSubscription()) {
            aliasName = aliases[1]
        } else {
            aliasName = aliases[0]
        }
        SharedPrefHelper.getInstance()["activeActivityAlias", aliasName].let { aliasName ->
           /* if (!isAliasEnabled(aliasName)) {
                setAliasEnabled(aliasName)
            }*/
        }

        setLog("ChangeAppIconWorker", "onTaskRemoved: changeAppIcon called aliasName:${aliasName}")
    }

/*    private fun isAliasEnabled(aliasName: String): Boolean {
        return context?.packageManager?.getComponentEnabledSetting(ComponentName(context, "${BuildConfig.APPLICATION_ID}$aliasName")) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
    }*/

    private fun setAliasEnabled(aliasName: String) {
        if (aliasName.equals(aliases.get(1))) {
            context?.packageManager?.setComponentEnabledSetting(
                ComponentName(
                    applicationContext.packageName,
                    applicationContext.packageName + ".FreeAlias"
                ),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
            )

            context?.packageManager?.setComponentEnabledSetting(
                ComponentName(
                    applicationContext.packageName,
                    applicationContext.packageName + ".GoldAlias"
                ),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
            )
        } else {
            context?.packageManager?.setComponentEnabledSetting(
                ComponentName(
                    applicationContext.packageName,
                    applicationContext.packageName + ".FreeAlias"
                ),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
            )

            context?.packageManager?.setComponentEnabledSetting(
                ComponentName(
                    applicationContext.packageName,
                    applicationContext.packageName + ".GoldAlias"
                ),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
            )
        }

        setLog("ChangeAppIconWorker", "onTaskRemoved: setAliasEnabled called aliasName:${aliasName}")

    }
}