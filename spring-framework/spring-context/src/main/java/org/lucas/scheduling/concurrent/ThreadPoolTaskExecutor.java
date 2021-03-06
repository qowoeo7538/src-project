package org.lucas.scheduling.concurrent;

import org.lucas.core.task.TaskRejectedException;
import org.lucas.lang.Nullable;
import org.lucas.core.task.AsyncListenableTaskExecutor;
import org.lucas.core.task.TaskDecorator;
import org.lucas.scheduling.SchedulingTaskExecutor;
import org.lucas.util.Assert;
import org.lucas.util.ConcurrentReferenceHashMap;
import org.lucas.util.concurrent.ListenableFuture;
import org.lucas.util.concurrent.ListenableFutureTask;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池
 */
public class ThreadPoolTaskExecutor extends ExecutorConfigurationSupport
        implements AsyncListenableTaskExecutor, SchedulingTaskExecutor {

    /**
     * 核心线程池大小
     * <p>
     * 当线程池小于corePoolSize时，新提交任务将创建一个新线程执行任务，
     * 即使此时线程池中存在空闲线程。
     * <p>
     * 当线程池达到corePoolSize时，新提交任务将被放入任务队列中.
     */
    private int corePoolSize = 1;

    /**
     * 最大线程池大小
     * <p>
     * 如果任务队列已满,将创建最大线程池的数量执行任务,如果超出最大线程池的大小,
     * 将提交给RejectedExecutionHandler处理
     */
    private int maxPoolSize = Integer.MAX_VALUE;

    /**
     * 线程池中超过核心线程数目的空闲线程最大存活时间；
     * 可以allowCoreThreadTimeOut(true)使得核心线程有效时间
     */
    private int keepAliveSeconds = 60;

    /**
     * 阻塞任务队列容量(默认为int的最大值)
     */
    private int queueCapacity = Integer.MAX_VALUE;

    /**
     * 任务装饰对象
     */
    @Nullable
    private TaskDecorator taskDecorator;

    /**
     * 线程池对象
     */
    @Nullable
    private ThreadPoolExecutor threadPoolExecutor;

    /**
     * 对空闲核心线程是否进行关闭
     */
    private boolean allowCoreThreadTimeOut = false;

    private final Map<Runnable, Object> decoratedTaskMap =
            new ConcurrentReferenceHashMap<>(16, ConcurrentReferenceHashMap.ReferenceType.WEAK);

    /**
     * 初始化线程池
     *
     * @param threadFactory            线程工厂
     * @param rejectedExecutionHandler 被拒绝的处理策略对象
     * @return ExecutorService
     */
    @Override
    protected ExecutorService initializeExecutor(ThreadFactory threadFactory, RejectedExecutionHandler rejectedExecutionHandler) {
        // 创建任务队列
        BlockingQueue<Runnable> queue = createQueue(this.queueCapacity);
        ThreadPoolExecutor executor;
        if (this.taskDecorator != null) {
            /**
             * 任务装饰对象如果不为空,则重写 execute 方法创建线程池,调用装饰对象任务.
             */
            executor = new ThreadPoolExecutor(
                    this.corePoolSize, this.maxPoolSize, this.keepAliveSeconds, TimeUnit.SECONDS,
                    queue, threadFactory, rejectedExecutionHandler) {
                @Override
                public void execute(Runnable command) {
                    Runnable decorated = taskDecorator.decorate(command);
                    if (decorated != command) {
                        decoratedTaskMap.put(decorated, command);
                    }
                    // 实际执行任务为装饰任务
                    super.execute(decorated);
                }
            };
        } else {
            executor = new ThreadPoolExecutor(
                    this.corePoolSize, this.maxPoolSize, this.keepAliveSeconds, TimeUnit.SECONDS,
                    queue, threadFactory, rejectedExecutionHandler);
        }

        if (this.allowCoreThreadTimeOut) {
            /**
             * 核心线程超时是否销毁(keepAliveSeconds设置)
             */
            executor.allowCoreThreadTimeOut(true);
        }
        this.threadPoolExecutor = executor;
        return executor;
    }

    /**
     * 获取线程池活动线程数量
     *
     * @return int
     */
    public int getActiveCount() {
        if (this.threadPoolExecutor == null) {
            // Not initialized yet: assume no active threads.
            return 0;
        }
        return this.threadPoolExecutor.getActiveCount();
    }

    /**
     * 执行线程任务
     *
     * @param task 任务对象
     */
    @Override
    public void execute(Runnable task) {
        Executor executor = getThreadPoolExecutor();
        try {
            executor.execute(task);
        } catch (RejectedExecutionException ex) {
            // 提交任务被拒绝抛出异常
            throw new TaskRejectedException("Executor [" + executor + "] did not accept task: " + task, ex);
        }

    }

    /**
     * 执行线程任务
     *
     * @param task         任务对象
     * @param startTimeout
     */
    @Override
    public void execute(Runnable task, long startTimeout) {
        execute(task);
    }

    /**
     * Runnable异步任务
     *
     * @param task 任务对象
     * @return Future 回调结果(Future的get方法为null)
     */
    @Override
    public Future<?> submit(Runnable task) {
        ExecutorService executor = getThreadPoolExecutor();
        try {
            return executor.submit(task);
        } catch (RejectedExecutionException ex) {
            throw new TaskRejectedException("Executor [" + executor + "] did not accept task: " + task, ex);
        }
    }

    /**
     * Callable异步任务
     *
     * @param task 任务对象
     * @return Future 回调结果(Future的get方法为Callable的执行结果)
     */
    @Override
    public <T> Future<T> submit(Callable<T> task) {
        ExecutorService executor = getThreadPoolExecutor();
        try {
            return executor.submit(task);
        } catch (RejectedExecutionException ex) {
            throw new TaskRejectedException("Executor [" + executor + "] did not accept task: " + task, ex);
        }
    }

    /**
     * Runnable异步任务
     *
     * @param task 任务对象
     * @return ListenableFuture 回调结果(扩展Future接口)
     */
    @Override
    public ListenableFuture<?> submitListenable(Runnable task) {
        ExecutorService executor = getThreadPoolExecutor();
        try {
            /**
             * @param task 异步任务
             * @param result 是否返回特定的结果
             */
            ListenableFutureTask<Object> future = new ListenableFutureTask<>(task, null);
            executor.execute(future);
            return future;
        } catch (RejectedExecutionException ex) {
            throw new TaskRejectedException("Executor [" + executor + "] did not accept task: " + task, ex);
        }
    }

    /**
     * Callable异步任务
     *
     * @param task 任务对象
     * @return ListenableFuture 回调结果(扩展Future接口)
     */
    @Override
    public <T> ListenableFuture<T> submitListenable(Callable<T> task) {
        ExecutorService executor = getThreadPoolExecutor();
        try {
            ListenableFutureTask<T> future = new ListenableFutureTask<>(task);
            executor.execute(future);
            return future;
        } catch (RejectedExecutionException ex) {
            throw new TaskRejectedException("Executor [" + executor + "] did not accept task: " + task, ex);
        }
    }

    /**
     * 如果指定队列容量,将返回指定容量的LinkedBlockingQueue对象,
     * 否则返回无缓冲的队列SynchronousQueue对象
     *
     * @param queueCapacity 任务队列容量
     * @return BlockingQueue 队列
     */
    protected BlockingQueue<Runnable> createQueue(int queueCapacity) {
        if (queueCapacity > 0) {
            return new LinkedBlockingQueue<>(queueCapacity);
        } else {
            return new SynchronousQueue<>();
        }
    }

    /**
     * 尝试取消 Future 异步任务
     *
     * @param task Future 任务
     */
    @Override
    protected void cancelRemainingTask(Runnable task) {
        super.cancelRemainingTask(task);
        // Cancel associated user-level Future handle as well
        Object original = this.decoratedTaskMap.get(task);
        if (original instanceof Future) {
            ((Future<?>) original).cancel(true);
        }
    }

    /**
     * 返回当前对象线程池
     *
     * @return ThreadPoolExecutor 线程池
     * @throws IllegalStateException
     */
    public ThreadPoolExecutor getThreadPoolExecutor() throws IllegalStateException {
        Assert.state(this.threadPoolExecutor != null, "ThreadPoolTaskExecutor not initialized");
        return this.threadPoolExecutor;
    }

}
