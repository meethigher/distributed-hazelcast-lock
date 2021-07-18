# [基于Hazelcast及Kafka实现的分布式锁与集群负载均衡](http://localhost:4000/blog/2021/distributed-hazelcast-lock/)

之所以产出这一博客，是因为公司的项目上线了集群之后出现了问题。

大佬排查之后，发现我写的代码存在一点问题，所以就趁周末时间，进行了修改与测试，产出了这一Demo。

## 一、分布式锁

参考文章

* [IMap (Hazelcast Root 3.6 API)](https://docs.hazelcast.org/docs/3.6/javadoc/com/hazelcast/core/IMap.html)

* [HazelcastInstance (Hazelcast Root 4.2 API)](https://docs.hazelcast.org/docs/4.2/javadoc/com/hazelcast/core/HazelcastInstance.html)

* [Hazelcast - 配置 - Gingerdoc 姜知笔记](https://www.gingerdoc.com/hazelcast/hazelcast_configuration)

* [ThreadPoolTaskExecutor和ThreadPoolExecutor区别](https://blog.csdn.net/weixin_43168010/article/details/97613895)

* [Java ThreadPoolExecutor的拒绝策略CallerRunsPolicy的一个潜在的大坑](https://blog.csdn.net/w605283073/article/details/89930497)

* [Java ThreadPoolExecutor的拒绝策略](https://blog.csdn.net/w605283073/article/details/89930154)
* [meethigher/distributed-hazelcast-lock: 基于Hazelcast实现的集群分布式锁Demo](https://github.com/meethigher/distributed-hazelcast-lock)

先来看下流程图

![](https://meethigher.top/blog/2021/distributed-hazelcast-lock/1.png)

准备三台节点，每台节点上面都有相同的定时任务，将三台节点部署成一个集群，定时任务同时启动，经过分布式锁的过滤，每个任务只有拿到锁的那台机器进行执行。

HazelcastConfig.java

```java
@Configuration
public class HazelcastConfig {
    @Bean
    public HazelcastInstance hazelcastInstance() {
        return Hazelcast.newHazelcastInstance();
    }
}
```

TaskExecutorConfig.java

```java
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
```

TaskEnum.java

```java
public enum TaskEnum {
    FIRST(0, "一级任务"),
    SECOND(1, "二级任务"),
    THIRD(2, "三级任务"),
    FORTH(3, "四级任务");
    public final int code;
    public final String desc;

    TaskEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
```

DailyTask.java

```java
@Component
public class DailyTask {
    Logger log = LoggerFactory.getLogger(DailyTask.class);
    @Autowired
    HazelcastInstance hazelcastInstance;

    private final static String API_MONITOR_TASK_MAP_NAME = "api_monitor_task_map";

    @Async("taskExecutor")
    @Scheduled(cron = "0 10 14 * * ? ")
    public void apiQhScheduledTask() {
        doTask(TaskEnum.FIRST);
    }


    @Async("taskExecutor")
    @Scheduled(cron = "0 10 14 * * ?")
    public void apiSHScheduledTask() {
        doTask(TaskEnum.SECOND);
    }

    @Async("taskExecutor")
    @Scheduled(cron = "0 10 14 * * ?")
    public void apiTHScheduledTask() {
        doTask(TaskEnum.THIRD);
    }

    @Async("taskExecutor")
    @Scheduled(cron = "0 10 14 * * ?")
    public void apiODScheduledTask() {
        doTask(TaskEnum.FORTH);
    }


    public void doTask(TaskEnum task) {
        if (!ObjectUtils.isEmpty(task)) {
            IMap<Integer, String> map = hazelcastInstance.getMap(API_MONITOR_TASK_MAP_NAME);
            map.put(task.code, task.desc);
            //判断任务是否能锁
            boolean canLocked = map.tryLock(task.code);
            if(canLocked){
                System.out.println("本次抢到锁，执行任务...");
                log.info(task.desc);
                map.unlock(task.code);
            }else{
                System.out.println("本次不抢锁！");
            }
        }
    }
}
```

最终执行结果

![](https://meethigher.top/blog/2021/distributed-hazelcast-lock/2.png)

缺点：同一时刻的任务，有可能全部被同一台机器抢到，其他两台机器会空闲，这种极限情况下的分配存在问题。不过我目前的项目同时只有一条任务，够用。

## 二、负载均衡

参考文章

* [ @ConfigurationProperties 注解使用姿势](https://blog.csdn.net/yusimiao/article/details/97622666)

* [spring-kafka生产者消费者配置详解](https://blog.csdn.net/u014774648/article/details/90110508)

* [spring boot 中Spring data jpa数据库表字段命名策略](https://blog.csdn.net/alalala2015/article/details/102408360)

* [java -jar 和 -cp详解 - 知乎](https://zhuanlan.zhihu.com/p/214093661)

* [Data source rejected establishment of connection, message from server: "Too many connections"](https://www.cnblogs.com/Komorebi-john/p/11470468.html)

* [Mysql查看连接数（连接总数、活跃数、最大并发数）](https://www.cnblogs.com/caoshousong/p/10845396.html)

* [Kafka轮询生产_](https://blog.csdn.net/qq_31473465/article/details/108080116)

* [Kafka轮询消费](https://blog.csdn.net/u010634066/article/details/109778491)

* [springboot kafka 发送消息 分区不均 RoundRobinPartitioner](https://blog.csdn.net/weixin_35928208/article/details/109489895)

* [springboot集成整合kafka-自定义分区策略、将消息发送到指定的分区partition](https://blog.csdn.net/H900302/article/details/109818137)

先看下流程图

![](https://meethigher.top/blog/2021/distributed-hazelcast-lock/3.png)

我是准备了两台机器提供Kafka集群。具体配置过程

1. 两台机器均启动zookeeper，保持zookeeper默认配置即可
2. 配置Kafka的配置文件。
   * 每台kafka的brokerId保持唯一。
   * 每台kafka的zookeeper.connect配置为zookeeper集群。
   * 每台kafka的advertised.listeners配置PLAINTEXT://当前节点ip:9092，好像不用配也可以。

通过Kafka实现的负载均衡可以解决上面的那个问题，哪怕所有锁都被他自己抢到了，也无所谓，也就是抢到锁的节点只需要将工作内容抛给Kafka，经过Kafka，然后均衡地分配给下面的各个节点进行消费，从而达到负载均衡。

运行结果

![](https://meethigher.top/blog/2021/distributed-hazelcast-lock/4.png)