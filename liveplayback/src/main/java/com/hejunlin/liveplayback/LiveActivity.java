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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import media.IjkVideoView;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class LiveActivity extends FragmentActivity {

    private IjkVideoView mVideoView;
    private RelativeLayout mVideoViewLayout;
    private RelativeLayout mLoadingLayout;
    private TextView mLoadingText;
    private TextView mTextClock;
    private String mVideoUrl = "";
    private int mRetryTimes = 0;
    private static final int CONNECTION_TIMES = 5;
    private ChannelListFragment channelListFragment;
    private ChannelsParser channelsParser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live);
        mVideoUrl = getIntent().getStringExtra("url");
        mVideoView = (IjkVideoView) findViewById(R.id.videoview);
        mVideoViewLayout = (RelativeLayout) findViewById(R.id.fl_videoview);
        mLoadingLayout = (RelativeLayout) findViewById(R.id.rl_loading);
        mLoadingText = (TextView) findViewById(R.id.tv_load_msg);
        mTextClock = (TextView)findViewById(R.id.tv_time);
        mTextClock.setText(getDateFormate());
        mLoadingText.setText("节目加载中...");

        initVideo();


        final FragmentManager fragmentManager=LiveActivity.this.getSupportFragmentManager();
          channelListFragment=new ChannelListFragment();
        fragmentManager.beginTransaction().replace(R.id.channelListContainer,channelListFragment).hide(channelListFragment).commitAllowingStateLoss();

        channelsParser=new ChannelsParser().setRootUrl("http://m.iptv203.com/?tid=hbitv").process(new AsyncCallback<ArrayList>() {
            @Override
            public void onFinished(final ArrayList data) {
                LiveActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        channelListFragment.setData(data);
                        mLoadingLayout.setVisibility(View.GONE);
                        fragmentManager.beginTransaction().show(channelListFragment).commitAllowingStateLoss();

                    }
                });
            }

            @Override
            public void onError() {

            }
        });

        mVideoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                FragmentManager fragmentManager=LiveActivity.this.getSupportFragmentManager();
                fragmentManager.beginTransaction().show(channelListFragment).commitAllowingStateLoss();
                return true;
            }
        });
        //new CachePrepareManager().setVideoUrl(mVideoUrl).process();
    }

    private String getDateFormate(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }

    public void initVideo() {
        // init player
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!mVideoView.isBackgroundPlayEnabled()) {
            mVideoView.stopPlayback();
            mVideoView.release(true);
            mVideoView.stopBackgroundPlay();
        }
        IjkMediaPlayer.native_profileEnd();
    }



    public void play(ChannelVO channelVO) {
        FragmentManager fragmentManager=LiveActivity.this.getSupportFragmentManager();
        fragmentManager.beginTransaction().hide(channelListFragment).commitAllowingStateLoss();

        channelsParser.url(channelVO.url, new AsyncCallback<String>() {
            @Override
            public void onFinished(final String data) {
                LiveActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        playUrl(data);
                    }
                });
            }

            @Override
            public void onError() {

            }
        });

    }

    private void playUrl(String data) {
        //HttpProxyCacheServer proxy = MyApp.getProxy(this);
        //mVideoUrl = proxy.getProxyUrl(mVideoUrl);

        //onStop();

//        initVideo();

        mVideoView.setVideoURI(Uri.parse(data));
        mLoadingLayout.setVisibility(View.VISIBLE);

        mVideoView.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer mp) {
                mVideoView.start();
            }
        });

        mVideoView.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(IMediaPlayer mp, int what, int extra) {
                switch (what) {
                    case IjkMediaPlayer.MEDIA_INFO_BUFFERING_START:
                        mLoadingLayout.setVisibility(View.VISIBLE);
                        break;
                    case IjkMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                    case IjkMediaPlayer.MEDIA_INFO_BUFFERING_END:
                        mLoadingLayout.setVisibility(View.GONE);
                        break;
                }
                return false;
            }
        });

        mVideoView.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer mp) {
                mLoadingLayout.setVisibility(View.VISIBLE);
                mVideoView.stopPlayback();
                mVideoView.release(true);
                mVideoView.setVideoURI(Uri.parse(mVideoUrl));
            }
        });

        mVideoView.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer mp, int what, int extra) {
                if (mRetryTimes > CONNECTION_TIMES) {
                    new AlertDialog.Builder(LiveActivity.this)
                            .setMessage("节目暂时不能播放")
                            .setPositiveButton("确定",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            LiveActivity.this.finish();
                                        }
                                    })
                            .setCancelable(false)
                            .show();
                } else {
                    mVideoView.stopPlayback();
                    mVideoView.release(true);
                    mVideoView.setVideoURI(Uri.parse(mVideoUrl));
                }
                return false;
            }
        });
    }
}
