package com.hungama.music.ui.main.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.data.model.PaymentConformationModel

class PaymentConformationAdapter(val context: Context):RecyclerView.Adapter<PaymentConformationAdapter.ViewHolder>() {
    var mlist = emptyList<PaymentConformationModel>()
    internal fun setOfferList(list:List<PaymentConformationModel>){
        mlist = list
        notifyDataSetChanged()
    }
    class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView) {
        var imageview = itemView.findViewById(R.id.ivOrderConformation)as ImageView
        var title = itemView.findViewById(R.id.tvTitle)as TextView
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.row_payment_conformation,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var datalist = mlist[position]
        holder.imageview.setImageResource(datalist.image)
        holder.title.text = datalist.Title
    }

    override fun getItemCount(): Int {
        return mlist.size
    }
}