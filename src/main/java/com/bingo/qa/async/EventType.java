package com.bingo.qa.async;

/**
 * 事件类型
 *
 * @author bingo
 */
public enum EventType {
    /**
     * 类型
     */
    LIKE(0),
    COMMENT(1),
    LOGIN(2),
    MAIL(3),
    FOLLOW(4),
    UNFOLLOW(5),
    ADD_QUESTION(6);

    private int value;

    EventType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
