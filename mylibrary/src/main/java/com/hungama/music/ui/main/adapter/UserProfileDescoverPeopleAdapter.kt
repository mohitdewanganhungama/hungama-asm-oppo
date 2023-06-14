package com.hungama.music.ui.main.adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.data.model.ContactListModel
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.ImageLoader
import com.hungama.music.utils.fontmanger.FontDrawable

class UserProfileDescoverPeopleAdapter(
    context: Context,
    list: List<List<ContactListModel.Data?>?>,
    val onChildItemClick: OnChildItemClick?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ctx: Context = context
    var list: List<List<ContactListModel.Data?>?> = list

    private inner class UserProfileFollowerViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvInvitePeopleName: TextView = itemView.findViewById(R.id.tvInvitePeopleName)
        var tvInvitePeopleName2: TextView = itemView.findViewById(R.id.tvInvitePeopleName2)
        var tvInvitePeopleCount: TextView = itemView.findViewById(R.id.tvInvitePeopleCount)
        val ivInvitePeopleImage: ImageView = itemView.findViewById(R.id.ivInvitePeopleImage)
        val ivAddInvitePeople: ImageView = itemView.findViewById(R.id.ivAddInvitePeople)
        val llInvite: LinearLayoutCompat = itemView.findViewById(R.id.llInvite)
        val llMain: ConstraintLayout = itemView.findViewById(R.id.llMain)
        fun bind(position: Int) {


            val list = list!![position]!!

            if (!TextUtils.isEmpty(list?.get(0)?.name)) {
                tvInvitePeopleName.text = list?.get(0)?.name
                tvInvitePeopleName.visibility = View.VISIBLE
            } else {
                tvInvitePeopleName.visibility = View.GONE
            }

            if (!TextUtils.isEmpty(list?.get(0)?.uId) && list?.get(0)?.followerCount!! > 0) {
                tvInvitePeopleCount.text = CommonUtils.ratingWithSuffix(list?.get(0)?.followerCount?.toString()!!) + " " + ctx.getString(R.string.profile_str_3)
                tvInvitePeopleCount.visibility = View.VISIBLE
            } else {
                tvInvitePeopleCount.visibility = View.GONE
            }

            if (!TextUtils.isEmpty(list?.get(0)?.uId) && !TextUtils.isEmpty(list?.get(0)?.profileImage)) {
                ImageLoader.loadImage(
                    ctx,
                    ivInvitePeopleImage,
                    list?.get(0)?.profileImage!!,
                    R.drawable.bg_gradient_placeholder
                )
            }else if (!TextUtils.isEmpty(list?.get(0)?.uId) && !TextUtils.isEmpty(list?.get(0)?.alternateProfileImage)) {
                ImageLoader.loadImage(
                    ctx,
                    ivInvitePeopleImage,
                    list?.get(0)?.alternateProfileImage!!,
                    R.drawable.bg_gradient_placeholder
                )
            }
            if (!TextUtils.isEmpty(list?.get(0)?.uId)){
                if(list?.get(0)?.isAdded!!){
                    val drawable = FontDrawable(ctx, R.string.icon_tick)
                    drawable.setTextColor(ContextCompat.getColor(ctx, R.color.colorWhite))
                    ivAddInvitePeople.setImageDrawable(drawable)
                }else{
                    val drawable = FontDrawable(ctx, R.string.icon_follow)
                    drawable.setTextColor(ContextCompat.getColor(ctx, R.color.colorWhite))
                    ivAddInvitePeople.setImageDrawable(drawable)
                }
                llInvite?.visibility = View.GONE
                ivAddInvitePeople?.visibility = View.VISIBLE
            }else{
                ivAddInvitePeople?.visibility = View.GONE
                llInvite?.visibility = View.VISIBLE
            }

            ivAddInvitePeople.setOnClickListener {
                setFollowUnfollow(list.get(0), ivAddInvitePeople)
                if (onChildItemClick != null) {
                    onChildItemClick.onUserClick(position, true)
                }
            }
            llMain.setOnClickListener {
                if (onChildItemClick != null && !TextUtils.isEmpty(list?.get(0)?.uId) ) {
                    onChildItemClick.onUserClick(position, false)
                }
            }
            llInvite.setOnClickListener {
                if (onChildItemClick != null) {
                    onChildItemClick.onInviteClick(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return UserProfileFollowerViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_item_discover_people, parent, false)
            )
    }

    override fun getItemCount(): Int {
        return list!!.size
        //return 10
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as UserProfileFollowerViewHolder).bind(position)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    interface OnChildItemClick {
        fun onUserClick(childPosition: Int, isAddedClick:Boolean)
        fun onInviteClick(childPosition: Int)
    }

    fun setFollowUnfollow(data: ContactListModel.Data?, ivAddedFollowing: ImageView) {
        if (data?.isAdded!!){
            val drawable = FontDrawable(ctx, R.string.icon_follow)
            drawable.setTextColor(ContextCompat.getColor(ctx, R.color.colorWhite))
            ivAddedFollowing.setImageDrawable(drawable)
            data?.isAdded = false
        }else{
            val drawable = FontDrawable(ctx, R.string.icon_tick)
            drawable.setTextColor(ContextCompat.getColor(ctx, R.color.colorWhite))
            ivAddedFollowing.setImageDrawable(drawable)
            data?.isAdded = true
        }
    }
}