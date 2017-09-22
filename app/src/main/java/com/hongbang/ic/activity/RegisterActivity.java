package com.hongbang.ic.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hongbang.ic.R;
import com.hongbang.ic.api.request.GetCaptchaParams;
import com.hongbang.ic.api.request.LoginParams;
import com.hongbang.ic.api.request.RegisterParams;
import com.hongbang.ic.api.response.BaseResponse;
import com.hongbang.ic.api.response.DefaultResponse;
import com.hongbang.ic.common.UserDefaults;
import com.hongbang.ic.common.app;
import com.hongbang.ic.constant.AppConstants;
import com.hongbang.ic.model.LoginResponse;
import com.hongbang.ic.model.UserInfo;
import com.hongbang.ic.util.AppUtils;
import com.hongbang.ic.util.StringUtils;
import com.hongbang.ic.util.T;

import org.xutils.common.Callback;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

public class RegisterActivity extends BaseActivity {

    @ViewInject(value = R.id.edit_register_phone)
    private EditText mEditPhone;

    @ViewInject(value = R.id.edit_register_pwd)
    private EditText mEditPassword;

    @ViewInject(value = R.id.edit_register_pwd_confirm)
    private EditText mEditConfirmPwd;

    @ViewInject(value = R.id.edit_captcha)
    private EditText mEditCaptcha;

    @ViewInject(value = R.id.btn_get_captcha)
    private Button mGetCaptchaButton;

    @ViewInject(value = R.id.btn_register)
    private Button mRegisterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setTitle(R.string.register_title_text);
        x.view().inject(this);
    }

    private final static int HANDLER_REFRESH_BUTTON = 0;

    @Override
    protected void handleMessage(Message msg) {
        switch (msg.what) {
            case HANDLER_REFRESH_BUTTON:
                if (msg.arg1 <= 0) {
                    mGetCaptchaButton.setEnabled(true);
                    mGetCaptchaButton.setText(R.string.captcha_btn_text);
                } else {
                    mGetCaptchaButton.setEnabled(false);
                    mGetCaptchaButton.setText(getString(R.string.captcha_btn_text) + "\n(" + msg.arg1 + ")");
                    Message message = mHandler.obtainMessage(HANDLER_REFRESH_BUTTON);
                    message.arg1 = msg.arg1 - 1;
                    mHandler.sendMessageDelayed(message, 1000);
                }
                break;
        }
    }

    @Event(value = {R.id.btn_get_captcha})
    private void getCaptcha(View view) {
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
        mGetCaptchaButton.setEnabled(false);
        showLoadingDialog();
        app.data().invokeRequest(new GetCaptchaParams(phone, AppConstants.CAPTCHA_USER_REGISTER), new Callback.CommonCallback<DefaultResponse>() {
            @Override
            public void onSuccess(DefaultResponse result) {
                if (result.code == 0) {
                    Message message = mHandler.obtainMessage(HANDLER_REFRESH_BUTTON);
                    message.arg1 = AppConstants.CAPTCHA_GET_INTERVAL;
                    mHandler.sendMessageDelayed(message, 1000);
                } else {
                    mGetCaptchaButton.setEnabled(true);
                    T.showShort(RegisterActivity.this, result.msg == null ? "获取验证码失败" : result.msg);
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                T.showShort(RegisterActivity.this, ex.getMessage());
                mGetCaptchaButton.setEnabled(true);
            }

            @Override
            public void onCancelled(CancelledException cex) {
                mGetCaptchaButton.setEnabled(true);
            }

            @Override
            public void onFinished() {
                dismissLoadingDialog();
            }
        });
    }

    @Event(value = {R.id.btn_register})
    private void doRegister(View view) {
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

        String confirm = mEditConfirmPwd.getText().toString();
        if (confirm.length() == 0) {
            T.show(this, "请再次输入密码", T.LENGTH_SHORT);
            mEditConfirmPwd.requestFocus();
            return;
        } else if (!confirm.equals(password)) {
            T.show(this, "两次输入的密码不一致", T.LENGTH_SHORT);
            mEditConfirmPwd.requestFocus();
            return;
        }

        String captcha = mEditCaptcha.getText().toString();
        if (captcha.length() == 0) {
            T.show(this, "请输入短信获取的验证码", T.LENGTH_SHORT);
            mEditCaptcha.requestFocus();
            return;
        } else if (!StringUtils.isCaptcha(captcha)) {
            T.show(this, "验证码必须为6位的数字", T.LENGTH_SHORT);
            mEditCaptcha.requestFocus();
            return;
        }

        mRegisterButton.setEnabled(false);
        final ProgressDialog dialog = ProgressDialog.show(this, null, null, false, false);
        RegisterParams params = new RegisterParams();
        params.mobile = phone;
        params.pwd = StringUtils.MD5(password);
        params.mobileId = AppUtils.getDeviceId(this);
        params.captcha = captcha;

        app.data().invokeRequest(params, new Callback.CommonCallback<DefaultResponse>() {
            @Override
            public void onSuccess(DefaultResponse result) {
                if (result.code == 0) {
                    onRegisterSuccess();
                } else {
                    T.showShort(RegisterActivity.this, result.msg == null ? "注册失败" : result.msg);
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ex.printStackTrace();
                T.showShort(RegisterActivity.this, ex.getMessage());
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {
                dialog.dismiss();
                mRegisterButton.setEnabled(true);
            }
        });
    }

    private void onRegisterSuccess() {
        final AlertDialog dialog = new AlertDialog.Builder(this, R.style.register_success)
                .setCancelable(false)
                .create();
        dialog.show();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                doLogin(dialog);
            }
        }, 1000);
    }

    private void doLogin(final AlertDialog dialog) {
        String mobile = mEditPhone.getText().toString().trim();
        String password = StringUtils.MD5(mEditPassword.getText().toString());

        LoginParams params = new LoginParams();
        params.account = mobile;
        params.password = password;
        params.mobileId = AppUtils.getDeviceId(this);

        app.data().invokeRequest(params, new Callback.CommonCallback<BaseResponse<LoginResponse>>() {
            @Override
            public void onSuccess(BaseResponse<LoginResponse> result) {
                if (result.code == 0 && result.getData().token != null) {
                    LoginResponse response = result.getData();
                    UserInfo userInfo = new UserInfo();
                    userInfo.mobile = response.mobile;
                    userInfo.role = response.role;
                    userInfo.headPortrait = response.headPortrait;
                    userInfo.nickname = response.nickname;
                    UserDefaults.defaults().setUserInfo(userInfo);
                    UserDefaults.defaults().setToken(response.token);
                    startActivity(new Intent(RegisterActivity.this, CommunityChooseActivity.class));
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, result.msg == null ? "登录失败!" : result.msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                T.showShort(RegisterActivity.this, "登录失败");
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {
                dialog.dismiss();
            }
        });
    }


}
