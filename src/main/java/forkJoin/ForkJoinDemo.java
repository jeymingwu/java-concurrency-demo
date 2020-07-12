package forkJoin;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.function.DoublePredicate;

/**
 * Fork-Join框架
 * 工作密取：使用一种有效的智能方法来平衡可用线程的工作负载；
 *
 * Demo内容：统计数组中有多少个元素满足某个特定的属性
 *
 * RecursiveTask<T>: 计算返回一个类型为T的结果
 * RecursiveAction: 不生成任何结果
 *
 * @author jeymingwu
 * @date 2020/7/12 19:31
 */
class Counter extends RecursiveTask<Integer> {

    public static final int THRESHOLD = 1000;
    private double[] values;
    private int from;
    private int to;
    private DoublePredicate filter;

    public Counter(double[] values, int from, int to, DoublePredicate filter) {
        this.values = values;
        this.from = from;
        this.to = to;
        this.filter = filter;
    }

    // 生成并调用子方法
    protected Integer compute() {
        if (to - from < THRESHOLD) {
            int count = 0;
            for (int i = from; i < to; ++i) {
                if (filter.test(values[i]))
                    ++count;
            }
            return count;
        } else {
            int mid = (from + to) / 2;
            Counter first = new Counter(values, from, mid, filter);
            Counter second = new Counter(values, mid, to, filter);
            invokeAll(first, second); // 接收多任务直至所有任务都已完成
            return first.join() + second.join(); // join() 生成结果
        }
    }
}

public class ForkJoinDemo {

    public static void main(String[] args) {
        final int SIZE = 10000000;
        double[] numbers = new double[SIZE];
        for (int i = 0; i < SIZE; ++i) {
            numbers[i] = Math.random();
        }
        Counter counter = new Counter(numbers, 0, numbers.length, x -> x > 0.5);
        ForkJoinPool pool = new ForkJoinPool();
        pool.invoke(counter);
        System.out.println(counter.join());

    }
}
