package com.hungama.music.ui.main.view.fragment

import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.HungamaMusicApp
import com.hungama.music.R
import com.hungama.music.data.model.MessageModel
import com.hungama.music.data.model.MessageType
import com.hungama.music.utils.customview.bottomsheet.SuperBottomSheetFragment
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.eventreporter.AddedToPlaylistEvent
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.ui.main.adapter.UserProfilePlaylistsAdapter
import com.hungama.music.data.model.PlaylistRespModel
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.utils.ConnectionUtil
import com.hungama.music.utils.Utils
import com.hungama.music.ui.main.viewmodel.PlayableContentViewModel
import com.hungama.music.ui.main.viewmodel.PlaylistViewModel
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import kotlinx.android.synthetic.main.add_to_playlist_menu_fragment.*
import kotlinx.android.synthetic.main.fr_my_playlist_detail.*
import org.json.JSONObject

class AddToPlaylistMenuFragment(val contentId: String?, val contentName:String, val contentType: String) : SuperBottomSheetFragment(), CreatePlaylistDialog.createPlayListListener  {

    var userViewModel: UserViewModel? = null
    var playlistListViewModel: PlaylistViewModel? = null
    var userPlaylist: ArrayList<PlaylistRespModel.Data> = ArrayList()
    var defaultPlaylistName = ""
    var height=0
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.add_to_playlist_menu_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setSheetHeight(0)
        defaultPlaylistName = getString(R.string.hungama_playlist) + " 1"
        add_to_playlist_menu_close.setOnClickListener {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), add_to_playlist_menu_close!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            closeDialog()
        }
        setUpViewModel()
        CommonUtils.PageViewEvent("","","",
            "", MainActivity.lastItemClicked,"popup_add to playlist","")
        CommonUtils.applyButtonTheme(requireContext(), llCreatePlaylist)
        llCreatePlaylist.setOnClickListener {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), llCreatePlaylist!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            createPlaylistPopup()

            CommonUtils.PageViewEvent("","","", "", MainActivity.lastItemClicked,"popup_create playlist","")
        }

    }

    override fun getCornerRadius() = requireContext().resources.getDimension(R.dimen.common_popup_round_corner)
    override fun getStatusBarColor() = Color.RED
    override fun isSheetAlwaysExpanded(): Boolean = true
    override fun isSheetCancelableOnTouchOutside(): Boolean  = true

    override fun getExpandedHeight(): Int{
        //dimen_525
        setLog("TAG", "getExpandedHeight height:${height}")
        return height
    }
    override fun getBackgroundColor(): Int = ContextCompat.getColor(requireContext(), R.color.transparent)

    private fun setUpViewModel() {
        userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)

        playlistListViewModel = ViewModelProvider(
            this
        ).get(PlaylistViewModel::class.java)

        val playableShuffledContentViewModel = ViewModelProvider(
            this
        ).get(PlayableContentViewModel::class.java)


        if (ConnectionUtil(context).isOnline) {
            playableShuffledContentViewModel?.getPlayableContentList(requireContext(), contentId!!)
        } else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }

        getUserPlaylistData()
    }


    protected fun setProgressBarVisible(visible: Boolean) {
        if (pb_progress != null) {
            if (visible) {
                pb_progress?.visibility = View.VISIBLE
            } else {
                pb_progress?.visibility = View.GONE
            }

        }
    }


    private fun getUserPlaylistData() {
        if (isAdded && context != null){
            if (ConnectionUtil(requireContext()).isOnline) {
                userViewModel?.getUserPlaylistData(
                    requireContext(),
                    SharedPrefHelper.getInstance().getUserId()!!
                )?.observe(this,
                    Observer {
                        when(it.status){
                            Status.SUCCESS->{
                                setProgressBarVisible(false)
                                if (it?.data != null) {
                                    setUpLists(it?.data)
                                }

                            }

                            Status.LOADING ->{
                                setProgressBarVisible(true)
                            }

                            Status.ERROR ->{
                                setProgressBarVisible(false)
                                Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                            }
                        }
                    })
            } else {
                val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                    MessageType.NEGATIVE, true)
                CommonUtils.showToast(requireContext(), messageModel)
            }
        }
    }

    private fun setUpLists(userPlaylistData: PlaylistRespModel) {
        if (userPlaylistData?.data != null && userPlaylistData?.data?.size!! > 0) {
            userPlaylist = userPlaylistData?.data!!
            if (!userPlaylist.isNullOrEmpty()){
                setSheetHeight(userPlaylist.size)
            }
            var lastNum = 1
            userPlaylist?.forEachIndexed { index, item ->
                if (item?.data?.type == 99999){
                    if (item?.data?.title?.contains("Hungama Playlist ") == true){
                        val count = item.data.title.filter { it.isDigit() }
                        if (!TextUtils.isEmpty(count)){
                            try {
                                val num = count.toInt() + 1
                                if (lastNum < num){
                                    lastNum = num
                                    defaultPlaylistName = getString(R.string.hungama_playlist) + " " + lastNum
                                }
                            }catch (e:Exception){

                            }
                        }
                    }
                }
            }
            rvAddToPlaylist.apply {
                layoutManager =
                    GridLayoutManager(context, 1)
                adapter = UserProfilePlaylistsAdapter(
                    context, userPlaylist,
                    object : UserProfilePlaylistsAdapter.OnChildItemClick {
                        override fun onUserClick(childPosition: Int) {
                            if (!TextUtils.isEmpty(contentId)){
                                if (ConnectionUtil(requireContext()).isOnline) {
                                    val jsonObject = JSONObject()
                                    jsonObject.put("contentid",contentId)
                                    playlistListViewModel?.addSong(requireContext(),jsonObject,userPlaylist.get(childPosition).data.id)



                                    val messageModel = MessageModel(getString(R.string.playlist_str_4), MessageType.NEUTRAL, true)
                                    CommonUtils.showToast(requireContext(), messageModel)
                                    
                                    callAddToPlaylistEvent(userPlaylist.get(childPosition))
                                }
                            }
                            closeDialog()
                        }
                    }
                )
                setRecycledViewPool(RecyclerView.RecycledViewPool())
                setHasFixedSize(true)
            }
            tvAllPlaylist.visibility = View.VISIBLE
            rvAddToPlaylist.visibility = View.VISIBLE
        } else {
            tvAllPlaylist.visibility = View.GONE
            rvAddToPlaylist.visibility = View.GONE
            setSheetHeight(0)
            createPlaylistPopup()
        }
        setLog("TAG", "callAddToPlaylistEvent: userPlaylist size ${userPlaylist?.size}")
//        getExpandedHeight()
    }

    private fun callAddToPlaylistEvent(modelItem: PlaylistRespModel.Data) {
        val hashMap = HashMap<String,String>()
        val model = HungamaMusicApp.getInstance().getEventData(contentId!!)

        setLog("TAG", "callAddToPlaylistEvent: modelItem${modelItem}")
        setLog("TAG", "callAddToPlaylistEvent: model${model}")

        hashMap.put(EventConstant.ACTOR_EPROPERTY,model.actor)
        hashMap.put(EventConstant.CONTENTID_EPROPERTY,contentId)
        hashMap.put(EventConstant.CONTENTNAME_EPROPERTY,contentName)
        if (contentType.toString().equals("21")){
            hashMap.put(EventConstant.CONTENTTYPE_EPROPERTY,"Song")
        }else{
            hashMap.put(EventConstant.CONTENTTYPE_EPROPERTY,"" + Utils.getContentTypeName("" + contentType))
        }

        hashMap.put(EventConstant.GENRE_EPROPERTY,""+model?.genre)
        hashMap.put(EventConstant.LANGUAGE_EPROPERTY,""+model?.language)
        hashMap.put(EventConstant.LYRICIST_EPROPERTY,""+model?.lyricist)
        hashMap.put(EventConstant.MOOD_EPROPERTY,""+model.mood)
        hashMap.put(EventConstant.MUSICDIRECTOR_EPROPERTY,""+model?.musicDirectorComposer)

        hashMap.put(EventConstant.PLAYLISTNAME_EPROPERTY,""+modelItem?.data?.title)
        hashMap.put(EventConstant.PODCASTNAME_EPROPERTY,""+model?.pName)
        hashMap.put(EventConstant.PODCASTHOST_EPROPERTY,""+model?.podcast_host)
        hashMap.put(EventConstant.SINGER_EPROPERTY,""+model?.singer)
        hashMap.put(EventConstant.TEMPO_EPROPERTY,""+model?.tempo)
        hashMap.put(EventConstant.YEAROFRELEASE_EPROPERTY,""+model?.release_Date)
        EventManager.getInstance().sendEvent(AddedToPlaylistEvent(hashMap))
    }

    override fun playListCreatedSuccessfull(id: String) {
        setLog("TAG", "playListCreatedSuccessfull id:${id}")
        getUserPlaylistData()

        if (!TextUtils.isEmpty(contentId)){
            if (ConnectionUtil(requireContext()).isOnline) {
                val jsonObject = JSONObject()
                jsonObject.put("contentid",contentId)
                playlistListViewModel?.addSong(requireContext(),jsonObject,id)

                val messageModel = MessageModel(getString(R.string.playlist_str_4), MessageType.NEUTRAL, true)
                CommonUtils.showToast(requireContext(), messageModel)
            }
        }
        closeDialog()
    }

    private fun createPlaylistPopup(){
            val createPlaylistDialog =
                CreatePlaylistDialog(this@AddToPlaylistMenuFragment, defaultPlaylistName)
            createPlaylistDialog.show(
                activity?.supportFragmentManager!!,
                "open createplaylist dialog"
            )

    }

    private fun setSheetHeight(noOItems:Int){
        //Approx 65dp difference
        if (noOItems > 4){
            height=requireContext().resources.getDimension(R.dimen.dimen_545).toInt()
        }else if (noOItems == 4){
            height=requireContext().resources.getDimension(R.dimen.dimen_525).toInt()
        }else if (noOItems == 3){
            height=requireContext().resources.getDimension(R.dimen.dimen_460).toInt()
        }else if (noOItems == 2){
            height=requireContext().resources.getDimension(R.dimen.dimen_395).toInt()
        }else if (noOItems == 1){
            height=requireContext().resources.getDimension(R.dimen.dimen_330).toInt()
        }else{
            height=requireContext().resources.getDimension(R.dimen.dimen_245).toInt()
        }
        getExpandedHeight()
        resetExpendedHeight()
    }
}

