package com.projectw.domain.notification.events.push;

public interface PushEvent<Sender, Target> {

    String getMessage();
    Sender getSender();
    Target getTarget();
    void send();
}
