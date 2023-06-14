package com.hungama.music.ui.main.adapter

import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.data.model.PurchasesEventsModel
import com.hungama.music.data.model.RentedMovieRespModel
import com.hungama.music.ui.main.view.fragment.LibraryRentedMovieFragment
import com.hungama.music.utils.CommonUtils.getFormattedDateWithSuffixedDay
import com.hungama.music.utils.CommonUtils.getRentedContentExpiryDate
import com.hungama.music.utils.CommonUtils.isUserHasRentedContent
import com.hungama.music.utils.CommonUtils.isUserHasRentedSubscription
import com.hungama.music.utils.DateUtils
import com.hungama.music.utils.DateUtils.convertStringToDate
import com.hungama.music.utils.ImageLoader
import com.hungama.music.utils.hide
import com.hungama.music.utils.show

class PurchasesRentedContentAdapter(var context:Context, val onMovieItemClick: OnMovieItemClick?):RecyclerView.Adapter<PurchasesRentedContentAdapter.ViewHolder>() {

    var eventList = ArrayList<RentedMovieRespModel.Data.Movie>()

    internal fun setEventList(eventData: ArrayList<RentedMovieRespModel.Data.Movie>){
        eventList = eventData
        notifyDataSetChanged()
    }

    class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        /*var eventTitle = itemView.findViewById(R.id.tvMovieTitle)as TextView
        var eventTime = itemView.findViewById(R.id.tvEventTime)as TextView
        var eventDate = itemView.findViewById(R.id.tvDate)as TextView
        var eventMonth = itemView.findViewById(R.id.tvMonth)as TextView
        var eventAmountPaid = itemView.findViewById(R.id.tvAmountPaid)as TextView
        var eventAmount = itemView.findViewById(R.id.tvEventAmount)as TextView
        var eventImage = itemView.findViewById(R.id.ivEvent)as ImageView
        var clMain = itemView.findViewById(R.id.clMain)as ConstraintLayout*/

        var title = itemView.findViewById(R.id.tvTitle) as TextView
        var tvSubtitle = itemView.findViewById(R.id.tvSubtitle) as TextView
        var tvSubscriptionDate = itemView.findViewById(R.id.tvSubscriptionDate) as TextView
        var tvSubscriptionStatus = itemView.findViewById(R.id.tvSubscriptionStatus) as TextView
        var ivContentImage = itemView.findViewById(R.id.ivContentImage) as ImageView
        var rlMoreInfo = itemView.findViewById(R.id.rlMoreInfo) as RelativeLayout
        var rlSubscripionStatus = itemView.findViewById(R.id.rlSubscripionStatus) as RelativeLayout
        var clMain = itemView.findViewById(R.id.clMain) as ConstraintLayout
        var cvBgView = itemView.findViewById(R.id.cvBgView) as ConstraintLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.row_rented_movies,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var eventData = holder?.adapterPosition?.let { eventList.get(it) }

        if (position % 2 == 0){
            holder.cvBgView.setBackgroundColor(0)
        }else{
            holder.cvBgView.setBackgroundColor(Color.parseColor("#33000000"))
        }
        holder.title.text = eventData?.data?.title
        holder.tvSubtitle.text = ""+eventData?.data?.subtitle
        /*val isRented = isUserHasRentedContent(eventData.data?.id)
        if (isRented){
            val expiryDate = getRentedContentExpiryDate(eventData.data?.id)
            if (isUserHasRentedSubscription(eventData.data?.id) && !TextUtils.isEmpty(expiryDate)){
                val dt = DateUtils.convertDate(DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_ss, DateUtils.DATE_FORMAT_DD_MMMM_YYYY, expiryDate)
                val date = convertStringToDate(dt, DateUtils.DATE_FORMAT_DD_MMMM_YYYY)
                val formattedDT = getFormattedDateWithSuffixedDay(date)
                holder.tvSubscriptionDate.text = context.getString(R.string.active_till)+" - $formattedDT"
                holder.tvSubscriptionDate.show()
                holder.rlSubscripionStatus.hide()
            }else{
                holder.tvSubscriptionDate.hide()
                holder.rlSubscripionStatus.show()
            }
        }else{
            holder.tvSubscriptionDate.hide()
            holder.rlSubscripionStatus.show()
        }*/
        if (eventData != null) {
            if (!eventData.isExpired){
                val dt = DateUtils.convertDate(DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_ss, DateUtils.DATE_FORMAT_DD_MMMM_YYYY, eventData?.endDate)
                val date = convertStringToDate(dt, DateUtils.DATE_FORMAT_DD_MMMM_YYYY)
                val formattedDT = getFormattedDateWithSuffixedDay(date)
                holder.tvSubscriptionDate.text = context.getString(R.string.active_till)+" - $formattedDT"
                holder.tvSubscriptionDate.show()
                holder.rlSubscripionStatus.hide()
            }else{
                holder.tvSubscriptionDate.hide()
                holder.rlSubscripionStatus.show()
            }
        }

        ImageLoader.loadImage(
            context,
            holder.ivContentImage,
            eventData?.data?.image!!,
            R.drawable.bg_gradient_placeholder
        )

        holder?.clMain?.setOnClickListener {
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