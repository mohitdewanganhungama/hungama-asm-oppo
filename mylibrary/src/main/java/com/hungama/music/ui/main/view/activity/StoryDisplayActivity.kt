package com.hungama.music.ui.main.view.activity

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.SparseIntArray
import androidx.annotation.OptIn
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSourceFactory
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.CacheWriter
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.hungama.music.R
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.home.eventreporter.StoryEvent
import com.hungama.music.ui.main.view.fragment.DiscoverTabFragment.Companion.UPDATED_STORY_USER_LIST
import com.hungama.music.data.model.BodyDataItem
import com.hungama.music.utils.customview.stories.StoryPagerAdapter
import com.hungama.music.data.model.PageChangeListener
import com.hungama.music.data.model.PageViewOperator
import com.hungama.music.data.model.UserStoryModel
import com.hungama.music.eventanalytic.eventreporter.PageViewEvent
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.main.view.fragment.StoryDisplayFragment
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant
import com.hungama.music.utils.Constant.isVideoStoryPlaying
import com.hungama.music.utils.CubeOutTransformer
import com.hungama.music.utils.DateUtils
import com.hungama.music.utils.Utils
import kotlinx.android.synthetic.main.activity_story_display.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
class StoryDisplayActivity : AppCompatActivity(),
    PageViewOperator {

    private lateinit var pagerAdapter: StoryPagerAdapter
    private var currentPage: Int = 0
    private var position: Int = 0
    private var storyUserList = ArrayList<BodyDataItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story_display)
        if (intent.hasExtra("position")) {
            position = intent.extras?.getInt("position")!!
        }

        if (BaseActivity.player11 != null){
            if (BaseActivity.player11?.isPlaying == true){
                BaseActivity.player11?.pause()
            }
        }

        currentPage = position!!

        if (intent.hasExtra("list")) {
            storyUserList = intent.extras?.getParcelableArrayList<BodyDataItem>("list")!!
            setLog("lajlghlafhglah", Gson().toJson(storyUserList[currentPage]))

/*            CoroutineScope(Dispatchers.Main).launch {
                val hashMapPageView = java.util.HashMap<String, String>()

                hashMapPageView[EventConstant.CONTENT_NAME_EPROPERTY] =
                    "" + Utils.getContentTypeNameForStream("" + storyUserList[currentPage].title)
                hashMapPageView[EventConstant.CONTENT_TYPE_EPROPERTY] =
                    "" + Utils.getContentTypeNameForStream("" + storyUserList[currentPage].type)
                hashMapPageView[EventConstant.CONTENT_TYPE_ID_EPROPERTY] =
                    "" + Utils.getContentTypeNameForStream("" + storyUserList[currentPage].id)
                hashMapPageView[EventConstant.SOURCE_DETAILS_EPROPERTY] = MainActivity.lastItemClicked
                hashMapPageView[EventConstant.SOURCEPAGE_EPROPERTY] =
                    "" + MainActivity.lastItemClicked + "_" + MainActivity.headerItemName + "_" + "History"
                hashMapPageView[EventConstant.PAGE_NAME_EPROPERTY] = "" + "History"

                setLog("VideoPlayerPageView", hashMapPageView.toString())
                EventManager.getInstance().sendEvent(PageViewEvent(hashMapPageView))
            }*/
        }

