package com.projectw.domain.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class SlackEventListener {

    private final MessageClient slackClient;

    @TransactionalEventListener
    public void onSlackNotify(SlackEvent slackEvent) {
        String message = slackEvent.getMessage();
        String url = slackEvent.getHookUrl();
        
        // hook url 없으면 디폴트 훅 url로 전송
        if(StringUtils.hasText(url)) {
            ((SlackClient) slackClient).sendMessage(url, message);
        } else {
            slackClient.sendMessage(message);
        }
    }
}
