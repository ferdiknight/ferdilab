/*
 * Copyright (C) 2013 guang
 */

package com.blueferdi.diamondbox.redis;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.pubsub.RedisPubSubListener;

/**
 *
 * @author ferdi email:ferdi@blueferdi.com
 * @since Aug 28, 2013 2:41:35 PM
 * @version 0.0.1
*/
public class PubsubListener implements RedisPubSubListener<String,String>
{
    RedisClient client;

    public RedisClient getClient()
    {
        return client;
    }

    public void setClient(RedisClient client)
    {
        this.client = client;
    }

    public void message(String k, String v)
    {
        client.connectAsync().del(v);
    }

    public void message(String k, String k1, String v)
    {
        
    }

    public void subscribed(String k, long l)
    {
        
    }

    public void psubscribed(String k, long l)
    {
        
    }

    public void unsubscribed(String k, long l)
    {
        
    }

    public void punsubscribed(String k, long l)
    {
        
    }

}
