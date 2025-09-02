package com.ruoyi.system.core.pool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * @author: xiaodemos
 * @date: 2025-04-13 13:59
 * @description: 异步任务、设置线程池配置类
 */

@Slf4j
@EnableAsync
@Configuration
public class AsyncConfig {



    private static final int CPU_CORES = Runtime.getRuntime().availableProcessors(); // 获取CPU核心数
    private static final int CORE_POOL_SIZE = CPU_CORES * 2; // 如16核→32
    private static final int MAX_POOL_SIZE = CORE_POOL_SIZE * 2; // 如64
    private static final int QUEUE_CAPACITY = 1000;
    private static final long KEEP_ALIVE_SECONDS = 60; // 空闲线程存活时间
    private static final String THREAD_NAME_PREFIX = "Async-";

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //设置线程池的核心线程数
        executor.setCorePoolSize(CORE_POOL_SIZE);
        //设置线程池的最大线程数
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        //线程池的工作队列容量
        executor.setQueueCapacity(QUEUE_CAPACITY);
        //线程池中线程的名称前缀
        executor.setThreadNamePrefix(THREAD_NAME_PREFIX);
        //空闲核心线程超时回收（仅当核心线程数 < 最大线程数时生效）
        executor.setKeepAliveSeconds((int) KEEP_ALIVE_SECONDS);
        //允许核心线程超时回收（默认false，核心线程不回收）
        executor.setAllowCoreThreadTimeOut(true);
        //设置自定义的拒绝策略
        executor.setRejectedExecutionHandler((r, e) -> {
            try {
                // 记录一个警告日志，说明当前保存评价的连接池已满，触发了拒绝策略。
                log.warn("保存评价连接池任务已满,触发拒绝策略");

                // 尝试将任务重新放入队列中，等待30秒。
                // 如果在这30秒内队列有空闲空间，任务将被成功放入队列；否则，offer方法将返回false。
                boolean offer = e.getQueue().offer(r, 30, TimeUnit.SECONDS);

                // 记录日志，显示等待30秒后尝试重新放入队列的结果。
                log.warn("保存评价连接池任务已满，拒绝接收任务,等待30s重新放入队列结果rs:{}", offer);
            } catch (InterruptedException ex) {
                // 如果在等待过程中线程被中断，捕获InterruptedException异常。
                // 记录一个错误日志，说明在尝试重新放入队列时发生了异常。
                log.error("【保存评价】连接池任务已满，拒绝接收任务了，再重新放入队列后出现异常", ex);

                // 构建一条警告消息，其中包含线程池的各种信息（如池大小、活动线程数、核心线程数等）。
                String msg = String.format("保存评价线程池拒绝接收任务! Pool Size: %d (active: %d, core: %d, max: %d, largest: %d), Task: %d (completed: %d)"
                        , e.getPoolSize(), e.getActiveCount(), e.getCorePoolSize(),
                        e.getMaximumPoolSize(), e.getLargestPoolSize(), e.getTaskCount(),
                        e.getCompletedTaskCount());
                // 记录包含线程池详细信息的警告日志。
                log.warn(msg);
            }
        });
        //初始化线程池，线程池就会处于可以接收任务的状态
        executor.initialize();

        return executor;
    }
}
