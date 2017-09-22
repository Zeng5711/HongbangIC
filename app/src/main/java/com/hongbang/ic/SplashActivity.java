package com.hongbang.ic;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.hongbang.ic.activity.CommunityChooseActivity;
import com.hongbang.ic.activity.LoginActivity;
import com.hongbang.ic.activity.MainActivity;
import com.hongbang.ic.common.UserDefaults;
import com.hongbang.ic.model.UserInfo;
import com.hongbang.ic.util.AppUtils;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

public class SplashActivity extends AppCompatActivity {
    protected int _splashTime = 1500;

    @ViewInject(R.id.splash_view)
    ImageView mSplashView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        x.view().inject(this);

        x.task().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (AppUtils.shouldShowGuideView(SplashActivity.this)) {
                    startActivity(new Intent(SplashActivity.this, GuideActivity.class));
                } else if (UserDefaults.defaults().getToken() == null ||
                        UserDefaults.defaults().getUserInfo() == null) {
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                } else {
                    UserInfo userInfo = UserDefaults.defaults().getUserInfo();
                    if (userInfo.communityId == null) {
                        startActivity(new Intent(SplashActivity.this, CommunityChooseActivity.class));
                    } else {
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    }
                }
                SplashActivity.this.finish();
                overridePendingTransition(-1, R.anim.fade_out);
            }
        }, _splashTime);
    }

    @Override
    public void onBackPressed() {
    }
}
