package com.hungama.music.ui.main.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.data.model.InAppSelfHandledModel
import com.hungama.music.data.model.OnParentItemClickListener
import com.hungama.music.data.model.RowsItem
import com.hungama.music.utils.Constant
import kotlinx.android.synthetic.main.row_bucket.view.*

class NextInQueueAdapter (private var parents: List<RowsItem?>,
                          val context: Context,
                          val onParentItemClick: OnParentItemClickListener?,
                          val varient: Int = Constant.ORIENTATION_VERTICAL) :    RecyclerView.Adapter<NextInQueueAdapter.ViewHolder>(){
    private val viewPool = RecyclerView.RecycledViewPool()
    var isStoryUpdate = false
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_bucket,parent,false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return parents.size
    }

    fun addData(list: MutableList<RowsItem?>) {
        parents = list
        parents.forEach {
            if(it?.itype==null && it?.items?.size!!>0){
                it.itype = it.items?.get(0)?.itype
            }
        }
        Handler(Looper.getMainLooper()).post {
            notifyDataSetChanged()
        }
    }

    @SuppressLint("WrongConstant")
    override fun onBindViewHolder(holder: ViewHolder,
                                  position: Int) {
        val parent = parents[position]

        if(parent?.heading!=null){
            holder.tvTitle.text= parent.heading
            holder.tvTitle.visibility= View.VISIBLE
        }else{
            holder.tvTitle.visibility= View.GONE
        }

        if(parent?.more!=null && parent.more ==1){
            holder.ivMore.visibility= View.VISIBLE
        }else{
            holder.ivMore.visibility= View.GONE
        }

        if(parent?.subhead!=null){
            holder.tvSubTitle.text= parent.subhead
            holder.tvSubTitle.visibility= View.VISIBLE
        }else{
            holder.tvSubTitle.visibility= View.GONE
        }

        if(parent?.public != null){
            holder.switchPublic.isChecked = parent.public == 1
            holder.switchPublic.visibility = View.VISIBLE
        }
    }


    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val rvChildItem : RecyclerView = itemView.rvBucketItem
        val tvTitle: TextView = itemView.tvTitle
        val tvSubTitle: TextView = itemView.tvSubTitle
        val switchPublic: SwitchCompat = itemView.switchPublic
        val ivMore: ImageView = itemView.ivMore
    }

    private fun setChildRecyclerView(
        holder: ViewHolder,
        layoutManager2: GridLayoutManager,
        parent: RowsItem,
        position: Int
    ) {
        holder.rvChildItem.apply {
            layoutManager = layoutManager2
            adapter = BucketChildAdapter(context, parent.items!!, varient,
                object : BucketChildAdapter.OnChildItemClick {
                    override fun onUserClick(childPosition: Int, view: View?) {
                        if(onParentItemClick!=null){
                            onParentItemClick.onParentItemClick(parent,position,childPosition)
                        }
                    }

                    override fun onInAppSubmitClick(
                        childPosition: InAppSelfHandledModel?,
                        position: Int
                    ) {

                    }
                })
            setRecycledViewPool(viewPool)
            setHasFixedSize(true)
        }
    }
}