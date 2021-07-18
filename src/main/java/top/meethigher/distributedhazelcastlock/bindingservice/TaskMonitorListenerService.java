package top.meethigher.distributedhazelcastlock.bindingservice;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import top.meethigher.distributedhazelcastlock.dto.TaskContent;
import top.meethigher.distributedhazelcastlock.respositories.TaskContentRepository;

import javax.annotation.Resource;
import java.util.Optional;

/**
 * TaskMonitorListenerService
 * 监听任务
 *
 * @author kit chen
 * @github https://github.com/meethigher
 * @blog https://meethigher.top
 * @time 2021/7/18
 */
@Component
public class TaskMonitorListenerService {
    private final static String MESSAGE = "没有查询到数据";

    @Resource
    TaskContentRepository taskContentRepository;

    @KafkaListener(topics = {"${kafka-topic.topic.task-monitor}"})
    public void taskMonitorListenerAction(ConsumerRecord<?, ?> record) {
        Optional<?> optional = Optional.ofNullable(record.value());
        if (optional.isPresent()) {
            String originTaskCode = ((String) optional.get()).replace("\"", "");
            int taskCode = Integer.parseInt(originTaskCode);
            TaskContent taskContent = findTaskContentById(taskCode);

            execTaskContent(taskContent);
        }
    }

    public TaskContent findTaskContentById(Integer taskCode) {
        Optional<TaskContent> optional = taskContentRepository.findById(taskCode);
        if (optional.isPresent()) {
            TaskContent task = optional.get();
            return task;
        } else {
            throw new RuntimeException(MESSAGE);
        }
    }


    public void execTaskContent(TaskContent taskContent) {
        String taskName = taskContent.getTask().getTaskName();
        System.out.println("执行【"+taskName+"】->【" + taskContent.getContentDesc() + "】");

    }
}
