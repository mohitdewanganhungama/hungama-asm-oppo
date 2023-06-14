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
import com.hungama.music.databinding.RowItype2Binding
import com.hungama.music.data.model.BodyRowsItemsItem
import com.hungama.music.utils.Utils

/**
 * Created by Chetan(chetan.patel@saeculumsolutions.com)
 * Copyright (c) by saeculumsolutions(www.saeculumsolutions.com)
 * Purpose: set user notification data
 */
class iType9Adapter(
    val context: Context, val spanCount: Int,
    var arrayList: List<BodyRowsItemsItem?>,
    val onitemclick: OnItemClick?
) :
    RecyclerView.Adapter<iType9Adapter.ItemViewHolder>() {

    private val ROW_TYPE_1 = 1
    private val ROW_TYPE_2 = 2
    var itemWidth: Int = 100
    var itemHeight: Int = 100

    fun addData(list: List<BodyRowsItemsItem?>?) {
        if (list != null) {
            arrayList = list
        }
        Handler(Looper.getMainLooper()).post {
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding: RowItype2Binding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.row_itype_2,
            parent,
            false
        )
        val displayWidth: Int = Utils.getDisplayWidth(parent.context)
        val itemSpacing: Int =
            Utils.dpToPx(16, parent.context)
        itemWidth = (displayWidth - 3 * itemSpacing) / spanCount
        itemHeight = itemWidth * 16 / 9

        return ItemViewHolder(binding)

    }


    class ItemViewHolder(val binding: RowItype2Binding) : RecyclerView.ViewHolder(binding.root)


    override fun getItemCount(): Int {
        return arrayList.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, pos: Int) {
//        holder.bind(holidayList.get(pos))


        if (arrayList.get(holder.adapterPosition)?.data?.image != null) {
            //holder.binding.ivUserImage.setImageURLWithoutCalculatingSize(arrayList.get(holder.adapterPosition)?.data?.image!!,R.drawable.bg_gradient_placeholder)
        } else {
            if (arrayList.get(holder.adapterPosition)?.data?.title != null) {
                holder.binding.tvTitlePlaceHolder.text =
                    arrayList.get(holder.adapterPosition)?.data?.title
                holder.binding.tvTitlePlaceHolder.visibility = View.VISIBLE
            } else {
                holder.binding.tvTitlePlaceHolder.visibility = View.GONE
            }
        }
        holder.binding.tvTitle.visibility = View.GONE
        holder.binding.tvSubTitle.visibility = View.GONE

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