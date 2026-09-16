package com.supremeaiventures.flashdistancelab;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import java.util.Locale;

public final class FlashSceneView extends View {
    private static final float SUBJECT_X = 0.80f;
    private static final float HEAD_OFFSET = 0.056f;
    private static final float PERCENT_PER_FOOT = 0.0435f;
    private static final float BEAM_SPREAD_RATIO = 0.952f;
    private static final float STAGE_RATIO = 941f / 1672f;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Bitmap background;
    private float distance = 4f;
    private int powerStop = -4;
    private double relativeExposure = 1d;
    private double stops;

    public FlashSceneView(Context context, AttributeSet attrs) {
        super(context, attrs);
        background = BitmapFactory.decodeResource(getResources(), R.drawable.studio_subject_background);
        strokePaint.setStrokeCap(Paint.Cap.ROUND);
        setContentDescription("Studio scene with a movable flash stand and a fixed portrait subject");
        setFocusable(true);
    }

    public void setSimulation(float distance, int powerStop, double relativeExposure, double stops) {
        this.distance = distance;
        this.powerStop = powerStop;
        this.relativeExposure = relativeExposure;
        this.stops = stops;
        setContentDescription(String.format(Locale.US,
                "Flash at %.2f feet, power %s, exposure shift %+.1f stops",
                distance, MainActivity.powerLabel(powerStop), stops));
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = Math.round(width * STAGE_RATIO);
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
            height = MeasureSpec.getSize(heightMeasureSpec);
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();
        float height = getHeight();
        canvas.drawBitmap(background, null, new RectF(0, 0, width, height), paint);

        float subjectX = width * SUBJECT_X;
        float headGap = distance * PERCENT_PER_FOOT * width;
        float lightHeadX = subjectX - headGap;
        float flashX = lightHeadX - HEAD_OFFSET * width;
        float beamCenterY = height * 0.23f;
        float beamSpread = headGap * BEAM_SPREAD_RATIO;

        float exposureVisual = clamp((float) ((stops + 5d) / 9d), 0.05f, 1f);
        float powerVisual = clamp((powerStop + 7f) / 7f, 0f, 1f);
        float beamAlpha = clamp(0.14f + powerVisual * 0.35f + exposureVisual * 0.24f, 0.12f, 0.78f);
        float subjectLight = clamp(0.1f + exposureVisual * 0.9f, 0.08f, 1f);

        drawSubjectGlow(canvas, subjectX, beamCenterY, width, subjectLight);
        drawBeam(canvas, lightHeadX, subjectX, beamCenterY, beamSpread, beamAlpha, subjectLight);
        drawDistanceLine(canvas, lightHeadX, subjectX, height, distance);
        drawLightRig(canvas, flashX, width, height, beamAlpha, subjectLight);
    }

    private void drawSubjectGlow(Canvas canvas, float subjectX, float centerY, float width, float subjectLight) {
        int glowAlpha = Math.round(26f + subjectLight * 42f);
        paint.setShader(new RadialGradient(
                subjectX,
                centerY + getHeight() * 0.16f,
                width * 0.72f,
                new int[]{Color.argb(glowAlpha, 255, 244, 218), Color.argb(8, 255, 244, 218), Color.TRANSPARENT},
                new float[]{0f, 0.58f, 1f},
                Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, width, getHeight(), paint);
        paint.setShader(null);
    }

    private void drawBeam(Canvas canvas, float lightHeadX, float subjectX, float centerY, float spread,
                          float beamAlpha, float subjectLight) {
        Path beamPath = new Path();
        beamPath.moveTo(lightHeadX, centerY);
        beamPath.lineTo(subjectX, centerY - spread / 2f);
        beamPath.lineTo(subjectX, centerY + spread / 2f);
        beamPath.close();

        int startAlpha = Math.round(beamAlpha * 185f);
        int endAlpha = Math.round(beamAlpha * (34f + subjectLight * 40f));
        paint.setShader(new LinearGradient(
                lightHeadX,
                centerY,
                subjectX,
                centerY,
                Color.argb(startAlpha, 255, 255, 255),
                Color.argb(endAlpha, 255, 233, 190),
                Shader.TileMode.CLAMP));
        canvas.drawPath(beamPath, paint);
        paint.setShader(null);
    }

    private void drawDistanceLine(Canvas canvas, float lightHeadX, float subjectX, float height, float distance) {
        float lineY = height * 0.84f;
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(Math.max(2f, getResources().getDisplayMetrics().density * 1.3f));
        strokePaint.setColor(Color.argb(190, 255, 255, 255));
        strokePaint.setPathEffect(new DashPathEffect(new float[]{10f, 8f}, 0f));
        canvas.drawLine(lightHeadX, lineY, subjectX, lineY, strokePaint);
        strokePaint.setPathEffect(null);
        strokePaint.setStrokeWidth(Math.max(2f, getResources().getDisplayMetrics().density * 1.6f));
        canvas.drawLine(lightHeadX, lineY - dp(6), lightHeadX, lineY + dp(7), strokePaint);
        canvas.drawLine(subjectX, lineY - dp(6), subjectX, lineY + dp(7), strokePaint);

        float pillWidth = dp(74);
        float pillHeight = dp(25);
        float pillLeft = ((lightHeadX + subjectX) / 2f) - pillWidth / 2f;
        float pillTop = Math.min(lineY + dp(8), height - pillHeight - dp(7));
        paint.setColor(Color.argb(224, 255, 255, 255));
        canvas.drawRoundRect(new RectF(pillLeft, pillTop, pillLeft + pillWidth, pillTop + pillHeight), dp(7), dp(7), paint);
        paint.setColor(Color.rgb(20, 20, 22));
        paint.setTextSize(dp(13));
        paint.setTypeface(android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD));
        paint.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics metrics = paint.getFontMetrics();
        float textY = pillTop + pillHeight / 2f - (metrics.ascent + metrics.descent) / 2f;
        canvas.drawText(String.format(Locale.US, "%.1f ft", distance), pillLeft + pillWidth / 2f, textY, paint);
        paint.setTextAlign(Paint.Align.LEFT);
    }

