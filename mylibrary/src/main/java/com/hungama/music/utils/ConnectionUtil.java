package com.hungama.music.utils;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.hungama.music.data.model.MediaObject;
import com.hungama.music.ui.main.view.fragment.NoInternetDialog;

import java.util.ArrayList;
import java.util.List;


public class ConnectionUtil implements LifecycleObserver {

    private static final String TAG = "LOG_TAG";
    private static ConnectivityManager mConnectivityMgr;
    private Context mContext;
    private NetworkStateReceiver mNetworkStateReceiver;

    /*
     * boolean indicates if my device is connected to the internet or not
     * */
    private boolean mIsConnected = false;
    static ConnectionMonitor mConnectionMonitor;


    /**
     * Indicates there is no available network.
     */
    private static final int NO_NETWORK_AVAILABLE = -1;


    /**
     * Indicates this network uses a Cellular transport.
     */
    public static final int TRANSPORT_CELLULAR = 0;


    /**
     * Indicates this network uses a Wi-Fi transport.
     */
    public static final int TRANSPORT_WIFI = 1;
    public static final int TRANSPORT_ETHERNET = 3;



    public interface ConnectionStateListener {
        void onAvailable(boolean isAvailable);
    }


    public ConnectionUtil(Context context) {

        try {
            mContext = context;
            if (mConnectivityMgr == null) {
                mConnectivityMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (mContext instanceof AppCompatActivity) {
                    ((AppCompatActivity) mContext).getLifecycle().addObserver(this);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                    if (mConnectionMonitor == null) {
                        mConnectionMonitor = new ConnectionMonitor();
                    }

                    NetworkRequest networkRequest = new NetworkRequest.Builder()
                            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                            .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
                            .build();
                    mConnectivityMgr.registerNetworkCallback(networkRequest, mConnectionMonitor);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * Returns true if connected to the internet, and false otherwise
     *
     * <p>
     * NetworkInfo is deprecated in API 29
     * https://developer.android.com/reference/android/net/NetworkInfo
     * <p>
     * getActiveNetworkInfo() is deprecated in API 29
     * https://developer.android.com/reference/android/net/ConnectivityManager#getActiveNetworkInfo()
     * <p>
     * getNetworkInfo(int) is deprecated as of API 23
     * https://developer.android.com/reference/android/net/ConnectivityManager#getNetworkInfo(int)
     */
    // Checking internet connectivity
    NetworkInfo activeNetwork = null;
    public boolean isOnline() {

        mIsConnected = false;

        if (mConnectionMonitor != null) {
            NetworkCapabilities networkCapabilities = mConnectivityMgr.getNetworkCapabilities(mConnectivityMgr.getActiveNetwork());
            if (networkCapabilities != null) {
                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                        || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN))
                    mIsConnected = true;
            }
        }

        if(!mIsConnected){
            openNoInternetPopup();
        }
        return mIsConnected;

    }

    public boolean isOnline(Boolean isDisplayPopup) {

        mIsConnected = false;

        if (mConnectionMonitor != null) {
            NetworkCapabilities networkCapabilities = mConnectivityMgr.getNetworkCapabilities(mConnectivityMgr.getActiveNetwork());
            if (networkCapabilities != null) {
                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                        || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN))
                    mIsConnected = true;
            }
        }

        if(!mIsConnected && isDisplayPopup){
            openNoInternetPopup();
        }
        return mIsConnected;

    }

