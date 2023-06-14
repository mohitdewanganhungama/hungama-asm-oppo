package com.hungama.music.ui.main.view.fragment

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.bluetooth.*
import android.content.*
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
import android.os.Parcelable
import android.text.TextUtils
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.hungama.music.IBluetoothA2dp
import com.hungama.music.R
import com.hungama.music.data.model.ConnectedDialogModel
import com.hungama.music.data.model.MessageModel
import com.hungama.music.data.model.MessageType
import com.hungama.music.ui.main.adapter.ConnectDeviceDialogAdapter
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.utils.CommonUtils.isBluetoothDeviceConnected
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.customview.bottomsheet.SuperBottomSheetFragment
import kotlinx.android.synthetic.main.dialog_device_connected.*
import java.lang.reflect.Method
import java.util.*
import kotlin.collections.ArrayList



class ConnectedDeviceDialog(val onConnectedDeviceMenuItemClicked:OnConnectedDeviceMenuItemClick) : SuperBottomSheetFragment(), ConnectDeviceDialogAdapter.onItemClick {

    private var connecteddeviceadapter : ConnectDeviceDialogAdapter? = null

    private var connectdeviceList = mutableListOf<ConnectedDialogModel>()

    private var mDeviceList = ArrayList<BluetoothDevice>()

    var mBluetoothAdapter: BluetoothAdapter? = null
    private var mProgressDlg: ProgressDialog? = null
    var lastSelectedDevice:BluetoothDevice? = null


    private var isEnabled: Boolean = false
    private var REQUEST_ENABLE_BT = 0
    private var devices: MutableSet<BluetoothDevice>? = null
    private var device: BluetoothDevice? = null
    private var b: IBinder? = null
    private lateinit var a2dp: BluetoothA2dp  //class to connect to an A2dp device
    private lateinit var ia2dp: IBluetoothA2dp

    private var mIsA2dpReady = false



