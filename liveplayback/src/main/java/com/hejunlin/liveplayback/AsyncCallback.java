package com.hejunlin.liveplayback;

public interface AsyncCallback<T> {
    void onFinished(T data);
    void onError();
}
