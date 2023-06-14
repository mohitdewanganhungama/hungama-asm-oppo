package com.hungama.music.ui.main.adapter

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.databinding.RowItype6Binding
import com.hungama.music.data.model.CollectionDetailModel

class BrandHubShortFilmAdapter(
    val context: Context,
    val onitemclick: OnItemClick?
) :
    RecyclerView.Adapter<BrandHubShortFilmAdapter.ItemViewHolder>() {

    private val ROW_TYPE_1 = 1
    private val ROW_TYPE_2 = 2
    var arrayList = emptyList<CollectionDetailModel>()

    fun addData(list: List<CollectionDetailModel>) {
        arrayList = list
        Handler(Looper.getMainLooper()).post {
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding: RowItype6Binding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.row_itype_6,
            parent,
            false
        )
        return ItemViewHolder(binding)

    }


    class ItemViewHolder(val binding: RowItype6Binding) :
        RecyclerView.ViewHolder(binding.root)


    override fun getItemCount(): Int {
//        return arrayList?.size!!
        return 5
    }

    override fun onBindViewHolder(holder: ItemViewHolder, pos: Int) {
//        holder.bind(holidayList.get(pos))
//        if (arrayList?.get(holder.adapterPosition)?.data?.title != null) {
//            holder.binding.tvTitle.text = arrayList?.get(holder.adapterPosition)?.data?.title
//            holder.binding.tvTitle.visibility = View.VISIBLE
//        } else {
//            holder.binding.tvTitle.visibility = View.GONE
//        }
//
//        if (arrayList?.get(holder.adapterPosition)?.data?.subtitle != null) {
//            holder.binding.tvSubTitle.text =
//                arrayList?.get(holder.adapterPosition)?.data?.subtitle
//            holder.binding.tvSubTitle.visibility = View.VISIBLE
//        } else {
//            holder.binding.tvSubTitle.visibility = View.GONE
//        }
//
//        if (arrayList?.get(holder.adapterPosition)?.data?.misc?.movierights != null&&arrayList?.get(holder.adapterPosition)?.data?.misc?.movierights?.size!!>0) {
//            Utils.setMovieRightTextForBucket(holder.binding.txtRent,arrayList?.get(holder.adapterPosition)?.data?.misc?.movierights!!,context, arrayList?.get(holder.adapterPosition)?.data?.id.toString())
//        } else {
//            holder.binding.txtRent.visibility = View.GONE
//        }
//
//        if (arrayList?.get(holder.adapterPosition)?.data?.misc?.ratingCritic != null&&arrayList?.get(holder.adapterPosition)?.data?.misc?.ratingCritic!!>0) {
//            holder.binding.txtRating.text =
//                ""+arrayList?.get(holder.adapterPosition)?.data?.misc?.ratingCritic?.toDouble()
//            holder.binding.txtRating.visibility = View.VISIBLE
//        } else {
//            holder.binding.txtRating.visibility = View.GONE
//        }
//
//        if (arrayList?.get(holder.adapterPosition)?.data?.image != null) {
//            ImageLoader.loadImage(
//                context,
//                holder.binding.ivUserImage,
//                arrayList?.get(holder.adapterPosition)?.data?.image!!,
//                R.drawable.bg_gradient_placeholder
//            )
//        }
//
//        holder.binding.llMain.setOnClickListener {
//            if (onitemclick != null) {
//                onitemclick.onUserClick(holder.adapterPosition)
//            }
//        }
    }//onBind
//
//    override fun getItemViewType(position: Int): Int {
//        return ROW_TYPE_1
//
//    }

    interface OnItemClick {
        fun onUserClick(position: Int)
    }
}