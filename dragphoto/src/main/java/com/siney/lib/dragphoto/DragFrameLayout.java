package com.siney.lib.dragphoto;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * author: siney
 * Date: 2019/4/25
 * description:
 */
public class DragFrameLayout extends FrameLayout {

    public interface OnDragLayoutListener{
        void onFinish(List<DragImageView> images);
        void onImageClick(DragImageView image);
        boolean allowDrag(DragImageView image);
        boolean allowExchange(DragImageView dragImage, DragImageView exchangeImage);
    }

    public DragFrameLayout(@NonNull Context context) {
        super(context);
        setClickable(true);
    }

    public DragFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
        setClickable(true);
    }

    public DragFrameLayout( Context context,  AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initParams(attrs);
    }

    private void initParams(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.DragFrameLayout);
        placeholder = a.getResourceId(R.styleable.DragFrameLayout_placeholder, R.drawable.ic_add);
        gap = a.getDimensionPixelSize(R.styleable.DragFrameLayout_gap, (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()));
    }

    private class Param{
        public int width, x, y;
    }

    private static final int DEFAULT = 1, STARTANIM = 2, ZOOMOUT = 3, FINISHANIM = 4;

    private int placeholder, gap, width, height;

    private List<DragImageView> list = new ArrayList<>();

    private List<Param> params = new ArrayList<>();

    private int smallW = Integer.MAX_VALUE, largeW, dragW;

    private float lastX, lastY;

    private boolean isFinish;

    private int dragStatus = DEFAULT;//拖拽状态

    private DragImageView target;

    private long startTime;

    private OnDragLayoutListener listener;

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if(!isFinish){
            isFinish = true;
            initImages();
            init();
        }
    }

    private void initImages() {
        width = getWidth() - getPaddingStart() - getPaddingEnd();
        int rest = this.width - 3 * gap;
        largeW = 2 * (rest - gap) / 3 + gap;
        smallW = rest - largeW;
        dragW = (int) (smallW * 0.8);
        height = smallW + largeW + gap;
        setViews();
        //开始初始化
        setParams(largeW, gap, 0, 0);
        setParams(smallW, 2 * gap + largeW, 0, 1);
        setParams(smallW, 2 * gap + largeW, gap + smallW, 2);
        setParams(smallW, 2 * gap + largeW, 2 * (gap + smallW), 3);
        setParams(smallW, 2 * gap + smallW, 2 * (gap + smallW), 4);
        setParams(smallW, gap, 2 * (gap + smallW), 5);
    }

    private void setViews() {
        int childCount = getChildCount();
        for(int i = 0;i < childCount;i++){
            View v = getChildAt(i);
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams)v.getLayoutParams();
            lp.setMargins(lp.leftMargin, height + lp.topMargin, lp.rightMargin, lp.bottomMargin);
            v.setLayoutParams(lp);
        }
    }

    //传入所有图片地址，进行初始化
    public void init() {
        post(new Runnable() {
            @Override
            public void run() {
                if(listener != null){
                    listener.onFinish(list);
                }
            }
        });
    }

    private void setParams(int width, int left, int top, int num) {
        Param param = new Param();
        param.width = width;
        param.x = left;
        param.y = top;
        params.add(param);
        DragImageView image = new DragImageView(getContext());
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(width, width);
        image.setLayoutParams(lp);
        image.setNumber(num);
        image.setTranslationX(left);
        image.setTranslationY(top);
        image.setImage(placeholder, DragImageView.IMAGE_PLACEHOLDER);
        list.add(image);
        addView(image);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        float x = ev.getX();
        float y = ev.getY();
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                lastX = x;
                lastY = y;
                if(dragStatus == DEFAULT){
                    target = identifyImage(x, y);
                }
                startTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_MOVE:
                //对象有，而且缩小动画已经结束
                if(target != null){
                    if(dragStatus == DEFAULT){
                        boolean res = true;
                        if(listener != null){
                            res = listener.allowDrag(target);
                        }
                        if(res){
                            dragStatus = STARTANIM;
                            anim();
                        }
                    }else if(dragStatus == STARTANIM){
                        lastX = x;
                        lastY = y;
                    }else if(dragStatus == ZOOMOUT){
                        moveDragImage(x, y);
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if(dragStatus == DEFAULT && target != null && System.currentTimeMillis() - startTime <= 200){
                    //单击图片事件
                    if(listener != null){
                        listener.onImageClick(target);
                    }
                }else if(dragStatus == ZOOMOUT || dragStatus == STARTANIM){
                    //如果是缩小状态了，或者正在开始动画状态，都允许取消
                    dragStatus = FINISHANIM;
                    if(target != null){
                        moveImages(x, y);
                    }
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    //识别点击处是否有图像
    private DragImageView identifyImage(float x, float y) {
        for(DragImageView image : list){
            Param param = params.get(image.getNumber());
            int x1 = param.x;
            int x2 = param.x + param.width;
            int y1 = param.y;
            int y2 = param.y + param.width;
            if(x >= x1 && x <= x2 && y >= y1 && y <= y2){
                return image;
            }
        }
        return null;
    }

    //移除当前图像
    public void removeImage(int number){
        int start = number + 1, end = 0;
        for(int i = number + 1;i < list.size();i++){
            DragImageView view = list.get(i);
            if(view.imageType == DragImageView.IMAGE_PLACEHOLDER){
                end = i - 1;
                break;
            }else if(i == list.size() - 1){
                end = list.size() - 1;
            }
        }
        ValueAnimator[] anim = new ValueAnimator[end - start + 2];
        int pos = 0;
        for(int i = start;i <= end;i++){
            anim[pos++] = replace(list.get(i), params.get(i - 1), i - 1);
        }
        list.get(number).setImage(placeholder, DragImageView.IMAGE_PLACEHOLDER);
        anim[pos] = replace(list.get(number), params.get(end), end);
        setAnimSetFinish(anim);
        sortList();
    }

    //设为大头图像
    public void setMainImage(int number){
        int start = number, end = 0;
        ValueAnimator[] anim = new ValueAnimator[start - end + 1];
        int pos = 0;
        for(int i = start - 1;i >= end;i--){
            anim[pos++] = replace(list.get(i), params.get(i + 1), i + 1);
        }
        anim[pos] = replace(list.get(number), params.get(end), end);
        setAnimSetFinish(anim);
        sortList();
    }

    //移动图像
    private void moveDragImage(float x, float y) {
        float gapx = x - dragW / 2;
        float gapy = y - dragW  /2;
        target.setTranslationX(gapx);
        target.setTranslationY(gapy);
    }

    private void moveImages(float x, float y) {
        DragImageView image = identifyImage(x, y);
        if(image != null && image != target){
            //如果不是本身image 并且不为空，说明需要替换
            boolean res = true;
            if(listener != null)
                res = listener.allowExchange(target, image);
            if(!res){
                setAnimFinish(replace(target, params.get(target.getNumber()), target.getNumber()));
                return;
            }
            int start = target.getNumber();
            int end = image.getNumber();
            if(start < end){
                ValueAnimator[] anim = new ValueAnimator[end - start + 1];
                int pos = 0;
                for(int i = start + 1;i <= end;i++){
                    anim[pos++] = replace(list.get(i), params.get(i - 1), i - 1);
                }
                anim[pos] = replace(target, params.get(end), end);
                setAnimSetFinish(anim);
            }else if(start > end){
                ValueAnimator[] anim = new ValueAnimator[start - end + 1];
                int pos = 0;
                for(int i = start - 1;i >= end;i--){
                    anim[pos++] = replace(list.get(i), params.get(i + 1), i + 1);
                }
                anim[pos] = replace(target, params.get(end), end);
                setAnimSetFinish(anim);
            }
            sortList();
        }else{
            setAnimFinish(replace(target, params.get(target.getNumber()), target.getNumber()));
        }
    }

    private void setAnimFinish(ValueAnimator anim) {
        anim.setDuration(200);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                dragStatus = DEFAULT;
                target = null;
            }
        });
        anim.start();
    }

    private void setAnimSetFinish(ValueAnimator[] anim) {
        AnimatorSet set = new AnimatorSet();
        set.setDuration(200);
        set.playTogether(anim);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                dragStatus = DEFAULT;
                target = null;
            }
        });
        set.start();
    }

    private ValueAnimator replace(final DragImageView source, final Param dest, int num){
        ValueAnimator anim = ValueAnimator.ofFloat(0, 1f);
        final ViewGroup.LayoutParams lp = source.getLayoutParams();
        final int width = lp.width;
        final float startX = source.getTranslationX();
        final float startY = source.getTranslationY();
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = (float) animation.getAnimatedValue();
                lp.width = lp.height = (int) (width + (dest.width - width) * v);
                source.setLayoutParams(lp);
                float endX = startX + (dest.x - startX) * v;
                float endY = startY + (dest.y - startY) * v;
                source.setTranslationX(endX);
                source.setTranslationY(endY);
            }
        });
        source.setNumber(num);
        return anim;
    }

    private void sortList() {
        Collections.sort(list, new Comparator<DragImageView>() {
            @Override
            public int compare(DragImageView o1, DragImageView o2) {
                return Integer.compare(o1.getNumber(), o2.getNumber());
            }
        });
    }

    //开始动画
    private void anim() {
        target.bringToFront();
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1f);
        animator.setDuration(200);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = (float) animation.getAnimatedValue();
                ViewGroup.LayoutParams lp = target.getLayoutParams();
                Param param = params.get(target.getNumber());
                lp.width = lp.height = (int) (param.width - (param.width - dragW) * v);
                target.setLayoutParams(lp);
                float endX = lastX - dragW / 2;
                float endY = lastY - dragW / 2;
                float xx = endX - param.x;
                float yy = endY - param.y;
                target.setTranslationX(param.x + xx * v);
                target.setTranslationY(param.y + yy * v);
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if(dragStatus != FINISHANIM)
                    dragStatus = ZOOMOUT;
            }
        });
        animator.start();
    }

    public OnDragLayoutListener getListener() {
        return listener;
    }

    public void setListener(OnDragLayoutListener listener) {
        this.listener = listener;
    }

    public DragImageView getMainImage() {
        return list.get(0);
    }
}
