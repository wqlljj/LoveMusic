package com.example.cloud.fmoddemo;

import android.Manifest;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.File;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private int[] ids;
    private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Resources resources = getResources();
        ids = resources.getIntArray(R.array.example_ids);
        String[] example_name = resources.getStringArray(R.array.example_name);
        LinearLayout content = (LinearLayout) findViewById(R.id.content);
        Button[] buttons = new Button[ids.length];
        LinearLayout.LayoutParams lpLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        for (int i = 0; i < buttons.length; i++)
        {
            buttons[i] = new Button(this);
            buttons[i].setText(example_name[i]);
            buttons[i].setOnClickListener(this);
            buttons[i].setId(ids[i]);
            content.addView(buttons[i],lpLayout);
        }
        String path = Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + "kgmusic"
                + File.separator + "download";
        Log.d("TEST", "onCreate: "+path);
        File file = new File(path);
        String[] list = file.list();
        if(list!=null){
            for (int i = 0; i < 10; i++) {
                Log.d("TEST", "onCreate: "+list[i]);
            }
        }else{
            Log.e("TEST", "onCreate: list==null" );
        }
    }

    @Override
    public void onClick(View v) {
        String text = ((Button) v).getText().toString();
        Log.e(TAG, "onClick: "+text );
        switch (text){
            case "musicplayer":
                Intent intent = new Intent(this, MusicPlayerActivity.class);
                startActivity(intent);
                break;
            default:
                Intent intent1 = new Intent(this, ExampleActivity.class);
                intent1.putExtra("example_name",text);
                startActivity(intent1);
                break;
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        MainActivityPermissionsDispatcher.needPerWithPermissionCheck(this);
    }

    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void needPer() {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnShowRationale({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void onShowRational(final PermissionRequest request) {
    }

    @OnPermissionDenied({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void onPermissionDenied() {
    }
}
