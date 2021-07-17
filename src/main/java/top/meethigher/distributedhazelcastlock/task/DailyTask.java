package top.meethigher.distributedhazelcastlock.task;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import top.meethigher.distributedhazelcastlock.enums.TaskEnum;

/**
 * @author chenchuancheng
 * @date 2021-07-16 17:45:31
 **/
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
