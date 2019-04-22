package com.example.cloud.fmoddemo;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.cloud.fmoddemo.bean.MusicBean;
import com.example.cloud.fmoddemo.customview.CustomImageButon;
import com.example.cloud.fmoddemo.greendao.DBManager;
import com.leon.lfilepickerlibrary.LFilePicker;

import java.io.File;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

import static com.example.cloud.fmoddemo.Constant.EFFECT_RANGE;
import static com.example.cloud.fmoddemo.Constant.KEY_MODE;
import static com.example.cloud.fmoddemo.Constant.KEY_PLAYMODE;
import static com.example.cloud.fmoddemo.Constant.KEY_RECORD;
import static com.example.cloud.fmoddemo.Constant.KEY_USE_EFFECT;
import static com.example.cloud.fmoddemo.Constant.MODE_DASHU;
import static com.example.cloud.fmoddemo.Constant.MODE_GAOGUAI1;
import static com.example.cloud.fmoddemo.Constant.MODE_GAOGUAI2;
import static com.example.cloud.fmoddemo.Constant.MODE_JINGSONG;
import static com.example.cloud.fmoddemo.Constant.MODE_KONGLING;
import static com.example.cloud.fmoddemo.Constant.MODE_LUOLI;
import static com.example.cloud.fmoddemo.Constant.MODE_NORMAL;
import static com.example.cloud.fmoddemo.bean.MusicBean.PLAYFINISH;
import static com.example.cloud.fmoddemo.bean.MusicBean.PLAYSTART;

