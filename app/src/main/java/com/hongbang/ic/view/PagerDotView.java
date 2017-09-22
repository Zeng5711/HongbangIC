package com.hongbang.ic.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.hongbang.ic.R;

import org.xutils.common.util.DensityUtil;

/**
 * 作者：熊海峰 on 16/7/10 00:32
 * ©2016 中国高端品牌网 All Rights Reserved.
 */
public class PagerDotView extends View {

    private Paint mDotPaint = new Paint();

    private int mDotCount = 5;

    private int mCurrentSelected = 0;

    private int mDotDefaultColor = Color.WHITE;

    private int mDotSelectedColor = Color.parseColor("#fff08519");

    private float mDotSize = DensityUtil.dip2px(8);

    private float mDotSpace = 8;

    private float mSelectedDotWidth = 0;

    private RectF mSelectedDotRect = new RectF(0, 0, 0, 0);

    public PagerDotView(Context context) {
        super(context);
        init(null, 0);
    }

    public PagerDotView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public PagerDotView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.PagerDotView, defStyle, 0);

        float dotSize = a.getDimension(
                R.styleable.PagerDotView_dotSize,
                mDotSize);
        if (dotSize > 0) {
            mDotSize = dotSize;
        }


        mDotDefaultColor = a.getColor(R.styleable.PagerDotView_defaultColor,
                mDotDefaultColor);

        mDotSelectedColor = a.getColor(R.styleable.PagerDotView_selectedColor,
                mDotSelectedColor);

        float dotSpace = a.getDimension(R.styleable.PagerDotView_dotSpace,
                mDotSize / 2);
        if (dotSpace > 0) {
            mDotSpace = dotSpace;
        }

        float selectedDotWidth = a.getDimension(R.styleable.PagerDotView_selectedDotWidth,
                0);
        mSelectedDotWidth = Math.max(0, selectedDotWidth);

        a.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mDotPaint.setStyle(Paint.Style.FILL);

        float x = getWidth() - ((mDotCount - 1) * (mDotSize + mDotSpace) + mSelectedDotWidth
                + mDotSize / 2 + getPaddingRight());
        float y = getHeight() - mDotSize / 2 - getPaddingBottom();
        for (int i = 0; i < mDotCount; i++) {
            if (i == mCurrentSelected) {
                mDotPaint.setColor(mDotSelectedColor);
                mSelectedDotRect.set(x - mDotSize / 2, y - mDotSize / 2,
                        x + mDotSize / 2 + mSelectedDotWidth, y + mDotSize / 2);
                canvas.drawRoundRect(mSelectedDotRect, mDotSize / 2, mDotSize / 2, mDotPaint);
            } else {
                mDotPaint.setColor(mDotDefaultColor);
                canvas.drawCircle(x, y, mDotSize / 2, mDotPaint);
            }

            x += mDotSize + mDotSpace;
            if (i == mCurrentSelected) {
                x += mSelectedDotWidth;
            }
        }

        setCount(mDotCount);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            width = (int) ((mDotCount - 1) * (mDotSize + mDotSpace)
                    + mDotSize + mSelectedDotWidth) + getPaddingLeft() + getPaddingRight();
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = (int) mDotSize + getPaddingTop() + getPaddingBottom();
        }


        setMeasuredDimension(width, height);
    }

    public void setCount(int count) {
        this.mDotCount = count;
        this.invalidate();
    }

    public void setCurrentSelected(int current) {
        this.mCurrentSelected = current;
        this.invalidate();
    }
}
