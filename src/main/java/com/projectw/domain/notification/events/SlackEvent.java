package com.projectw.domain.notification.events;

import org.springframework.context.ApplicationEvent;

public class SlackEvent extends ApplicationEvent {

    private String hookUrl = null;

    public SlackEvent(String message) {
        super(message);
    }

    public SlackEvent(String hookUrl, String message) {
        super(message);
        this.hookUrl = hookUrl;
    }

    public String getMessage() {
        return (String) super.getSource();
    }

    public String getHookUrl() {
        return hookUrl;
    }
}
