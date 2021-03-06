package org.lucas.util;

import org.lucas.lang.Nullable;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 用于创建新线程实例的自定义类。
 * <p>
 * 提供线程自定义属性:
 * 线程名前缀、线程优先级，是否守护线程，所属线程组。
 */
public class CustomizableThreadCreator implements Serializable {

    /***
     * 线程相对优先级(默认级别:5)
     */
    private int threadPriority = Thread.NORM_PRIORITY;

    /**
     * 线程名前缀
     */
    private String threadNamePrefix;

    /**
     * 是否守护线程
     * <p>
     * 守护线程会随主线程消亡而消亡
     */
    private boolean daemon = false;

    /**
     * 所属线程组
     */
    @Nullable
    private ThreadGroup threadGroup;

    /**
     * 线程个数
     * <p>
     * 通过AtomicInteger类的原子特性,保证并发
     */
    private final AtomicInteger threadCount = new AtomicInteger(0);

    /**
     * 使用默认线程名创建
     */
    public CustomizableThreadCreator() {
        this.threadNamePrefix = getDefaultThreadNamePrefix();
    }

    /**
     * 使用自定义线程前缀名
     */
    public CustomizableThreadCreator(@Nullable String threadNamePrefix) {
        this.threadNamePrefix = (threadNamePrefix != null ? threadNamePrefix : getDefaultThreadNamePrefix());
    }

    /**
     * 自定义创建线程
     *
     * @param runnable Runnable对象
     * @return Thread
     */
    public Thread createThread(Runnable runnable) {
        // 分配线程组,线程名,创建线程
        Thread thread = new Thread(getThreadGroup(), runnable, nextThreadName());
        // 设置线程优先级
        thread.setPriority(getThreadPriority());
        // 是否后台线程
        thread.setDaemon(isDaemon());
        return thread;
    }

    /**
     * 获取下一个新线程名称{@link Thread#name}。
     *
     * @return String
     */
    protected String nextThreadName() {
        return getThreadNamePrefix() + this.threadCount.incrementAndGet();
    }

    /**
     * 设置线程名前缀(默认:类名-)
     *
     * @param threadNamePrefix
     */
    public void setThreadNamePrefix(@Nullable String threadNamePrefix) {
        this.threadNamePrefix = (threadNamePrefix != null ? threadNamePrefix : getDefaultThreadNamePrefix());
    }

    /**
     * 返回默认线程前缀名
     *
     * @return String 前缀名
     */
    protected String getDefaultThreadNamePrefix() {
        /** 获取调用者的类名(不含包名) */
        return ClassUtils.getShortName(getClass()) + "-";
    }

    public String getThreadNamePrefix() {
        return this.threadNamePrefix;
    }

    /**
     * 设置线程工厂的优先级.
     * 默认5.
     *
     * @see java.lang.Thread#NORM_PRIORITY
     */
    public void setThreadPriority(int threadPriority) {
        this.threadPriority = threadPriority;
    }

    public int getThreadPriority() {
        return this.threadPriority;
    }

    public void setDaemon(boolean daemon) {
        this.daemon = daemon;
    }

    public boolean isDaemon() {
        return this.daemon;
    }

    /**
     * 指定线程组的名称
     */
    public void setThreadGroupName(String name) {
        this.threadGroup = new ThreadGroup(name);
    }

    @Nullable
    public ThreadGroup getThreadGroup() {
        return this.threadGroup;
    }
}
