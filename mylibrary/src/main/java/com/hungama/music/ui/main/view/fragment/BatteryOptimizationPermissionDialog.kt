package com.hungama.music.ui.main.view.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.BackgroundActivityEvent
import com.hungama.music.eventanalytic.eventreporter.ProgressiveSurveyTappedEvent
import com.hungama.music.R
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.customview.bottomsheet.SuperBottomSheetFragment
import com.hungama.music.utils.preference.PrefConstant
import com.hungama.music.utils.preference.SharedPrefHelper
import kotlinx.android.synthetic.main.battery_optimization_permission_dialog.*
import kotlinx.android.synthetic.main.delete_downloaded_content_dialog.*
import kotlinx.android.synthetic.main.delete_downloaded_content_dialog.tvCancel
import kotlinx.android.synthetic.main.delete_downloaded_content_dialog.tvDelete

class BatteryOptimizationPermissionDialog(batteryPermission1: BatteryPermission) : SuperBottomSheetFragment(){
    val batteryPermission = batteryPermission1


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.battery_optimization_permission_dialog,container,false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    dialog!!.setCanceledOnTouchOutside(false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        tvCancel.setOnClickListener {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), tvCancel!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }

            SharedPrefHelper.getInstance().save(PrefConstant.backgroundActivity, "Custom Denied")
            dismiss()
        }
        CommonUtils.applyButtonTheme(requireContext(), tvGotoSettings)
        tvGotoSettings.setOnClickListener {

            batteryPermission.onBatteryPermission()
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), tvDelete!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            dismiss()

        }

    }

    override fun getCornerRadius() = requireContext().resources.getDimension(R.dimen.common_popup_round_corner)

//    override fun getStatusBarColor() = Color.RED

    override fun isSheetAlwaysExpanded(): Boolean = true

    override fun getExpandedHeight(): Int = requireContext().resources.getDimension(R.dimen.dimen_200).toInt()
    interface BatteryPermission{
        fun onBatteryPermission()
    }
}

