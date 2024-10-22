package com.projectw.common.aop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectw.common.annotations.SlackAlert;
import com.projectw.common.utils.CustomSpringELParser;
import com.projectw.domain.notification.client.MessageClient;
import com.projectw.domain.notification.client.SlackClient;
import com.projectw.security.AuthUser;
import com.projectw.security.JwtAuthenticationToken;
import io.jsonwebtoken.lang.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.expression.ExpressionException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
@Aspect
@Slf4j
@RequiredArgsConstructor
public class SlackAlertAop {

    @Value("${DEFAULT_SLACK_WEBHOOK_URL}")
    private String defaultSlackWebhookUrl;  // 여기에서 설정 값 주입

    private final MessageClient slackClient;
    private final ObjectMapper objectMapper;


    @Pointcut("@annotation(com.projectw.common.annotations.SlackAlert)")
    public void annotaionPc(){}

    @Around("annotaionPc()")
    public Object slackAlertAround(ProceedingJoinPoint joinPoint) throws Throwable {

        boolean isExceptionOccurred = false;
        long startTime = System.currentTimeMillis();
        Exception exception = null;
        Object result = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            isExceptionOccurred = true;
            exception = e;
            throw e;
        } finally {
            Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
            SlackAlert annotation = method.getAnnotation(SlackAlert.class);
            Secured secured = method.getAnnotation(Secured.class);
            String[] authorities = secured.value();

            // 스프링 EL 파싱
            String[] spELs = annotation.requestEL();

            Map<String, Object> objects = parseSpEl(joinPoint, annotation, spELs);

            String webhookUrl = StringUtils.hasText(annotation.hookUrl()) ? annotation.hookUrl() : defaultSlackWebhookUrl;
            String msg = isExceptionOccurred
                    ? "["+ exception.getClass().getSimpleName() + "] " + (StringUtils.hasText(annotation.onFailure()) ? annotation.onFailure() : exception.getMessage())
                    : (StringUtils.hasText(annotation.onSuccess())? annotation.onSuccess() : "Success Alert");

            long executionTime = System.currentTimeMillis() - startTime;

            // Auth user 가져오기
            AuthUser auth = null;
            JwtAuthenticationToken authentication = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
            auth = authentication == null ? null : (AuthUser) authentication.getPrincipal();

            String authInfo = auth == null
                    ? "NO AUTH"
                    : "{ "+ MessageFormat.format("Id: {0} || Email: {1} || ROLE: {2}" ,auth.getUserId(), auth.getEmail(), Arrays.toString(auth.getAuthorities().toArray())) + " }";

            String payload = MessageFormat.format(
                    "\n"+"""
                ```
                🔔 [Slack Alert] 🔔
                🔒 Method Secured: {0}
                👤 Auth: {1}
                📌 Method: {2}
                ✉️ Message: {3}
                {6} Result: {4}
                ⏳ ExecutionTime: {5}ms
                🕒 Timestamp: {7}{8}{9}
                ```
                """ +"\n",
                    authorities == null ? "NONE" : String.join(",", authorities),
                    authInfo,
                    joinPoint.getSignature().toShortString(),
                    msg,
                    isExceptionOccurred ? "FAILED" : "SUCCESS",
                    executionTime,
                    LocalDateTime.now().toString(),
                    isExceptionOccurred ? "🔴" : "🟢",
                    objects.isEmpty() ? "" :  "\n\n💾 Request Data\n" + getPrettyString(objects),
                    annotation.attachResult() ? (isExceptionOccurred ? "" :  "\n\n💾 Result Data\n" + getPrettyString(result)) : ""
            );

            ((SlackClient)(slackClient)).sendMessage(webhookUrl, payload);
        }
    }


    private Map<String, Object> parseSpEl(JoinPoint joinPoint, SlackAlert annotation, String[] request) {
        // 스프링 EL 파싱
        String[] spELs = annotation.requestEL();
        if(spELs == null || spELs.length == 0) {
            return Map.of();
        }

        Map<String, Object> objects = new HashMap<>();
        for(String spEl : spELs) {
            try {
                Object requestData = CustomSpringELParser.getDynamicValue(((MethodSignature) joinPoint.getSignature()).getParameterNames(), joinPoint.getArgs(), spEl);
                spEl = spEl.substring(1);
                objects.put(spEl, requestData);
            } catch (ExpressionException e) {
                log.error("SpEL parse Failed: {}",e.getExpressionString());
            }
        }

        return objects;
    }
    private String getPrettyString(Object  object) throws JsonProcessingException {
        if(Objects.nullSafeEquals(object, null)) {
            return "";
        }
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }
}
