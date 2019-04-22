package com.example.cloud.fmoddemo;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.IdRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public  class FmodActivity extends AppCompatActivity implements Runnable{
    public TextView mTxtScreen;
    private Thread mThread;
    private String TAG ="FmodActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            String[] perms = { "android.permission.RECORD_AUDIO", "android.permission.WRITE_EXTERNAL_STORAGE" };
            if (checkSelfPermission(perms[0]) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(perms[1]) == PackageManager.PERMISSION_DENIED)
            {
                requestPermissions(perms, 200);
            }
        }

        org.fmod.FMOD.init(this);

        mThread = new Thread(this, "Example Main");
        mThread.start();
        Log.d(TAG, "onCreate: ");
        setStateCreate();
    }
    @Override
    protected void onStart()
    {
        super.onStart();
        Log.d(TAG, "onStart: ");
        setStateStart();
    }
    @Override
    protected void onStop()
    {
        Log.d(TAG, "onStop: ");
        setStateStop();
        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        Log.d(TAG, "onDestroy: ");
        setStateDestroy();

        try
        {
            mThread.join();
        }
        catch (InterruptedException e) { }

        org.fmod.FMOD.close();
        Log.d(TAG, "onDestroy:1 ");
        super.onDestroy();
    }


    @Override
    public void run()
    {
        String example_name = getIntent().getStringExtra("example_name");
        if(TextUtils.isEmpty(example_name)){
            example_name = getResources().getStringArray(R.array.example_name)[0];
        }
//        main();
        mainExample(example_name);
    }
    public void setShowMessageView(TextView view){
        mTxtScreen = view;
    }
    public void toast(final String msg){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(FmodActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void getJniData(int key,String data){
        Log.d(TAG, "getJniData: "+key+"  "+data);
    }

    public int getStringHash(String data){
        return data.hashCode();
    }
    public void updateScreen(final String text)
    {
        String.format("",1);
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if(mTxtScreen!=null) {
                    mTxtScreen.setText(text);
                }else{
                    Toast.makeText(FmodActivity.this, "Show Message View == NULL", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public native String getButtonLabel(int index);
    public native void buttonDown(int index);
    public native void buttonUp(int index);
    public native void setStateCreate();
    public native void setStateStart();
    public native void setStateStop();
    public native void setStateDestroy();
    public native void mainExample(String example_name);
    public native void useEffect(int type,String param);

    static
    {
        System.loadLibrary("fmod");
        System.loadLibrary("fmodL");
//        System.loadLibrary("voicechange");
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        hide();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hide();
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    public int getBtn_Index(@IdRes int id){
        switch (id){
            case R.id.a:
                return 0;
            case R.id.b:
                return 1;
            case R.id.c:
                return 2;
            case R.id.d:
                return 3;
            case R.id.left:
                return 4;
            case R.id.right:
                return 5;
            case R.id.up:
                return 6;
            case R.id.down:
                return 7;
            case R.id.e:
                return 8;
            default:
                return 9;
        }
    }
}
