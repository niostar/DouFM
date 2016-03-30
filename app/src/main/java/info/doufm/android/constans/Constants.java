package info.doufm.android.constans;

import info.doufm.android.R;

/**
 * Created by niostar on 15-3-1.
 */
public class Constants {

    public static final boolean openTestMode = false;
    public static final String User_Agent = "Android-DouFM:3.0:863097177@qq.com";

    public static final int DISMISS = 1000;
    public static final int UPDATE_TIME = 2000;
    public static final int REQUEST_LOGIN_CODE = 300;
    public static final int REQUEST_USER_CODE = 400;
    public static final int REQUEST_WIFI_SETTING_CODE = 500;


    public static final String EXTRA_LIST_TYPE = "info.doufm.android.list_type";
    public static final String EXTRA_MUSIC_ID = "info.doufm.android.music_id";
    public static final String EXTRA_THEME = "info.doufm.android.theme";
    public static final String ACTION_CHOOSE_MUSIC = "info.doufm.android.action.CHOOSE_MUSIC";
    //new color scheme by lsc
    public static final String[] ACTIONBAR_COLORS = {
            "#384e87","#ba3a31","#08415e",
            "#248b75","#4e619c","#35a189",
            "#3d2e55","#5b3f3c","#3d2e55",
            "#464270","#373737","#314468"
    };

    public static final String[] BACKGROUND_COLORS = {
            "#272f61","#ecbeba","#3298be",
            "#51b39d","#a3b1e3","#cbdabc",
            "#e9aa99","#ed7d7d","#815683",
            "#4d4d8d","#272727","#7fb3ff"
    };
    public static final int[] SLIDE_MENU_HEADERS = {
            R.drawable.theme_01, R.drawable.theme_02, R.drawable.theme_03,
            R.drawable.theme_04, R.drawable.theme_05, R.drawable.theme_06,
            R.drawable.theme_07, R.drawable.theme_08, R.drawable.theme_09,
            R.drawable.theme_10, R.drawable.theme_11, R.drawable.theme_12 };
    //SharedPreferences
    public static final String USER_INFO = "userinfo";
    public static final String THEME = "theme";
    public static final String SAVE_USER_LOGIN_INFO_FLAG = "save_login_info_flag";
    public static final String LOGIN_USR_NAME = "rm_user_name";
    public static final String LOGIN_USR_PASSWORD = "rm_user_password";
    public static final String PLAYLIST = "playlist";
    public static final String COOKIE = "cookie";
    public static final String LAST_VERSION_CHECK_TIME = "last_version_check_time";
    public static final String LAST_DOWNLOAD_APP_OK = "last_download_app_ok";
    public static final String APP_DOWNLOAD_ID = "app_download_id";
    public static final byte HISTORY_TYPE = 1;
    public static final byte LOVE_TYPE = 2;
}
