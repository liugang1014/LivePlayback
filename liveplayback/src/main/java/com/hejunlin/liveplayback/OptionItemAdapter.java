/*
 * Copyright (C) 2016 hejunlin <hejunlin2013@gmail.com>
 * 
 * Github:https://github.com/hejunlin2013/LivePlayback
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hejunlin.liveplayback;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by hejunlin on 2015/10/28.
 * blog: http://blog.csdn.net/hejjunlin
 */
public class OptionItemAdapter extends RecyclerView.Adapter<OptionItemAdapter.ViewHolder> {

    // 数据集
//    private String[] mDataList = new String[] {
//            "湖南卫视HD","湖南卫视FHD","Test Qiniu FHD"
//    };
//
//    private String [] mUrlList = new String[]{
//            "http://api.iptv888.com/play.m3u8?token=urREjHmdRXicrT%2B6r%2F0WfQwG&tid=hbitv&id=15",
//            "http://223.110.241.203:6610/gitv/live1/G_HUNAN-CQ/G_HUNNA-CQ/1.m3u8?IASHttpSessionId=OTT2054620190412053529016349",
//            "http://poyelz9e8.bkt.clouddn.com/c001_1555049601_1555049611.ts"
//    };

    private ArrayList<ChannelVO> voArrayList;
    private Context mContext;
    private int id;
    private View.OnFocusChangeListener mOnFocusChangeListener;
    private OnBindListener onBindListener;
    private static final String TAG = OptionItemAdapter.class.getSimpleName();

    private View.OnClickListener onClickListener;
    public void setOnItemClickListener(View.OnClickListener onClickListener) {
        this.onClickListener=onClickListener;
    }

    public interface OnBindListener {
        void onBind(View view, int i);
    }

    public OptionItemAdapter(Context context) {
        super();
        mContext = context;
    }

    public OptionItemAdapter(Context context, int id) {
        super();
        mContext = context;
        this.id = id;
    }

    public OptionItemAdapter(Context context, int id, ArrayList data) {
        super();
        mContext = context;
        this.id = id;
        this.voArrayList=data;
        //this.mOnFocusChangeListener = onFocusChangeListener;
    }

    public void setOnBindListener(OnBindListener onBindListener) {
        this.onBindListener = onBindListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        int resId = R.layout.detail_menu_item;
        if (this.id > 0) {
            resId = this.id;
        }
        View view = LayoutInflater.from(mContext).inflate(resId, viewGroup, false);
        ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int i) {
        if ( voArrayList==null) {
            Log.d(TAG, "mDataset has no data!");
            return;
        }
        viewHolder.mTextView.setText(voArrayList.get(i).name);
        viewHolder.itemView.setTag(i);
        viewHolder.itemView.setOnFocusChangeListener(mOnFocusChangeListener);
        viewHolder.itemView.setOnClickListener(this.onClickListener);
        if (onBindListener != null) {
            onBindListener.onBind(viewHolder.itemView, i);
        }
    }

    @Override
    public int getItemCount() {
        return voArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.tv_menu_title);
        }
    }

}
