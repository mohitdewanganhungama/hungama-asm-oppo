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
import com.hungama.music.data.model.FacebookListModel
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.ImageLoader
import com.hungama.music.utils.fontmanger.FontDrawable

class UserProfileFindFriendsAdapter(
    context: Context,
    list: List<List<FacebookListModel.Data?>?>,
    val onChildItemClick: OnChildItemClick?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ctx: Context = context
    var list:  List<List<FacebookListModel.Data?>?> = list

    private inner class UserProfileFollowerViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvFindFriendName: TextView = itemView.findViewById(R.id.tvFindFriendName)
        var tvFindFriendCount: TextView = itemView.findViewById(R.id.tvFindFriendCount)
        val ivFindFriendImage: ImageView = itemView.findViewById(R.id.ivFindFriendImage)
        val ivAddFindFriend: ImageView = itemView.findViewById(R.id.ivAddFindFriend)
        val llMain: ConstraintLayout = itemView.findViewById(R.id.llMain)
        fun bind(position: Int) {

            val list = list[position]!!

            if (!TextUtils.isEmpty(list.get(0)?.name)) {
                tvFindFriendName.text = list.get(0)?.name
                tvFindFriendName.visibility = View.VISIBLE
            } else {
                tvFindFriendName.visibility = View.GONE
            }

            if (!TextUtils.isEmpty(list?.get(0)?.uId) && list?.get(0)?.followerCount!! > 0) {
                tvFindFriendCount.text = CommonUtils.ratingWithSuffix(list?.get(0)?.followerCount?.toString()!!) + " " + ctx.getString(R.string.profile_str_3)
                tvFindFriendCount.visibility = View.VISIBLE
            } else {
                tvFindFriendCount.visibility = View.GONE
            }

            if (!TextUtils.isEmpty(list?.get(0)?.uId) && !TextUtils.isEmpty(list?.get(0)?.profileImage)) {
                ImageLoader.loadImage(
                    ctx,
                    ivFindFriendImage,
                    list.get(0)?.profileImage!!,
                    R.drawable.bg_gradient_placeholder
                )
            }else if (!TextUtils.isEmpty(list?.get(0)?.uId) && !TextUtils.isEmpty(list?.get(0)?.alternateProfileImage)) {
                ImageLoader.loadImage(
                    ctx,
                    ivFindFriendImage,
                    list.get(0)?.alternateProfileImage!!,
                    R.drawable.bg_gradient_placeholder
                )
            }
            if (!TextUtils.isEmpty(list?.get(0)?.uId)){
                if(list?.get(0)?.isAdded!!){
                    val drawable = FontDrawable(ctx, R.string.icon_tick)
                    drawable.setTextColor(ContextCompat.getColor(ctx, R.color.colorWhite))
                    ivAddFindFriend.setImageDrawable(drawable)
                }else{
                    val drawable = FontDrawable(ctx, R.string.icon_follow)
                    drawable.setTextColor(ContextCompat.getColor(ctx, R.color.colorWhite))
                    ivAddFindFriend.setImageDrawable(drawable)
                }
                ivAddFindFriend?.visibility = View.VISIBLE
            }else{
                ivAddFindFriend?.visibility = View.GONE
            }

            ivAddFindFriend.setOnClickListener {
                setFollowUnfollow(list.get(0), ivAddFindFriend)
                if (onChildItemClick != null) {
                    onChildItemClick.onUserClick(position, true)
                }
            }
            llMain.setOnClickListener {
                if (onChildItemClick != null && !TextUtils.isEmpty(list?.get(0)?.uId) ) {
                    onChildItemClick.onUserClick(position, false)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return UserProfileFollowerViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_item_find_friends, parent, false)
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
    }

    fun setFollowUnfollow(data: FacebookListModel.Data?, ivAddedFollowing: ImageView) {
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