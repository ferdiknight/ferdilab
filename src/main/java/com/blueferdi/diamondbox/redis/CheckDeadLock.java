/*
 * Copyright (C) 2013 guang
 */

package com.blueferdi.diamondbox.redis;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.pubsub.RedisPubSubConnection;

/**
 *
 * @author ferdi email:ferdi@blueferdi.com
 * @since Aug 28, 2013 2:36:38 PM
 * @version 0.0.1
*/
public class CheckDeadLock
{
    
    public static RedisClient client;
    
    public static void main(String[] args)    
    {
        client = new RedisClient("127.0.0.1");
        
        System.out.println("xxxxx");
        
        Subscriber subscriber = new Subscriber();
        
        subscriber.setClient(client);
        
        new Thread(subscriber).start();
        
        
        RedisPubSubConnection<String,String> conn = client.connectPubSub();
        
        conn.publish("test", "test");
        
        System.out.println("1111111111");
        
        
    }
}
