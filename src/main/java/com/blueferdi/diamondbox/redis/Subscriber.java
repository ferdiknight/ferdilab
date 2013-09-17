/*
 * Copyright (C) 2013 guang
 */

package com.blueferdi.diamondbox.redis;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.pubsub.RedisPubSubConnection;

/**
 *
 * @author ferdi email:ferdi@blueferdi.com
 * @since Aug 28, 2013 3:10:42 PM
 * @version 0.0.1
*/
public class Subscriber implements Runnable
{
    
    RedisClient client;
    
    boolean running = true;

    public RedisClient getClient()
    {
        return client;
    }

    public void setClient(RedisClient client)
    {
        this.client = client;
    }
    
    public void run()
    {
        
        final RedisPubSubConnection<String,String> conn = client.connectPubSub();
        PubsubListener listener = new PubsubListener();
        listener.setClient(client);
        conn.addListener(listener);
        conn.subscribe("test");        
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                conn.unsubscribe("test");

                synchronized (Subscriber.class)
                {
                    running = false;
                    Subscriber.class.notify();
                }
            }
        });

        synchronized (Subscriber.class)
        {
            while (running)
            {
                try
                {
                    Subscriber.class.wait();
                }
                catch (Throwable e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

}
