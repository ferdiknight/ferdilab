/*
 * Copyright (C) 2013 ferdi
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 */

package com.blueferdi.diamondbox.jetty.connector;

import java.io.IOException;
import java.nio.channels.Channel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executors;
import org.jboss.netty.channel.socket.ClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

/**
 *
 * @author ferdi email:ferdi@blueferdi.com
 * @since Jul 16, 2013 2:24:28 PM
 * @version 0.0.1
*/
public class NioDemo 
{
    public static void main(String[] args) throws IOException    
    {
        ServerSocketChannel channel = ServerSocketChannel.open();
        Channel systemChannel = System.inheritedChannel();
        
        ClientSocketChannelFactory clientSocketFactory = new NioClientSocketChannelFactory(Executors.newFixedThreadPool(2),Executors.newFixedThreadPool(6));
        
        
        
        
    }
}
