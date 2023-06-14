package com.hungama.music.ui.main.view.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hungama.music.R
import com.hungama.music.data.model.*
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.main.adapter.StoryPlatformDialogAdapter
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.ImageLoader
import kotlinx.android.synthetic.main.dialog_sleep.*

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.hungama.music.HungamaMusicApp
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.CommonUtils.toBitmap
import kotlinx.coroutines.*
import java.lang.Runnable
import java.net.URL


class ShareStoryPlatformDialog(val itemClicked: BaseActivity, val currentTrack: Track?) :
    BottomSheetDialogFragment(),
    StoryPlatformDialogAdapter.OnStoryPlatformItemClick {
    lateinit var recyclerview: RecyclerView
    private lateinit var sleepadapter: StoryPlatformDialogAdapter
    private var storyPlatform = mutableListOf<ShareStoryPlatformDialogModel>()
    var onStoryPlatformItemClick: OnStoryPlatformItemClick? = null
    var storyBitmap: Bitmap? = null

    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_sleep, container, false)
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.onStoryPlatformItemClick = itemClicked
        setLog("TAG", "onViewCreated: " + currentTrack?.contentType)
        setLog("TAG", "onViewCreated: " + currentTrack?.heading)
        setLog("TAG", "onViewCreated: " + currentTrack?.playerType)
        setLog("TAG", "onViewCreated: " + currentTrack?.title)
        setLog("TAG", "onViewCreated: " + currentTrack?.subTitle)
//        setLog("TAG", "onViewCreated: " + currentTrack?.lang)

        tv_Title.setText(getString(R.string.general_str_7))

        var btnclose = view.findViewById(R.id.btnClose) as LinearLayoutCompat?
        btnclose?.setOnClickListener {
            dismiss()
        }

        btnclose?.visibility=View.GONE

        recyclerview = view.findViewById(R.id.rvList)
        recyclerview.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        sleepadapter = StoryPlatformDialogAdapter(requireContext(), this)
        recyclerview.adapter = sleepadapter
        storyPlatform.add(ShareStoryPlatformDialogModel("Facebook", currentTrack!!))
        storyPlatform.add(ShareStoryPlatformDialogModel("Instagram", currentTrack!!))


        sleepadapter.setdata(storyPlatform)


        setStoryDataInvisiable(currentTrack)
        val playableContentModel= HungamaMusicApp.getInstance().getEventData(""+currentTrack?.id.toString())
        setLog("TAG", "setStoryDataInvisiable: "+playableContentModel.language)
        nowPlayingSubtitleTextView.text = playableContentModel.language
