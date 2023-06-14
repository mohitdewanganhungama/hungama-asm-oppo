package com.hungama.music.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.appsflyer.AFInAppEventParameterName
import com.appsflyer.AFInAppEventType
import com.appsflyer.AppsFlyerLib
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.hungama.music.BuildConfig
import com.hungama.music.utils.preference.PrefConstant
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.HungamaMusicApp
import com.hungama.music.R
import com.hungama.music.data.model.MessageModel
import com.hungama.music.data.model.MessageType
import com.hungama.music.data.model.PlanNames
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.RegistrationSuccessEvent
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.utils.CommonUtils.getUserSubscriptionPlan
import com.hungama.music.utils.CommonUtils.isUserHasRentedSubscription
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant.SUBSCRIPTION_TYPE_GOLD
import com.hungama.music.utils.Constant.SUBSCRIPTION_TYPE_GOLD_WITH_ADS
import kotlinx.coroutines.*
import java.io.File
import java.security.MessageDigest
import java.util.*
import kotlin.random.Random


class Utils {
    companion object {

        fun getDeviceName(): String? {
            val manufacturer: String = Build.MANUFACTURER
            val model: String = Build.MODEL
            return if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
                capitalize(model)
            } else {
                capitalize(manufacturer) + " " + model
            }
        }


        private fun capitalize(s: String?): String {
            if (s == null || s.length == 0) {
                return ""
            }
            val first = s[0]
            return if (Character.isUpperCase(first)) {
                s
            } else {
                Character.toUpperCase(first).toString() + s.substring(1)
            }
        }

        /**
         * Method that prints hash key.
         */
//        fun printHashKey(context: Context) {
//            // Add code to print out the key hash
//
//            // Add code to print out the key hash
//            try {
//                val info: PackageInfo = context.packageManager.getPackageInfo(
//                    BuildConfig.APPLICATION_ID,
//                    PackageManager.GET_SIGNATURES
//                )
//                for (signature in info.signatures) {
//                    val md: MessageDigest = MessageDigest.getInstance("SHA")
//                    md.update(signature.toByteArray())
//                    setLog("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }


