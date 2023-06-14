package com.hungama.music.ui.main.view.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
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
import com.hungama.music.data.model.SpeedChangeDialogModel
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.main.adapter.SpeedChangeDialogAdapter
import com.hungama.music.utils.CommonUtils

class SpeedChangeDialog(val onSpeedChangeItemClicked: BaseActivity, val currentTrack: Track?) : BottomSheetDialogFragment(),
    SpeedChangeDialogAdapter.OnSpeedChangeItemClick {

    lateinit var speedchangerecyclerview : RecyclerView

    private lateinit var speedchangeadapter : SpeedChangeDialogAdapter

    private var speedchangelist = mutableListOf<SpeedChangeDialogModel>()
    var onSpeedChangeItemClick: OnSpeedChangeItemClick? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_speed_change,container,false)
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /*view.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener() {
            override fun onGlobalLayout() {
                val dialog = dialog as BottomSheetDialog?
                dialog?.behavior
            }

        })
*/

        this.onSpeedChangeItemClick = onSpeedChangeItemClicked
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

        speedchangerecyclerview = view.findViewById(R.id.rvList)
        speedchangerecyclerview.layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)
        speedchangeadapter = SpeedChangeDialogAdapter(requireContext(), this)
        speedchangerecyclerview.adapter = speedchangeadapter
        speedchangelist.add(SpeedChangeDialogModel("1x", 1.0))
        speedchangelist.add(SpeedChangeDialogModel("1.25x", 1.25))
        speedchangelist.add(SpeedChangeDialogModel("1.5x", 1.5))
        speedchangeadapter.setdata(speedchangelist)
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        dialog?.behavior?.peekHeight = resources.getDimensionPixelSize(R.dimen.dimen_330)
        return dialog
    }

    interface OnSpeedChangeItemClick {
        fun onSpeedChangeItemClick(data: SpeedChangeDialogModel)
    }

    override fun onSpeedChangeItemClick(data: SpeedChangeDialogModel) {
        if (onSpeedChangeItemClick != null){
            onSpeedChangeItemClick?.onSpeedChangeItemClick(data)
        }
        dismiss()
    }
}