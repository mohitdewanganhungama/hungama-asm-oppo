package com.hungama.music.ui.main.view.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hungama.music.utils.preference.PrefConstant
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.home.eventreporter.UserAttributeEvent
import com.hungama.music.utils.CommonUtils
import kotlinx.android.synthetic.main.dialog_logout.*

class LogoutDialog constructor(private val logoutListener: LogoutListener) : BottomSheetDialogFragment(){
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_logout,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var btncancel = view.findViewById(R.id.ivCancel) as LinearLayoutCompat
        CommonUtils.applyButtonTheme(requireContext(), btncancel)
        btncancel.setOnClickListener {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), btncancel!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            dismiss()
        }
        var btnlogout = view?.findViewById(R.id.btnLogout) as LinearLayoutCompat
        btnlogout.setOnClickListener {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), btnLogout!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            dismiss()
            logoutListener.LogoutUser()
            val userDataMap=HashMap<String,String>()
            userDataMap.put(EventConstant.HUNGAMA_ID,""+ SharedPrefHelper.getInstance().get(
                PrefConstant.SILENT_USER_ID,""))
            userDataMap.put(EventConstant.MOREANONYMOUS_ID,""+ SharedPrefHelper.getInstance().get(
                PrefConstant.SILENT_USER_ID,""))
            EventManager.getInstance().sendUserAttribute(UserAttributeEvent(userDataMap))
        }

    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(context!!,R.style.BottomSheetDialogTheme)
    }
    interface LogoutListener{
        fun LogoutUser()
    }


}