        fun showSnakbar(context: Context,view: View, isError: Boolean, message: String) {
            val messageModel = MessageModel(message,
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(context, messageModel)
//            try {
//                var tmpMessage: String = message
//                if (TextUtils.isEmpty(message)) {
////                    tmpMessage = HungamaMusicApp.getInstance().getString(R.string.discover_str_2)
//                    tmpMessage = message
//                }
//                val snackbar = Snackbar.make(view, tmpMessage, Snackbar.LENGTH_LONG)
//                val snackBarView = snackbar.view
//                val textView =
//                    snackBarView.findViewById<TextView>(R.id.snackbar_text)
//                if (isError) {
//                    snackBarView.setBackgroundColor(
//                        ContextCompat.getColor(
//                            HungamaMusicApp.getInstance(),
//                            R.color.colorDarkRed
//                        )
//                    )
//                    textView.setTextColor(
//                        ContextCompat.getColor(
//                            HungamaMusicApp.getInstance(),
//                            R.color.colorWhite
//                        )
//                    )
//                    textView.textSize =
//                        HungamaMusicApp.getInstance().resources.getDimension(R.dimen._5ssp)
//                } else {
//                    snackBarView.setBackgroundColor(
//                        ContextCompat.getColor(
//                            HungamaMusicApp.getInstance(),
//                            R.color.colorPrimary
//                        )
//                    )
//                    textView.setTextColor(
//                        ContextCompat.getColor(
//                            HungamaMusicApp.getInstance(),
//                            R.color.colorWhite
//                        )
//                    )
//                    textView.textSize =
//                        HungamaMusicApp.getInstance().resources.getDimension(R.dimen._5ssp)
//                }
//                textView.maxLines = 5
//                textView.typeface = ResourcesCompat.getFont(
//                    HungamaMusicApp.getInstance(),
//                    R.font.sf_pro_text
//                )
//                snackbar.show()
//            } catch (e: Exception) {
//
//            }
//
//        }
        }
        /**
         * Validates the Email Id
         *
         * @param emailId email id to be verified
         * @return true valid email id, false invalid emailid
         */
        fun isValidEmailId(emailId: String): Boolean {
            return !TextUtils.isEmpty(emailId) && Patterns.EMAIL_ADDRESS.matcher(emailId).matches()
        }

        /**
         * Validates the Email Id
         *
         * @param emailId email id to be verified
         * @return true valid email id, false invalid emailid
         */
        fun isValidEmailIdChar(emailId: CharSequence): Boolean {
            return !TextUtils.isEmpty(emailId) && Patterns.EMAIL_ADDRESS.matcher(emailId).matches()
        }

        /**
         * Hide the soft keyboard from screen for edit text only
         *
         * @param context context of current visible activity
         * @param view    clicked view
         */
        fun hideSoftKeyBoard(context: Context, view: View) {
            try {
                val imm =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            } catch (e: Exception) {
                // TODO: handle exception
                e.printStackTrace()
            }

        }

        /**
         * Show the soft keyboard from screen for edit text only
         *
         * @param context context of current visible activity
         * @param view    clicked view
         */
        fun showSoftKeyBoard(context: Context, view: View) {
            try {
                val imm =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
            } catch (e: Exception) {
                // TODO: handle exception
                e.printStackTrace()
            }

        }

        fun getLastVisiblePosition(rv: RecyclerView?): Int {
            if (rv != null) {
                val layoutManager: RecyclerView.LayoutManager = rv?.getLayoutManager()!!
                if (layoutManager is GridLayoutManager) {
                    return (layoutManager as GridLayoutManager)
                        .findLastVisibleItemPosition()
                }
            }
            return 0
        }

        fun convertArrayToString(arrayList: List<String>?): String {
            var rtnStr=""
            if (arrayList != null && arrayList.size > 0) {
                rtnStr= "" + TextUtils.join(",", arrayList)
            }
            setLog("convertArrayToString","convertArrayToString rtnStr:${rtnStr}")
            setLog("convertArrayToString","convertArrayToString arrayList to string:${arrayList?.toString()}")
            return rtnStr
        }

        fun arrayToString(arrayList: List<String>?): String {
            var rtnStr=""
            if (arrayList != null && arrayList.size > 0) {
                rtnStr= arrayList.toString()
            }
            setLog("arrayToString","arrayToString arrayList to string:${rtnStr}")
            return rtnStr!!
        }

        fun getContentTypeName(contentTypeId: String): String {
            if (contentTypeId.equals("20", true)) {
                return "Live Concert"
            } else if (contentTypeId.equals("21", true)) {
                return "Audio"
            } else if (contentTypeId.equals("22", true) || contentTypeId.equals(
                    "51",
                    true
                )  || contentTypeId.equals("88888", true)
            ) {
                return "Music Video"
            } else if (contentTypeId.equals("4", true) || contentTypeId.equals(
                    "65",
                    true
                ) || contentTypeId.equals("66", true)
            ) {
                return "Movies"
            } else if (contentTypeId.equals("93", true)) {
                return "Short Films"
            } else if (contentTypeId.equals("96", true) || contentTypeId.equals(
                    "97",
                    true
                ) || contentTypeId.equals("98", true) || contentTypeId.equals(
                    "102",
                    true
                ) || contentTypeId.equals("107", true)
            ) {
                return "TV Show"
            } else if (contentTypeId.equals("109", true)) {
                return "Podcast"
            } else if (contentTypeId.equals("110", true)) {
                return "Podcast Episode"
            } else if (contentTypeId.equals("77777", true) || contentTypeId.equals(
                    "33",
                    true
                ) || contentTypeId.equals("34", true) || contentTypeId.equals(
                    "35",
                    true
                ) || contentTypeId.equals("36", true)
            ) {
                return "Radio"
            } else if (contentTypeId.equals("55555", true)) {
                return "Playlist"
            } else if (contentTypeId.equals("19", true)) {
                return "Chart"
            } else if (contentTypeId.equals("53", true)) {
                return "short_video"
            }else if (contentTypeId.equals("60", true)) {
                return "Games"
            }else {
                return getContentTypeDetailName(contentTypeId)
            }
        }

        fun getContentTypeNameForStream(contentTypeId: String): String {
            if (contentTypeId.equals("20", true)) {
                return "live_concert"
            } else if (contentTypeId.equals("21", true)) {
                return "song"
            } else if (contentTypeId.equals("22", true) || contentTypeId.equals(
                    "51",
                    true
                ) || contentTypeId.equals("88888", true)
            ) {
                return "music_video"
            } else if (contentTypeId.equals("4", true) || contentTypeId.equals(
                    "65",
                    true
                ) || contentTypeId.equals("66", true)
            ) {
                return "movie"
            } else if (contentTypeId.equals("93", true)) {
                return "short_film"
            } else if (contentTypeId.equals("53", true)) {
                return "short_video"
            } else if (contentTypeId.equals("96", true) || contentTypeId.equals(
                    "97",
                    true
                ) || contentTypeId.equals("98", true) || contentTypeId.equals(
                    "102",
                    true
                ) || contentTypeId.equals("107", true)
            ) {
                return "tv_show_episode"
            } else if (contentTypeId.equals("109", true) || contentTypeId.equals("110", true)) {
                return "podcast_episode"
            } else if (contentTypeId.equals("77777", true)
            ) {
                return "radio"
            } else if (contentTypeId.equals("36", true)) {
                return "artist_radio"
            } else if (contentTypeId.equals("35", true)) {
                return "on_demand_radio"
            } else if (contentTypeId.equals("34", true)) {
                return "live_radio"
            } else if (contentTypeId.equals("33", true)) {
                return "mood_radio"
            } else {
                return getContentTypeDetailName(contentTypeId)
            }
        }

        fun getContentTypeDetailName(contentTypeId: String): String {
            if (contentTypeId.equals("0", true)) {
                return "Artist"
            } else if (contentTypeId.equals("1", true)) {
                return "Audio Album"
            } else if (contentTypeId.equals("2", true)) {
                return "Music Video Album"
            } else if (contentTypeId.equals("3", true)) {
                return "Image Album"
            } else if (contentTypeId.equals("4", true) || contentTypeId.equals(
                    "65",
                    true
                ) || contentTypeId.equals("66", true)
            ) {
                return "Movie"
            } else if (contentTypeId.equals("93", true)) {
                return "Short Films"
            } else if (contentTypeId.equals("15", true)) {
                return "Collection"
            } else if (contentTypeId.equals("19", true)) {
                return "Chart"
            } else if (contentTypeId.equals("20", true)) {
                return "Live event"
            } else if (contentTypeId.equals("21", true)) {
                return "Music"
            } else if (contentTypeId.equals("22", true)  || contentTypeId.equals("88888", true)
            ) {
                return "Music Video"
            } else if (contentTypeId.equals("25", true)) {
                return "Category Video"
            } else if (contentTypeId.equals("51", true)) {
                return "Movie Videos"
            } else if (contentTypeId.equals("52", true)) {
                return "Events and Broadcasts Album"
            }else if (contentTypeId.equals("60", true)) {
                return "Games Detail"
            } else if (contentTypeId.equals("96", true) || contentTypeId.equals(
                    "97",
                    true
                ) || contentTypeId.equals("98", true) || contentTypeId.equals(
                    "102",
                    true
                ) || contentTypeId.equals("107", true)
            ) {
                return "TV Show"
            } else if (contentTypeId.equals("109", true)) {
                return "Podcast"
            } else if (contentTypeId.equals("110", true)) {
                return "Podcast Episode"
            } else if (contentTypeId.equals("55555", true)) {
                return "Playlist"
            } else {
                return ""
            }
        }

        fun shareItem(context: Context, mURL: String) {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, mURL)
                // (Optional) Here we're setting the title of the content
                putExtra(Intent.EXTRA_TITLE, context?.getString(R.string.music_player_str_18))
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                type = "text/plain"
            }

