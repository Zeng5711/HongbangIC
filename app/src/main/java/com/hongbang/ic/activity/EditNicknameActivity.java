package com.hongbang.ic.activity;

import android.os.Bundle;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.hongbang.ic.R;
import com.hongbang.ic.api.response.BaseResponse;
import com.hongbang.ic.common.UserDefaults;
import com.hongbang.ic.common.app;
import com.hongbang.ic.model.UserInfo;
import com.hongbang.ic.util.T;

import org.xutils.common.Callback;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

public class EditNicknameActivity extends BaseActivity implements View.OnClickListener {

    @ViewInject(R.id.edit_nickname)
    private EditText mEditNickNameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_nickname);

        setTitle(R.string.title_edit_nickname);

        x.view().inject(this);

        setResult(RESULT_CANCELED);

        setCustomTitleButton(getString(R.string.confirm), this);

        String nickname = UserDefaults.defaults().getUserInfo().nickname;
        if (nickname != null) {
            mEditNickNameView.setText(nickname);
            mEditNickNameView.setSelection(nickname.length());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        String nickname = mEditNickNameView.getText().toString().trim();
        mEditNickNameView.setText(nickname);
        if (nickname.length() == 0) {
            T.showShort(getBaseContext(), "昵称不能为空");
            return;
        }

        if (null != this.getCurrentFocus()) {
            InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            mInputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }

        showLoadingDialog(R.string.processing);
        app.data().setUserInfo(null, nickname, new Callback.CommonCallback<BaseResponse<UserInfo>>() {
            @Override
            public void onSuccess(BaseResponse<UserInfo> result) {
                if (result.code == 0) {
                    T.showShort(getBaseContext(), "修改成功");
                    UserInfo data = result.getData();
                    UserInfo userInfo = UserDefaults.defaults().getUserInfo();
                    userInfo.nickname = data.nickname;
                    UserDefaults.defaults().saveUserInfo();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    T.showShort(getBaseContext(), "修改成功");
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                T.showShort(getBaseContext(), ex.getMessage());
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
}
