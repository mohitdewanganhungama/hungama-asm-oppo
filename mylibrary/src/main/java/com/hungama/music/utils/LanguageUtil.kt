package com.hungama.music.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import com.hungama.music.utils.preference.PrefConstant
import com.hungama.music.utils.preference.SharedPrefHelper
import java.util.*

/**
 * Multi lang support utility
 */
object LanguageUtil {
    fun getLocal(context: Context): Context {

        val lang = SharedPrefHelper.getInstance().getLanguage()

        return updateResources(context, lang!!)!!
    }

    fun getCurrentLanguage(): String {
        return SharedPrefHelper.getInstance().get(PrefConstant.PREFERENCE_LANGUAGE, Constant.EN)
    }

    /**
     * update resource
     */
    fun updateResources(context: Context, language: String): Context? {
        var context = context
        val locale = Locale(language)
        Locale.setDefault(locale)
        val res: Resources = context.resources
        val config = Configuration(res.getConfiguration())
        if (Build.VERSION.SDK_INT >= 21) {
            config.setLocale(locale)
            context = context.createConfigurationContext(config)
        } else {
            config.locale = locale
            res.updateConfiguration(config, res.getDisplayMetrics())
        }
        return context
    }

}