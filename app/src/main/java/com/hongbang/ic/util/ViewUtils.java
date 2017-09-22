package com.hongbang.ic.util;

import android.graphics.drawable.Drawable;
import android.media.Image;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;

import com.hongbang.ic.constant.HttpConstants;

import org.xutils.common.Callback;
import org.xutils.image.ImageOptions;
import org.xutils.x;

/**
 * 视图工具类
 * <p/>
 * Created by xionghf on 16/4/17.
 */
public class ViewUtils {
    public static void setGridViewHeightBasedOnChildren(GridView gridView, int columns) {
        ListAdapter listAdapter = gridView.getAdapter();
        if (listAdapter == null)
            return;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(gridView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        int rows = listAdapter.getCount() / columns;
        if (listAdapter.getCount() % columns > 0) {
            rows++;
        }
        for (int i = 0; i < rows; i++) {
            view = listAdapter.getView(i, view, gridView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, LinearLayout.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = gridView.getLayoutParams();
        params.height = totalHeight + gridView.getPaddingTop() + gridView.getPaddingBottom();
        gridView.setLayoutParams(params);
        gridView.requestLayout();
    }

    public static void loadImage(ImageView view, String url, ImageOptions options) {
        if (view == null) {
            return;
        }
        if (url != null && url.length() > 0 && !url.startsWith("http")) {
            if (url.startsWith("/")) {
                url = HttpConstants.SERVER_IMAGE + url;
            } else {
                url = HttpConstants.SERVER_IMAGE + "/" + url;
            }
        }
        x.image().bind(view, url, options, DEFAULT_IMAGE_CALLBACK);
    }

    private static Callback.CacheCallback<Drawable> DEFAULT_IMAGE_CALLBACK
            = new Callback.CacheCallback<Drawable>() {
        @Override
        public boolean onCache(Drawable result) {
            return true;
        }

        @Override
        public void onSuccess(Drawable result) {
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
    };
}
