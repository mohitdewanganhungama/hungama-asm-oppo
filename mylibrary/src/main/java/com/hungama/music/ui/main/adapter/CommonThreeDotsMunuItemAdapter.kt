package com.hungama.music.ui.main.adapter

import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.data.model.CommonThreeDotsMenuItemModel
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.hide

class CommonThreeDotsMunuItemAdapter(
    val context: Context,
    var arrayList: ArrayList<CommonThreeDotsMenuItemModel>,
    val onMenuItemClick: OnMenuItemClick?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ctx: Context = context
    var list: ArrayList<CommonThreeDotsMenuItemModel> = arrayList

    private inner class ThreeDotMenuViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tv_three_dot_menu)
        var ivIcon: ImageView = itemView.findViewById(R.id.iv_three_dot_menu)
        val llMain: RelativeLayout = itemView.findViewById(R.id.rl_three_dot_menu_main)
        val devider1: View = itemView.findViewById(R.id.devider1)
        fun bind(position: Int) {
            val data = list[position]

            if (data.title != null) {
                tvTitle.text = data.title
                setLog("TAG", "TITLE = " +data.title )
                tvTitle.visibility = View.VISIBLE
            } else {
                tvTitle.visibility = View.GONE
            }

            if (!TextUtils.isEmpty(data.icon?.toString())){
                /*val drawable = FontDrawable(ctx, list.icon!!)
                drawable.setTextColor(ContextCompat.getColor(ctx, R.color.colorWhite))
                ivIcon.setImageDrawable(drawable)*/
                ivIcon?.setImageDrawable(ctx.faDrawable(data.icon!!, R.color.colorWhite, ctx.resources.getDimensionPixelSize(R.dimen.font_17).toFloat()))
            }else{
                ivIcon.visibility = View.GONE
            }

            if (!list.isNullOrEmpty() && list.size-1 == position){
                devider1.hide()
            }

            llMain.setOnClickListener {
                onMenuItemClick?.onMenuItemClick(position, data)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ThreeDotMenuViewHolder(
            LayoutInflater.from(ctx).inflate(R.layout.three_dot_menu_item_layout, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ThreeDotMenuViewHolder).bind(position)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    interface OnMenuItemClick {
        fun onMenuItemClick(position: Int, commonThreeDotsMenuItemModel: CommonThreeDotsMenuItemModel)
    }
}