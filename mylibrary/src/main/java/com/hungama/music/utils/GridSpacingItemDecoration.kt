package com.hungama.music.utils

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

/**
 * Apply a spacing around items of a grid. It works with GridLayoutManager and StaggeredGridLayoutManager
 */
class GridSpacingItemDecoration(val spacing: Int, val isTop: Boolean, val context: Context) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        if (outRect != null && view != null && parent != null) {
            val (spanCount, spanIndex, spanSize) = extractGridData(parent, view)
            //outRect.left = (spacing * ((spanCount - spanIndex) / spanCount.toFloat())).toInt()
            //outRect.right = (spacing * ((spanIndex + spanSize) / spanCount.toFloat())).toInt()
            //outRect.bottom = spacing
            //outRect.top = spacing
            if (isTop) {
                if (parent.getChildAdapterPosition(view) == 0) {
                    outRect.top = spacing
                }else if (parent.getChildAdapterPosition(view) == 1){
                    outRect.top = spacing
                }
            } else {
                if (parent.getChildAdapterPosition(view) == state.itemCount - 1) {
                    outRect.bottom = spacing
                }/*else if (parent.getChildAdapterPosition(view) == state.itemCount - 2){
                    outRect.bottom = CommonUtils.getValueInDP(context, spacing)
                }*/
            }
        }
    }

    private fun extractGridData(parent: RecyclerView, view: View): GridItemData {
        val layoutManager = parent.layoutManager
        if (layoutManager is GridLayoutManager) {
            return extractGridLayoutData(layoutManager, view)
        } else if (layoutManager is StaggeredGridLayoutManager) {
            return extractStaggeredGridLayoutData(layoutManager, view)
        } else {
            throw UnsupportedOperationException("Bad layout params")
        }
    }

    private fun extractGridLayoutData(layoutManager: GridLayoutManager, view: View): GridItemData {
        val lp: GridLayoutManager.LayoutParams = view.layoutParams as GridLayoutManager.LayoutParams
        return GridItemData(
            layoutManager.spanCount,
            lp.spanIndex,
            lp.spanSize
        )
    }

    private fun extractStaggeredGridLayoutData(layoutManager: StaggeredGridLayoutManager, view: View): GridItemData {
        val lp: StaggeredGridLayoutManager.LayoutParams = view.layoutParams as StaggeredGridLayoutManager.LayoutParams
        return GridItemData(
            layoutManager.spanCount,
            lp.spanIndex,
            if (lp.isFullSpan) layoutManager.spanCount else 1
        )
    }

    internal data class GridItemData(val spanCount: Int, val spanIndex: Int, val spanSize: Int)
}