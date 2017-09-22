package com.hongbang.ic.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hongbang.ic.R;
import com.hongbang.ic.api.SelectCallbcak;

import org.xutils.common.Callback;

import java.util.ArrayList;

public abstract class BaseActivity extends AppCompatActivity {

    private TextView mTitleLabel;

    private static ArrayList<BaseActivity> mActivityStack = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityStack.add(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mActivityStack.contains(this)) {
            mActivityStack.remove(this);
        }
    }

    @Override
    protected void onStop() {
        dismissLoadingDialog();
        super.onStop();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (null != this.getCurrentFocus()) {
            /**
             * 点击空白位置 隐藏软键盘
             */
            InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            return mInputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
        return super.onTouchEvent(event);
    }

    protected void hideSoftInputFromWindow() {
        if (null != this.getCurrentFocus()) {
            InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            mInputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
    }

    public static void finishAll() {
        for (Activity activity : mActivityStack) {
            try {
                activity.finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setContentView(int layout) {
        this.setContentView(layout, R.layout.common_title_bar);
    }

    public void setContentView(int layout, int titleResId) {
        this.setContentView(layout, titleResId, true);
    }

    public void setContentView(int layout, boolean showBackBtn) {
        this.setContentView(layout, R.layout.common_title_bar, showBackBtn);
    }

    public void setContentView(int layout, int titleResId, boolean showBackBtn) {
        super.setContentView(layout);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Color.TRANSPARENT);
        }
        this.addTitleView(titleResId, showBackBtn);
    }

    private void addTitleView(int layout, boolean showBackBtn) {
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                layout);
        mTitleLabel = (TextView) findViewById(R.id.text_title_label);

        if (showBackBtn) {
            findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BaseActivity.this.onTitleBackClick();
                }
            });
        } else {
            findViewById(R.id.btn_back).setVisibility(View.GONE);
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        this.setTitleText(title);
    }

    @Override
    public void setTitle(int titleId) {
        super.setTitle(titleId);
        this.setTitleText(titleId);
    }

    protected void setTitleText(int resId) {
        if (mTitleLabel == null) {
            return;
        }
        mTitleLabel.setText(resId);
    }

    protected void setTitleText(CharSequence title) {
        if (mTitleLabel == null) {
            return;
        }
        mTitleLabel.setText(title);
    }

    protected void setCustomTitleButton(String text, View.OnClickListener onClickListener) {
        this.setCustomTitleButton(text, 0, onClickListener);
    }

    protected void setCustomTitleButton(String text, int resId, View.OnClickListener onClickListener) {
        if (text == null || text.length() == 0) {
            throw new IllegalArgumentException("text of custom button on title bar can't be empty");
        }
        Button button = (Button) findViewById(R.id.btn_title_custom);
        button.setText(text);
        button.setVisibility(View.VISIBLE);
        if (resId > 0) {
            Drawable drawable = getResources().getDrawable(resId);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            button.setCompoundDrawables(drawable, null, null, null);
        }
        if (onClickListener != null) {
            button.setOnClickListener(onClickListener);
        }
    }

    protected void setCustomImageButton(int resId, View.OnClickListener onClickListener) {
        if (resId > 0) {
            Button button = (Button) findViewById(R.id.btn_title_custom);
            button.setBackgroundResource(resId);
            button.setVisibility(View.VISIBLE);
            if (onClickListener != null) {
                button.setOnClickListener(onClickListener);
            }
        }
    }

    protected void hideCustomTitleButton() {
        Button button = (Button) findViewById(R.id.btn_title_custom);
        button.setVisibility(View.GONE);
        button = (Button) findViewById(R.id.btn_title_custom2);
        button.setVisibility(View.GONE);
    }

    protected void onTitleBackClick() {
        this.finish();
    }

    class OwnHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            BaseActivity.this.handleMessage(msg);
        }
    }

    protected OwnHandler mHandler = new OwnHandler();

    protected void handleMessage(Message msg) {

    }


    private MaterialDialog mLoadingDialog;

    private ArrayList<Callback.Cancelable> mRequestStack = new ArrayList<>();

    public void showLoadingDialog() {
        showLoadingDialog(R.string.loading);
    }

    public void showLoadingDialog(int resId) {
        showLoadingDialog(getString(resId));
    }

    public void showLoadingDialog(String msg) {
        if (mLoadingDialog == null) {
            mLoadingDialog = new MaterialDialog.Builder(this)
                    .content(msg)
                    .progress(true, 0)
                    .build();
            mLoadingDialog.setCancelable(false);
        } else {
            mLoadingDialog.setContent(msg);
        }
        if (!mLoadingDialog.isShowing()) {
            mLoadingDialog.show();
        }
    }

    public void showLoadingDialog(String msg, DialogInterface.OnCancelListener cancelListener) {
        if (mLoadingDialog == null) {
            mLoadingDialog = new MaterialDialog.Builder(this)
                    .content(msg)
                    .progress(true, 0)
                    .build();
            mLoadingDialog.setCancelable(true);
        } else {
            mLoadingDialog.setContent(msg);
        }
        mLoadingDialog.setOnCancelListener(cancelListener);
        if (!mLoadingDialog.isShowing()) {
            mLoadingDialog.show();
        }
    }

    public void showLoadingDialog(String msg, Callback.Cancelable cancelableRequest) {
        if (mLoadingDialog == null) {
            mLoadingDialog = new MaterialDialog.Builder(this)
                    .content(msg)
                    .progress(true, 0)
                    .build();
            mLoadingDialog.setCancelable(true);
        } else {
            mLoadingDialog.setContent(msg);
        }
        mLoadingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                if (mRequestStack != null) {
                    for (Callback.Cancelable cancelable : mRequestStack) {
                        cancelable.cancel();
                    }
                }
            }
        });
        if (!mRequestStack.contains(cancelableRequest)) {
            mRequestStack.add(cancelableRequest);
        }
        if (!mLoadingDialog.isShowing()) {
            mLoadingDialog.show();
        }
    }

    public void dismissLoadingDialog() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
            mRequestStack.clear();
        }
        mLoadingDialog = null;
    }

    public void showSelectDialog(String title,String wifiList[], final SelectCallbcak callbcak){
        if (mLoadingDialog == null) {
            mLoadingDialog = new MaterialDialog.Builder(this)
                    .title(title)
                    .items(wifiList)
                    .positiveText("取消")
                    .onAny(new MaterialDialog.SingleButtonCallback() {

                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            if (which == DialogAction.POSITIVE) {
                                callbcak.onPositive();
                            }
                        }
                    })
                    .itemsCallback(new MaterialDialog.ListCallback(){

                        @Override
                        public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                            callbcak.onSelection(position,text);
                        }
                    }).build();
            mLoadingDialog.setCancelable(false);
        }

        if (!mLoadingDialog.isShowing()) {
            mLoadingDialog.show();
        }
    }
}
