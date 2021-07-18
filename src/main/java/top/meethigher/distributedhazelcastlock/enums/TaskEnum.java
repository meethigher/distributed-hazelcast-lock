package top.meethigher.distributedhazelcastlock.enums;

/**
 * @author chenchuancheng
 * @date 2021-07-16 17:55:57
 **/
public enum TaskEnum {
    FIRST(1, "一级任务"),
    SECOND(2, "二级任务"),
    THIRD(3, "三级任务"),
    FORTH(4, "四级任务");
    public final int code;
    public final String desc;

    TaskEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
