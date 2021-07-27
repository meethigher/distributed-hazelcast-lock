package top.meethigher.distributedhazelcastlock.config;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.Cluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import javax.annotation.Resource;
import java.time.LocalTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * KafkaProducer
 *
 * @author kit chen
 * @github https://github.com/meethigher
 * @blog https://meethigher.top
 * @time 2021/7/17
 */
@Component
public class KafkaProducer {

    private final static Logger log = LoggerFactory.getLogger(KafkaProducer.class);

    @Resource
    private KafkaTemplate<String, Object> kafkaTemplate;


    public <T> void send(String topic, T message) {
        //发送消息
        try {
            SendResult<String, Object> stringStringSendResult = kafkaTemplate.send(topic, JacksonUtil.toJSon(message)).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("kafka出错:" + e.getMessage());
        }
    }

    /**
     * 异步发送信息
     *
     * @param topic
     * @param message
     * @param <T>
     */
    public <T> void sendAsy(String topic, T message) {

        ListenableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, JacksonUtil.toJSon(message));
        future.addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {
            @Override
            public void onFailure(Throwable throwable) {
                //发送失败的处理
                log.error(topic + " - 生产者 发送消息失败：" + throwable.getMessage());
            }

            @Override
            public void onSuccess(SendResult<String, Object> stringObjectSendResult) {
                RecordMetadata recordMetadata = stringObjectSendResult.getRecordMetadata();
                int partition = recordMetadata.partition();
                log.info("->发送到分区" + partition);
                //成功的处理
                log.info(topic + " - 生产者 发送消息成功：" + stringObjectSendResult.toString());
            }
        });
    }

    /**
     * 指定分区异步发送消息
     * @param topic
     * @param partition
     * @param message
     * @param <T>
     */
    public <T> void sendAsy(String topic,int partition, T message) {

        ListenableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic,partition,null, JacksonUtil.toJSon(message));
        future.addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {
            @Override
            public void onFailure(Throwable throwable) {
                //发送失败的处理
                log.error(topic + " - 生产者 发送消息失败：" + throwable.getMessage());
            }

            @Override
            public void onSuccess(SendResult<String, Object> stringObjectSendResult) {
                RecordMetadata recordMetadata = stringObjectSendResult.getRecordMetadata();
                int partition = recordMetadata.partition();
                log.info("->发送到分区" + partition);
                //成功的处理
                log.info(topic + " - 生产者 发送消息成功：" + stringObjectSendResult.toString());
            }
        });
    }
}