/*
 * Copyright (C) 2013 guang
 */

package com.blueferdi.diamondbox.datastruct;

import com.blueferdi.diamondbox.util.SkipListMap;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 *
 * @author ferdi email:ferdi@blueferdi.com
 * @since Sep 16, 2013 12:15:56 PM
 * @version 0.0.1
*/
public class HashMapvSkipList 
{
    public static void main(String[] args)    
    {
        Random r = new Random();
        int capacity = 10000000;
        
        Comparator<Integer> intComparator = new Comparator<Integer>() {

            public int compare(Integer o1, Integer o2)
            {
                return o1.intValue() - o2.intValue();
            }
        };
        
        HashMap<Integer,String> hashMap = new HashMap<Integer,String>();
        
        SkipListMap<Integer,String> skipListMap = new SkipListMap<Integer,String>(intComparator);
        
        ConcurrentSkipListMap<Integer,String> casSkipListMap = new ConcurrentSkipListMap<Integer,String>();
        long start = 0,stop = 0;
        
//        start = System.currentTimeMillis();
//        
//        for(int i=0;i<capacity;i++)
//        {
//            hashMap.put(i, Integer.toString(i));
//        }
//        
//        stop = System.currentTimeMillis();
//        
//        System.out.println(stop - start);
//        
//        start = System.currentTimeMillis();
//        for(int i=0;i<capacity;i++)
//        {
//            hashMap.get(r.nextInt(capacity));
//        }
//        stop = System.currentTimeMillis();
//        System.out.println(stop - start);
//        
//        hashMap.clear();
//        
//        System.gc();
//        
//        start = System.currentTimeMillis();
//        
//        for(int i=0;i<capacity;i++)
//        {
//            hashMap.put(i, Integer.toString(i));
//        }
//        
//        stop = System.currentTimeMillis();
//        
//        System.out.println(stop - start);
//        
//        start = System.currentTimeMillis();
//        for(int i=0;i<capacity;i++)
//        {
//            hashMap.get(r.nextInt(capacity));
//        }
//        stop = System.currentTimeMillis();
//        System.out.println(stop - start);
//        
//        hashMap.clear();
//        
//        System.gc();
        
        start = System.currentTimeMillis();
        for(int i=0;i<capacity;i++)
        {
            skipListMap.put(i, Integer.toString(i));
        }
        
        stop = System.currentTimeMillis();
        System.out.println(stop - start);
        
        start = System.currentTimeMillis();
        for(int i=0;i<capacity;i++)
        {
            skipListMap.get(r.nextInt(capacity));
        }
        stop = System.currentTimeMillis();
        System.out.println(stop - start);
        
        skipListMap.clear();
        
        System.gc();
        
        start = System.currentTimeMillis();
        for(int i=0;i<capacity;i++)
        {
            skipListMap.put(i, Integer.toString(i));
        }
        
        stop = System.currentTimeMillis();
        System.out.println(stop - start);
        
        start = System.currentTimeMillis();
        for(int i=0;i<capacity;i++)
        {
            skipListMap.get(r.nextInt(capacity));
        }
        stop = System.currentTimeMillis();
        System.out.println(stop - start);
        
        skipListMap.clear();
        
        System.out.println(skipListMap.compareCost + " " + skipListMap.randomCost + " " + skipListMap.indexCost + " " + skipListMap.scanCost);
        
        
        System.gc();
        
        
        
        
        
//        start = System.currentTimeMillis();
//        for(int i=0;i<capacity;i++)
//        {
//            casSkipListMap.put(i, Integer.toString(i));
//        }
//        
//        stop = System.currentTimeMillis();
//        System.out.println(stop - start);
//         
//        
//        start = System.currentTimeMillis();
//        for(int i=0;i<capacity;i++)
//        {
//            casSkipListMap.get(r.nextInt(capacity));
//        }
//        stop = System.currentTimeMillis();
//        System.out.println(stop - start);
//        
//        casSkipListMap.clear();
//        
//        System.gc();
        
        
        
        
        
    }
}
