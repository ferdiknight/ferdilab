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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 *
 * @author ferdi email:ferdi@blueferdi.com
 * @since Jul 15, 2013 1:57:01 PM
 * @version 0.0.1
*/
public class JettyClient 
{
    public static void main(String[] args) throws IOException    
    {
        int i = 10000;
        while(i > 0)
        {
        Socket socket = new Socket();
        SocketAddress address = new InetSocketAddress("10.1.2.102",8080);
        socket.connect(address);
        socket.getOutputStream().write("HTTP/1.0 /testblocking/BlockingServlet".getBytes());
        socket.setSoTimeout(1);
        try
        {
        socket.getInputStream().read();
        }
        catch(Exception ex)
        {
            socket.close();
        }
        
        
        i--;
        }
    }
}
