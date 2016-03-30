package info.doufm.android.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by niostar on 15-3-31.
 */
public class HeadSetReceiver extends BroadcastReceiver {

    private static final String TAG = "HeadSetReceiver";
    public static final String Filter = "android.intent.action.HEADSET_PLUG";
    private MyBinder myBinder;

    public HeadSetReceiver(MyBinder myBinder) {
        this.myBinder = myBinder;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.hasExtra("state")){
            if(intent.getIntExtra("state",0) == 0){
                if(myBinder.mService.isPlaying){
                    myBinder.mService.pausePlayMusic();
                }
            }
        }
    }
}
