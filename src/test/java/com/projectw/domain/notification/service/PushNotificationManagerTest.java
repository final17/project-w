package com.projectw.domain.notification.service;

import com.projectw.domain.notification.events.push.StoreOpenEvent;
import org.junit.jupiter.api.Test;

class PushNotificationManagerTest {

    private final PushNotificationManager pushNotificationManager = new PushNotificationManager();

    @Test
    public void t() throws Exception {
        // given
        pushNotificationManager.push(new StoreOpenEvent("test", null, null));
        // when

        // then
    }
}