    public NoInternetDialog sheet;
    private void openNoInternetPopup(){
        try{
            if(mContext!=null&&mContext instanceof AppCompatActivity){
                AppCompatActivity mActivity= (AppCompatActivity) mContext;
                //CommonUtils.INSTANCE.setLog(TAG, "openNoInternetPopup: getCurrentFragment-"+Utils.Companion.getCurrentFragment(mContext));
                if(sheet==null){
                    sheet = new NoInternetDialog(mActivity);
                }

                if(!NoInternetDialog.Companion.isShowing() && CommonUtils.INSTANCE.preventDoubleClick()){
                    CommonUtils.INSTANCE.setLog(TAG, "openNoInternetPopup: visiable");
                    sheet.show(mActivity.getSupportFragmentManager(), "NoInternetDialog");
                }else {
                    CommonUtils.INSTANCE.setLog(TAG, "openNoInternetPopup: not visiable");
                }


            }else {
                CommonUtils.INSTANCE.setLog(TAG, "openNoInternetPopup: not visiable");
            }
        }catch (Exception e){
            CommonUtils.INSTANCE.setLog(TAG, "openNoInternetPopup: not visiable error-"+e.getMessage());
        }


    }



    /**
     * Returns
     * <p> <p>
     * <p><p> NO_NETWORK_AVAILABLE >>> when you're offline
     * <p><p> TRANSPORT_CELLULAR >> When Cellular is the active network
     * <p><p> TRANSPORT_WIFI >> When Wi-Fi is the Active network
     * <p>
     */
    public int getActiveNetwork() {

        activeNetwork = mConnectivityMgr.getActiveNetworkInfo(); // Deprecated in API 29
        if (activeNetwork != null)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                NetworkCapabilities capabilities = mConnectivityMgr.getNetworkCapabilities(mConnectivityMgr.getActiveNetwork());
                if (capabilities != null)
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {

//                        NETWORK_TYPE = "MOBILE DATA";
                        NETWORK_TYPE = getNetworkType(activeNetwork.getSubtype());
                        // connected to mobile data
                        return TRANSPORT_CELLULAR;

                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        NETWORK_TYPE = "WIFI";
                        // connected to wifi
                        return TRANSPORT_WIFI;
                    }else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                        NETWORK_TYPE = "ETHERNET";
                        // connected to wifi
                        return TRANSPORT_ETHERNET;
                    }

            } else {
                if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) { // Deprecated in API 28
//                    NETWORK_TYPE = "MOBILE DATA";
                    NETWORK_TYPE = getNetworkType(activeNetwork.getSubtype());
                    // connected to mobile data
                    return TRANSPORT_CELLULAR;

                } else if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) { // Deprecated in API 28
                    NETWORK_TYPE = "WIFI";
                    // connected to wifi
                    return TRANSPORT_WIFI;
                }else if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) { // Deprecated in API 28
                    NETWORK_TYPE = "ETHERNET";
                    // connected to wifi
                    return TRANSPORT_ETHERNET;
                }
            }
        return NO_NETWORK_AVAILABLE;
    }

    public static String NETWORK_TYPE = "";

    public String getNetworkType() {
        if (getActiveNetwork() == TRANSPORT_WIFI) {
            NETWORK_TYPE = "WIFI";
            return NETWORK_TYPE;
        } else if (getActiveNetwork() == TRANSPORT_CELLULAR) {
            NETWORK_TYPE = getNetworkType(activeNetwork.getSubtype());
            return NETWORK_TYPE;
        }else if (getActiveNetwork() == TRANSPORT_ETHERNET) {
            NETWORK_TYPE = getNetworkType(activeNetwork.getSubtype());
            return NETWORK_TYPE;
        }

        return "NO NETWORK AVAILABLE";
    }



    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        CommonUtils.INSTANCE.setLog(TAG, "onDestroy");
        ((AppCompatActivity) mContext).getLifecycle().removeObserver(this);
        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (mConnectionMonitor != null)
                    mConnectivityMgr.unregisterNetworkCallback(mConnectionMonitor);
            } else {
                if (mNetworkStateReceiver != null)
                    mContext.unregisterReceiver(mNetworkStateReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public class NetworkStateReceiver extends BroadcastReceiver {

        ConnectionStateListener mListener;

        public NetworkStateReceiver(ConnectionStateListener listener) {
            mListener = listener;
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras() != null) {

                activeNetwork = mConnectivityMgr.getActiveNetworkInfo(); // deprecated in API 29

                /*
                 * activeNetworkInfo.getState() deprecated in API 28
                 * NetworkInfo.State.CONNECTED deprecated in API 29
                 * */
                if (!mIsConnected && activeNetwork != null && activeNetwork.getState() == NetworkInfo.State.CONNECTED) {
                    CommonUtils.INSTANCE.setLog(TAG, "onReceive: " + "Connected To: " + activeNetwork.getTypeName());
                    mIsConnected = true;
                    mListener.onAvailable(true);

                } else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
                    if (!isOnline()) {
                        mListener.onAvailable(false);
                        mIsConnected = false;
                    }

                }

            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public class ConnectionMonitor extends ConnectivityManager.NetworkCallback {

        private ConnectionStateListener mConnectionStateListener;

        void setOnConnectionStateListener(ConnectionStateListener connectionStateListener) {
            mConnectionStateListener = connectionStateListener;
        }

        @Override
        public void onAvailable(@NonNull Network network) {

            if (mIsConnected)
                return;

            CommonUtils.INSTANCE.setLog(TAG, "onAvailable: ");

            if (mConnectionStateListener != null) {
                mConnectionStateListener.onAvailable(true);
                mIsConnected = true;
            }

        }

        @Override
        public void onLost(@NonNull Network network) {
            CommonUtils.INSTANCE.setLog(TAG, "onLost: ");
            if (mConnectionStateListener != null) {
                mConnectionStateListener.onAvailable(false);
                mIsConnected = false;
            }

        }

    }


    public static ArrayList<MediaObject> prepareVideoList() {
        ArrayList<MediaObject> mediaObjectList = new ArrayList<>();
        MediaObject mediaObject = new MediaObject();
        mediaObject.setId(1);
        mediaObject.setUserHandle("@By Blender Foundation");
        mediaObject.setTitle(
                "Big Buck Bunny");
        mediaObject.setCoverUrl(
                "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/BigBuckBunny.jpg");
        mediaObject.setUrl("https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4");
        MediaObject mediaObject2 = new MediaObject();
        mediaObject2.setId(2);
        mediaObject2.setUserHandle("@By Blender Foundation");
        mediaObject2.setTitle(
                "Elephant Dream");
        mediaObject2.setCoverUrl(
                "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ElephantsDream.jpg");
        mediaObject2.setUrl("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4");
        MediaObject mediaObject3 = new MediaObject();
        mediaObject3.setId(3);
        mediaObject3.setUserHandle("@By Google");
        mediaObject3.setTitle("For Bigger Blazes");
        mediaObject3.setCoverUrl(
                "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ForBiggerBlazes.jpg");
        mediaObject3.setUrl("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4");
        MediaObject mediaObject4 = new MediaObject();
        mediaObject4.setId(4);
        mediaObject4.setUserHandle("@By Google");
        mediaObject4.setTitle("For Bigger Escape");
        mediaObject4.setCoverUrl(
                "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ForBiggerEscapes.jpg");
        mediaObject4.setUrl("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4");
        MediaObject mediaObject5 = new MediaObject();
        mediaObject5.setId(5);
        mediaObject5.setUserHandle("@By Google");
        mediaObject5.setTitle("For Bigger Fun?");
        mediaObject5.setCoverUrl(
                "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ForBiggerFun.jpg");
        mediaObject5.setUrl("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4");
        mediaObjectList.add(mediaObject);
        mediaObjectList.add(mediaObject2);
        mediaObjectList.add(mediaObject3);
        mediaObjectList.add(mediaObject4);
        mediaObjectList.add(mediaObject5);
        mediaObjectList.add(mediaObject);
        mediaObjectList.add(mediaObject2);
        mediaObjectList.add(mediaObject3);
        mediaObjectList.add(mediaObject4);
        mediaObjectList.add(mediaObject5);
        return mediaObjectList;
    }


    /**
     * To get device consuming netowkr type is 2g,3g,4g
     *
     * @return "2g","3g","4g" as a String based on the network type
     */
    public static String getNetworkType(int subType) {
        switch (subType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "2g";
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "3g";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "4g";
            default:
                return "MOBILE DATA";
        }
    }
}