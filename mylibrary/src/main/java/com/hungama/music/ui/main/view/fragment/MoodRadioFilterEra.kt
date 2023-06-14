package com.hungama.music.ui.main.view.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.slider.RangeSlider
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import com.hungama.music.utils.customview.bottomsheet.SuperBottomSheetFragment
import com.hungama.music.data.model.HomeItem
import com.hungama.music.utils.Constant
import kotlinx.android.synthetic.main.mood_radio_filter_era.*
import kotlinx.android.synthetic.main.mood_radio_filter_select_mood.ivBack
import java.util.*
import kotlin.collections.ArrayList

class MoodRadioFilterEra(val onItemClick: MoodRadioFilterSelectMood.OnItemClick) : SuperBottomSheetFragment() {

    var moodRadioList = ArrayList<HomeItem?>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        //hideSystemNavigationBar()
        return inflater.inflate(R.layout.mood_radio_filter_era, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sliderRange.setValues(SharedPrefHelper.getInstance().getMoodRadioEraFilterMinRange()?.toFloat(), SharedPrefHelper.getInstance().getMoodRadioEraFilterMaxRange()?.toFloat())
        tvMinValue.text = sliderRange.valueFrom.toInt().toString()
        tvMaxValue.text = sliderRange.valueTo.toInt().toString()
        ivBack?.setOnClickListener {
            setRange()
        }

        three_dot_menu_close?.setOnClickListener {
            setRange()
        }
        sliderRange.addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: RangeSlider) {
                // Responds to when slider's touch event is being started
            }

            override fun onStopTrackingTouch(slider: RangeSlider) {
                // Responds to when slider's touch event is being stopped
                //tvMinValue.text = Collections.min(slider.values).toInt().toString()
                //tvMaxValue.text = Collections.max(slider.values).toInt().toString()
            }
        })

        sliderRange.addOnChangeListener { rangeSlider, value, fromUser ->
            // Responds to when slider's value is changed
            //tvMinValue.text = Collections.min(rangeSlider.values).toInt().toString()
            //tvMaxValue.text = Collections.max(rangeSlider.values).toInt().toString()
        }
    }

    private fun setRange(){
        val minRange = Collections.min(sliderRange.values).toInt()
        val maxRange = Collections.max(sliderRange.values).toInt()

        SharedPrefHelper.getInstance().setMoodRadioEraFilterMinRange(minRange)
        SharedPrefHelper.getInstance().setMoodRadioEraFilterMaxRange(maxRange)
        if (onItemClick != null){
            onItemClick.onUserClick(0, Constant.RADIO_ERA_LIST)
            dismiss()
        }
    }

    override fun getCornerRadius() =
        requireContext().resources.getDimension(R.dimen.common_popup_round_corner)

    override fun getStatusBarColor() = Color.RED

    override fun isSheetAlwaysExpanded(): Boolean = true
    override fun getExpandedHeight(): Int =
        requireContext().resources.getDimension(R.dimen.dimen_280).toInt()

    /*override fun getPeekHeight(): Int = requireContext().resources.getDimension(R.dimen.dimen_540).toInt()*/

    override fun getBackgroundColor(): Int =
        requireContext().resources.getColor(R.color.transparent)


}

