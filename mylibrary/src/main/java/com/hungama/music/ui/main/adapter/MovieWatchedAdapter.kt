package com.hungama.music.ui.main.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.data.model.BodyRowsItemsItem
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.ImageLoader
import com.hungama.music.utils.Utils

class MovieWatchedAdapter(
    private val ctx: Context,
    var list: ArrayList<BodyRowsItemsItem?>?,
    val onChildItemClick: OnChildItemClick?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private inner class IType6ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        var txtRent: TextView = itemView.findViewById(R.id.txtRent)
        val ivSubscription: ImageView = itemView.findViewById(R.id.ivSubscription)
        val ivUserImage: ImageView = itemView.findViewById(R.id.ivUserImage)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        val txtRating : TextView = itemView.findViewById(R.id.txtRating)
        fun bind(position: Int) {
            val list = list!![position]!!.data

            if(position==0){
                llMain.setPadding(ctx?.resources?.getDimensionPixelSize(R.dimen.dimen_20)!!,0,0,0)
            }else{
                llMain.setPadding(0,0,0,0)
            }

            if (list!!.title != null) {
                tvTitle.text = list.title
                tvTitle.visibility = View.VISIBLE
            } else {
                tvTitle.visibility = View.GONE
            }

            if (list!!.subTitle != null) {
                tvSubTitle.text = list.subTitle
                tvSubTitle.visibility = View.VISIBLE
            } else {
                tvSubTitle.visibility = View.GONE
            }
            if(list?.misc!=null&&list?.misc?.movierights!=null&&list?.misc?.movierights?.size!!>0){
                Utils.setMovieRightTextForBucket(ivSubscription, list?.misc?.movierights!!,ctx, list?.id.toString())
            }else{
                txtRent.visibility=View.GONE
            }

            if (list!!.image != null) {
                ImageLoader.loadImage(
                    ctx,
                    ivUserImage,
                    list.image!!,
                    R.drawable.bg_gradient_placeholder
                )
            }
            if (list.misc?.rating_critic!!.equals(0)){
                txtRating.text = "1.0"
            }else{
                var ratingString = list.misc.rating_critic
                var ratingDouble = ratingString.toFloat()
                setLog("TAG", "bind: "+ratingDouble)
                txtRating.text = ratingDouble.toString()
            }
            llMain.setOnClickListener {
                if (onChildItemClick != null) {
                    onChildItemClick.onUserClick(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return IType6ViewHolder(
            LayoutInflater.from(ctx).inflate(R.layout.row_itype_6, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return list!!.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as IType6ViewHolder).bind(position)
    }

    override fun getItemViewType(position: Int): Int {
        return list!![position]?.itype!!
    }

    interface OnChildItemClick {
        fun onUserClick(childPosition: Int)
    }
}