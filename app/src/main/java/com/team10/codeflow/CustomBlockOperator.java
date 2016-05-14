package com.team10.codeflow;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * This is our Custom View for the operators seen in pseudocode blocks
 * Operators include: = , += , -= , > , >= , < , <= , == , !=
 * Created by tristan on 10/05/2016.
 */
public class CustomBlockOperator extends TextView {

    private Paint paint;
    private Paint textPaint;
    private int size;
    private String currentOperator;

    private boolean showSelections;

    /**
     * Constructor for Custom View
     * @param context
     */
    public CustomBlockOperator(Context context) {
        super(context);
        init();
    }
    public CustomBlockOperator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public CustomBlockOperator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        paint = new Paint();            //Paint for background circle
        textPaint = new Paint();        //Paint for text 'operator'


        paint.setColor(Color.argb(90,250,250,250)); //background circle colour

        textPaint.setColor(Color.BLACK);            //operator text colour
        textPaint.setTextSize(50);                 //operator text size
        textPaint.setTextAlign(Paint.Align.CENTER);

        currentOperator = "=";
        showSelections = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //draw circle at centre, with diameter of view size
        canvas.drawCircle(size/2,size/2,size/2,paint);

        //get centre for text
        int xPos = (canvas.getWidth() / 2);
        int yPos = (int) ((canvas.getHeight() / 2) - ((textPaint.descent() + textPaint.ascent()) / 2)) ;

        //draw text with size/1.5
        textPaint.setTextSize( size/(float)1.5 ); //operator text size
        canvas.drawText(currentOperator,xPos,yPos,textPaint);

    }

    //Find size applied to custom view
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        size = 0;
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        if (width > height) {
            size = height;
        }
        else {
            size = width;
        }
    }

    public void newOperator(String newOp) {
        currentOperator = newOp;
        invalidate();
    }

}
