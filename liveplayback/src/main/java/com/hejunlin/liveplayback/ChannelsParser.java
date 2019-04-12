package com.hejunlin.liveplayback;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class ChannelsParser {

    public static final String TAG = "ChannelsParser";
    private String mRootUrl;

    public ChannelsParser setRootUrl(String url) {
        this.mRootUrl = url;
        return this;
    }

    public void process() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Document doc = Jsoup.connect(mRootUrl).get();
                    Elements divs = doc.select("a[href*=act=play]");//ui-link-inherit

                    for (Element div : divs) {
                        String herf = div.absUrl("href");
                        if (div.childNodeSize() > 0) {
                            String name = div.childNode(0).toString();
                            Document doc2 = Jsoup.connect(herf).get();
                            Elements elements = doc2.select("option");
                            for (Element element : elements
                            ) {
                                String url = element.attr("value");//http://api2.iptv888.com/player.php?token=2qJc5bZJOMHUOMa9vB6KeHSc&tid=hbitv&id=1

                                if ((url.indexOf("player.php?")) != -1) {
                                    url = url.replace("player.php", "play.m3u8");
                                }
                                Log.d(TAG, name + ":" + url);

                            }

                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
}
