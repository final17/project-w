package com.projectw.domain.waiting.dto;

public sealed interface WaitingQueueResponse permits WaitingQueueResponse.List,
        WaitingQueueResponse.Info,
        WaitingQueueResponse.WaitingInfo,
        WaitingQueueResponse.MyWaitingStoreList{
    record List(int totalWaitingNumber, java.util.List<Info> userIds) implements WaitingQueueResponse { }
    record Info(int rank, Long userId) implements WaitingQueueResponse {}
    record WaitingInfo(boolean isWaiting) implements WaitingQueueResponse {}
    record MyWaitingStoreList(java.util.List<Long> storeIds) implements WaitingQueueResponse { }
}
