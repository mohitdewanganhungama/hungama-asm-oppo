package com.hungama.music.ui.main.adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.data.model.UserOrdersModel
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.DateUtils
import com.hungama.music.utils.ImageLoader

class MyOrderListAdapter(val context:Context,var showOrderDeail: OrderDetail):RecyclerView.Adapter<MyOrderListAdapter.ViewHolder> (){

    var userOrderList:ArrayList<UserOrdersModel.Order> = ArrayList()

    internal fun setUserOrderData(userOrderList:ArrayList<UserOrdersModel.Order>){
        this.userOrderList = userOrderList
        notifyDataSetChanged()
    }

   inner class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        var image = itemView.findViewById(R.id.ivMyOrder)as ImageView
        var title = itemView.findViewById(R.id.tvMyOrderTitle)as TextView
        var orderupdate = itemView.findViewById(R.id.tvOrderUpdate)as TextView
        var orderdatetext = itemView.findViewById(R.id.tvOrderDate)as TextView
        var orderdate  = itemView.findViewById(R.id.tvDateFormate)as TextView
        var coinredeem = itemView.findViewById(R.id.tvCoinRedeem)as TextView
        var coinCount = itemView.findViewById(R.id.tvTotalCoin)as TextView
        var pDelivery = itemView.findViewById(R.id.tvProductDelivery)as TextView
        var business =  itemView.findViewById(R.id.tvBusinessDay)as TextView
        var vdevider  = itemView.findViewById(R.id.vOrlderListDevider2)as View
       var clMain = itemView.findViewById(R.id.clMain) as ConstraintLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_my_orders,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val orderData = userOrderList?.get(position)
        //holder.image.setImageResource(orderData.image)
        if (!orderData?.lineItems.isNullOrEmpty()){
            holder.title.text = orderData?.lineItems?.get(0)?.name
            val coin = CommonUtils.covertNumberToCurrencyFormat(orderData?.lineItems?.get(0)?.priceSet?.shopMoney?.amount?.toDouble()?.toInt().toString())
            holder.coinCount.text = coin
            if (orderData?.lineItems?.get(0)?.image != null && !TextUtils.isEmpty(orderData?.lineItems?.get(0)?.image?.src.toString())) {
                ImageLoader.loadImage(context,
                    holder.image,
                    orderData.lineItems.get(0).image?.src.toString(),
                    R.drawable.bg_gradient_placeholder
                )
            }
        }
        val strDate = DateUtils.convertDate(DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_WITH_TX, DateUtils.DATE_FORMAT_DD_MMMM_YYYY, orderData?.createdAt)
        val strTime = DateUtils.convertDate(DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_WITH_TX, DateUtils.DATE_FORMAT_hh_mm_A, orderData?.createdAt)
        holder.orderupdate.text = "Processing"
        holder.orderdate.text = strDate + " "+ context.getString(R.string.at) + " " + strTime

        holder.business.text = "7 - 10 business days"

        /*if (holder.position == userOrderList?.size!! - 1){
            holder.pDelivery.visibility = View.GONE
            holder.business.visibility = View.GONE
        }
        if (holder.position == userOrderList?.size!! -2){
            holder.pDelivery.visibility = View.GONE
            holder.business.visibility = View.GONE
        }*/
        holder.clMain.setOnClickListener {
            orderData?.let { it1 -> showOrderDeail.ShowOrderDetail(it1,position) }
        }
    }

    override fun getItemCount(): Int {
        return userOrderList.size
    }
    interface OrderDetail{
        fun ShowOrderDetail(orderData: UserOrdersModel.Order, position: Int)
    }
}