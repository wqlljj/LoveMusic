package com.example.cloud.fmoddemo.bean;

import android.util.Log;

import com.example.cloud.fmoddemo.customview.ProgressTextView;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.io.Serializable;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Transient;

/**
 * Created by cloud on 2019/4/12.
 */
@Entity
public class MusicBean implements Serializable {
    @Transient
    public static int PLAYFINISH=0;
    @Transient
    public static int PLAYSTART=1;
    @Transient
    public static int PLAYPAUSE=2;
    @Id()
    long id;
    public String musicPath;
    public String singer;
    public String musicName;
    int current;
    int len;
    @Transient
    int playState = 0;
    @Transient
    ProgressTextView.Listener listener;



    public MusicBean(String musicPath, String singer, String musicName) {
        id = musicPath.hashCode();
        this.musicPath = musicPath;
        this.singer = singer;
        this.musicName = musicName;
    }


    @Generated(hash = 1216488654)
    public MusicBean(long id, String musicPath, String singer, String musicName,
            int current, int len) {
        this.id = id;
        this.musicPath = musicPath;
        this.singer = singer;
        this.musicName = musicName;
        this.current = current;
        this.len = len;
    }


    @Generated(hash = 1899243370)
    public MusicBean() {
    }
    

    public ProgressTextView.Listener getListener() {
        return listener;
    }

    public void setListener(ProgressTextView.Listener listener) {
        this.listener = listener;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }
    public String getMusicPath() {
        return musicPath;
    }

    public void setMusicPath(String musicPath) {
        this.musicPath = musicPath;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getMusicName() {
        return musicName;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }

    @Override
    public String toString() {
        return "MusicBean{" +
                "id=" + id +
                ", musicPath='" + musicPath + '\'' +
                ", singer='" + singer + '\'' +
                ", musicName='" + musicName + '\'' +
                ", current=" + current +
                ", len=" + len +
                ", playState=" + playState +
                ", listener=" + listener +
                '}';
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getPlayState() {
        return playState;
    }

    public void setPlayState(int playState) {
        this.playState = playState;
        if(playState == PLAYSTART&&listener!=null){
            listener.change();
        }
    }


}
