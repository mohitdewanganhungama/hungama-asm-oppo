package com.hungama.music.ui.main.adapter

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hungama.music.R
import com.hungama.music.model.LangItem
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.Constant
import kotlinx.android.synthetic.main.row_language.view.*

class LanguageGridRecyclerAdapter(val ctx: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var listOfLanguage = ArrayList<LangItem>()
    var itemClick: ((LangItem) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        //return LanguageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_language, parent, false))

        return LanguageViewHolder(ctx, LayoutInflater.from(parent.context).inflate(R.layout.row_language, parent, false)).apply {
            itemClick = { languageModel ->
                this@LanguageGridRecyclerAdapter.itemClick?.invoke(languageModel)
            }
        }
    }

    override fun getItemCount(): Int = listOfLanguage.size

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val languageViewHolder = viewHolder as LanguageViewHolder
        val item = listOfLanguage[position]
        languageViewHolder.bindView(item)
    }

    fun setLanguageList(listOfLanguage: ArrayList<LangItem>) {
        this.listOfLanguage = listOfLanguage
        Handler(Looper.getMainLooper()).post {
            notifyDataSetChanged()
        }

    }

    class LanguageViewHolder(val ctx: Context, itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemClick: ((LangItem) -> Unit)? = null
        var margin = 0.0
        var marginTop = 0.0
        var marginBottom = 0.0
        var marginStart = 0.0
        var marginEnd = 0.0
        var itemPadding = 0.0
        var itemPaddingTop = 0.0
        var itemPaddingBottom = 0.0
        var itemPaddingStart = 0.0
        var itemPaddingEnd = 0.0
        var parentStartSpacing = 0.0
        var noOfColums = 0.0
        var itemWidth = 0.0
        var lineOne = 0.0
        var lineTwo = 0.0
        var lineThree = 0.0
        var textSize = 0.0
        var imageHeightByAspectRatio = 0.0
        var itemHeight = 0.0
        private var maxScreenWidth: Int = 0
        fun bindView(languageModel: LangItem) {
            marginStart = 0.0
            marginEnd = ctx.resources.getDimensionPixelSize(R.dimen.dimen_20).toDouble()
            marginTop = ctx.resources.getDimensionPixelSize(R.dimen.dimen_20).toDouble()
            marginBottom = 0.0
            itemPaddingStart = 0.0
            itemPaddingEnd = 0.0
            itemPaddingTop = 0.0
            itemPaddingBottom = 0.0

            noOfColums = 2.0
            maxScreenWidth = CommonUtils.getDeviceWidth(ctx)
            parentStartSpacing = ctx.resources.getDimensionPixelSize(R.dimen.dimen_18).toDouble() / noOfColums
            itemWidth = (maxScreenWidth / noOfColums) - marginStart - marginEnd - itemPaddingStart - itemPaddingEnd - parentStartSpacing
            lineOne = 0.0
            lineTwo = 0.0
            lineThree = 0.0
            textSize = lineOne + lineTwo + lineThree
            imageHeightByAspectRatio = itemWidth * 9 / 16
            itemHeight = imageHeightByAspectRatio + textSize + marginTop + marginBottom + itemPaddingTop + itemPaddingBottom
            itemView.itemView?.layoutParams?.height = imageHeightByAspectRatio.toInt()
            itemView.itemView?.layoutParams?.width = itemWidth.toInt()
            itemView.itemView?.requestLayout()
            itemView.imageMain?.layoutParams?.height = imageHeightByAspectRatio.toInt()
            itemView.imageMain?.layoutParams?.width = itemWidth.toInt()
            itemView.imageMain?.requestLayout()
            Glide.with(itemView.context).load(languageModel.image).into(itemView.imageMain)
            if(languageModel.isSelected){
                itemView.imageRadio.visibility = View.VISIBLE
            }else{
                itemView.imageRadio.visibility = View.GONE
            }

            itemView.setOnClickListener {
                //invoke() function will pass the value to receiver function.
                itemClick?.invoke(languageModel)
            }
            //Glide.with(itemView.context).load(movieModel.moviePicture!!).into(itemView.imageMovie)
        }

    }
}