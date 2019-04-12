package com.hejunlin.liveplayback;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;

import com.iheartradio.m3u8.Encoding;
import com.iheartradio.m3u8.Format;
import com.iheartradio.m3u8.ParseException;
import com.iheartradio.m3u8.PlaylistException;
import com.iheartradio.m3u8.PlaylistParser;
import com.iheartradio.m3u8.data.Playlist;
import com.iheartradio.m3u8.data.TrackData;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import okhttp3.Call;


public class CachePrepareManager {

    public static final String TAG="CachePrepareManager";

    private String mVideoUrl;

    public CachePrepareManager setVideoUrl(String videoUrl){
        this.mVideoUrl=videoUrl;
        return this;
    }

    public void process(){

        OkHttpUtils.get().url(mVideoUrl).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                e.printStackTrace();
            }

            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(String response, int id) {
                InputStream inputStream = new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8));
                PlaylistParser parser = new PlaylistParser(inputStream, Format.EXT_M3U, Encoding.UTF_8);
                try {
                    Playlist playlist = parser.parse();
                    List<TrackData> datas= playlist.getMediaPlaylist().getTracks();
                    for (TrackData data:datas
                         ) {
                        downloadTSFile(data);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (PlaylistException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void downloadTSFile(TrackData data) {
        String dir= Environment.getExternalStorageDirectory().getAbsolutePath();
        final File file=new File(dir+"/ts_cache/");
        if (!file.exists()){
            if (!file.mkdir()){
                Log.e(TAG,"mkdir failed!");
                return;
            }
        }

        String url=data.getUri();
        if (!url.startsWith("http://") || !url.startsWith("https://")){
            int n;
            if ((n=mVideoUrl.indexOf(".m3u8"))!=-1){
                String host=mVideoUrl.substring(0,n-1);
                url=host+data.getUri();
            }
        }
        Log.d(TAG,"download ts:"+url);
        OkHttpUtils.get().url(url).build().execute(new FileCallBack(file.getAbsolutePath(),System.currentTimeMillis()+".ts") {
            @Override
            public void onError(Call call, Exception e, int id) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(File response, int id) {
                Log.d(TAG,"finish download ts file :"+file.getName());
            }
        });
    }

}
