package info.doufm.android.debug;

import android.content.Context;
import android.util.Log;

/**
 * Created by niostar on 15-10-27.
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private static volatile CrashHandler instance;  //单例引用，这里我们做成单例的，因为我们一个应用程序里面只需要一个UncaughtExceptionHandler实例

    private CrashHandler(){}

    public synchronized static CrashHandler getInstance(){  //同步方法，以免单例多线程环境下出现异常
        if (instance == null){
            instance = new CrashHandler();
        }
        return instance;
    }

    public void init(Context ctx){  //初始化，把当前对象设置成UncaughtExceptionHandler处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        Log.v("Sandy", "uncaughtException, thread: " + thread
                + " name: " + thread.getName() + " id: " + thread.getId() + "exception: "
                + ex);
        String threadName = thread.getName();

    }
}
