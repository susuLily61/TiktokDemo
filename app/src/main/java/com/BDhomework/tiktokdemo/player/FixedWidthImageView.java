package com.BDhomework.tiktokdemo.player;

public class FixedWidthImageView extends androidx.appcompat.widget.AppCompatImageView {

    private final android.graphics.Matrix matrix = new android.graphics.Matrix();

    public FixedWidthImageView(android.content.Context c) { super(c); init(); }
    public FixedWidthImageView(android.content.Context c, android.util.AttributeSet a) { super(c, a); init(); }
    public FixedWidthImageView(android.content.Context c, android.util.AttributeSet a, int s) { super(c, a, s); init(); }

    private void init() {
        setScaleType(ScaleType.MATRIX);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateMatrix();
    }

    @Override
    public void setImageDrawable(android.graphics.drawable.Drawable drawable) {
        super.setImageDrawable(drawable);
        updateMatrix();
    }

    private void updateMatrix() {
        android.graphics.drawable.Drawable d = getDrawable();
        if (d == null) return;

        int vw = getWidth();
        int vh = getHeight();
        int dw = d.getIntrinsicWidth();
        int dh = d.getIntrinsicHeight();
        if (vw <= 0 || vh <= 0 || dw <= 0 || dh <= 0) return;

        // 固定宽度缩放：宽度铺满
        float scale = vw * 1f / dw;
        float scaledH = dh * scale;

        float dy = (vh - scaledH) / 2f; // scaledH > vh 时 dy 为负 => 上下裁剪；scaledH < vh 时 dy 为正 => 上下留黑边
        matrix.reset();
        matrix.setScale(scale, scale);
        matrix.postTranslate(0f, dy);

        setImageMatrix(matrix);
    }
}
