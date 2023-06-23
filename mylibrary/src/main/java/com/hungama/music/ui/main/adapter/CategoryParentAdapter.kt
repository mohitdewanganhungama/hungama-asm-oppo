package com.hungama.music.ui.main.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.hungama.music.data.model.BodyRowsItemsItem
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import com.hungama.music.data.model.OnParentItemClickListener
import com.hungama.music.data.model.RowsItem
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant
import com.hungama.music.utils.Utils
import kotlinx.android.synthetic.main.row_bucket.view.*
import com.hungama.music.utils.customview.scrollingpagerindicator.ScrollingPagerIndicator

class CategoryParentAdapter(
    private var parents: List<RowsItem?>,
    val context: Context,
    val onParentItemClick: OnParentItemClickListener?,
    val onMoreItemClick: OnMoreItemClick,
    val varient: Int
) : RecyclerView.Adapter<CategoryParentAdapter.ViewHolder>() {
    private val viewPool = RecyclerView.RecycledViewPool()
    var isStoryUpdate = false
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_bucket, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return parents.size
    }


    fun addData(list: MutableList<RowsItem?>) {
        parents = list
        parents.forEach {
            if (it != null && it?.itype == null && !it?.items.isNullOrEmpty() && it?.items?.size!! > 0) {
                it.itype = it.items!!.get(0)?.itype
            }
        }

        Handler(Looper.getMainLooper()).post {
            notifyDataSetChanged()
        }

    }

    @SuppressLint("WrongConstant")
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val parent = parents[position]
        setLog("ParentAdapter", "onBindViewHolder: position: $position, heading: ${parent?.heading}, itype: ${parent?.itype}")

        if (parent?.heading != null&&!TextUtils.isEmpty(parent?.heading!!) && parent?.items?.size!! > 0) {
            if (!TextUtils.isEmpty(parent.identifier.toString()) && parent.identifier == 1){
                CommonUtils.getDayGreetings(context)
                if (TextUtils.isEmpty(SharedPrefHelper.getInstance().getUsername())){

                    holder.tvTitle.text =  CommonUtils.greeting.value + " Guest"
                }else{
                    holder.tvTitle.text = CommonUtils.greeting.value + " " + SharedPrefHelper.getInstance().getUsername()
                }

                holder.tvTitle.visibility = View.VISIBLE
            }else{
                holder.tvTitle.text = parent.heading
                holder.tvTitle.visibility = View.VISIBLE
            }
        } else {
            holder.tvTitle.visibility = View.GONE
        }

        if (parent?.more != null && parent.more == 1 && parent?.items?.size!! > 1&&parents?.size!!>1) {
                holder.ivMore.visibility = View.VISIBLE
                holder.ivMore.setOnClickListener {
                    if (onMoreItemClick != null){
                        if (parent?.heading != null&&!TextUtils.isEmpty(parent?.heading!!)) {
                            onMoreItemClick.onMoreClick(parent)
                        }

                    }
                }
                holder.llHeaderTitle.setOnClickListener {
                    if (onMoreItemClick != null){
                        if (parent?.heading != null&&!TextUtils.isEmpty(parent?.heading!!)) {
                            onMoreItemClick.onMoreClick(parent)
                        }

                    }
                }
        } else {
            holder.ivMore.visibility = View.GONE
        }

        if (parent?.subhead != null&&!TextUtils.isEmpty(parent?.subhead!!) && parent?.items?.size!! > 0) {
            holder.tvSubTitle.text = parent.subhead
            holder.tvSubTitle.visibility = View.VISIBLE
        } else {
            holder.tvSubTitle.visibility = View.GONE
        }

        if (parent?.public != null) {
            holder.switchPublic.isChecked = parent.public == 1
            holder.switchPublic.visibility = View.VISIBLE
        }

        if (parent?.items != null && parent?.items?.size!! > 0) {
            holder.rvChildItem.visibility = View.VISIBLE
            holder.dotedView.visibility = View.GONE

            var layoutManager: GridLayoutManager? = null
            if (varient == Constant.ORIENTATION_VERTICAL){
                layoutManager =
                    GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
            }else{
                if (parent.numrow != null) {
                    layoutManager = GridLayoutManager(
                        context,
                        parent.numrow!!,
                        GridLayoutManager.HORIZONTAL,
                        false
                    )
                } else {
                    layoutManager =
                        GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false)
                }
            }
            setChildRecyclerView(holder, layoutManager, parent, position)
            holder.rvChildItem.setPadding(context.resources.getDimensionPixelSize(R.dimen.dimen_14),0,0,0)
            Utils.setMargins(holder.llHeaderTitle, context.resources.getDimensionPixelSize(R.dimen.dimen_10))
            Utils.setMarginsTop(
                holder.rvChildItem,
                context.resources.getDimensionPixelSize(R.dimen.dimen_0)
            )
            Utils.setMarginsTop(
                holder.llHeaderTitle,
                context.resources.getDimensionPixelSize(R.dimen.common_two_bucket_space_detail_page)
            )

            if (parent.itype == 1) {
                if (!isStoryUpdate) {
                    val allStories = CommonUtils.getDataFromJson(context, position, 0)
                    if(allStories!=null&&allStories.size>0){
                        for (i in allStories?.indices!!) {
                            if(i>=parent?.items?.size!!){

                            }else{
                                //parent?.items?.get(i)!!.data!!.viewInex = allStories.get(i).viewInex
                                //parent?.items?.get(i)!!.data!!.stories = allStories.get(i).stories
                            }

                        }
                    }

                }
            } else if (parent.itype == 2) {

            } else if (parent.itype == 3) {

            } else if (parent.itype == 4) {

            } else if (parent.itype == 5) {

            } else if (parent.itype == 6) {

            } else if (parent.itype == 7) {

            } else if (parent.itype == 8) {

            } else if (parent.itype == 9) {

            } else if (parent.itype == 10) {

            } else if (parent.itype == 11) {

            } else if (parent.itype == 12) {

            } else if (parent.itype == 13) {

            } else if (parent.itype == 14) {

            } else if (parent.itype == 15) {

            } else if (parent.itype == 16) {

            } else if (parent.itype == 17) {

            } else if (parent.itype == 18) {

            } else if (parent.itype == 19) {

            } else if (parent.itype == 20) {

            } else if (parent.itype == 21) {

                var layoutManager: GridLayoutManager? = null
                if (parent.numrow != null) {
                    layoutManager = GridLayoutManager(
                        context,
                        parent.numrow!!,
                        GridLayoutManager.HORIZONTAL,
                        false
                    )
                } else {
                    layoutManager =
                        GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false)
                }
                holder.rvChildItem.visibility = View.GONE
                holder.dotedView.visibility = View.VISIBLE
                //setChildRecyclerView(holder, layoutManager, parent, position)

                setLog("TAG", "onBindViewHolder Itype23PagerAdapter:111 ")
                val pagerAdapter = Itype21PagerAdapter(parent, context,
                    object : Itype21PagerAdapter.OnChildItemClick {
                    override fun onUserClick(childPosition: Int) {
                        if (onParentItemClick != null) {
                            onParentItemClick.onParentItemClick(parent, position, childPosition)
                        }
                    }

                })
                holder.pager.adapter = pagerAdapter
                holder.pagerIndicator.attachToPager(holder.pager)

                Utils.setMargins(holder.llMain, 0)
                Utils.setMargins(
                    holder.llHeaderTitle,
                    context.resources.getDimensionPixelSize(R.dimen.dimen_12)
                )
                Utils.setMarginsTop(
                    holder.llHeaderTitle,
                    context.resources.getDimensionPixelSize(R.dimen.common_two_bucket_space_detail_page)
                )
            } else if (parent.itype == 22) {

            } else if (parent.itype == 23) {
                var layoutManager: GridLayoutManager? = null
                if (parent.numrow != null) {
                    layoutManager = GridLayoutManager(
                        context,
                        parent.numrow!!,
                        GridLayoutManager.HORIZONTAL,
                        false
                    )
                } else {
                    layoutManager =
                        GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false)
                }
                holder.rvChildItem.visibility = View.GONE
                holder.dotedView.visibility = View.VISIBLE
                //setChildRecyclerView(holder, layoutManager, parent, position)

                val pagerAdapter =
                    Itype23PagerAdapter(parent,
                        context,
                        object :
                            Itype23PagerAdapter.OnChildItemClick {
                            override fun onUserClick(childPosition: Int) {
                                if (onParentItemClick != null) {
                                    onParentItemClick.onParentItemClick(
                                        parent,
                                        position,
                                        childPosition
                                    )
                                }
                            }

                        })

                holder.pager.adapter = pagerAdapter
                holder.pagerIndicator.attachToPager(holder.pager)

                Utils.setMargins(holder.llMain, 0)
                Utils.setMargins(
                    holder.llHeaderTitle,
                    context.resources.getDimensionPixelSize(R.dimen.dimen_12)
                )
                Utils.setMarginsTop(
                    holder.llHeaderTitle,
                    context.resources.getDimensionPixelSize(R.dimen.common_two_bucket_space_detail_page)
                )
            }else if (parent.itype == 41) {

            }  else if (parent.itype == 42) {

            }else if (parent.itype == 43) {

            }else {
                setLog("NoView", "NoVIew 2")
                holder.rvChildItem.visibility = View.GONE
            }

            if (position == 0){
                Utils.setMarginsTop(
                    holder.llHeaderTitle,
                    context.resources.getDimensionPixelSize(R.dimen.dimen_0)
                )
            }
        } else {
            setLog("NoView", "NoVIew 1")
            holder.rvChildItem.visibility = View.GONE
        }
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rvChildItem: RecyclerView = itemView.rvBucketItem
        val tvTitle: TextView = itemView.tvTitle
        val tvSubTitle: TextView = itemView.tvSubTitle
        val switchPublic: SwitchCompat = itemView.switchPublic
        val ivMore: LinearLayoutCompat = itemView.ivMore
        val llMain: LinearLayoutCompat = itemView.llMain
        val llHeaderTitle: LinearLayoutCompat = itemView.llHeaderTitle
        val dotedView = itemView.dotedView
        val pager: ViewPager = itemView.pager
        val pagerIndicator: ScrollingPagerIndicator = itemView.pager_indicator

    }


    private fun setChildRecyclerView(
        holder: ViewHolder,
        layoutManager2: GridLayoutManager,
        parent: RowsItem,
        position: Int
    ) {
        holder.rvChildItem.apply {
            layoutManager = layoutManager2
            adapter = MoreBucketChildAdapter(context, parent.items!!, varient,
                object : MoreBucketChildAdapter.OnChildItemClick {
                    override fun onUserClick(childPosition: Int) {
                        if (onParentItemClick != null) {
                            CommonUtils.setLog(
                                "CategoryDetailPage",
                                "CategoryParentChildAdapter-onUserClick"
                            )
                            onParentItemClick.onParentItemClick(parent, position, childPosition)
                        }
                    }

                })
//            setRecycledViewPool(viewPool)
            setHasFixedSize(true)
        }
    }
    /*
    fun updateList(data: ArrayList<BodyDataItem>?, parentPosition: Int) {

        for (stories in data!!.indices) {
            this.parents.get(parentPosition)!!.items!!.get(stories)!!.data!!.viewInex =
                data[stories].viewInex
        }
        isStoryUpdate = true
        notifyDataSetChanged()
    }
    */

    interface OnMoreItemClick {
        fun onMoreClick(selectedMoreBucket: RowsItem?)
    }
}