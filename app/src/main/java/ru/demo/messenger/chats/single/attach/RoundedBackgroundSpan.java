package ru.demo.messenger.chats.single.attach;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.text.style.ReplacementSpan;

public class RoundedBackgroundSpan extends ReplacementSpan {
    private static final int CORNER_RADIUS = 8;
    private int backgroundColor;
    private int textColor;

    @SuppressWarnings("ResourceType")
    public RoundedBackgroundSpan(@NonNull Context context,
                                 @ColorRes int backgroundColor,
                                 @ColorRes int textColor) {
        super();
        this.backgroundColor = ContextCompat.getColor(context, backgroundColor);
        this.textColor = ContextCompat.getColor(context, textColor);
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, @NonNull Paint paint) {
        RectF rect = new RectF(x, top, x + measureText(paint, text, start, end), bottom);
        paint.setColor(backgroundColor);
        canvas.drawRoundRect(rect, CORNER_RADIUS, CORNER_RADIUS, paint);
        paint.setColor(textColor);
        canvas.drawText(text, start, end, x, y, paint);
    }

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        return Math.round(paint.measureText(text, start, end));
    }

    private float measureText(Paint paint, CharSequence text, int start, int end) {
        return paint.measureText(text, start, end);
    }
}