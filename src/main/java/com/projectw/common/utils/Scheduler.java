package com.projectw.common.utils;

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

    public <T> void scheduleOnceAfterDelay(long delayTime , TimeUnit timeUnit , Consumer<T> task, T parameter) {
        scheduler.schedule(() -> task.accept(parameter), delayTime, timeUnit);
    }

}