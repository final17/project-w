package com.projectw.domain.notification.events;

import com.projectw.domain.notification.service.PushNotificationService;
import com.projectw.domain.notification.service.SseNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final SseNotificationService sseNotificationService;

    private final PushNotificationService pushNotificationService;

    @TransactionalEventListener
    public void onNotify(NotifyEvent event){

        if(event instanceof SseNotifyEvent sseNotifyEvent) {
            sseNotificationService.broadcast(sseNotifyEvent.getKey(), sseNotifyEvent.getData());
        } else if(event instanceof PushEvent pushEvent){
            pushNotificationService.push("", pushEvent.getSource());
        }
    }
}
