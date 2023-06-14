package com.hungama.music.ui.main.view.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hungama.music.R
import com.hungama.music.data.model.BookmarkDataModel
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import kotlinx.android.synthetic.main.dialog_watchlist_three_dot_all.*

class WatchlistThreeDotDialog(
    val position:Int,
    private val modelItem: BookmarkDataModel.Data.Body.Row?,
    var listener: WatchlistThreeDotListener,
    var contentId : Int,
) : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_watchlist_three_dot_all, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (contentId == 2){
            vDeviderLine.visibility = View.GONE
            tvDownloadMovie.visibility = View.GONE
            ivDownload.visibility = View.GONE
            llviews.visibility = View.GONE
            vDeviderLine1.visibility = View.GONE
        }
        else{
            vDeviderLine.visibility = View.VISIBLE
            tvDownloadMovie.visibility = View.VISIBLE
            ivDownload.visibility = View.VISIBLE
        }
        btnClose.setOnClickListener {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), btnClose!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            dismiss()
        }

        if (modelItem?.data?.isSelected==0){
            setLog("TAG", "modelItem?.data?.isSelected = "+modelItem?.data?.isSelected )
            tvRemoveFromList.visibility = View.GONE
            vPause.visibility = View.GONE
        }

        tvRemoveFromList.setOnClickListener {
            dismiss()
            if(listener!=null){
                listener?.removeFromMyList(modelItem,position)
            }
        }

        tvDownloadMovie.setOnClickListener {
            dismiss()
            if(listener!=null){
                listener?.download(modelItem,position)
            }
        }

        tvShareMovie.setOnClickListener {
            dismiss()
            if(listener!=null){
                listener?.share(modelItem,position)
            }
        }
        llviews.setOnClickListener {
            if (listener != null){
                listener.postAsStory()
                dismiss()
            }
        }

    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        dialog.behavior.peekHeight = resources.getDimensionPixelSize(R.dimen.dimen_290)
        dialog.behavior.isDraggable = true
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    interface WatchlistThreeDotListener {
        fun removeFromMyList(modelItem: BookmarkDataModel.Data.Body.Row?, position: Int)
        fun download(modelItem: BookmarkDataModel.Data.Body.Row?, position: Int)
        fun share(modelItem: BookmarkDataModel.Data.Body.Row?, position: Int)
        fun postAsStory()
        fun cancel()
    }


}

