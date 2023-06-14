package com.hungama.music.ui.main.adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.hungama.fetch2.Status
import com.hungama.music.R
import com.hungama.music.utils.customview.downloadmanager.model.DownloadQueue
import com.hungama.music.utils.customview.downloadmanager.model.DownloadedAudio
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.data.model.ContentTypes
import com.hungama.music.data.model.PlanNames
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.convertByteToHumanReadableFormat
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.DateUtils
import com.hungama.music.utils.ImageLoader
import com.hungama.music.utils.fontmanger.FontDrawable
import com.hungama.music.utils.hide
import com.hungama.music.utils.preference.PrefConstant
import com.hungama.music.utils.preference.SharedPrefHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import kotlin.collections.ArrayList

class MovieDownloadAdapter(
    var context: Context,
    var contentType: Int,
    var movieItemListener: MovieItemListener
) :
    RecyclerView.Adapter<MovieDownloadAdapter.ViewHolder>() {

    var movieList = ArrayList<DownloadedAudio>()

    internal fun addData(movieslist: ArrayList<DownloadedAudio>) {
        CoroutineScope(Dispatchers.Main).launch {
            movieList = movieslist
            notifyDataSetChanged()
        }

    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var title = itemView.findViewById(R.id.tvMovieTitle) as TextView
        var desc = itemView.findViewById(R.id.tvMovieBody) as TextView
        var movielanguage = itemView.findViewById(R.id.tvMoviewLanguage) as TextView
        var moviaction = itemView.findViewById(R.id.tvMovieAction) as TextView
        var downloadSize = itemView.findViewById(R.id.downloadSize) as TextView
        var moviedate = itemView.findViewById(R.id.tvMoviedate) as TextView
        var movieimage = itemView.findViewById(R.id.ivMoviesDownloadImage) as ImageView
        var download = itemView.findViewById(R.id.ivMovieDownloading) as ImageView
        var ivinformation = itemView.findViewById(R.id.ivMoreInfo) as ImageView
        var cvMain = itemView.findViewById(R.id.cvMain) as ConstraintLayout
        var tvDownloadState = itemView.findViewById(R.id.tvDownload) as TextView
        var rlDownloadeStates = itemView.findViewById(R.id.rlDownloadeStates) as RelativeLayout
        var vDevider = itemView.findViewById(R.id.vDevider) as View
        var view2 = itemView.findViewById(R.id.view2) as View

        //var checkiamge = itemView.findViewById(R.id.checkiamge) as ImageView
        var clSelection = itemView.findViewById(R.id.clSelection) as ConstraintLayout
        var ivSelection = itemView.findViewById(R.id.ivSelection) as ImageView

        init {
            itemView.setOnClickListener(this)
            ivinformation.setOnClickListener(this)
        }

        override fun onClick(v: View?) {

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.row_movies_download, parent, false)
        return ViewHolder(view)
    }


    override fun getItemCount(): Int {
        return movieList.size
    }

    interface MovieItemListener {
        fun showMovieDeleteDialog(position: Int)
        fun showDetail(datalist: DownloadedAudio)
        fun pauseOrResumeDownload(datalist: DownloadedAudio, position: Int)
        fun onItemSelection(data: DownloadedAudio, childPosition: Int, isSelected: Int)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var datalist = movieList[position]
        //holder.checkiamge.visibility = View.GONE
        holder.title.text = datalist.title

        setLog("MovieDownloadAdapter","onBindViewHolder datalist:${datalist}")
        if (contentType == ContentTypes.SHORT_FILMS.value){
            holder.desc.visibility=View.GONE
            holder.moviaction?.visibility=View.GONE
            holder.movielanguage.text = ""+datalist?.language

        }else{
            holder.desc.text = datalist.subTitle
            //holder.movielanguage.text = datalist.language
            holder.movielanguage.text = ""
            val genre=TextUtils.join("/",CommonUtils.getStringToArray(datalist.genre!!))
            if (!TextUtils.isEmpty(genre)){
                holder.moviaction.text = "• " + genre
            }else{
                holder.moviaction.text = ""
            }
        }


        holder.moviedate.text = ""

        setLog("TAG", "DATA LIST = " + datalist.toString())

        if (!TextUtils.isEmpty(datalist.thumbnailPath)){
            ImageLoader.loadImage(
                context,
                holder.movieimage,
                datalist.thumbnailPath!!,
                R.drawable.bg_gradient_placeholder
            )
        }else if (!TextUtils.isEmpty(datalist.image)){
            ImageLoader.loadImage(
                context,
                holder.movieimage,
                datalist.image!!,
                R.drawable.bg_gradient_placeholder
            )
        }
        setLog("TAG", "onBindViewHolder:datalist?.isSelected"+datalist.isSelected)
        if (datalist.isSelected == 1){
            setLog("TAG", "onBindViewHolder: 1 working")
            holder.clSelection.visibility = View.VISIBLE
            holder.ivSelection.setImageDrawable(context.faDrawable(R.string.icon_success, R.color.half_opacity_white_color, context.resources.getDimensionPixelSize(R.dimen.font_20).toFloat()))
        }else if (datalist.isSelected == 2){
            setLog("TAG", "onBindViewHolder: 2 working")
            holder.clSelection.visibility = View.VISIBLE
            holder.ivSelection.setImageDrawable(context.faDrawable(R.string.icon_success, R.color.colorWhite, context.resources.getDimensionPixelSize(R.dimen.font_20).toFloat()))
        }else{
            holder.clSelection.visibility = View.GONE
        }

        holder.download.setImageResource(R.drawable.image_movie_downloading_icon)

        holder.moviaction.hide()
        holder.vDevider.hide()
        holder.view2.hide()

        holder.cvMain.setOnClickListener {
            if (movieItemListener != null) {
                movieItemListener.showDetail(datalist)
            }
        }
        holder.ivinformation.setOnClickListener {
            if (movieItemListener != null) {
                movieItemListener.showMovieDeleteDialog(position)
            }
        }

        holder.download.setOnClickListener {
            if (movieItemListener != null) {
                movieItemListener.pauseOrResumeDownload(datalist, position)
            }
        }

        holder.clSelection?.setOnClickListener {
            holder.clSelection?.visibility = View.VISIBLE
            if (datalist?.isSelected == 1){
                datalist?.isSelected = 2
                holder.ivSelection?.setImageDrawable(context.faDrawable(R.string.icon_success, R.color.colorWhite, context.resources.getDimensionPixelSize(R.dimen.font_20).toFloat()))
            }else if (datalist?.isSelected == 2){
                datalist?.isSelected = 1
                holder.ivSelection?.setImageDrawable(context.faDrawable(R.string.icon_success, R.color.half_opacity_white_color, context.resources.getDimensionPixelSize(R.dimen.font_20).toFloat()))
            }
            if (movieItemListener != null) {
                movieItemListener.onItemSelection(datalist, position, datalist.isSelected)
            }
        }

        val ivMoreDrawable = FontDrawable(context, R.string.icon_option)
        ivMoreDrawable.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
        holder.ivinformation.setImageDrawable(ivMoreDrawable)

        val drawable = FontDrawable(context, R.string.icon_download)
        drawable.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
        holder.download.setImageDrawable(drawable)

        val downloadedAudio =
            AppDatabase?.getInstance()?.downloadedAudio()?.findByContentId(datalist?.contentId!!)
        val downloadQueue =
            AppDatabase?.getInstance()?.downloadQueue()?.findByContentId(datalist?.contentId!!)
        if (downloadQueue != null) {
            holder.rlDownloadeStates.visibility = View.VISIBLE
            setLog("TAG", "onBindViewHolder: datalist.totalDownloadBytes "+datalist.totalDownloadBytes)
            setLog("TAG", "onBindViewHolder: datalist.downloadedBytes "+datalist.downloadedBytes)
            if (datalist.totalDownloadBytes.toInt() == -1){
                datalist.totalDownloadBytes = 0
                datalist.downloadedBytes = 0
                setLog("TAG", "onBindViewHolder: logd is working")
                holder.downloadSize.text = "" + convertByteToHumanReadableFormat(datalist.downloadedBytes) + "/" + convertByteToHumanReadableFormat(datalist.totalDownloadBytes)
            }else{
                holder.downloadSize.text = "" + convertByteToHumanReadableFormat(datalist.downloadedBytes) + "/" + convertByteToHumanReadableFormat(datalist.totalDownloadBytes)
            }
//                downloadIconStates(downloadQueue, holder.download, holder.tvDownloadState,holder.moviedate)
                downloadIconStates(downloadQueue, holder.download, holder.tvDownloadState,holder.moviedate, holder.downloadSize)

        }

        if (downloadedAudio != null) {
            if (downloadedAudio?.downloadStatus == Status.COMPLETED.value) {
                try {
                    holder.rlDownloadeStates.visibility = View.GONE
                    holder.tvDownloadState.text = context.getString(R.string.video_player_str_2)
                    holder.downloadSize.text =
                        "" + convertByteToHumanReadableFormat(datalist.totalDownloadBytes)
                    setLog("TAG", "onBindViewHolder:holder.downloadSize download audio "+holder.downloadSize)
                    val drawable1 = FontDrawable(context, R.string.icon_downloaded2)
                    drawable1.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
                    holder.download.setImageDrawable(drawable1)

                    val userSubscriptionDetail = SharedPrefHelper.getInstance().getPayUserDetail(
                        PrefConstant.USER_PAY_DATA)
                    val sdf = SimpleDateFormat(DateUtils.DATE_FORMAT_YYYY_MM_DD)

                    if (CommonUtils.isUserHasGoldSubscription()){
                        val strExpiryDate = DateUtils.convertDate(DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_ss,DateUtils.DATE_FORMAT_DD_MM_YYYY_slash,userSubscriptionDetail?.data?.subscription?.subscriptionEndDate!!)
                        setLog("MovieDownloadAdapter", "adapter-1-strExpiryDate-$strExpiryDate")
                        if(!strExpiryDate.isNullOrEmpty()){
                            holder.moviedate.text = context.getString(R.string.library_video_str_11)+" "+strExpiryDate
                            holder.cvMain.alpha = 1F
                        }else{
                            holder.cvMain.alpha = 0.4F
                            holder.moviedate.text = context.getString(R.string.general_setting_str_50)
                        }
                    }else  if (datalist?.movierights?.contains(PlanNames.TVOD.name, true)!! || datalist?.movierights?.contains(PlanNames.PTVOD.name, true)!!){
                        setLog("MovieDownloadAdapter", "adapter-2")
                        var isFound = false
                        if (!userSubscriptionDetail?.data?.tvod.isNullOrEmpty()){
                            for (item in userSubscriptionDetail?.data?.tvod?.iterator()!!){
                            if(item?.contentId?.equals(datalist?.contentId)!!){
                                isFound = true
                                setLog("MovieDownloadAdapter", "adapter-3-contentId-${item?.contentId}")
                                val strExpiryDate =DateUtils.convertDate(DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_ss,DateUtils.DATE_FORMAT_YYYY_MM_DD,item?.expiryDate)
                                setLog("MovieDownloadAdapter", "adapter-4-strExpiryDate-$strExpiryDate")
                                holder.moviedate.text = context.getString(R.string.library_video_str_11)+" "+strExpiryDate
                            }
                            }

                        }
                        if (!isFound){
                            setLog("MovieDownloadAdapter", "adapter-5")
                            holder.moviedate.text = context.getString(R.string.general_setting_str_50)
                            holder.cvMain.alpha = 0.4F
                        }
                    } else{
                        setLog("MovieDownloadAdapter", "adapter-6")
                        holder.moviedate.text = context.getString(R.string.general_setting_str_50)
                        holder.cvMain.alpha = 0.4F
                    }

                } catch (e: Exception) {

                }
            }
        }
    }

    private fun downloadIconStates(
        downloadQueue: DownloadQueue,
        ivAudioDownload: ImageView,
        tvDownloadState: TextView,
        moviedate: TextView,
        tvDownloadSize: TextView
    ) {
        setLog("TAG", "downloadIconStates: ${downloadQueue.downloadStatus}")
        when (downloadQueue.downloadStatus) {
            Status.NONE.value -> {
                tvDownloadState.text = "-"
                val drawable = FontDrawable(context, R.string.icon_download)
                drawable.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
                ivAudioDownload.setImageDrawable(drawable)
                moviedate.visibility =View.GONE
                tvDownloadSize.visibility =View.GONE
            }
            Status.QUEUED.value -> {
                tvDownloadState.text = context.getString(R.string.download_str_15)
                val drawable = FontDrawable(context, R.string.icon_download_queue)
                drawable.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
                ivAudioDownload.setImageDrawable(drawable)
                moviedate.visibility =View.GONE
                tvDownloadSize.visibility =View.GONE
            }
            Status.DOWNLOADING.value -> {
                tvDownloadState.text =
                    context.getString(R.string.download_str_2) + " " + downloadQueue.percentDownloaded + "%"
                val drawable = FontDrawable(context, R.string.icon_downloading)
                drawable.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
                ivAudioDownload.setImageDrawable(drawable)
                moviedate.visibility =View.GONE
                tvDownloadSize.visibility =View.VISIBLE
            }
            Status.COMPLETED.value -> {
                tvDownloadState.text = context.getString(R.string.video_player_str_2)
                val drawable = FontDrawable(context, R.string.icon_downloaded2)
                drawable.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
                ivAudioDownload.setImageDrawable(drawable)
                moviedate.visibility =View.VISIBLE
                tvDownloadSize.visibility =View.VISIBLE
            }
            Status.PAUSED.value -> {
                tvDownloadState.text = context.getString(R.string.download_str_16)
                ivAudioDownload.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_pause_round
                    )
                )
                moviedate.visibility =View.GONE
                tvDownloadSize.visibility =View.VISIBLE
            }
            Status.FAILED.value -> {
                tvDownloadState.text = context.getString(R.string.download_str_17)
                ivAudioDownload.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_download_error
                    )
                )
                moviedate.visibility =View.GONE
                tvDownloadSize.visibility =View.GONE
            }
        }
    }
}