package com.hongbang.ic.activity;

import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.hongbang.ic.R;
import com.hongbang.ic.api.response.DefaultResponse;
import com.hongbang.ic.common.UserDefaults;
import com.hongbang.ic.common.app;
import com.hongbang.ic.constant.AppConstants;
import com.hongbang.ic.constant.HttpConstants;
import com.hongbang.ic.util.StringUtils;
import com.hongbang.ic.util.T;

import org.xutils.common.Callback;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

public class ChangePasswordActivity extends BaseActivity {

    @ViewInject(value = R.id.edit_old_pwd)
    private EditText mEditOldPassword;

    @ViewInject(value = R.id.edit_new_pwd)
    private EditText mEditNewPassword;

    @ViewInject(value = R.id.edit_new_pwd_again)
    private EditText mEditConfirmPwd;

    @ViewInject(value = R.id.edit_captcha)
    private EditText mEditCaptcha;

    @ViewInject(value = R.id.btn_get_captcha)
    private Button mGetCaptchaButton;

    @ViewInject(value = R.id.btn_commit)
    private Button mCommitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        x.view().inject(this);

        setTitle(R.string.title_change_pwd);
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
        mGetCaptchaButton.setEnabled(false);
        showLoadingDialog();
        app.data().getCaptcha(UserDefaults.defaults().getUserInfo().mobile,
                AppConstants.CAPTCHA_CHANGE_PASSWORD,
                new Callback.CommonCallback<DefaultResponse>() {
                    @Override
                    public void onSuccess(DefaultResponse result) {
                        if (result.code == 0) {
                            Message message = mHandler.obtainMessage(HANDLER_REFRESH_BUTTON);
                            message.arg1 = AppConstants.CAPTCHA_GET_INTERVAL;
                            mHandler.sendMessageDelayed(message, 1000);
                        } else {
                            mGetCaptchaButton.setEnabled(true);
                            T.showShort(getBaseContext(), result.msg == null ? "获取验证码失败" : result.msg);
                        }
                    }

                    @Override
                    public void onError(Throwable ex, boolean isOnCallback) {
                        T.showShort(getBaseContext(), ex.getMessage());
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

    @Event(value = {R.id.btn_commit})
    private void doCommit(View view) {
        String oldPassword = mEditOldPassword.getText().toString().trim();
        if (oldPassword.length() == 0) {
            T.show(this, "请输入原密码", T.LENGTH_SHORT);
            mEditOldPassword.requestFocus();
            return;
        } else if (oldPassword.length() < 6) {
            T.show(this, "密码过短，请输入6-15位的密码", T.LENGTH_SHORT);
            mEditOldPassword.requestFocus();
            return;
        }

        String newPassword = mEditNewPassword.getText().toString();
        if (newPassword.length() == 0) {
            T.show(this, "请输入密码", T.LENGTH_SHORT);
            mEditNewPassword.requestFocus();
            return;
        } else if (newPassword.length() < 6) {
            T.show(this, "密码过短，请输入6-15位的密码", T.LENGTH_SHORT);
            mEditNewPassword.requestFocus();
            return;
        }

        String confirm = mEditConfirmPwd.getText().toString();
        if (confirm.length() == 0) {
            T.show(this, "请再次输入密码", T.LENGTH_SHORT);
            mEditConfirmPwd.requestFocus();
            return;
        } else if (!confirm.equals(newPassword)) {
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

        mCommitButton.setEnabled(false);

        showLoadingDialog();
        app.data().changePassword(oldPassword, newPassword, captcha,
                new Callback.CommonCallback<DefaultResponse>() {
                    @Override
                    public void onSuccess(DefaultResponse result) {
                        if (result.code == 0) {
                            T.showShort(getBaseContext(), "密码修改成功");
                            finish();
                        } else {
                            T.showShort(getBaseContext(), result.msg == null ? "密码修改失败" : result.msg);
                        }
                    }

                    @Override
                    public void onError(Throwable ex, boolean isOnCallback) {
                        ex.printStackTrace();
                        T.showShort(getBaseContext(), ex.getMessage());
                    }

                    @Override
                    public void onCancelled(CancelledException cex) {
                    }

                    @Override
                    public void onFinished() {
                        mCommitButton.setEnabled(true);
                        dismissLoadingDialog();
                    }
                });
    }

}
