package com.hungama.music.player.download;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.media3.exoplayer.offline.DownloadService;

import com.hungama.music.utils.CommonUtils;

import java.util.Objects;


public class DownloadReceiver extends BroadcastReceiver {
    public static final String ACTION_1 = "Press11111";
    public static final String ACTION_2 = "Press22222";



    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getStringExtra("action");

       // assert action != null;
        String toastStr = "you touch";
        if(!Objects.requireNonNull(action).isEmpty()) {

            if (action.equals(ACTION_1)) {
                //download......

//           DemoUtil.getDownloadManager(context).pauseDownloads();
//            DownloadService.sendPauseDownloads(context, DemoDownloadService.class,true);
                //Toast.makeText(context, toastStr + "Pause", Toast.LENGTH_SHORT).show();
            } else if (action.equals(ACTION_2)) {
                //cancle download......


//            Intent intent1= new Intent(context, DownloadService.class);
//            context.stopService(intent1);
//            new DownloadService().onTaskStop(intent);

//            DemoUtil.getDownloadManager(context).setStopReason(DemoUtil.getDownloadManager(context).getCurrentDownloads().get(0).request.id,DemoUtil.getDownloadManager(context).getCurrentDownloads().get(0).stopReason);
                DownloadService.sendRemoveAllDownloads(context, DemoDownloadService.class, true);
                //Toast.makeText(context, toastStr + "Stop", Toast.LENGTH_SHORT).show();

                CommonUtils.INSTANCE.setLog("TAG", "onReceive: Notification clicked");


            }
        }
    }
}