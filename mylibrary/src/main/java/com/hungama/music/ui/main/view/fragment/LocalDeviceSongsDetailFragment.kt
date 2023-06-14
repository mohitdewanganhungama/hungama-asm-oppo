package com.hungama.music.ui.main.view.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appsflyer.AppsFlyerLib
import androidx.media3.common.util.Util
import com.hungama.music.HungamaMusicApp
import com.hungama.music.R
import com.hungama.music.player.audioplayer.TracksContract
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.services.AudioPlayerService
import com.hungama.music.player.audioplayer.viewmodel.TracksViewModel
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.home.eventreporter.UserAttributeEvent
import com.hungama.music.player.audioplayer.Injection
import com.hungama.music.ui.main.adapter.LocalDeviceSongsDetailAdapter
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.setLog
import kotlinx.android.synthetic.main.common_header_action_bar.view.*
import kotlinx.android.synthetic.main.local_device_songs_fragment_layout.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.HashMap

private const val KEY_STORAGE_PERMISSION_ALREADY_ASKED = "storagePermissionAlreadyAsked"
const val READ_EXTERNAL_STORAGE_REQ_CODE: Int = 101
class LocalDeviceSongsDetailFragment : BaseFragment(), TracksContract.View, PermissionCallbacks {

    private lateinit var tracksViewModel: TracksContract.Presenter
    var localDeviceSongsList: ArrayList<Track> = ArrayList()
    var localDeviceSongsAdpter: LocalDeviceSongsDetailAdapter? = null
    private val trackRepository = Injection.provideTrackRepository()
    private lateinit var permissionUtils: PermissionUtils
    private var bAlreadyAskedForStoragePermission: Boolean = false

    companion object {
        var localDeviceSongsRespModel: ArrayList<Track>? = null
        var playableItemPosition = 0
    }

    override fun initializeComponent(view: View) {

        ivBack!!.setOnClickListener{
            backPress()
        }

        rlHeader.tvActionBarHeading.text = getString(R.string.library_str_4)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tracksViewModel = TracksViewModel(Injection.provideTrackRepository(), this)
        permissionUtils = PermissionUtils()

        if (savedInstanceState != null) {
            bAlreadyAskedForStoragePermission =
                savedInstanceState.getBoolean(KEY_STORAGE_PERMISSION_ALREADY_ASKED, false)
        }

        if (!bAlreadyAskedForStoragePermission) {

            bAlreadyAskedForStoragePermission = permissionUtils.requestPermissionsWithRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                READ_EXTERNAL_STORAGE_REQ_CODE,
                this
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragmen
        return inflater.inflate(R.layout.local_device_songs_fragment_layout, container, false)
    }

    @SuppressLint("LogNotTimber")
    private fun setupDeviceSongList(){
        rvLocalSongList.visibility = View.VISIBLE
        baseMainScope.launch {
            try {

                localDeviceSongsList = ArrayList()
                localDeviceSongsList.addAll(trackRepository.getAllLocalDeviceTracks(requireContext()))
                withContext(Dispatchers.Main) {
                    if (!localDeviceSongsList.isNullOrEmpty()){
                        setDownloadedSongsDetailsListData()
                    }
                }

            } catch (e: RuntimeException) {
                e.printStackTrace()
                setLog("LocalDeviceSongs", "Exception: ${e.message}")
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    private fun setDownloadedSongsDetailsListData() {
        localDeviceSongsRespModel = localDeviceSongsList
        if (!localDeviceSongsList.isNullOrEmpty()) {
            rvLocalSongList.apply {
                layoutManager =
                    GridLayoutManager(context, 1, GridLayoutManager.VERTICAL, false)

                setRecycledViewPool(RecyclerView.RecycledViewPool())
                setHasFixedSize(true)

            }
            setDownloadedSongsSongAdapter()

        }
    }

    private fun setDownloadedSongsSongAdapter() {
        localDeviceSongsAdpter = LocalDeviceSongsDetailAdapter(
            requireContext(), localDeviceSongsList,
            object : LocalDeviceSongsDetailAdapter.OnChildItemClick {
                override fun onUserClick(childPosition: Int) {
                    playableItemPosition = childPosition
                    if (isOnClick()) {
                        setPlayableContentListData()
                    }
                }
            })
        rvLocalSongList.adapter = localDeviceSongsAdpter

        val userDataMap= java.util.HashMap<String, String>()
        userDataMap.put(EventConstant.NUMBER_OF_LOCAL_SONGS, ""+ localDeviceSongsList?.size)
        EventManager.getInstance().sendUserAttribute(UserAttributeEvent(userDataMap))
    }

    override fun onDestroy() {
        super.onDestroy()
        tracksViewModel.onCleanup()
    }

    override fun startTrackPlayback(
        selectedTrackPosition: Int,
        tracksList: MutableList<Track>,
        trackPlayStartPosition: Long
    ) {


        val intent = Intent(getViewActivity(), AudioPlayerService::class.java)
        intent.action = AudioPlayerService.PlaybackControls.PLAY.name
        intent.putExtra(Constant.SELECTED_TRACK_POSITION, selectedTrackPosition)
        intent.putExtra(Constant.PLAY_CONTEXT_TYPE, Constant.PLAY_CONTEXT.LOCAL_DEVICE_LIBRARY_TRACKS)
        Util.startForegroundService(getViewActivity(), intent)
        (activity as MainActivity).reBindService()
    }

    override fun getViewActivity(): AppCompatActivity {
        return activity as AppCompatActivity
    }

    override fun getApplicationContext(): Context {
        return (activity as AppCompatActivity).applicationContext
    }

    var songDataList: ArrayList<Track> = ArrayList()
    fun setPlayableContentListData() {
        songDataList= ArrayList()

        if (!localDeviceSongsList.isNullOrEmpty()) {
            for (i in localDeviceSongsList.indices) {
                if (i >= playableItemPosition) {
                    songDataList.add(localDeviceSongsList[i])
                }
            }
            if (!songDataList.isNullOrEmpty()) {
                BaseActivity.setTrackListData(songDataList)
                tracksViewModel.prepareTrackPlayback(0)
            }
        }

        /* Track Events in real time */
        val eventValue: MutableMap<String, Any> = HashMap()
        AppsFlyerLib.getInstance().logEvent(HungamaMusicApp.getInstance().applicationContext!!,EventConstant.AF_LOCAL_PLAY, eventValue)
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
            setMessage(R.string.library_all_str_5)
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
        setupDeviceSongList()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            READ_EXTERNAL_STORAGE_REQ_CODE -> {

                bAlreadyAskedForStoragePermission = false

                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupDeviceSongList()
                } else {
                    onShowPermissionRationale(permissions[0], READ_EXTERNAL_STORAGE_REQ_CODE)
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(
            KEY_STORAGE_PERMISSION_ALREADY_ASKED,
            bAlreadyAskedForStoragePermission
        )
        super.onSaveInstanceState(outState)
    }
}