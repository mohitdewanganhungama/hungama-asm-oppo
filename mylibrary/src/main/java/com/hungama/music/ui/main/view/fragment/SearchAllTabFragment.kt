package com.hungama.music.ui.main.view.fragment
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import androidx.media3.common.util.Util
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hungama.music.HungamaMusicApp
import com.hungama.music.data.model.CallSearchResultClicked
import com.hungama.music.data.model.*
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.*
import com.hungama.music.player.audioplayer.Injection
import com.hungama.music.player.audioplayer.TracksContract
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.services.AudioPlayerService
import com.hungama.music.player.audioplayer.viewmodel.TracksViewModel
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.*
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.viewmodel.PlayableContentViewModel
import com.hungama.music.ui.main.viewmodel.SearchViewModel
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.removeItemsFromFirst
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant.EXTRA_PAGE_DETAIL_NAME
import com.hungama.music.utils.Constant.REQUEST_CODE_STT
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.music.R
import kotlinx.android.synthetic.main.common_header_action_bar.*
import kotlinx.android.synthetic.main.fragment_search_all_tab.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.reflect.Type
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap


class SearchAllTabFragment : BaseFragment(), TabLayout.OnTabSelectedListener,
    SuggestionAdapter.SearchItem, SearchBucketAdapter.SearchResult, TextWatcher,
    TextView.OnEditorActionListener,
    BaseActivity.OnLocalBroadcastEventCallBack, SearchAdapter.SearchResult, TracksContract.View,
    ClearAllRecentSearchDialog.OnClearRecentSearch,SearchBottomSheet.clickItem, PermissionCallbacks,
    CallSearchResultClicked {
    var fragmentList: ArrayList<Fragment> = ArrayList()
    var fragmentName: ArrayList<String> = ArrayList()
    var deeplinkVoiceSearchText = ""
    var isDeeplinkVoiceSearchText = false
    var deeplinkSearchText = ""
    var suggestionList = ArrayList<SuggestionTextModel>()
    lateinit var searchRespModel: SearchRespModel
    lateinit var recentSearchAdapter: SearchAdapter
    lateinit var searchRecommendedAdapter : SearchRecommendedAdapter
    var searchDataList = mutableListOf<SearchBucketModel>()
    var searchViewModel:SearchViewModel? = null
    var rowList:ArrayList<SearchRespModel.Data.Body.Row?>? = ArrayList()
    var lastTabClicked = ""
    var recentSearchCount = 0
    var performed_type = ""
    var autosuggestion_clicked_position = ""
    var autosuggestion_count = ""
    var keyword_uttered = ""

    lateinit var callSearchResultClicked : CallSearchResultClicked

    private val textToSpeechEngine: TextToSpeech by lazy {
        TextToSpeech(requireContext()
        ) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeechEngine.language = Locale.ENGLISH
            }
        }
    }
    var playableContentViewModel: PlayableContentViewModel = PlayableContentViewModel()
    var CONTENT_TYPE = 0
    private lateinit var tracksViewModel: TracksContract.Presenter
    var playableItemPosition = 0
    var playableItem = BodyRowsItemsItem()
    private var isLastDurationPlay = false
    var defaultSelectedTabPosition = 0

    private lateinit var permissionUtils: PermissionUtils
    private var bAlreadyAskedForRecordPermission: Boolean = false
    var isAutoSuggestClick = false
    var suggestionAdapter: SuggestionAdapter? = null
    var recentSearchListItem = ArrayList<BodyRowsItemsItem>()

    companion object {
        val all = 1
        val song = 2
        val album = 3
        val artist = 4
        val podcast = 5
        val radio = 6
        val playlist = 7
        val movie = 8
        val tvshow = 9
        val musicVideo = 10
        val shortFilm = 11
        val shortVideo = 12
        val liveEvent = 13


        var lastSearchText=""


        fun getRecentSearchList(): ArrayList<BodyRowsItemsItem> {
            var recentSearchList: ArrayList<BodyRowsItemsItem> = ArrayList()
            try {
                val str = SharedPrefHelper.getInstance().getRecentSearchList()
                val gson = Gson()
                val searchListType: Type? = object : TypeToken<ArrayList<BodyRowsItemsItem>>() {}.type

                setLog("searchTab", "SearchAllTabFragment-getRecentSearchList()-str-$str")
                if (!TextUtils.isEmpty(str)){
                    recentSearchList= gson.fromJson(
                        str,
                        searchListType
                    )
                }
                if (!recentSearchList.isNullOrEmpty() && recentSearchList.size > 10){
                    val removableSize = recentSearchList.size - 10
                    recentSearchList.removeItemsFromFirst(removableSize)
                }
            }catch (e:Exception){

            }

            setLog("searchTab", "SearchAllTabFragment-getRecentSearchList()-recentSearchList-$recentSearchList")
            return recentSearchList
        }

        var fragment = SearchAllTabFragment()
        fun newInstance(mContext: Context?, bundle: Bundle): SearchAllTabFragment {
            if(fragment==null){
                fragment = SearchAllTabFragment()
            }
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_all_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        permissionUtils = PermissionUtils()
        if (savedInstanceState != null) {
            bAlreadyAskedForRecordPermission =
                savedInstanceState.getBoolean(KEY_RECORD_PERMISSION_ALREADY_ASKED, false)
        }

        callGetRecentSearh()
    }



    @SuppressLint("UseRequireInsteadOfGet")
    override fun initializeComponent(view: View) {

        callSearchResultClicked = this as CallSearchResultClicked
        if (arguments != null){
            if (requireArguments().containsKey(EXTRA_PAGE_DETAIL_NAME)){
                deeplinkSearchText = requireArguments().getString(EXTRA_PAGE_DETAIL_NAME).toString()
            }

            if (requireArguments().containsKey(Constant.isDeeplinkVoiceSearchText)){
                isDeeplinkVoiceSearchText = requireArguments().getBoolean(Constant.isDeeplinkVoiceSearchText, false)
            }

            if (requireArguments().containsKey(Constant.deeplinkVoiceSearchText)){
                deeplinkVoiceSearchText = requireArguments().getString(Constant.deeplinkVoiceSearchText).toString()
            }
        }

        var isTabOnSearch = true
        ivBack?.setOnClickListener { backPress()
            isTabOnSearch = false
        }
        tvActionBarHeading?.text = getString(R.string.search_str_10)
        headBarBlur?.hide()
        shimmerLayout.visibility = View.VISIBLE
        shimmerLayout.startShimmer()
        tracksViewModel = TracksViewModel(Injection.provideTrackRepository(), this)
        loadRecommandation()
        et_Search.onFocusChangeListener = View.OnFocusChangeListener { v, b ->
            iv_mic.visibility = View.GONE
            iv_cancel_search.visibility = View.VISIBLE
            //tabs.visibility = View.GONE
            //vpTransactions.visibility = View.GONE
            shimmerLayout.visibility = View.GONE
            shimmerLayout.stopShimmer()
            rlRecommendation.visibility = View.GONE
            fl_container_recommanded.visibility = View.GONE
            topBlurView.visibility = View.GONE
            if (TextUtils.isEmpty(et_Search?.text.toString().trim())){
                showRecentSearch(true)
            }

            if (isTabOnSearch){
                CommonUtils.PageViewEvent("","","","",
                    "Search",
                    "Search_bartapped","")
            }

        }

        llbtnCancel.setOnClickListener {
            et_Search?.setText("")
            activity?.let { Utils.hideSoftKeyBoard(it, view) }
            iv_mic.visibility = View.VISIBLE
            iv_cancel_search.visibility = View.GONE
            rv_search_suggestions_list.visibility = View.GONE
        }
        iv_cancel_search.setOnClickListener {
            et_Search?.setText("")
            activity?.let { Utils.hideSoftKeyBoard(it, view) }
            iv_mic.visibility = View.VISIBLE
            iv_cancel_search.visibility = View.GONE
            showRecentSearch(true)
        }

        llbtnSearch.setOnClickListener {
            callSearchApi()
        }

        btnExplore?.setOnClickListener {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), btnExplore!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }
            catch (e:Exception){

            }
            callSearchApi()
        }

        et_Search?.addTextChangedListener(this)
        et_Search?.setOnEditorActionListener(this)
        et_Search.setOnClickListener{
            val dataMap = HashMap<String, String>()
            dataMap.put(EventConstant.RECENT_SEARCH_COUNT, recentSearchCount.toString())
            setLog("PageType","Type${dataMap}")

            EventManager.getInstance().sendEvent(ClickSearchBar(dataMap))
        }
        clearRecentSearch()

        iv_mic?.setOnClickListener {
//            callSpeechToText()

            if (!bAlreadyAskedForRecordPermission) {

                bAlreadyAskedForRecordPermission = permissionUtils.requestPermissionsWithRationale(
                    this,
                    Manifest.permission.RECORD_AUDIO,
                    RECORD_AUDIO_REQ_CODE,
                    this
                )

                CommonUtils.PageViewEvent("","","","",
                    "Search",
                    "Search_voicesearch","")
            }

            val dataMap = HashMap<String, String>()
            dataMap.put(EventConstant.RECENT_SEARCH_COUNT, recentSearchCount.toString())

            setLog("PageType","Type${dataMap}")

            EventManager.getInstance().sendEvent(VoiceSearchClicked(dataMap))
        }

        if (isDeeplinkVoiceSearchText){
            et_Search.setText(deeplinkVoiceSearchText)
            callSearchApi()
        }
