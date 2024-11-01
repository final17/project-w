package com.projectw.domain.notification.client;

public interface MessageClient {

    void sendMessage(String url, String message);
    void sendMessage(String message);
}