    private void drawLightRig(Canvas canvas, float flashX, float width, float height, float beamAlpha, float subjectLight) {
        float rigWidth = clamp(width * 0.14f, dp(54), dp(112));
        float rigTop = height * 0.185f;
        float rigHeight = height * 0.68f;
        float rigLeft = flashX - rigWidth / 2f;
        float standX = rigLeft + rigWidth * 0.35f;

        strokePaint.setStyle(Paint.Style.FILL);
        strokePaint.setColor(Color.argb(Math.round(34f + beamAlpha * 74f), 255, 255, 255));
        canvas.drawCircle(rigLeft + rigWidth * 0.68f, rigTop + rigHeight * 0.03f, rigWidth * 0.17f, strokePaint);

        paint.setShader(new LinearGradient(0, rigTop, 0, rigTop + rigHeight * 0.08f,
                Color.rgb(42, 42, 42), Color.rgb(4, 4, 4), Shader.TileMode.CLAMP));
        RectF body = new RectF(rigLeft + rigWidth * 0.21f, rigTop + rigHeight * 0.018f,
                rigLeft + rigWidth * 0.58f, rigTop + rigHeight * 0.075f);
        canvas.drawRoundRect(body, dp(4), dp(4), paint);
        paint.setShader(null);

        Path reflector = new Path();
        reflector.moveTo(rigLeft + rigWidth * 0.52f, rigTop + rigHeight * 0.012f);
        reflector.lineTo(rigLeft + rigWidth * 0.82f, rigTop - rigHeight * 0.005f);
        reflector.lineTo(rigLeft + rigWidth * 0.87f, rigTop + rigHeight * 0.105f);
        reflector.lineTo(rigLeft + rigWidth * 0.52f, rigTop + rigHeight * 0.075f);
        reflector.close();
        paint.setColor(Color.rgb(16, 16, 16));
        canvas.drawPath(reflector, paint);

        paint.setShader(new RadialGradient(
                rigLeft + rigWidth * 0.83f,
                rigTop + rigHeight * 0.052f,
                rigWidth * 0.12f,
                new int[]{Color.argb(Math.round(218f * beamAlpha), 255, 255, 255), Color.rgb(70, 70, 68)},
                new float[]{0f, 1f},
                Shader.TileMode.CLAMP));
        canvas.drawOval(new RectF(rigLeft + rigWidth * 0.77f, rigTop + rigHeight * 0.012f,
                rigLeft + rigWidth * 0.9f, rigTop + rigHeight * 0.107f), paint);
        paint.setShader(null);

        strokePaint.setColor(Color.rgb(7, 7, 7));
        strokePaint.setStrokeWidth(Math.max(dp(3), width * 0.003f));
        strokePaint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(standX, rigTop + rigHeight * 0.105f, standX, rigTop + rigHeight * 0.815f, strokePaint);
        strokePaint.setStyle(Paint.Style.FILL);
        for (float ratio : new float[]{0.18f, 0.43f, 0.72f}) {
            canvas.drawRoundRect(new RectF(standX - rigWidth * 0.095f, rigTop + rigHeight * ratio,
                    standX + rigWidth * 0.095f, rigTop + rigHeight * ratio + dp(7)), dp(3), dp(3), strokePaint);
        }

        float hubY = rigTop + rigHeight * 0.815f;
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(Math.max(dp(3), width * 0.0032f));
        strokePaint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawLine(standX, hubY, standX - rigWidth * 0.36f, rigTop + rigHeight * 0.995f, strokePaint);
        canvas.drawLine(standX, hubY, standX + rigWidth * 0.36f, rigTop + rigHeight * 0.995f, strokePaint);
        canvas.drawLine(standX, hubY, standX + rigWidth * 0.07f, rigTop + rigHeight * 0.995f, strokePaint);
        strokePaint.setStyle(Paint.Style.FILL);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private static float clamp(float value, float min, float max) {
        return Math.min(max, Math.max(min, value));
    }
}
