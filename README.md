# 总结
## 数据结构
* JDK7的HashMap
    * 由数组 + 单向链表组成
* JDK8的HashMap
    * 初始的时候：由数组 + 单向链表组成。
    * 当某个数组底下链表的长度大于等于8时：链表转成红黑树，由数组 + 链表 + 红黑树组成。
    * 当红黑树的节点小于等于6时：红黑树转成链表，由数组 + 链表组成。
* 红黑树的特点：
    * // todo
* 单向链表的特点：
    * 只需要修改元素钟的指针，在插入和删除需要O(1)。
    * 按序号查找，需要遍历全部，平均需要O(n)。
    * 按值查找，无论有序无序需要O(n)。
    * 只要有内存，元素个数不受限制。
* 数组的特点：
    * 按序号(数组下标)查找，需要O(1)。
    * 按值查找，无序需要O(n)，有序使用二分查找需要O(logN)
    * 添加与删除平均需要挪动n / 2个元素。
    * 分配相邻的内存地址，个数受分配的地址限制。

## 图解HashMap数据结构    
// todo
## 初始化长度
* 如果没有设置HashMap的初始化长度，默认长度为16
* 如果设置了HashMap的初始化长度，通过位运算都是2的幂次方，且大于等于2的幂次方。
    * 比如设置长度为1，则初始化长度为2
    * 比如设置长度为4，则初始化长度为4
    * 比如设置长度为10，则初始化长度为16
## 添加数据
* 第一：初始化数据结构、初始化HashMap长度。
* 第二：计算key的hashCode，通过一系列的位扰动，确定key的hashCode。
* 第三：数组的位置由(长度 - 1) & key的hashCode决定。**注：由与&的位运算决定，所以2的次幂都要减1。**
* 第四：当key的hashCode产生hash碰撞，JDK8实行的是尾插入，JDK7实行的是头插入。
* 第五：当key与之前存入的key冲突时，替换原先的value。
* 第六：当链表长度大于等于8时，链表转红黑树，在插入。
* 第七：快速失败机制，对象的修改次数不等于迭代器的修改次数一样，抛出异常。
* 第八：当HashMap的长度达到阈值，即 初始化长度 * 0.75的时候，扩容一倍。
**注：假设初始化长度为16，当插入的时候，HashMap的长度来到 16 * 0.75，即12的时候，进行一倍的扩容，最终长度为32**

## JDK8的hashMap缺点的由来
// todo
## JDK7的hashMap缺点的由来
// todo
## 删除数据
// todo
## 查询数据
* 第一：初始化数据结构，通过类的局部变量赋值进来去。
* 第二：通过位运算计算key的hashCode定位数组下标，确定位置。
* 第三：如果在数组上，比较key的hashCode、值是否相等，是则返回，否则进行找。
* 第四：确定数组底下是否链表还是红黑树。
* 第五：是红黑树，则前序遍历(以父、左、右的顺序遍历)出结果。
* 第六：是链表，则从头遍历到尾出结果。
## JDK7和JDK8的比较
// todo
## HashMap阈值的设定
https://blog.csdn.net/qq_42524262/article/details/100624933  
问：hashMap的数组长度为什么要求是2的整数次幂？  
答：为了能让HashMap存取高效，尽量减少Hash碰撞，将数据均与分布各个空间上。
首先让key的hashCode，更均匀松散。
```angular2html
int h;
(h = key.hashCode()) ^ (h >>> 16)
```
先将，hashCode与hashCode的高位补齐16个的值进行异或运算。hashCode取模是：
```angular2html
p = tab[i = (n - 1) & hash]
```
确定数组位置，&运算的特殊性决定了为啥2的次幂。假设 n = 8，则 n - 1 的二进制位111，
当 8 & hashCode 取值都在数组8之间，实现了数组均匀分散。  
  
问：hashMap的负载因子为什么是0.75？  
答：负载因子 * 数组长度就是hashMap扩容的阈值。假设hashMap长度为16，当put值，当前
数组的长度大于等于12，hashMap之后就开始扩容。  
假设，负载因子为1时，数组全部占满时才开始扩容，hash冲突变多，势必造成链表的堆积，
增加查询时间。  
假设，负载因子为0.5时，占到数组一半的时候开始扩容。虽然可以减少查询时间，但是空间的
利用率下降，同时提高了扩容次数，增加性能开销。  
假设，负载因子为0.75，占有数组的3分之2，后续put进来的值，多了之前的一倍 + 之前的三分之一
数组选择的位置，减少了hash碰撞，同时提高了扩容的阈值，减少了扩容次数。  
可以说0.75这个值对空间利用率和时间利用率的折中选择。   
补充：** 我在https://stackoverflow.com/questions/10901752/what-is-the-significance-of-load-factor-in-hashmap 看到一条更好的答案，就是将负载因子设置为0.7左右 **  
问：hashMap为什么当链表的长度为8时转为红黑树？  
答：
```angular2html
* Because TreeNodes are about twice the size of regular nodes, we
     * use them only when bins contain enough nodes to warrant use
     * (see TREEIFY_THRESHOLD). And when they become too small (due to
     * removal or resizing) they are converted back to plain bins.  In
     * usages with well-distributed user hashCodes, tree bins are
     * rarely used.  Ideally, under random hashCodes, the frequency of
     * nodes in bins follows a Poisson distribution
     * (http://en.wikipedia.org/wiki/Poisson_distribution) with a
     * parameter of about 0.5 on average for the default resizing
     * threshold of 0.75, although with a large variance because of
     * resizing granularity. Ignoring variance, the expected
     * occurrences of list size k are (exp(-0.5) * pow(0.5, k) /
     * factorial(k)). The first values are:
     *
     * 0:    0.60653066
     * 1:    0.30326533
     * 2:    0.07581633
     * 3:    0.01263606
     * 4:    0.00157952
     * 5:    0.00015795
     * 6:    0.00001316
     * 7:    0.00000094
     * 8:    0.00000006
     * more: less than 1 in ten million
```
通过泊松分布的概况可知，链表长度等于8的几率是0.00000006，也就是说官方其实也不想，
链表转红黑树。换句话说，8的设定是为了尽可能减少转红黑树，如果当链表长度大于8时，
转红黑树(红黑树虽然增删改查都在O(log2)，但是转化时，所带来性能消耗、空间变大也是一个大问题)，
转红黑树消耗的性能也是可以接受的。

 

 


