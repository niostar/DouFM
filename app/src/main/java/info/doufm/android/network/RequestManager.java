package info.doufm.android.network;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by WJ on 2015/1/28.
 */
public class RequestManager {

    private static RequestQueue mRequestQueue;
    private static String TAG = "RequestManager";

    private void RequestQueue(){
    }

    public static RequestQueue getRequestQueue(Context context){
        if(mRequestQueue==null){
            synchronized (TAG){
                if(mRequestQueue == null){
                    mRequestQueue = Volley.newRequestQueue(context);
                }
            }
        }
        return mRequestQueue;
    }

}
