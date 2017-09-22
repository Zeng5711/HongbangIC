package com.hongbang.ic.api;

import org.xutils.HttpManager;
import org.xutils.common.Callback;
import org.xutils.common.util.ParameterizedTypeUtil;
import org.xutils.http.HttpMethod;
import org.xutils.http.HttpTask;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.lang.reflect.Type;

/**
 * Created by xionghf on 16/5/12.
 */
public class CustomHttpManager implements HttpManager {


    private static final Object lock = new Object();
    private static CustomHttpManager instance;

    private CustomHttpManager() {
    }

    public static void registerInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new CustomHttpManager();
                }
            }
        }
        x.Ext.setHttpManager(instance);
    }

    @Override
    public <T> Callback.Cancelable get(RequestParams entity, Callback.CommonCallback<T> callback) {
        return request(HttpMethod.GET, entity, callback);
    }

    @Override
    public <T> Callback.Cancelable post(RequestParams entity, Callback.CommonCallback<T> callback) {
        return request(HttpMethod.POST, entity, callback);
    }

    @Override
    public <T> Callback.Cancelable request(HttpMethod method, RequestParams entity, Callback.CommonCallback<T> callback) {
        entity.setMethod(method);
        Callback.Cancelable cancelable = null;
        if (callback instanceof Callback.Cancelable) {
            cancelable = (Callback.Cancelable) callback;
        }
        Type type = ParameterizedTypeUtil.getParameterizedType(callback.getClass(),
                Callback.CommonCallback.class, 0);
        HttpTask<T> task;
        if (callback instanceof Callback.CacheCallback) {
            task = new HttpTask<>(entity, cancelable, callback);
        } else {
            DefaultCallback<T> redirect = new DefaultCallback<>(callback, type);
            task = new HttpTask<>(entity, cancelable, redirect);
        }
        return x.task().start(task);
    }

    @Override
    public <T> T getSync(RequestParams entity, Class<T> resultType) throws Throwable {
        return requestSync(HttpMethod.GET, entity, resultType);
    }

    @Override
    public <T> T postSync(RequestParams entity, Class<T> resultType) throws Throwable {
        return requestSync(HttpMethod.POST, entity, resultType);
    }

    @Override
    public <T> T requestSync(HttpMethod method, RequestParams entity, Class<T> resultType) throws Throwable {
        DefaultSyncCallback<T> callback = new DefaultSyncCallback<>(resultType);
        return requestSync(method, entity, callback);
    }

    @Override
    public <T> T requestSync(HttpMethod method, RequestParams entity, Callback.TypedCallback<T> callback) throws Throwable {
        entity.setMethod(method);
        HttpTask<T> task = new HttpTask<>(entity, null, callback);
        return x.task().startSync(task);
    }

    private class DefaultSyncCallback<T> implements Callback.TypedCallback<T> {

        private final Class<T> resultType;

        public DefaultSyncCallback(Class<T> resultType) {
            this.resultType = resultType;
        }

        @Override
        public Type getLoadType() {
            return resultType;
        }

        @Override
        public void onSuccess(T result) {

        }

        @Override
        public void onError(Throwable ex, boolean isOnCallback) {

        }

        @Override
        public void onCancelled(CancelledException cex) {

        }

        @Override
        public void onFinished() {

        }
    }

}
