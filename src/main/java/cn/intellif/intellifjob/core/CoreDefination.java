package cn.intellif.intellifjob.core;

/**
 * author 尹冲
 */
public class CoreDefination {
    private String cron;
    private Class clazz;
    private String name;
    private Integer shardingTotalCount;
    private String itemParameters;

    public String getItemParameters() {
        return itemParameters;
    }

    public void setItemParameters(String itemParameters) {
        this.itemParameters = itemParameters;
    }

    public Integer getShardingTotalCount() {
        return shardingTotalCount;
    }

    public void setShardingTotalCount(Integer shardingTotalCount) {
        this.shardingTotalCount = shardingTotalCount;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
