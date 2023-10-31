package ru.demo.messenger.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Shader;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

class CircleFrameLayout extends FrameLayout {

    private Canvas childViewCanvas;
    private Paint childViewPaint;

    public CircleFrameLayout(Context context) {
        super(context);
        initialize();
    }

    public CircleFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public CircleFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    private void initialize() {
        childViewPaint = new Paint();
        childViewPaint.setAntiAlias(true);
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        return super.drawChild(childViewCanvas, child, drawingTime);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        final int radius = Math.round(canvas.getWidth() / (float) 2);
        canvas.drawCircle(radius, radius, radius, childViewPaint);
        super.dispatchDraw(canvas);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        final Bitmap shaderBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        childViewCanvas = new Canvas(shaderBitmap);
        final Shader shader = new BitmapShader(shaderBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        childViewPaint.setShader(shader);
    }

    protected void clearCanvas() {
        if (childViewCanvas == null) {
            return;
        }
        final int color = ContextCompat.getColor(getContext(), android.R.color.transparent);
        childViewCanvas.drawColor(color, PorterDuff.Mode.CLEAR);
    }

}