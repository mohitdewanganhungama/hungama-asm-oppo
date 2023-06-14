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
import com.hungama.music.utils.ImageLoader

class MovieCastAndCrewExtra(
    private val ctx: Context,
    var list:ArrayList<BodyRowsItemsItem?>?,
    val onChildItemClick: OnChildItemClick?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private inner class IType5ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvCast: TextView = itemView.findViewById(R.id.tvCast)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        /*var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        var tvSubTitle2: ShowMoreTextView = itemView.findViewById(R.id.tvSubTitle2)*/

        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        val Main: LinearLayoutCompat = itemView.findViewById(R.id.Main)
        fun bind(position: Int) {
            val data = list!![position]!!.data

            if(position==0){
                Main.setPadding(ctx?.resources?.getDimensionPixelSize(R.dimen.dimen_20)!!,0,0,0)
            }else{
                Main.setPadding(0,0,0,0)
            }

            if (data!!.title != null) {
                tvTitle.text = data.title
                tvTitle.visibility = View.VISIBLE
            } else {
                tvTitle.visibility = View.GONE
            }

            if (data!!.misc?.cast != null) {
                tvCast.text = data!!.misc?.cast
                tvCast.visibility = View.VISIBLE
            } else {
                tvCast.visibility = View.GONE
            }

            if (data?.image != null && !TextUtils.isEmpty(data?.image)) {
                ImageLoader.loadImage(
                    ctx,
                    ivUserImage,
                    data.image!!,
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
        return IType5ViewHolder(
            LayoutInflater.from(ctx).inflate(R.layout.row_itype_1001, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return list!!.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as IType5ViewHolder).bind(position)
    }

    override fun getItemViewType(position: Int): Int {
        return list!![position]?.itype!!
    }

    interface OnChildItemClick {
        fun onUserClick(childPosition: Int)
    }
}