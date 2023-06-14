package com.hungama.music.ui.main.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.data.model.DeviceConnectDialogModel

class ConnectedDeviceDialogAdapter(var context: Context):RecyclerView.Adapter<ConnectedDeviceDialogAdapter.ViewHolder>() {
    var deviceconnectedlist = emptyList<DeviceConnectDialogModel>()

    internal fun setdata(deviceconnectdata : List<DeviceConnectDialogModel>){
        this.deviceconnectedlist = deviceconnectdata
        notifyDataSetChanged()
    }

    class ViewHolder(itemview : View):RecyclerView.ViewHolder(itemview) {
        var deviceconnectedimage = itemview.findViewById(R.id.iv)as ImageView?
        var deviceconnectedtitle = itemview.findViewById(R.id.tvTitle)as TextView?
        var deviceconnectedunderline = itemview.findViewById(R.id.vUnderline)as View?
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.row_device_connected_dialog,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var data  = deviceconnectedlist[position]
        holder.deviceconnectedimage?.setImageResource(data.deviceconnectimage)
        holder.deviceconnectedtitle?.text = data.deviceconnectimagetitle
        if (holder.position == deviceconnectedlist.size - 1){
            holder.deviceconnectedunderline?.setVisibility(View.INVISIBLE)
        }

    }

    override fun getItemCount(): Int {
        return deviceconnectedlist.size
    }
}