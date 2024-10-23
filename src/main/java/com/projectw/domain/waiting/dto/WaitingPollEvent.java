package com.projectw.domain.waiting.dto;

import org.springframework.context.ApplicationEvent;

public class WaitingPollEvent extends ApplicationEvent {
    public WaitingPollEvent(WaitingPoll waitingPollInfo) {
        super(waitingPollInfo);
    }
}
