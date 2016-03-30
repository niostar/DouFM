package info.doufm.android.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.umeng.analytics.MobclickAgent;

import info.doufm.android.R;
import info.doufm.android.adapter.UserLoveListAdapter;
import info.doufm.android.constans.Constants;
import info.doufm.android.service.PlayMusicReceiver;
import info.doufm.android.user.User;
import info.doufm.android.user.UserLoveMusicInfo;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class UserLikeActivity extends ActionBarActivity {

    private static final String TAG = UserLikeActivity.class.getSimpleName();
    private Toolbar mToolbar;
    private int themeNum;
    private UserLoveListAdapter adapter;
    private RealmResults<UserLoveMusicInfo> userLoveInfoList;
    private ListView lvLove;
    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_like);
        context = this;
        themeNum = getIntent().getIntExtra(Constants.EXTRA_THEME, 13);
        findViews();
        initViews();
    }

    private void findViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar_custom);
        lvLove = (ListView) findViewById(R.id.lvLove);
    }

    private void initViews() {
        mToolbar.setBackgroundColor(Color.parseColor(Constants.ACTIONBAR_COLORS[themeNum]));
        mToolbar.setTitle("我的收藏");
        mToolbar.setTitleTextColor(Color.WHITE);
        mToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha));
        setSupportActionBar(mToolbar);
        LoadingLoveMusic();
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        lvLove.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.setAction(PlayMusicReceiver.FILTER);
                intent.putExtra(Constants.EXTRA_LIST_TYPE, Constants.LOVE_TYPE);
                intent.putExtra(Constants.EXTRA_MUSIC_ID, userLoveInfoList.get(position).getLove_id());
                sendBroadcast(intent);
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    private void LoadingLoveMusic() {
        Realm realm = Realm.getInstance(this);
        if (realm != null) {
            userLoveInfoList = realm.where(UserLoveMusicInfo.class)
                    .equalTo("userID", User.getInstance().getUserID())
                    .findAll();
            userLoveInfoList.sort("love_id", RealmResults.SORT_ORDER_DESCENDING);

            adapter = new UserLoveListAdapter(UserLikeActivity.this, userLoveInfoList);
            adapter.notifyDataSetChanged();
            lvLove.setAdapter(adapter);
            //解决java.lang.IllegalStateException: The content of the adapter has changed but ListView did not receive a notification.
            realm.addChangeListener(new RealmChangeListener() {
                @Override
                public void onChange() {
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(TAG);
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(TAG);
        MobclickAgent.onPause(this);
    }
}