//        setLog("TAG", "onViewCreated: playableContentModel"+playableContentModel.language)
    }

    var artworkColor = 0
    private fun setStoryDataInvisiable(currentTrack: Track) {
        setLog("TAG", "setStoryDataInvisiable albumArtBitmap: " + currentTrack.albumArtBitmap)
        setLog("TAG", "setStoryDataInvisiable:currentTrack.playerType " + currentTrack.playerType)
        setLog("TAG", "setStoryDataInvisiable: currentTrack?.contentType" + currentTrack?.contentType)
        setLog("TAG", "setStoryDataInvisiable: currentTrack?.pType" + currentTrack?.pType)
        setLog("TAG", "setStoryDataInvisiable: currentTrack?.heading " + currentTrack?.heading)
        setLog("TAG", "setStoryDataInvisiable: currentTrack.title"+currentTrack.title)
        val movieheading : String = 6.toString()
        if (currentTrack?.heading!!.equals(movieheading)){
            setLog("TAG", "setStoryDataInvisiable:  working:${currentTrack.albumArtBitmap} storyBitmap:${storyBitmap}")
            if (storyBitmap == null) {
                albumArtImageView.visibility = View.GONE
                albumfullscreen.visibility = View.VISIBLE
                ImageLoader.loadImage(
                    requireActivity(),
                    albumfullscreen,
                    currentTrack?.image!!,
                    R.mipmap.ic_launcher
                )
                setLog(
                    "TAG",
                    "setStoryDataInvisiable:nowPlayingTitleTextView " + nowPlayingTitleTextView.toString()
                )
                setLog(
                    "TAG",
                    "setStoryDataInvisiable:nowPlayingSubtitleTextView " + nowPlayingSubtitleTextView.toString()
                )
                try {
                    Handler(Looper.getMainLooper()).postDelayed(Runnable {
                        if (shareImageFullscreen != null) {
//                            storyBitmap = getBitmapFromViewFullScreen(shareImageFullscreen)
                            setLog("TAG", "setStoryDataInvisiable : storyBitmap" + storyBitmap)
                        }
                    }, 300)
                } catch (exp: Exception) {
                    exp.message
                }
            }
        }else{
            setLog("TAG", "setStoryDataInvisiable: not working:${currentTrack.albumArtBitmap} storyBitmap:${storyBitmap}")
            if (storyBitmap == null) {
                albumArtImageView.visibility = View.VISIBLE
                shareImageFullscreen.visibility = View.GONE
                val result: Deferred<Bitmap?> =  CoroutineScope(Dispatchers.Default).async {
                    val urlImage = URL(currentTrack?.image)
                    urlImage.toBitmap()
                }
                CoroutineScope(Dispatchers.Main).launch{
                    try {
                        val bitmap : Bitmap? = result.await()
                        if (bitmap != null){
                            //val artImage = BitmapDrawable(resources, bitmap)
                            albumArtImageView?.setImageBitmap(bitmap)
                            artworkColor = 0
                            artworkColor = CommonUtils.calculateAverageColor(bitmap, 1)
                            //llShareImage?.setBackgroundColor(artworkColor)
                        }else{
                            ImageLoader.loadImage(
                                requireActivity(),
                                albumArtImageView,
                                currentTrack?.image!!,
                                R.mipmap.ic_launcher
                            )
                        }
                        withContext(Dispatchers.Main){
                            try {
                                if (llShareImage != null) {
                                    storyBitmap = getBitmapFromView(llShareImage)
                                    if (artworkColor == 0){
                                        artworkColor = CommonUtils.calculateAverageColor(storyBitmap!!, 1)
                                    }
                                    playon.visibility = View.VISIBLE
                                    setLog("TAG", "setStoryDataInvisiable : storyBitmap" + storyBitmap)
                                }else{

                                }
                            } catch (exp: Exception) {
                                exp.message
                            }
                        }
                    }catch (e:Exception){

                    }

                }
                /*ImageLoader.loadImage(
                    requireActivity(),
                    albumArtImageView,
                    currentTrack?.image!!,
                    R.mipmap.ic_launcher
                )*/
                    nowPlayingTitleTextView.setText(currentTrack.title)
                    nowPlayingSubtitleTextView.setText(currentTrack.subTitle)

                setLog(
                    "TAG",
                    "setStoryDataInvisiable:nowPlayingTitleTextView " + nowPlayingTitleTextView.text.toString()
                )
                setLog(
                    "TAG",
                    "setStoryDataInvisiable:nowPlayingSubtitleTextView " + nowPlayingSubtitleTextView.text.toString()
                )

                /*try {
                    Handler(Looper.getMainLooper()).postDelayed(Runnable {
                        if (llShareImage != null) {
                            storyBitmap = getBitmapFromView(llShareImage)
                            playon.visibility = View.VISIBLE
                            setLog("TAG", "setStoryDataInvisiable : storyBitmap" + storyBitmap)
                        }
                    }, 300)
                } catch (exp: Exception) {
                    exp.message
                }*/
            }

        }

    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        dialog?.behavior?.peekHeight = resources.getDimensionPixelSize(R.dimen.dimen_250)
        dialog?.behavior?.isDraggable = false
        dialog?.setCancelable(true)
        dialog?.setCanceledOnTouchOutside(true)
        return dialog
    }

    interface OnStoryPlatformItemClick {
        fun onStoryPlatformClick(data: ShareStoryPlatformDialogModel)
    }


    override fun onStoryPlatformClick(data: ShareStoryPlatformDialogModel) {
        if (onStoryPlatformItemClick != null) {

            if (storyBitmap != null) {
                //val statusBarColor = CommonUtils.calculateAverageColor(storyBitmap!!, 1)
                data.currentTrack.albumArtBitmap = storyBitmap
                data.currentTrack.statusBarColor = artworkColor

                setLog("TAG", "onStoryPlatformClick: statusBarColor:${artworkColor} storyBitmap:${storyBitmap}")
            }
            onStoryPlatformItemClick?.onStoryPlatformClick(data)
        }
        dismiss()
    }

    private fun getBitmapFromView(view: View): Bitmap? {
        //Define a bitmap with the same size as the view
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        setLog("TAG", "getBitmapFromView:returnedBitmap " + returnedBitmap)
        setLog("TAG", "getBitmapFromView: view.heights "+view.height)
        setLog("TAG", "getBitmapFromView: view.height "+view.width)
        //Bind a canvas to it
        val canvas = Canvas(returnedBitmap)
        setLog("TAG", "getBitmapFromView:canvas " + canvas)

        //Get the view's background
        val bgDrawable = view.background
        if (bgDrawable != null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas)
        } else {
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.TRANSPARENT)
        }
        // draw the view on the canvas
        view.draw(canvas)
        //return the bitmap
        return returnedBitmap
    }

    private fun getBitmapFromViewFullScreen(view: View,): Bitmap? {
        //Define a bitmap with the same size as the view
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        setLog("TAG", "getBitmapFromView:returnedBitmap " + returnedBitmap)
        setLog("TAG", "getBitmapFromView: view.height "+view.height)
        setLog("TAG", "getBitmapFromView: view.height "+view.width)
        //Bind a canvas to it
        val canvas = Canvas(returnedBitmap)
        setLog("TAG", "getBitmapFromView:canvas " + canvas)

        //Get the view's background
        val bgDrawable = view.background
        if (bgDrawable != null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas)
        } else {
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.TRANSPARENT)
        }
        // draw the view on the canvas
        view.draw(canvas)
        //return the bitmap
        return returnedBitmap
    }



}