package com.hongbang.ic.view;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hongbang.ic.util.DensityUtils;

import org.xutils.common.util.DensityUtil;

/**
 * Created by xionghf on 16/3/29.
 */
public class BottomBarItemView extends LinearLayout {

    private static final int NORMAL_TEXT_COLOR = Color.GRAY;

    private static final int SELECTED_TEXT_COLOR = Color.BLUE;

    private Context mContext;

    private int mNormalIconId;
    private int mSelectedIconId;
    private String mText;

    private ImageView mIconView;

    private TextView mNameLabel;

    public BottomBarItemView(Context context, int normalIconId, int selectedIconId, String text) {
        super(context);

        this.mContext = context;
        this.mNormalIconId = normalIconId;
        this.mSelectedIconId = selectedIconId;
        this.mText = text;


        this.setPadding(0, 0, 0, 0);
        this.setOrientation(LinearLayout.VERTICAL);
        this.setGravity(Gravity.CENTER);
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1);
        lp.gravity = Gravity.CENTER;
        this.setLayoutParams(lp);

        this.addIconView();
    }

    private void addIconView() {
        mIconView = new ImageView(mContext);
        mIconView.setLayoutParams(new LayoutParams(DensityUtil.dip2px(25), DensityUtil.dip2px(25)));
        mIconView.setImageResource(mNormalIconId);
        this.addView(mIconView);
        this.addNameLabel();
    }

    private void addNameLabel() {
        mNameLabel = new TextView(mContext);
        mNameLabel.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        mNameLabel.setText(mText);
        mNameLabel.setTextColor(NORMAL_TEXT_COLOR);
        mNameLabel.setTextSize(14);
        this.addView(mNameLabel);
    }

    @Override
    public void setSelected(boolean selected) {
        if (selected) {
            mIconView.setImageResource(mSelectedIconId);
            mNameLabel.setTextColor(SELECTED_TEXT_COLOR);
        } else {
            mIconView.setImageResource(mNormalIconId);
            mNameLabel.setTextColor(NORMAL_TEXT_COLOR);
        }
        super.setSelected(selected);
    }
}
