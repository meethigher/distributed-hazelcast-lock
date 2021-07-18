package top.meethigher.distributedhazelcastlock.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.RoundRobinPartitioner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import javax.annotation.Resource;
import java.util.HashMap;

/**
 * KafkaConfig
 * 初始化一些Kafka的配置
 * @author kit chen
 * @github https://github.com/meethigher
 * @blog https://meethigher.top
 * @time 2021/7/17
 */
@Configuration
public class KafkaConfig {
    /**
     * 分区数>=节点数，目前有三个节点
     */
    private final Integer PARTITION_NUM=3;
    /**
     * 副本数<=节点数
     */
    private final short REPLICATION_NUM=2;

    @Resource
    private KafkaTopic topic;


    /**
     * 初始化Task的Topic
     * 作用：将定时任务动态分配的topic配置分区，从而能够在多个节点上均衡消费！
     * @return
     */
    @Bean
    public NewTopic initialTaskTopic() {
        String topicName=topic.getTopic().getTaskMonitor();
        return new NewTopic(topicName,PARTITION_NUM,REPLICATION_NUM);
    }

    public Integer getPARTITION_NUM() {
        return PARTITION_NUM;
    }

    public short getREPLICATION_NUM() {
        return REPLICATION_NUM;
    }
}
