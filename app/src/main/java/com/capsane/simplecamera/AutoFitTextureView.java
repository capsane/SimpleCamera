package com.capsane.simplecamera;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;

/**
 * Created by capsane on 18-7-3.
 *
 */

public class AutoFitTextureView extends TextureView {

    private int mRationWidth = 0;
    private int mRationHeight = 0;

    public AutoFitTextureView(Context context) {
        this(context, null);
    }

    public AutoFitTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoFitTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    /**
     * 设置View的比例
     * */
    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative");
        }
        mRationWidth = width;
        mRationHeight = height;
        // TODO: 自定义控件涉及到View的大小变化时，要求重新执行View的绘制中的layout
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (0 == mRationHeight || 0 == mRationWidth) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * mRationWidth / mRationHeight) {
                setMeasuredDimension(width, width * mRationHeight / mRationWidth);
            } else {
                setMeasuredDimension(height * mRationWidth / mRationHeight, height);
            }
        }
    }
}
