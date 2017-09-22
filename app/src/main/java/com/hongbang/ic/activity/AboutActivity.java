package com.hongbang.ic.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.hongbang.ic.R;
import com.hongbang.ic.constant.AppConstants;
import com.hongbang.ic.util.AppUtils;
import com.hongbang.ic.util.T;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.crashreport.CrashReport;

import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

public class AboutActivity extends BaseActivity {

    @ViewInject(R.id.debug_area)
    private View debugView;
    @ViewInject(R.id.app_name_version)
    private TextView version;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        setTitle("关于");

        x.view().inject(this);

        version.setText(String.format("%s %s", getString(R.string.app_name), AppUtils.getVersionName(this)));

        if (x.isDebug()) {
            debugView.setVisibility(View.VISIBLE);
        }
    }

    @Event(R.id.btn_score)
    private void scoreApp(View view) {
        T.showShort(this, "暂不支持应用评分！");
//        try {
//            Uri uri = Uri.parse("market://details?id=" + getPackageName());
//            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intent);
//        } catch (Exception e) {
//            T.showShort(this, "启动应用商店失败！");
//        }
    }

    @Event(R.id.btn_check_update)
    private void checkUpdate(View view) {
        Beta.checkUpgrade();
    }

    @Event(R.id.btn_debug)
    private void startDebugActivity(View view) {
        startActivity(new Intent(this, DebugActivity.class));
    }

    @Event(R.id.btn_manual)
    private void showManual(View view) {
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra(WebViewActivity.EXTRA_TITLE, "使用手册");
        intent.putExtra(WebViewActivity.EXTRA_URL, AppConstants.MANUAL_URL);
        intent.putExtra(WebViewActivity.EXTRA_DIRECT, true);
        startActivity(intent);
    }
}
