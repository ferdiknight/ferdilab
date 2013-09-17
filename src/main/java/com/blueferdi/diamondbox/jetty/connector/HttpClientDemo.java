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
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;


/**
 *
 * @author ferdi email:ferdi@blueferdi.com
 * @since Jul 15, 2013 5:18:24 PM
 * @version 0.0.1
*/
public class HttpClientDemo 
{
    public static void main(String[] args) throws IOException    
    {
        HttpClient client = new HttpClient();
        
        client.getHttpConnectionManager().getParams().setSoTimeout(1);
        
        int i = 1000;
        
        while(i > 0)            
        {
        try
        {
            GetMethod method = new GetMethod("http://localhost:8080/testblocking/BlockingServlet");
            method.setRequestHeader("Connection", "close");
            client.executeMethod(method);
        }
        catch(Exception ex)
        {
            i--;
            System.out.println(i);
        }
        }
    }
}
