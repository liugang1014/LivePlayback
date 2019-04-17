package com.hejunlin.liveplayback;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hejunlin.liveplayback.vo.HideLoadingEvent;
import com.hejunlin.liveplayback.vo.ShowLoadingEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class ChannelListFragment extends Fragment {

    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_channel_list, null);
        initViews(view);
        EventBus.getDefault().register(this);
        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.channel_list_view);
        GridLayoutManager layoutManager = new GridLayoutManager(this.getContext(), 1);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setFocusable(false);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(String url) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void setData(final ArrayList data) {
        OptionItemAdapter adapter = new OptionItemAdapter(this.getActivity(), R.layout.detail_menu_item, data);
        adapter.setOnItemClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int index = Integer.parseInt(v.getTag().toString());
                ChannelVO channelVO = (ChannelVO) data.get(index);

                ChannelListFragment.this.getFragmentManager().beginTransaction().hide(ChannelListFragment.this).commitAllowingStateLoss();
                EventBus.getDefault().post(new ShowLoadingEvent());
                new ChannelsParser().url(channelVO.url, new AsyncCallback<String>() {
                    @Override
                    public void onFinished(String data) {
                        EventBus.getDefault().post(data);
                        EventBus.getDefault().post(new HideLoadingEvent());
                    }

                    @Override
                    public void onError() {

                    }
                });
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.scrollToPosition(0);
    }
}
