package info.doufm.android.utils;

import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import info.doufm.android.R;
import info.doufm.android.activity.MainActivity;
import info.doufm.android.activity.MainApplication;
import info.doufm.android.activity.UpgradeAppActivity;
import info.doufm.android.constans.Constants;
import info.doufm.android.constans.NetworkConstans;
import info.doufm.android.network.RequestManager;

/**
 * Created by WJ on 2015/11/24.
 */
public class CheckApplicationVersion {

    private static final String TAG = CheckApplicationVersion.class.getSimpleName();
    public static final int START_DOWNLOAD_APP = 100;
    public static final int NOTIFICATION_ID = 100;

    private static BroadcastReceiver broadcastReceiver;
    private static MyHandler handler = new MyHandler();
    private static AppUpdateInfo mAppUpdateInfo;
    private static DownloadManager downloadManager;
    private static String appPath;

    public static void run(final Context context){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                checkApplicationVersion(context);
            }
        });
        thread.start();
    }


    private static void checkApplicationVersion(final Context context){
        boolean isDownloaded = SharedPreferencesUtils.getBoolean(context, Constants.LAST_DOWNLOAD_APP_OK);
        long preTime = SharedPreferencesUtils.getLong(context, Constants.LAST_VERSION_CHECK_TIME);
        final long currentTime = System.currentTimeMillis();
//        if( (currentTime - preTime) > 24*60*60*1000){
            //do check
            SharedPreferencesUtils.putBoolean(context, Constants.LAST_DOWNLOAD_APP_OK, false);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(NetworkConstans.CHECK_APP_VERSION,
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {
                            mAppUpdateInfo = parseAppUpdateInfo(jsonObject);
                            MainApplication.appUpdateInfo = mAppUpdateInfo;
                            Log.v(TAG, mAppUpdateInfo.toString());
                            handler.sendEmptyMessage(START_DOWNLOAD_APP);
                            SharedPreferencesUtils.putLong(context, Constants.LAST_VERSION_CHECK_TIME, currentTime);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Log.v(TAG,volleyError.toString());
                        }
                    });
            RequestManager.getRequestQueue(context).add(jsonObjectRequest);
