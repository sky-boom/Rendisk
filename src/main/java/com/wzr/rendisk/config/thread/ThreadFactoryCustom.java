package com.wzr.rendisk.config.thread;

import com.sun.istack.internal.NotNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自定义线程工程，可以用于指定线程名称、以及设置守护线程等。
 * @author wzr
 * @date 2023-06-17 10:50
 */
public class ThreadFactoryCustom implements ThreadFactory {
    /** 自增整数 */
    private final AtomicInteger threadNum = new AtomicInteger();
    /** 指定原版线程工厂 */
    private final ThreadFactory delegate;
    /** 线程主要名字 */
    private final String name;

    public ThreadFactoryCustom(ThreadFactory delegate, String name) {
        this.delegate = delegate;
        this.name = name;
    }

    @Override
    public Thread newThread(@NotNull Runnable r) {
        Thread t = delegate.newThread(r);
        t.setName(name + " [线程号 #" + threadNum.incrementAndGet() + "]");
        return t;
    }
}
