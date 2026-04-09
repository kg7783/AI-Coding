package de.einmaleins.trainer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

public class StatsView extends View {
    private Paint correctPaint;
    private Paint wrongPaint;
    private Paint bgPaint;
    private Paint textPaint;
    private Paint symbolPaint;
    private Paint progressBgPaint;

    private int correct = 0;
    private int wrong = 0;
    private int total = 0;

    private int correctColor = 0xFF228B22;
    private int wrongColor = 0xFFDC143C;
    private int bgColor = 0xFFE0E0E0;
    private int textColor = 0xFF333333;

    public StatsView(Context context) {
        super(context);
        init();
    }

    public StatsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StatsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        correctPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        correctPaint.setColor(correctColor);
        correctPaint.setStyle(Paint.Style.FILL);

        wrongPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wrongPaint.setColor(wrongColor);
        wrongPaint.setStyle(Paint.Style.FILL);

        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(bgColor);
        bgPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        symbolPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        symbolPaint.setTextAlign(Paint.Align.CENTER);

        progressBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressBgPaint.setColor(bgColor);
        progressBgPaint.setStyle(Paint.Style.FILL);
    }

    public void setStats(int correct, int wrong) {
        this.correct = correct;
        this.wrong = wrong;
        this.total = correct + wrong;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        if (width <= 0 || height <= 0) return;

        int padding = 24;
        int barHeight = 24;
        int barY = height - padding - barHeight;
        int barWidth = width - padding * 2;
        int barX = padding;

        RectF bgRect = new RectF(barX, barY, barX + barWidth, barY + barHeight);
        canvas.drawRoundRect(bgRect, 12, 12, bgPaint);

        float correctRatio = total > 0 ? (float) correct / total : 0;
        float correctWidth = barWidth * correctRatio;

        if (correctWidth > 0) {
            RectF correctRect = new RectF(barX, barY, barX + correctWidth, barY + barHeight);
            canvas.drawRoundRect(correctRect, 12, 12, correctPaint);
        }

        // Correct number with symbol
        textPaint.setColor(correctColor);
        textPaint.setTextSize(height * 0.5f);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        
        String correctNum = String.valueOf(correct);
        canvas.drawText(correctNum, padding + 30, height / 2 + 8, textPaint);
        
        symbolPaint.setColor(correctColor);
        symbolPaint.setTextSize(height * 0.35f);
        canvas.drawText(getContext().getString(R.string.symbol_correct), padding + 140, height / 2 + 5, symbolPaint);

        // Wrong number with symbol
        textPaint.setColor(wrongColor);
        textPaint.setTextSize(height * 0.5f);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        
        String wrongNum = String.valueOf(wrong);
        canvas.drawText(wrongNum, width - padding - 40, height / 2 + 8, textPaint);
        
        symbolPaint.setColor(wrongColor);
        symbolPaint.setTextSize(height * 0.35f);
        canvas.drawText(getContext().getString(R.string.symbol_wrong), width - padding - 150, height / 2 + 5, symbolPaint);

        // Percentage in center
        if (total > 0) {
            int percentage = (int) Math.round(correctRatio * 100);
            textPaint.setColor(0xFF666666);
            textPaint.setTextSize(height * 0.3f);
            textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            canvas.drawText(percentage + "%", width / 2, height / 2 + 5, textPaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = 200;
        int desiredHeight = 80;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(desiredWidth, widthSize);
        } else {
            width = desiredWidth;
        }

        int height;
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(desiredHeight, heightSize);
        } else {
            height = desiredHeight;
        }

        setMeasuredDimension(width, height);
    }
}
