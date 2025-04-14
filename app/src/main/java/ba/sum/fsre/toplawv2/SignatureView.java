package ba.sum.fsre.toplawv2;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class SignatureView extends View {
    private Path path = new Path();
    private Paint paint = new Paint();

    public SignatureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(5f);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(path, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX(), y = e.getY();
        if (e.getAction() == MotionEvent.ACTION_DOWN) path.moveTo(x, y);
        else if (e.getAction() == MotionEvent.ACTION_MOVE) path.lineTo(x, y);
        invalidate();
        return true;
    }

    public void clear() {
        path.reset();
        invalidate();
    }

    public Bitmap getSignatureBitmap() {
        Bitmap signature = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(signature);
        draw(canvas);
        return signature;
    }
}
