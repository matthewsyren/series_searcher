package com.matthewsyren.seriessearcher.customviews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.matthewsyren.seriessearcher.R;

public class RoundedImageView extends AppCompatImageView {
    //Declarations
    private Path mRoundedPath;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int mBorderRadius = 0;

    /**
     * Constructor
     */
    public RoundedImageView(Context context) {
        super(context);
        setUpView(context);
    }

    /**
     * Constructor
     */
    public RoundedImageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setUpView(context);
    }

    /**
     * Constructor
     */
    public RoundedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setUpView(context);
    }

    /**
     * Sets up the components needed to draw the View
     */
    private void setUpView(Context context) {
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mBorderRadius = (int) context.getResources().getDimension(R.dimen.list_row_image_border_radius);
    }

    /**
     * Generates the rounded Path
     */
    private void generateRoundedPath(int w, int h) {
        mRoundedPath = new Path();
        mRoundedPath.addRoundRect(new RectF(0,0, w, h), mBorderRadius, mBorderRadius, Path.Direction.CW);
        mRoundedPath.setFillType(Path.FillType.INVERSE_WINDING);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);

        //Generates the rounded Path for the new View size
        if (w != oldW || h != oldH) {
            generateRoundedPath(w, h);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //Makes the Canvas transparent if it is not already
        if(canvas.isOpaque()) {
            canvas.saveLayerAlpha(0, 0, getWidth(), getHeight(), 255, Canvas.ALL_SAVE_FLAG);
        }

        super.onDraw(canvas);

        //Draws the View
        if(mRoundedPath != null) {
            canvas.drawPath(mRoundedPath, mPaint);
        }
    }
}