package com.example.cloud.fmoddemo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.example.cloud.fmoddemo.bean.MusicBean;
import com.example.cloud.fmoddemo.customview.ProgressTextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Created by cloud on 2019/4/12.
 */

public class ListAdater extends BaseAdapter {
    List<MusicBean> data;
    HashSet<Long> ids = new HashSet<>();

    public ListAdater(List<MusicBean> data) {
        this.data = data;
        for (MusicBean musicBean : data) {
            ids.add(musicBean.getId());
        }
    }

    public ListAdater() {
        data=new ArrayList<>();
    }

    public List<MusicBean> getData() {
        return data;
    }
    public boolean addMusic(MusicBean bean){
        if(ids.contains(bean.getId())){
            return false;
        }
        ids.add(bean.getId());
        data.add(bean);
        notifyDataSetChanged();
        return true;
    }
    public boolean removeMusic(long id){
        if(ids.remove(id)){
            Iterator<MusicBean> iterator = data.iterator();
            while (iterator.hasNext()){
                MusicBean next = iterator.next();
                if(next.getId()==id){
                    data.remove(next);
                    notifyDataSetChanged();
                    return true;
                }
            }
        }
        return false;
    }

    public void setData(ArrayList<MusicBean> data) {
        this.data = data;
    }


    @Override
    public int getCount() {
        return data==null?0:data.size();
    }

    @Override
    public MusicBean getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return data.get(position).getMusicPath().hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null){
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem,null);
        }
        ProgressTextView textview = (ProgressTextView)convertView;
        textview.setText(data.get(position).getMusicName());
        textview.setBean(data.get(position));
        return convertView;
    }

}