    fun setIsA2dpReady(ready: Boolean) {
        mIsA2dpReady = ready
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.dialog_device_connected, container, false)
    }

    override fun getCornerRadius() =
        requireContext().resources.getDimension(R.dimen.common_popup_round_corner)

    override fun getStatusBarColor() = Color.RED

    override fun isSheetAlwaysExpanded(): Boolean = true
    override fun getExpandedHeight(): Int =
        requireContext().resources.getDimension(R.dimen.dimen_360).toInt()

    /*override fun getPeekHeight(): Int = requireContext().resources.getDimension(R.dimen.dimen_540).toInt()*/

    override fun getBackgroundColor(): Int =
        requireContext().resources.getColor(R.color.transparent)

    override fun isSheetCancelableOnTouchOutside(): Boolean = false

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val connectedDevice = CommonUtils.getCurrentConnectedBTDevice(requireContext())
        if (connectedDevice != null && !TextUtils.isEmpty(connectedDevice.name)){
            tvSubTitle.text = connectedDevice?.name
            ivConnectedDevice.setImageDrawable(requireContext().faDrawable(R.string.icon_bluetooth, R.color.connectedDevice))
        }
        mBluetoothAdapter	= BluetoothAdapter.getDefaultAdapter()

        mProgressDlg = ProgressDialog(requireActivity())

        mProgressDlg?.setMessage("Scanning...")
        mProgressDlg?.setCancelable(false)
        mProgressDlg?.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel"
        ) { dialog, which ->
            dialog.dismiss()
            mBluetoothAdapter?.cancelDiscovery()
        }

        if (mBluetoothAdapter == null) {
            showUnsupported()
        } else {
            getPairedDevices()
        }

        val filter = IntentFilter()

        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)

        requireContext().registerReceiver(mReceiver, filter)
        btnClose?.setOnClickListener {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), btnClose!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            dismiss()
        }



    }



    override fun onItemClick(connectedData: ConnectedDialogModel) {
        //Toast.makeText(requireContext(), connectedData?.Title, Toast.LENGTH_LONG).show()
        onConnectedDeviceMenuItemClicked?.onConnectedDeviceMenuItemClick(connectedData)
        dismiss()
    }

    interface OnConnectedDeviceMenuItemClick {
        fun onConnectedDeviceMenuItemClick(connectedData: ConnectedDialogModel)
    }

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            if (BluetoothAdapter.ACTION_STATE_CHANGED == action) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                if (state == BluetoothAdapter.STATE_ON) {
                    showToast("Enabled")
                    getPairedDevices()
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED == action) {
                mProgressDlg?.show()
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                mProgressDlg?.dismiss()
                setDeviceList(mDeviceList)
            } else if (BluetoothDevice.ACTION_FOUND == action) {
                val device =
                    intent.getParcelableExtra<Parcelable>(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice?
                mDeviceList.add(device!!)
                //showToast("Found device " + device.name)
            }else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED == action) {
                val state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)
                val prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR)
                val device =
                    intent.getParcelableExtra<Parcelable>(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice?
                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    showToast("Paired");
                    //updateList(true)
                    connectUsingBluetoothA2dp(device)

                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
                    showToast("Unpaired");
                    //updateList(isBluetoothDeviceConnected(device))
                }
            }else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                updateList(true, device)
            }else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                //Device has disconnected
                updateList(true, null)
            }
        }
    }

    override fun onPause() {
        /*if (mBluetoothAdapter != null) {
            if (mBluetoothAdapter?.isDiscovering!!) {
                mBluetoothAdapter?.cancelDiscovery()
            }
        }*/
        super.onPause()
    }

    override fun onDestroy() {
        requireContext().unregisterReceiver(mReceiver)
        super.onDestroy()
    }


    private fun showUnsupported() {
        showToast("Bluetooth is unsupported by this device")
    }
    private fun showToast(message: String) {
        val messageModel = MessageModel(message, MessageType.NEUTRAL, true)
        CommonUtils.showToast(requireContext(), messageModel)
    }

    private fun setDeviceList(mDeviceList: ArrayList<BluetoothDevice>) {
        setLog("TAG", "setDeviceList: mDeviceList "+mDeviceList)
        setLog("TAG", "setDeviceList: mDeviceList.size "+mDeviceList.size)
        rvList.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        connecteddeviceadapter = ConnectDeviceDialogAdapter(requireContext(), this)
        rvList.adapter = connecteddeviceadapter
        /*connectdeviceList.add(ConnectedDialogModel(1, getString(R.string.airplay),R.string.icon_airplay))
        connectdeviceList.add(ConnectedDialogModel(2, getString(R.string.chromcast),R.string.icon_cast))
        connectdeviceList.add(ConnectedDialogModel(3, getString(R.string.music_player_str_7),R.string.icon_bluetooth))*/
        if (connecteddeviceadapter != null){
            connecteddeviceadapter?.setdata(mDeviceList)
        }
    }
    private fun getPairedDevices(){
        if (mBluetoothAdapter?.isEnabled == true) {
            val pairedDevices = mBluetoothAdapter?.bondedDevices
            if (pairedDevices == null || pairedDevices.size == 0) {
                showToast("No Paired Devices Found")
            } else {
                mDeviceList.addAll(pairedDevices)
                mBluetoothAdapter?.startDiscovery()
                connectUsingBluetoothA2dp(device)
            }
        } else {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(intent, 1000)
        }

    }

    override fun onPairClick(position: Int) {
        val device = mDeviceList[position]

        if (device.bondState == BluetoothDevice.BOND_BONDED) {
            try {
                /*showToast("Unpairing...")
                removeBond(device)
                lastSelectedDevice = null*/
                //disConnectBTDevice(device)
                    if (isBluetoothDeviceConnected(device)){
                        disConnectUsingBluetoothA2dp(device)
                        setLog("BluetoothDevice-11", "disconnect")
                    }else{
                        connectUsingBluetoothA2dp(device)
                        setLog("BluetoothDevice-12", "connect")
                    }

                lastSelectedDevice = null
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        } else {
            showToast("Pairing...")
            try {

                createBond(device)
                //connectBTDevice(device)
                lastSelectedDevice = device
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun createBond(device: BluetoothDevice) {
        try {
            device::class.java.getMethod("createBond").invoke(device)
        } catch (e: Exception) {
            setLog("BluetoothDevice-1", "Removing bond has been failed. ${e.message}")
        }
    }
    fun removeBond(device: BluetoothDevice) {
        try {
            device::class.java.getMethod("removeBond").invoke(device)
        } catch (e: Exception) {
            setLog("BluetoothDevice-2", "Removing bond has been failed. ${e.message}")
        }
    }

    private fun updateList(bluetoothDeviceConnected: Boolean, device: BluetoothDevice?) {
        if (connecteddeviceadapter != null){
            connecteddeviceadapter?.notifyDataSetChanged()
        }
        var connectedData = ConnectedDialogModel(4, getString(R.string.popup_str_52), R.string.icon_device)
        if (device != null){
            connectedData = ConnectedDialogModel(3, device.name.toString(), R.string.icon_bluetooth)
        }
        //if (bluetoothDeviceConnected){
            onConnectedDeviceMenuItemClicked?.onConnectedDeviceMenuItemClick(connectedData)
            dismiss()
        //}
    }

    private fun connectBTDevice(device: BluetoothDevice){
        try {
            device::class.java.getMethod("connect").invoke(device)
        } catch (e: Exception) {
            setLog("BluetoothDevice-10", "connect bond has been failed. ${e.message}")
        }
    }
    private fun disConnectBTDevice(device: BluetoothDevice){
        try {
            device::class.java.getMethod("disconnect").invoke(device)
        } catch (e: Exception) {
            setLog("BluetoothDevice-11", "disconnect bond has been failed. ${e.message}")
        }
    }

    @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
    fun connectUsingBluetoothA2dp(
        deviceToConnect: BluetoothDevice?
    ) {
        try {
            val c2 = Class.forName("android.os.ServiceManager")
            val m2: Method = c2.getDeclaredMethod("getService", String::class.java)
            b = m2.invoke(c2.newInstance(), "bluetooth_a2dp") as IBinder?
            if (b == null) {
                // For Android 4.2 Above Devices
                device = deviceToConnect
                //establish a connection to the profile proxy object associated with the profile
                BluetoothAdapter.getDefaultAdapter().getProfileProxy(
                    requireActivity(),
                    // listener notifies BluetoothProfile clients when they have been connected to or disconnected from the service
                    object : BluetoothProfile.ServiceListener {
                        override fun onServiceDisconnected(profile: Int) {
                            setIsA2dpReady(false)
                            disConnectUsingBluetoothA2dp(device)
                        }

                        override fun onServiceConnected(
                            profile: Int,
                            proxy: BluetoothProfile
                        ) {
                            a2dp = proxy as BluetoothA2dp
                            if (deviceToConnect != null){
                                try {
                                    //establishing bluetooth connection with A2DP devices
                                    a2dp.javaClass
                                        .getMethod("connect", BluetoothDevice::class.java)
                                        .invoke(a2dp, deviceToConnect)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                setIsA2dpReady(true)
                            }


                        }
                    }, BluetoothProfile.A2DP
                )
            } else {
                val c3 =
                    Class.forName("android.bluetooth.IBluetoothA2dp")
                val s2 = c3.declaredClasses
                val c = s2[0]
                val m: Method = c.getDeclaredMethod("asInterface", IBinder::class.java)
                m.isAccessible = true
                ia2dp = m.invoke(null, b) as IBluetoothA2dp
                ia2dp.connect(deviceToConnect)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
    fun disConnectUsingBluetoothA2dp(
        deviceToConnect: BluetoothDevice?
    ) {
        try {
            device = deviceToConnect
            // For Android 4.2 Above Devices
            if (b == null) {
                try {
                    //disconnecting bluetooth device
                    a2dp.javaClass.getMethod(
                        "disconnect",
                        BluetoothDevice::class.java
                    ).invoke(a2dp, deviceToConnect)
                    BluetoothAdapter.getDefaultAdapter()
                        .closeProfileProxy(BluetoothProfile.A2DP, a2dp)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                ia2dp.disconnect(deviceToConnect)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}