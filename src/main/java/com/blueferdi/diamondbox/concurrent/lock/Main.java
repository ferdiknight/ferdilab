/*
 * Copyright (C) 2013 ferdi
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 */
package com.blueferdi.diamondbox.concurrent.lock;

import java.util.concurrent.CyclicBarrier;

/**
 *
 * @author ferdi email:ferdi@blueferdi.com
 * @since Jul 8, 2013 4:55:07 PM
 * @version 0.0.1
 */
public class Main
{

    private Counter counter;
    private CyclicBarrier barrier;
    private int number;
        
    
    public static void main(String[] args)    
    {
        InnerLockCounter counter = new InnerLockCounter();
        
        new Main(counter,500).test();
        
        ReentrantlockCounter rtc = new ReentrantlockCounter(true);
        new Main(rtc,500).test();
        
        ReentrantlockCounter rfc = new ReentrantlockCounter(false);
        new Main(rfc,500).test();
        
    }
    
    public Main(Counter counter, int number)
    {
        this.counter = counter;
        barrier = new CyclicBarrier(number + 1);
        this.number = number;
    }

    public void test()
    {
        try
        {
            for (int i = 0; i < number; i++)
            {
                new TestThread(counter).start();
            }
            long start = System.currentTimeMillis();
            barrier.await(); // 等待所有任务线程创建
            barrier.await(); // 等待所有任务计算完成
            long end = System.currentTimeMillis();
            System.out.println("count value:" + counter.getValue());
            System.out.println("花费时间:" + (end - start) + "毫秒");
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    class TestThread extends Thread
    {

        private Counter counter;

        public TestThread(final Counter counter)
        {
            this.counter = counter;
        }

        public void run()
        {
            try
            {
                barrier.await();
                for (int i = 0; i < 100; i++)
                {
                    counter.increament();
                }
                barrier.await();
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }
}
