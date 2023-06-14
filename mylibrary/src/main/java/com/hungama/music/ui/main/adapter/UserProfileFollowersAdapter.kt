package com.hungama.music.ui.main.adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.data.model.UserSocialData
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.ImageLoader
import com.hungama.music.utils.fontmanger.FontDrawable

class UserProfileFollowersAdapter(
    context: Context,
    list: List<UserSocialData.Follower?>?,
    val onChildItemClick: OnChildItemClick?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ctx: Context = context
    var list: List<UserSocialData.Follower?>? = list

    private inner class UserProfileFollowerViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvFollowerName: TextView = itemView.findViewById(R.id.tvFollowerName)
        var tvFollowerCount: TextView = itemView.findViewById(R.id.tvFollowerCount)
        val ivFollowerImage: ImageView = itemView.findViewById(R.id.ivFollowerImage)
        val ivAddFollower: ImageView = itemView.findViewById(R.id.ivAddFollower)
        val ivAddedFollower: ImageView = itemView.findViewById(R.id.ivAddedFollower)
        val llMain: ConstraintLayout = itemView.findViewById(R.id.llMain)
        fun bind(position: Int) {

            val list = list!![position]!!

             var username = ""
            if (!TextUtils.isEmpty(list?.firstName)){
                username += list?.firstName + " "
            }
            if (!TextUtils.isEmpty(list?.lastName)){
                username += list?.lastName
            }

            if (!TextUtils.isEmpty(username)){
                tvFollowerName.text = username
                tvFollowerName.visibility = View.VISIBLE
            }else{
                if (!TextUtils.isEmpty(list?.handleName)) {
                    tvFollowerName.text = list.handleName
                    tvFollowerName.visibility = View.VISIBLE
                } else {
                    tvFollowerName.visibility = View.INVISIBLE
                }
            }

            if (!TextUtils.isEmpty(list?.followerCount.toString()) && list?.followerCount!! > 0) {
                tvFollowerCount.text = CommonUtils.ratingWithSuffix(list?.followerCount!!.toString()) + " " + ctx.getString(R.string.profile_str_3)
                tvFollowerCount.visibility = View.VISIBLE
            } else {
                tvFollowerCount.visibility = View.GONE
            }

            if (!TextUtils.isEmpty(list.profileImage)) {
                ImageLoader.loadImage(
                    ctx,
                    ivFollowerImage,
                    list.profileImage!!,
                    R.drawable.bg_gradient_placeholder
                )
            }else if (!TextUtils.isEmpty(list.alternateProfileImage)){
                ImageLoader.loadImage(
                    ctx,
                    ivFollowerImage,
                    list.alternateProfileImage!!,
                    R.drawable.bg_gradient_placeholder
                )
            }
            if(list.isAdded){
                val drawable = FontDrawable(ctx, R.string.icon_tick)
                drawable.setTextColor(ContextCompat.getColor(ctx, R.color.colorWhite))
                ivAddedFollower.setImageDrawable(drawable)
            }else{
                val drawable = FontDrawable(ctx, R.string.icon_follow)
                drawable.setTextColor(ContextCompat.getColor(ctx, R.color.colorWhite))
                ivAddedFollower.setImageDrawable(drawable)
            }

            ivAddedFollower.setOnClickListener {
                setFollowUnfollow(list, ivAddedFollower)
                if (onChildItemClick != null) {
                    onChildItemClick.onUserClick(position, true)
                }
            }
            llMain.setOnClickListener {
                if (onChildItemClick != null) {
                    onChildItemClick.onUserClick(position, false)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return UserProfileFollowerViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_item_followers, parent, false)
            )
    }

    override fun getItemCount(): Int {
        return list!!.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as UserProfileFollowerViewHolder).bind(position)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    interface OnChildItemClick {
        fun onUserClick(childPosition: Int, isAddedClick:Boolean)
    }

    fun setFollowUnfollow(data: UserSocialData.Follower, ivAddedFollowing: ImageView) {
        if (data.isAdded){
            val drawable = FontDrawable(ctx, R.string.icon_follow)
            drawable.setTextColor(ContextCompat.getColor(ctx, R.color.colorWhite))
            ivAddedFollowing.setImageDrawable(drawable)
            data.isAdded = false
        }else{
            val drawable = FontDrawable(ctx, R.string.icon_tick)
            drawable.setTextColor(ContextCompat.getColor(ctx, R.color.colorWhite))
            ivAddedFollowing.setImageDrawable(drawable)
            data.isAdded = true
        }
    }
}