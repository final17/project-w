package com.projectw.common.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Aspect
public class ExecutionTimeAop {

    @Pointcut("@annotation(com.projectw.common.annotations.ExecutionTimeLog)")
    public void annotationExecutionTimePointCut() {}

    @Pointcut("execution(* com.projectw.domain..*(..))")
    public void allDomainMethodPointCut(){}

    @Pointcut("@within(org.springframework.stereotype.Service)")
    public void serviceClassAnnotationPointCut(){}

    @Pointcut("@within(org.springframework.stereotype.Controller) || @within(org.springframework.web.bind.annotation.RestController)")
    public void controllerClassPointCut(){}

    @Pointcut("@within( org.springframework.transaction.annotation.Transactional)")
    public void transactionalClassAnnotationPointCut(){}

    @Around("annotationExecutionTimePointCut()")
    public Object logExecutionTimeAnnotation(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        try {
            return joinPoint.proceed();
        } finally {
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            // 실행 시간 로그 출력
            log.info("[{}] executed in {} ms", joinPoint.getSignature().toShortString(), executionTime);
        }
    }
}
