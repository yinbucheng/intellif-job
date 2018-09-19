package cn.intellif.intellifjob.core;

public class CoreDefination {
    private String core;
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

    public String getCore() {
        return core;
    }

    public void setCore(String core) {
        this.core = core;
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
