package cn.intellif.intellifjob.core;

import cn.intellif.intellifjob.utils.BeanUtils;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * author 尹冲
 */
public abstract class IntellifEasyJob implements SimpleJob {
    private static Logger logger = LoggerFactory.getLogger(IntellifEasyJob.class);

    @Override
    public void execute(ShardingContext shardingContext) {
        logger.info("--------------------->"+ BeanUtils.getCurrentTime()+"...."+this.getClass().getName()+"...run");
        executeAndLog(shardingContext);
    }

    public void executeAndLog(ShardingContext shardingContext){

    }
}
