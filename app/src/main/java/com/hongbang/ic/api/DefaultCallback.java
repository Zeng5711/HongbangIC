package com.hongbang.ic.api;

import android.content.Intent;

import com.hongbang.ic.activity.BaseActivity;
import com.hongbang.ic.activity.LoginActivity;
import com.hongbang.ic.api.response.AbstractResponse;
import com.hongbang.ic.common.UserDefaults;
import com.hongbang.ic.constant.ResponseConstants;
import com.hongbang.ic.keycenter.WifiKeyService;

import org.xutils.common.Callback;
import org.xutils.x;

import java.lang.reflect.Type;

/**
 * 用来处理token过期等共同的服务器返回code
 * <p>
 * Created by xionghf on 16/5/12.
 */
public class DefaultCallback<T> implements Callback.TypedCallback<T> {

    private CommonCallback<T> callback;

    private final Type resultType;

    public DefaultCallback(CommonCallback<T> callback, Type resultType) {
        this.callback = callback;
        this.resultType = resultType;
    }

    @Override
    public Type getLoadType() {
        return resultType;
    }

    @Override
    public void onSuccess(T result) {
        if (result == null || result instanceof String) {
            callback.onSuccess(result);
        } else if (!(result instanceof AbstractResponse)) {
            callback.onError(new Exception("服务器返回的数据格式不正确"), false);
        } else {
            AbstractResponse response = (AbstractResponse) result;
            if (response.code == ResponseConstants.RESULT_TOKEN_EXPIRED) {
                Intent intent = new Intent(x.app(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(LoginActivity.EXTRA_MOBILE,
                        UserDefaults.defaults().getUserInfo().mobile);
                x.app().startActivity(intent);
                BaseActivity.finishAll();

                x.task().post(new Runnable() {
                    @Override
                    public void run() {
                        com.hongbang.ic.util.T.showShort(x.app(), "登录失效，请重新登录！");
                    }
                });

                callback.onCancelled(null);
                callback.onFinished();

                UserDefaults.defaults().setToken(null);
                UserDefaults.defaults().clear();

                x.app().sendBroadcast(new Intent(WifiKeyService.USER_INFO_CHANGED_ACTION));
            } else {
                callback.onSuccess(result);
            }
        }
    }

    @Override
    public void onError(Throwable ex, boolean isOnCallback) {
        callback.onError(ex, isOnCallback);
    }

    @Override
    public void onCancelled(CancelledException cex) {
        callback.onCancelled(cex);
    }

    @Override
    public void onFinished() {
        callback.onFinished();
    }

}
