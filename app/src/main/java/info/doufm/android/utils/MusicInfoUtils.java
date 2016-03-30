package info.doufm.android.utils;

import android.content.Context;
import android.os.UserManager;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import info.doufm.android.activity.MainApplication;
import info.doufm.android.constans.Constants;
import info.doufm.android.constans.NetworkConstans;
import info.doufm.android.info.MusicInfo;
import info.doufm.android.network.JsonArrayRequestWithCookie;
import info.doufm.android.network.JsonObjectRequestWithCookie;
import info.doufm.android.network.RequestManager;
import info.doufm.android.user.User;
import info.doufm.android.user.UserLoveMusicInfo;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by niostar on 15-3-1.
 */
public class MusicInfoUtils {
    private static final String TAG = "MusicInfoUtils";

    public static void downloadLoveList(final Context context){
        //从服务器获取喜欢列表信息
        JsonArrayRequestWithCookie jsonArrayRequestWithCookie = new JsonArrayRequestWithCookie(NetworkConstans.USER_MUSIC_URL + "?type=favor&start=0&end=1000", new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
                int sum = jsonArray.length();
                for (int i = 0; i < sum; i++) {
                    MusicInfo loveMusicInfo = new MusicInfo();
                    try {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        loveMusicInfo.setKey(jsonObject.getString("key"));
                        loveMusicInfo.setArtist(jsonObject.getString("artist"));
                        loveMusicInfo.setTitle(jsonObject.getString("title"));
                        loveMusicInfo.setAudio(jsonObject.getString("audio"));
                        loveMusicInfo.setCover(jsonObject.getString("cover"));
                        saveLoveMusic(loveMusicInfo, sum - i, context);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.w("LOG", "show favor history error " + volleyError);
            }
        });
        try {
            String localCookie = SharedPreferencesUtils.getString(context, Constants.COOKIE, "");
            jsonArrayRequestWithCookie.setCookie(localCookie);
        } catch (AuthFailureError authFailureError) {
            authFailureError.printStackTrace();
        }
        RequestManager.getRequestQueue(context).add(jsonArrayRequestWithCookie);
    }


    private static void saveLoveMusic(MusicInfo musicInfo, int love_id, Context context) {
        if (User.getInstance().getLogin()) {
            Realm realm = Realm.getInstance(context);

            RealmResults<UserLoveMusicInfo> results = realm.where(UserLoveMusicInfo.class)
                    .equalTo("musicURL", musicInfo.getAudio())
                    .equalTo("userID", User.getInstance().getUserID())
                    .findAll();

            if(results.size() == 0){

                RealmResults<UserLoveMusicInfo> results1 = realm.where(UserLoveMusicInfo.class)
                        .equalTo("userID", User.getInstance().getUserID())
                        .findAll();

                UserLoveMusicInfo userLoveMusicInfo = new UserLoveMusicInfo();
                userLoveMusicInfo.setCover(musicInfo.getCover());
                userLoveMusicInfo.setKey(musicInfo.getKey());
                userLoveMusicInfo.setMusicURL(musicInfo.getAudio());
                userLoveMusicInfo.setSinger(musicInfo.getArtist());
                userLoveMusicInfo.setTitle(musicInfo.getTitle());
                userLoveMusicInfo.setUserID(User.getInstance().getUserID());
                if(love_id <= 0){
                    userLoveMusicInfo.setLove_id(results1.size()+1);
                }else {
                    userLoveMusicInfo.setLove_id(love_id);
                }
                Log.v(TAG,"userLovedMusicInfo --> " + userLoveMusicInfo.getTitle() + " " + userLoveMusicInfo.getLove_id());
                realm.beginTransaction();
                realm.copyToRealm(userLoveMusicInfo);
                realm.commitTransaction();
            }

            realm.close();
        }
    }

