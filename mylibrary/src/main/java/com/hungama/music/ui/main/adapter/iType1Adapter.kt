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
import com.hungama.music.databinding.RowItype1Binding
import com.hungama.music.data.model.BodyRowsItemsItem
import com.hungama.music.utils.ImageLoader

/**
 * Created by Chetan(chetan.patel@saeculumsolutions.com)
 * Copyright (c) by saeculumsolutions(www.saeculumsolutions.com)
 * Purpose: set user notification data
 */
class iType1Adapter(
    val context: Context,
    var cakeList: List<BodyRowsItemsItem?>?,
    val onitemclick: OnItemClick?
) :
    RecyclerView.Adapter<iType1Adapter.ItemViewHolder>() {

    private val ROW_TYPE_1 = 1


    fun addData(list: List<BodyRowsItemsItem?>?) {
        cakeList = list
        Handler(Looper.getMainLooper()).post {
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding: RowItype1Binding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.row_itype_1,
            parent,
            false
        )
        return ItemViewHolder(binding)

    }


    class ItemViewHolder(val binding: RowItype1Binding) : RecyclerView.ViewHolder(binding.root)


    override fun getItemCount(): Int {
        return cakeList?.size!!
    }

    override fun onBindViewHolder(holder: ItemViewHolder, pos: Int) {
//        holder.bind(holidayList.get(pos))

        if (cakeList?.get(holder.adapterPosition)?.data?.title != null) {
            holder.binding.tvTitle.text = cakeList?.get(holder.adapterPosition)?.data?.title
            holder.binding.tvTitle.visibility = View.VISIBLE
        } else {
            holder.binding.tvTitle.visibility = View.GONE
        }

        if (cakeList?.get(holder.adapterPosition)?.data?.image != null) {
            ImageLoader.loadImage(
                context,
                holder.binding.ivUserImage,
                cakeList?.get(holder.adapterPosition)?.data?.image!!,
                R.drawable.bg_gradient_placeholder
            )
        }

        if (holder.adapterPosition == 3 || holder.adapterPosition == 4 || holder.adapterPosition == 5) {
            //holder.binding.ivUserImage.borderWidth = 0

        } else {
            //holder.binding.ivUserImage.borderWidth = 1
            //holder.binding.ivUserImage.borderColor = ContextCompat.getColor(context, R.color.red)
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