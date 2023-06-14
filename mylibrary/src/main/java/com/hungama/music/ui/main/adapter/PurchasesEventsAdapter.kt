package com.hungama.music.ui.main.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.data.model.PurchasesEventsModel
import com.hungama.music.data.model.RentedMovieRespModel
import com.hungama.music.ui.main.view.fragment.LibraryRentedMovieFragment
import com.hungama.music.utils.ImageLoader

class PurchasesEventsAdapter(var context:Context, val onMovieItemClick: OnMovieItemClick?):RecyclerView.Adapter<PurchasesEventsAdapter.ViewHolder>() {

    var eventList = ArrayList<RentedMovieRespModel.Data.Movie>()

    internal fun setEventList(eventData: ArrayList<RentedMovieRespModel.Data.Movie>){
        eventList = eventData
        notifyDataSetChanged()
    }

    class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        var eventTitle = itemView.findViewById(R.id.tvEventTitle)as TextView
        var eventTime = itemView.findViewById(R.id.tvEventTime)as TextView
        var eventDate = itemView.findViewById(R.id.tvDate)as TextView
        var eventMonth = itemView.findViewById(R.id.tvMonth)as TextView
        var eventAmountPaid = itemView.findViewById(R.id.tvAmountPaid)as TextView
        var eventAmount = itemView.findViewById(R.id.tvEventAmount)as TextView
        var eventImage = itemView.findViewById(R.id.ivEvent)as ImageView
        var clMain = itemView.findViewById(R.id.clMain)as ConstraintLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.ticket_view,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var eventData = holder?.adapterPosition?.let { eventList.get(it) }

        holder.eventTitle.text = eventData?.data?.title
        holder.eventTime.text = ""+eventData?.data?.duration
        holder.eventDate.text = eventData?.data?.releasedate
        holder.eventMonth.text = "March"
        holder.eventAmountPaid.text = ""+eventData?.data?.misc?.movierights
        holder.eventAmount.text = eventData?.data?.id
        ImageLoader.loadImage(
            context,
            holder.eventImage,
            eventData?.data?.image!!,
            R.drawable.bg_gradient_placeholder
        )

        holder.clMain.setOnClickListener {
            if (onMovieItemClick != null) {
                holder?.adapterPosition?.let { it1 -> onMovieItemClick.onItemClick(it1) }
            }
        }
    }

    override fun getItemCount(): Int {
        return eventList.size
    }

    interface OnMovieItemClick {
        fun onItemClick(childPosition: Int)
    }
}