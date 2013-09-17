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

/**
 *
 * @author ferdi email:ferdi@blueferdi.com
 * @since Jul 8, 2013 3:57:47 PM
 * @version 0.0.1
*/
public interface Counter 
{
    public void increament();
    
    public int getValue();
}
