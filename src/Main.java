import javax.swing.tree.TreeNode;
import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) {


        Map<String, String> map = new HashMap<>(10);

        map.put("刘备", "蜀国");
        map.put("孙策", "吴国");

        map.get("刘备");

        map.remove("刘备");

        int h;
        // 哈希值为674287
        h = "刘备".hashCode();
        // ^ 异或运算
        // 0 0得0
        // 1 0得1
        // 1 1得0
        // >>> 左边补零且都是正数，例如 >>>16 则左边补齐16个零，溢出部分删除
        // 存储在HashMap里面真实的哈希为：h ^ (h >>> 16)
    }

    /**
     * 对于初始化的值进行计算，返回2倍大小的幂
     * 比方说：设置初始值为1 则HashMap大小为2 设置为10，则HashMap大小为16
     * @param cap
     * @return
     */
    static final int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    /**
     * HashMap存储结构
     * @param <K>
     * @param <V>
     */
    static class Node<K,V> implements Map.Entry<K,V> {
        // 哈希值
        final int hash;
        // 键
        final K key;
        // 值
        V value;
        // 单向链表
        HashMap.Node<K, V> next;

        // 单向链表
        Node(int hash, K key, V value, HashMap.Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }



    /**
     * 减少哈希碰撞，使其数组分散均匀
     * @param key
     * @return
     */
    static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    /**
     * 添加
     * @param hash
     * @param key
     * @param value
     * @param onlyIfAbsent
     * @param evict
     * @return
     */
    final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
        // 初始化一个数组链表
        HashMap.Node<K,V>[] tab;
        HashMap.Node<K,V> p;
        int n, i;
        // 判断当前的数组是否已经进行了初始化
        if ((tab = table) == null || (n = tab.length) == 0) {
            // 没有初始化，去初始化HashMap的空间，扩容数组
            // tab 当前的Node数组
            n = (tab = resize()).length;
        }
        // 计算hashCode放到数组
        // 表示当前index位置没有存储元素
        if ((p = tab[i = (n - 1) & hash]) == null) {
            // 添加进节点里面，第一次先map.put("刘备", "蜀国")
            tab[i] = newNode(hash, key, value, null);
        }
        // 如果有存储元素，第二次map.put("刘备", "蜀国1")
        else {
            HashMap.Node<K,V> e;
            K k;
            // 判断当前的key是否已经存在了，由于key("刘备")存在了
            if (p.hash == hash &&
                    ((k = p.key) == key || (key != null && key.equals(k)))) {
                // 替换相同key里面的value
                // 则最后存储为map.put("刘备", "蜀国1")
                e = p;
            }
            // 判断是否是红黑树还有链表
            else if (p instanceof TreeNode) {
                // 是红黑树
                // 往红黑树插入
                e = ((HashMap.TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
            }

            else {
                // 是链表
                // 遍历当前的链表
                for (int binCount = 0; ; ++binCount) {
                    // 遍历到尾部
                    if ((e = p.next) == null) {
                        // 添加到尾部
                        p.next = newNode(hash, key, value, null);
                        // 判断是否到了8 如果大于等于8 则将链表变成红黑树
                        // TREEIFY_THRESHOLD = 8
                        if (binCount >= TREEIFY_THRESHOLD - 1) {
                            // -1 for 1st
                            // 转成红黑树
                            treeifyBin(tab, hash);
                        }

                        break;
                    }
                    // 判断当前的key是否已经存在了
                    if (e.hash == hash &&
                            ((k = e.key) == key || (key != null && key.equals(k)))) {
                        break;
                    }
                    p = e;
                }
            }
            // 替换旧值
            if (e != null) {
                // existing mapping for key
                V oldValue = e.value;
                if (!onlyIfAbsent || oldValue == null)
                    e.value = value;
                afterNodeAccess(e);
                return oldValue;
            }
        }
        // 快速失败机制
        // 记录对象的修改次数，比如增加、删除、修改
        // 如果在遍历的过程中，一旦发现这个对象的modCount和迭代器存储的modCount不一样，就会报错
        ++modCount;
        // 判断当前HashMap是否到了扩容的阈值 16 * 0.75
        if (++size > threshold) {
            // 进行扩容
            resize();
        }

        afterNodeInsertion(evict);
        return null;
    }

    /**
     * 初始化HashMap大小或者扩容HashMap的操作
     * @return
     */
    final HashMap.Node<K,V>[] resize() {
        HashMap.Node<K,V>[] oldTab = table;
        // 如果没有初始化则oldCap = 0
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
        // threshold是HashMap所能容下的阈值，超阈值则扩容
        // threshold = capacity * loadFactor
        // capacity 为HashMap初始化的值的2次幂
        // 比方说 初始值为1 则 capacity = 2
        // loadFactor 负载因子 默认时0.75
        int oldThr = threshold;
        int newCap, newThr = 0;
        // Node数组已经初始化，扩容长度
        if (oldCap > 0) {
            if (oldCap >= MAXIMUM_CAPACITY) {
                threshold = Integer.MAX_VALUE;
                return oldTab;
            }
            else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                    oldCap >= DEFAULT_INITIAL_CAPACITY) {
                // double threshold
                // 扩容之前的两倍
                newThr = oldThr << 1;
            }

        }
        // 还没有到边界值
        else if (oldThr > 0) {
            // initial capacity was placed in threshold
            newCap = oldThr;
        }
        // 如果都没有设置初始化的长度，则设置HashMap长度是16，预扩容是16 * 0.75 = 12
        else {
            // zero initial threshold signifies using defaults
            // DEFAULT_INITIAL_CAPACITY 1 << 4 = 16 newCap = 16
            newCap = DEFAULT_INITIAL_CAPACITY;
            // newThr = 16 * 0.75 = 12 DEFAULT_LOAD_FACTOR = 0.75f
            newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }
        if (newThr == 0) {
            float ft = (float)newCap * loadFactor;
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                    (int)ft : Integer.MAX_VALUE);
        }
        threshold = newThr;
        // 扩容操作
        @SuppressWarnings({"rawtypes","unchecked"})
        HashMap.Node<K,V>[] newTab = (HashMap.Node<K,V>[])new HashMap.Node[newCap];
        table = newTab;
        if (oldTab != null) {
            for (int j = 0; j < oldCap; ++j) {
                HashMap.Node<K,V> e;
                if ((e = oldTab[j]) != null) {
                    oldTab[j] = null;
                    if (e.next == null)
                        newTab[e.hash & (newCap - 1)] = e;
                    else if (e instanceof HashMap.TreeNode) {
                        // 红黑树的长度小于等于6的时候 转成链表
                        ((HashMap.TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                    }

                    else { // preserve order
                        HashMap.Node<K,V> loHead = null, loTail = null;
                        HashMap.Node<K,V> hiHead = null, hiTail = null;
                        HashMap.Node<K,V> next;
                        // 避免JDK7出现得死循环
                        do {
                            next = e.next;
                            if ((e.hash & oldCap) == 0) {
                                if (loTail == null)
                                    loHead = e;
                                else
                                    loTail.next = e;
                                loTail = e;
                            }
                            else {
                                if (hiTail == null)
                                    hiHead = e;
                                else
                                    hiTail.next = e;
                                hiTail = e;
                            }
                        } while ((e = next) != null);
                        if (loTail != null) {
                            loTail.next = null;
                            newTab[j] = loHead;
                        }
                        if (hiTail != null) {
                            hiTail.next = null;
                            newTab[j + oldCap] = hiHead;
                        }
                    }
                }
            }
        }
        return newTab;
    }

    /**
     * 获取节点信息
     * @param hash
     * @param key
     * @return
     */
    final HashMap.Node<K,V> getNode(int hash, Object key) {
        HashMap.Node<K,V>[] tab;
        HashMap.Node<K,V> first, e;
        int n; K k;
        // HashMap不为null，且长度大于0
        // 数组不为null
        if ((tab = table) != null && (n = tab.length) > 0 &&
                // hashCode位运算定位数组下标
                (first = tab[(n - 1) & hash]) != null) {
            // always check first node
            // 如果取得值刚好是第一个
            // 比较key是否相等、hashCode是否相等
            if (first.hash == hash &&
                    ((k = first.key) == key || (key != null && key.equals(k)))) {
                // 返回第一个
                return first;
            }
            // 不是第一个，且数组底下的链表或者红黑树不为null
            if ((e = first.next) != null) {
                // 是红黑树
                if (first instanceof HashMap.TreeNode) {
                    // 查询红黑树返回结果
                    return ((HashMap.TreeNode<K,V>)first).getTreeNode(hash, key);
                }
                // 是链表
                do {
                    // 一个一个遍历出结果
                    if (e.hash == hash &&
                            ((k = e.key) == key || (key != null && key.equals(k)))) {
                        return e;
                    }

                } while ((e = e.next) != null);
            }
        }
        return null;
    }

    /**
     * 删除节点
     * @param hash
     * @param key
     * @param value
     * @param matchValue
     * @param movable
     * @return
     */
    final HashMap.Node<K,V> removeNode(int hash, Object key, Object value,
                                       boolean matchValue, boolean movable) {
        HashMap.Node<K,V>[] tab;
        HashMap.Node<K,V> p;
        int n, index;
        // HashMap不为null且长度切大于0
        // 通过位运算计算key的hashCode确定数组下标，来确定key的位置，不为null
        if ((tab = table) != null && (n = tab.length) > 0 &&
                (p = tab[index = (n - 1) & hash]) != null) {
            HashMap.Node<K,V> node = null, e;
            K k;
            V v;
            // 在数组的key
            if (p.hash == hash &&
                    ((k = p.key) == key || (key != null && key.equals(k)))) {
                node = p;
            }
            // 数组底下的数据结构的key
            else if ((e = p.next) != null) {
                // 在红黑树上的key
                if (p instanceof HashMap.TreeNode) {
                    // 红黑树先序查询出来赋值给一个节点
                    node = ((HashMap.TreeNode<K,V>)p).getTreeNode(hash, key);
                }
                // 在链表上的key
                else {
                    // 链表遍历
                    do {
                        // 确定key
                        if (e.hash == hash &&
                                ((k = e.key) == key ||
                                        (key != null && key.equals(k)))) {
                            node = e;
                            break;
                        }
                        p = e;
                    } while ((e = e.next) != null);
                }
            }
            //
            if (node != null && (!matchValue || (v = node.value) == value ||
                    (value != null && value.equals(v)))) {
                // 红黑树删除
                if (node instanceof HashMap.TreeNode) {
                    ((HashMap.TreeNode<K,V>)node).removeTreeNode(this, tab, movable);
                }
                // 数组删除，且底下是链表
                else if (node == p) {
                    tab[index] = node.next;
                }
                // 链表删除，将要删除的节点的最后一个指向null，就可删除
                else {
                    p.next = node.next;
                }
                ++modCount;
                --size;
                afterNodeRemoval(node);
                return node;
            }
        }
        return null;
    }

}
