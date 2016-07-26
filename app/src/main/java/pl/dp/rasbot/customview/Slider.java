package pl.dp.rasbot.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Project4You S.C. on 02.05.15.
 * Author: Dawid Podolak
 * Email: dawidpod1@gmail.com
 * All rights reserved!
 */
public class Slider extends View implements View.OnTouchListener {

    public static final String TAG = "Slider";
    private static final int MAX_VALUE = 100;
    private Context context;

    private Rect sliderRect;
    private float dp;
    private int centerSlider = -1;
    private int centerView = -1;
    private int padding;
    private int maxOffsetMargin;

    private OnSliderValueChanged onSliderValueChanged;

    public Slider(Context context) {
        super(context);
        init(context);
    }

    public Slider(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public Slider(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){
        this.context = context;
        dp = context.getResources().getDisplayMetrics().density;

        padding = (int) (10*dp);
        maxOffsetMargin = (int) (50*dp);
        sliderRect = new Rect();

        setOnTouchListener(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int centerX = canvas.getWidth()/2;
        int centerY = canvas.getHeight()/2;

        if (centerSlider == -1){
            centerSlider = centerY;
            centerView = centerY;
        }
        Paint paint = new Paint();
        paint.setColor(Color.GRAY);
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);

        Paint sliderPaint = new Paint();
        sliderPaint.setColor(Color.BLUE);
        sliderRect.set(0 + padding,
                (int)(centerSlider-(30*dp) + padding),
                canvas.getWidth()-padding,
                (int)(centerSlider+(30*dp) - padding));

//        sliderRect.set(0, 33, canvas.getWidth(), (centerY );
        canvas.drawRect(sliderRect, sliderPaint);

    }

    public void setOnSliderValueChanged(OnSliderValueChanged onSliderValueChanged) {
        this.onSliderValueChanged = onSliderValueChanged;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        int topOffset = maxOffsetMargin;
        int bottomOffset = view.getHeight()-maxOffsetMargin;

        switch (motionEvent.getAction()){
            case MotionEvent.ACTION_DOWN: {
                calculateSliderCenter(motionEvent, topOffset, bottomOffset);


            }break;

            case MotionEvent.ACTION_MOVE: {
                calculateSliderCenter(motionEvent, topOffset, bottomOffset);

            }break;

            case MotionEvent.ACTION_UP:
                centerSlider = getHeight()/2;
                break;
        }

        int sValue = calculateSliderValue(topOffset, bottomOffset);
        Log.d(TAG, "slider value: " + sValue);
        if (onSliderValueChanged!=null)
            onSliderValueChanged.onSliderValueChanged(sValue);
        invalidate();
        return true;
    }

    private void calculateSliderCenter(MotionEvent motionEvent, int topOffset, int bottomOffset) {
        int y = (int) motionEvent.getY();

        if (y > topOffset && y < bottomOffset) {
            centerSlider = y;
        } else if (y < topOffset) {
            centerSlider = topOffset;
        } else if (y > bottomOffset) {
            centerSlider = bottomOffset;
        }
    }

    private int calculateSliderValue(int topOffset, int bottomOffset){

        if (centerView == centerSlider)
            return 0;
        else if (centerSlider<centerView){
            int difference = centerView-centerSlider;
            return (difference * MAX_VALUE)/(centerView-topOffset);
        }else if (centerSlider>centerView){

            int difference = centerView - centerSlider;
            return (difference * MAX_VALUE)/(bottomOffset-centerView);
        }

        return 0;
    }

    public interface OnSliderValueChanged {
        public void onSliderValueChanged(int value);
    }
}
