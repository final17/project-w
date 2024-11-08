package com.projectw.domain.notification.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slack.api.Slack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class SlackClient implements MessageClient{
    @Value("${DEFAULT_SLACK_WEBHOOK_URL}")
    private String slackAlertWebhookUrl;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void sendMessage(String message) {
        sendMessage(slackAlertWebhookUrl, message);
    }

    public void sendMessage(String hookUrl, String message) {

        try {
            Slack instance = Slack.getInstance();
            Map<String, String> map = new HashMap<>();
            map.put("text", message);

            instance.send(hookUrl, mapper.writeValueAsString(map));
        } catch (Exception e) {
            log.error("Slack Message Send Failed!! {}", e.getMessage());
        }
    }
}
