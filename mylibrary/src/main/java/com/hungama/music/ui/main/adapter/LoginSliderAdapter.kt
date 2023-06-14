package com.hungama.music.ui.main.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.R
import com.hungama.music.ui.main.viewmodel.LoginSliderModel
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.animation.ValueAnimator.INFINITE


class LoginSliderAdapter : RecyclerView.Adapter<LoginSliderAdapter.SliderViewHolder>() {
    var sliderItems = emptyList<LoginSliderModel>()
    lateinit var context : Context
    private var custom_position = 0


    internal fun setdata(list: List<LoginSliderModel>,context: Context){
        sliderItems = list
        this.context = context
    }

    inner class SliderViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        var imageview = itemView.findViewById(R.id.ivImage)as ImageView
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LoginSliderAdapter.SliderViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.viewpager_item,parent,false)
        return SliderViewHolder(view)
    }
    override fun onBindViewHolder(holder: LoginSliderAdapter.SliderViewHolder, position: Int) {
        var dataItem = sliderItems[position]
//        val animZoom = AnimationUtils.loadAnimation(context,R.anim.spinner_fade_in)
//        holder.imageview.startAnimation(animZoom)


        holder.imageview.setImageResource(dataItem.image)
        val anim = ValueAnimator.ofFloat(1f, 1.5f)
        anim.duration = 10000
        anim.addUpdateListener { animation ->
            holder.imageview.setScaleX(animation.animatedValue as Float)
            holder.imageview.setScaleY(animation.animatedValue as Float)
        }
        anim.repeatCount = INFINITE
        anim.start()
    }
    override fun getItemCount(): Int {
        return sliderItems.size
    }
}