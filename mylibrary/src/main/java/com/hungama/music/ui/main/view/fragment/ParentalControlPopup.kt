package com.hungama.music.ui.main.view.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hungama.music.R
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.addFragment
import com.hungama.music.utils.Constant
import kotlinx.android.synthetic.main.parental_control_popup_view.*

class ParentalControlPopup(ctx: Context, val isExplicit:Boolean) : BottomSheetDialogFragment(),
    View.OnClickListener {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.parental_control_popup_view,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isExplicit){
            tvTitle.text = getString(R.string.popup_str_44)
            TvSubTitle.text = getString(R.string.popup_str_60)
        }else{
            tvTitle.text = getString(R.string.popup_str_61)
            TvSubTitle.text = getString(R.string.popup_str_62)
        }
        llGoToSetting.setOnClickListener(this)
        tvNotNow.setOnClickListener(this)
    }
    

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        dialog.behavior.peekHeight = resources.getDimensionPixelSize(R.dimen.dimen_275)
        dialog.behavior.isDraggable = true
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    override fun onClick(v: View?) {
        if (v == llGoToSetting){
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), llGoToSetting!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            val bundle = Bundle()
            bundle.putString(Constant.EXTRA_PAGE_DETAIL_NAME, "general-setting")
            bundle.putString(Constant.EXTRA_MORE_PAGE_NAME, "-")
            val profileFragment = ProfileFragment()
            profileFragment.arguments = bundle
            addFragment(requireActivity(), R.id.fl_container, this, profileFragment, false)
            dismiss()
        }else if (v == tvNotNow){
            dismiss()
        }
    }
}