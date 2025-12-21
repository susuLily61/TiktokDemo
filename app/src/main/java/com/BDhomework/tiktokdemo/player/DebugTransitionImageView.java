package com.BDhomework.tiktokdemo.player;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.BDhomework.tiktokdemo.R;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

public class DebugTransitionImageView extends AppCompatImageView {

    private Integer lastVisibility = null;
    private Float lastAlpha = null;

    public DebugTransitionImageView(Context context) {
        super(context);
    }

    public DebugTransitionImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DebugTransitionImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setVisibility(int visibility) {
        if (getId() == com.BDhomework.tiktokdemo.R.id.video_transition_cover) {

            if (lastVisibility == null || lastVisibility != visibility) {
                lastVisibility = visibility;

                if (visibility == View.VISIBLE) {
                    Log.e(
                            "TCOVER_TRACE",
                            "❌ setVisibility(VISIBLE) 被调用！调用栈如下：\n"
                                    + Log.getStackTraceString(new Throwable())
                    );
                } else {
                    Log.d("TCOVER_TRACE", "setVisibility(" + visibility + ")");
                }
            }
        }
        super.setVisibility(visibility);
    }

    @Override
    public void setAlpha(float alpha) {
        if (getId() == com.BDhomework.tiktokdemo.R.id.video_transition_cover) {

            float rounded = Math.round(alpha * 1000f) / 1000f;

            if (lastAlpha == null || Math.abs(lastAlpha - rounded) > 0.001f) {
                lastAlpha = rounded;

                if (rounded >= 0.99f) {
                    Log.e(
                            "TCOVER_TRACE",
                            "❌ setAlpha(" + rounded + ") 被调用！调用栈如下：\n"
                                    + Log.getStackTraceString(new Throwable())
                    );
                }
            }
        }
        super.setAlpha(alpha);
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);

        if (getId() == com.BDhomework.tiktokdemo.R.id.video_transition_cover
                && changedView == this
                && visibility == View.VISIBLE) {

            Log.e(
                    "TCOVER_TRACE",
                    "❌ onVisibilityChanged -> VISIBLE，调用栈：\n"
                            + Log.getStackTraceString(new Throwable())
            );
        }
    }
}