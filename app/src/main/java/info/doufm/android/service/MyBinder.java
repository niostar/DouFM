package info.doufm.android.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import info.doufm.android.activity.MainActivity;
import info.doufm.android.activity.MainApplication;
import info.doufm.android.adapter.MusicListAdapter;
import info.doufm.android.constans.NetworkConstans;
import info.doufm.android.custom.PlayUIInterface;
import info.doufm.android.info.MusicInfo;
import info.doufm.android.info.PlayListInfo;
import info.doufm.android.io.DiskLruCache;
import info.doufm.android.network.JsonArrayRequestWithCookie;
import info.doufm.android.network.RequestManager;
import info.doufm.android.utils.CacheUtil;
import info.doufm.android.utils.MusicInfoUtils;

/**
 * Created by WJ on 2015/1/27.
 */
public class MyBinder extends Binder {

    private static final String TAG = "MyBinder";
    private Context context;
    public MusicService mService;
    public MainActivity mainActivity;

    public  List<PlayListInfo> mPlayListInfoList = null;
    private List<String> mLeftResideMenuItmeTitleList = null;
    private Handler handler;
    private ListView leftMusicChannelList;
    private Response.ErrorListener errorListener;

    public MusicInfo playMusicInfo=null;
    public MusicInfo nextMusicInfo=null;
    public MusicInfo preMusicInfo=null;
    private String localCookie;
    private Bitmap converBitmap=null;

    private int myPlayChannleNum=0;
    private PlayUIInterface mPlayUIInterface;

    private DiskLruCache mDiskLruCache;
    public DownloadMusicThread downloadMusicThread;
    private File cacheDir;
    public List<MusicInfo> currentListenHistory;
    public boolean runFlag;  //true 表示当前正在下载  false表示当前没有下载  下载的线程必须退出
    public boolean isError = false;


