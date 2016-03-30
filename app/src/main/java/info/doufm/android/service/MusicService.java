package info.doufm.android.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.android.volley.AuthFailureError;

import java.util.Timer;
import java.util.TimerTask;

import info.doufm.android.R;
import info.doufm.android.activity.MainActivity;
import info.doufm.android.constans.NetworkConstans;
import info.doufm.android.info.MusicInfo;
import info.doufm.android.user.User;
import info.doufm.android.utils.MusicInfoUtils;

/**
 * Created by WJ on 2015/1/27.
 */
public class MusicService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener,
                                                     MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnPreparedListener{

    private static final String TAG = "MusicService";
    private static final String INTENT_ACTION_TAG = "edu.xidian.doufm.service.MusicService";
    private static final String INTENT_BUTTON_TAG = "ButtonID";
    private NotificationManager mNotificationManager;
    private MyBinder myBinder;
    private NotificationCompat.Builder mBuilder;
    public MediaPlayer mediaPlayer;
    private NotificationBroadcastReceiver mReceiver;
    private RemoteViews contentView=null;
    public boolean isPlaying = false;
    public boolean isLoopPlaying = false;
    public boolean isFirstSong = true;
    public boolean isFirstOpen = true;
    private PlayMusicReceiver playMusicReceiver;
    private boolean phoneCome = false;
    private HeadSetReceiver headerSetReceiver;

    @Override
    public IBinder onBind(Intent intent) {
        //同时要在通知栏进行一个显示
        if(myBinder == null){
            myBinder = new MyBinder(getApplicationContext(),this);
        }
        return myBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG,"onDestory!");
        mNotificationManager.cancelAll();

        exitPlayMusic();
        unregisterReceiver(mReceiver);
        unregisterReceiver(playMusicReceiver);
        unregisterReceiver(headerSetReceiver);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG,"onCreate!");

        if(myBinder == null){
            myBinder = new MyBinder(getApplicationContext(),this);
            mBuilder = new NotificationCompat.Builder(getApplicationContext());
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            postNotification("听一首歌，奋斗不孤单","DouFM 睿思音乐电台");
            Log.v(TAG, "postedNotification");
            initMediaPlayer();
            Log.v(TAG, "after initMediaPlayer");
            getUserLoveList();
            Log.v(TAG, "after getUserLoveList");
            mReceiver = new NotificationBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(INTENT_ACTION_TAG);
            registerReceiver(mReceiver,intentFilter);

            playMusicReceiver = new PlayMusicReceiver(myBinder,getApplicationContext());
            IntentFilter intentFilter1 = new IntentFilter();
            intentFilter1.addAction(PlayMusicReceiver.FILTER);
            registerReceiver(playMusicReceiver, intentFilter1);

            phoneInComingListener();
            registerHeadSetListener();
        }

    }


    private void postNotification(String contentTitle, String contentText){

        contentView = new RemoteViews(getPackageName(),R.layout.notification);
        contentView.setTextViewText(R.id.tv_music_name_notification,"听一首歌，奋斗不孤单");
        contentView.setTextViewText(R.id.tv_music_singer_notification,"DouFM 睿思音乐电台");

        if(isPlaying){
            contentView.setImageViewResource(R.id.iv_play_notification,R.drawable.btn_stop_play);
        }else {
            contentView.setImageViewResource(R.id.iv_play_notification,R.drawable.btn_start_play);
        }

        Intent buttonIntent = new Intent(INTENT_ACTION_TAG);

        buttonIntent.putExtra(INTENT_BUTTON_TAG,R.id.iv_play_notification);
        PendingIntent playPendingIntent = PendingIntent.getBroadcast(getApplicationContext(),R.id.iv_play_notification,buttonIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        contentView.setOnClickPendingIntent(R.id.iv_play_notification,playPendingIntent);

        buttonIntent.putExtra(INTENT_BUTTON_TAG,R.id.iv_next_notification);
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(getApplicationContext(),R.id.iv_next_notification,buttonIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        contentView.setOnClickPendingIntent(R.id.iv_next_notification,nextPendingIntent);

        buttonIntent.putExtra(INTENT_BUTTON_TAG,R.id.iv_close_notification);
        PendingIntent closePendingIntent = PendingIntent.getBroadcast(getApplicationContext(),R.id.iv_close_notification,buttonIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        contentView.setOnClickPendingIntent(R.id.iv_close_notification,closePendingIntent);

        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(),1,new Intent(getApplicationContext(), MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher_small)
                .setTicker(contentTitle)
                .setContent(contentView)
                .setContentIntent(contentIntent);
        startForeground(1,mBuilder.build());
    }


    private void initMediaPlayer(){
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnPreparedListener(this);
        tryToPlayRandomMusic();
    }

    /**
     * 软件一开始启动就拉取用户信息
     * */
    private void getUserLoveList(){
        if(User.getInstance().getLogin() && isFirstOpen){
            MusicInfoUtils.downloadLoveList(this);
        }
    }


    /**
     * 停止播放音乐 并且清理相关资源
     */
    public void exitPlayMusic(){
        mediaPlayer.stop();
        mediaPlayer.release();
        isPlaying = false;
    }

    public void pausePlayMusic(){
        isPlaying = false;
        mediaPlayer.pause();
        updateNotificationView();
        myBinder.doPlayViewUIUpdate(NetworkConstans.STATE_STOP_MUSIC,0);
    }


    public void startPlayMusic(){
        Log.v(TAG,"start play music");
       // if(myBinder.isNetworkAvailable()){
            if(myBinder.getPlayMusicInfo() != null){
                isPlaying = true;
                myBinder.isError = false;
                mediaPlayer.start();
                updateNotificationView();
                myBinder.doPlayViewUIUpdate(NetworkConstans.STATE_PLAYING_MUSIC,0);
            }else {
                tryToPlayRandomMusic();
            }
      //  }else{
           // Toast.makeText(getApplicationContext(),"网络错误",Toast.LENGTH_LONG).show();
      //  }
    }


    public void playCurrentMusic(){
        pausePlayMusic();
        if(myBinder.playMusicInfo.isHasCache()){
            //播放缓存音乐
            myBinder.playCacheMusic();
            Log.v(TAG,"播放缓存音乐");
        }else{
            //播放在线音乐
            myBinder.playNetMusic(myBinder.playMusicInfo);
        }
    }



    public void playNextMusic(){
        pausePlayMusic();
        if(myBinder.nextMusicInfo != null){
            myBinder.preMusicInfo = myBinder.playMusicInfo;
            myBinder.playMusicInfo = myBinder.nextMusicInfo;
            myBinder.nextMusicInfo = null;

            if(myBinder.playMusicInfo.isHasCache()){
                //播放缓存音乐
                myBinder.playCacheMusic();
                Log.v(TAG,"播放缓存音乐");
            }else{
                //播放在线音乐
                try {
                    myBinder.playRandomMusic();
                } catch (AuthFailureError authFailureError) {
                    authFailureError.printStackTrace();
                }
            }
        }else {
            try {
                myBinder.playRandomMusic();
            } catch (AuthFailureError authFailureError) {
                authFailureError.printStackTrace();
            }
        }

    }



    public void playUserChooseMisic(MusicInfo musicInfo){
            pausePlayMusic();
            myBinder.playMusicInfo = musicInfo;
            if(myBinder.playMusicInfo.isHasCache()){
                //播放缓存音乐
                myBinder.playCacheMusic();
            }else{
                //播放在线音乐
                myBinder.playNetMusic(myBinder.playMusicInfo);
            }

    }


    public void playPreMusic(){
        if(myBinder.preMusicInfo != null){
            pausePlayMusic();
            myBinder.playMusicInfo = myBinder.preMusicInfo;
            myBinder.preMusicInfo = null;

            if(myBinder.playMusicInfo.isHasCache()){
                //播放缓存音乐
                myBinder.playCacheMusic();
            }else{
                //播放在线音乐
                myBinder.playNetMusic(myBinder.playMusicInfo);
            }
        }else{
            Toast.makeText(getApplicationContext(),"已经倒退到最后",Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * 更换音乐频道
     * @param channle
     */
    public void changeChannle(int channle){
        if(myBinder != null){
            myBinder.preMusicInfo = myBinder.playMusicInfo;
            myBinder.nextMusicInfo = null;

            isFirstSong = true;

            myBinder.setMyPlayChannleNum(channle);
            myBinder.mService.pausePlayMusic();
            try {
                myBinder.playRandomMusic();
            } catch (AuthFailureError authFailureError) {
                authFailureError.printStackTrace();
            }

        }
    }




    @Override
    public void onCompletion(MediaPlayer mp) {
        uploadUserListenHistory();
        if(isPlaying == true){
            mediaPlayer.stop();
            isPlaying = false;
            Log.v(TAG,"onCompletion");
            if(isLoopPlaying){
                playCurrentMusic();
            }else {
                playNextMusic();
            }

        }
    }



    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        //保存当前下载的进度

        myBinder.doPlayViewUIUpdate(NetworkConstans.UPDATE_BUF_PECENT, percent);


    }

    private void updateNotificationView(){
        Log.v(TAG,"updateNotificationView");
        if(myBinder.getPlayMusicInfo() == null){
            return;
        }
        if(isPlaying){
            contentView.setImageViewResource(R.id.iv_play_notification,R.drawable.btn_stop_play);
        }else {
            contentView.setImageViewResource(R.id.iv_play_notification,R.drawable.btn_start_play);
        }
        contentView.setTextViewText(R.id.tv_music_name_notification,myBinder.getPlayMusicInfo().getTitle());
        contentView.setTextViewText(R.id.tv_music_singer_notification,myBinder.getPlayMusicInfo().getArtist());
        mBuilder.setSmallIcon(R.mipmap.ic_launcher_small)
                .setContent(contentView);
        startForeground(1,mBuilder.build());

    }

    public void updateNotificationCover(Bitmap bitmap){
        contentView.setImageViewBitmap(R.id.iv_cover_notification,bitmap);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher_small)
                .setContent(contentView);
        startForeground(1,mBuilder.build());
    }



    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.v(TAG,"onPrepared");
        myBinder.setConverImage(myBinder.playMusicInfo);
        myBinder.doPlayViewUIUpdate(NetworkConstans.UPDATE_MUSIC_INFO, 0);
        mediaPlayer.start();
        isPlaying = true;
        myBinder.doPlayViewUIUpdate(NetworkConstans.STATE_START_PLAY_MUSIC, 0);
        myBinder.doPlayViewUIUpdate(NetworkConstans.STATE_PLAYING_MUSIC,0);
        updateNotificationView();
        //开始准备下一首歌曲
        try {

            if(isLoopPlaying)
                return;

            myBinder.runFlag = false;

            Log.v(TAG,"开始缓存下一首歌曲信息和内容");
            myBinder.getNextMusicInfo(myBinder.mPlayListInfoList.get(myBinder.getMyPlayChannleNum()).getKey());


        } catch (AuthFailureError authFailureError) {
            authFailureError.printStackTrace();
        }

    }


    /**
     * 开启软件后进行音乐播放
     */
    public void tryToPlayRandomMusic(){

        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
               // if (myBinder.isNetworkAvailable() && myBinder.hasGetPlayListInfo()) {
                if ( myBinder.hasGetPlayListInfo()) {
                    try {
                        myBinder.playRandomMusic();
                    } catch (AuthFailureError e) {
                        e.printStackTrace();
                    }
                    timer.cancel();
                    Log.v(TAG,"tryToPlayRandomMusic");
                }
            }
        }, 0, 1000);
    }


    /**
     * 用于注册一个监听器 监听电话状态改变
     */
    private void phoneInComingListener(){
        TelephonyManager telephoneManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        telephoneManager.listen(new MyPhoneListener(), PhoneStateListener.LISTEN_CALL_STATE);
    }

    private class MyPhoneListener extends PhoneStateListener{
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state){
                case TelephonyManager.CALL_STATE_RINGING:
                    if(isPlaying){
                        pausePlayMusic();
                        phoneCome = true;
                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    if(phoneCome == true && mediaPlayer != null){
                        startPlayMusic();
                        phoneCome = false;
                    }
                    break;
            }
        }
    }

    private void registerHeadSetListener(){
        headerSetReceiver = new HeadSetReceiver(myBinder);
        IntentFilter filter = new IntentFilter();
        filter.addAction(headerSetReceiver.Filter);
        registerReceiver(headerSetReceiver,filter);

    }


    private class NotificationBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG,"onReceiver-->"+intent.getIntExtra(INTENT_BUTTON_TAG,0));
            switch (intent.getIntExtra(INTENT_BUTTON_TAG,0)){
                case R.id.iv_play_notification:
                    if(isPlaying){
                        pausePlayMusic();
                    }else{
                        startPlayMusic();
                    }
                    break;
                case R.id.iv_next_notification:
                    playNextMusic();
                    break;
                case R.id.iv_close_notification:
                    Log.v(TAG, "iv_close_notification");
                    stopForeground(true);
                    stopService(new Intent(getApplicationContext(),MusicService.class));
                    myBinder.mainActivity.finish();
                    break;
            }
        }
    }


    private void uploadUserListenHistory() {
        if (User.getInstance().getLogin()) {
            //上传服务器
            if (myBinder.playMusicInfo.getKey() != null) {
                try {
                    MusicInfoUtils.uploadUserOp("listened", myBinder.playMusicInfo);
                } catch (AuthFailureError authFailureError) {
                    authFailureError.printStackTrace();
                }
            }
        }
    }


}
