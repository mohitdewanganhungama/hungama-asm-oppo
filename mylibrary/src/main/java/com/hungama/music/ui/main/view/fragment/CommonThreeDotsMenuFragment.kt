package com.hungama.music.ui.main.view.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.utils.customview.bottomsheet.SuperBottomSheetFragment
import com.hungama.music.ui.main.adapter.CommonThreeDotsMunuItemAdapter
import com.hungama.music.data.model.CommonThreeDotsMenuItemModel
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import kotlinx.android.synthetic.main.common_three_dots_menu_fragment.*

class CommonThreeDotsMenuFragment : SuperBottomSheetFragment() {
    companion object{
        var threeDotMenuItemsDataModelList: ArrayList<CommonThreeDotsMenuItemModel>? = null
        var onThreeDotMenuItemClick: OnThreeDotMenuItemClick? = null
        fun newInstance(threeDotMenuItemsDataModelList: ArrayList<CommonThreeDotsMenuItemModel>, onThreeDotMenuItemClick: OnThreeDotMenuItemClick): CommonThreeDotsMenuFragment{
            val fragment = CommonThreeDotsMenuFragment()
            this.threeDotMenuItemsDataModelList = threeDotMenuItemsDataModelList
            this.onThreeDotMenuItemClick = onThreeDotMenuItemClick
            return fragment
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.common_three_dots_menu_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        three_dot_menu_close.setOnClickListener {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), three_dot_menu_close!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            closeDialog()
        }
        shimmerLayout.visibility = View.GONE
        shimmerLayout.stopShimmer()

        setUpMenuList()
    }

    private fun setUpMenuList() {
        rvThreeMenuDots.apply {
            layoutManager =
                GridLayoutManager(context, 1)
            adapter =
                threeDotMenuItemsDataModelList?.let {
                    CommonThreeDotsMunuItemAdapter(context, it,
                        object : CommonThreeDotsMunuItemAdapter.OnMenuItemClick {
                            override fun onMenuItemClick(position: Int, commonThreeDotsMenuItemModel: CommonThreeDotsMenuItemModel) {
                                if (onThreeDotMenuItemClick != null){
                                    onThreeDotMenuItemClick?.onThreeDotMenuItemClick(position, commonThreeDotsMenuItemModel)
                                    setLog("TAG", "onMenuItemClick: position "+position)
                                    dismiss()
                                }
                            }


                        })
                }
            setRecycledViewPool(RecyclerView.RecycledViewPool())
            setHasFixedSize(true)
        }
    }

    override fun getCornerRadius() = requireContext().resources.getDimension(R.dimen.common_popup_round_corner)
    override fun getStatusBarColor() = Color.RED
    override fun isSheetAlwaysExpanded(): Boolean = true

    //calculate height based on three_dot_menu_item_layout and close button
    override fun getExpandedHeight(): Int {
        val rvThreeMenuDotsHeightMargin = requireContext().resources.getDimension(R.dimen.dimen_10)
        val menuItemSize =
            requireContext().resources.getDimension(R.dimen.dimen_50) * (threeDotMenuItemsDataModelList?.size
                ?: 0)
        val closeBtnHeightMargin = requireContext().resources.getDimension(R.dimen.dimen_92)
        return (rvThreeMenuDotsHeightMargin + menuItemSize + closeBtnHeightMargin).toInt()
    }
    override fun getBackgroundColor(): Int = ContextCompat.getColor(requireContext(), R.color.transparent)

    interface OnThreeDotMenuItemClick {
        fun onThreeDotMenuItemClick(position: Int, commonThreeDotsMenuItemModel: CommonThreeDotsMenuItemModel)
    }
}

