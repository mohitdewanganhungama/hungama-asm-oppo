package com.hungama.music.ui.main.adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.data.model.LoginPlatformSequenceModel
import com.hungama.music.utils.ImageLoader
import com.hungama.music.R

class LoginPlatformSequenceAdapter(
    val ctx: Context,
    val loginPlatformSequenceList:ArrayList<LoginPlatformSequenceModel>?,
    val onLoginPlatformClickListener:OnLoginPlatformClickListener?):RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnLoginPlatformClickListener{
        fun onLoginPlatformClickListener(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return LoginSequenceViewHolder(LayoutInflater.from(ctx).inflate(R.layout.row_login_sequence_view, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as LoginSequenceViewHolder).bind(position)
    }

    override fun getItemCount(): Int {
        return loginPlatformSequenceList?.size!!
    }

    private inner class LoginSequenceViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvLoginPlatform: TextView = itemView.findViewById(R.id.tvLoginPlatform)
        val ivLoginPlatform: ImageView = itemView.findViewById(R.id.ivLoginPlatform)
        val clMain: ConstraintLayout = itemView.findViewById(R.id.clMain)
        fun bind(position: Int) {
            if (!loginPlatformSequenceList.isNullOrEmpty() && loginPlatformSequenceList.size > position){
                if(position==0){
                    clMain.setPadding(0,0,0,0)
                }else{
                    clMain.setPadding(ctx.resources.getDimensionPixelSize(R.dimen.dimen_16),0,0,0)
                }

                val list = loginPlatformSequenceList[position]

                if (!TextUtils.isEmpty(list.name)) {
                    tvLoginPlatform.text = list.name
                    tvLoginPlatform.visibility = View.VISIBLE
                } else {
                    tvLoginPlatform.visibility = View.GONE
                }


                if (list.image != null) {
                    ImageLoader.loadImage(
                        ctx,
                        ivLoginPlatform,
                        "",
                        list.image!!
                    )
                }
                clMain.setOnClickListener {
                    if (onLoginPlatformClickListener != null) {
                        onLoginPlatformClickListener.onLoginPlatformClickListener(position)
                    }
                }
            }
        }
    }
}