package com.hongbang.ic.common;

import com.hongbang.ic.util.Logger;

import org.xutils.http.RequestParams;
import org.xutils.http.app.RequestTracker;
import org.xutils.http.request.UriRequest;

/**
 * 日志输出
 * <p>
 * Created by xionghf on 16/5/23.
 */
public class DefaultRequestTracker implements RequestTracker {
    private static final String TAG = DefaultRequestTracker.class.getName();

    @Override
    public void onWaiting(RequestParams params) {
        Logger.info(TAG, "function=onWaiting\nrequest=" + params.toString());
    }

    @Override
    public void onStart(RequestParams params) {
        Logger.info(TAG, "function=onStart\nrequest=" + params.toString());
    }

    @Override
    public void onRequestCreated(UriRequest request) {
        Logger.info(TAG, "function=onSuccess\nrequest=" + (request != null ? request.toString() : "null"));
    }

    @Override
    public void onCache(UriRequest request, Object result) {
        Logger.debug(TAG, "function=onCache\nrequest=" + (request != null ? request.toString() : "null")
                + "\nresult=" + result);
    }

    @Override
    public void onSuccess(UriRequest request, Object result) {
        Logger.debug(TAG, "function=onSuccess\nrequest=" + (request != null ? request.toString() : "null")
                + "\nresult=" + result);
    }

    @Override
    public void onCancelled(UriRequest request) {
        Logger.debug(TAG, "function=onCancelled\nrequest=" + (request != null ? request.toString() : "null"));
    }

    @Override
    public void onError(UriRequest request, Throwable ex, boolean isCallbackError) {
        if (ex == null) {
            ex = new Exception("");
        }
        Logger.error(TAG, "function=onError\nrequest=" + (request != null ? request.toString() : "null")
                + "\nerror=" + ex.toString()
                + "\ncallback=" + isCallbackError);
        Logger.exception(TAG, ex);
    }

    @Override
    public void onFinished(UriRequest request) {
        Logger.info(TAG, "function=onFinished\nrequest=" + (request != null ? request.toString() : "null"));
    }
}
