package com.hungama.music.ui.main.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.data.model.BodyRowsItemsItem
import com.hungama.music.utils.ImageLoader

class MovieTrailersExtra(
    private val ctx: Context,
    var list: ArrayList<BodyRowsItemsItem?>?,
    val onChildItemClick: OnChildItemClick?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private inner class IType5ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        //var tvHeading: TextView = itemView.findViewById(R.id.tvHeading)
        /*var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        var tvSubTitle2: ShowMoreTextView = itemView.findViewById(R.id.tvSubTitle2)*/
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        fun bind(position: Int) {
            val list = list!![position]!!.data

            if(position==0){
                llMain.setPadding(ctx?.resources?.getDimensionPixelSize(R.dimen.dimen_20)!!,0,0,0)
            }else{
                llMain.setPadding(0,0,0,0)
            }
            /*if (list!!.title != null) {
                tvHeading.text = list.title
                tvHeading.visibility = View.VISIBLE
            } else {
                tvHeading.visibility = View.GONE
            }*/

            if (list!!.image != null) {
                ImageLoader.loadImage(
                    ctx,
                    ivUserImage,
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
        return IType5ViewHolder(
            LayoutInflater.from(ctx).inflate(R.layout.row_itype_5, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return list!!.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as IType5ViewHolder).bind(position)
    }

    override fun getItemViewType(position: Int): Int {
        return list?.get(position)?.itype!!
    }

    interface OnChildItemClick {
        fun onUserClick(childPosition: Int)
    }
}