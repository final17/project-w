package com.projectw.domain.notification;

public sealed interface WaitingQueueResponse permits WaitingQueueResponse.List, WaitingQueueResponse.Info {
    record List(int totalWaitingNumber, java.util.List<Info> userIds) implements WaitingQueueResponse { }
    record Info(int rank, String userId) implements WaitingQueueResponse {}
}
