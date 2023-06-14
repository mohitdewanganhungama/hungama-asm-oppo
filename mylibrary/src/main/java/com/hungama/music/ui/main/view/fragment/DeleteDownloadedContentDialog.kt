package com.hungama.music.ui.main.view.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hungama.music.R
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.customview.bottomsheet.SuperBottomSheetFragment
import kotlinx.android.synthetic.main.delete_downloaded_content_dialog.*

class DeleteDownloadedContentDialog(val listener: addPauseListener, val size:Int, val contentType: String) : SuperBottomSheetFragment(){
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.delete_downloaded_content_dialog,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(size > 1){
            tvTitle.text = getString(R.string.popup_str_5)+" $size $contentType " + getString(R.string.popup_str_7)
        }else{
            tvTitle.text = getString(R.string.popup_str_11)
        }

        tvCancel.setOnClickListener {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), tvCancel!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            dismiss()
            if(listener != null){
                listener.deleteDownloadedContent(false)
            }
        }
        CommonUtils.applyButtonTheme(requireContext(), tvDelete)
        tvDelete.setOnClickListener {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), tvDelete!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            dismiss()
            if(listener != null){
                listener.deleteDownloadedContent(true)
            }
        }

    }

    override fun getCornerRadius() = requireContext().resources.getDimension(R.dimen.common_popup_round_corner)

    override fun getStatusBarColor() = Color.RED

    override fun isSheetAlwaysExpanded(): Boolean = true

    override fun getExpandedHeight(): Int = requireContext().resources.getDimension(R.dimen.dimen_200).toInt()



    //    @SuppressLint("UseRequireInsteadOfGet")
//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
//        dialog?.behavior.peekHeight = resources.getDimensionPixelSize(R.dimen.dimen_200)
//        dialog?.behavior.isDraggable = false
//        dialog?.setCancelable(false)
//        dialog?.setCanceledOnTouchOutside(true)
//        return dialog
//    }
  interface addPauseListener{
      fun deleteDownloadedContent(status:Boolean)
  }
}

