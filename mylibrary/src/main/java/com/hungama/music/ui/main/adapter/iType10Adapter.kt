package com.hungama.music.ui.main.adapter

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.databinding.RowItype10Binding
import com.hungama.music.data.model.BodyRowsItemsItem

/**
 * Created by Chetan(chetan.patel@saeculumsolutions.com)
 * Copyright (c) by saeculumsolutions(www.saeculumsolutions.com)
 * Purpose: set user notification data
 */
class iType10Adapter(
    val context: Context,
    var arrayList: List<BodyRowsItemsItem?>?,
    val onitemclick: OnItemClick?
) :
    RecyclerView.Adapter<iType10Adapter.ItemViewHolder>() {

    private val ROW_TYPE_1 = 1
    private val ROW_TYPE_2 = 2


    fun addData(list: List<BodyRowsItemsItem?>?) {
        arrayList = list
        Handler(Looper.getMainLooper()).post {
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding: RowItype10Binding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.row_itype_10,
            parent,
            false
        )
        return ItemViewHolder(binding)

    }


    class ItemViewHolder(val binding: RowItype10Binding) :
        RecyclerView.ViewHolder(binding.root)


    override fun getItemCount(): Int {
        return arrayList?.size!!
    }

    override fun onBindViewHolder(holder: ItemViewHolder, pos: Int) {
//        holder.bind(holidayList.get(pos))

        if (arrayList?.get(holder.adapterPosition)?.data?.title != null) {
            holder.binding.tvTitle.text = arrayList?.get(holder.adapterPosition)?.data?.title
            holder.binding.tvTitle.visibility = View.VISIBLE
        } else {
            holder.binding.tvTitle.visibility = View.GONE
        }

        holder.binding.llMain.setOnClickListener {
            if (onitemclick != null) {
                onitemclick.onUserClick(holder.adapterPosition)
            }
        }

    }//onBind

    override fun getItemViewType(position: Int): Int {
        return ROW_TYPE_1

    }

    interface OnItemClick {
        fun onUserClick(position: Int)
    }
}