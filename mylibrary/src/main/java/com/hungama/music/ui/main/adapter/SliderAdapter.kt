package com.hungama.music.ui.main.adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.viewpager.widget.PagerAdapter
import com.hungama.music.R
import com.hungama.music.data.model.MessageModel
import com.hungama.music.data.model.MessageType
import com.hungama.music.data.model.SliderModel
import com.hungama.music.utils.ImageLoader
import kotlinx.android.synthetic.main.row_earn_colin_slider.view.*

class SliderAdapter(val context: Context,val mySlider:ArrayList<SliderModel>):PagerAdapter() {
    override fun getCount(): Int {
        return mySlider.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
       return view == `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(context).inflate(R.layout.row_earn_colin_slider,container,false)

        val model = mySlider[position]
        val image = model.image
        if (!TextUtils.isEmpty(model.imageUrl)){
            ImageLoader.loadImage(
                context,
                view.ivslider,
                model.imageUrl,
                R.drawable.bg_gradient_placeholder
            )
        }else{
            ImageLoader.loadImage(
                context,
                view.ivslider,
                "",
                R.drawable.bg_gradient_placeholder
            )
        }
        /*if (model.height != 0){
            val lp = view.ivslider.layoutParams
            lp.height = model.height
            view.ivslider.requestLayout()
        }*/

        //view.ivslider.setImageResource(image)

        view.setOnClickListener {
            val messageModel = MessageModel("clicked", MessageType.NEUTRAL, true)
            //CommonUtils.showToast(context, messageModel)
        }

        container.addView(view)

        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}