    public MyBinder(Context context,MusicService mService) {
        this.context = context;
        this.mService = mService;
        this.handler = new MyHandler();
        cacheDir = CacheUtil.getDiskCacheDir(MainApplication.mContext, "music");
        Log.v(TAG,"cacheDir = " + cacheDir);
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        try {
            mDiskLruCache = DiskLruCache.open(cacheDir,
                    CacheUtil.getAppVersion(MainApplication.mContext), 1, CacheUtil.DISK_CACHE_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
            mDiskLruCache = null;
        }

        currentListenHistory = new ArrayList<MusicInfo>(100);

    }

    public int getMyPlayChannleNum(){
        return myPlayChannleNum;
    }

    public void setMyPlayChannleNum(int myPlayChannleNum){
        this.myPlayChannleNum = myPlayChannleNum;
    }

    public void setMainActivity(Context context){
        mainActivity = (MainActivity)context;
    }


    public void setUIUpdateInterface(PlayUIInterface playUIInterface){
        mPlayUIInterface = playUIInterface;
    }

    public Bitmap getConverBitmap(){
        return converBitmap;
    }

    public void doPlayViewUIUpdate(int mode, int percent){
        if(mPlayUIInterface != null){
            mPlayUIInterface.updatePlayViewUI(mode,percent);
        }
    }


    public MusicInfo getPlayMusicInfo(){
        return playMusicInfo;
    }



    public void setConverImage(MusicInfo musicInfo){
        ImageRequest imageRequest = new ImageRequest(NetworkConstans.ROOT_URL + musicInfo.getCover(),
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        converBitmap = bitmap;
                        doPlayViewUIUpdate(NetworkConstans.UPDATE_MUSIC_COVER,0);
                        mService.updateNotificationCover(bitmap);
                    }
                },0,0,null,errorListener);
        RequestManager.getRequestQueue(MainApplication.mContext).add(imageRequest);
    }



    /** 用于设置音乐频道列表 从服务器获取数据在本地显示
     * @param mDrawerList
     * @throws IllegalStateException
     */
    public void setMusicChannelList(final ListView mDrawerList)throws IllegalStateException{
        Log.v(TAG,"getMusicList");
        leftMusicChannelList = mDrawerList;
        errorListener = new MyNetWorkErrorListener();
        if(mPlayListInfoList == null){
            Log.v(TAG,"begin getMusic List");
            mPlayListInfoList = new ArrayList<PlayListInfo>();
            try{
               // if(isNetworkAvailable()){
                    JsonArrayRequestWithCookie jsonArrayRequestWithCookie = new JsonArrayRequestWithCookie(NetworkConstans.PLAYLIST_URL,
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray jsonArray) {
                                    JSONObject jsonObject = new JSONObject();
                                    try{
                                        for(int i=0;i<jsonArray.length();i++){
                                            jsonObject = jsonArray.getJSONObject(i);
                                            PlayListInfo info = new PlayListInfo();
                                            info.setKey(jsonObject.getString("key"));
                                            info.setName(jsonObject.getString("name"));
                                            info.setMusic_list(jsonObject.getString("music_list"));
                                            mPlayListInfoList.add(info);
                                            Log.v(TAG, "" + jsonObject.getString("name"));
                                        }

                                        handler.sendEmptyMessage(NetworkConstans.STATE_GOT_MUSIC_CHANNLE);

                                    }catch (JSONException e){
                                        e.printStackTrace();
                                    }
                                }
                            },errorListener);
                    RequestManager.getRequestQueue(MainApplication.mContext).add(jsonArrayRequestWithCookie);
              //  }
            }catch (IllegalStateException e) {
                throw e;
            }
        }else{
            handler.sendEmptyMessage(NetworkConstans.STATE_GOT_MUSIC_CHANNLE);
        }
    }


    /**
     * 将频道信息进行显示
     */
    private void doSetMusicChannle(){
        mLeftResideMenuItmeTitleList = new ArrayList<String>();
        for(int i=0;i<mPlayListInfoList.size();i++){
            PlayListInfo info = mPlayListInfoList.get(i);
            mLeftResideMenuItmeTitleList.add(info.getName());
        }
        MusicListAdapter adapter = new MusicListAdapter(MainApplication.mContext,mLeftResideMenuItmeTitleList);
        leftMusicChannelList.setAdapter(adapter);
    }



    public boolean hasGetPlayListInfo(){
        if(mPlayListInfoList!=null && mPlayListInfoList.size()>0){
            return true;
        }else {
            return false;
        }
    }


    /**
     * 缓存下一首歌曲的信息和内容
     * @param playlist_key
     * @throws com.android.volley.AuthFailureError
     */
    public void getNextMusicInfo(String playlist_key) throws AuthFailureError{
        final String MUSIC_URL = NetworkConstans.PLAYLIST_URL + playlist_key + "/?num=1";
        JsonArrayRequestWithCookie request = new JsonArrayRequestWithCookie(MUSIC_URL,new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
                try{
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    nextMusicInfo = new MusicInfo();
                    nextMusicInfo.setTitle(jsonObject.getString("title"));
                    nextMusicInfo.setArtist(jsonObject.getString("artist"));
                    nextMusicInfo.setAudio(jsonObject.getString("audio"));
                    nextMusicInfo.setCover(jsonObject.getString("cover"));
                    nextMusicInfo.setKey(jsonObject.getString("key"));
                    Log.v(TAG,nextMusicInfo.toString());
                    boolean isLoved = MusicInfoUtils.isLoved(nextMusicInfo, MainApplication.mContext);
                    nextMusicInfo.setLoved(isLoved);
                    Log.v(TAG,"开始下载歌曲");
                    //获取完成信息后 开始下载该文件 并且在本地缓存
                    downloadMusicThread = new DownloadMusicThread(nextMusicInfo.getAudio());
                    downloadMusicThread.start();

                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        },errorListener);
        request.setCookie(localCookie);
        RequestManager.getRequestQueue(MainApplication.mContext).add(request);

    }


    /**
     * 播放缓存音乐
     */
    public void playCacheMusic() {
        Log.v(TAG,"播放缓存音乐");
        doPlayViewUIUpdate(NetworkConstans.STATE_CHANGE_MUSIC,0);
        String key = CacheUtil.hashKeyForDisk(playMusicInfo.getAudio());
        try {
            mService.mediaPlayer.pause();
            mService.mediaPlayer.stop();
            mService.mediaPlayer.reset();
            Log.v(TAG, "playcachemusic = " + cacheDir.toString() + "/" + key + ".0");
            mService.mediaPlayer.setDataSource(cacheDir.toString() + "/" + key + ".0");
            mService.mediaPlayer.prepareAsync();
            doPlayViewUIUpdate(NetworkConstans.UPDATE_BUF_PECENT, 100);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 播放在线音乐
     * @param musicInfo
     */
    public void playNetMusic(MusicInfo musicInfo){
        Log.v(TAG,"播放在线音乐");
        doPlayViewUIUpdate(NetworkConstans.STATE_CHANGE_MUSIC,0);
        //mService.mediaPlayer.stop();
        mService.mediaPlayer.reset();
        try {
            Log.v(TAG,"music url->"+NetworkConstans.ROOT_URL+musicInfo.getAudio());
            mService.mediaPlayer.setDataSource(NetworkConstans.ROOT_URL+musicInfo.getAudio());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.v(TAG,"before prepareAsync");
        mService.mediaPlayer.prepareAsync();

    }


    /**
     * 随机播放音乐
     * @throws com.android.volley.AuthFailureError
     */
    public void playRandomMusic() throws AuthFailureError{
        if(hasGetPlayListInfo()){
            runFlag = false;
            String Music_URL = NetworkConstans.PLAYLIST_URL+mPlayListInfoList.get(myPlayChannleNum).getKey()+"/?num=1";
            JsonArrayRequestWithCookie request = new JsonArrayRequestWithCookie(Music_URL,new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray jsonArray) {
                    try{
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        playMusicInfo = new MusicInfo();
                        playMusicInfo.setTitle(jsonObject.getString("title"));
                        playMusicInfo.setArtist(jsonObject.getString("artist"));
                        playMusicInfo.setAudio(jsonObject.getString("audio"));
                        playMusicInfo.setCover(jsonObject.getString("cover"));
                        playMusicInfo.setKey(jsonObject.getString("key"));
                        boolean isLoved = MusicInfoUtils.isLoved(playMusicInfo, MainApplication.mContext);
                        playMusicInfo.setLoved(isLoved);
                        playNetMusic(playMusicInfo);
                        currentListenHistory.add(playMusicInfo);
                        if(mService.isFirstSong){
                            Toast.makeText(MainApplication.mContext, "已加载频道" + "『" +
                                    mLeftResideMenuItmeTitleList.get(myPlayChannleNum) + "』", Toast.LENGTH_LONG).show();
                            mService.isFirstSong = false;
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }

                }
            },errorListener);
            request.setCookie(localCookie);
            RequestManager.getRequestQueue(MainApplication.mContext).add(request);
        }
    }


    private class MyHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case NetworkConstans.STATE_GOT_MUSIC_CHANNLE:
                    doSetMusicChannle();
                    break;
                default:
                    break;
            }
        }
    }


    public class DownloadMusicThread extends Thread {

        private String url;

        public DownloadMusicThread(String url) {
            this.url = url;
            runFlag = true;  //表示正在下载缓存
        }

        @Override
        public void run() {

            if(!runFlag)
                return;
            if(mDiskLruCache == null)
                return;
            try {
                String key = CacheUtil.hashKeyForDisk(url);
                Log.v(TAG,"download key = " + key);
                DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                if(editor != null && nextMusicInfo != null){
                    OutputStream outputStream = editor.newOutputStream(0);
                    if (downloadUrlToStream(NetworkConstans.ROOT_URL + url, outputStream)) {
                        editor.commit();
                       // Log.v(TAG,"Test 1");
                        nextMusicInfo.setHasCache(true);
                       // Log.v(TAG,"Test 2");
                        currentListenHistory.add(nextMusicInfo);
                        Log.v(TAG,"缓存下一首完成");
                        runFlag = false;
                    } else {
                        editor.abort();
                    }
                }
                mDiskLruCache.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

        private boolean downloadUrlToStream(String urlString, OutputStream outputStream) {
            HttpURLConnection urlConnection = null;
            BufferedOutputStream out = null;
            BufferedInputStream in = null;
            if(!runFlag)
                return false;
            try {
                final URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                in = new BufferedInputStream(urlConnection.getInputStream(), 8 * 1024);
                out = new BufferedOutputStream(outputStream, 8 * 1024);
                int b;
                while ((b = in.read()) != -1) {
                    if (runFlag) {
                        out.write(b);
                    } else {
                        return false;
                    }
                }
                return true;
            } catch (final IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null) {
                        in.close();
                    }
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }
    }


    private class MyNetWorkErrorListener implements Response.ErrorListener{
        @Override
        public void onErrorResponse(VolleyError volleyError) {
            mService.pausePlayMusic();
            isError = true;
            doPlayViewUIUpdate(NetworkConstans.UPDATE_SHOW_DIALOG,0);
        }
    }


}
