package com.example.cloud.fmoddemo.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;
import android.util.Log;

import com.example.cloud.fmoddemo.R;

/**
 * Created by cloud on 2019/4/18.
 */

public class CustomImageButon extends AppCompatImageButton {

    private int imageLevel = 0;
    private int maxState = 0;
    private String TAG = "CustomImageButon";

    public CustomImageButon(Context context) {
        super(context);
        Log.e(TAG, "CustomImageButon: ()" );
    }

    public CustomImageButon(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.e(TAG, "CustomImageButon: (c,a)" );
        TypedArray array=context.obtainStyledAttributes(attrs, R.styleable.CustomImageButon);
        if(array.hasValue(R.styleable.CustomImageButon_maxState)) {
            maxState = array.getInt(R.styleable.CustomImageButon_maxState, 0);
        }
        if(array.hasValue(R.styleable.CustomImageButon_imageLevel)) {
            imageLevel = array.getInt(R.styleable.CustomImageButon_imageLevel, 0);
            setCImageLevel(imageLevel);
        }
    }

    public CustomImageButon(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Log.e(TAG, "CustomImageButon: c,a,d" );
        TypedArray array=context.obtainStyledAttributes(attrs, R.styleable.CustomImageButon);
        maxState=array.getInt(R.styleable.CustomImageButon_maxState,0);
        imageLevel=array.getInt(R.styleable.CustomImageButon_imageLevel,0);
    }

    public int getCImageLevel() {
        return imageLevel;
    }

    public void setCImageLevel(int imageLevel) {
        imageLevel = (imageLevel%maxState);
        this.imageLevel = imageLevel;
        Log.d(TAG, "setCImageLevel: "+imageLevel);
        final int finalImageLevel = imageLevel;
        post(new Runnable() {
            @Override
            public void run() {
                setImageLevel(finalImageLevel);
            }
        });

    }

    public int getMaxState() {
        return maxState;
    }

    public void setMaxState(int maxState) {
        this.maxState = maxState;
    }
}
