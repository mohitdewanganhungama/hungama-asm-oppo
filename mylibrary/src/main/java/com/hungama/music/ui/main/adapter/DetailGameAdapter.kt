package com.hungama.music.ui.main.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.utils.CommonUtils.getDeviceWidth
import com.hungama.music.utils.Constant
import com.hungama.music.utils.ImageLoader
import com.hungama.music.utils.hide
import com.hungama.music.R


class DetailGameAdapter(val context: Context, val mode: String, private val mList: List<String>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var view: View? = null

    private val ctx: Context = context
    var margin = 0.0
    var marginTop = 0.0
    var marginBottom = 0.0
    var marginStart = 0.0
    var marginEnd = 0.0
    var itemPadding = 0.0
    var itemPaddingTop = 0.0
    var itemPaddingBottom = 0.0
    var itemPaddingStart = 0.0
    var itemPaddingEnd = 0.0
    var parentStartSpacing = 0.0
    var noOfColums = 0.0
    var itemWidth = 0.0
    var lineOne = 0.0
    var lineTwo = 0.0
    var lineThree = 0.0
    var textSize = 0.0
    var imageHeightByAspectRatio = 0.0
    var itemHeight = 0.0
    var commonTopMargin = 0.0
    private var maxScreenWidth: Int = 0
    private var maxScreenHeight: Int = 0

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (mode.contains(Constant.landscape)) {

            ITypeLandscapeViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_5, parent, false)
            )

        } else {
            ITypepViewHolder(LayoutInflater.from(ctx).inflate(R.layout.row_itype_22, parent, false))
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (!mList.isEmpty()) {
            if (mode.contains(Constant.landscape)) {
                (holder as DetailGameAdapter.ITypeLandscapeViewHolder).bind(position)
            } else {
                (holder as DetailGameAdapter.ITypepViewHolder).bind(position)
            }
        }
    }

    private inner class ITypeLandscapeViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val playIcon: ImageView = itemView.findViewById(R.id.play_icon)

        fun bind(position: Int) {
            tvTime.hide()
            playIcon.hide()

            marginStart = 0.0
            marginEnd = ctx.resources.getDimensionPixelSize(R.dimen.dimen_20).toDouble()
            marginTop = commonTopMargin
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0
            maxScreenWidth = getDeviceWidth(ctx)

            noOfColums = 1.50

            parentStartSpacing =
                ctx.resources.getDimensionPixelSize(R.dimen.dimen_18).toDouble() / noOfColums
            itemWidth =
                (maxScreenWidth / noOfColums) - marginStart - marginEnd - itemPaddingStart - itemPaddingEnd - parentStartSpacing

            imageHeightByAspectRatio = itemWidth * 9 / 16
            itemHeight =
                imageHeightByAspectRatio + marginTop + marginBottom + itemPaddingTop + itemPaddingBottom


            val params: ViewGroup.LayoutParams = llMain.layoutParams
            params.height = itemHeight.toInt()
            params.width = itemWidth.toInt()
            llMain.requestLayout()
            val params2: ViewGroup.LayoutParams = ivUserImage.layoutParams
            params2.height = imageHeightByAspectRatio.toInt()
            params2.width = itemWidth.toInt()
            ivUserImage.requestLayout()
            ImageLoader.loadImage(
                ctx, ivUserImage, mList[position], R.drawable.bg_gradient_placeholder
            )
        }
    }

    private inner class ITypepViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)

        fun bind(position: Int) {

            if (!mList.isNullOrEmpty()) {

                ImageLoader.loadImage(
                    ctx, ivUserImage, mList[position], R.drawable.ic_game_placeholder
                )
            }
        }
    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }


}