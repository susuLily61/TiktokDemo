package com.BDhomework.tiktokdemo.ai;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.BDhomework.tiktokdemo.R;

public class DraggableFloatingBall extends AppCompatImageView {

    private float downRawX, downRawY;
    private float downX, downY;
    private int touchSlop;
    private boolean moved;

    public DraggableFloatingBall(Context context) {
        super(context);
        init();
    }

    public DraggableFloatingBall(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DraggableFloatingBall(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setClickable(true);
        setFocusable(true);

        setBackgroundResource(R.drawable.bg_ai_ball);

        setImageResource(R.drawable.ic_doubao);

        setScaleType(ScaleType.FIT_CENTER);

        int p = dp(10);
        setPadding(p, p, p, p);

        setElevation(dp(12));
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        View parent = (View) getParent();
        if (parent == null) return super.onTouchEvent(event);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                moved = false;
                downRawX = event.getRawX();
                downRawY = event.getRawY();
                downX = getX();
                downY = getY();
                return true;

            case MotionEvent.ACTION_MOVE: {
                float dx = event.getRawX() - downRawX;
                float dy = event.getRawY() - downRawY;

                if (!moved && (Math.abs(dx) > touchSlop || Math.abs(dy) > touchSlop)) {
                    moved = true;
                }

                float newX = downX + dx;
                float newY = downY + dy;

                // 限制在父布局内
                newX = Math.max(0, Math.min(newX, parent.getWidth() - getWidth()));
                newY = Math.max(0, Math.min(newY, parent.getHeight() - getHeight()));

                setX(newX);
                setY(newY);
                return true;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (!moved) {
                    performClick();
                }
                return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
