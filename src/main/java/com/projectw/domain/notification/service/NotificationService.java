package com.projectw.domain.notification.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface NotificationService {
    void broadcast(String key, Object data);
    SseEmitter subscribe(String key, Object data);
    void delete(String key);
}
