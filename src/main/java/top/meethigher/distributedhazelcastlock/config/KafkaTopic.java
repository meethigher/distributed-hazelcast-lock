package top.meethigher.distributedhazelcastlock.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * KafkaTopic
 *
 * @author kit chen
 * @github https://github.com/meethigher
 * @blog https://meethigher.top
 * @time 2021/7/17
 */
@Configuration
@ConfigurationProperties(prefix = "kafka-topic")
public class KafkaTopic {
    private TopicName topic;


    public static class TopicName {
        private String taskMonitor;

        public String getTaskMonitor() {
            return taskMonitor;
        }

        public void setTaskMonitor(String taskMonitor) {
            this.taskMonitor = taskMonitor;
        }
    }

    public TopicName getTopic() {
        return topic;
    }

    public void setTopic(TopicName topic) {
        this.topic = topic;
    }
}
