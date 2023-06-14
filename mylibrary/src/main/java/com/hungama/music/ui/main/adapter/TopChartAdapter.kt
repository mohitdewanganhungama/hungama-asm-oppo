package com.hungama.music.ui.main.adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.data.model.BodyRowsItemsItem
import com.hungama.music.data.model.PlaylistModel
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.ImageLoader

class TopChartAdapter(
    context: Context,
    list: ArrayList<BodyRowsItemsItem?>?,
    val onChildItemClick: OnChildItemClick?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ctx: Context = context
    var list: ArrayList<BodyRowsItemsItem?>? = list

    private inner class IType12ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val ivUserImage2: ImageView = itemView.findViewById(R.id.ivUserImage2)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        fun bind(position: Int) {
            val list = list!![position]!!.data

            if (!TextUtils.isEmpty(list?.title!!)) {
                tvTitle.text = list.title
                tvTitle.visibility = View.VISIBLE
            } else {
                tvTitle.visibility = View.GONE
            }

            if (list?.misc != null && !TextUtils.isEmpty(list?.misc?.f_playcount)) {
                tvSubTitle.text = list?.misc?.f_playcount+ " " +ctx.getString(R.string.discover_str_24)
                tvSubTitle.visibility = View.VISIBLE
            } else {
                tvSubTitle.visibility = View.GONE
            }


            if (list?.images!! != null && list?.images?.size!! > 0) {
                val turl=list?.images?.get((0..list?.images?.size!!-1).random())
                ImageLoader.loadImage(
                    ctx,
                    ivUserImage,
                    turl!!,
                    R.drawable.bg_gradient_placeholder
                )
            }
            if (list?.image != null) {
                ImageLoader.loadImage(
                    ctx,
                    ivUserImage2,
                    list.image!!,
                    R.drawable.bg_gradient_placeholder
                )
            }
            llMain.setOnClickListener {
                if (onChildItemClick != null) {
                    onChildItemClick.onUserClick(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return IType12ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_42, parent, false)
            )
    }

    override fun getItemCount(): Int {
        return list!!.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as IType12ViewHolder).bind(position)
    }

    override fun getItemViewType(position: Int): Int {
        return list!![position]?.itype!!
    }

    interface OnChildItemClick {
        fun onUserClick(childPosition: Int)
    }
}