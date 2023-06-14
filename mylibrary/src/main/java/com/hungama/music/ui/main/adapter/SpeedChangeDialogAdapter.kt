package com.hungama.music.ui.main.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.data.model.SpeedChangeDialogModel

class SpeedChangeDialogAdapter(var context: Context, val onSpeedChangeItemClick: OnSpeedChangeItemClick):RecyclerView.Adapter<SpeedChangeDialogAdapter.ViewHolder>() {

    var speedchangelist = emptyList<SpeedChangeDialogModel>()

    internal fun setdata(speedchangedata : List<SpeedChangeDialogModel>){
        this.speedchangelist = speedchangedata
        notifyDataSetChanged()
    }

    class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView) {
        var speedText = itemView.findViewById(R.id.tvTitle)as TextView
        var speedunderline = itemView.findViewById(R.id.vUnderline)as View
        var clMain = itemView.findViewById(R.id.clMain)as ConstraintLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.row_change_speed,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var data = speedchangelist[position]
        holder.speedText.text = data.title
        if(holder.position == speedchangelist.size-1){
            holder.speedunderline.setVisibility(View.INVISIBLE)
        }
        holder.clMain.setOnClickListener {
            if (onSpeedChangeItemClick != null){
                onSpeedChangeItemClick?.onSpeedChangeItemClick(data)
            }
        }

    }

    override fun getItemCount(): Int {
      return speedchangelist.size
    }

    interface OnSpeedChangeItemClick {
        fun onSpeedChangeItemClick(data: SpeedChangeDialogModel)
    }
}