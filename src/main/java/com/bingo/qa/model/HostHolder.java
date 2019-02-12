package com.bingo.qa.model;

import org.springframework.stereotype.Component;

/**
 * @author bingo
 */
@Component
public class HostHolder {
    /**
     * 线程本地变量
     * 每条线程都有自己的一个变量
     */
    private static ThreadLocal<User> users = new ThreadLocal<>();

    public User getUser() {
        return users.get();
    }

    public void setUser(User user) {
        users.set(user);
    }

    public void clear() {
        users.remove();
    }
}
