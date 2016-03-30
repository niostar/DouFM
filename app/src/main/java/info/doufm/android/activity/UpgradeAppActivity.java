package info.doufm.android.activity;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import info.doufm.android.R;
import info.doufm.android.constans.Constants;
import info.doufm.android.utils.CheckApplicationVersion;
import info.doufm.android.utils.SharedPreferencesUtils;

/**
 * Created by WJ on 2015/11/25.
 */
public class UpgradeAppActivity extends ActionBarActivity implements View.OnClickListener{

    private static final String TAG = UpgradeAppActivity.class.getSimpleName();
    private int mThemeNum;
    private Toolbar mToolbar;
    private NotificationManager mNotificationManager;
    private TextView tvUpgradeTitle;
    private TextView tvUpgradeContent;
    private Button btnUpgrade;
    private StateListDrawable mStateListDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_upgrade);
        mThemeNum = SharedPreferencesUtils.getInt(UpgradeAppActivity.this, Constants.THEME, 11);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(CheckApplicationVersion.NOTIFICATION_ID);

        findViews();
        initViews();

    }


    private void findViews(){
        mToolbar = (Toolbar) findViewById(R.id.toolbar_custom);
        tvUpgradeTitle = (TextView) findViewById(R.id.tv_upgrade_title);
        tvUpgradeContent = (TextView) findViewById(R.id.tv_upgrade_content);
        btnUpgrade = (Button) findViewById(R.id.btn_start_upgrade);
    }

    private void initViews(){
        mToolbar.setBackgroundColor(Color.parseColor(Constants.ACTIONBAR_COLORS[mThemeNum]));
        mToolbar.setTitle("软件升级");
        mToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationContentDescription(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(MainApplication.appUpdateInfo != null){
            tvUpgradeTitle.setText("DouFM『" + MainApplication.appUpdateInfo.getVersion() + "』");
            tvUpgradeContent.setText(MainApplication.appUpdateInfo.getDescription());


            mStateListDrawable = new StateListDrawable();
            mStateListDrawable.addState(new int[]{android.R.attr.state_pressed},
                    new ColorDrawable(Color.parseColor(Constants.ACTIONBAR_COLORS[mThemeNum])));
            mStateListDrawable.addState(new int[]{android.R.attr.state_enabled},
                    new ColorDrawable(Color.parseColor(Constants.BACKGROUND_COLORS[mThemeNum])));

            if (Build.VERSION.SDK_INT >= 16) {
                btnUpgrade.setBackground(mStateListDrawable);
            } else {
                btnUpgrade.setBackgroundDrawable(mStateListDrawable);
            }

            btnUpgrade.setOnClickListener(this);

        }

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_start_upgrade:
                CheckApplicationVersion.downloadAPP(MainApplication.mContext, MainApplication.appUpdateInfo);
                Toast.makeText(UpgradeAppActivity.this, "开始后台更新", Toast.LENGTH_SHORT).show();
                onBackPressed();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
