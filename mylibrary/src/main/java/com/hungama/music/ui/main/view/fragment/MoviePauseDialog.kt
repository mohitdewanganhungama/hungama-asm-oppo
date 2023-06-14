package com.hungama.music.ui.main.view.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hungama.fetch2.Status
import com.hungama.music.R
import com.hungama.music.player.download.DemoUtil
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.utils.customview.downloadmanager.model.DownloadedAudio
import kotlinx.android.synthetic.main.dialog_download_pause.*

class MoviePauseDialog(val downloadedAudio: DownloadedAudio, val listener: openDeletePopupListener,val postStory : Boolean) :BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_download_pause,container,false)
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        dialog?.behavior?.peekHeight = resources.getDimensionPixelSize(R.dimen.dimen_270)
        dialog?.behavior?.isDraggable = false
        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(false)
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvTitle.setText(downloadedAudio.title)
        clResumePause.setOnClickListener {
            if (downloadedAudio.downloadStatus == Status.PAUSED.value){
                tvDownloadPause.setText(getString(R.string.popup_str_13) +" "+ "("+ downloadedAudio.percentDownloaded + "%)")
                //DemoUtil.getDownloadManager(requireContext()).resumeDownloads()
                //(requireActivity() as BaseActivity).resumeAllVideoDownloads()
                (activity as BaseActivity).pauseAllVideoDownloads(false, downloadedAudio.downloadUrl)
            }else if (downloadedAudio.downloadStatus == Status.DOWNLOADING.value){
                tvDownloadPause.setText(getString(R.string.popup_str_12) +" "+ "("+ downloadedAudio.percentDownloaded + "%)")
                (requireActivity() as BaseActivity).pauseAllVideoDownloads(true, downloadedAudio.downloadUrl)
            }else if (downloadedAudio.downloadStatus == Status.COMPLETED.value){
                tvDownloadPause.setText(getString(R.string.general_str_2))
                if (listener != null){
                    listener.openDeletePopup(true)
                }
            }
            dismiss()
        }
        if (downloadedAudio.downloadStatus == Status.PAUSED.value){
            tvDownloadPause.setText(getString(R.string.popup_str_12) +" "+ "("+ downloadedAudio.percentDownloaded + "%)")
        }else if (downloadedAudio.downloadStatus == Status.DOWNLOADING.value){
            tvDownloadPause.setText(getString(R.string.popup_str_13) +" "+ "("+ downloadedAudio.percentDownloaded + "%)")
        }else if (downloadedAudio.downloadStatus == Status.COMPLETED.value){
            tvDownloadPause.setText(getString(R.string.general_str_2))
            vPause?.setImageDrawable(requireContext().faDrawable(R.string.icon_delete, R.color.colorWhite))
        }else{
            vPause.visibility = View.GONE
            tvDownloadPause.visibility = View.GONE
            clResumePause.visibility = View.INVISIBLE
            clResumePause.setOnClickListener(null)
            if (listener != null){
                listener.openDeletePopup(true)
            }
            dismiss()
        }
        if (postStory){
            clPostasStory.visibility = View.VISIBLE
            clPostasStory.setOnClickListener {
                if (listener != null){
                    listener.postAsStory()
                }
            }
        }else{
            clPostasStory.visibility = View.GONE
        }
//        tvDeleteMovie.setOnClickListener {
////            var deleteMovieDialog = DownloadDeleteDialog()
////            deleteMovieDialog.show(activity?.supportFragmentManager!!,"open movie delete dialog")
//            if (listener != null){
//                listener.openDeletePopup(true)
//            }
//            dismiss()
//
//        }
        btnClose.setOnClickListener {
            if (listener != null){
                listener.openDeletePopup(false)
            }
            dismiss()
        }
        clShare.setOnClickListener {
            if (listener != null){
                listener.share()
                dismiss()
            }
        }
    }

    interface openDeletePopupListener{
        fun openDeletePopup(status: Boolean)
        fun share()
        fun postAsStory()
    }

}