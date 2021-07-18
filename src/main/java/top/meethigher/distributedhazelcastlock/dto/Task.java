package top.meethigher.distributedhazelcastlock.dto;


import javax.persistence.*;
import java.util.Set;

/**
 * Task
 *
 * @author kit chen
 * @github https://github.com/meethigher
 * @blog https://meethigher.top
 * @time 2021/7/17
 */
@Entity
@Table(name = "task")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer taskId;

    private String taskName;

    //CascadeType.ALL 允许级联操作，比如删除当前task，会将关联的taskContent一并删掉
    @OneToMany(mappedBy = "task",fetch = FetchType.EAGER,cascade = CascadeType.ALL)
    private Set<TaskContent> content;


    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Set<TaskContent> getContent() {
        return content;
    }

    public void setContent(Set<TaskContent> content) {
        this.content = content;
    }
}
