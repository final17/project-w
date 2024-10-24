package com.projectw.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.projectw.common.annotations.RedisListener;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisListenerProcessor implements ApplicationListener<ContextRefreshedEvent> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RedissonClient redissonClient;
    private final ApplicationContext applicationContext;

    @PostConstruct
    public void init() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void onApplicationEvent(@NotNull ContextRefreshedEvent event) {
        // Component가 달려있는 빈들을 찾는다.
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(Component.class);

        for (Object bean : beans.values()) {
            Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
            Method[] methods = targetClass.getDeclaredMethods();
            for (Method method : methods)
            {
                // 메서드들 중 RedisListener 어노테이션이 있는 메서드를 찾는다.
                RedisListener annotation = method.getAnnotation(RedisListener.class);
                if (annotation != null) {
                    String topic = annotation.topic();
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if(parameterTypes.length == 0) {
                        subscribeToRedisTopic(topic, method, bean);
                    } else if(parameterTypes.length == 1){
                        subscribeToRedisTopic(topic, method, bean, parameterTypes[0]);
                    } else {
                        throw new IllegalArgumentException("파라미터가 2개 이상 존재합니다.");
                    }
                }
            }
        }
    }

    /**
     * redis pub/sub 구독
     * @param topic 구독 채널 이름
     * @param method 어노테이션이 있는 메서드
     * @param bean 빈
     * @param parameterType 메세지 변환 타입
     */
    private void subscribeToRedisTopic(String topic, Method method, Object bean, Class<?> parameterType) {
        RTopic redisTopic = redissonClient.getTopic(topic);
        redisTopic.addListener(String.class, (channel, message) -> {
            try {
                Object deserializedMessage = objectMapper.readValue(message, parameterType);
                method.invoke(bean, deserializedMessage);
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.error(e.getMessage());
            } catch ( JsonProcessingException e) {
                log.error("메시지 타입이 일치하지 않습니다. 예상한 타입: {}, 수신한 메시지: {} 에러 메세지: {}", parameterType, message, e.getMessage());
            }
        });
    }

    /**
     * 매개변수 없을 때 실행되는 메서드
     * @param topic 구독 채널 이름
     * @param method 어노테이션이 있는 메서드
     * @param bean 빈
     */
    private void subscribeToRedisTopic(String topic, Method method, Object bean) {
        RTopic redisTopic = redissonClient.getTopic(topic);
        redisTopic.addListener(String.class, (channel, message) -> {
            try {
                method.invoke(bean);
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.error(e.getMessage());
            }
        });
    }
}
