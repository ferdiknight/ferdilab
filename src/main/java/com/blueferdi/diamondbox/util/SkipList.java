/*
 * Copyright (C) 2013 guang
 */
package com.blueferdi.diamondbox.util;

import java.util.Comparator;
import java.util.Random;

/**
 *
 * @author ferdi email:ferdi@blueferdi.com
 * @since Sep 18, 2013 1:10:02 PM
 * @version 0.0.1
 */
public class SkipList<K, V>
{

    private final Comparator<? super K> comparator;
    private static final Random seedGen = new Random();
    private static int randomSeed;
    private HeadIndex head;
    private static final Object BASE_HEADER = new Object();

    final void initialize()
    {
        randomSeed = seedGen.nextInt() | 0x0100; // ensure nonzero
        head = new HeadIndex(null, null, new Node(null, BASE_HEADER, null), 1);
    }

    public SkipList(Comparator<? super K> comparator)
    {
        this.comparator = comparator;
        initialize();
    }

    /*---------insert------*/
    
    public V put(K key,V value)
    {
        return doPut(key,value,false);
    }
    
    public V doPut(K key,V value,boolean ifOnlyAbsent)
    {
        Node q = findPredecessor(key);
        Node n = q.next;
        
        for(;;)
        {
            if(n != null)
            {
                int c = 0;
                if((c=comparator.compare(key, n.key))>0)
                {
                    q = n;
                    n = q.next;
                    continue;
                }
                else if(c == 0 && !ifOnlyAbsent)
                {
                    Object oldV = n.value;
                    n.value = value;
                    return (V)oldV;
                }
                //when c < 0 fall over
            }
            
            Node newNode = new Node(key,value,n);
            q.next = newNode;            
        }
        //TODO push the index to random level
    }
    
    
    
    
    /*-------read-------*/
    public V get(K key)
    {
        Node n = null;
        return (n = findNode(key))!=null?(V)n.value:null;
    }

    private Node findNode(K key)
    {
        Node q = findPredecessor(key);
        Node n = q.next;
        
        for(;;)
        {
            if(n != null)
            {
                int c = 0;
                if((c = comparator.compare(key, n.key)) > 0)
                {
                    q = n;
                    n = q.next;
                    continue;
                }
                else if(c == 0)
                {
                    return n;
                }
                //when c < 0 fall over to return null
            }
            
            return null;
        }
        
    }
    
    private Node findPredecessor(K key)
    {
        Index q = head.right;
        Index r = q.right;

        for (;;)
        {
            if (r != null)
            {
                Node n = r.node;
                int c = 0;
                if ((c=comparator.compare(key, n.key)) > 0)
                {
                    q = r;
                    r = q.right;
                    continue;
                }
                else if(c == 0)
                {
                    return n;
                }
                
                //when c < 0 fall over
            }         
            
            Index d = q.down;
            
            if(d == null)
            {
                return q.node;
            }
            
            q = d;
            r = q.right;

        }
    }

    
    /*--------data struct------------*/
    
    class Node
    {

        K key;
        Object value;
        Node next;

        public Node(K key, Object value, Node next)
        {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    class Index
    {

        Index right;
        Index down;
        Node node;

        public Index(Index right, Index down, Node node)
        {
            this.right = right;
            this.down = down;
            this.node = node;
        }
    }

    class HeadIndex extends Index
    {

        int level;

        public HeadIndex(Index right, Index down, Node node, int level)
        {
            super(right, down, node);
            this.level = level;
        }
    }
}
