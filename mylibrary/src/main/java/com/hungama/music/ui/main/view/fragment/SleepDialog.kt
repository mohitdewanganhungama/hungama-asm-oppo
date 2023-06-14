package com.hungama.music.ui.main.view.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hungama.music.R
import com.hungama.music.data.model.*
import com.hungama.music.ui.main.adapter.SleepDialogAdapter
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog


class SleepDialog(val onSleepTimeChangeItemClicked: BaseActivity, val currentTrack: Track?): BottomSheetDialogFragment(),
    SleepDialogAdapter.OnSleepTimeChangeItemClick {
    lateinit var recyclerview : RecyclerView
    private lateinit var sleepadapter : SleepDialogAdapter
    private var sleepList = mutableListOf<SleepDialogModel>()
    var onSleepTimeChangeItemClick: OnSleepTimeChangeItemClick? = null
    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return  inflater.inflate(R.layout.dialog_sleep,container,false)
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.onSleepTimeChangeItemClick = onSleepTimeChangeItemClicked
        var btnclose = view.findViewById(R.id.btnClose)as LinearLayoutCompat?
        btnclose?.setOnClickListener {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), btnclose!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            dismiss()
        }

        recyclerview = view.findViewById(R.id.rvList)
        recyclerview.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL,false)
        sleepadapter = SleepDialogAdapter(requireContext(), this)
        recyclerview.adapter = sleepadapter
        sleepList.add(SleepDialogModel("5 "+getString(R.string.popup_str_59), 5.0))
        sleepList.add(SleepDialogModel("10 "+getString(R.string.popup_str_59), 10.0))
        sleepList.add(SleepDialogModel("15 "+getString(R.string.popup_str_59), 15.0))
        sleepList.add(SleepDialogModel("30 "+getString(R.string.popup_str_59), 30.0))
        sleepList.add(SleepDialogModel("45 "+getString(R.string.popup_str_59), 45.0))
        sleepList.add(SleepDialogModel("60 "+getString(R.string.popup_str_59), 60.0))
        if (currentTrack?.contentType == ContentTypes.PODCAST.value){
            sleepList.add(SleepDialogModel(getString(R.string.popup_str_30), 0.0))
        }else{
            sleepList.add(SleepDialogModel(getString(R.string.popup_str_31), 0.0))
        }
        sleepadapter.setdata(sleepList)
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        dialog?.behavior?.peekHeight = resources.getDimensionPixelSize(R.dimen.dimen_500)
        return dialog
    }

    interface OnSleepTimeChangeItemClick {
        fun onSleepTimeChangeItemClick(data: SleepDialogModel)
    }

    override fun onSleepTimeChangeItemClick(data: SleepDialogModel) {
        setLog("TAG", "onSleepTimeChangeItemClick: data "+data)
        if (onSleepTimeChangeItemClick != null){
            val messageModel = MessageModel(getString(R.string.popup_str_29), getString(R.string.popup_str_82),
                MessageType.NEUTRAL, true)
            CommonUtils.showToast(requireContext(), messageModel)
            onSleepTimeChangeItemClick?.onSleepTimeChangeItemClick(data)
        }
        dismiss()
    }

}