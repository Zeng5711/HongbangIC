package com.hongbang.ic.view;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.hongbang.ic.R;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

public class ChooseImageMenu extends PopupWindow implements View.OnClickListener {

    private OnMenuCallback mCallback;

    public ChooseImageMenu(Context context, OnMenuCallback callback) {
        mCallback = callback;

        final View view = LayoutInflater.from(context).inflate(R.layout.layout_choose_image, null);

        view.findViewById(R.id.choose_from_camera).setOnClickListener(this);
        view.findViewById(R.id.choose_from_album).setOnClickListener(this);
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);

        this.setOutsideTouchable(true);

        view.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {

                int height = view.findViewById(R.id.pop_layout).getTop();

                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });


        // 设置视图
        this.setContentView(view);
        // 设置弹出窗体的宽和高
        this.setHeight(RelativeLayout.LayoutParams.MATCH_PARENT);
        this.setWidth(RelativeLayout.LayoutParams.MATCH_PARENT);

        // 设置弹出窗体可点击
        this.setFocusable(true);

//        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0xb0000000);
//         设置弹出窗体的背景
        this.setBackgroundDrawable(dw);

        // 设置弹出窗体显示时的动画，从底部向上弹出
        this.setAnimationStyle(R.style.popup_anim);

    }

    @Override
    public void onClick(View v) {
        dismiss();
        if (mCallback == null) {
            return;
        }
        if (v.getId() == R.id.choose_from_album) {
            mCallback.fromAlbum();
        } else if (v.getId() == R.id.choose_from_camera) {
            mCallback.fromCamera();
        }
    }

    public interface OnMenuCallback {
        void fromCamera();

        void fromAlbum();
    }

}
