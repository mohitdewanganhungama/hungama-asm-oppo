package com.hungama.music.ui.main.adapter

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.data.model.ConnectedDialogModel
import com.hungama.music.utils.CommonUtils.faDrawable

class ConnectDeviceDialogAdapter(var context: Context, val onItemClicked:ConnectDeviceDialogAdapter.onItemClick):RecyclerView.Adapter<ConnectDeviceDialogAdapter.ViewHolder>() {

    //var connectedlist = emptyList<ConnectedDialogModel>()
    var connectedlist = ArrayList<BluetoothDevice>()

    internal fun setdata(connecteddata: ArrayList<BluetoothDevice>){
        this.connectedlist = connecteddata
        notifyDataSetChanged()
    }

    class ViewHolder(itemview:View):RecyclerView.ViewHolder(itemview) {
        var connectedimage = itemview.findViewById(R.id.iv)as ImageView
        var connectedText = itemview.findViewById(R.id.tvTitle)as TextView
        var connectedline = itemview.findViewById(R.id.vUnderline)as View
        var clMain = itemview.findViewById(R.id.clMain) as ConstraintLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.row_device_connect_dialog,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       var connectedData = connectedlist[position]
        /*holder.connectedimage.setImageDrawable(connectedData.icon?.let { context.faDrawable(it, R.color.colorWhite) })
        holder.connectedText.text = connectedData.Title
        if(holder.position == connectedlist.size - 1){
            holder.connectedline.setVisibility(View.INVISIBLE)
        }

        holder.clMain?.setOnClickListener {
            if (onItemClicked != null){
                onItemClicked.onItemClick(connectedData)
            }
        }*/
        holder.connectedimage.setImageDrawable(context.faDrawable(R.string.icon_bluetooth, R.color.colorWhite))
        holder.connectedText.text = connectedData.name
        holder.clMain?.setOnClickListener {
            if (onItemClicked != null) {
                onItemClicked.onPairClick(position);
            }
        }
    }

    override fun getItemCount(): Int {
        return connectedlist.size
    }

    interface onItemClick{
        fun onItemClick(connectedData: ConnectedDialogModel)
        fun onPairClick(position: Int)
    }
}