package com.hongbang.ic.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.hongbang.ic.R;
import com.hongbang.ic.api.request.LoginParams;
import com.hongbang.ic.api.response.BaseResponse;
import com.hongbang.ic.common.UserDefaults;
import com.hongbang.ic.common.app;
import com.hongbang.ic.keycenter.WifiKeyService;
import com.hongbang.ic.model.LoginResponse;
import com.hongbang.ic.model.UserInfo;
import com.hongbang.ic.util.AppUtils;
import com.hongbang.ic.util.StringUtils;
import com.hongbang.ic.util.T;

import org.xutils.common.Callback;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity {

    public static final String EXTRA_MOBILE = "mobile";

    @ViewInject(R.id.edit_login_phone)
    private EditText mEditPhone;

    @ViewInject(R.id.edit_login_pwd)
    private EditText mEditPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        x.view().inject(this);

        String mobile = getIntent().getStringExtra(EXTRA_MOBILE);
        if (mobile != null) {
            mEditPhone.setText(mobile);
        }
    }

    @Event(value = {R.id.btn_login}, type = View.OnClickListener.class)
    private void doLogin(View view) {
        String phone = mEditPhone.getText().toString().trim();
        if (phone.length() == 0) {
            T.show(this, "请输入手机号", T.LENGTH_SHORT);
            mEditPhone.requestFocus();
            return;
        } else if (!StringUtils.isPhone(phone)) {
            T.show(this, "手机号码格式不正确", T.LENGTH_SHORT);
            mEditPhone.requestFocus();
            return;
        }

        String password = mEditPassword.getText().toString();
        if (password.length() == 0) {
            T.show(this, "请输入密码", T.LENGTH_SHORT);
            mEditPassword.requestFocus();
            return;
        } else if (password.length() < 6) {
            T.show(this, "密码过短，请输入6-16位的密码", T.LENGTH_SHORT);
            mEditPassword.requestFocus();
            return;
        }

        LoginParams params = new LoginParams();
        params.account = phone;
        params.password = StringUtils.MD5(password);
        params.mobileId = AppUtils.getDeviceId(this);

        showLoadingDialog("登录中...");
        app.data().invokeRequest(params, new Callback.CommonCallback<BaseResponse<LoginResponse>>() {
            @Override
            public void onSuccess(BaseResponse<LoginResponse> result) {
                if (result.code == 0) {
                    LoginResponse response = result.getData();
                    UserInfo userInfo = new UserInfo();
                    userInfo.communityId = response.communityId;
                    userInfo.mobile = response.mobile;
                    userInfo.role = response.role;
                    userInfo.headPortrait = response.headPortrait;
                    userInfo.nickname = response.nickname;
                    UserDefaults.defaults().setUserInfo(userInfo);
                    UserDefaults.defaults().setToken(response.token);
                    sendBroadcast(new Intent(WifiKeyService.USER_INFO_CHANGED_ACTION));

                    if (response.communityId == null) {
                        startActivity(new Intent(LoginActivity.this, CommunityChooseActivity.class));
                    } else {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    }
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this,
                            "登录失败" + (result.msg == null ? "" : ": " + result.msg),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                T.showShort(LoginActivity.this, "登录失败:" + ex.getMessage());
                ex.printStackTrace();
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

    @Event(value = {R.id.btn_login_sign_up}, type = View.OnClickListener.class)
    private void startSignUp(View view) {
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
    }

    @Event(value = {R.id.btn_forget}, type = View.OnClickListener.class)
    private void startRetrievePassword(View view) {
        startActivity(new Intent(LoginActivity.this, RetrievePasswordActivity.class));
    }

    private MaterialDialog mLoadingDialog;

    public void showLoadingDialog(String msg) {
        if (mLoadingDialog == null) {
            mLoadingDialog = new MaterialDialog.Builder(this)
                    .content(msg)
                    .progress(true, 0)
                    .build();
            mLoadingDialog.setCancelable(false);
        }
        if (!mLoadingDialog.isShowing()) {
            mLoadingDialog.show();
        }
    }

    public void dismissLoadingDialog() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
        mLoadingDialog = null;
    }
}

