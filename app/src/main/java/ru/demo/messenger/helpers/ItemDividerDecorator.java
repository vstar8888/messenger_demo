package ru.demo.messenger.helpers;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

public class ItemDividerDecorator extends RecyclerView.ItemDecoration {
    private final Rect margin;
    private final Paint dividerPaint;
    private final int skipFirstCount;
    private int halfWidth;

    public ItemDividerDecorator(@ColorInt int color, float width) {
        this(color, width, new Rect());
    }

    public ItemDividerDecorator(@ColorInt int color, float width, @NonNull Rect margin) {
        this(color, width, margin, 0);
    }

    public ItemDividerDecorator(@ColorInt int color, float width, @NonNull Rect margin, int skipFirstCount) {
        this.margin = margin;
        this.dividerPaint = new Paint();
        this.dividerPaint.setColor(color);
        this.skipFirstCount = skipFirstCount;
        final float strokeWidth = width < 1 ? 1 : width;
        this.dividerPaint.setStrokeWidth(strokeWidth);
        final int offset = Math.round(strokeWidth) / 2;
        halfWidth = offset < 1 ? 1 : offset;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (isLastItem(view, parent)) {
            return;
        }
        switch (getOrientation(parent)) {
            case LinearLayoutManager.VERTICAL:
                setVerticalOffsets(outRect);
                return;
            case LinearLayoutManager.HORIZONTAL:
                setHorizontalOffsets(outRect);
                return;
        }
        throw new IllegalStateException("Invalid layout orientation");
    }

    private boolean isLastItem(View view, RecyclerView parent) {
        return parent.getChildAdapterPosition(view) == parent.getAdapter().getItemCount() - 1;
    }

    private void setVerticalOffsets(Rect outRect) {
        final int height = margin.height();
        if (height < 0) {
            outRect.set(0, -height, 0, 0);
        } else {
            outRect.set(0, 0, 0, height);
        }
    }

    private void setHorizontalOffsets(Rect outRect) {
        final int width = margin.width();
        if (width < 0) {
            outRect.set(-width, 0, 0, 0);
        } else {
            outRect.set(0, 0, width, 0);
        }
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        switch (getOrientation(parent)) {
            case LinearLayoutManager.VERTICAL:
                drawVertical(c, parent);
                return;
            case LinearLayoutManager.HORIZONTAL:
                drawHorizontal(c, parent);
                return;
        }
        throw new IllegalStateException("Invalid layout orientation");
    }

    private void drawHorizontal(Canvas c, RecyclerView parent) {
        final int top = parent.getPaddingTop() + margin.top;
        final int bottom = parent.getHeight() - parent.getPaddingBottom() - margin.bottom;

        for (int index = 0, childCount = parent.getChildCount(); index < childCount; index++) {
            final View child = parent.getChildAt(index);
            final int adapterPosition = parent.getChildAdapterPosition(child);
            if (adapterPosition < skipFirstCount || isLastItem(child, parent)) {
                continue;
            }
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int center = (int) (child.getRight() + child.getTranslationX() + params.rightMargin - halfWidth);
            c.drawLine(center, top, center, bottom, dividerPaint);
        }
    }

    private void drawVertical(Canvas c, RecyclerView parent) {
        final int left = parent.getPaddingLeft() + margin.left;
        final int right = parent.getWidth() - parent.getPaddingRight() - margin.right;

        for (int index = 0, childCount = parent.getChildCount(); index < childCount; index++) {
            final View child = parent.getChildAt(index);
            final int adapterPosition = parent.getChildAdapterPosition(child);
            if (adapterPosition < skipFirstCount || isLastItem(child, parent)) {
                continue;
            }
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int center = (int) (child.getBottom() + child.getTranslationY() + params.bottomMargin - halfWidth);
            c.drawLine(left, center, right, center, dividerPaint);
        }
    }

    private int getOrientation(RecyclerView parent) {
        if (parent.getLayoutManager() instanceof LinearLayoutManager) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) parent.getLayoutManager();
            return layoutManager.getOrientation();
        } else {
            throw new IllegalStateException("DividerItemDecoration can only be used with a LinearLayoutManager.");
        }
    }

}
