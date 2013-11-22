/*
 * Copyright (C) 2013 guang
 */

package com.blueferdi.diamondbox.concurrent.executor;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author ferdi email:ferdi@blueferdi.com
 * @since Oct 11, 2013 12:23:54 PM
 * @version 0.0.1
*/
public class ExecutorDemo 
{
    public static void main(String[] args) throws InterruptedException, TimeoutException, ExecutionException    
    {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        
        Future future = executor.submit(new Callable() {

            public Object call() throws Exception
            {
                boolean flag = true;
                int i = 0;
                Thread.sleep(10000);
                
                return 0;
            }
        });
        
       try
       {
        
        future.get(1, TimeUnit.SECONDS);
       }
       catch (Exception ex)
       {
           
       }
        
//        Thread.sleep(10000);
//        
//        future.cancel(true);
        
    }
}
