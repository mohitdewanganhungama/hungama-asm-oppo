package com.hungama.music.ui.main.view.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.utils.customview.bottomsheet.SuperBottomSheetFragment
import com.hungama.music.ui.main.adapter.MoodFilterSelectLangAdapter
import com.hungama.music.data.model.HomeItem
import com.hungama.music.data.model.MoodRadioFilterModel
import com.hungama.music.utils.Constant.RADIO_LANGUAGE_LIST
import kotlinx.android.synthetic.main.mood_radio_filter_select_mood.*

class MoodRadioFilterSelectLanguage(
    val moodRadioPopupLangModel: MoodRadioFilterModel,
    val onItemClick: MoodRadioFilterSelectMood.OnItemClick
) : SuperBottomSheetFragment() {

    var moodRadioList = ArrayList<HomeItem?>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        //hideSystemNavigationBar()
        return inflater.inflate(R.layout.mood_radio_filter_select_lang, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ivBack?.setOnClickListener {
            dismiss()
        }
        btnAsk?.setOnClickListener {
            dismiss()
        }
        rvMood.apply {
            layoutManager = GridLayoutManager(context, 1)
            if (moodRadioPopupLangModel.data?.body?.rows!!.isNotEmpty()) {
                adapter = MoodFilterSelectLangAdapter(context,
                    moodRadioPopupLangModel.data?.body?.rows?.get(0)?.items,
                    object : MoodFilterSelectLangAdapter.OnItemClick {
                        override fun onUserClick(position: Int) {
                            if (onItemClick != null) {
                                onItemClick.onUserClick(position, RADIO_LANGUAGE_LIST)
                                dismiss()
                            }
                        }


                    })
                setRecycledViewPool(RecyclerView.RecycledViewPool())
                setHasFixedSize(true)
            }
        }
    }

    override fun getCornerRadius() =
        requireContext().resources.getDimension(R.dimen.common_popup_round_corner)

    override fun getStatusBarColor() = Color.RED

    override fun isSheetAlwaysExpanded(): Boolean = true
    override fun getExpandedHeight(): Int =
        requireContext().resources.getDimension(R.dimen.dimen_525).toInt()

    /*override fun getPeekHeight(): Int = requireContext().resources.getDimension(R.dimen.dimen_540).toInt()*/

    override fun getBackgroundColor(): Int =
        requireContext().resources.getColor(R.color.transparent)

    interface OnItemClick {
        fun onUserClick(position: Int, type: Int)
    }
}

