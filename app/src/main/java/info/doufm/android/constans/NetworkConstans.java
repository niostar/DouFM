package info.doufm.android.constans;

/**
 * Created by WJ on 2015/1/28.
 */
public class NetworkConstans {

    // API URL
    // https://github.com/hellolintong/LinDouFm/blob/master/doc/api.md


    public static final String ROOT_URL = "http://doufm.info";
    public static final String PLAYLIST_URL = ROOT_URL + "/api/playlist/";
    public static final String LOGIN_URL = ROOT_URL + "/api/app_auth/";
    public static final String USER_MUSIC_URL = ROOT_URL + "/api/user/music/";
    public static final String USER_PROFILE_URL = ROOT_URL + "/api/user/profile/";
    public static final String LOGOUT_URL = ROOT_URL + "/api/user/logout/";
    public static final String USER_HISTORY_URL = ROOT_URL + "/api/user/history/";
    public static final String CHECK_APP_VERSION = ROOT_URL + "/api/android_app/";



    public static final int STATE_GOT_MUSIC_CHANNLE = 1;
    public static final int STATE_START_PLAY_MUSIC = 10;
    public static final int STATE_PLAYING_MUSIC = 11;
    public static final int STATE_STOP_MUSIC = 12;
    public static final int STATE_CHANGE_MUSIC = 13;

    public static final int UPDATE_MUSIC_INFO = 20;
    public static final int UPDATE_MUSIC_COVER = 21;
    public static final int UPDATE_SHOW_DIALOG = 22;

    public static final int UPDATE_BUF_PECENT = 23;
    public static final int CHECK_IS_ERROR = 24;


}
