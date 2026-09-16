package com.supremeaiventures.flashdistancelab;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

public final class MeterView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float stops;

    public MeterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setContentDescription("Exposure stop meter from minus four to plus four");
    }

    public void setStops(double stops) {
        this.stops = (float) stops;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(width, Math.round(dp(52)));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float left = dp(16);
        float right = getWidth() - dp(16);
        float top = dp(12);
        float bottom = top + dp(15);
        float radius = dp(6);

        paint.setShader(new LinearGradient(left, 0, right, 0,
                new int[]{Color.rgb(132, 200, 208), Color.rgb(255, 191, 77), Color.rgb(190, 69, 69)},
                null,
                Shader.TileMode.CLAMP));
        canvas.drawRoundRect(new RectF(left, top, right, bottom), radius, radius, paint);
        paint.setShader(null);

        paint.setColor(Color.argb(90, 20, 20, 22));
        paint.setStrokeWidth(dp(1));
        for (int index = 0; index <= 8; index++) {
            float x = left + (right - left) * index / 8f;
            canvas.drawLine(x, top, x, bottom, paint);
        }

        float position = clamp(50f + stops * 12.5f, 0f, 100f) / 100f;
        float needleX = left + (right - left) * position;
        paint.setColor(Color.rgb(22, 22, 22));
        canvas.drawRoundRect(new RectF(needleX - dp(2), top - dp(3), needleX + dp(2), bottom + dp(6)), dp(3), dp(3), paint);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private static float clamp(float value, float min, float max) {
        return Math.min(max, Math.max(min, value));
    }
}
