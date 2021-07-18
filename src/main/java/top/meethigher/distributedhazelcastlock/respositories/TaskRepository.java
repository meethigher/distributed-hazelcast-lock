package top.meethigher.distributedhazelcastlock.respositories;

import org.springframework.data.jpa.repository.JpaRepository;
import top.meethigher.distributedhazelcastlock.dto.Task;

/**
 * TaskRespository
 *
 * @author kit chen
 * @github https://github.com/meethigher
 * @blog https://meethigher.top
 * @time 2021/7/18
 */
public interface TaskRepository extends JpaRepository<Task, Integer> {

}
