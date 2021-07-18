package top.meethigher.distributedhazelcastlock.respositories;

import org.springframework.data.jpa.repository.JpaRepository;
import top.meethigher.distributedhazelcastlock.dto.TaskContent;

/**
 * TaskContentRespository
 *
 * @author kit chen
 * @github https://github.com/meethigher
 * @blog https://meethigher.top
 * @time 2021/7/18
 */
public interface TaskContentRepository extends JpaRepository<TaskContent, Integer> {
}
