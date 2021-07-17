package top.meethigher.distributedhazelcastlock.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;
/**
 * @description 任务执行器的配置
 * @author chenchuancheng
 * @since 2021/7/16 23:01
 */
@Configuration
@EnableAsync
public class TaskExecutorConfig {
    @Bean("taskExecutor")
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //核心线程数
        executor.setCorePoolSize(10);
        //最大线程数
        executor.setMaxPoolSize(20);
        //队列的长度
        executor.setQueueCapacity(8);
        //线程池维护线程所允许的空闲时间
        executor.setKeepAliveSeconds(60);
        //线程是对拒绝任务的处理策略，也就是没有线程可用的时候
        //CallerRunsPolicy在任务被拒绝添加后，会在调用execute方法的的线程来执行被拒绝的任务。除非executor被关闭，否则任务不会被丢弃。
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        //任务执行器的前缀，打印日志时输出
        executor.setThreadNamePrefix("task-thread-");
        return executor;
    }
}
