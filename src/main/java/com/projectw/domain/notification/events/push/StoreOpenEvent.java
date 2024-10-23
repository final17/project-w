package com.projectw.domain.notification.events.push;

import com.projectw.domain.store.entity.Store;
import com.projectw.domain.user.entity.User;

import java.util.List;

public class StoreOpenEvent implements PushEvent<Store, List<User>> {

    private final String message;
    private final Store store;
    private final List<User> targetList;

    public StoreOpenEvent(String message, Store sender, List<User> target) {
        this.message = message;
        this.store = sender;
        this.targetList = target;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Store getSender() {
        return store;
    }

    @Override
    public List<User> getTarget() {
        return targetList;
    }


    @Override
    public void send() {
        System.out.println("message = " + message);
    }
}
