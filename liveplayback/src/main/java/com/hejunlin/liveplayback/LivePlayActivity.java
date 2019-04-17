

package com.hejunlin.liveplayback;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.hejunlin.liveplayback.vo.HideLoadingEvent;
import com.hejunlin.liveplayback.vo.ShowLoadingEvent;
import com.hejunlin.liveplayback.vo.ShowOrHideChannleViewEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class LivePlayActivity extends FragmentActivity {

    private RelativeLayout mVideoViewLayout;
    private ProgressBar progressBar;
    private ChannelListFragment channelListFragment;
    private PlayerFragment playerFragment;
    private ChannelsParser channelsParser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_play);
        mVideoViewLayout = findViewById(R.id.videoPlayContainer);
        progressBar = findViewById(R.id.loadingView);

        EventBus.getDefault().register(this);

        final FragmentManager fragmentManager = LivePlayActivity.this.getSupportFragmentManager();

        channelListFragment = new ChannelListFragment();
        fragmentManager.beginTransaction().replace(R.id.channelListContainer, channelListFragment).hide(channelListFragment).commitAllowingStateLoss();

        playerFragment = PlayerFragment.newInstance("", "");
        fragmentManager.beginTransaction().replace(R.id.videoPlayContainer, playerFragment).show(playerFragment).commitAllowingStateLoss();

        channelsParser = new ChannelsParser().setRootUrl("http://m.iptv203.com/?tid=hbitv").process(new AsyncCallback<ArrayList>() {
            @Override
            public void onFinished(final ArrayList data) {
                LivePlayActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        channelListFragment.setData(data);
                        fragmentManager.beginTransaction().show(channelListFragment).commitAllowingStateLoss();
                    }
                });
            }

            @Override
            public void onError() {

            }
        });

        mVideoViewLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                FragmentManager fragmentManager = LivePlayActivity.this.getSupportFragmentManager();
                fragmentManager.beginTransaction().show(channelListFragment).commitAllowingStateLoss();
                return true;
            }
        });
        //new CachePrepareManager().setVideoUrl(mVideoUrl).process();

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showLoadingView(ShowLoadingEvent event) {
        this.progressBar.setVisibility(View.VISIBLE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void hideLoadingView(HideLoadingEvent event) {
        this.progressBar.setVisibility(View.GONE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showOrHideChannelView(ShowOrHideChannleViewEvent event) {
        if (channelListFragment.isHidden()) {
            this.getSupportFragmentManager().beginTransaction().show(channelListFragment).commitAllowingStateLoss();
        } else {
            this.getSupportFragmentManager().beginTransaction().hide(channelListFragment).commitAllowingStateLoss();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
