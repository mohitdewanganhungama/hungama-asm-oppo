package com.hungama.music.ui.main.view.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hungama.music.R
import com.hungama.music.data.model.MessageModel
import com.hungama.music.data.model.MessageType
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.ConnectionUtil
import com.hungama.music.utils.Constant
import com.hungama.music.utils.Utils
import com.hungama.music.utils.preference.SharedPrefHelper
import kotlinx.android.synthetic.main.user_censor_rationg_popup_view.*
import org.json.JSONArray
import org.json.JSONObject

class UserCensorRatingPopup(ctx: Context, val onUserCensorRatingChange: OnUserCensorRatingChange?) : BottomSheetDialogFragment(),
    View.OnClickListener {
    var userViewModel: UserViewModel? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.user_censor_rationg_popup_view,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("TAG", "onCensorRatingChange 1: onViewCreated")
        llYes.setOnClickListener(this)
        llNo.setOnClickListener(this)
    }
    

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        dialog.behavior.peekHeight = resources.getDimensionPixelSize(R.dimen.dimen_200)
        dialog.behavior.isDraggable = true
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    override fun onClick(v: View?) {
        if (v == llYes){
            Log.d("TAG", "onCensorRatingChange 2: llYes clicked")

            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), llYes!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
            saveSetting(true)
            SharedPrefHelper.getInstance().setUserCensorRating(18)
            onUserCensorRatingChange?.onCensorRatingChange(18)
            dismiss()
        }else if (v == llNo){
            Log.d("TAG", "onCensorRatingChange 3: llNo clicked")

            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), llNo!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){
e.printStackTrace()
            }
            saveSetting(false)
            SharedPrefHelper.getInstance().setUserCensorRating(0)
            onUserCensorRatingChange?.onCensorRatingChange(0)
            dismiss()
        }
    }

    interface OnUserCensorRatingChange{
        fun onCensorRatingChange(rating:Int)
    }

    private fun saveSetting(isUser18Plus:Boolean) {
        userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)
        if (ConnectionUtil(requireContext()).isOnline) {

            try {


                val mainJson = JSONObject()
                val prefArrays = JSONArray()
                val emailSettingJson = JSONObject()
                val userSettingRespModel=SharedPrefHelper.getInstance().getUserPlayBackSetting(Constant.TYPE_GENERAL_SETTING)
                if (userSettingRespModel?.data != null && userSettingRespModel.data?.data?.get(0)?.preference?.get(0) != null) {

                    emailSettingJson.put("emailNotification", userSettingRespModel.data?.data?.get(0)?.preference?.get(0)?.emailNotification!!)
                    emailSettingJson.put("mobileNotification", userSettingRespModel.data?.data?.get(0)?.preference?.get(0)?.mobileNotification!!)
                    //emailSettingJson.put("allowExplicitContent", svExplicit?.isChecked)
                    val parentalJson = JSONObject()
                    parentalJson.put("allowExplicitAudioContent", userSettingRespModel.data?.data?.get(0)?.preference?.get(0)?.parentalControl?.allowExplicitAudioContent!!)
                    parentalJson.put("allowExplicitVideoContent", userSettingRespModel.data?.data?.get(0)?.preference?.get(0)?.parentalControl?.allowExplicitVideoContent!!)
                    emailSettingJson.put("parentalControl", parentalJson)
                    emailSettingJson.put("allowAge18Plus", isUser18Plus)
                    emailSettingJson.put("appLanguage", SharedPrefHelper.getInstance().getLanguage())


                }else{
                    emailSettingJson.put("emailNotification", true)
                    emailSettingJson.put("mobileNotification", true)
                    //emailSettingJson.put("allowExplicitContent", svExplicit?.isChecked)
                    val parentalJson = JSONObject()
                    parentalJson.put("allowExplicitAudioContent", false)
                    parentalJson.put("allowExplicitVideoContent", false)
                    emailSettingJson.put("parentalControl", parentalJson)
                    emailSettingJson.put("allowAge18Plus", isUser18Plus)
                    emailSettingJson.put("appLanguage", SharedPrefHelper.getInstance().getLanguage())
                }

                prefArrays.put(emailSettingJson)


                mainJson.put("type", Constant.TYPE_GENERAL_SETTING)
                mainJson.put("preference", prefArrays)


                userViewModel?.saveUserPref(
                    requireContext(),
                    mainJson.toString(),
                    Constant.TYPE_GENERAL_SETTING
                )

            } catch (e: Exception) {
                e.printStackTrace()
                Utils.showSnakbar(requireContext(),
                    requireView(),
                    false,
                    getString(R.string.discover_str_2)
                )
            }


        } else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }
}