//        }
    }


    private static class MyHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case START_DOWNLOAD_APP:
                    checkAppVersion(MainApplication.mContext);
                break;

            }
        }
    }

    private static AppUpdateInfo parseAppUpdateInfo(JSONObject jsonObject){

        AppUpdateInfo appUpdateInfo = new AppUpdateInfo();
        try {
            appUpdateInfo.setDescription(jsonObject.getString("description"));
            appUpdateInfo.setHash_value(jsonObject.getString("file_hash"));
            appUpdateInfo.setUpload_date(jsonObject.getString("upload_date"));
            appUpdateInfo.setUrl(jsonObject.getString("url"));
            appUpdateInfo.setVersion(jsonObject.getString("version"));
            appUpdateInfo.setIsTest(jsonObject.getBoolean("test_flag"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return appUpdateInfo;
    }

    private static void checkAppVersion(Context mContext){
        Log.v(TAG,"check version");
        if(!Constants.openTestMode && mAppUpdateInfo.isTest)  // if response data is in test mode do nothing
            return;

        PackageManager packageManager = mContext.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(mContext.getPackageName(), 0);

            if(!packageInfo.versionName.equals(mAppUpdateInfo.getVersion())){
                postNotification(mContext, "有新版本更新", mAppUpdateInfo.getDescription());
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }


    private static void postNotification(Context mContext, String contentTitle, String contentText){

        Intent intent = new Intent(mContext, UpgradeAppActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(mContext,
                START_DOWNLOAD_APP, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
        builder.setSmallIcon(R.mipmap.ic_launcher_small);
        builder.setContentTitle(contentTitle);
        builder.setContentText(contentText);
        builder.setContentIntent(contentIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());

    }



    public static void downloadAPP(final Context context, AppUpdateInfo appUpdateInfo){
        Log.v(TAG,"download app");
        final Context mContext = context.getApplicationContext();
        downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        String url = NetworkConstans.ROOT_URL + appUpdateInfo.getUrl();
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        request.setAllowedOverRoaming(false);
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(url));
        request.setMimeType(mimeString);
        // 在通知栏中进行显示
        //request.setShowRunningNotification(true);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setVisibleInDownloadsUi(true);
        // 设置下载文件保存的目录
        request.setDestinationInExternalPublicDir("/download/", "DouFm-" + appUpdateInfo.getVersion() + ".apk");
        File file = Environment.getExternalStoragePublicDirectory("/download/");
        appPath = file.getPath()+"/DouFm-"+appUpdateInfo.getVersion()+".apk";
        Log.v(TAG, "appPath="+appPath);
        request.setTitle("DouFM更新中");
        long id = downloadManager.enqueue(request);
        SharedPreferencesUtils.putLong(mContext, Constants.APP_DOWNLOAD_ID, id);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                queryDownloadStatus(mContext);
            }
        };

        mContext.registerReceiver(broadcastReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }


    private static void queryDownloadStatus(Context mContext) {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(SharedPreferencesUtils.getLong(mContext, Constants.APP_DOWNLOAD_ID));
        Cursor c = downloadManager.query(query);
        if(c.moveToFirst()) {
            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            switch(status) {
                case DownloadManager.STATUS_PAUSED:
                    Log.v("down", "STATUS_PAUSED");
                case DownloadManager.STATUS_PENDING:
                    Log.v("down", "STATUS_PENDING");
                case DownloadManager.STATUS_RUNNING:
                    //正在下载，不做任何事情
                    Log.v("down", "STATUS_RUNNING");
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    //完成
                    Log.v("down", "下载完成");
                    installAPP(mContext);
                    break;
                case DownloadManager.STATUS_FAILED:
                    //清除已下载的内容，重新下载
                    Log.v("down", "STATUS_FAILED");
                    downloadManager.remove(SharedPreferencesUtils.getLong(mContext, Constants.APP_DOWNLOAD_ID));
                    SharedPreferencesUtils.putLong(mContext, Constants.APP_DOWNLOAD_ID, -1);
                    break;
            }
        }
    }

    private static void installAPP(Context mContext){
        if(!TextUtils.isEmpty(appPath)){
            // check whether the file is ok
            if(!CyptoUtils.verifyInstallPackage(appPath, MainApplication.appUpdateInfo.getHash_value())) {
                Log.v(TAG,"下载文件已损坏");
                File file = new File(appPath);
                file.delete();
                Toast.makeText(mContext, "下载文件已损坏", Toast.LENGTH_SHORT).show();
                return ;
            }
            File file = new File(appPath);
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_VIEW);
            String type = "application/vnd.android.package-archive";
            intent.setDataAndType(Uri.fromFile(file), type);
            mContext.startActivity(intent);
            SharedPreferencesUtils.putLong(mContext, Constants.APP_DOWNLOAD_ID, -1);
        }

    }


    public static class AppUpdateInfo {

        private String description;
        private String hash_value;
        private String upload_date;
        private String url;
        private String version;
        private boolean isTest;

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getHash_value() {
            return hash_value;
        }

        public void setHash_value(String hash_value) {
            this.hash_value = hash_value;
        }

        public String getUpload_date() {
            return upload_date;
        }

        public void setUpload_date(String upload_date) {
            this.upload_date = upload_date;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public boolean isTest() {
            return isTest;
        }

        public void setIsTest(boolean isTest) {
            this.isTest = isTest;
        }

        @Override
        public String toString() {
            return "AppUpdateInfo{" +
                    "description='" + description + '\'' +
                    ", hash_value='" + hash_value + '\'' +
                    ", upload_date='" + upload_date + '\'' +
                    ", url='" + url + '\'' +
                    ", version='" + version + '\'' +
                    ", isTest=" + isTest +
                    '}';
        }
    }

}
