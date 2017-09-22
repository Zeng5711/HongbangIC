package com.hongbang.ic.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.hongbang.ic.R;
import com.hongbang.ic.api.response.DefaultResponse;
import com.hongbang.ic.common.UserDefaults;
import com.hongbang.ic.common.app;
import com.hongbang.ic.model.OneKeyInfo4DB;
import com.hongbang.ic.util.StringUtils;
import com.hongbang.ic.util.T;

import org.xutils.common.Callback;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

public class ShareKeyActivity extends BaseActivity {

    public static final String EXTRA_SHARE_KEY = "share_key";

    @ViewInject(R.id.edit_phone)
    private EditText mEditPhone;

    private OneKeyInfo4DB mShareKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_key);

        try {
            mShareKey = (OneKeyInfo4DB) getIntent().getSerializableExtra(EXTRA_SHARE_KEY);
        } catch (Exception e) {
            finish();
            return;
        }
        if (mShareKey == null) {
            finish();
            return;
        }

        x.view().inject(this);
    }

    @Event(R.id.btn_share)
    private void onShareClick(View v) {
        String phone = mEditPhone.getText().toString().trim();
        if (phone.length() == 0) {
            T.show(this, "请输入手机号", T.LENGTH_SHORT);
            mEditPhone.requestFocus();
            return;
        } else if (!StringUtils.isPhone(phone)) {
            T.show(this, "手机号码格式不正确", T.LENGTH_SHORT);
            mEditPhone.requestFocus();
            return;
        } else if (phone.equals(UserDefaults.defaults().getUserInfo().mobile)) {
            T.show(this, "不能分享钥匙给自己", T.LENGTH_SHORT);
            mEditPhone.requestFocus();
            return;
        }
        hideSoftInputFromWindow();

        showLoadingDialog(R.string.processing);
        app.data().shareKey(phone, mShareKey,
                new Callback.CommonCallback<DefaultResponse>() {
                    @Override
                    public void onSuccess(DefaultResponse result) {
                        if (result.code == 0) {
                            T.showShort(getBaseContext(), "分享成功");
                        } else {
                            T.showShort(getBaseContext(), "分享失败: " + result.msg);
                        }
                    }

                    @Override
                    public void onError(Throwable ex, boolean isOnCallback) {
                        T.showShort(getBaseContext(), "分享失败: " + ex.getMessage());
                    }

                    @Override
                    public void onCancelled(CancelledException cex) {

                    }

                    @Override
                    public void onFinished() {
                        dismissLoadingDialog();
                    }
                });
    }
}