//        setupUserStory()
        setUpPager(null)

        CoroutineScope(Dispatchers.IO).launch {
            val hashMap = HashMap<String, String>()
            hashMap.put(EventConstant.SOURCE_EPROPERTY, "story")
            setLog("TAG", "login${hashMap}")
            EventManager.getInstance().sendEvent(StoryEvent(hashMap))
        }

    }

    override fun backPageView(isError:Boolean) {
        if (viewPager?.currentItem!! > 0) {
            try {
                fakeDrag(false, isError)
            } catch (e: Exception) {
                //NO OP
            }
        }
    }

    override fun nextPageView(isError:Boolean) {
        if (viewPager?.currentItem!! + 1 < viewPager?.adapter?.count ?: 0) {
            try {
                fakeDrag(true, isError)
            } catch (e: Exception) {
                //NO OP
            }
        } else {
            //there is no next story
            onBackPressed()
            //Toast.makeText(this, "All stories displayed.", Toast.LENGTH_LONG).show()
        }
    }

    private fun setUpPager(stories: UserStoryModel?) {
        //val storyUserList = StoryGenerator.generateStories()

        progressState.clear()
        /*storyUserList.forEachIndexed { index, data ->
            if (data.viewInex!! < data.stories!!.size)
                progressState.put(index, data.viewInex!!)
            else progressState.put(index, 0)
        }*/
        //storyUserList?.get(position!!)?.userStories = stories
        if (!storyUserList.isNullOrEmpty() && storyUserList.size > position && !storyUserList?.get(position!!)?.misc?.post?.items.isNullOrEmpty()) {
            storyUserList?.get(position!!)?.misc?.post?.items?.forEachIndexed { index, data ->
                setLog("TAG", "setUpPager: data ${data} watch${data.watch}")
                if (data.watch) {
                    progressState.put(index, currentPage)
                } else {
                    progressState.put(index, 0)
                }
            }
        }else{
            onBackPressed()
            return
        }

        setLog("progressState", "$progressState")

        if (stories != null) {
            preLoadStories(stories)
        }

        pagerAdapter = StoryPagerAdapter(
            supportFragmentManager,
            storyUserList,
            ::updateStoryUserList, ::setVideoStoryType
        )
        viewPager?.offscreenPageLimit = 0
        viewPager?.adapter = pagerAdapter
        viewPager?.currentItem = currentPage
        viewPager?.setPageTransformer(
            true,
            CubeOutTransformer()
        )
        viewPager?.addOnPageChangeListener(object : PageChangeListener() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position != currentPage){
                    currentPage = position
                    //setupUserStory()
                }
            }

            override fun onPageScrollCanceled() {
                setLog("StoryView", "StoryDisplayActivity-onPageScrollCanceled-resumeCurrentStory()")
                currentFragment()?.resumeCurrentStory()
            }
        })
    }

    private fun preLoadStories(stories: UserStoryModel) {
        val imageList = mutableListOf<String>()
        val videoList = mutableListOf<String>()

        /*storyUserList.forEach { storyUser ->
            storyUser.stories?.forEach { story ->
                if (story.isVideo() == true) {
                    videoList.add(story.url.toString())
                } else {
                    imageList.add(story.url.toString())
                }
            }
        }*/

        stories?.child?.forEach { story ->
            if (!story.media.isNullOrEmpty() && stories.isVideo(story.media.get(0))) {
                videoList.add(story.media?.get(0).toString())
            } else {
                imageList.add(story.media?.get(0).toString())
            }
        }
        preLoadVideos(videoList)
        preLoadImages(imageList)
    }

     private fun preLoadVideos(videoList: MutableList<String>) {
        videoList.map { data ->
            GlobalScope.async {
                val dataUri = Uri.parse(data)
                val dataSpec = DataSpec(dataUri, 0, 500 * 1024, null)
                val dataSource: DataSource =
                    DefaultDataSourceFactory(
                        applicationContext,
                        Util.getUserAgent(applicationContext, getString(R.string.login_str_2))
                    ).createDataSource()

                val listener =
                    CacheWriter.ProgressListener { requestLength: Long, bytesCached: Long, _: Long ->
                        val downloadPercentage = (bytesCached * 100.0
                                / requestLength)
                        setLog("preLoadVideos", "downloadPercentage: $downloadPercentage")
                    }

                try {
                    CacheWriter(
                        dataSource as CacheDataSource,
                        dataSpec,
                        ByteArray(CacheWriter.DEFAULT_BUFFER_SIZE_BYTES),
                        listener
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun preLoadImages(imageList: MutableList<String>) {
        imageList.forEach { imageStory ->
            Glide.with(this).load(imageStory).preload()
        }
    }

    private fun currentFragment(): StoryDisplayFragment? {
        return pagerAdapter.findFragmentByPosition(viewPager, currentPage) as StoryDisplayFragment
    }

    /**
     * Change ViewPage sliding programmatically(not using reflection).
     * https://tech.dely.jp/entry/2018/12/13/110000
     * What for?
     * setCurrentItem(int, boolean) changes too fast. And it cannot set animation duration.
     */
    private var prevDragPosition = 0

    private fun fakeDrag(forward: Boolean, isError: Boolean) {
        if (prevDragPosition == 0 && viewPager.beginFakeDrag()) {
            ValueAnimator.ofInt(0, viewPager.width).apply {
                duration = 400L
                interpolator = FastOutSlowInInterpolator()
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator) {}
                    override fun onAnimationStart(animation: Animator) {
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        removeAllUpdateListeners()
                        if (viewPager.isFakeDragging) {
                            viewPager.endFakeDrag()
                        }
                        prevDragPosition = 0
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        removeAllUpdateListeners()
                        if (viewPager.isFakeDragging) {
                            viewPager.endFakeDrag()
                        }
                        prevDragPosition = 0
                    }

                })
                addUpdateListener {
                    if (!viewPager?.isFakeDragging!!) return@addUpdateListener
                    val dragPosition: Int = it.animatedValue as Int
                    val dragOffset: Float =
                        ((dragPosition - prevDragPosition) * if (forward) -1 else 1).toFloat()
                    prevDragPosition = dragPosition
                    setLog("Story", "StoryDisplayActivity-fakeDrag-dragOffset-$dragOffset")
                    viewPager?.fakeDragBy(dragOffset)
                }
            }.start()
        }
    }

    companion object {
        val progressState = SparseIntArray()
    }

    override fun onBackPressed() {
        /*storyUserList?.forEachIndexed { index, storyUser ->
           // setLog("viewedIndex", "${storyUserList[index].viewInex}")
        }*/
        isVideoStoryPlaying = false
        val intent = Intent()
        intent.putParcelableArrayListExtra(UPDATED_STORY_USER_LIST, storyUserList)
        setResult(Activity.RESULT_OK, intent)
        finish()
        super.onBackPressed()
    }

    private fun updateStoryUserList(userIndex: Int, viewIndex: Int) {
        /*if (storyUserList[userIndex].viewInex!! < (viewIndex + 1)) {
            storyUserList[userIndex].viewInex = (viewIndex + 1)
        }*/

        if (!storyUserList.isNullOrEmpty() && storyUserList.size > userIndex){
            if (!storyUserList.get(userIndex).misc?.post?.items.isNullOrEmpty()
                && storyUserList.get(userIndex).misc?.post?.items?.size!! > viewIndex){
                storyUserList.get(userIndex).misc?.post?.items?.get(viewIndex)?.watch = true
                storyUserList.get(userIndex).misc?.post?.timestamp = DateUtils.curreentTimeStamp()
                if (storyUserList.get(userIndex).misc?.post?.count!! > viewIndex){
                    storyUserList.get(userIndex).misc?.post?.readCount = (viewIndex + 1)
                }
            }
        }
    }

    private fun setVideoStoryType(isVideoStory:Boolean, onResumeCalled: Boolean){
        setLog("setVideoStoryType","setVideoStoryType-isVideoStory-$isVideoStory-onResumeCalled-$onResumeCalled")
        if (onResumeCalled){
            val intent = Intent(Constant.STORY_PLAYER_EVENT)
            intent.putExtra("EVENT", Constant.STORY_RESULT_CODE)
            intent.putExtra(Constant.isVideoStory, isVideoStory)
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        }
    }
}