//        CommonUtils.setPageBottomSpacing(rv_search_suggestions_list, requireContext(),
//            resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
//            resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
        mainHandler = Handler(Looper.getMainLooper())

        CoroutineScope(Dispatchers.IO).launch {

            CommonUtils.PageViewEvent("","","","",
                MainActivity.lastItemClicked + "_" + MainActivity.headerItemName,
                "Search","")
        }
    }
    private fun clearRecentSearch(){
        tvClearSearch.setOnClickListener {
            Utils.hideSoftKeyBoard(requireActivity(),requireView())
            val clearAllRecentSearchDialog = ClearAllRecentSearchDialog(this,getRecentSearchList()?.size!!)
            clearAllRecentSearchDialog.show(requireFragmentManager(),"open dialog")
        }
    }

    fun setStyleForTab(tab: TabLayout.Tab, style: Int, typeface: Typeface?) {
        tab.view.children.find { it is TextView }?.let { tv ->
            (tv as TextView).post {
                tv.setTypeface(typeface, style)
            }
        }
    }

    var pageChangeCallback = object : ViewPager.OnPageChangeListener {
        override fun onPageSelected(position: Int) {
            setLog("onPageSelected", "Selected position:" + position)
            //vpTransactions?.adapter?.notifyItemChanged(position)
        }

        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {

        }

        override fun onPageScrollStateChanged(state: Int) {

        }
    }

    override fun onClick(v: View) {
        super.onClick(v)

    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden && activity != null) {
            (activity as BaseActivity).showBottomNavigationBar()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return false
    }




    override fun SearchBarItemClick(position: Int) {
        autosuggestion_clicked_position = position.toString()
        rv_search_suggestions_list.visibility = View.GONE
        tabs.visibility = View.VISIBLE
        vpTransactions.visibility = View.VISIBLE
        showRecentSearch(false)
        iv_mic.visibility = View.VISIBLE
        iv_cancel_search.visibility = View.GONE
        activity?.let { view?.let { it1 -> Utils.hideSoftKeyBoard(it, it1) } }

        isAutoSuggestClick = true
        if(!suggestionList.isNullOrEmpty()&&suggestionList?.get(position)!=null){
            et_Search?.setText(""+suggestionList.get(position).suggestion)
        }

        loadSearchAPI()
        keyword_uttered = ""
        performed_type = "autosuggest"
        callSearchPerFormed("autosuggest", position.toString(),)
    }

    override fun SearchItemClick() {
        rv_search_suggestions_list.visibility = View.GONE
        tabs.visibility = View.VISIBLE
        vpTransactions.visibility = View.VISIBLE
        showRecentSearch(false)
        iv_mic.visibility = View.VISIBLE
        iv_cancel_search.visibility = View.GONE
        activity?.let { view?.let { it1 -> Utils.hideSoftKeyBoard(it, it1) } }
    }

    private fun callSearchApi(searchText:String){
        lastSearchText=searchText
        searchViewModel = ViewModelProvider(
            this
        ).get(SearchViewModel::class.java)


        if (ConnectionUtil(activity).isOnline) {
            searchViewModel?.getSearchData(requireActivity(), searchText.trim())?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null) {

                                setAllTabs(it?.data)
                                CoroutineScope(Dispatchers.IO).launch {
                                    val hashMap = java.util.HashMap<String, String>()
                                    hashMap.put(EventConstant.KEYWORD_ENTERED_EPROPERTY,et_Search.text.toString())
                                    hashMap.put(EventConstant.KEYWORD_UTTERED_EPROPERTY, keyword_uttered)
                                    hashMap.put(EventConstant.PERFORMED_TYPE, performed_type)
                                    hashMap.put(EventConstant.NO_RESULT_FOUND, "")
                                    hashMap.put(EventConstant.PAGENAME, "searchresult")
                                    hashMap.put(EventConstant.FILTER_NAME, et_Search.text.toString())
                                    hashMap[EventConstant.RESULT_COUNT_ALL] = if (it.data.data?.body?.all?.size!! > 0) it.data.data!!.body?.all?.size.toString() else ""
                                    hashMap[EventConstant.RESULT_COUNT_SONG] = if (it.data.data?.body?.song?.size!! > 0) it.data.data!!.body?.song?.size.toString() else ""
                                    hashMap[EventConstant.RESULT_COUNT_ALBUM] = if (it.data.data?.body?.album?.size!! > 0) it.data.data!!.body?.album?.size.toString() else ""
                                    hashMap[EventConstant.RESULT_COUNT_PLAYLIST] = if (it.data.data?.body?.playlist?.size!! > 0) it.data.data!!.body?.playlist?.size.toString() else ""
                                    hashMap[EventConstant.RESULT_COUNT_ARTIST] = if (it.data.data?.body?.artist?.size!! > 0) it.data.data!!.body?.artist?.size.toString() else ""
                                    hashMap[EventConstant.RESULT_COUNT_MOVIES] = if (it.data.data?.body?.movie?.size!! > 0) it.data.data!!.body?.movie?.size.toString() else ""
                                    hashMap[EventConstant.RESULT_COUNT_SHORTFILMS] = if (it.data.data?.body?.shortfilm?.size!! > 0) it.data.data!!.body?.shortfilm?.size.toString() else ""
                                    hashMap[EventConstant.RESULT_COUNT_SHORTVIDEOS] = if (it.data.data?.body?.shortvideo?.size!! > 0) it.data.data!!.body?.shortvideo?.size.toString() else ""
                                    hashMap[EventConstant.RESULT_COUNT_TVSHOW] = if (it.data.data?.body?.tvshow?.size!! > 0) it.data.data!!.body?.tvshow?.size.toString() else ""
                                    hashMap[EventConstant.RESULT_COUNT_LIVEEVENT] = if (it.data.data?.body?.liveEvent?.size!! > 0) it.data.data!!.body?.liveEvent?.size.toString() else ""
                                    hashMap[EventConstant.RESULT_COUNT_PODCAST] = if (it.data.data?.body?.podcast?.size!! > 0) it.data.data!!.body?.podcast?.size.toString() else ""
                                    hashMap[EventConstant.RESULT_COUNT_MUSICVIDEOS] = if (it.data.data?.body?.musicvideo?.size!! > 0) it.data.data!!.body?.musicvideo?.size.toString() else ""
                                    EventManager.getInstance().sendEvent(SearchResultPopulated(hashMap))
                                }
                            }
                        }

                        Status.LOADING ->{
                            setProgressBarVisible(true)
                        }

                        Status.ERROR ->{
                            setEmptyVisible(false)
                            setProgressBarVisible(false)
//                            Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                        }
                    }
                })
        }else{
            val messageModel = MessageModel(getString(R.string.toast_message_5), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }

    }



    private fun setAllTabs(searchRespModel: SearchRespModel) {
        if (searchRespModel?.data != null && searchRespModel?.data?.body != null){
            fragmentList = ArrayList()
            fragmentName = ArrayList()
            this.searchRespModel = searchRespModel
            if (!searchRespModel.data?.body?.song.isNullOrEmpty()
                || !searchRespModel.data?.body?.album.isNullOrEmpty()
                || !searchRespModel.data?.body?.artist.isNullOrEmpty()
                || !searchRespModel.data?.body?.podcast.isNullOrEmpty()
                || !searchRespModel.data?.body?.radio.isNullOrEmpty()
                || !searchRespModel.data?.body?.playlist.isNullOrEmpty()
                || !searchRespModel.data?.body?.movie.isNullOrEmpty()
                || !searchRespModel.data?.body?.tvshow.isNullOrEmpty()
                || !searchRespModel.data?.body?.musicvideo.isNullOrEmpty()
                || !searchRespModel.data?.body?.shortvideo.isNullOrEmpty()) {

            }else{
                clNoResultFound.visibility = View.VISIBLE
                val messageModel = MessageModel(getString(R.string.search_str_21), MessageType.NEUTRAL, true)
                //CommonUtils.showToast(requireContext(), messageModel)

                CoroutineScope(Dispatchers.IO).launch {
                    val hashMap = HashMap<String, String>()
                    hashMap.put(EventConstant.PAGENAME_EPROPERTY,"searchresult" + if (!tabs.getTabAt(defaultSelectedTabPosition)?.text.toString().isNullOrEmpty()) "_" +  tabs.getTabAt(defaultSelectedTabPosition)?.text.toString() else "")
                    hashMap.put(EventConstant.ACTIONS_EPROPERTY,"click")
                    hashMap.put(EventConstant.BUTTONNAME_EPROPERTY,"button_tryagains")
                    hashMap.put(EventConstant.KEYWORD_ENTERED_EPROPERTY, et_Search.text.toString())
                    hashMap.put(EventConstant.KEYWORD_UTTERED_EPROPERTY,"")
                    //hashMap.put(EventConstant.SOURCEPAGE_EPROPERTY,""+MainActivity.lastItemClicked+"_"+MainActivity.headerItemName+"_"+parent?.heading)
                    EventManager.getInstance().sendEvent(SearchNoResultRound(hashMap))
                }
            }

            if (!searchRespModel?.data?.body?.all.isNullOrEmpty()) {
                fragmentList.add(SearchAllFragment(searchRespModel, all, callSearchResultClicked))
                fragmentName.add("All")

                rv_search_suggestions_list.visibility = View.GONE
                clNoResultFound.visibility = View.GONE
                tabs.visibility = View.VISIBLE
                vpTransactions.visibility = View.VISIBLE
                showRecentSearch(false)
                et_Search?.clearFocus()
                iv_mic.visibility = View.VISIBLE
                iv_cancel_search.visibility = View.GONE

                //activity?.let { view?.let { it1 -> Utils.hideSoftKeyBoard(it, it1) } }
            }
            if (!searchRespModel?.data?.body?.song.isNullOrEmpty()) {
                fragmentList.add(SearchAllFragment(searchRespModel, song, callSearchResultClicked))
                fragmentName.add("Songs")
            }
            if (!searchRespModel?.data?.body?.album.isNullOrEmpty()) {
                fragmentList.add(
                    SearchAllFragment(
                    searchRespModel,
                    album, callSearchResultClicked)
                )
                fragmentName.add("Albums")
            }
            if (!searchRespModel?.data?.body?.artist.isNullOrEmpty()) {
                fragmentList.add(
                    SearchAllFragment(
                    searchRespModel,
                    artist
                        , callSearchResultClicked)
                )
                fragmentName.add("Artists")
            }
            if (!searchRespModel?.data?.body?.podcast.isNullOrEmpty()) {
                fragmentList.add(
                    SearchAllFragment(
                    searchRespModel,
                    podcast
                        , callSearchResultClicked)
                )
                fragmentName.add("Podcasts")
            }
            if (!searchRespModel?.data?.body?.radio.isNullOrEmpty()) {
                fragmentList.add(
                    SearchAllFragment(
                    searchRespModel,
                    radio
                        , callSearchResultClicked)
                )
                fragmentName.add("Radio")
            }
            if (!searchRespModel?.data?.body?.playlist.isNullOrEmpty()) {
                fragmentList.add(
                    SearchAllFragment(
                    searchRespModel,
                    playlist
                        , callSearchResultClicked)
                )
                fragmentName.add("Playlists")
            }
            if (!searchRespModel?.data?.body?.movie.isNullOrEmpty()) {
                fragmentList.add(
                    SearchAllFragment(
                    searchRespModel,
                    movie
                        , callSearchResultClicked)
                )
                fragmentName.add("Movies")
            }
            if (!searchRespModel?.data?.body?.tvshow.isNullOrEmpty()) {
                fragmentList.add(
                    SearchAllFragment(
                    searchRespModel,
                    tvshow
                        , callSearchResultClicked)
                )
                fragmentName.add("TV Shows")
            }
            if (!searchRespModel?.data?.body?.musicvideo.isNullOrEmpty()) {
                fragmentList.add(
                    SearchAllFragment(
                    searchRespModel,
                    musicVideo
                        , callSearchResultClicked)
                )
                fragmentName.add("Music Videos")
            }
            if (!searchRespModel?.data?.body?.shortfilm.isNullOrEmpty()) {
                fragmentList.add(
                    SearchAllFragment(
                        searchRespModel,
                        shortFilm
                        , callSearchResultClicked)
                )
                fragmentName.add("Short Film")
            }
            if (!searchRespModel?.data?.body?.shortvideo.isNullOrEmpty()) {
                fragmentList.add(
                    SearchAllFragment(
                    searchRespModel,
                    shortVideo
                        , callSearchResultClicked)
                )
                fragmentName.add("Short Videos")
            }

            if (!searchRespModel?.data?.body?.liveEvent.isNullOrEmpty()) {
                fragmentList.add(
                    SearchAllFragment(
                        searchRespModel,
                        liveEvent
                        , callSearchResultClicked)
                )
                fragmentName.add("Live Event")
            }
            viewPagerSetUp()
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }

    override fun afterTextChanged(s: Editable?) {
        if (s?.length == 0) {
            suggestionList?.clear()
            suggestionAdapter?.notifyDataSetChanged()
            isAutoSuggestClick=false
            setUIBasedOnSearchFocus()
            tabs.visibility=View.GONE
            vpTransactions.visibility=View.GONE
        } else {
            if(!isAutoSuggestClick && !et_Search?.text.toString().trim().isNullOrBlank()){
                callAutoSuggestionAPI(et_Search?.text.toString().trim())
            }
        }
    }

    private fun setUIBasedOnSearchFocus(){
        iv_mic.visibility = View.GONE
        iv_cancel_search.visibility = View.VISIBLE
        shimmerLayout.visibility = View.GONE
        shimmerLayout.stopShimmer()
        rlRecommendation.visibility = View.GONE
        fl_container_recommanded.visibility = View.GONE
        topBlurView.visibility = View.GONE
        if (TextUtils.isEmpty(et_Search?.text.toString().trim())){
            showRecentSearch(true)
        }
    }

    private fun loadSearchAPI(){
        if (!TextUtils.isEmpty(et_Search?.text.toString().trim())){
            rv_search_suggestions_list?.visibility=View.GONE
            llbtnCancel.visibility = View.GONE
            llbtnSearch.visibility = View.VISIBLE
            iv_mic.visibility = View.GONE
            iv_cancel_search.visibility = View.VISIBLE
            actionButtons?.visibility = View.GONE
            showRecentSearch(false)
            startSearchApiCallback()
        }else{
            llbtnCancel.visibility = View.VISIBLE
            llbtnSearch.visibility = View.GONE
            iv_mic.visibility = View.VISIBLE
            iv_cancel_search.visibility = View.GONE
            actionButtons?.visibility = View.GONE
            showRecentSearch(true)
        }
    }
    var mainHandler: Handler? = null
    private val callApi = object : Runnable{
        override fun run() {
            setLog("SearchTab", "SeatchAllTabFragment-callApi-run-callSearchApi()")
            callSearchApi()
        }
    }

    private fun startSearchApiCallback(){
        if (mainHandler != null){
            setLog("SearchTab", "SeatchAllTabFragment-startApiCallback-removeApiCallback()")
            removeApiCallback()
            setLog("SearchTab", "SeatchAllTabFragment-startApiCallback-If-postDelayed(1000)")
            mainHandler?.postDelayed(callApi, 2000)
        }else{
            mainHandler = Handler(Looper.getMainLooper())
            setLog("SearchTab", "SeatchAllTabFragment-startApiCallback-Else-postDelayed(1000)")
            mainHandler?.postDelayed(callApi, 2000)
        }
    }
    private fun removeApiCallback(){
        if (mainHandler != null){
            mainHandler?.removeCallbacks(callApi)
        }
    }
    private fun viewPagerSetUp() {


        setupViewPager(vpTransactions, fragmentList, fragmentName)

        // If we dont use setupWithViewPager() method then
        // tabs are not used or shown when activity opened
        tabs.setupWithViewPager(vpTransactions)
        vpTransactions?.setCurrentItem(defaultSelectedTabPosition, false)
        //enableLayoutBehaviour()
        //vpTransactions?.offscreenPageLimit=1
        vpTransactions.addOnPageChangeListener(pageChangeCallback)
        tabs?.addOnTabSelectedListener(this)
        tabs?.getTabAt(defaultSelectedTabPosition)?.select()
        CommonUtils.PageViewEvent("","","","",
            "Search",
            "searchresult_" + tabs?.getTabAt(defaultSelectedTabPosition)?.text.toString().lowercase(),"")
         MainActivity.subHeaderItemName = ""
        lastTabClicked = tabs?.getTabAt(defaultSelectedTabPosition)?.text.toString().lowercase()
    }

    // This function is used to add items in arraylist and assign
    // the adapter to view pager
    private fun setupViewPager(
        viewpager: ViewPager,
        fragmentList: ArrayList<Fragment>,
        fragmentName: ArrayList<String>)
    {
        val adapter = TabsViewPagerAdapter(childFragmentManager)
        adapter.addFragment(fragmentList, fragmentName)
        // setting adapter to view pager.
        viewpager.adapter = adapter
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        val typeface = ResourcesCompat.getFont(
            requireContext(),
            R.font.sf_pro_text_bold)
        tab?.let {
            setStyleForTab(it, Typeface.BOLD, typeface)
        }


        CommonUtils.PageViewEvent("","","","",
            "searchresult_"+ lastTabClicked,
            "searchresult_" + tab?.text.toString().replace(" ","").lowercase(),"")
        lastTabClicked = tab?.text.toString().replace(" ","").lowercase()
        setLog("lastcLIDCK", MainActivity.lastItemClicked)
    }

    fun resultCount(text:String):Int?{
        if (searchRespModel != null){
            when(text){
                "All" ->  return searchRespModel.data?.body?.all?.size
                "Songs" ->  return searchRespModel.data?.body?.song?.size
                "Album" ->  return searchRespModel.data?.body?.album?.size
                "Albums" ->  return searchRespModel.data?.body?.album?.size
                "Artist" ->  return searchRespModel.data?.body?.artist?.size
                "Podcasts" ->  return searchRespModel.data?.body?.podcast?.size
                "Playlists" ->  return searchRespModel.data?.body?.playlist?.size
                "Movies" ->  return searchRespModel.data?.body?.movie?.size
                "Music Videos" ->  return searchRespModel.data?.body?.musicvideo?.size
            }

        }
        return 0
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
        val typeface = ResourcesCompat.getFont(
            requireContext(),
            R.font.sf_pro_text_medium
        )
        tab?.let {
            setStyleForTab(it, Typeface.NORMAL, typeface)
        }
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
        val typeface = ResourcesCompat.getFont(
            requireContext(),
            R.font.sf_pro_text_bold
        )
        tab?.let {
            setStyleForTab(it, Typeface.BOLD, typeface)
        }
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_SEARCH){
            callSearchApi()
            keyword_uttered = ""
            performed_type = "keypad"
            callSearchPerFormed("keypad")

            activity?.let { view?.let { it1 -> Utils.hideSoftKeyBoard(it, it1) } }
            return true
        }
        return false
    }

    private fun callSearchPerFormed(performed_type:String,
                                    autosuggestion_clicked_position:String=""){
        val hashMap = HashMap<String, String>()
        hashMap.put(EventConstant.PERFORMED_TYPE, performed_type)
        hashMap.put(EventConstant.KEYWORD_ENTERED, et_Search.text.toString())
        hashMap.put(EventConstant.AUTOSUGGEST_COUNT, autosuggestion_count)
        hashMap.put(EventConstant.AUTOSUGGESTION_CLICKED_POSITION, autosuggestion_clicked_position)
        hashMap.put(EventConstant.KEYWORD_UTTERED, keyword_uttered)
        EventManager.getInstance().sendEvent(SearchPerformed(hashMap))
    }

    private fun callSearchApi(){
        if (isAdded && !et_Search?.text.toString().trim().isNullOrBlank()){
            try {
                callSearchApi(et_Search?.text.toString().trim())
                rv_search_suggestions_list.visibility = View.GONE
                tabs.visibility = View.GONE
                vpTransactions.visibility = View.GONE
                iv_mic.visibility = View.GONE
                iv_cancel_search.visibility = View.VISIBLE
                rlRecommendation?.visibility = View.GONE
                fl_container_recommanded.visibility = View.GONE
                topBlurView.visibility = View.GONE
            }catch (e:Exception){

            }

        }
    }

    private fun loadRecommandation(){
        searchViewModel = ViewModelProvider(
            this
        ).get(SearchViewModel::class.java)

        if (ConnectionUtil(activity).isOnline) {
            searchViewModel?.getRecommendation(requireActivity())?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                setupRecommandationAdapter(it?.data)
                            }
                        }

                        Status.LOADING ->{
                            setProgressBarVisible(false)
                        }

                        Status.ERROR ->{
                            setEmptyVisible(false)
                            setProgressBarVisible(false)
                            Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                        }
                    }
                })

        }else{
            val messageModel = MessageModel(getString(R.string.toast_message_5), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }

    private fun setupRecommandationAdapter(searchRespModel: SearchRecommendationModel){

        val ft = childFragmentManager.beginTransaction()
        ft.add(R.id.fl_container_recommanded, SearchRecommendationFragment(searchRespModel, all, true), SearchAllTabFragment.javaClass.name)
        ft.commitAllowingStateLoss()
        clMain.visibility = View.VISIBLE
        tvLabel.visibility = View.VISIBLE
        shimmerLayout.visibility = View.GONE
        shimmerLayout.stopShimmer()
    }

/*    private fun callSpeechToText(){
        val sttIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        sttIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        sttIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.general_str_5))

        try {
            startActivityForResult(sttIntent, REQUEST_CODE_STT)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            val messageModel = MessageModel(getString(R.string.general_str_6), MessageType.NEUTRAL, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }

        val hashMap = HashMap<String, String>()
        hashMap.put(EventConstant.SOURCE_EPROPERTY, ""+MainActivity.lastItemClicked+"_"+ MainActivity.headerItemName)
        EventManager.getInstance().sendEvent(VoiceTabEvent(hashMap))
    }*/

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_STT -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    setLog(TAG, "onActivityResult: "+result)
                    result?.let {
                        setLog(TAG, "onActivityResult: it[0]"+it[0])
                        val recognizedText = it[0]
                        et_Search.setText(recognizedText)
                        keyword_uttered = recognizedText
                        callSearchApi()
                        val hashMap = HashMap<String, String>()
                        hashMap.put(EventConstant.KEYWORD_UTTERED, recognizedText)
                        hashMap.put(EventConstant.NO_OF_RETRY, "0")
                        EventManager.getInstance().sendEvent(SearchPerformed(hashMap))
                        performed_type = "voice"
                        callSearchPerFormed("voice","")
                        EventManager.getInstance().sendEvent(VoiceSearchKeywordUttered(hashMap))
                    }
                }
            }
        }
    }

    override fun onPause() {
        textToSpeechEngine.stop()
        super.onPause()
    }

    override fun onDestroy() {
        textToSpeechEngine.shutdown()
        removeApiCallback()
        super.onDestroy()
        tracksViewModel.onCleanup()
    }

    override fun onResume() {
        super.onResume()
        setLocalBroadcast()
    }
    private fun setLocalBroadcast(){
        (requireActivity() as MainActivity).setLocalBroadcastEventCall(this, Constant.AUDIO_PLAYER_EVENT)
    }

    override fun onLocalBroadcastEventCallBack(context: Context?, intent: Intent) {
        if (isAdded){
            val event = intent.getIntExtra("EVENT", 0)
            if (event == Constant.AUDIO_PLAYER_RESULT_CODE) {
                CommonUtils.setPageBottomSpacing(rv_search_suggestions_list, requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
            }
        }
    }
    var userViewModel:UserViewModel?=null
    private fun callGetRecentSearh(isShow: Boolean = false) {
        recentSearchCount = 0
        userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)

        if (ConnectionUtil(requireActivity()).isOnline) {
            setLog("callGetRecentSearh", "getRecentSearchCall called")
            userViewModel?.getRecentSearchCall(requireContext())?.observe(viewLifecycleOwner,
                Observer {
                    when (it.status) {
                        com.hungama.music.data.webservice.utils.Status.SUCCESS -> {
                            setProgressBarVisible(false)
                            if (!it?.data?.data?.isNullOrEmpty()!!) {
                                //Log.v(TAG, "isViewLoading $it")
                                recentSearchListItem = ArrayList<BodyRowsItemsItem>()
                                it.data.data?.forEach {
                                    val model = BodyRowsItemsItem()
                                    model.data = BodyDataItem()
                                    model.data?.image = it?.image!!
                                    model.data?.type = "" + it.itype
                                    model.data?.id = it.contentId
                                    model.data?.title = it.title
                                    model.data?.subTitle = it.artist

                                    recentSearchListItem?.add(model)
                                }
                                setLog(
                                    "callGetRecentSearh",
                                    "recentSearchList size:${recentSearchListItem?.size}"
                                )

                                loadRecentSearchData(isShow, recentSearchListItem)
                                    recentSearchCount = recentSearchListItem.size
                            }else{
                                callDeleteUserRecentSearh()
                            }
                        }

                        Status.LOADING ->{
                            setProgressBarVisible(false)
                        }

                        Status.ERROR ->{
                            setEmptyVisible(false)
                            setProgressBarVisible(false)
                            Utils.showSnakbar(requireContext(), requireView(), true, it.message!!)
                        }
                    }
                })


        }
    }

    private fun showRecentSearch(isShow: Boolean) {
//        if(WSConstants.IS_SEARCH_RECOMMENDED_DISPLAY){
//            if(isShow){
//                callGetRecentSearh(isShow)
//            }else{
//                loadRecentSearchData(isShow, recentSearchListItem)
//            }
//
//        }else{
//            loadRecentSearchData(isShow, getRecentSearchList())
//        }
        if(isShow){
                callGetRecentSearh(isShow)
            }else{
                loadRecentSearchData(isShow, recentSearchListItem)
            }


    }

    private fun loadRecentSearchData(isShow: Boolean, rowList: ArrayList<BodyRowsItemsItem>) {
        if (isShow && rowList.size > 0) {

            clNoResultFound.visibility = View.GONE

//            if(!WSConstants.IS_SEARCH_RECOMMENDED_DISPLAY){
//                Collections.reverse(rowList)
//            }
//            Collections.reverse(rowList)
            rvRecentHistory.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)


            if (!rowList.isNullOrEmpty()){
                recentSearchAdapter = SearchAdapter(requireContext(), this, rowList, SearchAllTabFragment.all)
                rvRecentHistory.adapter = recentSearchAdapter
                clHeader?.show()
                rvRecentHistory?.show()
            }else{
                clHeader?.hide()
                rvRecentHistory?.hide()
            }
            CoroutineScope(Dispatchers.IO).launch {
                val hashMap = HashMap<String, String>()
                hashMap.put(EventConstant.RECENT_SEARCHCOUNT_EPROPERTY, ""+rowList?.size)
                EventManager.getInstance().sendEvent(SearchBarClick(hashMap))
            }


        }else{
            clHeader?.hide()
            rvRecentHistory?.hide()
        }
    }


    override fun searchItemClick(searchdata: BodyRowsItemsItem, position: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val hashMap = HashMap<String, String>()
            hashMap.put(EventConstant.RECENT_SEARCH_COUNT_EPROPERTY, ""+getRecentSearchList()?.size!!)
            hashMap.put(EventConstant.CONTENT_SELECTED_EPROPERTY, searchdata?.data?.title!!)
            hashMap.put(EventConstant.CONTENTTYPE_EPROPERTY, Utils.getContentTypeName(searchdata?.data?.type!!))
            var newContentId=searchdata?.data?.id!!
            var contentIdData=newContentId.replace("playlist-","")
            hashMap.put(EventConstant.CONTENTID_EPROPERTY, contentIdData)
            EventManager.getInstance().sendEvent(RecentSearchSelectedEvent(hashMap))
        }



        if (searchdata?.data?.type?.toInt()==21||searchdata?.data?.type?.toInt()==110||searchdata?.data?.type?.toInt()==77777||searchdata?.data?.type?.toInt()==34) {
            if (searchdata?.data?.type?.toInt()==21 || searchdata?.data?.type?.toInt()==34){
                playableItem = searchdata
                playableItemPosition = position
                getPlayableContentUrl(searchdata?.data?.id!!)
            }else if (searchdata?.data?.type?.toInt()==110){
                playableItem = searchdata
                playableItemPosition = position
                CONTENT_TYPE = Constant.CONTENT_PODCAST
                getPlayableContentUrl(searchdata?.data?.id!!)
            }


        }else if (searchdata?.data?.type?.toInt()==33){
            playableItem = searchdata
            playableItemPosition = position
            CONTENT_TYPE = Constant.CONTENT_MOOD_RADIO
            getPlayableMoodRadioList(searchdata?.data?.moodid!!,
                Constant.CONTENT_MOOD_RADIO
            )

        }else if (searchdata?.data?.type?.toInt()==35){
            playableItem = searchdata
            playableItemPosition = position
            CONTENT_TYPE = Constant.CONTENT_ON_DEMAND_RADIO
            getPlayableMoodRadioList(searchdata?.data?.id!!,
                Constant.CONTENT_ON_DEMAND_RADIO
            )
        }else if (searchdata?.data?.type?.toInt()==36){
            playableItem = searchdata
            playableItemPosition = position
            CONTENT_TYPE = Constant.CONTENT_ARTIST_RADIO
            getPlayableMoodRadioList(searchdata?.data?.id!!,
                Constant.CONTENT_ARTIST_RADIO
            )
        }else {
            onItemDetailPageRedirection(searchdata, position)
        }
    }

    /**
     * get Playable url for song : 21
     *
     * @param id
     */
    fun getPlayableContentUrl(id:String){
        if (ConnectionUtil(context).isOnline) {
            playableContentViewModel?.getPlayableContentList(requireContext(), id)?.observe(this,
                Observer {
                    when(it.status){
                        com.hungama.music.data.webservice.utils.Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                //setLog(TAG, "isViewLoading $it")

                                if(!TextUtils.isEmpty(it?.data?.data?.head?.headData?.misc?.url)){
                                    setPlayableContentListData(it?.data)
                                }
                            }
                        }

                        com.hungama.music.data.webservice.utils.Status.LOADING ->{
                            setProgressBarVisible(true)
                        }

                        com.hungama.music.data.webservice.utils.Status.ERROR ->{
                            setEmptyVisible(false)
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

    /**
     * get Playable url for song : 33, 35
     *
     * @param id
     */
    private fun getPlayableMoodRadioList(id:String, type:Int){
        if (ConnectionUtil(context).isOnline) {
            if (type == Constant.CONTENT_MOOD_RADIO){
                playableContentViewModel?.getMoodRadioList(requireContext(), id)?.observe(this,
                    Observer {
                        when(it.status){
                            com.hungama.music.data.webservice.utils.Status.SUCCESS->{
                                setProgressBarVisible(false)
                                if (it?.data != null) {
                                    moodRadioListRespModel = it?.data
                                    if (it?.data?.size!! > 0) {
                                        getPlayableContentUrl(it?.data?.get(0)?.data?.id!!)
                                    }

                                }
                            }

                            com.hungama.music.data.webservice.utils.Status.LOADING ->{
                                setProgressBarVisible(true)
                            }

                            com.hungama.music.data.webservice.utils.Status.ERROR ->{
                                setProgressBarVisible(false)
                                Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                            }
                        }
                    })
            }else if (type == Constant.CONTENT_ARTIST_RADIO){
                playableContentViewModel?.getArtistRadioList(requireContext(), id)?.observe(this,
                    Observer {
                        when(it.status){
                            com.hungama.music.data.webservice.utils.Status.SUCCESS->{
                                setProgressBarVisible(false)
                                if (it?.data != null) {
                                    moodRadioListRespModel=it?.data
                                    if(it?.data?.size!!>0){
                                        getPlayableContentUrl(it?.data?.get(0)?.data?.id!!)
                                    }

                                }
                            }

                            com.hungama.music.data.webservice.utils.Status.LOADING ->{
                                setProgressBarVisible(true)
                            }

                            com.hungama.music.data.webservice.utils.Status.ERROR ->{
                                setProgressBarVisible(false)
                                Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                            }
                        }
                    })
            }else {
                playableContentViewModel?.getOnDemandRadioList(requireContext(), id)?.observe(this,
                    Observer {
                        when(it.status){
                            com.hungama.music.data.webservice.utils.Status.SUCCESS->{
                                setProgressBarVisible(false)
                                if (it?.data != null) {
                                    moodRadioListRespModel=it?.data
                                    if(it?.data?.size!!>0){
                                        getPlayableContentUrl(it?.data?.get(0)?.data?.id!!)
                                    }

                                }
                            }

                            com.hungama.music.data.webservice.utils.Status.LOADING ->{
                                setProgressBarVisible(true)
                            }

                            com.hungama.music.data.webservice.utils.Status.ERROR ->{
                                setProgressBarVisible(false)
                                Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                            }
                        }
                    })
            }

        } else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }
    var moodRadioListRespModel: MoodRadioListRespModel?=null
    private fun setUpPlayableContentListViewModel() {
        playableContentViewModel = ViewModelProvider(
            this
        ).get(PlayableContentViewModel::class.java)

    }



    fun setPlayableContentListData(playableContentModel: PlayableContentModel) {

        if (playableContentModel != null ) {
            setLog("PlayableItem", playableContentModel?.data?.head?.headData?.id.toString())
            songDataList = arrayListOf()
            isLastDurationPlay = false
            setLog(TAG, "onClick btn_next_play_mini size 1: ${songDataList?.size}")
            if(playableItem?.data?.type?.toInt()==33 || playableItem?.data?.type?.toInt()==35 || playableItem?.data?.type?.toInt()==36){
                for (i in moodRadioListRespModel?.indices!!){
                    if (playableContentModel?.data?.head?.headData?.id == moodRadioListRespModel?.get(i)?.data?.id){
                        setLog(TAG, "moodRadioListRespModel 1 playableItemPosition: ${playableItemPosition}")
                        setRadioDataList(playableContentModel, moodRadioListRespModel?.get(i), playableItemPosition,playableItem)
                    }

                    setLog(TAG, "moodRadioListRespModel 2 playableItemPosition: ${playableItemPosition}")
                    if (i > playableItemPosition){
                        setLog(TAG, "moodRadioListRespModel 3 playableItemPosition: ${playableItemPosition}")
                        setRadioDataList(null, moodRadioListRespModel?.get(i), playableItemPosition,playableItem)
                    }

                }
                setLog(TAG, "onClick btn_next_play_mini size 2: ${songDataList?.size}")
                BaseActivity.setTrackListData(songDataList)

                if(HungamaMusicApp?.getInstance()?.getContinueWhereLeftData()!=null&& HungamaMusicApp?.getInstance()?.getContinueWhereLeftData()?.size!!>0){

                    HungamaMusicApp?.getInstance()?.getContinueWhereLeftData()?.forEach {
                        if(it?.data?.id==playableContentModel?.data?.head?.headData?.id){
                            if(it?.data?.durationPlay!=null&&it?.data?.durationPlay?.toLong()!!>0){
                                //tracksViewModel.prepareTrackPlayback(playableItemPosition, TimeUnit.SECONDS.toMillis(it?.data?.durationPlay!!))
                                tracksViewModel.prepareTrackPlayback(0, TimeUnit.SECONDS.toMillis(it?.data?.durationPlay?.toLong()!!))

                            }else{
                                //tracksViewModel.prepareTrackPlayback(playableItemPosition)
                                tracksViewModel.prepareTrackPlayback(0)

                            }
                            isLastDurationPlay=true
                            return@forEach
                        }
                    }

                    if(!isLastDurationPlay){
                        tracksViewModel.prepareTrackPlayback(0)

                    }
                }else{
                    tracksViewModel.prepareTrackPlayback(0)

                }
            }else{

                if (playableContentModel?.data?.head?.headData?.id == playableItem?.data?.id){
                    setPlayableDataList(playableContentModel, playableItem, playableItemPosition)
                }

                setLog(TAG, "onClick btn_next_play_mini size 2: ${songDataList?.size}")
                BaseActivity.setTrackListData(songDataList)
                if(HungamaMusicApp?.getInstance()?.getContinueWhereLeftData()!=null&& HungamaMusicApp?.getInstance()?.getContinueWhereLeftData()?.size!!>0){

                    HungamaMusicApp?.getInstance()?.getContinueWhereLeftData()?.forEach {
                        if(it?.data?.id.equals(playableContentModel?.data?.head?.headData?.id,true)){
                            if(it?.data?.durationPlay!=null&&it?.data?.durationPlay?.toLong()!!>0){
                                setLog(TAG, "onClick btn_next_play_mini size 3: ${songDataList?.size}")
                                //tracksViewModel.prepareTrackPlayback(playableItemPosition, TimeUnit.SECONDS.toMillis(it?.data?.durationPlay!!))
                                tracksViewModel.prepareTrackPlayback(0, TimeUnit.SECONDS.toMillis(it?.data?.durationPlay?.toLong()!!))

                            }else{
                                setLog(TAG, "onClick btn_next_play_mini size 4: ${songDataList?.size}")
//                                tracksViewModel.prepareTrackPlayback(playableItemPosition)
                                tracksViewModel.prepareTrackPlayback(0)

                            }

                            isLastDurationPlay=true
                            return@forEach
                        }
                    }

                    if(!isLastDurationPlay){
                        setLog(TAG, "onClick btn_next_play_mini size 5: ${songDataList?.size}")
//                        tracksViewModel.prepareTrackPlayback(playableItemPosition)
                        tracksViewModel.prepareTrackPlayback(0)

                    }
                }else{
                    setLog(TAG, "onClick btn_next_play_mini size 6: ${songDataList?.size}")
//                    tracksViewModel.prepareTrackPlayback(playableItemPosition)
                    tracksViewModel.prepareTrackPlayback(0)

                }


            }


        }
    }

    var songDataList:ArrayList<Track> = arrayListOf()
    fun setPlayableDataList(
        playableContentModel: PlayableContentModel?,
        playableItem: BodyRowsItemsItem,
        position:Int
    ) {
        val track: Track = Track()

        if (!TextUtils.isEmpty(playableItem?.data?.id)){
            track.id = playableItem?.data?.id!!.toLong()
        }else{
            track.id = 0
        }
        if (!TextUtils.isEmpty(playableItem?.data?.title)){
            track.title = playableItem?.data?.title
        }else{
            track.title = ""
        }

        if (!TextUtils.isEmpty(playableItem?.data?.subTitle)){
            track.subTitle = playableItem?.data?.subTitle
        }else{
            track.subTitle = ""
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.url)){
            track.url = playableContentModel?.data?.head?.headData?.misc?.url
        }else{
            track.url = ""
        }

        if (!playableContentModel?.data?.head?.headData?.misc?.movierights.isNullOrEmpty()){
            track.movierights = playableContentModel?.data?.head?.headData?.misc?.movierights.toString()
        }else{
            track.movierights = ""
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.downloadLink?.drm?.token)){
            track.drmlicence = playableContentModel?.data?.head?.headData?.misc?.downloadLink?.drm?.token
        }else{
            track.drmlicence = ""
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.sl?.lyric?.link)) {
            track.songLyricsUrl = playableContentModel?.data?.head?.headData?.misc?.sl?.lyric?.link
        } else {
            track.songLyricsUrl = ""
        }

        if (!TextUtils.isEmpty(playableItem?.data?.type.toString())){
            track.playerType = playableItem?.data?.type.toString()
        }else{
            track.playerType = Constant.MUSIC_PLAYER
        }
       if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.pName.toString())){
            track.pName = playableContentModel?.data?.head?.headData?.misc?.pName.toString()
        }else{
            track.heading = ""
        }
        if (!TextUtils.isEmpty(playableItem?.data?.playble_image)){
            track.image = playableItem?.data?.playble_image
        }else if (!TextUtils.isEmpty(playableItem?.data?.image)){
            track.image = playableItem?.data?.image
        }else{
            track.image = ""
        }

        if (playableItem.data?.misc?.explicit != null){
            track.explicit = playableItem.data?.misc?.explicit!!
        }
        if (playableItem.data?.misc?.restricted_download != null){
            track.restrictedDownload = playableItem.data?.misc?.restricted_download!!
        }
        if (playableItem.data?.misc?.attributeCensorRating != null){
            track.attributeCensorRating =
                playableItem.data?.misc?.attributeCensorRating.toString()
        }

        if (!playableContentModel?.data?.head?.headData?.misc?.pid?.isNullOrEmpty()!!){
            track.parentId = ""+playableContentModel?.data?.head?.headData?.misc?.pid?.get(0)!!
        }

        if (playableContentModel != null){
            track.urlKey = playableContentModel.data.head.headData.misc.urlKey
        }

        songDataList.add(track)
    }

    fun setRadioDataList(
        playableContentModel: PlayableContentModel?,
        playableItem: MoodRadioListRespModel.MoodRadioListRespModelItem?,
        position: Int,
        playableItem1: BodyRowsItemsItem?
    ) {
        val track: Track = Track()


        if (!TextUtils.isEmpty(playableItem?.data?.id)){
            track.id = playableItem?.data?.id!!.toLong()
        }else{
            track.id = 0
        }
        if (!TextUtils.isEmpty(playableItem?.data?.title)){
            track.title = playableItem?.data?.title
        }else{
            track.title = ""
        }

        if (!TextUtils.isEmpty(playableItem?.data?.subtitle)){
            track.subTitle = playableItem?.data?.subtitle
        }else{
            track.subTitle = ""
        }

        if (playableItem?.data?.misc!=null&&playableItem?.data?.misc?.artist!=null){
            track.artistName = TextUtils.join(",", playableItem?.data?.misc?.artist!!)
        }else{
            track.artistName = ""
        }

        if (playableContentModel!=null&&!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.url)){
            track.url = playableContentModel?.data?.head?.headData?.misc?.url
        }else{
            track.url = ""
        }

        if (playableContentModel!=null&&!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.downloadLink?.drm?.token)){
            track.drmlicence = playableContentModel?.data?.head?.headData?.misc?.downloadLink?.drm?.token
        }else{
            track.drmlicence = ""
        }

        if (playableContentModel!=null&&!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.sl?.lyric?.link)) {
            track.songLyricsUrl = playableContentModel?.data?.head?.headData?.misc?.sl?.lyric?.link
        } else {
            track.songLyricsUrl = ""
        }

        if (!playableContentModel?.data?.head?.headData?.misc?.movierights.isNullOrEmpty()){
            track.movierights = playableContentModel?.data?.head?.headData?.misc?.movierights.toString()
        }else{
            track.movierights = ""
        }

        if (!TextUtils.isEmpty(playableItem1?.data?.type.toString())){
            track.playerType = playableItem1?.data?.type.toString()
        }else{
            track.playerType = Constant.PLAYER_RADIO
        }
        /*if (!TextUtils.isEmpty(playableItem1.heading)){
            track.heading = playableItem1.heading
        }else{
            track.heading = ""
        }*/
        if (!TextUtils.isEmpty(playableItem?.data?.playble_image)){
            track.image = playableItem?.data?.playble_image
        }else if (!TextUtils.isEmpty(playableItem?.data?.image)){
            track.image = playableItem?.data?.image
        }else{
            track.image = ""
        }

        if (playableItem1?.data?.misc?.explicit != null){
            track.explicit = playableItem1.data?.misc?.explicit!!
        }
        if (playableItem1?.data?.misc?.restricted_download != null){
            track.restrictedDownload = playableItem1.data?.misc?.restricted_download!!
        }
        if (playableItem1?.data?.misc?.attributeCensorRating != null){
            track.attributeCensorRating =
                playableItem1.data?.misc?.attributeCensorRating.toString()
        }

        if (playableContentModel!=null&&!playableContentModel?.data?.head?.headData?.misc?.pid?.isNullOrEmpty()!!){
            track.parentId = ""+playableContentModel?.data?.head?.headData?.misc?.pid?.get(0)!!
        }

        if (playableContentModel != null){
            track.urlKey = playableContentModel.data.head.headData.misc.urlKey
        }
        songDataList.add(track)
    }

    override fun startTrackPlayback(
        selectedTrackPosition: Int,
        tracksList: MutableList<Track>,
        trackPlayStartPosition: Long
    ) {
        val intent = Intent(getViewActivity(), AudioPlayerService::class.java)
        intent.action = AudioPlayerService.PlaybackControls.PLAY.name
        intent.putExtra(Constant.SELECTED_TRACK_POSITION, selectedTrackPosition)
        if(trackPlayStartPosition>0){
            intent.putExtra(Constant.SELECTED_TRACK_PLAY_START_POSITION, trackPlayStartPosition)
        }
        intent.putExtra(Constant.PLAY_CONTEXT_TYPE, Constant.PLAY_CONTEXT.LIBRARY_TRACKS)
        Util.startForegroundService(getViewActivity(), intent)
        (activity as MainActivity).reBindService()
    }

    override fun getViewActivity(): AppCompatActivity {
        return activity as AppCompatActivity
    }

    override fun getApplicationContext(): Context {
        return (activity as AppCompatActivity).applicationContext
    }

    fun onItemDetailPageRedirection(searchdata: BodyRowsItemsItem, position: Int) {

        val type=searchdata?.data?.type.toString()
        val image=searchdata?.data?.image
        val id=searchdata?.data?.id
        val title=searchdata?.data?.title
        val varientType=searchdata?.data?.variant

        if (type.equals("0", true)) {
            val bundle = Bundle()
            bundle.putString("image", image)
            bundle.putString("id", id)
            bundle.putString("playerType", type)
            var varient = 1
            if (!TextUtils.isEmpty(varientType)) {
                if (varientType.equals("v2", true)) {
                    bundle.putBoolean("varient", true)
                }else{
                    bundle.putBoolean("varient", false)
                }
            }else{
                bundle.putBoolean("varient", false)
            }
            val artistDetailsFragment = ArtistDetailsFragment()
            artistDetailsFragment.arguments = bundle
            addFragment(R.id.fl_container, this, artistDetailsFragment, false)

        } else if (type.equals("1", true)) {
            val bundle = Bundle()
            bundle.putString("image", image)
            bundle.putString("id", id)
            bundle.putString("playerType", type)

            val albumDetailFragment = AlbumDetailFragment()
            albumDetailFragment.arguments = bundle

            addFragment(R.id.fl_container, this, albumDetailFragment, false)

        } else if (type.equals("15", true)
            || type.equals("44444", true)
            || type.equals("66666", true)) {
            val bundle = Bundle()
            bundle.putString("image", image)
            bundle.putString("id", id)
            bundle.putString("playerType", type)

            val collectionDetailsFragment = CollectionDetailsFragment()
            collectionDetailsFragment.arguments = bundle

            addFragment(R.id.fl_container, this, collectionDetailsFragment, false)
        } else if (type.equals("19", true)) {
            val bundle = Bundle()
            bundle.putString("image", image)
            if (searchdata?.data?.images != null && searchdata?.data?.images?.size!! > 0 && searchdata?.itype == 42
            ) {
                bundle.putStringArrayList(
                    "imageArray",
                    searchdata?.data?.images as java.util.ArrayList<String>?
                )
            }

            if (searchdata?.data?.variant_images != null && searchdata?.data?.variant_images?.size!! > 0
            ) {
                bundle.putStringArrayList(
                    "variant_images",
                    searchdata?.data?.variant_images as java.util.ArrayList<String>?
                )
            }


            bundle.putString("id", id)
            bundle.putString("playerType", type)
            var varient = 1
            if (!TextUtils.isEmpty(varientType)) {
                if (varientType.equals("v2", true)) {
                    varient = 2
                }
            }
            val chartDetailFragment = ChartDetailFragment.newInstance(varient)
            chartDetailFragment.arguments = bundle

            addFragment(R.id.fl_container, this, chartDetailFragment, false)

        } else if (type.equals("20", true)) {
            val bundle = Bundle()
            bundle.putString("image", image)
            bundle.putString("id", id)
            if(searchdata?.data?.misc?.artistid!=null){
                bundle.putString("artistid", searchdata?.data?.misc?.artistid)
            }

            bundle.putString("playerType", type)
            bundle.putBoolean("varient", true)

            var varient = 1
            if (!TextUtils.isEmpty(varientType)) {
                if (varientType.equals("v2", true)) {
                    varient = 2
                }
            }
            val eventDetailFragment = EventDetailFragment()
            eventDetailFragment.arguments = bundle

            addFragment(R.id.fl_container, this, eventDetailFragment, false)

        }else if (type.equals("25", true)) {
            val bundle = Bundle()
            bundle.putString("heading", searchdata?.data?.title)
            val categoryDetailFragment = CategoryDetailFragment(
                searchdata?.data?.id!!.toString(),
                10
            )
            categoryDetailFragment.arguments = bundle
            addFragment(R.id.fl_container, this, categoryDetailFragment, false)

            CoroutineScope(Dispatchers.IO).launch {
                val hashMap = java.util.HashMap<String, String>()
                hashMap.put(EventConstant.TYPE_EPROPERTY,""+searchdata?.data?.type)
                hashMap.put(EventConstant.CONTENT_TYPE_EPROPERTY,""+Utils.getContentTypeName(searchdata?.data?.type.toString()))
                hashMap.put(EventConstant.CATEGORYNAME_EPROPERTY,""+searchdata?.data?.title)
                //hashMap.put(EventConstant.SOURCEPAGE_EPROPERTY,""+MainActivity.lastItemClicked+"_"+MainActivity.headerItemName+"_"+parent?.heading)

                EventManager.getInstance().sendEvent(CategoryClickedEvent(hashMap))
            }

        }
        else if (type.equals(
                "93",
                true
            ) || type.equals(
                "4",
                true
            )  || type.equals(
                "65",
                true
            ) || type.equals("66", true)
        ) {
            val bundle = Bundle()
            bundle.putString("image", image)
            bundle.putString("id", id)
            bundle.putString("playerType", type)
            bundle.putBoolean("varient", true)

            var varient = 1
            if (!TextUtils.isEmpty(varientType)) {
                if (varientType.equals("v2", true)) {
                    varient = 2
                    bundle.putString("variant_image", searchdata?.data!!.variant_images?.get(0))
                }
            }
            val movieDetailsFragment = MovieV1Fragment(varient)
            movieDetailsFragment.arguments = bundle

            addFragment(R.id.fl_container, this, movieDetailsFragment, false)

        } else if (type.equals(
                "96",
                true
            ) || type.equals(
                "97",
                true
            ) || type.equals(
                "98",
                true
            ) || type.equals(
                "102",
                true
            ) || type.equals("107", true)
        ) {
            val bundle = Bundle()
            bundle.putString("image", image)
            bundle.putString("id", id)
            bundle.putParcelable("child_item", searchdata)
            if (position % 2 == 0)
                bundle.putBoolean("varient", true)
            else
                bundle.putBoolean("varient", false)
            var varient = 1
            if (!TextUtils.isEmpty(varientType)) {
                if (varientType.equals("v2", true)) {
                    varient = 2
                }
            }
            bundle.putString("playerType", type)
            val tvShowDetailsFragment = TvShowDetailsFragment(varient)
            tvShowDetailsFragment.arguments = bundle
            addFragment(R.id.fl_container, this, tvShowDetailsFragment, false)

        } else if (type.equals("109", true)) {

            val bundle = Bundle()
            bundle.putString("image", image)
            bundle.putString("id", id)
            bundle.putString("playerType", type)
            //bundle.putParcelable("details", parent)
            //bundle.putInt("childPosition", childPosition)
            if (position % 2 == 0)
                bundle.putBoolean("varient", true)
            else
                bundle.putBoolean("varient", false)


            val podcastDetailsFragment = PodcastDetailsFragment()
            podcastDetailsFragment.arguments = bundle

            addFragment(R.id.fl_container, this, podcastDetailsFragment, false)

        } else if (type.equals("55555", true)) {

            val bundle = Bundle()
            bundle.putString("image", image)
            bundle.putString("id", id)
            bundle.putString("playerType", type)
            if (searchdata?.data?.images != null && searchdata.data?.images?.size!! > 0
            ) {
                bundle.putStringArrayList(
                    "imageArray",
                    searchdata?.data?.images as java.util.ArrayList<String>?
                )
            }

            if (searchdata?.data?.variant_images != null && searchdata?.data?.variant_images?.size!! > 0
            ) {
                bundle.putStringArrayList(
                    "variant_images",
                    searchdata?.data?.variant_images as java.util.ArrayList<String>?
                )
            }

            var varient = 1
            if (!TextUtils.isEmpty(varientType)) {
                if (varientType.equals("v2", true)) {
                    varient = 2
                }
            }
            val playlistDetailFragment = PlaylistDetailFragmentDynamic.newInstance(varient)
            playlistDetailFragment.arguments = bundle

            addFragment(R.id.fl_container, this, playlistDetailFragment, false)

        } else if (type.equals(
                "22",
                true
            ) || type.equals(
                "53",
                true
            ) || type.equals("88888", true)
        ) {

            if (activity != null && activity is MainActivity){
                (activity as MainActivity).setPauseMusicPlayerOnVideoPlay()
            }
            val bundle = Bundle()
            bundle.putString("id", id)
            val videoDetailsFragment = MusicVideoDetailsFragment()
            videoDetailsFragment.arguments = bundle
            addFragment(R.id.fl_container, this, videoDetailsFragment, false)
        } else if (type.equals("99999", true)) {
            var varient = 1
            val playlistDetailFragment = MyPlaylistDetailFragment(varient,object :MyPlaylistDetailFragment.onBackPreesHendel{
                override fun backPressItem(status: Boolean) {
                    setUpPlayableContentListViewModel()
                }

            })
            val bundle = Bundle()
            bundle.putString("image", image)
            bundle.putString("id", id)
            bundle.putString("playerType", type)
            playlistDetailFragment.arguments = bundle
            addFragment(
                R.id.fl_container,
                this,
                playlistDetailFragment,
                false
            )

        } else {
            Utils.showSnakbar(requireContext(),requireView(), false, "coming soon")
        }
    }

    override fun onClearRecentSearch(isClear: Boolean) {
        if (isClear){
            callDeleteUserRecentSearh()
//            SharedPrefHelper.getInstance().setRecentSearchList("")
//            showRecentSearch(false)
        }
    }

    override fun SearchView(data: ArrayList<String>?) {
        data?.let {
            setLog(TAG, "onActivityResult: it[0]" + it[0])
            val recognizedText = it[0]
            isAutoSuggestClick=true
            et_Search.setText(recognizedText)
            callSearchApi()
            keyword_uttered = recognizedText
            performed_type = "voice"
            callSearchPerFormed("voice", "")

        }


    }

    private fun openSTTPopup(){
        val searchBottomSheet  = SearchBottomSheet()
        searchBottomSheet.setItem(this)
        searchBottomSheet.show(requireFragmentManager(),"open")
    }
    override fun onShowPermissionRationale(permission: String, requestCode: Int) {

        /*Snackbar.make(rootLayout, R.string.permissions_rationale, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.discover_str_13) {
                PermissionUtils().requestPermission(this, permission, requestCode)
            }
            .show()*/
        val alertDialog = AlertDialog.Builder(requireContext())

        alertDialog.apply {
            //setIcon(R.drawable.hungama_text_icon)
            //setTitle("Hello")
            setMessage(R.string.record_audio_permission)
            setPositiveButton(getString(R.string.library_all_str_6)) { _, _ ->
                callPermission(permission, requestCode)
            }
            setNegativeButton(getString(R.string.download_str_3)) { _, _ ->

            }
            /*setNeutralButton("Neutral") { _, _ ->
                toast("clicked neutral button")
            }*/
        }.create().show()
        //Toast.makeText(requireContext(),R.string.permissions_rationale, Toast.LENGTH_LONG).show()
    }

    private fun callPermission(permission: String, requestCode: Int) {
        PermissionUtils().requestPermission(this, permission, requestCode)
    }

    override fun onPermissionGranted(permission: String) {
        openSTTPopup()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            RECORD_AUDIO_REQ_CODE -> {

                bAlreadyAskedForRecordPermission = false

                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openSTTPopup()
                } else {
                    onShowPermissionRationale(permissions[0], RECORD_AUDIO_REQ_CODE)
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(
            KEY_RECORD_PERMISSION_ALREADY_ASKED,
            bAlreadyAskedForRecordPermission
        )
        super.onSaveInstanceState(outState)
    }

    private fun callAutoSuggestionAPI(searchText: String){
        if(searchViewModel==null){
            searchViewModel = ViewModelProvider(
                this
            ).get(SearchViewModel::class.java)
        }

        if (ConnectionUtil(activity).isOnline) {
            searchViewModel?.getSuggestionData(requireActivity(),searchText)?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                setLog(TAG, "getSuggestionData: response:$it?.data?.data")
                                setAutoSuggestData(searchText,it?.data?.data)
                                autosuggestion_count = it.data.data!!.size.toString()
                            }

                            if(it?.data!=null && it.data.data?.size!!>0) {
                                val dataMap = HashMap<String, String>()
                                dataMap.put(EventConstant.KEYWORD_ENTERED, searchText)
                                dataMap.put(EventConstant.AUTOSUGGEST_COUNT, "" + it.data.data!!.size)
                                setLog("PageType", "Type${dataMap}")
                                EventManager.getInstance().sendEvent(SearchKeywordEnter(dataMap))
                                EventManager.getInstance().sendEvent(AutosuggestDisplayed(dataMap))
                            }
                        }

                        Status.LOADING ->{
                            setProgressBarVisible(false)
                        }

                        Status.ERROR ->{
                            setEmptyVisible(false)
                            setProgressBarVisible(false)
                            Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                        }
                    }
                })

        }else{
            val messageModel = MessageModel(getString(R.string.toast_message_5), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel)
        }
    }


    private fun setAutoSuggestData(searchText: String, data: List<SuggestionRespModel.Data?>?) {

        if(data!=null&&data?.size!!>0){
            rv_search_suggestions_list.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            suggestionList = java.util.ArrayList<SuggestionTextModel>()
            data?.forEach {
                if(!it?.name?.isNullOrEmpty()!!){
                    suggestionList.add(SuggestionTextModel("", it?.name!!))
                }

            }

            setLog(
                TAG,
                "getSuggestionData: suggestionList:${suggestionList?.size}"
            )

            suggestionAdapter = SuggestionAdapter(suggestionList, this)
            rv_search_suggestions_list.adapter = suggestionAdapter

            rv_search_suggestions_list?.visibility=View.VISIBLE
            clHeader?.hide()
            rvRecentHistory?.hide()
        }else{
            rv_search_suggestions_list?.visibility=View.GONE
        }

    }

    private fun callDeleteUserRecentSearh() {
        if(userViewModel==null){
            userViewModel = ViewModelProvider(
                this
            ).get(UserViewModel::class.java)
        }


        if (ConnectionUtil(requireActivity()).isOnline) {
            setLog("callDeleteUserRecentSearh", "callDeleteUserRecentSearh called")
            userViewModel?.deleteRecentSearchCall(requireContext())?.observe(viewLifecycleOwner,
                Observer {
                    when (it.status) {
                        com.hungama.music.data.webservice.utils.Status.SUCCESS -> {
                            setProgressBarVisible(false)
                            showRecentSearch(false)
                        }

                        com.hungama.music.data.webservice.utils.Status.LOADING ->{
                            setProgressBarVisible(true)
                        }

                        com.hungama.music.data.webservice.utils.Status.ERROR ->{
                            setEmptyVisible(false)
                            setProgressBarVisible(false)
                            Utils.showSnakbar(requireContext(), requireView(), true, it.message!!)
                        }
                    }
                })


        }
    }

    override fun contentClicked(searchdata: BodyRowsItemsItem, itemPosition:Int) {
        CoroutineScope(Dispatchers.IO).launch {
            MainActivity.lastItemClicked = "Search"
            MainActivity.headerItemName = tabs.getTabAt(tabs.selectedTabPosition)?.text.toString()
            val hashMap = java.util.HashMap<String, String>()
            hashMap[EventConstant.KEYWORD_ENTERED_EPROPERTY] = et_Search.text.toString()
            hashMap[EventConstant.KEYWORD_UTTERED_EPROPERTY] = keyword_uttered
            hashMap[EventConstant.CLICK_POSITION_EPROPERTY] = "" + itemPosition
            hashMap[EventConstant.RESULT_COUNT_EPROPERTY] = if (resultCount(tabs.getTabAt(tabs.selectedTabPosition)?.text.toString())!! >0) resultCount(tabs.getTabAt(tabs.selectedTabPosition)?.text.toString()).toString() else ""
            hashMap[EventConstant.PAGENAME_EPROPERTY] = "search_" + tabs.getTabAt(tabs.selectedTabPosition)?.text.toString().lowercase()

            hashMap[EventConstant.CONTENTTYPE_EPROPERTY] = Utils.getContentTypeName(searchdata.data?.type!!)
            hashMap[EventConstant.CONTENTNAME_EPROPERTY] = searchdata.data?.title.toString()
            hashMap[EventConstant.CONTENTID_EPROPERTY] = searchdata.data?.id.toString()
            hashMap[EventConstant.LANGUAGE_EPROPERTY] = Utils.arrayToString(searchdata.data?.misc?.languages)
            hashMap[EventConstant.GENRE_EPROPERTY] = searchdata.data?.genre.toString()
//            hashMap[EventConstant.LABEL_EPROPERTY] = searchdata.data?.misc.lab
//            hashMap[EventConstant.LABEL_ID_EPROPERTY] = searchdata.data?.misc.lab
            hashMap[EventConstant.ACTOR_EPROPERTY] = searchdata.data?.misc?.actorf.toString()
            hashMap[EventConstant.LYRICIST_EPROPERTY] = searchdata.data?.misc?.lyricist.toString()
            hashMap[EventConstant.MOOD_EPROPERTY] = searchdata.data?.misc?.mood.toString()
            hashMap[EventConstant.MUSICDIRECTOR_EPROPERTY] = searchdata.data?.misc?.musicdirectorf.toString()
//            hashMap[EventConstant.ORIGINAL_ALBUM_NAME_EPROPERTY] = searchdata.ori
//            hashMap[EventConstant.PODCAST_ALBUM_NAME_EPROPERTY] = searchdata.data?.misc.pod
            hashMap[EventConstant.SINGER_EPROPERTY] = searchdata.data?.misc?.singerf.toString()
            hashMap[EventConstant.SONGNAME_EPROPERTY] = searchdata.data?.title.toString()
            hashMap[EventConstant.YEAROFRELEASE_EPROPERTY] = searchdata.data?.misc?.releasedate.toString()
            setLog("akhgoahohgpa ", hashMap.toString())
            EventManager.getInstance().sendEvent(SearchResultClicked(hashMap))
        }
    }
}