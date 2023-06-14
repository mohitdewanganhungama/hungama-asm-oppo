package com.hungama.music.ui.main.view.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hungama.music.HungamaMusicApp
import com.hungama.music.R
import com.hungama.music.data.model.MessageModel
import com.hungama.music.data.model.MessageType
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.ConnectionUtil
import com.hungama.music.utils.Constant
import com.hungama.music.utils.Utils
import com.hungama.music.utils.customview.bottomsheet.SuperBottomSheetFragment

class NoInternetDialog(val mContext: Context) : BottomSheetDialogFragment() {

    companion object{
        var isShowing=false
    }
    var currentOrientation = ORIENTATION_PORTRAIT
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            currentOrientation = resources.configuration.orientation
        }catch (e:Exception){

        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.dialog_no_internet,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setLog("TAG", "onViewCreated: isShowing${isShowing}")
        var tvTitle = view.findViewById(R.id.tvTitle)as TextView
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            Utils.setMarginsTop(tvTitle, resources.getDimensionPixelSize(R.dimen.dimen_15))
        } else {
            Utils.setMarginsTop(tvTitle, resources.getDimensionPixelSize(R.dimen.dimen_39))
        }
        var btnRetry = view.findViewById(R.id.ivRetry)as LinearLayoutCompat
        var btnDownloads = view.findViewById(R.id.ivDownload)as LinearLayoutCompat
        CommonUtils.applyButtonTheme(requireContext(), btnDownloads)
        btnRetry.setOnClickListener {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), btnRetry!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            dismiss()
        }
        btnDownloads.setOnClickListener {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), btnDownloads!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }

                if (mContext != null){
                    if (!ConnectionUtil(mContext).isOnline(false)){
                        Constant.ISGOTODOWNLOADCLICKED = true
                    }
                    if (mContext is MainActivity){
                        (mContext as MainActivity).applyScreen(4)
                    }else{
                        val deeplinkUrl="https://www.hungama.com/library"
                        val intent = CommonUtils.getDeeplinkIntentData(Uri.parse(deeplinkUrl))
                        intent.setClass(requireContext(), MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                }
            }catch (e:Exception){

            }
            dismiss()
        }


      Utils.hideSoftKeyBoard(HungamaMusicApp.getInstance().applicationContext!!,requireView())

        isShowing=true
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        dialog.behavior.peekHeight = resources.getDimensionPixelSize(R.dimen.dimen_400)
        dialog.behavior.isDraggable = false
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    override fun onStop() {
        isShowing=false
        super.onStop()
    }
    override fun onDestroy() {
        isShowing=false
        super.onDestroy()

    }
}