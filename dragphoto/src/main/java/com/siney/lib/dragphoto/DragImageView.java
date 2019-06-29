package com.siney.lib.dragphoto;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

/**
 * author: siney
 * Date: 2019/4/25
 * description:
 */
public class DragImageView extends AppCompatImageView {

    public static final int IMAGE_PLACEHOLDER = 0, USE = 1;

    private int number;//照片序号，按照顺时针写

    private String path;

    public int imageType;

    private int width, height;

    private float circleWidth = 30;

    private Path p;

    private Paint pen;

    public DragImageView(Context context) {
        super(context);
    }

    public DragImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setImage(String path, int imageType){
        this.path = path;
        this.imageType = imageType;
        setBackgroundColor(Color.TRANSPARENT);
        Glide.with(getContext()).load(path).centerCrop().into(this);
    }

    public void setImage(int resourceId, int imageType){
        this.imageType = imageType;
        setBackgroundColor(Color.parseColor("#F7F7F7"));
        Glide.with(getContext()).load(resourceId).centerCrop().into(this);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        width = getMeasuredWidth();
        height = getMeasuredHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(imageType != IMAGE_PLACEHOLDER){
            if(p == null)p = new Path();
            int width = getWidth();
            int height = getHeight();
            p.reset();
            p.moveTo(circleWidth, 0);
            p.lineTo(width - circleWidth, 0);
            p.quadTo(width, 0, width, circleWidth);
            p.lineTo(width, height - circleWidth);
            p.quadTo(width, height, width - circleWidth, height);
            p.lineTo(circleWidth, height);
            p.quadTo(0, height, 0, height - circleWidth);
            p.lineTo(0, circleWidth);
            p.quadTo(0, 0, circleWidth, 0);
            canvas.clipPath(p);
            super.onDraw(canvas);
        }else{
            super.onDraw(canvas);
        }
    }
}
