package com.projectw.common.utils;

import com.projectw.common.enums.ResponseCode;
import com.projectw.common.exceptions.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Component
@EnableScheduling
public class Scheduler {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public <T> void scheduleOnceAfterDelay(long delayTime , String type , Consumer<T> task, T parameter) {
        TimeUnit timeUnit = switch (type) {
            case "h" -> TimeUnit.HOURS;
            case "m" -> TimeUnit.MINUTES;
            case "s" -> TimeUnit.SECONDS;
            case "ms" -> TimeUnit.MILLISECONDS;
            default -> throw new ApiException(HttpStatus.BAD_REQUEST , ResponseCode.INVALID_TIME_UNIT.getMessage());
        };
        scheduler.schedule(() -> task.accept(parameter), delayTime, timeUnit);
    }

}
