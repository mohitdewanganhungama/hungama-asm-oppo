package com.hungama.music.ui.main.adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.data.model.ProductRespModel
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.ImageLoader

class SimilarProductAdapter(var context: Context, var btnRedeem: RedeemInterface, val isVertical:Boolean) :
    RecyclerView.Adapter<SimilarProductAdapter.ViewHolder>() {
    var adscartlist = ArrayList<ProductRespModel.Product?>()
    internal fun setcart(data: ArrayList<ProductRespModel.Product?>){
        adscartlist = data
        notifyDataSetChanged()
    }
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var rectagleimage = itemView.findViewById(R.id.clCart) as ImageView
        var image = itemView.findViewById(R.id.ivimage)as ImageView
        var title = itemView.findViewById(R.id.tvcartTitle)as TextView
        var coinpoint = itemView.findViewById(R.id.tvCartCounPoint)as TextView
        var btnRedeem = itemView.findViewById(R.id.btnRedeem) as LinearLayoutCompat
        var clMain = itemView.findViewById(R.id.clMain) as ConstraintLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.row_redeem_coins, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var datalist = adscartlist[position]
        CommonUtils.applyButtonTheme(context, holder.btnRedeem)
        if(!isVertical){
            holder.clMain.layoutParams.width = context.resources.getDimensionPixelSize(R.dimen.dimen_152)
            holder.clMain?.requestLayout()
        }
        if (!datalist?.images.isNullOrEmpty() && !TextUtils.isEmpty(datalist?.images?.get(0)?.src)){
            ImageLoader.loadImage(
                context,
                holder.rectagleimage,
                datalist?.image?.src!!,
                R.drawable.bg_gradient_placeholder
            )
            ImageLoader.loadImage(
                context,
                holder.image,
                datalist?.image?.src!!,
                R.drawable.bg_gradient_placeholder
            )
        }else{
            ImageLoader.loadImage(
                context,
                holder.rectagleimage,
                "",
                R.drawable.bg_gradient_placeholder
            )
            ImageLoader.loadImage(
                context,
                holder.image,
                "",
                R.drawable.bg_gradient_placeholder
            )
        }
        holder.title.text = datalist?.title
        val coin = CommonUtils.covertNumberToCurrencyFormat(datalist?.variants?.get(0)?.price.toString())
        holder.coinpoint.text = coin + " " +context.getString(R.string.profile_str_49)

        holder.btnRedeem.setOnClickListener {
            if (btnRedeem != null){
                btnRedeem.btnRedeem(datalist)
            }
        }
    }

    override fun getItemCount(): Int {
        return adscartlist.size
    }
    interface RedeemInterface{
        fun btnRedeem(datalist: ProductRespModel.Product?)
    }
}