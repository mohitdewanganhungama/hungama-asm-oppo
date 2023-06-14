package com.hungama.music.ui.main.view.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hungama.music.R
import com.hungama.music.data.model.ContentTypes
import com.hungama.music.utils.CommonUtils
import kotlinx.android.synthetic.main.dialog_delete_song_from_queue.*

class DeleteContentFromQueueDialog(val listener: deleteListener,
                                   val contentName:String,
                                   val contentType: Int,
                                   val contentPosition: Int) : BottomSheetDialogFragment(){
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_delete_song_from_queue,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(contentType == ContentTypes.PODCAST.value){
            tvTitle.text = getString(R.string.popup_str_24)
            tvSubTitle2.text = getString(R.string.popup_str_25)
        }else{
            tvTitle.text = getString(R.string.popup_str_22)
            tvSubTitle2.text = getString(R.string.popup_str_23)
        }

        tvSubTitle?.text = "\"$contentName\""

        llNo.setOnClickListener {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), llNo!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            dismiss()
            if(listener != null){
                listener.deleteContentFromQueue(false, contentPosition)
            }
        }
        CommonUtils.applyButtonTheme(requireContext(), llYes)
        llYes.setOnClickListener {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), llYes!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            dismiss()
            if(listener != null){
                listener.deleteContentFromQueue(true, contentPosition)
            }
        }

    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        dialog?.behavior?.peekHeight = resources.getDimensionPixelSize(R.dimen.dimen_230)
        dialog?.behavior?.isDraggable = false
        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(false)
        return dialog
    }
  interface deleteListener{
      fun deleteContentFromQueue(status:Boolean, contentPosition: Int)
  }
}