            setLog("TAG", "shareItem: mURL:$mURL")
            val shareIntent =
                Intent.createChooser(sendIntent, context?.getString(R.string.music_player_str_19))
            context.startActivity(shareIntent)
        }

        fun randomImageNumber(): Int {
            return Random.nextInt(1000, 1200)
        }

        fun getDeviceId(context: Context): String {
            var deviceID = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )
            setLog("getDeviceId", "getDeviceId:=>" + deviceID)
            return deviceID

        }

        fun setTextViewText(textView: TextView, value: String) {
            if (!value.isNullOrEmpty() && !value.equals("null", true)) {
                textView.text = value
            }
        }

        fun setEditText(edittextView: EditText, value: String) {
            if (!value.isNullOrEmpty() && !value.equals("null", true)) {
                edittextView.setText(value)
            }
        }

        fun setButtonText(button: Button, value: String) {
            if (!value.isNullOrEmpty() && !value.equals("null", true)) {
                button.text = value
            }
        }

        /**
         * To find out the extension of required object in given uri
         * Solution by http://stackoverflow.com/a/36514823/1171484
         */
        @JvmStatic
        fun getMimeType(context: Context, uri: Uri): String? {
            val extension: String?

            //Check uri format to avoid null
            if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
                //If scheme is a content
                val mime = MimeTypeMap.getSingleton()
                extension = mime.getExtensionFromMimeType(context.contentResolver.getType(uri))
            } else {
                //If scheme is a File
                //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
                extension =
                    MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(File(uri.path)).toString())
            }

            return extension
        }

        fun getDisplayWidth(context: Context): Int {
            val displayMetrics = DisplayMetrics()
            val windowmanager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowmanager.defaultDisplay.getMetrics(displayMetrics)
            //int height = displayMetrics.heightPixels;
            return displayMetrics.widthPixels
        }

        fun dpToPx(dp: Int, context: Context): Int {
            val displayMetrics = context.resources.displayMetrics
            return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
        }

        fun setMargins(view: View, left: Int) {
            if (view.layoutParams is ViewGroup.MarginLayoutParams) {
                val p = view.layoutParams as ViewGroup.MarginLayoutParams
                p.marginStart = left
//                p.leftMargin=left
                view.requestLayout()
            }
        }

        fun setPedding(view: View, left: Int) {
            if (view.layoutParams is ViewGroup.LayoutParams) {
                val p = view.layoutParams as ViewGroup.LayoutParams
//                p.leftMargin=left
                view.requestLayout()
            }
        }

        fun setMarginsTop(view: View, top: Int) {
            if (view.layoutParams is ViewGroup.MarginLayoutParams) {
                val p = view.layoutParams as ViewGroup.MarginLayoutParams
                p.topMargin = top
                view.requestLayout()
            }
        }

        fun setMarginsEnd(view: View, end: Int) {
            if (view.layoutParams is ViewGroup.MarginLayoutParams) {
                val p = view.layoutParams as ViewGroup.MarginLayoutParams
                p.marginEnd = end
                view.requestLayout()
            }
        }

        fun setMarginsBottom(view: View, bottom: Int) {
            if (view.layoutParams is ViewGroup.MarginLayoutParams) {
                val p = view.layoutParams as ViewGroup.MarginLayoutParams
                p.bottomMargin = bottom
                view.requestLayout()
            }
        }

        fun setMovieRightTextForBucketWithPlay(
            txtRent: TextView,
            ivRent: ImageView,
            movierights: List<String?>,
            context: Context,
            contentId: String
        ) {
            var action = 0
            movierights.forEach {
                if (it?.contains(PlanNames.TVOD.name, true)!! || it?.contains(
                        PlanNames.PTVOD.name,
                        true
                    )!!
                ) {
                    if (isUserHasRentedSubscription(contentId)) {
                        txtRent.setText(context.getString(R.string.movie_str_7))
                        ivRent?.setImageDrawable(
                            context.faDrawable(
                                R.string.icon_play_2,
                                R.color.colorWhite
                            )
                        )
                        txtRent.visibility = View.VISIBLE
                        action = 3
                    } else {
                        txtRent.setText(context.getString(R.string.movie_str_5))
                        ivRent?.setImageDrawable(
                            context.faDrawable(
                                R.string.icon_rent,
                                R.color.colorWhite
                            )
                        )
                        txtRent.visibility = View.VISIBLE
                        action = 1
                    }

                    return
                } else if (it?.contains(PlanNames.SVOD.name, true)!!) {
                    val plan = getUserSubscriptionPlan()
                    if (plan == SUBSCRIPTION_TYPE_GOLD || plan == SUBSCRIPTION_TYPE_GOLD_WITH_ADS) {
                        txtRent.setText(context.getString(R.string.movie_str_7))
                        ivRent?.setImageDrawable(
                            context.faDrawable(
                                R.string.icon_play_2,
                                R.color.colorWhite
                            )
                        )
                        txtRent.visibility = View.VISIBLE
                        action = 3
                    } else {
                        txtRent.setText(context.getString(R.string.movie_str_6))
                        ivRent?.setImageDrawable(
                            context.faDrawable(
                                R.string.icon_crown,
                                R.color.colorWhite
                            )
                        )
                        txtRent.visibility = View.VISIBLE
                        action = 2
                    }
                    return
                } else if (it?.contains(
                        PlanNames.FVOD.name,
                        true
                    )!! || it?.contains(PlanNames.AVOD.name, true)!!
                ) {
                    txtRent.setText(context.getString(R.string.movie_str_7))
                    ivRent?.setImageDrawable(
                        context.faDrawable(
                            R.string.icon_play_2,
                            R.color.colorWhite
                        )
                    )
                    txtRent.visibility = View.VISIBLE
                    action = 3
                } else {
                    txtRent.visibility = View.GONE
                    action = 1
                }
            }
        }

        fun setMovieRightTextForBucket(
            ivSubscription: ImageView,
            movierights: List<String?>,
            context: Context,
            contentId: String,
            isFreeVisible:Boolean = false
        ) {
            movierights.forEach {
                if (it?.contains(PlanNames.TVOD.name, true)!! || it?.contains(
                        PlanNames.PTVOD.name,
                        true
                    )!!
                ) {
                    if (isUserHasRentedSubscription(contentId)) {
                        //txtRent.visibility=View.GONE
                        ivSubscription.visibility = View.GONE
                    } else {
                        //txtRent.setText(context.getString(R.string.movie_str_5))
                        //txtRent.visibility=View.VISIBLE
                        ivSubscription?.setImageResource(R.drawable.new_rent)
                        ivSubscription?.visibility = View.VISIBLE
                    }

                    return
                } else if (it?.contains(PlanNames.SVOD.name, true)!!) {
                    val plan = getUserSubscriptionPlan()
                    if (plan == SUBSCRIPTION_TYPE_GOLD || plan == SUBSCRIPTION_TYPE_GOLD_WITH_ADS) {
                        //txtRent.visibility=View.GONE
                        ivSubscription?.visibility = View.GONE
                    } else {
                        //txtRent.setText(context.getString(R.string.movie_str_6))
                        //txtRent.visibility=View.VISIBLE
                        ivSubscription?.setImageResource(R.drawable.ic_new_subscription)
                        ivSubscription?.visibility = View.VISIBLE
                    }
                    return
                } else if (it.contains(PlanNames.FVOD.name, true)) {
                    //txtRent.setText(context.getString(R.string.movie_str_7))
                    //txtRent.visibility=View.VISIBLE
                        if (isFreeVisible) {
                            ivSubscription.setImageResource(R.drawable.ic_new_free)
                            ivSubscription.visibility = View.VISIBLE
                            return
                        }
                    else{
                            ivSubscription.visibility = View.GONE
                            return
                        }
                } else if (it.contains(PlanNames.AVOD.name, true)) {
                    //txtRent.setText(context.getString(R.string.movie_str_7))
                    //txtRent.visibility=View.VISIBLE
//                    ivSubscription.setImageResource(R.drawable.ic_new_free)
                    ivSubscription.visibility = View.GONE
                } else {
                    //txtRent.visibility=View.GONE
                    ivSubscription.visibility = View.GONE
                }
            }
        }

        fun setMovieRightTextForDetail(
            txtRent: TextView,
            movierights: List<String?>,
            context: Context,
            contentId: String
        ): Int {
            var action = 0
            movierights.forEach {
                if (it?.contains(PlanNames.TVOD.name, true)!! || it?.contains(
                        PlanNames.PTVOD.name,
                        true
                    )!!
                ) {
                    if (isUserHasRentedSubscription(contentId)) {
                        txtRent.setText(context.getString(R.string.movie_str_7))
                        txtRent.visibility = View.VISIBLE
                        action = 3
                    } else {
                        txtRent.setText(context.getString(R.string.movie_str_5))
                        txtRent.visibility = View.VISIBLE
                        action = 1
                    }

                } else if (it?.contains(PlanNames.SVOD.name, true)!!) {
                    val plan = getUserSubscriptionPlan()
                    if (plan == SUBSCRIPTION_TYPE_GOLD || plan == SUBSCRIPTION_TYPE_GOLD_WITH_ADS) {
                        txtRent.setText(context.getString(R.string.movie_str_7))
                        txtRent.visibility = View.VISIBLE
                        action = 3
                    } else {
                        txtRent.setText(context.getString(R.string.movie_str_6))
                        txtRent.visibility = View.VISIBLE
                        action = 2
                    }
                } else if (it?.contains(PlanNames.AVOD.name, true)!!) {
                    txtRent.setText(context.getString(R.string.movie_str_7))
                    txtRent.visibility = View.VISIBLE
                    action = 3
                } else if (it?.contains(PlanNames.FVOD.name, true)!!) {
                    txtRent.setText(context.getString(R.string.movie_str_7))
                    txtRent.visibility = View.VISIBLE
                    action = 3
                    return action
                } else {
                    txtRent.visibility = View.GONE
                }
            }
            return action
        }

        /*fun getCurrentFragment(context: Context): Fragment? {
            var fragment: Fragment? = null
            try {
                if (!(context as MainActivity).supportFragmentManager.fragments.isNullOrEmpty()) {
                    fragment =
                        (context as MainActivity).supportFragmentManager.fragments[(context as MainActivity).supportFragmentManager.fragments.size - 1]
                    CommonUtils.setLog(
                        "getCurrentFragment",
                        "Utils-getCurrentFragment=${fragment?.javaClass?.simpleName}"
                    )
                }
            } catch (e: Exception) {

            }
            return fragment
        }*/

        fun getCurrentFragment(context: Context): Fragment? {
            var fragment: Fragment? = null
            try {
                if (!(context as MainActivity).supportFragmentManager.fragments.isNullOrEmpty()) {
                    val fragmentList = (context as MainActivity).supportFragmentManager.fragments
                    fragmentList.reverse()
                    for (fg in fragmentList.iterator()){
                        if (fg?.javaClass?.simpleName.equals("SupportRequestManagerFragment", true)
                            || fg?.javaClass?.simpleName.equals("SavedInstanceFragment", true)
                            || fg?.javaClass?.simpleName.equals("zzd", true)){

                        }else{
                            fragment = fg
                            CommonUtils.setLog(
                                "getCurrentFragment",
                                "Utils-getCurrentFragment=${fragment?.javaClass?.simpleName}"
                            )
                            return fragment
                        }
                    }

                }
            } catch (e: Exception) {

            }
            return fragment
        }

        fun likeAnimation(
            imageView: LottieAnimationView,
            animation: Int,
            like: Boolean,
            icFavoriteAudioCircleChecked: Int
        ): Boolean {


            if (like) {
                imageView.setImageResource(icFavoriteAudioCircleChecked)
                imageView.alpha = 1f
                imageView.setAnimation(animation)
                imageView.playAnimation()
            } else {
                imageView.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .setListener(object : AnimatorListenerAdapter() {

                        override fun onAnimationEnd(animator: Animator) {

                            imageView.setImageResource(icFavoriteAudioCircleChecked)
                            imageView.alpha = 1f
                        }

                    })

            }

            return !like
        }

        fun capitalizeString(string: String): String? {
            val chars = string.toLowerCase(Locale.US).toCharArray()
            var found = false
            for (i in chars.indices) {
                if (!found && Character.isLetter(chars[i])) {
                    chars[i] = Character.toUpperCase(chars[i])
                    found = true
                } else if (Character.isWhitespace(chars[i]) || chars[i] == '.' || chars[i] == '\'') { // You can add other chars here
                    found = false
                }
            }
            return String(chars)
        }

        fun registerUserMethod_AF(
            methodName: String
        ) {

            CoroutineScope(Dispatchers.IO).launch {
                val isRegisterEventAdded=SharedPrefHelper.getInstance().get(PrefConstant.AF_COMPLETE_REGISTRATION, true)
                setLog("Registration","registerUserMethod_AF called is register:${isRegisterEventAdded}")

                if (isRegisterEventAdded) {
                    /* Track Events in real time */
                    val eventValue: MutableMap<String, Any> = HashMap()
                    eventValue.put(AFInAppEventParameterName.REGSITRATION_METHOD, methodName)

                    AppsFlyerLib.getInstance().logEvent(
                        HungamaMusicApp.getInstance(),
                        AFInAppEventType.COMPLETE_REGISTRATION, eventValue
                    )


                    val hashMap1 = HashMap<String, String>()
                    hashMap1.put(EventConstant.METHOD_EPROPERTY,methodName)
                    setLog("Registration","Registration Success${hashMap1} AF_REFERRER_CUSTOMER_ID: ${SharedPrefHelper.getInstance().get(PrefConstant.AF_REFERRER_CUSTOMER_ID,"")}")
                    delay(3000)

                    EventManager.getInstance().sendEvent(RegistrationSuccessEvent(hashMap1))


                    if(!TextUtils.isEmpty(SharedPrefHelper.getInstance().get(PrefConstant.AF_REFERRER_CUSTOMER_ID,""))){
                        val dataMap=HashMap<String,String>()
                        dataMap.put(EventConstant.AF_USER_ID,
                            SharedPrefHelper.getInstance().get(PrefConstant.AF_REFERRER_CUSTOMER_ID,"")
                        )

//                        GamificationSDK.inviteUser(SharedPrefHelper.getInstance().get(PrefConstant.AF_REFERRER_CUSTOMER_ID,""))
                        setLog("ReferEarnRewardEvent","ReferEarnRewardEvent called ${dataMap}")
                        SharedPrefHelper.getInstance().delete(PrefConstant.AF_REFERRER_CUSTOMER_ID)
                    }else{
                        setLog("Utils","-----AF_REFERRER_CUSTOMER_ID: ${SharedPrefHelper.getInstance().get(PrefConstant.AF_REFERRER_CUSTOMER_ID,"")}")
                    }

                    SharedPrefHelper.getInstance().save(PrefConstant.AF_COMPLETE_REGISTRATION, false)

                }
            }

        }

        fun adTestUserEnable() {
            if (BuildConfig.DEBUG) {
                CoroutineScope(Dispatchers.IO).launch {
                    val testDeviceIds = Arrays.asList("DE798D9CF747377DA15CA9AA217D74EE")
                    val configuration =
                        RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
                    MobileAds.setRequestConfiguration(configuration)

                    CommonUtils.setLog(
                        "adTestUserEnable",
                        "adTestUserEnable called"
                    )
                }
            }

        }

        fun setStopScreenrecord(activity: Activity){
            activity.window.setFlags(WindowManager.LayoutParams.FLAG_SECURE,WindowManager.LayoutParams.FLAG_SECURE)
            setLog("BaseActivityLifecycleMethods", "onCreate")
        }


        fun setWindowProperty(activity: Activity){
            activity.window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false,activity)
            activity.window.statusBarColor = Color.TRANSPARENT
        }

        private fun setWindowFlag(bits: Int, on: Boolean,activity: Activity) {
            val win = activity.window
            val winParams = win.attributes
            if (on) {
                winParams.flags = winParams.flags or bits
            } else {
                winParams.flags = winParams.flags and bits.inv()
            }
            win.attributes = winParams
        }

    }
}