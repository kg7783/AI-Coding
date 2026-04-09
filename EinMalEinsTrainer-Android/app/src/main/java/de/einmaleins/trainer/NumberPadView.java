package de.einmaleins.trainer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class NumberPadView extends View {
    private static final int BUTTON_COUNT = 12;
    private static final int COLS = 3;
    private static final int ROWS = 4;
    private static final int PADDING = 4;
    private static final int SPACING = 4;
    private static final float CORNER_RADIUS = 8f;
    private static final int BUTTON_PRESSED_COLOR = 0xFFCCCCFF;
    private static final int BUTTON_NORMAL_COLOR = 0xFFF0F0FA;
    private static final int BUTTON_TEXT_COLOR = 0xFF000000;
    private static final int BORDER_COLOR = 0xFFCCCCCC;
    private static final int BORDER_WIDTH = 2;

    private static final int VALUE_DELETE = -1;
    private static final int VALUE_OK = -2;

    private static final int[] BUTTON_VALUES = {1, 2, 3, 4, 5, 6, 7, 8, 9, VALUE_DELETE, 0, VALUE_OK};
    private static final int[] BUTTON_LABELS = {1, 2, 3, 4, 5, 6, 7, 8, 9, R.string.btn_del, 0, R.string.btn_ok};

    private Paint buttonPaint;
    private Paint textPaint;
    private Paint borderPaint;
    private RectF[] buttonRects;

    private OnNumberPadListener listener;
    private int lastPressedButton = -1;

    public interface OnNumberPadListener {
        void onNumberPressed(int number);
        void onDeletePressed();
        void onOkPressed();
    }

    public NumberPadView(Context context) {
        super(context);
        init();
    }

    public NumberPadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NumberPadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        buttonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        buttonPaint.setColor(BUTTON_NORMAL_COLOR);
        buttonPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(BUTTON_TEXT_COLOR);
        textPaint.setTextAlign(Paint.Align.CENTER);

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(BORDER_COLOR);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(BORDER_WIDTH);

        buttonRects = new RectF[BUTTON_COUNT];
        for (int i = 0; i < BUTTON_COUNT; i++) {
            buttonRects[i] = new RectF();
        }
    }

    public void setOnNumberPadListener(OnNumberPadListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calculateRects(w, h);
    }

    private void calculateRects(int width, int height) {
        float btnW = (width - PADDING * 2f - SPACING * (COLS - 1)) / COLS;
        float btnH = (height - PADDING * 2f - SPACING * (ROWS - 1)) / ROWS;

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int idx = row * COLS + col;
                float left = PADDING + col * (btnW + SPACING);
                float top = PADDING + row * (btnH + SPACING);
                buttonRects[idx].set(left, top, left + btnW, top + btnH);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        float textSize = Math.min(w, h) / 12f;
        textPaint.setTextSize(textSize);

        for (int i = 0; i < BUTTON_COUNT; i++) {
            RectF rect = buttonRects[i];
            
            buttonPaint.setColor(i == lastPressedButton ? BUTTON_PRESSED_COLOR : BUTTON_NORMAL_COLOR);
            
            canvas.drawRoundRect(rect, CORNER_RADIUS, CORNER_RADIUS, buttonPaint);
            canvas.drawRoundRect(rect, CORNER_RADIUS, CORNER_RADIUS, borderPaint);

            float textY = rect.centerY() + textSize / 3f;
            int labelRes = BUTTON_LABELS[i];
            String label;
            if (labelRes >= 10) {
                label = getContext().getString(labelRes);
            } else {
                label = String.valueOf(labelRes);
            }
            canvas.drawText(label, rect.centerX(), textY, textPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            for (int i = 0; i < BUTTON_COUNT; i++) {
                if (buttonRects[i].contains(x, y)) {
                    lastPressedButton = i;
                    invalidate();
                    return true;
                }
            }
        }
        
        if (event.getAction() == MotionEvent.ACTION_UP) {
            for (int i = 0; i < BUTTON_COUNT; i++) {
                if (buttonRects[i].contains(x, y)) {
                    handleButtonPress(BUTTON_VALUES[i]);
                    lastPressedButton = -1;
                    invalidate();
                    return true;
                }
            }
            lastPressedButton = -1;
            invalidate();
        }
        
        return true;
    }

    private void handleButtonPress(int value) {
        if (listener == null) return;
        
        if (value >= 0) {
            listener.onNumberPressed(value);
        } else if (value == VALUE_DELETE) {
            listener.onDeletePressed();
        } else if (value == VALUE_OK) {
            listener.onOkPressed();
        }
    }
}
