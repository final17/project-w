package com.projectw.domain.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class SseNotificationServiceImpl implements NotificationService {

    private static final long TIMEOUT = 30 * 60 * 1000L; // 30분 타임아웃
    private static final long RECONNECTION_TIMEOUT = 5000L; // 5초 재연결 시간
    private final ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    // 새로운 구독을 추가하고 이전 구독을 삭제하여 중복을 방지합니다.
    public SseEmitter subscribe(String key, Object data) {
        delete(key);  // 이전 구독이 있을 경우 안전하게 삭제

        SseEmitter emitter = new SseEmitter(TIMEOUT);
        setupEmitterCallbacks(key, emitter);
        emitters.put(key, emitter);

        sendEvent(emitter, key, data); // 초기 데이터 전송
        return emitter;
    }

    // 특정 키의 구독을 안전하게 삭제하는 메서드
    @Override
    public void delete(String key) {
        SseEmitter emitter = emitters.remove(key);
        if (emitter != null) {
            try {
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }
    }

    // 모든 구독에 데이터를 전송합니다. 실패 시 구독을 정리합니다.
    public void broadcast(String key, Object data) {
        SseEmitter emitter = emitters.get(key);
        if (emitter == null) {
            return;
        }
        sendEvent(emitter, key, data); // 데이터 전송 시도
    }

    // 공통 이벤트 전송 메서드, 실패 시 구독 제거 및 예외 처리
    private void sendEvent(SseEmitter emitter, String key, Object data) {
        try {
            SseEmitter.SseEventBuilder event = SseEmitter.event()
                    .name("message")
                    .id(key)
                    .data(data)
                    .reconnectTime(RECONNECTION_TIMEOUT);

            emitter.send(event);
        } catch (IOException e) {
            handleEmitterError(emitter, key, e); // 에러 발생 시 처리
        }
    }

    // SseEmitter의 콜백 설정
    private void setupEmitterCallbacks(String key, SseEmitter emitter) {
        emitter.onCompletion(() -> emitters.remove(key)); // 완료 시 제거
        emitter.onTimeout(() -> handleTimeout(emitter, key)); // 타임아웃 시 제거 및 종료
        emitter.onError(throwable -> handleEmitterError(emitter, key, throwable)); // 에러 시 제거 및 종료
    }

    // 타임아웃 발생 시 처리 메서드
    private void handleTimeout(SseEmitter emitter, String key) {
        emitters.remove(key);
        emitter.complete();
    }

    // SseEmitter 에러 발생 시 공통 처리 메서드
    private void handleEmitterError(SseEmitter emitter, String key, Throwable throwable) {
        emitters.remove(key);
        emitter.completeWithError(throwable);
    }
}
