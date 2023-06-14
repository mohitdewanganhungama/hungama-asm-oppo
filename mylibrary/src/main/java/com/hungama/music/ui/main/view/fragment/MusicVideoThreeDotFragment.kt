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
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.ThreeDotsClickedEvent
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.Utils
import com.hungama.music.R
import kotlinx.android.synthetic.main.delete_downloaded_content_dialog.*
import kotlinx.android.synthetic.main.fragment_music_video_three_dot.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


class MusicVideoThreeDotFragment(
    var contentShareLink: String,
    val listener: addPauseListener,
    var title: String
) : BottomSheetDialogFragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_music_video_three_dot, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clShare.setOnClickListener {
            Utils.shareItem(requireContext(),contentShareLink)
            eventThreeDotsMenuClick(title, getString(R.string.popup_str_101))

        }
        clResumePause.setOnClickListener {
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
                listener.RemoveMusicVideo(true)
            }
        }
        btnClose.setOnClickListener {
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
                listener.RemoveMusicVideo(false)
            }
        }
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

    fun eventThreeDotsMenuClick(source:String, menuTitle:String){
        CoroutineScope(Dispatchers.IO).launch {
            var hashMap = HashMap<String, String>()
            hashMap.put(EventConstant.SOURCE_EPROPERTY, MainActivity.lastItemClicked+"_"+ MainActivity.headerItemName+"_"+source)
            hashMap.put(EventConstant.ACTION_EPROPERTY, menuTitle)
            EventManager.getInstance().sendEvent(ThreeDotsClickedEvent(hashMap))
        }

    }

    interface addPauseListener{
        fun RemoveMusicVideo(status:Boolean)
    }
}