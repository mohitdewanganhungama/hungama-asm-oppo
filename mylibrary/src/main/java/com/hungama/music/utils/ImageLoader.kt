package com.hungama.music.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.ImageView
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.hungama.music.utils.CommonUtils.isAvailable
import com.hungama.music.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.launch
import java.net.URL


object ImageLoader {

    fun loadImageRound(context: Context?, imageView: ImageView, path: String, placeHolderRes: Int) {
        if (context == null) {
            return
        }

            try {
                CoroutineScope(Dispatchers.Main).async{
                Glide.with(context).asBitmap().load("" + path)
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE).apply(
                        RequestOptions().dontAnimate().dontTransform()
                            .placeholder(R.drawable.bg_gradient_placeholder)
                            .apply(RequestOptions.circleCropTransform())
                    ).into<BitmapImageViewTarget>(object : BitmapImageViewTarget(imageView) {
                        override fun setResource(resource: Bitmap?) {

                            val circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(context.resources, resource)
                            circularBitmapDrawable.isCircular = true
                            imageView.setImageDrawable(circularBitmapDrawable)
                        }
                    })
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }




    }

    fun loadImageFitCenter(context: Context?, imageView: ImageView, imageUrl: String, placeHolderRes: Int) {
        if (context == null) {
            return
        }

            try {
                CoroutineScope(Dispatchers.Main).async{
                Glide.with(context)
                    .load("" + imageUrl)
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .placeholder(R.drawable.bg_gradient_placeholder)
                    .into(imageView)
                }
            } catch (exp: Exception) {
                exp.printStackTrace()
            }




    }
    fun loadImageWithFullScreen(context: Context?, imageView: ImageView, imageUrl: String, placeHolderRes: Int) {
        if (context == null || imageView==null) {
            return
        }

            try {
                CoroutineScope(Dispatchers.Main).async{
                Glide.with(context)
                    .load("" + imageUrl)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .placeholder(R.drawable.bg_gradient_placeholder)
                    .into(imageView)
                }
            } catch (exp: Exception) {
                exp.printStackTrace()
            }




    }

    fun loadImageWithoutPlaceHolder(context: Context?, imageView: ImageView, imageUrl: String) {
        if (context == null) {
            return
        }
        try {


            Glide.with(context)
                .load("" + imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(imageView)
        } catch (exp: Exception) {
            exp.printStackTrace()
        }
    }
    fun loadImage(context: Context?, imageView: ImageView, imageUrl: String, placeHolderRes: Int) {
            if(context != null && context.isAvailable()){
                try {
                    CoroutineScope(Dispatchers.Main).async{

                    Glide.with(context)
                        .load("" + imageUrl)
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .error(placeHolderRes)
                        .placeholder(placeHolderRes)
                        .into(imageView)
                    }
                } catch (exp: Exception) {
                    exp.printStackTrace()
                    Log.v("GlideException", " " + exp.localizedMessage!!.toString() + "\n" + imageUrl)
                }

        }
    }

    fun loadImage(context: Context, imageView: ImageView, resource: Int, placeHolderRes: Int) {
            try {
                CoroutineScope(Dispatchers.Main).async {
                    Glide.with(context).load(resource).fitCenter().apply(
                        RequestOptions().placeholder(R.drawable.bg_gradient_placeholder)
                            .dontAnimate().dontTransform()
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    ).into(imageView)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }




    }

    fun loadImage(context: Context, imageView: ImageView, resource: Drawable, placeHolderRes: Int) {
        try {

            CoroutineScope(Dispatchers.Main).async{
                Glide.with(context).asBitmap().load(resource).diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .apply(
                        RequestOptions().dontAnimate().dontTransform()
                            .placeholder(R.drawable.bg_gradient_placeholder)
                            .apply(RequestOptions.circleCropTransform())
                    ).into<BitmapImageViewTarget>(object : BitmapImageViewTarget(imageView) {
                        override fun setResource(resource: Bitmap?) {
                            val circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(context.resources, resource)
                            circularBitmapDrawable.isCircular = true
                            imageView.setImageDrawable(circularBitmapDrawable)
                        }
                    })
            }



        } catch (e: Exception) {
            e.printStackTrace()
        }


    }

    fun loadImage(context: Context?, imageView: ImageView, image: Bitmap, placeHolderRes: Int) {
        if(context != null && context.isAvailable()){
            try {
                CoroutineScope(Dispatchers.Main).async{

                    Glide.with(context)
                        .asBitmap()
                        .load(image)
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .error(placeHolderRes)
                        .placeholder(placeHolderRes)
                        .into(imageView)
                }
            } catch (exp: Exception) {
                exp.printStackTrace()
            }

        }
    }

}