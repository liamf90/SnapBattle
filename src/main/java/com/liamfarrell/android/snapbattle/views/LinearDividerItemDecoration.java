package com.liamfarrell.android.snapbattle.views;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class LinearDividerItemDecoration extends RecyclerView.ItemDecoration{
    private Drawable mDivider;

    public LinearDividerItemDecoration(Drawable divider) {
        mDivider = divider;
    }


    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        if (parent.getChildAdapterPosition(view) == 0) {
            return;
        }

        outRect.top = mDivider.getIntrinsicHeight();

    }
    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int dividerLeft = parent.getPaddingLeft();
        int dividerRight = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount - 1; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int dividerTop = child.getBottom() + params.bottomMargin;
            int dividerBottom = dividerTop + mDivider.getIntrinsicHeight();

            mDivider.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom);
            mDivider.draw(c);
        }
    }

}