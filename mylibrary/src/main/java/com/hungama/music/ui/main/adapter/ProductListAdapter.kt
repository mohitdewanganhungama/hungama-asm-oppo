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
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.data.model.ProductRespModel
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.applyButtonTheme
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.utils.CommonUtils.setAppButton3
import com.hungama.music.utils.ImageLoader

class ProductListAdapter(var context: Context,var btnRedeem: RedeemInterface) :
    RecyclerView.Adapter<ProductListAdapter.ViewHolder>() {
    var adscartlist = ArrayList<ProductRespModel.Product?>()
    internal fun setcart(data: ArrayList<ProductRespModel.Product?>){
        adscartlist = data
        notifyDataSetChanged()
    }
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var rectagleimage = itemView.findViewById(R.id.clCart) as ImageView
        var image = itemView.findViewById(R.id.ivimage)as ImageView
        var ivIcon = itemView.findViewById(R.id.ivIcon)as ImageView
        var title = itemView.findViewById(R.id.tvcartTitle)as TextView
        var coinpoint = itemView.findViewById(R.id.tvCartCounPoint)as TextView
        var tvRedeem = itemView.findViewById(R.id.tvRedeem)as TextView
        var btnRedeem = itemView.findViewById(R.id.btnRedeem) as LinearLayoutCompat
        var clMain = itemView.findViewById(R.id.clMain) as ConstraintLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_redeem_coins, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datalist = adscartlist[position]
        applyButtonTheme(context, holder.btnRedeem)
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

        if (datalist?.variants?.get(0)?.inventoryQuantity!! > 0){
            holder.tvRedeem.text = context.getString(R.string.profile_str_50)
            holder.ivIcon.setImageDrawable(context.faDrawable(R.string.icon_redeem_coin, R.color.colorWhite))
            //holder.btnRedeem?.background = ContextCompat.getDrawable(context, R.drawable.corner_radius_18_bg_blue)
            applyButtonTheme(context, holder.btnRedeem)
        }else{
            holder.tvRedeem.text = context.getString(R.string.profile_str_54)
            holder.ivIcon.setImageDrawable(context.faDrawable(R.string.icon_out_of_stock, R.color.colorWhite))
            //holder.btnRedeem?.background = ContextCompat.getDrawable(context, R.drawable.corner_radius_18_bg_out_of_stock)
            setAppButton3(context, holder.btnRedeem)
        }

        if (!TextUtils.isEmpty(datalist?.variants?.get(0)?.price)){
            holder.clMain.setOnClickListener {
                btnRedeem.btnRedeem(datalist)
            }
            val coin = CommonUtils.covertNumberToCurrencyFormat(datalist?.variants?.get(0)?.price.toString().toDouble().toInt().toString())
            holder.coinpoint.text =  coin + " " + context.getString(R.string.profile_str_49)
        }else{
            holder.clMain.setOnClickListener(null)
            holder.coinpoint.visibility = View.GONE
        }

    }

    override fun getItemCount(): Int {
        return adscartlist.size
    }
    interface RedeemInterface{
        fun btnRedeem(datalist: ProductRespModel.Product?)
    }
}