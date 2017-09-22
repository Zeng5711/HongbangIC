package com.hongbang.ic.view;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import static android.R.attr.orientation;

/**
 * 作者：熊海峰 on 2016/11/19 04:24
 * Copyright (c) 2016 佰饱箱. All Rights Reserved.
 */
public class DividerItemDecoration extends RecyclerView.ItemDecoration {
    public static final int HORIZONTAL = LinearLayout.HORIZONTAL;
    public static final int VERTICAL = LinearLayout.VERTICAL;

    private int mOrientation;

    private int mDividerSize;

    private boolean mDrawBound = false;

    private Paint mPaint = new Paint();

    public DividerItemDecoration(int orientation, int size, @ColorInt int color) {
        mOrientation = orientation;
        mDividerSize = size;
        mPaint.setStrokeWidth(size);
        mPaint.setColor(color);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (parent.getLayoutManager() == null) {
            return;
        }
        if (mOrientation == VERTICAL) {
            drawVertical(c, parent, state);
        } else {
            drawHorizontal(c, parent, state);
        }
    }

    public void setDrawBound(boolean drawBound) {
        mDrawBound = drawBound;
    }

    private void drawVertical(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        canvas.save();
        for (int i = 0; i < parent.getChildCount(); i++) {
            View v = parent.getChildAt(i);
            int index = parent.getChildAdapterPosition(v);
            int count = state.getItemCount();
            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) v.getLayoutParams();
            if (mDrawBound && index == 0) {
                canvas.drawLine(v.getLeft() - lp.leftMargin - mDividerSize / 2,
                        v.getTop() + lp.topMargin,
                        v.getLeft() - lp.leftMargin - mDividerSize / 2,
                        v.getBottom() - lp.bottomMargin, mPaint);
            }
            if (index < count - 1 || mDrawBound) {
                canvas.drawLine(v.getRight() + lp.rightMargin + mDividerSize / 2,
                        v.getTop() + lp.topMargin,
                        v.getRight() + lp.rightMargin + mDividerSize / 2,
                        v.getBottom() - lp.bottomMargin, mPaint);
            }
        }

        canvas.restore();
    }

    private void drawHorizontal(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        canvas.save();
        for (int i = 0; i < parent.getChildCount(); i++) {
            View v = parent.getChildAt(i);
            int index = parent.getChildAdapterPosition(v);
            int count = state.getItemCount();
            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) v.getLayoutParams();
            if (mDrawBound && index == 0) {
                canvas.drawLine(v.getLeft() + lp.leftMargin,
                        v.getTop() - lp.topMargin - mDividerSize / 2,
                        v.getRight() - lp.rightMargin,
                        v.getTop() - lp.topMargin - mDividerSize / 2, mPaint);
            }
            if (index < count - 1 || mDrawBound) {
                canvas.drawLine(v.getLeft() + lp.leftMargin,
                        v.getBottom() + lp.bottomMargin + mDividerSize / 2,
                        v.getRight() - lp.rightMargin,
                        v.getBottom() + lp.bottomMargin + mDividerSize / 2, mPaint);
            }
        }

        canvas.restore();
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int index = parent.getChildAdapterPosition(view);
        int count = parent.getAdapter().getItemCount();
        int start = mDrawBound && index == 0 ? mDividerSize : 0;
        int end = index < count - 1 || mDrawBound ? mDividerSize : 0;

        if (orientation == VERTICAL) {
            outRect.set(start, 0, end, 0);
        } else {
            outRect.set(0, start, 0, end);
        }
    }
}