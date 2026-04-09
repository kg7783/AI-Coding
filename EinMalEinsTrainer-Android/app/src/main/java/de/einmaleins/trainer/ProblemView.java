package de.einmaleins.trainer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ProblemView extends View {
    private static final long INPUT_DELAY_MS = 2000;

    private Paint bgPaint;
    private Paint textPaint;
    private Paint operatorPaint;
    private Paint inputBgPaint;
    private Paint borderPaint;
    private Paint activeBorderPaint;
    private Paint checkPaint;
    private Paint xPaint;

    private MathProblem problem;
    private String answerText = "";
    private boolean isActive = false;
    private float scale = 1.0f;
    private int bgColor = 0xFFFFFFFF;
    private int activeBgColor = 0xFFF0F8FF;

    private int correctColor = 0xFF228B22;
    private int wrongColor = 0xFFDC143C;

    private OnProblemClickListener clickListener;
    private OnTimerExpiredListener timerListener;

    private boolean showCheck = false;
    private float checkScale = 0f;

    private boolean showX = false;
    private float xScale = 0f;

    private boolean isAnimating = false;

    private Handler timerHandler;
    private Runnable timerRunnable;

    public boolean isAnimating() {
        return isAnimating;
    }

    public interface OnProblemClickListener {
        void onProblemClicked(ProblemView view);
    }

    public interface OnTimerExpiredListener {
        void onTimerExpired(ProblemView view);
    }

    public ProblemView(Context context) {
        super(context);
        init();
    }

    public ProblemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ProblemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setClickable(true);
        setFocusable(true);
        
        timerHandler = new Handler(Looper.getMainLooper());
        
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(bgColor);
        bgPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFF000000);
        textPaint.setTextAlign(Paint.Align.CENTER);

        operatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        operatorPaint.setColor(0xFF323232);
        operatorPaint.setTextAlign(Paint.Align.CENTER);

        inputBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        inputBgPaint.setColor(0xFFE6E6FA);
        inputBgPaint.setStyle(Paint.Style.FILL);

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(0xFF6464C8);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4);

        activeBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        activeBorderPaint.setColor(0xFF0066CC);
        activeBorderPaint.setStyle(Paint.Style.STROKE);
        activeBorderPaint.setStrokeWidth(8);

        checkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        checkPaint.setColor(0xFF228B22);
        checkPaint.setStyle(Paint.Style.STROKE);
        checkPaint.setStrokeWidth(36);
        checkPaint.setStrokeCap(Paint.Cap.ROUND);
        checkPaint.setStrokeJoin(Paint.Join.ROUND);

        xPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        xPaint.setColor(0xFFDC143C);
        xPaint.setStyle(Paint.Style.STROKE);
        xPaint.setStrokeWidth(36);
        xPaint.setStrokeCap(Paint.Cap.ROUND);
        xPaint.setStrokeJoin(Paint.Join.ROUND);
    }

    public void setProblem(MathProblem problem) {
        cancelTimer();
        this.problem = problem;
        this.answerText = "";
        this.scale = 1.0f;
        this.bgColor = 0xFFFFFFFF;
        bgPaint.setColor(bgColor);
        invalidate();
    }

    public void setActive(boolean active) {
        this.isActive = active;
        invalidate();
    }

    public void appendDigit(int digit) {
        if (answerText.length() < 4) {
            answerText += digit;
            startTimer();
            invalidate();
        }
    }

    public void deleteDigit() {
        if (!answerText.isEmpty()) {
            answerText = answerText.substring(0, answerText.length() - 1);
            startTimer();
            invalidate();
        }
    }

    public void startTimer() {
        if (timerHandler == null || timerListener == null) return;
        if (isAnimating || answerText.isEmpty()) return;
        
        if (timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
        
        timerRunnable = () -> {
            if (timerListener != null && !isAnimating && !answerText.isEmpty()) {
                timerListener.onTimerExpired(this);
            }
        };
        
        timerHandler.postDelayed(timerRunnable, INPUT_DELAY_MS);
    }

    public void cancelTimer() {
        if (timerHandler != null && timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }

    public String getAnswerText() {
        return answerText;
    }

    public void clearAnswer() {
        answerText = "";
        wasCorrect = false;
        showCheck = false;
        checkScale = 0f;
        showX = false;
        xScale = 0f;
        invalidate();
    }

    private boolean wasCorrect = false;

    public boolean checkAnswerAndGetResult() {
        if (problem == null || answerText.isEmpty()) {
            return false;
        }

        try {
            int answer = Integer.parseInt(answerText);
            wasCorrect = (answer == problem.result);

            if (wasCorrect) {
                animateCorrect();
            } else {
                animateWrong();
            }
            return wasCorrect;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean shouldGenerateNew() {
        return answerText.isEmpty() && wasCorrect;
    }

    private void animateCorrect() {
        isAnimating = true;
        bgColor = correctColor;
        bgPaint.setColor(bgColor);
        scale = 1.3f;
        showCheck = true;
        checkScale = 0f;
        invalidate();
        
        ValueAnimator scaleAnim = ValueAnimator.ofFloat(1.3f, 1.0f);
        scaleAnim.setDuration(300);
        scaleAnim.addUpdateListener(animation -> {
            scale = (float) animation.getAnimatedValue();
            invalidate();
        });
        scaleAnim.start();

        ValueAnimator colorAnim = ValueAnimator.ofArgb(correctColor, 0xFFFFFFFF);
        colorAnim.setStartDelay(300);
        colorAnim.setDuration(300);
        colorAnim.addUpdateListener(animation -> {
            bgColor = (int) animation.getAnimatedValue();
            bgPaint.setColor(bgColor);
            invalidate();
        });
        colorAnim.start();

        ValueAnimator scaleUp = ValueAnimator.ofFloat(0f, 2f);
        scaleUp.setDuration(500);
        scaleUp.addUpdateListener(animation -> {
            checkScale = (float) animation.getAnimatedValue();
            invalidate();
        });

        ValueAnimator scaleDown = ValueAnimator.ofFloat(2f, 0f);
        scaleDown.setDuration(500);
        scaleDown.setStartDelay(500);
        scaleDown.addUpdateListener(animation -> {
            checkScale = (float) animation.getAnimatedValue();
            invalidate();
        });

        scaleDown.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                showCheck = false;
                checkScale = 0f;
                isAnimating = false;
            }
        });

        scaleUp.start();
        scaleDown.start();
    }

    private void animateWrong() {
        isAnimating = true;
        bgColor = wrongColor;
        bgPaint.setColor(bgColor);
        scale = 0.7f;
        showX = true;
        xScale = 0f;
        invalidate();

        ValueAnimator scaleAnim = ValueAnimator.ofFloat(0.7f, 1.0f);
        scaleAnim.setDuration(300);
        scaleAnim.addUpdateListener(animation -> {
            scale = (float) animation.getAnimatedValue();
            invalidate();
        });
        scaleAnim.start();

        ValueAnimator xScaleUp = ValueAnimator.ofFloat(0f, 2f);
        xScaleUp.setDuration(500);
        xScaleUp.addUpdateListener(animation -> {
            xScale = (float) animation.getAnimatedValue();
            invalidate();
        });

        ValueAnimator xScaleDown = ValueAnimator.ofFloat(2f, 0f);
        xScaleDown.setDuration(500);
        xScaleDown.setStartDelay(500);
        xScaleDown.addUpdateListener(animation -> {
            xScale = (float) animation.getAnimatedValue();
            invalidate();
        });

        xScaleDown.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                showX = false;
                xScale = 0f;
                answerText = "";
                bgColor = 0xFFFFFFFF;
                bgPaint.setColor(bgColor);
                invalidate();
                isAnimating = false;
            }
        });

        xScaleUp.start();
        xScaleDown.start();
    }

    private void drawCheck(Canvas canvas, float centerX, float centerY, float size) {
        if (checkScale <= 0f) return;

        float s = size * checkScale;
        float alpha = Math.min(1f, checkScale) * 255;
        checkPaint.setAlpha((int) alpha);
        checkPaint.setStrokeWidth(28 * checkScale);

        Path path = new Path();
        path.moveTo(centerX - s * 0.15f, centerY + s * 0.05f);
        path.lineTo(centerX - s * 0.02f, centerY + s * 0.2f);
        path.lineTo(centerX + s * 0.18f, centerY - s * 0.15f);
        
        canvas.drawPath(path, checkPaint);
    }

    private void drawX(Canvas canvas, float centerX, float centerY, float size) {
        if (xScale <= 0f) return;

        float s = size * xScale;
        float alpha = Math.min(1f, xScale) * 255;
        xPaint.setAlpha((int) alpha);
        xPaint.setStrokeWidth(28 * xScale);

        canvas.drawLine(centerX - s * 0.15f, centerY - s * 0.15f, centerX + s * 0.15f, centerY + s * 0.15f, xPaint);
        canvas.drawLine(centerX - s * 0.15f, centerY + s * 0.15f, centerX + s * 0.15f, centerY - s * 0.15f, xPaint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) return;

        canvas.save();
        float cx = width / 2f;
        float cy = height / 2f;
        canvas.scale(scale, scale, cx, cy);

        RectF rect = new RectF(0, 0, width, height);
        bgPaint.setColor(isActive ? activeBgColor : bgColor);
        canvas.drawRoundRect(rect, 16, 16, bgPaint);

        if (isActive) {
            canvas.drawRoundRect(rect, 16, 16, activeBorderPaint);
        }

        if (problem == null) {
            canvas.restore();
            return;
        }

        float textSize = Math.min(width, height) * 0.5f;
        textPaint.setTextSize(textSize);
        operatorPaint.setTextSize(textSize);

        float centerY = height / 2f + textSize / 3f;
        
        String num1 = String.valueOf(problem.num1);
        String op = String.valueOf(problem.operation);
        String num2 = String.valueOf(problem.num2);
        String eq = getContext().getString(R.string.operator_equals);
        String answer = answerText.isEmpty() ? getContext().getString(R.string.placeholder_underscore) : answerText;

        float x1 = width * 0.12f;
        float xOp = width * 0.30f;
        float x2 = width * 0.42f;
        float xEq = width * 0.54f;
        float xAns = width * 0.78f;

        canvas.drawText(num1, x1, centerY, textPaint);
        canvas.drawText(op, xOp, centerY, operatorPaint);
        canvas.drawText(num2, x2, centerY, textPaint);
        canvas.drawText(eq, xEq, centerY, operatorPaint);

        float inputWidth = width * 0.35f;
        float inputLeft = xAns - inputWidth / 2;
        RectF inputRect = new RectF(inputLeft, height * 0.15f, inputLeft + inputWidth, height * 0.85f);
        canvas.drawRoundRect(inputRect, 8, 8, inputBgPaint);

        if (isActive) {
            activeBorderPaint.setStrokeWidth(5);
            canvas.drawRoundRect(inputRect, 8, 8, activeBorderPaint);
        }

        canvas.drawText(answer, xAns, centerY, textPaint);

        if (showCheck) {
            float inputCenterX = xAns;
            float inputCenterY = (height * 0.15f + height * 0.85f) / 2f;
            float checkSize = inputRect.height() * 1.6f;
            drawCheck(canvas, inputCenterX, inputCenterY, checkSize);
        }

        if (showX) {
            float inputCenterX = xAns;
            float inputCenterY = (height * 0.15f + height * 0.85f) / 2f;
            float xSize = inputRect.height() * 1.6f;
            drawX(canvas, inputCenterX, inputCenterY, xSize);
        }

        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            performClick();
            if (clickListener != null) {
                clickListener.onProblemClicked(this);
            }
            return true;
        }
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    public void setOnProblemClickListener(OnProblemClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnTimerExpiredListener(OnTimerExpiredListener listener) {
        this.timerListener = listener;
    }

    public MathProblem getProblem() {
        return problem;
    }
}
