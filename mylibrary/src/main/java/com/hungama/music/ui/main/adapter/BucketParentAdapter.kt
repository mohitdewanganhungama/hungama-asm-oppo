package com.hungama.music.ui.main.adapter

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.SystemClock
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.hungama.music.HungamaMusicApp
import com.hungama.music.data.model.*
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.BucketSwipedEvent
import com.hungama.music.eventanalytic.eventreporter.ProgressiveSurveyTappedEvent
import com.hungama.music.eventanalytic.util.callbacks.inapp.InAppCallback
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.viewmodel.RecentlyPlayViewModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.setClickAnimation
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.customview.scrollingpagerindicator.ScrollingPagerIndicator
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import com.moengage.inapp.MoEInAppHelper
import kotlinx.android.synthetic.main.row_bucket.view.*
import kotlinx.coroutines.*


class BucketParentAdapter(
    private var parents: ArrayList<RowsItem?>,
    val context: Context,
    val onParentItemClick: OnParentItemClickListener?,
    val onMoreItemClick: OnMoreItemClick,
    var bottomTabID: Int,
    val headItemsItem: HeadItemsItem?,
    val varient: Int = Constant.ORIENTATION_VERTICAL
) : RecyclerView.Adapter<BucketParentAdapter.ViewHolder>(), BaseActivity.PlayItemChangeListener {
    private val viewPool = RecyclerView.RecycledViewPool()
    var isStoryUpdate = false
    var continueWatchIndex = -1
    var commonSpaceBetweenBuckets = 0

    companion object {
        var isKeywordWatchCall = 999

        var lastHolderItem: ViewHolder? = null
        var bucketChildAdapterRecently: BucketChildAdapter? = null
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        isKeywordWatchCall = 999
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_bucket, parent, false)

        (context as MainActivity).addPlayItemChangeListener(this)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return parents.size
    }

    fun addData(list: MutableList<RowsItem?>) {

        setLog("continue-watching", "setLocalDataIntoContinueWatchBucket -5: array" + parents.size)
        parents?.forEach {
            if (it?.itype == null && it?.items != null && it.items?.size!! > 0) {
                setLog(
                    "continue-watching",
                    "99999: itype:${it.items!!.get(0)?.itype} title:${it.items!!.get(0)?.data?.title}"
                )
                it.itype = it.items!!.get(0)?.itype
            }
        }


        if (continueWatchIndex >= 0) {
            try {

                if (lastHolderItem?.rvChildItem?.adapter != null) {
                    setLog("continue-watching", "continue-watching 555")

                    bucketChildAdapterRecently?.refreshData(
                        parents.get(
                            continueWatchIndex
                        )?.items!!
                    )
//                    lastHolderItem?.rvChildItem?.invalidate()

                } else {
                    setLog("continue-watching", "lastHolderItem?.rvChildItem?.adapter is null")
                }

                setLog(
                    "continue-watching", "continue-watching 4 :${
                        parents.get(
                            continueWatchIndex
                        )?.items?.size
                    }"
                )
                notifyItemChanged(continueWatchIndex)
            } catch (exp: Exception) {
                exp.printStackTrace()
            }

        }
    }

    private var lastClickedTime: Long = 0

    fun isCalledMultipalTime(): Boolean {
        /*
          Prevents the Launch of the component multiple times
          on clicks encountered in quick succession.
         */
        if (SystemClock.elapsedRealtime() - lastClickedTime < Constant.MAX_CLICK_INTERVAL) {
            return false
        }
        lastClickedTime = SystemClock.elapsedRealtime()
        return true
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {

        setLog("continue-watching", "parent->keywords" + parents[position]?.keywords)
        setLog("continue-watching", "isKeywordWatchCall" + isKeywordWatchCall)
//        setLog("continue-watching","parent?.items?.size"+parents[position]?.items?.size)
        setLog("continue-watching", "position::" + position)

        if (position < parents.size) {
            val parent = parents[position]
            setLog(
                TAG,
                "onBindViewHolder: keywords : ${parent?.keywords} parent?.heading : ${parent?.heading} parent?.type : ${parent?.type} parent?.itype : ${parent?.itype} "
            )
            setLog(TAG, "onBindViewHolder: parent?.items : ${parent?.items} ")
            commonSpaceBetweenBuckets =
                context.resources.getDimensionPixelSize(R.dimen.common_two_bucket_space_listing_page)
            setLog("continue-watching", "continue-watching isKeywordWatchCall $isKeywordWatchCall")
//            if (parent?.keywords!=null&&parent?.keywords?.contains("continue-watching")!! && isKeywordWatchCall==999) {
            if (parent?.keywords != null && parent.keywords?.contains("continue-watching")!! && isKeywordWatchCall == 999 && isCalledMultipalTime()) {


                setLog("continue-watching", "continue-watching 0 called")
                continueWatchIndex = holder.adapterPosition
                lastHolderItem = holder

                setLog(
                    "continue-watching",
                    "continue-watching continueWatchIndex:${continueWatchIndex}"
                )
                setUpContinueWhereLeftListViewModel()
                Utils.setMargins(
                    holder.llHeaderTitle,
                    context.resources.getDimensionPixelSize(R.dimen.dimen_12)
                )
            } else {
                setLog("continue-watching", "continue-watching 0 not called")
                Utils.setMarginsTop(
                    holder.llHeaderTitle,
                    context.resources.getDimensionPixelSize(R.dimen.dimen_0)
                )
            }

            if (parent?.heading != null && !TextUtils.isEmpty(parent.heading!!) && ((parent.items != null && parent.items?.size!! > 0) || (parent.orignalItems != null && parent.orignalItems?.size!! > 0))) {
                if (!TextUtils.isEmpty(parent.identifier.toString()) && parent.identifier == 1) {
                    var displayName = ""
                    if (!TextUtils.isEmpty(SharedPrefHelper.getInstance().getUserFirstname())) {
                        displayName += SharedPrefHelper.getInstance().getUserFirstname()!! + " "
                    }

                    if (!TextUtils.isEmpty(SharedPrefHelper.getInstance().getUserLastname())) {
                        displayName += SharedPrefHelper.getInstance().getUserLastname()!!
                    }

                    if (TextUtils.isEmpty(displayName)) {
                        /*if (!TextUtils.isEmpty(SharedPrefHelper.getInstance().getHandleName())){
                            displayName += SharedPrefHelper.getInstance().getHandleName()!!
                            holder.tvTitle.text = getDayGreetings(context) + " " + displayName
                        }else{*/
                        CommonUtils.getDayGreetings(context)
                        holder.tvTitle.text = CommonUtils.greeting.value
                        //}

                    } else {
                        CommonUtils.getDayGreetings(context)
                        holder.tvTitle.text = CommonUtils.greeting.value+" "+displayName
                    }

                    holder.tvTitle.visibility = View.VISIBLE
                } else {
                    holder.tvTitle.text = parent.heading
                    holder.tvTitle.visibility = View.VISIBLE
                }
            } else {
                holder.tvTitle.visibility = View.GONE
            }

            if (parent?.more != null && parent.more == 1 && ((parent.items != null && parent.items?.size!! > 0) || (parent.orignalItems != null && parent.orignalItems?.size!! > 0))) {
                if (varient == Constant.ORIENTATION_HORIZONTAL) {
                    holder.ivMore.visibility = View.VISIBLE
                    holder.ivMore.setOnClickListener {
                        if (onMoreItemClick != null) {
                            if (parent.heading != null && !TextUtils.isEmpty(parent.heading!!)) {
                                onMoreItemClick.onMoreClick(parent, position)
                            }

                        }
                    }
                    holder.llHeaderTitle.setOnClickListener {
                        if (onMoreItemClick != null) {
                            if (parent.heading != null && !TextUtils.isEmpty(parent.heading!!)) {
                                onMoreItemClick.onMoreClick(parent, position)
                            }

                        }
                    }
                } else {
                    holder.ivMore.visibility = View.GONE
                }
            } else {
                holder.ivMore.visibility = View.GONE
            }

            if (parent?.subhead != null && !TextUtils.isEmpty(parent.subhead) && ((parent.items != null && parent.items?.size!! > 0) || (parent.orignalItems != null && parent.orignalItems?.size!! > 0))) {
                holder.tvSubTitle.text = parent.subhead
                holder.tvSubTitle.visibility = View.VISIBLE
            } else {
                holder.tvSubTitle.visibility = View.GONE
            }

            if (parent?.public != null) {
                holder.switchPublic.isChecked = parent.public == 1
                holder.switchPublic.visibility = View.VISIBLE
            }

            if (parent?.itype == 47) {
                holder.tvTitle.visibility = View.GONE
                holder.tvSubTitle.visibility = View.GONE
                holder.ivMore.visibility = View.GONE
                holder.switchPublic.visibility = View.GONE
            }

            if (((parent?.items != null && parent.items?.size!! > 0) || (parent?.orignalItems != null && parent.orignalItems?.size!! > 0))) {
                holder.rvChildItem.visibility = View.VISIBLE
                holder.dotedView.visibility = View.GONE

                var layoutManager: GridLayoutManager? = null
                setLog(
                    "onBindViewHolder",
                    "heading:${parent.heading} varient:${varient} numrow:${parent.numrow}"
                )
                if (varient == Constant.ORIENTATION_VERTICAL) {
                    layoutManager =
                        GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
                    setLog("onBindViewHolder", "heading:${parent.heading} ORIENTATION_VERTICAL")

                } else {
                    if (parent.numrow != null && parent.numrow!! > 0) {
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

                    setLog("onBindViewHolder", "heading:${parent.heading} numrow:${parent.numrow}")
                }

                if (parent.itype == 6 || parent.itype == 13 || parent.itype == 5 || parent.itype == 47 || parent.itype == 15 || parent.itype == 20 || parent.itype == 3
                    || parent.itype == 14 || parent.itype == 22 || parent.itype == 21 || parent.itype == 23 || parent.itype == 10 || parent.itype == 45
                    || parent.itype == 46 || parent.itype == 47 || parent.itype == 48){
                    holder.llMain.visibility = View.GONE
                    holder.llHeaderTitle.visibility = View.GONE
                }
                else {
                    holder.llMain.visibility = View.VISIBLE
                    holder.llHeaderTitle.visibility = View.VISIBLE
                }

                when (parent.itype) {
                    1 -> {
                        var layoutManager: GridLayoutManager? = null
                        if (parent.numrow != null && parent.numrow!! > 0) {
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
                        setChildRecyclerView(holder, layoutManager, parent, position)
                        holder.rvChildItem.setPadding(
                            context.resources.getDimensionPixelSize(R.dimen.dimen_18),
                            0,
                            0,
                            0
                        )
                        Utils.setMargins(
                            holder.llHeaderTitle,
                            context.resources.getDimensionPixelSize(R.dimen.dimen_12)
                        )
                        Utils.setMarginsTop(
                            holder.llHeaderTitle,
                            commonSpaceBetweenBuckets
                        )

                    }
                    2, 4, 7, 8, 9, 11, 12, 16, 17, 25, 41, 42, 43, 44,1000, 9999 -> {

                        setChildRecyclerView(holder, layoutManager, parent, position)
                        holder.rvChildItem.setPadding(
                            context.resources.getDimensionPixelSize(R.dimen.dimen_18),
                            0,
                            0,
                            0
                        )
                        Utils.setMargins(
                            holder.llHeaderTitle,
                            context.resources.getDimensionPixelSize(R.dimen.dimen_12)
                        )
                        Utils.setMarginsTop(
                            holder.llHeaderTitle,
                            commonSpaceBetweenBuckets
                        )
                    }
                    18 -> {

                        setChildRecyclerView(holder, layoutManager, parent, position)
                        holder.rvChildItem.setPadding(
                            context.resources.getDimensionPixelSize(R.dimen.dimen_18),
                            0,
                            0,
                            0
                        )
                        Utils.setMargins(
                            holder.llHeaderTitle,
                            context.resources.getDimensionPixelSize(R.dimen.dimen_12)
                        )
                        Utils.setMarginsTop(
                            holder.llHeaderTitle,
                            context.resources.getDimensionPixelSize(R.dimen.dimen_25)
                        )
                        Utils.setMarginsBottom(
                            holder.llHeaderTitle,
                            context.resources.getDimensionPixelSize(R.dimen.dimen_20)
                        )
                    }
                    19 -> {

                        setChildRecyclerView(holder, layoutManager, parent, position)
                        Utils.setMargins(holder.llMain, 0)
                        holder.rvChildItem.setPadding(0, 0, 0, 0)
                        Utils.setMargins(
                            holder.llHeaderTitle,
                            context.resources.getDimensionPixelSize(R.dimen.dimen_12)
                        )
                        Utils.setMarginsTop(
                            holder.llHeaderTitle,
                            commonSpaceBetweenBuckets
                        )
                    }
                    21 -> {
                        var layoutManager: GridLayoutManager? = null
                        if (parent.numrow != null && parent.numrow!! > 0) {
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
                        holder.rvChildItem.setPadding(0, 0, 0, 0)
                        Utils.setMargins(
                            holder.llHeaderTitle,
                            context.resources.getDimensionPixelSize(R.dimen.dimen_12)
                        )
                        Utils.setMarginsTop(
                            holder.llHeaderTitle,
                            commonSpaceBetweenBuckets
                        )
                    }
                    23 -> {
                        var layoutManager: GridLayoutManager? = null
                        if (parent.numrow != null && parent.numrow!! > 0) {
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
                            Itype23PagerAdapter(
                                parent,
                                context
                            ) { childPosition ->
                                if (onParentItemClick != null) {
                                    onParentItemClick.onParentItemClick(
                                        parent,
                                        position,
                                        childPosition
                                    )
                                }
                            }

                        holder.pager.adapter = pagerAdapter
                        holder.pagerIndicator.attachToPager(holder.pager)

                        Utils.setMargins(holder.llMain, 0)
                        holder.rvChildItem.setPadding(0, 0, 0, 0)
                        Utils.setMargins(
                            holder.llHeaderTitle,
                            context.resources.getDimensionPixelSize(R.dimen.dimen_12)
                        )
                        Utils.setMarginsTop(
                            holder.llHeaderTitle,
                            commonSpaceBetweenBuckets
                        )
                    }
                    47 -> {

                        val model = BodyRowsItemsItem()
                        model.itype = parent.itype
                        model.type = parent.type.toString()
                        model.id = parent.id.toString()
                        parent.orignalItems?.get(0)?.image = parent.image!!
                        parent.orignalItems?.get(0)?.videoUrl = parent.videoUrl!!
                        parent.orignalItems?.get(0)?.description = parent.description!!

                        model.orignalItems = parent.orignalItems

                        parent.items = ArrayList<BodyRowsItemsItem?>()
                        parent.items?.add(0, model)

                        setChildRecyclerView(holder, layoutManager, parent, position)
                        holder.rvChildItem.setPadding(0, 0, 0, 0)
//                    Utils.setMargins(holder.llHeaderTitle, context.resources.getDimensionPixelSize(R.dimen.dimen_12))
//                    Utils.setMarginsTop(
//                        holder.llHeaderTitle,
//                        commonSpaceBetweenBuckets
//                    )
                    }
                    51 -> {

                        setChildRecyclerView(holder, layoutManager, parent, position)
                        holder.rvChildItem.setPadding(
                            context.resources.getDimensionPixelSize(R.dimen.dimen_18),
                            0,
                            0,
                            0
                        )
                        Utils.setMargins(
                            holder.llHeaderTitle,
                            context.resources.getDimensionPixelSize(R.dimen.dimen_12)
                        )
                        Utils.setMarginsTop(
                            holder.llHeaderTitle,
                            context.resources.getDimensionPixelSize(R.dimen.dimen_25)
                        )
                        Utils.setMarginsBottom(
                            holder.llHeaderTitle,
                            context.resources.getDimensionPixelSize(R.dimen.dimen_20)
                        )
                    }
                    BucketChildAdapter.ROW_ITYPE_101,
                    BucketChildAdapter.ROW_ITYPE_102,
                    BucketChildAdapter.ROW_ITYPE_103,
                    BucketChildAdapter.ROW_ITYPE_104 -> {

                        holder.llHeaderTitle.visibility = View.INVISIBLE
                        setChildRecyclerView(holder, layoutManager, parent, position)
                        holder.rvChildItem.setPadding(
                            context.resources.getDimensionPixelSize(R.dimen.dimen_18),
                            0,
                            0,
                            0
                        )
                        Utils.setMargins(
                            holder.llHeaderTitle,
                            context.resources.getDimensionPixelSize(R.dimen.dimen_12)
                        )
                        Utils.setMarginsTop(
                            holder.llHeaderTitle,
                            commonSpaceBetweenBuckets
                        )
                    }
                    BucketChildAdapter.ROW_ITYPE_201 -> {
                        setLog("TAG", "onBindViewHolder datamodel: ${parent.items?.get(0)?.data}")
                        if (!parent.items.isNullOrEmpty()
                            && parent.items?.get(0)?.data != null && parent.items?.get(0)?.data?.isVisible!!
                        ) {

                            holder.llHeaderTitle.visibility = View.GONE
                            setChildRecyclerView(holder, layoutManager, parent, position)
                            holder.rvChildItem.setPadding(
                                context.resources.getDimensionPixelSize(R.dimen.dimen_18),
                                0,
                                0,
                                0
                            )
                            Utils.setMargins(
                                holder.llHeaderTitle,
                                context.resources.getDimensionPixelSize(R.dimen.dimen_12)
                            )
                            Utils.setMarginsTop(
                                holder.llHeaderTitle,
                                commonSpaceBetweenBuckets
                            )
                            holder.rvChildItem.visibility = View.VISIBLE
                        } else {
                            holder.rvChildItem.visibility = View.GONE
                        }

                    }
                    else -> {
                        setLog("NoView", "NoVIew 2")
                        holder.rvChildItem.visibility = View.GONE
                    }
                }

                if (position == 0) {


                    Utils.setMarginsTop(
                        holder.llHeaderTitle,
                        context.resources.getDimensionPixelSize(R.dimen.dimen_8)
                    )

                } else if (position == 1 && parents.get(0)?.items?.size!! <= 0) {
                    Utils.setMarginsTop(
                        holder.llHeaderTitle,
                        context.resources.getDimensionPixelSize(R.dimen.dimen_28)
                    )
                }
            } else {
                setLog("NoView", "NoVIew 1")
                holder.rvChildItem.visibility = View.GONE
            }
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
        val bucketChildAdapter = BucketChildAdapter(context, parent.items!!, varient,
            object : BucketChildAdapter.OnChildItemClick {
                override fun onUserClick(childPosition: Int, view: View?) {
                    if (onParentItemClick != null) {
                        CoroutineScope(Dispatchers.Main).launch {
                            BaseActivity.setTouchData()
                            setClickAnimation(context, view)
                            delay(150)
                            onParentItemClick.onParentItemClick(parent, position, childPosition)
                        }
                    }
                }

                override fun onInAppSubmitClick(
                    inAppModel: InAppSelfHandledModel?,
                    childPosition: Int
                ) {
                    setLog(
                        "setMoengageData",
                        "onInAppSubmitClick: called title:${parent.heading} position:${position} childPosition:${childPosition}length:${parents.size}"
                    )
                    if (parents != null && position < parents.size) {
                        parents.removeAt(position)
                        // call whenever in-app is dismissed
                        MoEInAppHelper.getInstance()
                            .selfHandledDismissed(context, inAppModel?.inAppCampaign!!)
                        notifyItemRemoved(position)
                        InAppCallback.mInAppCampaignList.values.remove(inAppModel)

                        notifyItemRangeChanged(position - 1, 2)

                        setLog("setMoengageData", "onInAppSubmitClick: anser called")
                        setLog("setMoengageData", "onInAppSubmitClick: selfHandledDismissed called")
                        setLog(
                            "setMoengageData",
                            "onInAppSubmitClick: called length:${parents.size}"
                        )


                        if (inAppModel.inAppCampaign.selfHandledCampaign != null && childPosition <= inAppModel.options.size) {
                            CoroutineScope(Dispatchers.IO).launch {
                                val dataMap = java.util.HashMap<String, String>()
                                dataMap.put(
                                    EventConstant.CAMPAIGN_ID_EPROPERTY,
                                    inAppModel.campaign_id
                                )
                                dataMap.put(
                                    EventConstant.TEMPLATE_ID_EPROPERTY,
                                    inAppModel.templateId
                                )
                                dataMap.put(EventConstant.TITLE_EPROPERTY, inAppModel.title)
                                dataMap.put(
                                    EventConstant.SUBTITLE_EPROPERTY,
                                    inAppModel.subTitle
                                )
                                dataMap.put(
                                    EventConstant.OPTION_EPROPERTY,
                                    "" + inAppModel.options
                                )
                                dataMap.put(
                                    EventConstant.OPTION_TAPPED_EPROPERTY,
                                    "" + inAppModel.userAnswer
                                )
                                dataMap.put(
                                    EventConstant.BOTTOM_NAV_POSITION_EPROPERTY,
                                    "" + inAppModel.bottom_nav_position
                                )
                                dataMap.put(
                                    EventConstant.TOP_NAV_POSITION_EPROPERTY,
                                    "" + inAppModel.top_nav_position
                                )
                                dataMap.put(
                                    EventConstant.POSITION_EPROPERTY,
                                    "" + inAppModel.position
                                )
                                EventManager.getInstance()
                                    .sendEvent(ProgressiveSurveyTappedEvent(dataMap))
                            }

                        }
                    }
                }

            })

        if (parent.keywords != null && parent.keywords?.contains("continue-watching")!!) {
            bucketChildAdapterRecently = bucketChildAdapter
        }

        holder.rvChildItem.apply {
            layoutManager = layoutManager2
            adapter = bucketChildAdapter
//            setRecycledViewPool(viewPool)
            //setHasFixedSize(true)
        }

        //holder.rvChildItem.setItemViewCacheSize(parent.items?.size!!)

        holder.rvChildItem.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                setLog("SwipedEventCall", " " + "Swiped called")

                val manager: GridLayoutManager =
                    (holder.rvChildItem.layoutManager as GridLayoutManager)
                val visiblePosition: Int = manager.findLastCompletelyVisibleItemPosition()

                CoroutineScope(Dispatchers.IO).launch {
                    val hashMap = HashMap<String, String>()
                    hashMap.put(EventConstant.BUCKETSWIPED_EPROPERTY, MainActivity.lastItemClicked)
                    //hashMap.put(EventConstant.SOURCE_EPROPERTY,""+Utils.getContentTypeDetailName(""+parent?.type))
                    hashMap.put(
                        EventConstant.SOURCE_EPROPERTY,
                        "" + MainActivity.lastItemClicked + "_" + MainActivity.headerItemName + "_" + parent.heading
                    )
                    hashMap.put(EventConstant.BUCKETNAME_EPROPERTY, parent.heading!!)
                    hashMap.put(EventConstant.LASTPOSITIONOFBUCKET_EPROPERTY, "" + visiblePosition)

                    if (BaseActivity.eventManagerStreamName != EventConstant.BUCKETSWIPED_ENAME) {
                        BaseActivity.eventManagerStreamName = EventConstant.BUCKETSWIPED_ENAME
                        EventManager.getInstance().sendEvent(BucketSwipedEvent(hashMap))
                    }

                }

            }
        })
        if (parent.itype == BucketChildAdapter.ROW_ITYPE_47) {
            setOriginalBucketAdapter(bucketChildAdapter)
            registerReceiver()
        }
    }

    fun updateList(data: ArrayList<BodyDataItem>?, parentPosition: Int) {
        /*for (stories in data?.indices!!) {
            *//*this.parents.get(parentPosition)!!.items!!.get(stories)!!.data!!.viewInex =
                data[stories].viewInex*//*
        }*/
        CoroutineScope(Dispatchers.IO).launch {
            if (!data.isNullOrEmpty()) {
                data.forEachIndexed { index, bodyDataItem ->
                    //this.parents.get(parentPosition)?.items?.get(index)?.data?.misc?.post = bodyDataItem?.misc?.post
                    this@BucketParentAdapter.parents.get(parentPosition)?.items?.get(index)?.data =
                        bodyDataItem
                }
                isStoryUpdate = true
                withContext(Dispatchers.Main) {
                    notifyDataSetChanged()
                }
            }
        }
    }

    fun getListData(): ArrayList<RowsItem?> {
        return parents
    }

    interface OnMoreItemClick {
        fun onMoreClick(selectedMoreBucket: RowsItem?, position: Int)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    var continueWhereLeftModel: ContinueWhereLeftModel? = null

    private fun setUpContinueWhereLeftListViewModel() {
        if (ConnectionUtil(context).isOnline) {
            val continueWhereLeftViewModel = ViewModelProvider(
                context as AppCompatActivity
            ).get(RecentlyPlayViewModel::class.java)

            continueWhereLeftViewModel.getContinueWhereLeftList(
                context,
                bottomTabID,
                headItemsItem
            )?.observe(context,
                Observer {
                    when (it.status) {
                        Status.SUCCESS -> {
                            setLog("continue-watching", "continue-watching 1")
                            if (it != null) {
                                continueWhereLeftModel = it.data
                                HungamaMusicApp.getInstance()
                                    .setContinueWhereLeftData(continueWhereLeftModel!!)
                                parents.get(continueWatchIndex)?.items = ArrayList()

                                setLog(
                                    "continue-watching",
                                    "continue-watching 11:${continueWhereLeftModel?.size}"
                                )
                                if (continueWhereLeftModel?.size!! > Constant.MIN_RECENTPLAY_SIZE) {
                                    isKeywordWatchCall = 1000
                                    setLog(
                                        "continue-watching",
                                        "continue-watching 11:${parents.get(continueWatchIndex)?.items?.size}"
                                    )
                                    setLog(
                                        "continue-watching",
                                        "continue-watching 111:${continueWhereLeftModel?.get(0)?.data?.title}"
                                    )
                                    setLog(
                                        "continue-watching",
                                        "continue-watching 1111:${
                                            continueWhereLeftModel?.get(continueWhereLeftModel?.size!! - 1)?.data?.title
                                        }"
                                    )

                                    parents.get(continueWatchIndex)?.items = continueWhereLeftModel
                                    setLog(
                                        "continue-watching",
                                        "continue-watching 2:${parents.get(continueWatchIndex)?.items?.size}"
                                    )
                                } else {
                                    setLog(
                                        "continue-watching",
                                        "continue-watching 3:${parents.get(continueWatchIndex)?.items?.size}"
                                    )
                                    parents.get(continueWatchIndex)?.items = ArrayList()
                                }
                                addData(parents as MutableList<RowsItem?>)

                            }
                        }

                        Status.LOADING -> {
                        }

                        Status.ERROR -> {
                            setLog(
                                "continue-watching",
                                "setUpContinueWhereLeftListViewModel: ${it.message!!}"
                            )

                        }
                    }
                })
        }
    }


    override fun playItemChange() {
        setLog("TAG", "continue playItemChange: ")
        isKeywordWatchCall = 999
        notifyItemChanged(continueWatchIndex)
    }

    var originalBucketChildAdapter: BucketChildAdapter? = null
    fun setOriginalBucketAdapter(adapter: BucketChildAdapter) {
        originalBucketChildAdapter = adapter
    }

    var screenOnOffReceiver: BroadcastReceiver? = null
    fun registerReceiver() {
        unRegisterOriginalReceiver()
        val theFilter = IntentFilter()
        /** System Defined Broadcast  */
        theFilter.addAction(Intent.ACTION_SCREEN_ON)
        theFilter.addAction(Intent.ACTION_SCREEN_OFF)
        theFilter.addAction(Intent.ACTION_USER_PRESENT)
        screenOnOffReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val strAction = intent.action
                val myKM: KeyguardManager =
                    context.getSystemService(AppCompatActivity.KEYGUARD_SERVICE) as KeyguardManager
                if (strAction == Intent.ACTION_USER_PRESENT || strAction == Intent.ACTION_SCREEN_OFF || strAction == Intent.ACTION_SCREEN_ON) if (myKM.inKeyguardRestrictedInputMode()) {
                    setLog("isDeviceLocked", "LOCKED")
                    pauseOriginalPlayer()
                } else {
                    setLog("isDeviceLocked", "UNLOCKED")
                    playOriginalPlayer()
                }
            }
        }
        context.registerReceiver(screenOnOffReceiver, theFilter)
    }

    private fun unRegisterOriginalReceiver() {
        if (screenOnOffReceiver != null) {
            context.unregisterReceiver(screenOnOffReceiver)
            screenOnOffReceiver = null
        }
    }

    fun playOriginalPlayer() {
        if (originalBucketChildAdapter != null) {
            originalBucketChildAdapter?.playPlayer()
        }
    }

    fun pauseOriginalPlayer() {
        if (originalBucketChildAdapter != null) {
            originalBucketChildAdapter?.pausePlayer()
        }
    }

    fun stopOriginalPlayer() {
        try {
            unRegisterOriginalReceiver()
            if (originalBucketChildAdapter != null) {
                originalBucketChildAdapter?.releasePlayer()
            }
        } catch (e: Exception) {

        }

    }
}