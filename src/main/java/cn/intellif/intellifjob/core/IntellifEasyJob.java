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

    /**
     * 重写这个将无日志输出
     *
     * @param shardingContext
     */
    @Override
    public void execute(ShardingContext shardingContext) {
        logger.info("--------------------->" + BeanUtils.getCurrentTime() + "...." + this.getClass().getName() + "...start run");
        try {
            long startTime = System.currentTimeMillis();
            executeAndLog(shardingContext);
            long endTime = System.currentTimeMillis();
            logger.info("--------------------->" + BeanUtils.getCurrentTime() + "...." + this.getClass().getName() + "...execute finish use " + (endTime - startTime) + " ms ");
        } catch (Exception e) {
            logger.info("--------------------->" + BeanUtils.getCurrentTime() + "...." + this.getClass().getName() + "...error cause:" + e);
            logger.error("--------------------->" + BeanUtils.getCurrentTime() + "...." + this.getClass().getName() + "...error cause:" + e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 重新这个方法可以打印执行中日志
     *
     * @param shardingContext
     */
    public void executeAndLog(ShardingContext shardingContext) {
    }
}
