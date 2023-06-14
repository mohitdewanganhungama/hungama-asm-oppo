package com.hungama.music.ui.main.adapter

import android.content.Context
import android.text.TextUtils
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.data.model.EarnCoinModel
import com.hungama.music.utils.CommonUtils

class EarnCoinAdapter(val context:Context,val BtnClicked: btnClicked):RecyclerView.Adapter<EarnCoinAdapter.ViewHolder>() {

    var earnCoinList = emptyList<EarnCoinModel>()

    internal fun setearnCoindata(data:List<EarnCoinModel>){
        earnCoinList = data
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView) {
        var ivcoin = itemView.findViewById(R.id.tvCoincount)as TextView
        var tvTitle = itemView.findViewById(R.id.tvTitle)as TextView
        var tvSubTitle = itemView.findViewById(R.id.tvSubTitle)as TextView
        var layout  = itemView.findViewById(R.id.llComplete)as ConstraintLayout
        var btnEarn = itemView.findViewById(R.id.btnEarn)as LinearLayoutCompat
        var btnNext = itemView.findViewById(R.id.btnNext)as LinearLayoutCompat
        var clMain  = itemView.findViewById(R.id.clMain) as ConstraintLayout
        var border = itemView.findViewById(R.id.border) as View
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.row_earn_coin,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val coindata = earnCoinList[position]

        if (position == 0){
            holder.btnEarn.visibility = View.INVISIBLE
            holder.btnNext.visibility = View.VISIBLE
            holder.clMain.background = ContextCompat.getDrawable(context, R.drawable.bg_profile_subscription)
        }else{
            holder.btnEarn.visibility = View.VISIBLE
            holder.btnNext.visibility = View.GONE
            holder.clMain.background = null
        }

        CommonUtils.applyButtonTheme(context, holder.btnEarn)
        if (!TextUtils.isEmpty(coindata.coin)){
            holder.ivcoin.text = coindata.coin
        }else{
            holder.ivcoin.visibility = View.GONE
        }
        holder.tvTitle.text = coindata.title
        holder.tvSubTitle.text = coindata.SubTitle

        if (!earnCoinList.isNullOrEmpty() && position == earnCoinList.size - 1){
            holder.layout.visibility = View.VISIBLE
            holder.border.visibility = View.GONE
        }

        holder.btnEarn.setOnClickListener {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(context, holder.btnEarn!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            if (BtnClicked != null){
                BtnClicked.btnEarn()
            }
        }
    }

    override fun getItemCount(): Int {
        return earnCoinList.size
    }
    interface btnClicked{
        fun btnEarn()
    }
}