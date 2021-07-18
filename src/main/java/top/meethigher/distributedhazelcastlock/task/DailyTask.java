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

    @Async("taskExecutor")
    @Scheduled(cron = "0 24 23 * * ? ")
    public void firstTask() {
        doTask(TaskEnum.FIRST);
    }


    @Async("taskExecutor")
    @Scheduled(cron = "0 24 23 * * ?")
    public void secondTask() {
        doTask(TaskEnum.SECOND);
    }

    @Async("taskExecutor")
    @Scheduled(cron = "0 24 23 * * ?")
    public void thirdTask() {
        doTask(TaskEnum.THIRD);
    }

    @Async("taskExecutor")
    @Scheduled(cron = "0 24 23 * * ?")
    public void forthTask() {
        doTask(TaskEnum.FORTH);
    }


    public void doTask(TaskEnum task) {
        if (!ObjectUtils.isEmpty(task)) {
            IMap<Integer, String> map = hazelcastInstance.getMap(API_MONITOR_TASK_MAP_NAME);
            map.put(task.code, task.desc);
            //判断任务是否能锁
            boolean canLocked = map.tryLock(task.code);
            if (canLocked) {
                System.out.println("抢到任务分发权限！");
                doMonitorTask(task);
                map.unlock(task.code);
            }else{
                System.out.println("没有抢到权限");
            }
        }
    }

    public void doMonitorTask(TaskEnum anEnum) {
        System.out.println("向所有节点分发【" + anEnum.desc + "】..");
        Optional<Task> optional = taskRepository.findById(anEnum.code);
        if (optional.isPresent()) {
            Task task = optional.get();
            Set<TaskContent> content = task.getContent();
            pushToKafka(content);

        }
    }

    /**
     * 实现分区间均衡分配
     * 亲测Apache提供的轮询策略，并没有在分区间均衡分配，并且有很大的几率，所有任务都发送同一分区，这就导致有种极端情况，所有任务都是同一台机器执行，集群无意义
     *
     * @param contents
     */
    public void pushToKafka(Set<TaskContent> contents) {
        int i = 0, partition;
        for (TaskContent item : contents) {
            partition = i % 3;
            producer.sendAsy(topic.getTopic().getTaskMonitor(), partition, item.getContentId());
            i++;
        }
    }
}
