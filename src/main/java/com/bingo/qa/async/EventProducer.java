package com.bingo.qa.async;

import com.alibaba.fastjson.JSONObject;
import com.bingo.qa.util.JedisAdapter;
import com.bingo.qa.util.RedisKeyUtil;
import com.google.gson.Gson;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 这是事件的入口
 * 由它统一发送事件:放到队列中,
 * 而EventConsumer将事件从队列中取出来
 *
 * @author bingo
 */
@Service
public class EventProducer {

    @Autowired
    JedisAdapter jedisAdapter;

    public boolean fireEvent(EventModel eventModel) {
        try {
            String json = new Gson().toJson(eventModel);
            		//JSONObject.toJSONString(eventModel);

            // key: EVENT_QUEUE
            String key = RedisKeyUtil.getEventQueueKey();
            // 将eventModel序列化，并存放到redis list队列中
            jedisAdapter.lpush(key, json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
