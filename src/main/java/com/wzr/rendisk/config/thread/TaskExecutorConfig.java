package com.wzr.rendisk.config.thread;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池管理类
 * @author wzr
 * @date 2023-06-17 10:40
 */
@Configuration
@EnableAsync
public class TaskExecutorConfig {
    
    /** 核心线程数 */
    private static final int CORE_SIZE = 3;
    /** 最大线程数 */
    private static final int MAX_SIZE = 3;
    /** 线程存活时间 */
    private static final int ALIVE_SECONDS = 60;
    /** 等待队列容量大小 */
    private static final int QUEUE_CAPACITY = 10;
    
    
    /**
     * 创建一个常规线程池
     * @return ThreadPoolExecutor
     */
    @Bean("commonTaskExecutor")
    public ThreadPoolExecutor commonTaskExecutor() {
        // 任务等待队列
        LinkedBlockingQueue<Runnable> linkedBlockingQueue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
        // 线程工厂 
        ThreadFactoryCustom threadFactory =
                new ThreadFactoryCustom(Executors.defaultThreadFactory(), "自定线程工厂-");
        // 不指定拒绝策略，则使用默认
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                CORE_SIZE, MAX_SIZE, ALIVE_SECONDS, TimeUnit.SECONDS,
                linkedBlockingQueue, threadFactory);
        return executor;
    }

    /**
     * 创建一个Spring任务线程池，允许异步
     * @return ThreadPoolTaskExecutor
     */
    @Bean("taskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        // 任务等待队列
        LinkedBlockingQueue<Runnable> linkedBlockingQueue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_SIZE);
        executor.setMaxPoolSize(MAX_SIZE);
        executor.setKeepAliveSeconds(ALIVE_SECONDS);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setThreadNamePrefix("自定线程池-");
        // 拒绝策略：CallerRunsPolicy：由调用线程（提交任务的线程）处理该任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 初始化
        executor.initialize();
        return executor;
    }
}
