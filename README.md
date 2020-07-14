# java-concurrentcy-demo
Java并发训练Demo

## 并发
- 多任务：在同一时刻运行多个程序的能力；
- 进程：独享变量，系统资源分配和调度的基本单位；
- 线程：共享数据；进程的一个实体，不独立存在；是进程的一个执行路径；（对比进程，线程之间通信更有效、更容易、更轻量级、开销少）

单独线程执行一个任务的简单过程
```

// 1.将任务代码移到实现Runnable接口的类的run方法中；
public interface Runnable {
    void run();
} 

// Runnable 是一个函数式接口，可使用lambda表达式建立一个实例
Runnable r = () -> {
    //task code
};

// 2.由Runnable创建一个Thread对象
Thread t = new Thread(r);

// 3.启动线程
t.start();

// 另一种方法
// 并不推介：因为并行运行的任务应该与运行机制解耦合；
class MyThread extend Thread {
    public void run() {
        // task code
    }
}

```

注意：不要直接调用Thread类或Runnable对象中的run()方法；因为直接调用，只会执行同一个线程中的任务，而不会启动新线程；应调用Thread.start()方法；

### 中断线程
-  线程终止：
    -  run()方法执行方法体中最后一条语句，并经由执行return语句返回；
    -  在方法中出现没有捕捉的异常；
    -  Java早期版本，stop()方法强制终止，现被弃用；
    -  interrupt()：请求终止线程；
        -  调用interrupt()：线程**中断状态**被置位(boolean)；
        -  判断当前线程是否被置位：Thread.currentThread().isInterrupted();
        -  若线程被阻塞（sleep或wait），就无法检测中断状态；若调用interrupt()则阻塞调用将会被InterruptedException 异常中断；
        -  中断意义：引起注意，被中断的线程可以决定如何响应中断；
        -  interrupted(): 静态方法，检测当前线程是否被中断；调用该方法会清除该线程的中断状态；
        -  isInterrupted(): 实例方法，检验是否有线程被中断；不会改变线程中断状态；
-  InterruptedException异常的处理：
    -  在catch字句中调用：Thread.currentThread().interrupt()来设置中断状态；
    -  直接抛出该异常： void mySubTask() throws InterruptedException {}
    
### 线程状态
+  New 新创建
+  Runnable 可运行
+  Blocked 被阻塞
+  Waiting 等待
+  Timed waiting 计时等待
+  Terminated  被终止

-  **新创建线程**： new Thread(r);线程被创建但未开始运行；状态为 New；

-  **可运行线程**： 一旦线程调用 start()方法，则该线程处于 Runnable 状态；
    -  Runnable 可运行：线程可能正在运行或者没有运行；这取决于操作系统给线程提供的运行时间；
    -  一旦线程开始运行，不必始终保持运行；
    -  运行中的线程被中断，目的让其他线程获取运行的机会；
    -  线程调度细节依赖操作系统提供的服务：
        -  抢占式调度： 给每一个可运行的线程一个时间片来执行任务；时间片用完，该线程的运行权被操作系统剥夺，并交给另一个线程运行（考虑线程的优先级）；
        -  协作式调度：一个线程只有调用 yield 方法，或者被阻塞或者等待时才失去控制权；
        
-  **被阻塞线程和等待线程**：线程暂时不活动，不运行任何代码且消耗最少资源；
    -  当线程获取一个被其他线程持有的内部锁，则该线程进入**阻塞状态**；当其他线程释放该锁，并且线程调度器允许本线程持有时，该线程将变成非阻塞状态；
    -  当线程等待另一个线程通知调度器一个条件时，该线程进入**等待状态**；（在调用 Object.wait()、Thread.join()、Lock、Condition时出现该情况）；
    -  方法中有一个超时参数，调用他们导致线程进入**计时等待状态**；该状态保持直至超时期满或接收到适合的通知；（带有超时参数方法：Thread.sleep、Object.wait、Thread.join、Lock.tryLock、Condition.await）;
        -  Thread.join(): 等待终止指定的线程；
        -  Thread.join(long millis): 等待指定的线程死亡或者经过指定的毫秒数；
        -  Thread.State getState(): 获取线程状态；
        
-  **被终止的线程**:
    - run 方法正常退出而自然死亡；
    - 一个没有被捕捉的异常终止了run方法二意外死亡；
   
![thread-status](./src/main/resources/pic/thread-status.png) 

### 线程属性
+  **线程优先级**：
    +  MIN_PRIORITY(1) - MAX_PRIORITY(10) 之间, NORM_PRIORITY(5) 默认优先级；
    +  setPriority方法可提高或降低线程的优先级；
    +  默认情况：一个线程继承父线程的优先级；
    +  线程调度器选择新线程，优先选择较高优先级的线程；
    +  static void yield(); 导致当前线程处于让步状态；
    
+  **守护线程**：
    +  t.setDaemon(true); 将线程转换为守护线程；（在线程启动之前调用）
    +  唯一用途： 为其他线程提供服务；

+  **处理未捕获异常的处理器**：
    +  线程run 方法中不能抛出任何受查异常，但非受查异常会导致线程终止；
    +  线程终止前，异常被传递到一个用于未捕获异常的处理器（必须实现 Thread.UncaughtExceptionHandler接口，该接口只有一个方法：void uncaughtException(Thread t, Throwable e）；
        +  setUncaughtExceptionHandler：为任何线程安装处理器；
        +  或 Thread.setDefaultUncaughtExceptionHandler: 为所有线程安装一个默认处理器；
        +  替换处理器可以使用日志API发送未捕获异常的报告到日志文件；
        +  若不安装处理器，则默认处理器为空；但不为独立的线程安装处理器，此时的处理器就是该线程的ThreadGroup对象（线程组）；
            +  线程组是一个可以统一管理的线程集合；
            +  默认情况，创建的所有线程属于相同线程组；
            +  ThreadGroup类实现 Thread.UncaughtExceptionHandler接口，uncaughtException方法如下操作：
                +  若线程组有父线程组，那么父线程的uncaughtException方法被调用；
                +  否则，若Thread.getDefaultExceptionHandler方法返回一个非空处理器，则调用该处理器；
                +  否则，如果Throwable是ThreadDeath的一个实例，什么不做；
                +  否则，线程名字以及Throwable的栈轨迹被输出到System.err上；
    
### [Fork-Join框架](./src/main/java/forkJoin/ForkJoinDemo.java)

