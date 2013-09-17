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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author ferdi email:ferdi@blueferdi.com
 * @since Jul 8, 2013 4:41:04 PM
 * @version 0.0.1
*/
public class ReentrantlockCounter implements Counter
{
    private int count = 0;
    
    private Lock lock;
    
    public ReentrantlockCounter(boolean flag)
    {
        lock = new ReentrantLock(flag);
    }

    public void increament()
    {
        lock.lock();
        try
        {
            count ++;
        }
        finally
        {
            lock.unlock();
        }
        
    }

    public int getValue()
    {
        return count;
    }

}
