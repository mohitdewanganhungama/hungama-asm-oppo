package com.hungama.music.utils

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

open class RecyclerViewMarginItemDecoration(
    private val sizeInDp: Int = 16,
    private val isTop: Boolean = false,
    private val context: Context
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        if (isTop) {
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.top = sizeInDp
            }
        } else {
            if (parent.getChildAdapterPosition(view) == state.itemCount - 1) {
                outRect.bottom = sizeInDp
            }
        }
    }
}