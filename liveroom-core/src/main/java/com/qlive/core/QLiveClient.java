package com.qlive.core;

public interface QLiveClient {

    /**
     * 获取服务实例
     * @param serviceClass
     * @param <T>
     * @return
     */
    <T extends QLiveService> T getService(Class<T> serviceClass);

    void addLiveStatusListener(QLiveStatusListener liveStatusListener);
    void removeLiveStatusListener(QLiveStatusListener liveStatusListener);

    void destroy();
    QClientType getClientType();

}