    private static void deleteLovedMusicInfo(MusicInfo musicInfo, Context context){
        if(User.getInstance().getLogin()){
            Realm realm = Realm.getInstance(context);
            realm.beginTransaction();
            RealmResults<UserLoveMusicInfo> results = realm.where(UserLoveMusicInfo.class).equalTo("userID", User.getInstance().getUserID())
                    .equalTo("musicURL", musicInfo.getAudio())
                    .findAll();

            Log.v(TAG,"delteLovedMusicInfo size = " + results.size());
            results.remove(0);

            realm.commitTransaction();
            realm.close();
        }
    }

    public static boolean isLoved(MusicInfo musicInfo,Context context){
        Realm realm = Realm.getInstance(context);
        RealmResults<UserLoveMusicInfo> realmResults = realm.where(UserLoveMusicInfo.class)
                .equalTo("musicURL",musicInfo.getAudio())
                .equalTo("userID", User.getInstance().getUserID())
                .findAll();

        if(realmResults.size() > 0){
            realm.close();
            return true;
        }
        realm.close();
        return false;
    }

    public static void addLovedMusicInfo(MusicInfo musicInfo,Context context){
        Realm realm = Realm.getInstance(context);
        if(musicInfo.getAudio() != null){
            RealmResults<UserLoveMusicInfo> realmResults = realm.where(UserLoveMusicInfo.class).equalTo("musicURL",musicInfo.getAudio()).findAll();
            if(realmResults.size() == 0){
                //说明这首歌曲信息没有进行保存 这里则进行保存
                RealmResults<UserLoveMusicInfo> records = realm.where(UserLoveMusicInfo.class).findAll();
                realm.beginTransaction();
                UserLoveMusicInfo newMusicInfo = realm.createObject(UserLoveMusicInfo.class);
                newMusicInfo.setLove_id(records.size() + 1);
                newMusicInfo.setKey(musicInfo.getKey());
                newMusicInfo.setTitle(musicInfo.getTitle());
                newMusicInfo.setSinger(musicInfo.getArtist());
                newMusicInfo.setCover(musicInfo.getCover());
                newMusicInfo.setMusicURL(musicInfo.getAudio());
                for(int i = 1; i < records.size(); i++){
                    records.get(i - 1).setLove_id(i);
                }
                realm.commitTransaction();
            }
        }
        realm.close();
    }

    public static void uploadUserOp(String opType, final MusicInfo musicInfo) throws AuthFailureError {
        // 1 upload the operation to the server
        // 2 update the local favor database

        HashMap<String, String> opMap = new HashMap<String, String>();
        opMap.put("op", opType);
        opMap.put("key", musicInfo.getKey());
        JSONObject opJsonObject = new JSONObject(opMap);
        JsonObjectRequestWithCookie jsonObjectRequestWithCookie = new JsonObjectRequestWithCookie(NetworkConstans.USER_HISTORY_URL, opJsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.w("LOG", "get response jsonObject from post user history" + jsonObject.toString());
                try {
                    if (jsonObject.getString("status").equals("listened_success")) {
                        Log.w("LOG", "post listened success");
                    } else if (jsonObject.getString("status").equals("favor_success")) {
                        Log.w("LOG", "favor success");
                        updateFavorMusic(musicInfo, true);
                    } else if (jsonObject.getString("status").equals("dislike_success")) {
                        Log.w("LOG", "dislike success");
                        updateFavorMusic(musicInfo, false);
                    } else if (jsonObject.getString("status").equals("shared_success")) {
                        Log.w("LOG", "shared success");
                    } else {
                        Log.w("LOG", "post /api/use/history/ failure");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.w("LOG", "操作失败");
            }
        }, opMap);
        String localCookie = SharedPreferencesUtils.getString(MainApplication.mContext, Constants.COOKIE, "");
        jsonObjectRequestWithCookie.setCookie(localCookie);
        RequestManager.getRequestQueue(MainApplication.mContext).add(jsonObjectRequestWithCookie);
    }


    private static void updateFavorMusic(MusicInfo musicInfo, boolean isNeedAdd){

        if(isNeedAdd){
            //add the music to the local database
            saveLoveMusic(musicInfo, -1, MainApplication.mContext);
        }else {
            // delete the music from local database
            deleteLovedMusicInfo(musicInfo, MainApplication.mContext);
        }
    }

}
