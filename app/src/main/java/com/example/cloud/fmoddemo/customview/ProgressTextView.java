package com.example.cloud.fmoddemo.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.Log;

import com.example.cloud.fmoddemo.bean.MusicBean;

import static com.example.cloud.fmoddemo.bean.MusicBean.PLAYSTART;

/**
 * Created by cloud on 2019/4/12.
 */

public class ProgressTextView extends AppCompatTextView implements Runnable {
    private  Paint progressPaint;
    MusicBean bean;
    private String TAG = "ProgressTextView";
    Listener listener=new Listener() {
        @Override
        public void change() {
            if(bean.getPlayState()==PLAYSTART){
                post(ProgressTextView.this);
            }
        }
    };

    public ProgressTextView(Context context) {
        super(context);
        //进度条画笔
        init();
    }

    public ProgressTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        //进度条画笔
        init();
    }

    public ProgressTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();

    }

    private void init() {
        //进度条画笔
        progressPaint = new Paint();
        //设置抗锯齿
        progressPaint.setAntiAlias(true);
        progressPaint.setColor(Color.parseColor("#aa26a4f6"));
        progressPaint.setStrokeWidth(2);
    }

    public MusicBean getBean() {
        return bean;
    }

    public void setBean(MusicBean bean) {
        this.bean = bean;
        bean.setListener(listener);
        if(bean.getPlayState()==PLAYSTART){
            listener.change();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.e(TAG, "onDraw: bean.isPlay "+bean.getPlayState()+"  "+bean.getMusicName()  );
        if(bean.getPlayState()==PLAYSTART) {
            int width = getWidth();
            int height = getHeight();
            progressPaint.setStyle(Paint.Style.FILL);
            RectF rect = new RectF(0,0,(float)( width*(1.0*bean.getCurrent()/bean.getLen())),height);
            Log.d(TAG, "onDraw: bean.isPlay "+(float)( width*(1.0*bean.getCurrent()/bean.getLen())));
            canvas.drawRect(rect,progressPaint);
        }
        super.onDraw(canvas);
    }


    @Override
    public void run() {
        Log.d(TAG, "run: 请求刷新  "+bean.getMusicName());
        requestLayout();
        invalidate();
        if(bean.getPlayState()==PLAYSTART) {
            postDelayed(this, 1000);
        }
    }

    public interface Listener{
        void change();
    }
}
