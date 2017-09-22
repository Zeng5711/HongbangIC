package com.hongbang.ic.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.google.gson.Gson;
import com.hongbang.ic.R;
import com.hongbang.ic.api.response.DefaultResponse;
import com.hongbang.ic.common.UserDefaults;
import com.hongbang.ic.common.app;
import com.hongbang.ic.constant.AppConstants;
import com.hongbang.ic.constant.ResponseConstants;
import com.hongbang.ic.keycenter.WifiKeyService;
import com.hongbang.ic.util.StringUtils;
import com.hongbang.ic.util.T;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

public class WebViewActivity extends BaseActivity implements Callback.CommonCallback<String> {

    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_URL = "url";
    public static final String EXTRA_DIRECT = "direct";

    @ViewInject(R.id.web_view)
    private WebView mWebView;
    @ViewInject(R.id.loading_view)
    private View mLoadingView;
    @ViewInject(R.id.error_view)
    private View mErrorView;
    @ViewInject(R.id.content_view)
    private View mContentView;

    private String mUrl;
    private boolean isDirect;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        Bundle bundle = getIntent().getExtras();
        String title = bundle.getString(EXTRA_TITLE);
        mUrl = bundle.getString(EXTRA_URL);
        isDirect = bundle.getBoolean(EXTRA_DIRECT, false);

        setTitle(title);

        x.view().inject(this);

        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient() {
            boolean hasError = false;

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                hasError = false;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                showErrorView();
                hasError = true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (!hasError) {
                    showContentView();
                }
            }
        });

        showLoadingView();
        if (isDirect) {
            mWebView.loadUrl(mUrl);
        } else {
            app.data().invokeRequest(new RequestParams(mUrl), this);
        }
    }

    @Override
    public void onSuccess(String result) {
        try {
            DefaultResponse response = new Gson().fromJson(result, DefaultResponse.class);
            if (response.code == ResponseConstants.RESULT_TOKEN_EXPIRED) {
                T.showShort(x.app(), "登录失效，请重新登录！");

                Intent intent = new Intent(x.app(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(LoginActivity.EXTRA_MOBILE,
                        UserDefaults.defaults().getUserInfo().mobile);
                startActivity(intent);
                BaseActivity.finishAll();

                UserDefaults.defaults().setToken(null);
                UserDefaults.defaults().clear();

                sendBroadcast(new Intent(WifiKeyService.USER_INFO_CHANGED_ACTION));
            } else if (response.code != ResponseConstants.RESULT_NORMAL) {
                T.showShort(this, response.msg);
            }
            showErrorView();
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
        showContentView();
        mWebView.loadDataWithBaseURL(mUrl, result, null, "UTF-8", null);
    }

    @Override
    public void onError(Throwable ex, boolean isOnCallback) {
        T.showShort(this, "加载失败: " + ex.getMessage());
    }

    @Override
    public void onCancelled(CancelledException cex) {

    }

    @Override
    public void onFinished() {
        mLoadingView.setVisibility(View.GONE);
    }

    @Event(R.id.error_view)
    private void onErrorViewClick(View v) {
        showLoadingView();
        if (isDirect) {
            mWebView.loadUrl(mUrl);
        } else {
            app.data().invokeRequest(new RequestParams(mUrl), this);
        }
    }

    private void showErrorView() {
        mLoadingView.setVisibility(View.GONE);
        mErrorView.setVisibility(View.VISIBLE);
        mContentView.setVisibility(View.GONE);
    }

    private void showLoadingView() {
        mLoadingView.setVisibility(View.VISIBLE);
        mErrorView.setVisibility(View.GONE);
        mContentView.setVisibility(View.GONE);
    }

    private void showContentView() {
        mLoadingView.setVisibility(View.GONE);
        mErrorView.setVisibility(View.GONE);
        mContentView.setVisibility(View.VISIBLE);
    }
}
