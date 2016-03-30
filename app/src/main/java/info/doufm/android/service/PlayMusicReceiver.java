package info.doufm.android.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import info.doufm.android.constans.Constants;
import info.doufm.android.info.MusicInfo;
import info.doufm.android.user.UserHistoryInfo;
import info.doufm.android.user.UserLoveMusicInfo;
import io.realm.Realm;
import io.realm.RealmResults;


/**
 * Created by niostar on 15-3-22.
 */
public class PlayMusicReceiver extends BroadcastReceiver {
    private static final String TAG = "PlayMusicReceiver";
    public static final String FILTER = "edu.xidian.doufm.service.PlayMusicReceiver";

    private MusicInfo playMusicInfo;
    private MyBinder myBinder;
    private Context mContext;

    public PlayMusicReceiver(MyBinder myBinder, Context context) {
        this.myBinder = myBinder;
        this.mContext = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG,intent.getAction());
        playMusicInfo = findMusic(intent);
        Log.v(TAG,"playMusicInfo-->"+playMusicInfo.toString());
        myBinder.mService.playUserChooseMisic(playMusicInfo);
    }


    private MusicInfo findMusic(Intent intent) {
        MusicInfo tempMusicInfo = new MusicInfo();
        byte listType = intent.getByteExtra(Constants.EXTRA_LIST_TYPE, (byte) -1);
        if (listType == Constants.HISTORY_TYPE) {
            UserHistoryInfo userHistoryInfo = (UserHistoryInfo) intent.getSerializableExtra(Constants.EXTRA_MUSIC_ID);
            tempMusicInfo.setKey(userHistoryInfo.getKey());
            tempMusicInfo.setTitle(userHistoryInfo.getTitle());
            tempMusicInfo.setArtist(userHistoryInfo.getSinger());
            tempMusicInfo.setAudio(userHistoryInfo.getMusicURL());
            tempMusicInfo.setCover(userHistoryInfo.getCover());
        } else if (listType == Constants.LOVE_TYPE) {
            Realm realm = Realm.getInstance(mContext);
            RealmResults<UserLoveMusicInfo> realmResults = realm.where(UserLoveMusicInfo.class).equalTo("love_id", intent.getIntExtra(Constants.EXTRA_MUSIC_ID, -1)).findAll();
            tempMusicInfo.setKey(realmResults.get(0).getKey());
            tempMusicInfo.setTitle(realmResults.get(0).getTitle());
            tempMusicInfo.setArtist(realmResults.get(0).getSinger());
            tempMusicInfo.setAudio(realmResults.get(0).getMusicURL());
            tempMusicInfo.setCover(realmResults.get(0).getCover());
            tempMusicInfo.setLoved(true);
        }
        return tempMusicInfo;
    }

}
