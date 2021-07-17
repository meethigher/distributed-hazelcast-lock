package top.meethigher.distributedhazelcastlock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DistributedHazelcastLockApplication {

    public static void main(String[] args) {
        SpringApplication.run(DistributedHazelcastLockApplication.class, args);
    }

}
