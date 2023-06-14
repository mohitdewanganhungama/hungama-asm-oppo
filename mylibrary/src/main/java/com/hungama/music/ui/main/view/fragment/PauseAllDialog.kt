package com.hungama.music.ui.main.view.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hungama.music.R
import com.hungama.music.ui.main.view.fragment.DownloadingProgressFragment.Companion.cancelAllDialog
import com.hungama.music.ui.main.view.fragment.DownloadingProgressFragment.Companion.pauseAllDialog
import com.hungama.music.ui.main.view.fragment.DownloadingProgressFragment.Companion.resumeAllDialog
import com.hungama.music.utils.CommonUtils
import kotlinx.android.synthetic.main.dialog_pause_all.*

class PauseAllDialog(private val listener: addPauseListener, var displayType: Int) : BottomSheetDialogFragment(){
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_pause_all,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(displayType==pauseAllDialog){
            tvTitle.setText(getString(R.string.download_str_7))
            tvSubTitle.setText(getString(R.string.download_str_8))
        }else if(displayType==resumeAllDialog){
            tvTitle.setText(getString(R.string.download_str_13))
            tvSubTitle.setText(getString(R.string.download_str_12))
        }else if(displayType==cancelAllDialog){
            tvTitle.setText(getString(R.string.download_str_6))
            //tvSubTitle.setText(getString(R.string.you_wont_be_able_to_listen_offline))
            tvSubTitle.setText("")
        }

        tvCancel.setOnClickListener {
            dismiss()
        }
        CommonUtils.applyButtonTheme(requireContext(), tvContinue)
        tvContinue.setOnClickListener {
            dismiss()
            if(displayType==pauseAllDialog){
                listener.pauseAll()
            }else if(displayType==resumeAllDialog){
                listener?.resumeAll()
            }else if(displayType==cancelAllDialog){
                listener?.cancelAll()
            }

        }

    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(context!!,R.style.BottomSheetDialogTheme)
    }
  interface addPauseListener{
      fun pauseAll()
      fun resumeAll()
      fun cancelAll()
  }


}

