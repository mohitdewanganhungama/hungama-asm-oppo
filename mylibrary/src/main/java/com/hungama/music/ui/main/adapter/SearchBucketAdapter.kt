package com.hungama.music.ui.main.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.data.model.BodyRowsItemsItem
import com.hungama.music.R
import com.hungama.music.data.model.SearchBucketModel

class SearchBucketAdapter(var context: Context,var searchItem: SearchResult):RecyclerView.Adapter<SearchBucketAdapter.ViewHolder>() {

    var searchDataList = emptyList<SearchBucketModel>()

    internal fun setSearchlist(searchdata:List<SearchBucketModel>){
        searchDataList = searchdata
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView),View.OnClickListener{
        var image : ImageView
        var title : TextView
        var subTitle : TextView
        var ivback : ImageView
        init {

            itemView.setOnClickListener(this)

            image = itemView.findViewById(R.id.ivSearch)
            title = itemView.findViewById(R.id.tvTitle)
            subTitle = itemView.findViewById(R.id.tvSubTitle)
            ivback = itemView.findViewById(R.id.ivRowSearchBack)
        }

        override fun onClick(p0: View?){
            searchItem.SearchItemClick()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.row_search_item,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var searchdata = searchDataList[position]
        holder.image.setImageResource(searchdata.Image)
        holder.title.text = searchdata.Title
        holder.subTitle.text = searchdata.SubTitle
        if (holder.position == searchDataList.size -6){
            holder.ivback.setVisibility(View.INVISIBLE)
        }
        if (holder.position == searchDataList.size -3){
            holder.ivback.setVisibility(View.INVISIBLE)
        }
    }

    override fun getItemCount(): Int {
        return searchDataList.size
    }
    interface SearchResult{
        fun SearchItemClick()
    }
}