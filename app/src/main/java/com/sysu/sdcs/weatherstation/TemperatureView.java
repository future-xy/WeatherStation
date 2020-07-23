package com.sysu.sdcs.weatherstation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;


public class TemperatureView extends View {
    private static final String TAG = "TemperatureView";

    private int minValue;
    private int maxValue;
    private int currentValue;
    private String currentDay;
    private int lastValue;
    private int nextValue;
    private Paint mPaint = new Paint();
    private int viewHeight;
    private int viewWidth;
    private int pointX;
    private int pointY;
    private boolean isDrawLeftLine;
    private boolean isDrawRightLine;
    private String color;
    private int yBias;
    private int pointTopY = (int) (40 * Util.getDensity(getContext()));
    private int pointBottomY = (int) (140 * Util.getDensity(getContext()));
    private int mMiddleValue;

    public TemperatureView(Context context) {
        super(context);
    }

    public TemperatureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TemperatureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    //设置最小值
    public void setMinValue(int minValue) {
        this.minValue = minValue;
    }

    //设置最大值
    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }

    //设置当前日期
    public void setCurrentDay(String currentDay) {
        this.currentDay = currentDay;
    }

    //设置目前的值
    public void setCurrentValue(int currentValue) {
        this.currentValue = currentValue;
    }

    //设置是否画左边线段(只有第一个View是false)
    public void setDrawLeftLine(boolean isDrawLeftLine) {
        this.isDrawLeftLine = isDrawLeftLine;
    }

    //设置是否画右边线段(只有最后一个View是false)
    public void setDrawRightLine(boolean isDrawRightLine) {
        this.isDrawRightLine = isDrawRightLine;
    }

    //设置之前温度点的值
    public void setLastValue(int lastValue) {
        this.lastValue = lastValue;
    }

    //设置下一个温度点的值
    public void setNextValue(int nextValue) {
        this.nextValue = nextValue;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //给一个初始长、宽
        int mDefaultWidth = 200;
        int mDefaultHeight = (int) (220 * Util.getDensity(getContext()));
        setMeasuredDimension(resolveSize(mDefaultWidth, widthMeasureSpec), resolveSize(mDefaultHeight, heightMeasureSpec));
        viewHeight = getMeasuredHeight();
        viewWidth = getMeasuredWidth();
        pointX = viewWidth / 2;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mMiddleValue = (pointTopY + pointBottomY) / 2;
        pointY = mMiddleValue + (int) ((pointBottomY - pointTopY) * 1f / (maxValue - minValue) * ((maxValue + minValue) / 2 - currentValue));

        drawGraph(canvas);
        drawValue(canvas);
        drawPoint(canvas);
        drawDay(canvas);
    }

    //还可以添加一堆东西，比如：天气图标，风速，，，
    //绘制日期
    private void drawDay(Canvas canvas) {
        mPaint.setTextSize(50);
        mPaint.setStrokeWidth(0);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(currentDay, pointX, 570, mPaint);
    }

    //绘制数值
    private void drawValue(Canvas canvas) {
        mPaint.setTextSize(40);
        setTextColor();
        mPaint.setStrokeWidth(0);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(currentValue + "°", pointX, pointY + yBias, mPaint);
    }


    //绘制温度点
    public void drawPoint(Canvas canvas) {
        mPaint.setColor(Color.BLUE);
        mPaint.setStrokeWidth(2);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(pointX, pointY, 10, mPaint);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(pointX, pointY, 5, mPaint);
    }

    //绘制线段（线段组成折线）
    public void drawGraph(Canvas canvas) {
        mPaint.setColor(0xFF24C3F1);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(3);
        mPaint.setAntiAlias(true);    //设置抗锯齿
        mPaint.setColor(Color.parseColor(color));

        //判断是否画左线段（第一个View不用，其他全要）
        if (isDrawLeftLine) {
            float middleValue = currentValue - (currentValue - lastValue) / 2f;

            float middleY = mMiddleValue + (int) ((pointBottomY - pointTopY) * 1f / (maxValue - minValue) * ((maxValue + minValue) / 2 - middleValue));
            canvas.drawLine(0, middleY, pointX, pointY, mPaint);
        }

        //判断是否画右线段（最后View不用，其他全要）
        if (isDrawRightLine) {
            float middleValue = currentValue - (currentValue - nextValue) / 2f;
            float middleY = mMiddleValue + (int) ((pointBottomY - pointTopY) * 1f / (maxValue - minValue) * ((maxValue + minValue) / 2 - middleValue));
            canvas.drawLine(pointX, pointY, viewWidth, middleY, mPaint);
        }
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setyBias(int yBias) {
        this.yBias = yBias;
    }


    //设置字体颜色
    public void setTextColor() {
        if (currentValue <= 10 && currentValue >= 0) {
            mPaint.setColor(Color.BLUE);
        } else if (currentValue <= 20 && currentValue > 10) {
            mPaint.setColor(Color.GREEN);
        } else if (currentValue <= 30 && currentValue > 20) {
            mPaint.setColor(0xFFFF8000);
        } else if (currentValue <= 40 && currentValue > 30) {
            mPaint.setColor(Color.RED);
        }
    }
}
