package cn.intellif.intellifjob.job;


import cn.intellif.intellifjob.annotation.IntellifSimpleJob;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;

@IntellifSimpleJob(id = "myIntellifJob",core = "${intellif.job.core}",shardingTotalCount = "1",itemParameters = "0=A,1=B")
public class MyIntellifJob implements SimpleJob {


    @Override
    public void execute(ShardingContext shardingContext) {
        System.out.println(">>>>>>>>>>>>>>>>>nice");
    }
}
