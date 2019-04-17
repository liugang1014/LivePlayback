package com.hejunlin.liveplayback;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Util;
import com.hejunlin.liveplayback.vo.HideLoadingEvent;
import com.hejunlin.liveplayback.vo.ShowLoadingEvent;
import com.hejunlin.liveplayback.vo.ShowOrHideChannleViewEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class PlayerFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    public static final String TAG="PlayerFragment";
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    private SimpleExoPlayerView playerView;
    private SimpleExoPlayer player;
    private DataSource.Factory mediaDataSourceFactory;


    public static PlayerFragment newInstance(String param1, String param2) {
        PlayerFragment fragment = new PlayerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_player, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initializePlayer();
        EventBus.getDefault().register(this);
    }

    private void initializePlayer() {
        playerView = this.getView().findViewById(R.id.player_view);
        player = ExoPlayerFactory.newSimpleInstance(
                new DefaultRenderersFactory(this.getContext()),
                new DefaultTrackSelector(), new DefaultLoadControl());

        playerView.setPlayer(player);

        playerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction()==MotionEvent.ACTION_UP)
                    EventBus.getDefault().post(new ShowOrHideChannleViewEvent());
                return true;
            }
        });

        player.setPlayWhenReady(true);
        // player.seekTo(currentWindow, playbackPosition);

        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        mediaDataSourceFactory = new DefaultDataSourceFactory(this.getContext(), Util.getUserAgent(this.getContext(),
                "mediaPlayerSample"), (TransferListener<? super DataSource>) bandwidthMeter);

        play("http://ivi.bupt.edu.cn/hls/hunanhd.m3u8");

        player.addListener(new Player.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {

            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
                Log.d(TAG,"player loading:"+isLoading);
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                switch (playbackState){
                    case Player.STATE_READY:
                        Log.d(TAG,"STATE_READY");
                        EventBus.getDefault().post(new HideLoadingEvent());
                        break;
                    case Player.STATE_BUFFERING:
                        Log.d(TAG,"STATE_BUFFERING");
                        EventBus.getDefault().post(new ShowLoadingEvent());
                        break;
                    case Player.STATE_IDLE:
                        Log.d(TAG,"STATE_IDLE");
                        break;
                }
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {

            }

            @Override
            public void onPositionDiscontinuity() {

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(String url) {
        play(url);
    }

    public void play(String url) {
        Uri uri = Uri.parse(url);
        MediaSource mediaSource = new HlsMediaSource(uri, mediaDataSourceFactory, null, null);
        player.prepare(mediaSource, true, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (player != null) {
            player.release();
            player = null;
        }
    }
}
