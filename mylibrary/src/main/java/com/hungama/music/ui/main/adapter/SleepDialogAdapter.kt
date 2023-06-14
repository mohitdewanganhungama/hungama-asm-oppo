package com.hungama.music.ui.main.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.data.model.SleepDialogModel
import com.hungama.music.data.model.SpeedChangeDialogModel

class SleepDialogAdapter(var context: Context, val onSleepTimeChangeItemClick: OnSleepTimeChangeItemClick):RecyclerView.Adapter<SleepDialogAdapter.ViewHolder>(){
    var sleepList = emptyList<SleepDialogModel>()

    internal fun setdata(podlist:List<SleepDialogModel>){
        this.sleepList = podlist
        notifyDataSetChanged()
    }

    class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView) {
        var speedText = itemView.findViewById(R.id.tvTitle)as TextView
        var speedunderline = itemView.findViewById(R.id.vUnderline)as View
        var clMain = itemView.findViewById(R.id.clMain)as ConstraintLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.row_sleep,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var data = sleepList[position]
        holder.speedText.text = data.title
        if(holder.position == sleepList.size-1){
            holder.speedunderline.setVisibility(View.INVISIBLE)
        }
        holder.clMain.setOnClickListener {
            if (onSleepTimeChangeItemClick != null){
                onSleepTimeChangeItemClick?.onSleepTimeChangeItemClick(data)
            }
        }
    }

    override fun getItemCount(): Int {
        return sleepList.size
    }

    interface OnSleepTimeChangeItemClick {
        fun onSleepTimeChangeItemClick(data: SleepDialogModel)
    }
}