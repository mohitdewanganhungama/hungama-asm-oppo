package com.hungama.music.ui.main.adapter

import android.content.Context
import android.text.TextUtils
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.ads.interactivemedia.v3.internal.it
import com.hungama.music.R
import com.hungama.music.data.model.BodyRowsItemsItem
import com.hungama.music.databinding.RowItype51Binding
import com.hungama.music.data.model.VideoDetailModel
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.ImageLoader
import kotlin.math.log

class VideoSimilarAdapter(
    val context: Context,
    var arrayList: ArrayList<BodyRowsItemsItem?>?,
    val onitemclick: OnItemClick?
) :
    RecyclerView.Adapter<VideoSimilarAdapter.ItemViewHolder>() {

    private val ROW_TYPE_1 = 1
    private val ROW_TYPE_2 = 2


    fun addData(list: ArrayList<BodyRowsItemsItem?>?) {
        arrayList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        setLog("TAG", "onCreateViewHolder: "+viewType)
        val binding: RowItype51Binding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.row_itype_5_1,
            parent,
            false
        )
        return ItemViewHolder(binding)

    }


    class ItemViewHolder(val binding: RowItype51Binding) :
        RecyclerView.ViewHolder(binding.root)


    override fun getItemCount(): Int {
        return arrayList?.size!!
        //return 5
    }

    override fun onBindViewHolder(holder: ItemViewHolder, pos: Int) {

        if (arrayList?.get(holder.adapterPosition)?.data?.image != null) {
            ImageLoader.loadImage(
                context,
                holder.binding.ivUserImage,
                arrayList?.get(holder.adapterPosition)?.data?.image!!,
                R.drawable.bg_gradient_placeholder
            )
        }

        if (arrayList?.get(holder.adapterPosition)?.data?.title != null) {
            holder.binding.tvTitle.text = arrayList?.get(holder.adapterPosition)?.data?.title
            holder.binding.tvTitle.visibility = View.VISIBLE
        } else {
            holder.binding.tvTitle.visibility = View.GONE
        }

        if (arrayList?.get(holder.adapterPosition)?.data?.subTitle != null) {
            holder.binding.tvSubTitle.text =
                arrayList?.get(holder.adapterPosition)?.data?.subTitle
            holder.binding.tvSubTitle.visibility = View.VISIBLE
        } else {
            holder.binding.tvSubTitle.visibility = View.GONE
        }

        if (arrayList?.get(holder.adapterPosition)?.data?.misc?.lang != null) {
            holder.binding.tvLanguageView.visibility = View.GONE
        } else {
            holder.binding.tvLanguageView.visibility = View.GONE
        }

        if (!TextUtils.isEmpty(arrayList?.get(holder.adapterPosition)?.data?.duration!!)) {
            holder.binding.tvTime.text = ""+DateUtils.formatElapsedTime(arrayList?.get(holder.adapterPosition)?.data?.duration!!.toLong())
            holder.binding.tvTime.visibility = View.VISIBLE
        } else {
            holder.binding.tvTime.visibility = View.GONE
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