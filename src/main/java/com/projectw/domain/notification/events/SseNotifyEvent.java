package com.projectw.domain.notification.events;

import lombok.Getter;

@Getter
public class SseNotifyEvent extends NotifyEvent{

    private String key;
    private Object data;

    public SseNotifyEvent(String key, Object data) {
        super(data);
        this.key = key;
        this.data = data;
    }
}
