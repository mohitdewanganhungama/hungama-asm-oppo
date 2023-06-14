package com.hungama.music.ui.main.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.data.model.PlaylistModel

class PurchaseTicketAdapter(
    context: Context,
    list: List<PlaylistModel.Data.Body.Similar?>?,
    val onChildItemClick: OnChildItemClick?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ctx: Context = context
    var list: List<PlaylistModel.Data.Body.Similar?>? = list

    private inner class IType12ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        fun bind(position: Int) {

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return IType12ViewHolder(
            LayoutInflater.from(ctx).inflate(R.layout.ticket_view, parent, false)
        )
    }

    override fun getItemCount(): Int {
        //return list!!.size
        return 5
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as IType12ViewHolder).bind(position)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    interface OnChildItemClick {
        fun onUserClick(childPosition: Int)
    }
}