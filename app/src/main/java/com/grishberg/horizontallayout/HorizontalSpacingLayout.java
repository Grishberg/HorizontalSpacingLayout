package com.grishberg.horizontallayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * If childWidth attribute is not empty, then set new width every child.
 */
public class HorizontalSpacingLayout extends ViewGroup {
    private int space = 0;
    private int itemWidth = 0;
    private Rect tmpChildRect = new Rect();
    private boolean isStretchable;

    public HorizontalSpacingLayout(Context context) {
        this(context, null);
    }

    public HorizontalSpacingLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalSpacingLayout(Context context, AttributeSet attrs, int style) {
        super(context, attrs, style);
        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.HorizontalSpacingLayout, 0, 0);
            try {
                itemWidth = (int) (a.getDimension(R.styleable.HorizontalSpacingLayout_childWidth, 0) + 0.5f);
                isStretchable = a.getBoolean(R.styleable.HorizontalSpacingLayout_stretchable, false);
            } finally {
                a.recycle();
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        int maxWidth = View.MeasureSpec.getSize(widthMeasureSpec);
        int maxMeasuredHeight = 0;

        int maxHeight;
        int childState = 0;
        int itemsCount = 0;

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == View.GONE) {
                continue;
            }
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
            int measuredChildHeightWithMargins = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            maxMeasuredHeight = Math.max(maxMeasuredHeight, measuredChildHeightWithMargins);
            itemsCount++;
            if (itemWidth == 0) {
                itemWidth = child.getMeasuredWidth();
            }
        }

        maxHeight = Math.max(maxMeasuredHeight, getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());

        setMeasuredDimension(
                View.resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
                View.resolveSizeAndState(maxHeight, heightMeasureSpec, childState));

        int paddingStart = getPaddingStart();
        int paddingEnd = getPaddingEnd();
        int availableWidth = (maxWidth - paddingStart - paddingEnd);
        if (!isStretchable && itemsCount > 1) {
            space = (availableWidth - 2 * itemWidth - (itemsCount - 2) * itemWidth) / (itemsCount - 1);
        }
        if (isStretchable && itemsCount > 0) {
            itemWidth = (maxWidth - paddingStart - paddingEnd) / itemsCount;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int count = getChildCount();
        int leftPos = getPaddingLeft();
        int parentTop = getPaddingTop();
        int parentBottom = bottom - top - getPaddingBottom();

        for (int childIndex = 0; childIndex < count; childIndex++) {
            View child = getChildAt(childIndex);

            if (child.getVisibility() == View.GONE) {
                continue;
            }

            LayoutParams lp = (LayoutParams) child.getLayoutParams();

            int childHeight = child.getMeasuredHeight();

            int childHeightWithMargins = childHeight + lp.topMargin + lp.bottomMargin;
            tmpChildRect.top = (parentBottom - parentTop - childHeightWithMargins) / 2;
            tmpChildRect.bottom = tmpChildRect.top + childHeight;

            tmpChildRect.left = leftPos + lp.leftMargin;
            tmpChildRect.right = tmpChildRect.left + itemWidth;

            if (isStretchable) {
                leftPos = tmpChildRect.right + lp.rightMargin;
            } else {
                leftPos = tmpChildRect.right + space;
            }

            // Place the child.
            child.layout(tmpChildRect.left,
                    tmpChildRect.top,
                    tmpChildRect.right,
                    tmpChildRect.bottom);
        }
    }

    // The rest of the implementation is for custom per-child layout parameters.
    // If you do not need these (for example you are writing a layout manager
    // that does fixed positioning of its children), you can drop all of this.

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    public class LayoutParams extends MarginLayoutParams {

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(ViewGroup.LayoutParams p) {
            super(p);
        }

        public LayoutParams(int w, int h) {
            super(w, h);
        }
    }
}
