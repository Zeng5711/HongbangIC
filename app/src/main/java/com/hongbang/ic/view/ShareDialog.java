package com.hongbang.ic.view;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.hongbang.ic.R;
import com.hongbang.ic.constant.AppConstants;
import com.hongbang.ic.util.AppUtils;
import com.hongbang.ic.util.WXUtil;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

/**
 * 分享选择
 * <p/>
 * Created by xionghf on 16/5/24.
 */
public class ShareDialog extends PopupWindow implements View.OnClickListener {

    private Context mContext;

    public ShareDialog(Context context) {
        this.mContext = context;

        final View view = LayoutInflater.from(context).inflate(R.layout.layout_share_dialog, null);

        view.findViewById(R.id.btn_share_session).setOnClickListener(this);
        view.findViewById(R.id.btn_share_timeline).setOnClickListener(this);
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
        if (v.getId() == R.id.btn_share_session) {
            shareToWX(SendMessageToWX.Req.WXSceneSession);
        } else if (v.getId() == R.id.btn_share_timeline) {
            shareToWX(SendMessageToWX.Req.WXSceneTimeline);
        }
    }

    private void shareToWX(int scene) {
        final IWXAPI api = WXAPIFactory.createWXAPI(mContext, AppConstants.WEIXIN_APP_ID, true);
        api.registerApp(AppConstants.WEIXIN_APP_ID);
        WXWebpageObject media = new WXWebpageObject();
        media.webpageUrl = AppConstants.SHARE_URL;

        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = media;
        msg.title = AppConstants.SHARE_TITLE;
        msg.description = AppConstants.SHARE_DESCRIPTION;
        msg.thumbData = WXUtil.bmpToByteArray(AppUtils.getImageFromAssetsFile(mContext, "images/share_thumb.png"), true);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = String.valueOf(System.currentTimeMillis());
        req.message = msg;
        req.scene = scene;

        api.sendReq(req);
    }
}
