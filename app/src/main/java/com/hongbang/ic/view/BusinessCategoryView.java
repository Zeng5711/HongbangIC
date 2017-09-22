package com.hongbang.ic.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hongbang.ic.R;

import org.xutils.common.util.DensityUtil;

/**
 * 商品详情分类
 * <p/>
 * Created by xionghf on 16/5/30.
 */
public class BusinessCategoryView extends LinearLayout implements View.OnClickListener {

    private static final int COLUMN_COUNT = 5;

    private Context mContext;

    private OnCategorySelectedListener mListener;

    private int mCurrentSelected = -1;

    public BusinessCategoryView(Context context, int[] icons, String[] names, int[] ids) {
        super(context);
        mContext = context;
        setOrientation(VERTICAL);
        setPadding(0, DensityUtil.dip2px(10), 0, DensityUtil.dip2px(10));
        setBackgroundColor(Color.parseColor("#f0f0f0"));

        addChildren(icons, names, ids);
    }

    private void addChildren(int[] icons, String[] names, int[] ids) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout row = null;
        for (int i = 0; i < icons.length; i++) {
            if (i % COLUMN_COUNT == 0) {
                row = new LinearLayout(mContext);
                row.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                row.setOrientation(HORIZONTAL);
                row.setBackgroundColor(Color.WHITE);
                int p = DensityUtil.dip2px(5), top = 0, bottom = 0;
                if (i == 0) {
                    top = p;
                }
                if (i / COLUMN_COUNT == (icons.length - 1) / COLUMN_COUNT) {
                    bottom = p;
                }
                row.setPadding(p, top, p, bottom);
                addView(row);
            }
            View layout = inflater.inflate(R.layout.business_category_item, null);
            layout.setOnClickListener(this);
            layout.setId(ids[i]);
            ((ImageView) layout.findViewById(R.id.category_icon)).setImageResource(icons[i]);
            ((TextView) layout.findViewById(R.id.category_name)).setText(names[i]);

            if (row != null) {
                row.addView(layout, new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            }
        }

    }

    public void setSelection(int id) {
        if (mCurrentSelected == id) {
            return;
        }
        if (mCurrentSelected > 0) {
            View view = this.findViewById(mCurrentSelected);
            if (view != null) {
                view.setSelected(false);
            }
        }
        View view = this.findViewById(id);
        if (view != null) {
            mCurrentSelected = id;
            view.setSelected(true);
        }
    }

    public void setOnCategorySelectedListener(OnCategorySelectedListener l) {
        mListener = l;
    }

    @Override
    public void onClick(View v) {
        if (mListener != null) {
            setSelection(v.getId());
            mListener.onCategorySelected(v.getId());
        }
    }

    public interface OnCategorySelectedListener {
        void onCategorySelected(int categoryId);
    }
}
