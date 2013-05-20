    /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blueferdi.diamondbox.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ferdinand
 */
public class RabbitMQConsumer implements Runnable
{
    private Connection conn;

    public Connection getConn()
    {
        return conn;
    }

    public void setConn(Connection conn)
    {
        this.conn = conn;
    }
    
    public static void main(String[] args) throws IOException, InterruptedException
    {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        Connection conn =  factory.newConnection();
        
        for(int i=0;i<8;i++)
        {
            RabbitMQConsumer c = new RabbitMQConsumer();
            c.setConn(conn);
            new Thread(c).start();
        }
       
    }

    @Override
    public void run()
    {
        try
        {
            Channel channel = conn.createChannel();
            QueueingConsumer consumer = new QueueingConsumer(channel);
            channel.basicConsume("test", true, consumer);
            while(!Thread.interrupted())
            {
                consumer.nextDelivery();
            }
            channel.close();
        }
        catch (Exception ex)
        {
            Logger.getLogger(RabbitMQConsumer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}