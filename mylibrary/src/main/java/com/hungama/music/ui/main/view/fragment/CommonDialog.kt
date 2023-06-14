package com.hungama.music.ui.main.view.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hungama.music.HungamaMusicApp
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.home.eventreporter.UserAttributeEvent
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.hide
import com.hungama.music.utils.preference.PrefConstant
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import kotlinx.android.synthetic.main.dialog_logout.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CommonDialog constructor(
    private val listener: CommonDialogListener,
    private var title: String,
    private var tv_yes: String,
    private var tv_no: String
) : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_common,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var tv_Title = view.findViewById(R.id.tvTitle) as TextView
        var tvSubTitle = view.findViewById(R.id.tvSubTitle) as TextView

        var btnAsk = view.findViewById(R.id.btnAsk) as TextView
        var tv_cancel = view.findViewById(R.id.tv_cancel) as TextView

        tv_Title.hide()
        tvSubTitle.text= title
        btnAsk.text= tv_yes
        tv_cancel.text= tv_no


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
            listener.dialogClick()
            dismiss()
        }

    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        var dialog =  BottomSheetDialog(context!!,R.style.BottomSheetDialogTheme)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        return dialog
    }
    interface CommonDialogListener{
        fun dialogClick()
    }



}

