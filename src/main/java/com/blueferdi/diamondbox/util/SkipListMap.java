/*
 * Copyright (C) 2013 guang
 */
package com.blueferdi.diamondbox.util;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;

/**
 *
 * @author ferdi email:ferdi@blueferdi.com
 * @since Sep 17, 2013 12:43:48 PM
 * @version 0.0.1
 */
public class SkipListMap<K, V> extends AbstractMap<K, V>
        implements Map<K, V>, Cloneable, Serializable
{

    public static long start = 0,stop = 0,randomCost = 0,compareCost = 0,findPreCost = 0,indexCost = 0,scanCost = 0,usingFindNode = 0;
    
    private static final long serialVersionUID = -8627078645895051610L;
    /**
     * Generates the initial random seed for the cheaper per-instance random
     * number generators used in randomLevel.
     */
    private static final Random seedGenerator = new Random();
    /**
     * Special value used to identify base-level header
     */
    private static final Object BASE_HEADER = new Object();
    /**
     * The topmost head index of the skiplist.
     */
    private transient volatile HeadIndex<K, V> head;
    /**
     * The comparator used to maintain order in this map, or null if using
     * natural ordering.
     *
     * @serial
     */
    private final Comparator<? super K> comparator;
    /**
     * Seed for simple random number generator. Not volatile since it doesn't
     * matter too much if different threads don't see updates.
     */
    private transient int randomSeed;
    /**
     * Lazily initialized key set
     */
    private transient KeySet keySet;
    /**
     * Lazily initialized entry set
     */
    private transient EntrySet entrySet;
    /**
     * Lazily initialized values collection
     */
    private transient Values values;

    /**
     * Initializes or resets state. Needed by constructors, clone, clear,
     * readObject. and ConcurrentSkipListSet.clone. (Note that comparator must
     * be separately initialized.)
     */
    final void initialize()
    {
        keySet = null;
        entrySet = null;
        values = null;
        randomSeed = seedGenerator.nextInt() | 0x0100; // ensure nonzero
        head = new HeadIndex<K, V>(new Node<K, V>(null, BASE_HEADER, null),
                null, null, 1);
    }

    /* ---------------- Constructors -------------- */
    /**
     * Constructs a new, empty map, sorted according to the
     * {@linkplain Comparable natural ordering} of the keys.
     */
    public SkipListMap()
    {
        this.comparator = null;
        initialize();
    }

    /**
     * Constructs a new, empty map, sorted according to the specified
     * comparator.
     *
     * @param comparator the comparator that will be used to order this map. If
     * <tt>null</tt>, the {@linkplain Comparable natural
     *        ordering} of the keys will be used.
     */
    public SkipListMap(Comparator<? super K> comparator)
    {
        this.comparator = comparator;
        initialize();
    }

    /**
     * Constructs a new map containing the same mappings as the given map,
     * sorted according to the {@linkplain Comparable natural ordering} of the
     * keys.
     *
     * @param m the map whose mappings are to be placed in this map
     * @throws ClassCastException if the keys in <tt>m</tt> are not
     * {@link Comparable}, or are not mutually comparable
     * @throws NullPointerException if the specified map or any of its keys or
     * values are null
     */
    public SkipListMap(Map<? extends K, ? extends V> m)
    {
        this.comparator = null;
        initialize();
        putAll(m);
    }

    /**
     * Constructs a new map containing the same mappings and using the same
     * ordering as the specified sorted map.
     *
     * @param m the sorted map whose mappings are to be placed in this map, and
     * whose comparator is to be used to sort this map
     * @throws NullPointerException if the specified sorted map or any of its
     * keys or values are null
     */
    public SkipListMap(SortedMap<K, ? extends V> m)
    {
        this.comparator = m.comparator();
        initialize();
        buildFromSorted(m);
    }

    /**
     * set head node
     */
    private void head(HeadIndex<K, V> val)
    {
        this.head = val;
    }

    /* ---------------- Comparison utilities -------------- */
    /**
     * Represents a key with a comparator as a Comparable.
     *
     * Because most sorted collections seem to use natural ordering on
     * Comparables (Strings, Integers, etc), most internal methods are geared to
     * use them. This is generally faster than checking per-comparison whether
     * to use comparator or comparable because it doesn't require a (Comparable)
     * cast for each comparison. (Optimizers can only sometimes remove such
     * redundant checks themselves.) When Comparators are used,
     * ComparableUsingComparators are created so that they act in the same way
     * as natural orderings. This penalizes use of Comparators vs Comparables,
     * which seems like the right tradeoff.
     */
    static final class ComparableUsingComparator<K> implements Comparable<K>
    {

        final K actualKey;
        final Comparator<? super K> cmp;

        ComparableUsingComparator(K key, Comparator<? super K> cmp)
        {
            this.actualKey = key;
            this.cmp = cmp;
        }

        public int compareTo(K k2)
        {
            return cmp.compare(actualKey, k2);
        }
    }

    /**
     * If using comparator, return a ComparableUsingComparator, else cast key as
     * Comparable, which may cause ClassCastException, which is propagated back
     * to caller.
     */
    private Comparable<? super K> comparable(Object key) throws ClassCastException
    {
        
        if (key == null)
        {
            throw new NullPointerException();
        }
        if (comparator != null)
        {
            return new ComparableUsingComparator<K>((K) key, comparator);
        }
        else
        {
            return (Comparable<? super K>) key;
        }     
        
    }

    /**
     * Compares using comparator or natural ordering. Used when the
     * ComparableUsingComparator approach doesn't apply.
     */
    int compare(K k1, K k2) throws ClassCastException
    {
        Comparator<? super K> cmp = comparator;
        if (cmp != null)
        {
            return cmp.compare(k1, k2);
        }
        else
        {
            return ((Comparable<? super K>) k1).compareTo(k2);
        }
    }

    /**
     * Returns true if given key greater than or equal to least and strictly
     * less than fence, bypassing either test if least or fence are null. Needed
     * mainly in submap operations.
     */
    boolean inHalfOpenRange(K key, K least, K fence)
    {
        if (key == null)
        {
            throw new NullPointerException();
        }
        return ((least == null || compare(key, least) >= 0)
                && (fence == null || compare(key, fence) < 0));
    }

    /**
     * Returns true if given key greater than or equal to least and less or
     * equal to fence. Needed mainly in submap operations.
     */
    boolean inOpenRange(K key, K least, K fence)
    {
        if (key == null)
        {
            throw new NullPointerException();
        }
        return ((least == null || compare(key, least) >= 0)
                && (fence == null || compare(key, fence) <= 0));
    }

    /* ---------------- Traversal -------------- */
    /**
     * Returns a base-level node with key strictly less than given key, or the
     * base-level header if there is no such node. Also unlinks indexes to
     * deleted nodes found along the way. Callers rely on this side-effect of
     * clearing indices to deleted nodes.
     *
     * @param key the key
     * @return a predecessor of key
     */
    private Node<K, V> findPredecessor(Comparable<? super K> key)
    {
        if (key == null)
        {
            throw new NullPointerException(); // don't postpone errors
        }
        for (;;)
        {
            Index<K, V> q = head;
            Index<K, V> r = q.right;
            for (;;)
            {
                if (r != null)
                {
                    Node<K, V> n = r.node;
                    K k = n.key;
//                    if (n.value == null)
//                    {
//                        q.unlink(r);
//                        r = q.right;         // reread r
//                        continue;
//                    }
                    if (key.compareTo(k) > 0)
                    {
                        q = r;
                        r = r.right;
                        continue;
                    }
                }
                Index<K, V> d = q.down;
                if (d != null)
                {
                    q = d;
                    r = d.right;
                }
                else
                {
                    return q.node;
                }
            }
        }
    }

    /**
     * Returns node holding key or null if no such, clearing out any deleted
     * nodes seen along the way. Repeatedly traverses at base-level looking for
     * key starting at predecessor returned from findPredecessor, processing
     * base-level deletions as encountered. Some callers rely on this
     * side-effect of clearing deleted nodes.
     *
     * Restarts occur, at traversal step centered on node n, if:
     *
     * (1) After reading n's next field, n is no longer assumed predecessor b's
     * current successor, which means that we don't have a consistent 3-node
     * snapshot and so cannot unlink any subsequent deleted nodes encountered.
     *
     * (2) n's value field is null, indicating n is deleted, in which case we
     * help out an ongoing structural deletion before retrying. Even though
     * there are cases where such unlinking doesn't require restart, they aren't
     * sorted out here because doing so would not usually outweigh cost of
     * restarting.
     *
     * (3) n is a marker or n's predecessor's value field is null, indicating
     * (among other possibilities) that findPredecessor returned a deleted node.
     * We can't unlink the node because we don't know its predecessor, so rely
     * on another call to findPredecessor to notice and return some earlier
     * predecessor, which it will do. This check is only strictly needed at
     * beginning of loop, (and the b.value check isn't strictly needed at all)
     * but is done each iteration to help avoid contention with other threads by
     * callers that will fail to be able to change links, and so will retry
     * anyway.
     *
     * The traversal loops in doPut, doRemove, and findNear all include the same
     * three kinds of checks. And specialized versions appear in findFirst, and
     * findLast and their variants. They can't easily share code because each
     * uses the reads of fields held in locals occurring in the orders they were
     * performed.
     *
     * @param key the key
     * @return node holding key, or null if no such
     */
    private Node<K, V> findNode(Comparable<? super K> key)
    {
        for (;;)
        {
            Node<K, V> b = findPredecessor(key);
            Node<K, V> n = b.next;
            for (;;)
            {
                if (n == null)
                {
                    return null;
                }
                Node<K, V> f = n.next;
                Object v = n.value;
                if (v == null)
                {                // n is deleted
                    n.helpDelete(b, f);
                    break;
                }
                int c = key.compareTo(n.key);
                if (c == 0)
                {
                    return n;
                }
                if (c < 0)
                {
                    return null;
                }
                b = n;
                n = f;
            }
        }
    }

    /**
     * Specialized variant of findNode to perform Map.get. Does a weak
     * traversal, not bothering to fix any deleted index nodes, returning early
     * if it happens to see key in index, and passing over any deleted base
     * nodes, falling back to getUsingFindNode only if it would otherwise return
     * value from an ongoing deletion. Also uses "bound" to eliminate need for
     * some comparisons (see Pugh Cookbook). Also folds uses of null checks and
     * node-skipping because markers have null keys.
     *
     * @param okey the key
     * @return the value, or null if absent
     */
    private V doGet(Object okey)
    {
        Comparable<? super K> key = comparable(okey);
        Node<K, V> bound = null;
        Index<K, V> q = head;
        Index<K, V> r = q.right;
        Node<K, V> n;
        K k;
        int c;
        
        start = System.currentTimeMillis();
        
        for (;;)
        {
            Index<K, V> d;
            // Traverse rights
            if (r != null && (n = r.node) != bound && (k = n.key) != null)
            {
                if ((c = key.compareTo(k)) > 0)
                {
                    q = r;
                    r = r.right;
                    continue;
                }
                else if (c == 0)
                {
                    Object v = n.value;
                    return (v != null) ? (V) v : getUsingFindNode(key);
                }
                else
                {
                    bound = n;
                }
            }

            // Traverse down
            if ((d = q.down) != null)
            {
                q = d;
                r = d.right;
            }
            else
            {
                break;
            }
        }
        
        stop = System.currentTimeMillis();
        
        indexCost += (stop - start);
        
        start = System.currentTimeMillis();
        
        // Traverse nexts
        for (n = q.node.next; n != null; n = n.next)
        {
            if ((k = n.key) != null)
            {
                if ((c = key.compareTo(k)) == 0)
                {
                    Object v = n.value;
                    return (v != null) ? (V) v : getUsingFindNode(key);
                }
                else if (c < 0)
                {
                    break;
                }
            }
        }
        
        stop = System.currentTimeMillis();
        
        scanCost += (stop - start);
        
        return null;
    }

    /**
     * Performs map.get via findNode. Used as a backup if doGet encounters an
     * in-progress deletion.
     *
     * @param key the key
     * @return the value, or null if absent
     */
    private V getUsingFindNode(Comparable<? super K> key)
    {
        /*
         * Loop needed here and elsewhere in case value field goes
         * null just as it is about to be returned, in which case we
         * lost a race with a deletion, so must retry.
         */
        for (;;)
        {
            Node<K, V> n = findNode(key);
            if (n == null)
            {
                return null;
            }
            Object v = n.value;
            if (v != null)
            {
                return (V) v;
            }
        }
    }

    /* ---------------- Insertion -------------- */
    /**
     * Main insertion method. Adds element if not present, or replaces value if
     * present and onlyIfAbsent is false.
     *
     * @param kkey the key
     * @param value the value that must be associated with key
     * @param onlyIfAbsent if should not insert if already present
     * @return the old value, or null if newly inserted
     */
    private V doPut(K kkey, V value, boolean onlyIfAbsent)
    {
        start = System.currentTimeMillis();
        
        Comparable<? super K> key = comparable(kkey);
        
        stop = System.currentTimeMillis();
        
        compareCost += (stop - start);
        
        for (;;)
        {
            start = System.currentTimeMillis();
            
            Node<K, V> b = findPredecessor(key);
            
            stop = System.currentTimeMillis();
            
            findPreCost += (stop - start);
            
            Node<K, V> n = b.next;
            for (;;)
            {
                if (n != null)
                {
                    Node<K, V> f = n.next;
                    Object v = n.value;
                    int c = key.compareTo(n.key);
                    if (c > 0)
                    {
                        b = n;
                        n = f;
                        continue;
                    }
                    if (c == 0)
                    {
                        if (onlyIfAbsent)
                        {
                            return (V) v;
                        }
                        else
                        {
                            n.value = v;
                            return (V) v;
                        }
                    }
                    // else c < 0; fall through
                }

                Node<K, V> z = new Node<K, V>(kkey, value, n);
                b.next = z;
                
                start = System.currentTimeMillis();
                int level = randomLevel();
                stop = System.currentTimeMillis();
                randomCost += (stop - start);
                
                if (level > 0)
                {
                    insertIndex(z, level);
                }
                return null;
            }
        }
    }

    /**
     * Creates and adds index nodes for the given node.
     *
     * @param z the node
     * @param level the level of the index
     */
    private void insertIndex(Node<K, V> z, int level)
    {
        HeadIndex<K, V> h = head;
        int max = h.level;

        if (level <= max)
        {
            Index<K, V> idx = null;
            for (int i = 1; i <= level; ++i)
            {
                idx = new Index<K, V>(z, idx, null);
            }
            addIndex(idx, h, level);

        }
        else
        {
            /*
             * add new level in single Thread
             */
            level = max + 1;
            Index<K, V> idx = null, idxNew = null;
            for (int i = 1; i <= max; ++i)
            {
                idx = new Index<K, V>(z, idx, null);
            }

            idxNew = new Index<K, V>(z, idx, null);

            HeadIndex<K, V> newh = h;
            Node<K, V> oldbase = h.node;
            newh = new HeadIndex<K, V>(oldbase, newh, idxNew, level);
            head(newh);

            addIndex(idx, h, max);
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
    private void addIndex(Index<K, V> idx, HeadIndex<K, V> h, int indexLevel)
    {
        // Track next level to insert in case of retries
        int insertionLevel = indexLevel;
        Comparable<? super K> key = comparable(idx.node.key);
        if (key == null)
        {
            throw new NullPointerException();
        }

        // Similar to findPredecessor, but adding index nodes along
        // path to key.
        for (;;)
        {
            int j = h.level;
            Index<K, V> q = h;
            Index<K, V> r = q.right;
            Index<K, V> t = idx;
            for (;;)
            {
                if (r != null)
                {
                    Node<K, V> n = r.node;
                    int c = key.compareTo(n.key);
                    if (c > 0)
                    {
                        q = r;
                        r = r.right;
                        continue;
                    }
                }

                if (j == insertionLevel)
                {
                    q.link(r, t);
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

    /* ---------------- Deletion -------------- */
    /**
     * Main deletion method. Locates node, nulls value, appends a deletion
     * marker, unlinks predecessor, removes associated index nodes, and possibly
     * reduces head index level.
     *
     * Index nodes are cleared out simply by calling findPredecessor. which
     * unlinks indexes to deleted nodes found along path to key, which will
     * include the indexes to this node. This is done unconditionally. We can't
     * check beforehand whether there are index nodes because it might be the
     * case that some or all indexes hadn't been inserted yet for this node
     * during initial search for it, and we'd like to ensure lack of garbage
     * retention, so must call to be sure.
     *
     * @param okey the key
     * @param value if non-null, the value that must be associated with key
     * @return the node, or null if not found
     */
    final V doRemove(Object okey, Object value)
    {
        Comparable<? super K> key = comparable(okey);
        for (;;)
        {
            Node<K, V> b = findPredecessor(key);
            Node<K, V> n = b.next;
            for (;;)
            {
                if (n == null)
                {
                    return null;
                }
                Node<K, V> f = n.next;
                Object v = n.value;
                int c = key.compareTo(n.key);
                if (c < 0)
                {
                    return null;
                }
                if (c > 0)
                {
                    b = n;
                    n = f;
                    continue;
                }
                n.value = null;
                n.appendMarker(f);
                b.next = f;

                findPredecessor(key);// Clean index

                if (head.right == null)
                {
                    tryReduceLevel();
                }

                return (V) v;
            }
        }
    }

    /**
     * Possibly reduce head level if it has no nodes. This method can (rarely)
     * make mistakes, in which case levels can disappear even though they are
     * about to contain index nodes. This impacts performance, not correctness.
     * To minimize mistakes as well as to reduce hysteresis, the level is
     * reduced by one only if the topmost three levels look empty. Also, if the
     * removed level looks non-empty after CAS, we try to change it back quick
     * before anyone notices our mistake! (This trick works pretty well because
     * this method will practically never make mistakes unless current thread
     * stalls immediately before first CAS, in which case it is very unlikely to
     * stall again immediately afterwards, so will recover.)
     *
     * We put up with all this rather than just let levels grow because
     * otherwise, even a small map that has undergone a large number of
     * insertions and removals will have a lot of levels, slowing down access
     * more than would an occasional unwanted reduction.
     */
    private void tryReduceLevel()
    {
        HeadIndex<K, V> h = head;
        HeadIndex<K, V> d;
        HeadIndex<K, V> e;
        if (h.level > 3
                && (d = (HeadIndex<K, V>) h.down) != null
                && (e = (HeadIndex<K, V>) d.down) != null
                && e.right == null
                && d.right == null
                && h.right == null)
        {
            head(d);
            if (h.right != null)//recheck
            {
                head(h);// try to backout
            }
        }
    }

    /* ---------------- Finding and removing first element -------------- */
    /**
     * Specialized variant of findNode to get first valid node.
     *
     * @return first node or null if empty
     */
    Node<K, V> findFirst()
    {
        for (;;)
        {
            Node<K, V> b = head.node;
            Node<K, V> n = b.next;
            if (n == null)
            {
                return null;
            }
            return n;
        }
    }

    /**
     * Removes first entry; returns its snapshot.
     *
     * @return null if empty, else snapshot of first entry
     */
    Map.Entry<K, V> doRemoveFirstEntry()
    {
        for (;;)
        {
            Node<K, V> b = head.node;
            Node<K, V> n = b.next;
            if (n == null)
            {
                return null;
            }
            Node<K, V> f = n.next;
            Object v = n.value;
            n.value = null;

            n.appendMarker(f);
            b.next = f;
            clearIndexToFirst();
            return new SimpleImmutableEntry<K, V>(n.key, (V) v);
        }
    }

    /**
     * Clears out index nodes associated with deleted first entry.
     */
    private void clearIndexToFirst()
    {
        for (;;)
        {
            Index<K, V> q = head;
            for (;;)
            {
                Index<K, V> r = q.right;
                if (r != null && r.indexesDeletedNode() && r.indexesDeletedNode())
                {
                    q.unlink(r);
                }

                if ((q = q.down) == null)
                {
                    if (head.right == null)
                    {
                        tryReduceLevel();
                    }
                    return;
                }
            }
        }
    }

    /**
     * Streamlined bulk insertion to initialize from elements of given sorted
     * map. Call only from constructor or clone method.
     */
    private void buildFromSorted(SortedMap<K, ? extends V> map)
    {
        if (map == null)
        {
            throw new NullPointerException();
        }

        HeadIndex<K, V> h = head;
        Node<K, V> basepred = h.node;

        // Track the current rightmost node at each level. Uses an
        // ArrayList to avoid committing to initial or maximum level.
        ArrayList<Index<K, V>> preds = new ArrayList<Index<K, V>>();

        // initialize
        for (int i = 0; i <= h.level; ++i)
        {
            preds.add(null);
        }
        Index<K, V> q = h;
        for (int i = h.level; i > 0; --i)
        {
            preds.set(i, q);
            q = q.down;
        }

        Iterator<? extends Map.Entry<? extends K, ? extends V>> it =
                map.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry<? extends K, ? extends V> e = it.next();
            int j = randomLevel();
            if (j > h.level)
            {
                j = h.level + 1;
            }
            K k = e.getKey();
            V v = e.getValue();
            if (k == null || v == null)
            {
                throw new NullPointerException();
            }
            Node<K, V> z = new Node<K, V>(k, v, null);
            basepred.next = z;
            basepred = z;
            if (j > 0)
            {
                Index<K, V> idx = null;
                for (int i = 1; i <= j; ++i)
                {
                    idx = new Index<K, V>(z, idx, null);
                    if (i > h.level)
                    {
                        h = new HeadIndex<K, V>(h.node, h, idx, i);
                    }

                    if (i < preds.size())
                    {
                        preds.get(i).right = idx;
                        preds.set(i, idx);
                    }
                    else
                    {
                        preds.add(idx);
                    }
                }
            }
        }
        head = h;
    }

    public Set<Entry<K, V>> entrySet()
    {
        Set<Map.Entry<K, V>> es = entrySet;
        return es != null ? es : (entrySet = new EntrySet());
    }

    Iterator<K> keyIterator()
    {
        return new KeyIterator();
    }

    final class KeyIterator extends Iter<K>
    {

        public K next()
        {
            K k = next.key;
            advance();
            return k;
        }
    }

    final class KeySet extends AbstractSet<K>
    {

        @Override
        public Iterator<K> iterator()
        {
            return keyIterator();
        }

        @Override
        public int size()
        {
            return this.size();
        }
    }

    Iterator<V> valueIterator()
    {
        return new ValueIterator();
    }

    final class ValueIterator extends Iter<V>
    {

        public V next()
        {
            V v = nextValue;
            advance();
            return v;
        }
    }

    final class Values extends AbstractCollection<V>
    {

        @Override
        public Iterator<V> iterator()
        {
            return valueIterator();
        }

        @Override
        public int size()
        {
            return this.size();
        }
    }

    private final class EntryIterator extends Iter<Entry<K, V>>
    {

        public Entry<K, V> next()
        {
            return next;
        }
    }

    Iterator<Entry<K, V>> newEntryIterator()
    {
        return new EntryIterator();
    }

    final class EntrySet extends AbstractSet<Entry<K, V>>
    {

        public Iterator<Entry<K, V>> iterator()
        {
            return newEntryIterator();
        }

        @Override
        public boolean contains(Object o)
        {
            return this.contains(o);
        }

        @Override
        public boolean remove(Object o)
        {
            return this.remove(o);
        }

        @Override
        public void clear()
        {
            this.clear();
        }

        @Override
        public int size()
        {
            return this.size();
        }
    }

    /* ---------------- Iterators -------------- */
    /**
     * Base of iterator classes:
     */
    abstract class Iter<T> implements Iterator<T>
    {

        /**
         * the last node returned by next()
         */
        Node<K, V> lastReturned;
        /**
         * the next node to return from next();
         */
        Node<K, V> next;
        /**
         * Cache of next value field to maintain weak consistency
         */
        V nextValue;

        /**
         * Initializes ascending iterator for entire range.
         */
        Iter()
        {
            for (;;)
            {
                next = findFirst();
                if (next == null)
                {
                    break;
                }
                Object x = next.value;
                if (x != null && x != next)
                {
                    nextValue = (V) x;
                    break;
                }
            }
        }

        public final boolean hasNext()
        {
            return next != null;
        }

        /**
         * Advances next to higher entry.
         */
        final void advance()
        {
            if (next == null)
            {
                throw new NoSuchElementException();
            }
            lastReturned = next;
            for (;;)
            {
                next = next.next;
                if (next == null)
                {
                    break;
                }
                Object x = next.value;
                if (x != null && x != next)
                {
                    nextValue = (V) x;
                    break;
                }
            }
        }

        public void remove()
        {
            Node<K, V> l = lastReturned;
            if (l == null)
            {
                throw new IllegalStateException();
            }
            // It would not be worth all of the overhead to directly
            // unlink from here. Using remove is fast enough.
            SkipListMap.this.remove(l.key);
            lastReturned = null;
        }
    }

    /* ------ Map API methods ------ */
    /**
     * Returns <tt>true</tt> if this map contains a mapping for the specified
     * key.
     *
     * @param key key whose presence in this map is to be tested
     * @return <tt>true</tt> if this map contains a mapping for the specified
     * key
     * @throws ClassCastException if the specified key cannot be compared with
     * the keys currently in the map
     * @throws NullPointerException if the specified key is null
     */
    @Override
    public boolean containsKey(Object key)
    {
        return doGet(key) != null;
    }

    /**
     * Returns the value to which the specified key is mapped, or {@code null}
     * if this map contains no mapping for the key.
     *
     * <p>More formally, if this map contains a mapping from a key {@code k} to
     * a value {@code v} such that {@code key} compares equal to {@code k}
     * according to the map's ordering, then this method returns {@code v};
     * otherwise it returns {@code null}. (There can be at most one such
     * mapping.)
     *
     * @throws ClassCastException if the specified key cannot be compared with
     * the keys currently in the map
     * @throws NullPointerException if the specified key is null
     */
    @Override
    public V get(Object key)
    {
        return doGet(key);
    }

    /**
     * Associates the specified value with the specified key in this map. If the
     * map previously contained a mapping for the key, the old value is
     * replaced.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with the specified key, or
     * <tt>null</tt> if there was no mapping for the key
     * @throws ClassCastException if the specified key cannot be compared with
     * the keys currently in the map
     * @throws NullPointerException if the specified key or value is null
     */
    @Override
    public V put(K key, V value)
    {
        if (value == null)
        {
            throw new NullPointerException();
        }
        return doPut(key, value, false);
    }

    /**
     * Removes the mapping for the specified key from this map if present.
     *
     * @param key key for which mapping should be removed
     * @return the previous value associated with the specified key, or
     * <tt>null</tt> if there was no mapping for the key
     * @throws ClassCastException if the specified key cannot be compared with
     * the keys currently in the map
     * @throws NullPointerException if the specified key is null
     */
    @Override
    public V remove(Object key)
    {
        return doRemove(key, null);
    }

    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the specified
     * value. This operation requires time linear in the map size.
     *
     * @param value value whose presence in this map is to be tested
     * @return <tt>true</tt> if a mapping to <tt>value</tt> exists;
     * <tt>false</tt> otherwise
     * @throws NullPointerException if the specified value is null
     */
    @Override
    public boolean containsValue(Object value)
    {
        if (value == null)
        {
            throw new NullPointerException();
        }
        for (Node<K, V> n = findFirst(); n != null; n = n.next)
        {
            V v = n.getValidValue();
            if (v != null && value.equals(v))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the number of key-value mappings in this map. If this map
     * contains more than <tt>Integer.MAX_VALUE</tt> elements, it returns
     * <tt>Integer.MAX_VALUE</tt>.
     *
     * <p>Beware that, unlike in most collections, this method is
     * <em>NOT</em> a constant-time operation. Because of the asynchronous
     * nature of these maps, determining the current number of elements requires
     * traversing them all to count them. Additionally, it is possible for the
     * size to change during execution of this method, in which case the
     * returned result will be inaccurate. Thus, this method is typically not
     * very useful in concurrent applications.
     *
     * @return the number of elements in this map
     */
    @Override
    public int size()
    {
        long count = 0;
        for (Node<K, V> n = findFirst(); n != null; n = n.next)
        {
            if (n.getValidValue() != null)
            {
                ++count;
            }
        }
        return (count >= Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) count;
    }

    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     *
     * @return <tt>true</tt> if this map contains no key-value mappings
     */
    @Override
    public boolean isEmpty()
    {
        return findFirst() == null;
    }

    /**
     * Removes all of the mappings from this map.
     */
    @Override
    public void clear()
    {
        initialize();
    }

    /* ---------------- Nodes -------------- */
    /**
     * Nodes hold keys and values, and are singly linked in sorted order,
     * possibly with some intervening marker nodes. The list is headed by a
     * dummy node accessible as head.node. The value field is declared only as
     * Object because it takes special non-V values for marker and header nodes.
     */
    static final class Node<K, V> implements Map.Entry<K, V>
    {

        final K key;
        Object value = this;
        Node<K, V> next;

        /**
         * Creates a new regular node.
         */
        Node(K key, Object value, Node<K, V> next)
        {
            this.key = key;
            this.value = value;
            this.next = next;
        }

        /**
         * Creates a new marker node. A marker is distinguished by having its
         * value field point to itself. Marker nodes also have null keys, a fact
         * that is exploited in a few places, but this doesn't distinguish
         * markers from the base-level header node (head.node), which also has a
         * null key.
         */
        Node(Node<K, V> next)
        {
            this.key = null;
            this.next = next;
        }

        /**
         * Returns true if this node is a marker. This method isn't actually
         * called in any current code checking for markers because callers will
         * have already read value field and need to use that read (not another
         * done here) and so directly test if value points to node.
         *
         * @param n a possibly null reference to a node
         * @return true if this node is a marker node
         */
        boolean isMarker()
        {
            return value == this;
        }

        /**
         * Returns true if this node is the header of base-level list.
         *
         * @return true if this node is header node
         */
        boolean isBaseHeader()
        {
            return value == BASE_HEADER;
        }

        /**
         * Tries to append a deletion marker to this node.
         *
         * @param f the assumed current successor of this node
         * @return true if successful
         */
        void appendMarker(Node<K, V> f)
        {
            this.next = new Node<K, V>(f);
        }

        /**
         * Helps out a deletion by appending marker or unlinking from
         * predecessor. This is called during traversals when value field seen
         * to be null.
         *
         * @param b predecessor
         * @param f successor
         */
        void helpDelete(Node<K, V> b, Node<K, V> f)
        {
            /*
             * Rechecking links and then doing only one of the
             * help-out stages per call tends to minimize CAS
             * interference among helping threads.
             */
            if (f == next && this == b.next)
            {
                if (f == null || f.value != f) // not already marked
                {
                    appendMarker(f);
                }
                else
                {
                    b.next = f.next;
                }
            }
        }

        /**
         * Returns value if this node contains a valid key-value pair, else
         * null.
         *
         * @return this node's value if it isn't a marker or header or is
         * deleted, else null.
         */
        V getValidValue()
        {
            Object v = value;
            if (v == this || v == BASE_HEADER)
            {
                return null;
            }
            return (V) v;
        }

        /**
         * Creates and returns a new SimpleImmutableEntry holding current
         * mapping if this node holds a valid value, else null.
         *
         * @return new entry or null
         */
        SimpleImmutableEntry<K, V> createSnapshot()
        {
            V v = getValidValue();
            if (v == null)
            {
                return null;
            }
            return new SimpleImmutableEntry<K, V>(key, v);
        }

        public K getKey()
        {
            return this.key;
        }

        public V getValue()
        {
            return this.value == this ? null : (V) this.value;
        }

        public V setValue(V value)
        {
            V old = getValue();
            this.value = value;
            return old;
        }
    }

    /* ---------------- Indexing -------------- */
    /**
     * Index nodes represent the levels of the skip list. Note that even though
     * both Nodes and Indexes have forward-pointing fields, they have different
     * types and are handled in different ways, that can't nicely be captured by
     * placing field in a shared abstract class.
     */
    static class Index<K, V>
    {

        final Node<K, V> node;
        final Index<K, V> down;
        volatile Index<K, V> right;

        /**
         * Creates index node with given values.
         */
        Index(Node<K, V> node, Index<K, V> down, Index<K, V> right)
        {
            this.node = node;
            this.down = down;
            this.right = right;
        }

        /**
         * set right field
         */
        final void right(Index<K, V> val)
        {
            this.right = val;
        }

        /**
         * Returns true if the node this indexes has been deleted.
         *
         * @return true if indexed node is known to be deleted
         */
        final boolean indexesDeletedNode()
        {
            return node.value == null;
        }

        /**
         * link the new node.
         *
         * @param succ the expected current successor
         * @param newSucc the new successor
         * @return true if successful
         */
        final void link(Index<K, V> succ, Index<K, V> newSucc)
        {
            newSucc.right = succ;
            right(newSucc);
        }

        /**
         * unlink the succ to this node
         *
         * @param succ the expected current successor
         * @return true if successful
         */
        final void unlink(Index<K, V> succ)
        {
            right(succ.right);
        }
    }

    /* ---------------- Head nodes -------------- */
    /**
     * Nodes heading each level keep track of their level.
     */
    static final class HeadIndex<K, V> extends Index<K, V>
    {

        final int level;

        HeadIndex(Node<K, V> node, Index<K, V> down, Index<K, V> right, int level)
        {
            super(node, down, right);
            this.level = level;
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
}
