package info.doufm.android.activity;

import android.app.Application;
import android.content.Context;

import info.doufm.android.debug.CrashHandler;
import info.doufm.android.user.User;
import info.doufm.android.utils.CheckApplicationVersion;

/**
 * Created with Android Studio.
 * Time: 2014-12-12 03:28
 * Info:
 */
public class MainApplication extends Application {

    private static final String TAG = MainApplication.class.getSimpleName();
    public static Context mContext;
    public static CheckApplicationVersion.AppUpdateInfo appUpdateInfo;


    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {

        //初始化用户类
        User.init(this);
        mContext = getApplicationContext();
        CrashHandler handler = CrashHandler.getInstance();
        handler.init(getApplicationContext());

        CheckApplicationVersion.run(mContext);
    }
}
