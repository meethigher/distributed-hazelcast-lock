package top.meethigher.distributedhazelcastlock.task;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import top.meethigher.distributedhazelcastlock.config.KafkaConfig;
import top.meethigher.distributedhazelcastlock.config.KafkaProducer;
import top.meethigher.distributedhazelcastlock.config.KafkaTopic;
import top.meethigher.distributedhazelcastlock.dto.Task;
import top.meethigher.distributedhazelcastlock.dto.TaskContent;
import top.meethigher.distributedhazelcastlock.enums.TaskEnum;
import top.meethigher.distributedhazelcastlock.respositories.TaskRepository;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

/**
 * @author chenchuancheng
 * @date 2021-07-16 17:45:31
 **/
@Component
public class DailyTask {
    @Resource
    TaskRepository taskRepository;
    Logger log = LoggerFactory.getLogger(DailyTask.class);
    @Resource
    HazelcastInstance hazelcastInstance;
    @Resource
    KafkaProducer producer;
    @Resource
    KafkaTopic topic;


    private final static String API_MONITOR_TASK_MAP_NAME = "api_monitor_task_map";
    //单位毫秒
    private final static Long DISTRIBUTE_TASK_TIME_INTERVAL = 60000L;

    @Async("taskExecutor")
    @Scheduled(cron = "0 20 23 * * ? ")
    public void firstTask() {
        doTask(TaskEnum.FIRST);
    }


    @Async("taskExecutor")
    @Scheduled(cron = "0 20 23 * * ?")
    public void secondTask() {
        doTask(TaskEnum.SECOND);
    }

    @Async("taskExecutor")
    @Scheduled(cron = "0 20 23 * * ?")
    public void thirdTask() {
        doTask(TaskEnum.THIRD);
    }

    @Async("taskExecutor")
    @Scheduled(cron = "0 20 23 * * ?")
    public void forthTask() {
        doTask(TaskEnum.FORTH);
    }


    public void doTask(TaskEnum task) {
        if (!ObjectUtils.isEmpty(task)) {
            IMap<Integer, Long> map = hazelcastInstance.getMap(API_MONITOR_TASK_MAP_NAME);
            Long temp = map.get(task.code);
            Long lastExecTime = ObjectUtils.isEmpty(temp) ? 0L : temp;
            Long currentTime = System.currentTimeMillis();
            Long intervalTime = currentTime - lastExecTime;
            log.info("【" + task.desc + "】,lastExecTime=" + lastExecTime + ",currentTime=" + currentTime + ",intervalTime=" + intervalTime);
            //判断任务是否能锁
            boolean canLocked = map.tryLock(task.code);
            if (intervalTime > DISTRIBUTE_TASK_TIME_INTERVAL && canLocked) {
                log.info("抢到【" + task.desc + "】分发权限！");
                try {
                    map.put(task.code, currentTime);
                    doMonitorTask(task);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    map.unlock(task.code);
                    log.info("释放【" + task.desc + "】");
                }
            } else {
                log.info("没有抢到【" + task.desc + "】权限");
            }
        }
    }

    public void doMonitorTask(TaskEnum anEnum) {
        log.info(LocalDateTime.now() + "向所有节点分发【" + anEnum.desc + "】..");
        Optional<Task> optional = taskRepository.findById(anEnum.code);
        if (optional.isPresent()) {
            Task task = optional.get();
            Set<TaskContent> content = task.getContent();
            pushToKafka(content);
        }
        log.info(LocalDateTime.now() + "分发【" + anEnum.desc + "】完毕");
    }

    /**
     * 实现分区间均衡分配
     * 亲测Apache提供的RoundRobinPartitioner轮询策略，并没有在分区间均衡分配，并且有很大的几率，所有任务都发送同一分区，这就导致有种极端情况，所有任务都是同一台机器执行，集群无意义
     * spring.kafka.producer.properties.partitioner.class=org.apache.kafka.clients.producer.RoundRobinPartitioner，可以自己多次尝试
     *
     * @param contents
     */
    public void pushToKafka(Set<TaskContent> contents) {
        int i = 0, partition;
        for (TaskContent item : contents) {
            partition = i++ % 3;
            producer.sendAsy(topic.getTopic().getTaskMonitor(), partition, item.getContentId());
        }
    }
}
