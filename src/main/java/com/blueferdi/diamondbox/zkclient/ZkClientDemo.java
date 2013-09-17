/*
 * Copyright (C) 2013 ferdi
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 */

package com.blueferdi.diamondbox.zkclient;

import java.util.List;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.Watcher.Event.KeeperState;

/**
 *
 * @author ferdi email:ferdi@blueferdi.com
 * @since Jul 11, 2013 10:34:30 AM
 * @version 0.0.1
*/
public class ZkClientDemo 
{
    public static void main(String[] args) throws InterruptedException    
    {
        ZkClient client = new ZkClient("127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183");
        
        client.subscribeStateChanges(new IZkStateListener() {

            public void handleStateChanged(KeeperState state) throws Exception
            {
                switch(state)
                {
                    case Disconnected:
                        System.out.println("disconnected");
                        break;
                    case SyncConnected:
                        System.out.println("syncConnected");
                        break;
                    default :
                        System.out.println(state.toString());
                }
            }

            public void handleNewSession() throws Exception
            {
                System.out.println("need to reconnected!");
            }
        });
        
        client.subscribeChildChanges("/eunormia-config",new IZkChildListener() {

            public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception
            {
                System.out.println(parentPath);
                for(String s : currentChilds)
                {
                    System.out.println(s);
                }
            }
        } );
        
        client.subscribeDataChanges("/eunormia-config/test1", new IZkDataListener() {

            public void handleDataChange(String dataPath, Object data) throws Exception
            {
                System.out.println(dataPath + " " + data);
            }

            public void handleDataDeleted(String dataPath) throws Exception
            {
                System.out.println(dataPath);
            }
        });
        
        client.writeData("/eunormia-config/test1", "test");
        
        synchronized(ZkClientDemo.class)
        {
        
            ZkClientDemo.class.wait();
        }
    }
}
