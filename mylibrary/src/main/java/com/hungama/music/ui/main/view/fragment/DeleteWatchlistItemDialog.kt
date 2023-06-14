package com.hungama.music.ui.main.view.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hungama.music.R
import com.hungama.music.utils.CommonUtils
import kotlinx.android.synthetic.main.delete_downloaded_content_dialog.*


class DeleteWatchlistItemDialog(val listener: addPauseListener, val size:Int, val contentType: String) : BottomSheetDialogFragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.delete_downloaded_content_dialog, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(size > 1){
            tvTitle.text = getString(R.string.popup_str_5)+" $size $contentType " + getString(R.string.popup_str_99)
        }else{
            tvTitle.text = getString(R.string.popup_str_98)
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
                listener.deleteWatchlistItem(false)
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
                listener.deleteWatchlistItem(true)
            }
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        dialog?.behavior?.peekHeight = resources.getDimensionPixelSize(R.dimen.dimen_200)
        dialog?.behavior?.isDraggable = false
        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(false)
        return dialog
    }
    interface addPauseListener{
        fun deleteWatchlistItem(status:Boolean)
    }

}