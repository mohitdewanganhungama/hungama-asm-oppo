package com.hungama.music.ui.main.view.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.data.model.BookmarkDataModel
import com.hungama.music.data.model.ContentTypes
import com.hungama.music.data.model.LibraryMusicModel
import com.hungama.music.data.model.PlanNames
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.home.eventreporter.UserAttributeEvent
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.LibraryVideoAdapter
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant
import com.hungama.music.utils.show
import com.hungama.music.R
import kotlinx.android.synthetic.main.fr_video_download_all.*
import kotlinx.coroutines.launch


class VideoDownloadAllFragment() : BaseFragment(), TextWatcher {

    companion object {
        val MOVIE_LISTING = 8001
        val TV_SHOW_LISTING = 8002
        val MUSIC_VIDEO_SONGS = 8003
        val SHORT_FILMS = 8004
        val SHORT_VIDEOS = 8005
    }

    private lateinit var musicLibarayAdapter: LibraryVideoAdapter
    private var musicplayList = ArrayList<LibraryMusicModel>()
    var userViewModel: UserViewModel? = null
    var bookmarkDataModel: BookmarkDataModel? = null

    var downloadedMoviesSize=0
    var downloadedTVShowSize=0
    var downloadedMusicVideoSize=0
    var downloadedShortFilmSize=0
    var downloadedShortVideoSize=0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fr_video_download_all, container, false)
    }
    override fun initializeComponent(view: View) {
        baseMainScope.launch {
            downloadedMoviesSize=0
            downloadedTVShowSize=0
            downloadedMusicVideoSize=0
            downloadedShortFilmSize=0
            downloadedShortVideoSize=0

            CommonUtils.applyButtonTheme(requireContext(), btnDownload)
            musicplayList = ArrayList<LibraryMusicModel>()
            //val contentTypes:Array<Int> = arrayOf(ContentTypes.MOVIES.value)
            val downloadedMoviesTotal = AppDatabase?.getInstance()?.downloadedAudio()
                ?.getDownloadQueueItemsByContentType(ContentTypes.MOVIES.value)
            val downloadQueuededMoviesTotal = AppDatabase?.getInstance()?.downloadQueue()
                ?.getDownloadQueueItemsByContentType(ContentTypes.MOVIES.value)

            var movieSubTitle="0 " + getString(R.string.download_str_18)
            if(downloadQueuededMoviesTotal!=null&&downloadQueuededMoviesTotal?.size!!>0){
                downloadedMoviesSize +=downloadQueuededMoviesTotal.size
            }
            if (downloadedMoviesTotal!=null&&downloadedMoviesTotal?.size!!>0){
                downloadedMoviesSize+=downloadedMoviesTotal.size
            }
            if (downloadedMoviesSize > 0){
                movieSubTitle=""+downloadedMoviesSize+" " + getString(R.string.download_str_18)

                val userDataMap= java.util.HashMap<String, String>()
                userDataMap.put(EventConstant.NUMBER_OF_DOWNLOADED_VIDEOS, ""+ downloadedMoviesSize)
                EventManager.getInstance().sendUserAttribute(UserAttributeEvent(userDataMap))
                musicplayList.add(
                    LibraryMusicModel(
                        ""+
                                MOVIE_LISTING,
                        getString(R.string.search_str_6),
                        movieSubTitle, "", ""
                    )
                )
            }

            val downloadedTVShowTotal = AppDatabase?.getInstance()?.downloadedAudio()
                ?.getDownloadQueueItemsByContentType(ContentTypes.TV_SHOWS.value)

            val downloadQueuededTVShowTotal = AppDatabase?.getInstance()?.downloadQueue()
                ?.getDownloadQueueItemsByContentType(ContentTypes.TV_SHOWS.value)

            var tvShowSubTitle="0 " + getString(R.string.download_str_19)


            if(downloadQueuededTVShowTotal!=null&&downloadQueuededTVShowTotal?.size!!>0){
                downloadedTVShowSize +=downloadQueuededTVShowTotal.size
            }
            if (downloadedTVShowTotal!=null&&downloadedTVShowTotal?.size!!>0){
                downloadedTVShowSize+=downloadedTVShowTotal.size
            }
            if (downloadedTVShowSize > 0){
                tvShowSubTitle=""+downloadedTVShowSize + " " + getString(R.string.download_str_19)
                musicplayList.add(
                    LibraryMusicModel(
                        ""+
                                TV_SHOW_LISTING,
                        getString(R.string.search_str_8),
                        tvShowSubTitle, "", ""
                    )
                )
            }



            val downloadedMusicVideoTotal = AppDatabase?.getInstance()?.downloadedAudio()
                ?.getDownloadQueueItemsByContentType(ContentTypes.VIDEO.value)
            val downloadedQueuedMusicVideoTotal = AppDatabase?.getInstance()?.downloadQueue()
                ?.getDownloadQueueItemsByContentType(ContentTypes.VIDEO.value)

            var musicVideoSubTitle="0 " + getString(R.string.artist_str_9)

            if(downloadedQueuedMusicVideoTotal!=null&&downloadedQueuedMusicVideoTotal?.size!!>0){
                downloadedMusicVideoSize +=downloadedQueuedMusicVideoTotal.size
            }
            if (downloadedMusicVideoTotal!=null&&downloadedMusicVideoTotal?.size!!>0){
                downloadedMusicVideoSize+=downloadedMusicVideoTotal.size
            }
            if (downloadedMusicVideoSize > 0){
                musicVideoSubTitle=""+downloadedMusicVideoSize + " " + getString(R.string.artist_str_9)
                musicplayList.add(
                    LibraryMusicModel(
                        ""+
                                MUSIC_VIDEO_SONGS,
                        getString(R.string.artist_str_9),
                        musicVideoSubTitle, "", ""
                    )
                )
            }

            //val contentTypes:Array<Int> = arrayOf(ContentTypes.SHORT_FILMS.value)
            var downloadedShortFilmTotal = AppDatabase?.getInstance()?.downloadedAudio()
                ?.getDownloadQueueItemsByContentType(ContentTypes.SHORT_FILMS.value)
            var downloadQueuededShortFilmTotal = AppDatabase?.getInstance()?.downloadQueue()
                ?.getDownloadQueueItemsByContentType(ContentTypes.SHORT_FILMS.value)

            var shortFilmSubTitle="0 " + getString(R.string.library_video_str_6)
            if(!downloadQueuededShortFilmTotal.isNullOrEmpty()){
                downloadedShortFilmSize +=downloadQueuededShortFilmTotal.size
            }
            if (!downloadedShortFilmTotal.isNullOrEmpty()){
                downloadedShortFilmSize+=downloadedShortFilmTotal.size
            }
            if (downloadedShortFilmSize > 0){
                shortFilmSubTitle=""+downloadedShortFilmSize+" " + getString(R.string.library_video_str_6)

                val userDataMap= java.util.HashMap<String, String>()
                userDataMap.put(EventConstant.NUMBER_OF_DOWNLOADED_SHORT_FILM, ""+ downloadedShortFilmSize)
                EventManager.getInstance().sendUserAttribute(UserAttributeEvent(userDataMap))
                musicplayList.add(
                    LibraryMusicModel(
                        ""+
                                SHORT_FILMS,
                        getString(R.string.library_video_str_6),
                        shortFilmSubTitle, "", ""
                    )
                )
            }

            //val contentTypes:Array<Int> = arrayOf(ContentTypes.SHORT_VIDEO.value)
            val downloadedShortVideoTotal = AppDatabase?.getInstance()?.downloadedAudio()
                ?.getDownloadQueueItemsByContentType(ContentTypes.SHORT_VIDEO.value)
            val downloadQueuededShortVideoTotal = AppDatabase?.getInstance()?.downloadQueue()
                ?.getDownloadQueueItemsByContentType(ContentTypes.SHORT_VIDEO.value)

            var shortVideoSubTitle="0 " + getString(R.string.library_video_str_8)
            if(!downloadQueuededShortVideoTotal.isNullOrEmpty()){
                downloadedShortVideoSize +=downloadQueuededShortVideoTotal.size
            }
            if (!downloadedShortVideoTotal.isNullOrEmpty()){
                downloadedShortVideoSize+=downloadedShortVideoTotal.size
            }
            if (downloadedShortVideoSize > 0){
                shortVideoSubTitle=""+downloadedShortVideoSize+" " + getString(R.string.library_video_str_8)

                val userDataMap= java.util.HashMap<String, String>()
                userDataMap.put(EventConstant.NUMBER_OF_DOWNLOADED_SHORT_VIDEO, ""+ downloadedShortVideoSize)
                EventManager.getInstance().sendUserAttribute(UserAttributeEvent(userDataMap))
                musicplayList.add(
                    LibraryMusicModel(
                        ""+
                                SHORT_VIDEOS,
                        getString(R.string.library_video_str_8),
                        shortVideoSubTitle, "", ""
                    )
                )
            }

            if (!musicplayList.isNullOrEmpty()){
                clExplore.visibility = View.GONE
                clSearchView?.show()
                rvMusicPlaylist?.show()
            }


            btnDownload?.setOnClickListener {
                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        CommonUtils.hapticVibration(requireContext(), btnDownload!!,
                            HapticFeedbackConstants.CONTEXT_CLICK
                        )
                    }
                    (activity as MainActivity).applyScreen(2)
                }catch (e:Exception){

                }
                // clExplore.visibility = View.GONE
                // clSearchView.visibility = View.VISIBLE

            }

            musicLibarayAdapter = LibraryVideoAdapter(requireContext()!!, musicplayList,object : LibraryVideoAdapter.PlayListItemClick{
                override fun libraryItemOnClick(musicData: LibraryMusicModel) {
                    if(musicData?.id.equals(""+ MOVIE_LISTING) || musicData?.id.equals(""+ SHORT_FILMS)){
                        if(downloadedMoviesSize>0 || downloadedShortFilmSize>0){
                            var contentType = ContentTypes.MOVIES.value
                            if (musicData?.id.equals(""+ SHORT_FILMS)){
                                contentType = ContentTypes.SHORT_FILMS.value
                            }
                            val bundle = Bundle()
                            bundle.putInt(Constant.CONTENT_TYPE, contentType)
                            val fragment = MovieDownloadFragment()
                            fragment.arguments = bundle
                            addFragment(R.id.fl_container,this@VideoDownloadAllFragment,
                                fragment,false)
                        }

                    }else if(musicData?.id.equals(""+ TV_SHOW_LISTING)){
                        if(downloadedTVShowSize>0){
                            addFragment(R.id.fl_container,this@VideoDownloadAllFragment,
                                DownloadedEpsiodesFragment(),false)
                        }

                    }else if (musicData?.id.equals(""+ MUSIC_VIDEO_SONGS) || musicData?.id.equals(""+ SHORT_VIDEOS)){
                        if(downloadedMusicVideoSize>0 || downloadedShortVideoSize>0){
                            var contentType = ContentTypes.VIDEO.value
                            if (musicData?.id.equals(""+ SHORT_VIDEOS)){
                                contentType = ContentTypes.SHORT_VIDEO.value
                            }
                            val bundle = Bundle()
                            bundle.putInt(Constant.CONTENT_TYPE, contentType)
                            val fragment = MusicVideoDownloadFragment()
                            fragment.arguments = bundle
                            addFragment(R.id.fl_container,this@VideoDownloadAllFragment,
                                fragment,false)

                        }

                    }

                }
            })
            rvMusicPlaylist.layoutManager =
                LinearLayoutManager(requireContext()!!, LinearLayoutManager.VERTICAL, false)
            rvMusicPlaylist.adapter = musicLibarayAdapter
            musicLibarayAdapter.notifyDataSetChanged()


            val fragment=LibraryMainTabFragment()
            fragment?.addReloadListener(object :LibraryMainTabFragment.onReloadListener{
                override fun onRefresh() {
                    if (isAdded) {
                        if (requireView() != null) {
                            setLog(TAG, "onHiddenChanged: initializeComponent called")
                            initializeComponent(requireView())
                        }
                    }
                }

            })
            downloadSearch?.addTextChangedListener(this@VideoDownloadAllFragment)
        }

    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return true
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }

    override fun afterTextChanged(s: Editable?) {
        val searchText = downloadSearch?.text.toString().trim()
        musicLibarayAdapter?.filter?.filter(searchText)
    }


}