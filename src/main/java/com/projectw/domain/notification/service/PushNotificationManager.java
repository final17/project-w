package com.projectw.domain.notification.service;

import com.projectw.domain.notification.events.push.PushEvent;
import org.springframework.stereotype.Component;

@Component
public class PushNotificationManager {

    public void push(PushEvent<?,?> event) {
        event.send();
    }
}
