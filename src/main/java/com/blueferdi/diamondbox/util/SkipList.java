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
    public V put(K key, V value)
    {
        return doPut(key, value, false);
    }

    public V doPut(K key, V value, boolean ifOnlyAbsent)
    {
        Node q = findPredecessor(key);
        Node n = q.next;

        for (;;)
        {
            if (n != null)
            {
                int c = 0;
                if ((c = comparator.compare(key, n.key)) > 0)
                {
                    q = n;
                    n = q.next;
                    continue;
                }
                else if (c == 0 && !ifOnlyAbsent)
                {
                    Object oldV = n.value;
                    n.value = value;
                    return (V) oldV;
                }
                //when c < 0 fall over
            }

            Node newNode = new Node(key, value, n);
            q.next = newNode;
        }
        //TODO push the index to random level
    }

    /**
     * Creates and adds index nodes for the given node.
     *
     * @param z the node
     * @param level the level of the index
     */
    private void insertIndex(Node z, int level)
    {
        HeadIndex h = head;
        int max = h.level;

        if (level <= max)
        {
            Index idx = null;
            for (int i = 1; i <= level; ++i)
            {
                idx = new Index(idx, null, z);
            }
            addIndex(idx, h, level);

        }
        else
        { // Add a new level
            /*
             * To reduce interference by other threads checking for
             * empty levels in tryReduceLevel, new levels are added
             * with initialized right pointers. Which in turn requires
             * keeping levels in an array to access them while
             * creating new head index nodes from the opposite
             * direction.
             */
            level = max + 1;
            Index newIdx = null;
            
            for (int i = 1; i <= level; ++i)
            {
                newIdx = new Index(newIdx, null, z);
            }
            
            HeadIndex oldh;
            for (;;)
            {
                oldh = head;
                Node oldbase = oldh.node;
                
                HeadIndex newh = new HeadIndex(newIdx, oldh, oldbase, level);
                
                this.head = newh;
                break;
            }
            addIndex(newIdx.down, oldh, level);
        }
        
    }

    /**
     * Adds given index nodes from given level down to 1.
     *
     * @param idx the topmost index node being inserted
     * @param h the value of head to use to insert. This must be snapshotted by
     * callers to provide correct insertion level
     * @param indexLevel the level of the index
     */
    private void addIndex(Index idx, HeadIndex h, int indexLevel)
    {
        // Track next level to insert in case of retries
        int insertionLevel = indexLevel;
        K key = idx.node.key;
        if (key == null)
        {
            throw new NullPointerException();
        }

        // Similar to findPredecessor, but adding index nodes along
        // path to key.
        for (;;)
        {
            int j = h.level;
            Index q = h;
            Index r = q.right;
            Index t = idx;
            for (;;)
            {
                if (r != null)
                {
                    Node n = r.node;
                    // compare before deletion check avoids needing recheck
                    int c = comparator.compare(key, n.key);

                    if (c > 0)
                    {
                        q = r;
                        r = r.right;
                        continue;
                    }
                }

                if (j == insertionLevel)
                {

                    q.right = t;
                    t.right = r;


                    if (--insertionLevel == 0)
                    {
                        return;
                    }
                }

                if (--j >= insertionLevel && j < indexLevel)
                {
                    t = t.down;
                }
                q = q.down;
                r = q.right;
            }
        }
    }

    /**
     * Returns a random level for inserting a new node. Hardwired to k=1, p=0.5,
     * max 31 (see above and Pugh's "Skip List Cookbook", sec 3.4).
     *
     * This uses the simplest of the generators described in George Marsaglia's
     * "Xorshift RNGs" paper. This is not a high-quality generator but is
     * acceptable here.
     */
    private int randomLevel()
    {
        int x = randomSeed;
        x ^= x << 13;
        x ^= x >>> 17;
        randomSeed = x ^= x << 5;
        if ((x & 0x8001) != 0) // test highest and lowest bits
        {
            return 0;
        }
        int level = 1;
        while (((x >>>= 1) & 1) != 0)
        {
            ++level;
        }
        return level;
    }

    /*-------read-------*/
    public V get(K key)
    {
        Node n = null;
        return (n = findNode(key)) != null ? (V) n.value : null;
    }

    private Node findNode(K key)
    {
        Node q = findPredecessor(key);
        Node n = q.next;

        for (;;)
        {
            if (n != null)
            {
                int c = 0;
                if ((c = comparator.compare(key, n.key)) > 0)
                {
                    q = n;
                    n = q.next;
                    continue;
                }
                else if (c == 0)
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
                if ((c = comparator.compare(key, n.key)) > 0)
                {
                    q = r;
                    r = q.right;
                    continue;
                }
                else if (c == 0)
                {
                    return n;
                }

                //when c < 0 fall over
            }

            Index d = q.down;

            if (d == null)
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
