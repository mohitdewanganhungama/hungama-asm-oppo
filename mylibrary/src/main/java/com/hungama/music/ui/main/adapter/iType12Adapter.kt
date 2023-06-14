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
import com.hungama.music.databinding.RowItype12Binding
import com.hungama.music.data.model.BodyRowsItemsItem
import com.hungama.music.utils.ImageLoader


/**
 * Created by Chetan(chetan.patel@saeculumsolutions.com)
 * Copyright (c) by saeculumsolutions(www.saeculumsolutions.com)
 * Purpose: set user notification data
 */
class iType12Adapter(
    val context: Context,
    var arrayList: List<BodyRowsItemsItem?>,
    val onitemclick: OnItemClick?
) :
    RecyclerView.Adapter<iType12Adapter.ItemViewHolder>() {

    private val ROW_TYPE_1 = 1
    private val ROW_TYPE_2 = 2


    fun addData(list: List<BodyRowsItemsItem?>?) {
        if (list != null) {
            arrayList = list
        }
        Handler(Looper.getMainLooper()).post {
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding: RowItype12Binding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.row_itype_12,
            parent,
            false
        )
        return ItemViewHolder(binding)

    }


    class ItemViewHolder(val binding: RowItype12Binding) : RecyclerView.ViewHolder(binding.root)


    override fun getItemCount(): Int {
        return arrayList.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, pos: Int) {
//        holder.bind(holidayList.get(pos))

        if (arrayList.get(holder.adapterPosition)?.data?.title != null) {
            holder.binding.tvTitle.text = arrayList.get(holder.adapterPosition)?.data?.title
            holder.binding.tvTitle.visibility = View.VISIBLE
        } else {
            holder.binding.tvTitle.visibility = View.GONE
        }

        if (arrayList.get(holder.adapterPosition)?.data?.subTitle != null) {
            holder.binding.tvSubTitle.text =
                arrayList.get(holder.adapterPosition)?.data?.subTitle
            holder.binding.tvSubTitle.visibility = View.VISIBLE
        } else {
            holder.binding.tvSubTitle.visibility = View.GONE
        }

        if (arrayList.get(holder.adapterPosition)?.data?.image != null) {
//            Glide.with(context)
//                .asBitmap()
//                .load(arrayList?.get(holder.adapterPosition)?.data?.image)
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .placeholder(R.drawable.bg_gradient_placeholder)
//                .listener(object : RequestListener<Bitmap?> {
//                    override fun onLoadFailed(
//                        e: GlideException?,
//                        model: Any?,
//                        target: Target<Bitmap?>?,
//                        isFirstResource: Boolean
//                    ): Boolean {
//                       return false
//                    }
//
//                    override fun onResourceReady(
//                        resource: Bitmap?,
//                        model: Any?,
//                        target: Target<Bitmap?>?,
//                        dataSource: DataSource?,
//                        isFirstResource: Boolean
//                    ): Boolean {
//                        if (resource != null) {
//                            Palette.from(resource!!).generate(PaletteAsyncListener {
//                                val startColor=it?.getDominantColor(ContextCompat.getColor(context!!, R.color.colorStartGradiant))
//                                val endColor=it?.getDarkVibrantColor(ContextCompat.getColor(context!!, R.color.colorEndGradiant))
//
//                                val gradientDrawable = GradientDrawable(
//                                    GradientDrawable.Orientation.TOP_BOTTOM,
//                                    intArrayOf(
//                                        startColor!!,
//                                        endColor!!)
//                                );
//                                gradientDrawable.cornerRadius = 0.5f;
//
//
//                               holder?.binding.tvHeightLightContent.setBackgroundDrawable(gradientDrawable)
//                            })
//
//                            holder.binding.ivUserImage.setImageBitmap(resource)
//                        }
//                        return true
//                    }
//                }).into(holder?.binding?.ivUserImage)

            ImageLoader.loadImage(
                context,
                holder.binding.ivUserImage,
                arrayList.get(holder.adapterPosition)?.data?.image!!,
                R.drawable.bg_gradient_placeholder
            )
        } else {
            if (arrayList.get(holder.adapterPosition)?.data?.title != null) {
                holder.binding.tvTitlePlaceHolder.text =
                    arrayList.get(holder.adapterPosition)?.data?.title
                holder.binding.tvTitlePlaceHolder.visibility = View.VISIBLE
            } else {
                holder.binding.tvTitlePlaceHolder.visibility = View.GONE
            }
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