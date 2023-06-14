package com.hungama.music.ui.main.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.drawable.PaintDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager.widget.PagerAdapter
import com.hungama.music.R
import com.hungama.music.data.model.RowsItem
import com.hungama.music.utils.CommonUtils.applyButtonTheme
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.ImageLoader.loadImage
import com.hungama.music.utils.Utils.Companion.setMovieRightTextForBucketWithPlay

class Itype50PagerAdapter(
    private val rowsItem: RowsItem,
    private val ctx: Context,
    onChildItemClick: OnChildItemClick?
) : PagerAdapter() {
    private var pageCount: Int
    var onChildItemClick: OnChildItemClick?

    interface OnChildItemClick {
        fun onUserClick(childPosition: Int)
    }

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val layout = LayoutInflater.from(collection.context)
            .inflate(R.layout.row_itype_50, collection, false) as ViewGroup
        val title = layout.findViewById<TextView>(R.id.tvTitle)
        val subTitle = layout.findViewById<TextView>(R.id.tvSubTitle)
        val ivUserImage = layout.findViewById<ImageView>(R.id.ivUserImage)
        val llMain: ConstraintLayout = layout.findViewById(R.id.llMain)
        val txtRent = layout.findViewById<TextView>(R.id.txtRent)
        val ivRent = layout.findViewById<ImageView>(R.id.ivRent)
        val llRent: LinearLayoutCompat = layout.findViewById(R.id.llRent)
        val vCenterGradient:View = layout.findViewById(R.id.vCenterGradient)
        applyButtonTheme(ctx, llRent)
        llMain.layoutParams.height = ctx.resources.getDimensionPixelSize(R.dimen.dimen_500)
        llMain?.requestLayout()
        if (!rowsItem.items.isNullOrEmpty() && rowsItem.items?.size!! > position) {
            if (!TextUtils.isEmpty(rowsItem.items?.get(position)?.data?.title)) {
                title.text = rowsItem.items?.get(position)?.data?.title
                title.visibility = View.VISIBLE
            } else {
                title.visibility = View.GONE
            }
            if (rowsItem.items?.get(position)?.data?.subTitle != null) {
                subTitle.text = rowsItem.items?.get(position)?.data?.subTitle
                subTitle.visibility = View.VISIBLE
            } else {
                subTitle.visibility = View.GONE
            }

            if (rowsItem.items?.get(position)?.data?.misc != null && !rowsItem.items?.get(position)?.data?.misc?.movierights.isNullOrEmpty()) {
                setLog("TAG", "instantiateItem:" + rowsItem.items?.get(position)?.data)
                setMovieRightTextForBucketWithPlay(
                    txtRent,
                    ivRent,
                    rowsItem.items?.get(position)?.data?.misc?.movierights!!,
                    ctx,
                    rowsItem.items?.get(position)?.data?.id.toString()
                )
            } else {
                txtRent.visibility = View.GONE
            }
            if (rowsItem.items?.get(position)?.data?.image != null) {
                loadImage(
                    ctx,
                    ivUserImage,
                    rowsItem.items?.get(position)?.data?.image.toString(),
                    R.drawable.bg_gradient_placeholder
                )
            }
            llMain.setOnClickListener { v: View? ->
                if (onChildItemClick != null) {
                    onChildItemClick!!.onUserClick(position)
                }
            }
            /*val colors = intArrayOf(Color.parseColor("#00000000"),
                Color.parseColor("#FF000000"), Color.parseColor("#00282828"))
            val positions = floatArrayOf(
                0f,
                0.5f,
                1f
            )
            applyAppButtonGradient(180f,0f,180f,443.916f, 0f, colors, positions, ctx, vCenterGradient)*/

        }

        collection.addView(layout)
        setLog("TAG", "instantiateItem: 111$position")
        return layout
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }

    override fun getCount(): Int {
        return pageCount
    }

    fun setCount(count: Int) {
        pageCount = count
        notifyDataSetChanged()
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return position.toString()
    }

    init {
        pageCount = rowsItem.items!!.size
        this.onChildItemClick = onChildItemClick
    }
}