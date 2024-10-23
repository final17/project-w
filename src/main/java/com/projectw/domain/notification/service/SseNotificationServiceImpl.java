package com.projectw.domain.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectw.common.exceptions.NotifyCationFailedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class SseNotificationServiceImpl implements SseNotificationService {

    private final ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private static final long TIMEOUT = 60 * 1000L;
    private static final long RECONNECTION_TIMEOUT = 0L;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public SseEmitter subscribe(String key, Object data) throws NotifyCationFailedException {

        if (emitters.containsKey(key)) {
            return emitters.get(key);
        }

        SseEmitter.SseEventBuilder event = SseEmitter.event()
                .name(key)
                .id(key)
                .data(data == null ? "SSE CONNECTED" : data)
                .reconnectTime(RECONNECTION_TIMEOUT);

        SseEmitter emitter = new SseEmitter(TIMEOUT);
        emitter.onCompletion(() -> this.emitters.remove(key));
        emitter.onTimeout(emitter::complete);

        try {
            emitter.send(event);
        } catch (IOException e) {
            emitter.completeWithError(e);

            throw new NotifyCationFailedException();
        }

        emitters.put(key, emitter);
        return emitter;
    }

    @Override
    public void broadcast(String key, Object data) throws NotifyCationFailedException {
        if(!emitters.containsKey(key)) {
            return;
        }
        SseEmitter sseEmitter = emitters.get(key);

        try{
            SseEmitter.SseEventBuilder event = SseEmitter.event()
                    .name(key)
                    .id(key)
                    .data(objectMapper.writeValueAsString(data))
                    .reconnectTime(RECONNECTION_TIMEOUT);

            sseEmitter.send(event);
        } catch (IOException e) {
            sseEmitter.completeWithError(e);
            emitters.remove(key);
            throw new NotifyCationFailedException();
        }
    }

    @Override
    public void delete(String key) {
        if(emitters.containsKey(key)) {
            emitters.get(key).complete();
        }
    }
}
