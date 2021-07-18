package top.meethigher.distributedhazelcastlock.dto;

import javax.persistence.*;

/**
 * TaskContent
 *
 * @author kit chen
 * @github https://github.com/meethigher
 * @blog https://meethigher.top
 * @time 2021/7/17
 */
@Entity
@Table(name = "taskcontent")
public class TaskContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer contentId;

    private String contentDesc;

    @ManyToOne(targetEntity = Task.class)
    @JoinColumn(name = "taskId",referencedColumnName = "taskId")
    private Task task;


    public Integer getContentId() {
        return contentId;
    }

    public void setContentId(Integer contentId) {
        this.contentId = contentId;
    }

    public String getContentDesc() {
        return contentDesc;
    }

    public void setContentDesc(String contentDesc) {
        this.contentDesc = contentDesc;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }
}
