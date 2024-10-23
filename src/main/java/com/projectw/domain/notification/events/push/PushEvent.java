package com.projectw.domain.notification.events.push;

public interface PushEvent<SENDER, TARGET> {

    String getMessage();
    SENDER getSender();
    TARGET getTarget();
    void send();
}