@RuntimePermissions
public class MusicPlayerActivity extends FmodActivity implements View.OnTouchListener, View.OnClickListener, AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener, RadioGroup.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener {
    private TextView message;
    private ListView musicList;
    private TextView musicPathTv;
    private int chooseReqCode = 1000;
    private String TAG = "MusicPlayerActivity";
    private SharedPreferences sharedPreferences;
    private DBManager dbManager;
    private ListAdater adapter;
    private CustomImageButon add;
    private CustomImageButon delete;
    private TextView msg_param1_tv;
    private TextView msg_param2_tv;
    private SeekBar param1_bar;
    private SeekBar param2_bar;
    private int effectIndex;
    private View setContentView;
    private RadioGroup effect_rg;
    private MusicBean playMusic;
    private CustomImageButon playbt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
        findViewById(R.id.up).setOnTouchListener(this);
        playbt = ((CustomImageButon) findViewById(R.id.e));
        playbt.setOnTouchListener(this);
        findViewById(R.id.down).setOnTouchListener(this);
        findViewById(R.id.d).setOnTouchListener(this);
        add = (CustomImageButon) findViewById(R.id.add);
        delete = (CustomImageButon) findViewById(R.id.delete);
        add.setOnClickListener(this);
        delete.setOnClickListener(this);
        add.setEnabled(false);
        delete.setEnabled(false);
        setContentView = findViewById(R.id.content_set);
        musicPathTv = ((TextView) findViewById(R.id.selectFile));
        musicPathTv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.e(TAG, "afterTextChanged: "+s.length() );
                if(s.length()==0){
                    add.setEnabled(false);
                    delete.setEnabled(false);
                    Log.d(TAG, "afterTextChanged: setCImageLevel" +10 );
                }else if(!add.isEnabled()){
                    add.setEnabled(true);
                    delete.setEnabled(true);
                }
            }
        });
        musicPathTv.setOnClickListener(this);
        findViewById(R.id.showMsg).setOnClickListener(this);
        message = ((TextView) findViewById(R.id.message));
        setShowMessageView(message);
        musicList = ((ListView) findViewById(R.id.musicList));
        findViewById(R.id.set).setOnClickListener(this);
        msg_param1_tv = ((TextView) findViewById(R.id.msg_param1));
        msg_param2_tv = ((TextView) findViewById(R.id.msg_param2));
        param1_bar = ((SeekBar) findViewById(R.id.param1));
        param2_bar = ((SeekBar) findViewById(R.id.param2));
        param1_bar.setOnSeekBarChangeListener(this);
        param2_bar.setOnSeekBarChangeListener(this);
        effect_rg = ((RadioGroup) findViewById(R.id.radioG_effect));
        effect_rg.setOnCheckedChangeListener(this);
        sharedPreferences = getSharedPreferences("SharedPreferences",MODE_PRIVATE);
        initData();
        musicList.setAdapter(adapter);
        musicList.setOnItemLongClickListener(this);
        musicList.setOnItemClickListener(this);
    }
    private void initData() {
        int effectIndex = sharedPreferences.getInt(KEY_USE_EFFECT, 0);
        String param = sharedPreferences.getString(KEY_MODE[effectIndex], "");
        Log.d(TAG, "initData: "+effectIndex+"  "+param);
        if(!param.equals("")){
            useEffect(effectIndex,param);
        }
        boolean playMode = sharedPreferences.getBoolean(KEY_PLAYMODE, true);
        Log.d(TAG, "initData: "+effectIndex+"  "+param);
        setPlayMode(playMode);
        ((CustomImageButon) findViewById(R.id.d)).setCImageLevel(playMode?0:2);
        dbManager = DBManager.getInstance(this);
        List<MusicBean> musicBeen = dbManager.queryBeanList(MusicBean.class);
        Log.d(TAG, "initData: musicBeen.size = "+musicBeen.size());
        adapter = new ListAdater(musicBeen);
        String record = sharedPreferences.getString(KEY_RECORD, "");
        if(!record.equals("")||adapter.getCount()>0){
            for (int i = 0; i < adapter.getCount(); i++) {
                if(adapter.getItem(i).getMusicPath().equals(record)){
                    playMusic = adapter.getItem(i);
                    break;
                }
            }
        }
        if(playMusic==null&&adapter.getCount()>0){
            playMusic = adapter.getItem(0);
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent)
    {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
        {
            Log.d(TAG, "onTouch: ACTION_DOWN "+ getBtn_Index(view.getId()));
            buttonDown(getBtn_Index(view.getId()));
            if(view instanceof CustomImageButon){
                CustomImageButon imageButton = (CustomImageButon) view;
                Log.d(TAG, "onTouch: setCImageLevel = "+imageButton.getCImageLevel());
                if(view.getId()!=R.id.e) {
                    imageButton.setCImageLevel(imageButton.getCImageLevel() + 1);
                }
            }
        }
        else if (motionEvent.getAction() == MotionEvent.ACTION_UP)
        {
            if(view instanceof CustomImageButon){
                CustomImageButon imageButton = (CustomImageButon) view;
                Log.d(TAG, "onTouch: setCImageLevel = "+imageButton.getCImageLevel()+"  "+imageButton.getMaxState());
                if(view.getId()!=R.id.e) {
                    imageButton.setCImageLevel(imageButton.getCImageLevel() + 1);
                }
            }
            onClick(view);
            Log.d(TAG, "onTouch: ACTION_UP "+ getBtn_Index(view.getId()));
            buttonUp(getBtn_Index(view.getId()));
        }
        return true;
    }

    @Override
    public void getJniData(int key, String data) {
//        Log.d(TAG, "getJniData: "+key+"    "+data);
        switch (key){
            case 100:
                String[] split = data.split(":");
                int id = Integer.valueOf(split[0]);
                int current = Integer.valueOf(split[1]);
                int len = Integer.valueOf(split[2]);
                    playMusic.setCurrent(current);
                    playMusic.setLen(len);
                if(playMusic.getPlayState()!=PLAYSTART){
                    playMusic.setPlayState(PLAYSTART);
                }
                break;
            case 101:
                playMusic.setCurrent(0);
                playMusic.setPlayState(PLAYFINISH);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.selectFile:
                chooseFile();
                break;
            case R.id.e:
                if(playMusic!=null&&playMusic.getPlayState()==PLAYFINISH){
                    playSong(playMusic.musicPath);
                }
                break;
            case R.id.showMsg:
                if(message.getVisibility()==View.GONE) {
                    message.setVisibility(View.VISIBLE);
                }else{
                    message.setVisibility(View.GONE);
                }
                ((TextView) v).setText(message.getVisibility()==View.GONE?"帮助信息":"隐藏信息");
                break;
            case R.id.add:
                for (String musicPath : listPath) {
                    String name = musicPath.substring(musicPath.lastIndexOf('/')+1,musicPath.length()-4);
                    MusicBean bean = new MusicBean(musicPath, "singer", name);
//                    dbManager.insertBean(bean);
                    if(adapter.addMusic(bean)) {
                        dbManager.insertBean(bean);
                    }else{
                        toast("重复添加！");
                    }
                }
                if(playMusic==null&&adapter.getCount()>0){
                    playMusic = adapter.getItem(0);
                }
                musicPathTv.setText("");
                break;
            case R.id.delete:
                String path = musicPathTv.getText().toString();
                if(adapter.removeMusic(path.hashCode())) {
                    dbManager.deleteBeanByKey(MusicBean.class, (long)path.hashCode());
                }else{
                    toast("歌单为查询到此歌曲！");
                }
                musicPathTv.setText("");
                break;
            case R.id.down:
                int index = -1;
                for (int i = 0; i < adapter.getCount(); i++) {
                    if(adapter.getItem(i).getMusicPath().equals(playMusic.getMusicPath())){
                        index = i;
                        playMusic.setCurrent(0);
                        adapter.getItem(i).setPlayState(PLAYFINISH);
                        break;
                    }
                }
                index = ((index+1)%adapter.getCount());
                    playMusic = adapter.getItem(index);
//                    playMusic.setPlayState(PLAYSTART);
                    playSong(playMusic.musicPath);
                break;
            case R.id.up:
                int index1 = -1;
                for (int i = 0; i < adapter.getCount(); i++) {
                    if(adapter.getItem(i).getMusicPath().equals(playMusic.getMusicPath())){
                        index1 = i;
                        playMusic.setCurrent(0);
                        adapter.getItem(i).setPlayState(PLAYFINISH);
                        break;
                    }
                }
                index1 = (index1-1+adapter.getCount())%adapter.getCount();
                    playMusic = adapter.getItem(index1);
//                    playMusic.setPlayState(PLAYSTART);
                    playSong(playMusic.musicPath);
                break;
            case R.id.set:
                if(setContentView.getVisibility()==View.GONE) {
                    setContentView.setVisibility(View.VISIBLE);
                    effectIndex=getUseEffectIndex();
                    switch (effectIndex) {
                        case MODE_NORMAL:
                            ((RadioButton) findViewById(R.id.mode_normal)).setChecked(true);
                            break;
                        case MODE_GAOGUAI1:
                            ((RadioButton) findViewById(R.id.mode_gaoguai1)).setChecked(true);
                            break;
                        case MODE_LUOLI:
                            ((RadioButton) findViewById(R.id.mode_luoli)).setChecked(true);
                            break;
                        case MODE_DASHU:
                            ((RadioButton) findViewById(R.id.mode_dashu)).setChecked(true);
                            break;
                        case MODE_JINGSONG:
                            ((RadioButton) findViewById(R.id.mode_jingsong)).setChecked(true);
                            break;
                        case MODE_KONGLING:
                            ((RadioButton) findViewById(R.id.mode_kongling)).setChecked(true);
                            break;
                        case MODE_GAOGUAI2:
                            ((RadioButton) findViewById(R.id.mode_gaoguai2)).setChecked(true);
                            break;
                    }
                }else{
//                    findViewById(R.id.middle).setVisibility(View.VISIBLE);
                    setContentView.setVisibility(View.GONE);
                }
                break;
        }
    }
    List<String> listPath;
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == chooseReqCode){
            if (data==null || !data.hasExtra("paths")){
                return;
            }
            listPath = data.getStringArrayListExtra("paths");
            if (listPath.size()>0){
                String path = listPath.get(0);
                for (String s : listPath) {
                    Log.d(TAG, "onActivityResult: "+s);
                }
                Log.d(TAG, "onActivityResult: path = "+path);
                musicPathTv.setText(path);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    private void chooseFile(){
        String path = Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + "kgmusic"
                + File.separator + "download";
        if(!new File(path).exists()){
            path = Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        LFilePicker filePicker = new LFilePicker()
                .withActivity(this)
                .withRequestCode(chooseReqCode)
                .withStartPath(path)
                .withTitle("选择歌曲")
                .withFileFilter(new String[]{".mp3"});
        filePicker.start();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemLongClick: "+adapter.getItem(position).musicPath);
        musicPathTv.setText(adapter.getItem(position).getMusicPath());
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Log.d(TAG, "onItemClick: "+adapter.getItem(i).musicPath);
        playSong(adapter.getItem(i).musicPath);
        playMusic =  adapter.getItem(i);
//        playMusic.setIsPlay(true);
    }
    void initSet(int id){
        if(id == R.id.mode_normal){
            msg_param2_tv.setVisibility(View.GONE);
            param2_bar.setVisibility(View.GONE);
            msg_param1_tv.setVisibility(View.GONE);
            param1_bar.setVisibility(View.GONE);
            return;
        }
        msg_param1_tv.setVisibility(View.VISIBLE);
        param1_bar.setVisibility(View.VISIBLE);
        if(id==R.id.mode_kongling){
            msg_param2_tv.setVisibility(View.VISIBLE);
            param2_bar.setVisibility(View.VISIBLE);
            String[] split = getEffect(effectIndex).split(":");
            Log.d(TAG, "initSet: "+split[0]+"  "+EFFECT_RANGE[effectIndex][1]+"  "+Float.valueOf(split[1])+"   "+EFFECT_RANGE[effectIndex][3]);
            param1_bar.setProgress((int)convertPercent(EFFECT_RANGE[effectIndex][0],EFFECT_RANGE[effectIndex][1],Float.valueOf(split[0])));
            param2_bar.setProgress((int)convertPercent(EFFECT_RANGE[effectIndex][2],EFFECT_RANGE[effectIndex][3],Float.valueOf(split[1])));
            Log.d(TAG, "initSet: "+param1_bar.getProgress()+"  "+param2_bar.getProgress());
        }else{

            param1_bar.setProgress((int)convertPercent(EFFECT_RANGE[effectIndex][0],EFFECT_RANGE[effectIndex][1],Float.valueOf(getEffect(effectIndex))));
            Log.d(TAG, "initSet: "+param1_bar.getProgress());
            msg_param2_tv.setVisibility(View.GONE);
            param2_bar.setVisibility(View.GONE);
        }
        setMsg();
    }
    void setMsg(){
        switch (effectIndex){
            case MODE_GAOGUAI1:
                msg_param1_tv.setText(String.format("加快倍数(%.2f - %.2f)   当前值：%s",EFFECT_RANGE[1][0],EFFECT_RANGE[1][1],getEffect(MODE_GAOGUAI1)));
                break;
            case MODE_LUOLI:
                msg_param1_tv.setText(String.format("音调(%.2f - %.2f)   当前值：%s",EFFECT_RANGE[2][0],EFFECT_RANGE[2][1],getEffect(MODE_LUOLI)));
                break;
            case MODE_DASHU:
                msg_param1_tv.setText(String.format("音调(%.2f - %.2f)   当前值：%s",EFFECT_RANGE[3][0],EFFECT_RANGE[3][1],getEffect(MODE_DASHU)));
                break;
            case MODE_JINGSONG:
                msg_param1_tv.setText(String.format("抖动(%.2f - %.2f)   当前值：%s",EFFECT_RANGE[4][0],EFFECT_RANGE[4][1],getEffect(MODE_JINGSONG)));
                break;
            case MODE_KONGLING:
                String[] split = getEffect(MODE_KONGLING).split(":");
                msg_param1_tv.setText(String.format("重复时延（%.2f - %.2f）   当前值：%s",EFFECT_RANGE[5][0],EFFECT_RANGE[5][1],split[0]));
                msg_param2_tv.setText(String.format("重复次数(%.2f - %.2f)   当前值：%s",EFFECT_RANGE[5][2],EFFECT_RANGE[5][3],split[1]));
                break;
            case MODE_GAOGUAI2:
                msg_param1_tv.setText(String.format("减慢倍数(%.2f - %.2f)   当前值：%s",EFFECT_RANGE[6][0],EFFECT_RANGE[6][1],getEffect(MODE_GAOGUAI2)));
                break;
        }
    }



    @Override
    public void onCheckedChanged(RadioGroup radioGroup, @IdRes int id) {
        int index=0;
        switch (id){
            case R.id.mode_gaoguai1:
                index = 1;
                break;
            case R.id.mode_luoli:
                index = 2;
                break;
            case R.id.mode_dashu:
                index = 3;
                break;
            case R.id.mode_jingsong:
                index = 4;
                break;
            case R.id.mode_kongling:
                index = 5;
                break;
            case R.id.mode_gaoguai2:
                index = 6;
                break;
            default:
                index = 0;
                break;
        }
        effectIndex = index;
        String param = sharedPreferences.getString(KEY_MODE[effectIndex], "");
        useEffect(index,param.equals("")?getEffect(effectIndex):param);
        initSet(id);
    }

    @Override
    protected void onStop() {
        super.onStop();
        int useEffectIndex = getUseEffectIndex();
        Log.e(TAG, "onStop: "+ useEffectIndex +"  "+getEffect(useEffectIndex)+"  "+getPlayMode() );
        sharedPreferences.edit().putInt(KEY_USE_EFFECT, useEffectIndex).apply();
        sharedPreferences.edit().putString(KEY_MODE[useEffectIndex],getEffect(useEffectIndex)).apply();
        sharedPreferences.edit().putBoolean(KEY_PLAYMODE,getPlayMode()).apply();
        if(playMusic!=null) {
            sharedPreferences.edit().putString(KEY_RECORD, playMusic.musicPath).apply();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
        if(!b)return;
        Log.e(TAG, "onProgressChanged: "+progress+"    "+b );
        String result ;
        int progress1 = param1_bar.getProgress();
        result = ""+ convertValue(EFFECT_RANGE[effectIndex][0],EFFECT_RANGE[effectIndex][1],progress1);
        if(effectIndex==MODE_KONGLING) {
            int progress2 = param2_bar.getProgress();
            result+=":"+ convertValue(EFFECT_RANGE[effectIndex][2],EFFECT_RANGE[effectIndex][3],progress2);
        }
        Log.d(TAG, "onProgressChanged: "+result);
        useEffect(effectIndex,result);
        initSet(effect_rg.getCheckedRadioButtonId());
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        String result ;
        int progress1 = param1_bar.getProgress();
        result = ""+convertValue(EFFECT_RANGE[effectIndex][0],EFFECT_RANGE[effectIndex][1],progress1);
        if(effectIndex==MODE_KONGLING) {
            int progress2 = param2_bar.getProgress();
            result+=":"+convertValue(EFFECT_RANGE[effectIndex][2],EFFECT_RANGE[effectIndex][3],progress2);
        }
        Log.d(TAG, "onProgressChanged: "+effectIndex+"  "+result);
        sharedPreferences.edit().putString(KEY_MODE[effectIndex],result).apply();
    }

    private float convertValue(float min, float max, int progress){
        return min+(max-min)*progress/100;
    }
    private float convertPercent(float min, float max, float value){
        return (value-min)/(max-min)*100;
    }
    public native int playSong(String musicPath);
    public native String getEffect(int mode);
    public native int getUseEffectIndex();
    public native boolean getPlayMode();
    public native void setPlayMode(boolean playMode);
    void onPlayFinish(int id,int event){//返回ID
        Log.d(TAG, "onPlayFinish: "+id);
        int index = -1;
        for (int i = 0; i < adapter.getCount(); i++) {
            if(adapter.getItemId(i)==id){
                index = (i+1)%adapter.getCount();
                adapter.getItem(i).setCurrent(0);
                adapter.getItem(i).setPlayState(PLAYFINISH);
            }
        }
        if(event==0) {
            playMusic = adapter.getItem(index);
//            playMusic.setIsPlay(true);
            playSong(adapter.getItem(index).musicPath);
        }
    }
    void onPlayFailed(String msg){
        if(playbt.getCImageLevel()!=3&&playbt.getCImageLevel()!=0) {
            Log.d(TAG, "onPlayFailed: setCImageLevel "+msg);
            playbt.setCImageLevel(0);
        }
    }
    void onPlayStart(int id){
        if(playbt.getCImageLevel()!=1&&playbt.getCImageLevel()!=2) {
            Log.d(TAG, "onPlayStart: setCImageLevel "+id);
            playbt.setCImageLevel(2);
        }
    }
    void onPlayPause(int id){
        if(playbt.getCImageLevel()!=3&&playbt.getCImageLevel()!=0) {
            Log.d(TAG, "onPlayPause: setCImageLevel "+id);
            playbt.setCImageLevel(0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MusicPlayerActivityPermissionsDispatcher.needPerWithPermissionCheck(this);
    }

    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void needPer() {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MusicPlayerActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnShowRationale({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void onShowRational(final PermissionRequest request) {
    }
}
