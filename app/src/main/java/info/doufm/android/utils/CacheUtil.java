package info.doufm.android.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 缓存帮助类
 * Created by niostar on 15-2-28.
 */
public class CacheUtil {
    private static final String TAG = "CacheUtil";
    public static final int DISK_CACHE_SIZE = 20*1024*1024;

    /**
     * 将byte数组转换成十六进制的字符串
     * @param bytes
     * @return
     */
    public static String bytesToHexString(byte[] bytes){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < bytes.length; i++){
            String hex = Integer.toHexString(0xFF&bytes[i]);
            if(hex.length() == 1){
                sb.append('0');
            }
            sb.append(hex);
        }

        return sb.toString();
    }


    /**
     * 对 String进行散列操作
     * @param key
     * @return
     */
    public static String hashKeyForDisk(String key){
        String cacheKey;
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(key.getBytes());
            cacheKey = bytesToHexString(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }

        return cacheKey;
    }


    /**
     * 获取当前APP的版本号
     * @param context
     * @return
     */
    public static int getAppVersion(Context context){
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(),0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 3;
    }


    /**
     * 获取手机的缓存目录
     * @param context
     * @param uniqueName
     * @return
     */
    public static File getDiskCacheDir(Context context, String uniqueName){
        Log.v(TAG,Environment.getExternalStorageState().toString());
        String cachePath;
        //经测试,对于有两块SD卡的手机该处判断代码同样适用,但是默认使用的外部缓存目录是/storage/sdcard0/Android/data
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            try{
                cachePath = context.getExternalCacheDir().getPath();
                Log.v(TAG, "cachePath=" + cachePath);
            }catch (Exception e){
                e.printStackTrace();
                Log.v(TAG,"Extern SD card Read Error we should use internal SD");
            }finally {
                cachePath = context.getCacheDir().getPath();
            }

        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }

}
