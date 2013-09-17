/*
 * Copyright (C) 2013 ferdi
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 */

package com.blueferdi.diamondbox.jdbc.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author ferdi email:ferdi@blueferdi.com
 * @since Jul 18, 2013 4:52:56 AM
 * @version 0.0.1
*/
public class HugeResultSetTest 
{
    public static final String STRING_1M = "It is the ConnectionFactory instances injected into a ServerConnector that create the protocol handling Connection instances for the network endpoints the connector accepts. Thus the different instances of connectors in a Jetty setup vary mostly in the configuration of the factories for the protocols they support. Other than selecting which factories to use, there is typically very little factory configuration required other than injecting the HTTPConfiguration or SslContextFactory instances.";
    
    public static void main(String[] args) throws ClassNotFoundException, SQLException    
    {
        Class.forName("com.mysql.jdbc.Driver");
        
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/test", "root", "12345");
        
        
        Statement st = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
        
        ResultSet rs = st.executeQuery("select * from bigtest");
        
        
        
        int count = 0,i=0;
        rs.last();
        count = rs.getRow();
//        String[] strs = new String[1000000];
        
//        while(rs.next())
//        {
//            rs.getInt(1);
//            rs.getString(2);
////            strs[i] = rs.getString(2);
////            i++;
//            count++;
//        }
        
        System.out.println(count);
//        
//        int id = 1;
//        
//        for(int i=0;i<500;i++)
//        {
//            String sql = "insert into bigtest value ";
//            
//            for(int j=0;j<2000;j++)
//            {
//                sql += "(" + id + ",'" + STRING_1M + "'),";
//                id++;
//            }
//            
//            sql = sql.substring(0, sql.lastIndexOf(","));
//            
//            st.execute(sql);
//        }
//        
        st.close();
        conn.close();
        
        
    }
}
