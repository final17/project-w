package com.projectw.domain.notification.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public abstract class NotifyEvent extends ApplicationEvent {
    public NotifyEvent(Object source) {
        super(source);
    }
}
