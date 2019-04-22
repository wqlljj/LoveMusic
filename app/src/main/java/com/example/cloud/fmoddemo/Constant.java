package com.example.cloud.fmoddemo;

import android.os.Environment;

/**
 * Created by cloud on 2019/4/12.
 */

public class Constant {
    public static String MusicListFilePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/fmoddemo/musiclist";
    public static final int MODE_NORMAL = 0;
    public static final int MODE_GAOGUAI1 =1;
    public static final int MODE_LUOLI =2;
    public static final int MODE_DASHU =3;
    public static final int MODE_JINGSONG =4;
    public static final int MODE_KONGLING =5;
    public static final int MODE_GAOGUAI2 =6;
    public static final float EFFECT_RANGE[][] ={{},{1.0f,3.0f},
            {1f,3f},{0.5f,1f},{0.5f,1f},{1f,1000f,1f,50f},{0.5f,1f}};
    public static final String KEY_MODE[] = {
            "MODE_NORMAL","MODE_GAOGUAI1","MODE_LUOLI","MODE_DASHU",
            "MODE_JINGSONG","MODE_KONGLING","MODE_GAOGUAI2"
    };
    public static final String KEY_USE_EFFECT = "KEY_USE_EFFECT";
    public static final String KEY_PLAYMODE= "KEY_PLAYMODE";
    public static final String KEY_RECORD = "KEY_RECORD";//保存上次销毁前播放歌曲
}
