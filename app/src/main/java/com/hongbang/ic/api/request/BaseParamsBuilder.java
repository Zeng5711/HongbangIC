package com.hongbang.ic.api.request;

import com.hongbang.ic.api.annotation.Parameter;
import com.hongbang.ic.constant.HttpConstants;

import org.xutils.common.util.KeyValue;
import org.xutils.common.util.LogUtil;
import org.xutils.http.RequestParams;
import org.xutils.http.annotation.HttpRequest;
import org.xutils.http.app.ParamsBuilder;
import org.xutils.http.body.BodyItemWrapper;
import org.xutils.x;

import java.lang.reflect.Field;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * 默认参数构造器
 * <p>
 * Created by xionghf on 16/3/27.
 */
public class BaseParamsBuilder implements ParamsBuilder {


    /**
     * 根据@HttpRequest构建请求的url
     *
     * @param params
     * @param httpRequest
     * @return
     */
    @Override
    public String buildUri(RequestParams params, HttpRequest httpRequest) {
        String host;
        if (x.isDebug()) {
            host = HttpConstants.SERVER_HOST_DEBUG;
        } else {
            host = HttpConstants.SERVER_HOST;
        }
        return host + "/" + httpRequest.path();
    }

    /**
     * 根据注解的cacheKeys构建缓存的自定义key,
     * 如果返回null, 默认使用 url 和整个 query string 组成.
     *
     * @param params
     * @param cacheKeys
     * @return
     */
    @Override
    public String buildCacheKey(RequestParams params, String[] cacheKeys) {
        String cacheKey = null;
        if (cacheKeys != null && cacheKeys.length > 0) {

            cacheKey = params.getUri() + "?";

            // 添加cacheKeys对应的参数
            for (String key : cacheKeys) {
                String value = params.getStringParameter(key);
                if (value != null) {
                    cacheKey += key + "=" + value + "&";
                }
            }
        }
        return cacheKey;
    }

    /**
     * 自定义SSLSocketFactory
     *
     * @return
     */
    @Override
    public SSLSocketFactory getSSLSocketFactory() {
        return getTrustAllSSLSocketFactory();
    }

    /**
     * 为请求添加通用参数等操作
     *
     * @param params
     */
    @Override
    public void buildParams(RequestParams params) {
        checkParams(params);
    }

    private void checkParams(RequestParams params) {
        Field[] fields = params.getClass().getDeclaredFields();
        if (fields == null) {
            return;
        }
        for (Field field : fields) {
            Parameter annotation = field.getAnnotation(Parameter.class);
            if (annotation != null && annotation.value() != null) {
                String rename = annotation.value();
                renameParams(params, field, rename);
            }
        }
    }

    private void renameParams(RequestParams params, Field field, String rename) {
        List<KeyValue> list = params.getBodyParams();
        KeyValue bodyParam = null, fileParam = null, queryParam = null;
        if (list != null) {
            for (KeyValue kv : list) {
                if (field.getName().equals(kv.key)) {
                    bodyParam = kv;
                    break;
                }
            }
        }

        list = params.getFileParams();
        if (list != null) {
            for (KeyValue kv : list) {
                if (field.getName().equals(kv.key)) {
                    fileParam = kv;
                    break;
                }
            }
        }

        list = params.getQueryStringParams();
        if (list != null) {
            for (KeyValue kv : list) {
                if (field.getName().equals(kv.key)) {
                    queryParam = kv;
                    break;
                }
            }
        }

        if (bodyParam != null || queryParam != null) {
            params.removeParameter(field.getName());
            if (bodyParam != null) {
                if (bodyParam.value != null && bodyParam.value instanceof BodyItemWrapper) {
                    BodyItemWrapper wrapper = (BodyItemWrapper) bodyParam.value;
                    params.addBodyParameter(rename, wrapper.getValue(),
                            wrapper.getContentType(), wrapper.getFileName());
                } else {
                    params.addBodyParameter(rename, bodyParam.value, null, null);
                }
            }

            if (fileParam != null) {
                if (fileParam.value != null && fileParam.value instanceof BodyItemWrapper) {
                    BodyItemWrapper wrapper = (BodyItemWrapper) fileParam.value;
                    params.addBodyParameter(rename, wrapper.getValue(),
                            wrapper.getContentType(), wrapper.getFileName());
                } else {
                    params.addBodyParameter(rename, fileParam.value, null, null);
                }
            }

            if (queryParam != null) {
                params.addQueryStringParameter(rename,
                        queryParam.value != null ? queryParam.value.toString() : "");
            }
        }
    }

    /**
     * 自定义参数签名
     *
     * @param params
     * @param signs
     */
    @Override
    public void buildSign(RequestParams params, String[] signs) {

    }

    private static SSLSocketFactory trustAllSSlSocketFactory;

    public static SSLSocketFactory getTrustAllSSLSocketFactory() {
        if (trustAllSSlSocketFactory == null) {
            synchronized (BaseParamsBuilder.class) {
                if (trustAllSSlSocketFactory == null) {

                    // 信任所有证书
                    TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        @Override
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }};
                    try {
                        SSLContext sslContext = SSLContext.getInstance("TLS");
                        sslContext.init(null, trustAllCerts, null);
                        trustAllSSlSocketFactory = sslContext.getSocketFactory();
                    } catch (Throwable ex) {
                        LogUtil.e(ex.getMessage(), ex);
                    }
                }
            }
        }

        return trustAllSSlSocketFactory;
    }

}
