package com.hungama.music.ui.main.view.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hungama.music.R
import com.hungama.music.ui.main.adapter.ConnectedDeviceDialogAdapter
import com.hungama.music.data.model.DeviceConnectDialogModel

class ConnectDeviceDialog : BottomSheetDialogFragment() {

    lateinit var connectdatarecyclerview : RecyclerView
    private lateinit var connectadapter  : ConnectedDeviceDialogAdapter
    private var connectlists = mutableListOf<DeviceConnectDialogModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_device_connect,container,false)
    }
    @SuppressLint("UseRequireInsteadOfGet")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        connectdatarecyclerview = view.findViewById(R.id.rvList)
        connectdatarecyclerview.layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)
        connectadapter = ConnectedDeviceDialogAdapter(context!!)
        connectdatarecyclerview.adapter = connectadapter
        connectlists.add(DeviceConnectDialogModel("Airplay",R.drawable.connect_device_airplay_image))
        connectlists.add(DeviceConnectDialogModel("Chromecast",R.drawable.bottom_dialog_chromecast_image))
        connectlists.add(DeviceConnectDialogModel("Bluetooth",R.drawable.normal_bluthooth_image))
        connectadapter.setdata(connectlists)
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(context!!,R.style.BottomSheetDialogTheme)
    }
}