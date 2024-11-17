package com.projectw.common.aop;

import com.projectw.common.annotations.RedisLock;
import com.projectw.common.exceptions.RedisLocakTimeoutException;
import com.projectw.common.utils.CustomSpringELParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@Aspect
@RequiredArgsConstructor
public class RedisLockAop {
    private final RedissonClient redissonClient;

    @Around("@annotation(com.projectw.common.annotations.RedisLock)")
    public Object redissonLock(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RedisLock annotation = method.getAnnotation(RedisLock.class);
        String lockKey = method.getName() + CustomSpringELParser.getDynamicValue(signature.getParameterNames(), joinPoint.getArgs(), annotation.value());

        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean lockable = lock.tryLock(annotation.waitTime(), annotation.leaseTime(), TimeUnit.MILLISECONDS);
            if (!lockable) {
                throw new RedisLocakTimeoutException();
            }

            return joinPoint.proceed();
        } finally {
            lock.unlock();
        }

    }
}
