package cn.intellif.intellifjob.excute;


import cn.intellif.intellifjob.annotation.IntellifSimpleJob;
import cn.intellif.intellifjob.config.ApplicationUtils;
import cn.intellif.intellifjob.config.EnviromentUtils;
import cn.intellif.intellifjob.core.CoreDefination;
import cn.intellif.intellifjob.event.ApplicationFinishEvent;
import cn.intellif.intellifjob.utils.ScannerClassUtils;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.lite.api.JobScheduler;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * author 尹冲
 */
public class JobExcute implements ApplicationListener {

    private static Logger logger = LoggerFactory.getLogger(JobExcute.class);
    @Autowired
    private ZookeeperRegistryCenter zookeeperRegistryCenter;

    private static BlockingQueue<CoreDefination> simpleCores = new LinkedBlockingQueue<>();

    public static void addSimpleJob(Class clazz) {
        IntellifSimpleJob intellifSimpleJob = (IntellifSimpleJob) clazz.getAnnotation(IntellifSimpleJob.class);
        String id = intellifSimpleJob.id();
        if (id.equals("")) {
            id = clazz.getName();
        }
        id = resovleName(id);
        String cron = intellifSimpleJob.cron();
        cron = resovleName(cron);
        String data = intellifSimpleJob.shardingTotalCount();
        data = resovleName(data);
        int count = Integer.parseInt(data);
        String itemParameters = intellifSimpleJob.itemParameters();
        itemParameters = resovleName(itemParameters);
        CoreDefination coreDefination = new CoreDefination();
        coreDefination.setClazz(clazz);
        coreDefination.setCron(cron);
        coreDefination.setName(id);
        coreDefination.setItemParameters(itemParameters);
        coreDefination.setShardingTotalCount(count);
        boolean addFlag = simpleCores.offer(coreDefination);
        if (addFlag) {
            reflectField(clazz);
        }
    }

    /**
     * 解析是否为springboot中的${}表达式如果是从springboot中获取其值
     *
     * @param name
     * @return
     */
    private static String resovleName(String name) {
        if (name.startsWith("${")) {
            String temp = name.substring(2, name.length() - 1);
            return (String) EnviromentUtils.get(temp);
        }
        return name;
    }

    /**
     * 将定时器类中使用spring ioc对象反射上去
     *
     * @param clazz
     */
    private static void reflectField(Class clazz) {
        try {
            Field[] fields = clazz.getDeclaredFields();
            if (fields != null && fields.length > 0) {
                for (Field field : fields) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        Autowired autowired = field.getAnnotation(Autowired.class);
                        Resource resource = null;
                        if (autowired != null) {
                            field.setAccessible(true);
                            Object bean = ApplicationUtils.getBean(field.getType());
                            field.set(null, bean);
                        } else if ((resource = field.getAnnotation(Resource.class)) != null) {
                            String name = resource.name();
                            field.setAccessible(true);
                            Object bean = ApplicationUtils.getBean(name, field.getType());
                            field.set(null, bean);
                        } else {
                            Value value = field.getAnnotation(Value.class);
                            if (value != null) {
                                String name = value.value();
                                name = resovleName(name);
                                field.setAccessible(true);
                                field.set(null, name);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取事件通知并执行开启定时器
     *
     * @param applicationEvent
     */
    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (applicationEvent instanceof ApplicationFinishEvent) {
            String packageName = (String) applicationEvent.getSource();
            if (!packageName.equals("finish")) {
                initJob(packageName);
            }
            doStartJobs();
        }
    }


    private static void initJob(String packageName) {
        Set<Class<?>> clazzs = ScannerClassUtils.getClzFromPkg(packageName);
        for (Class clazz : clazzs) {
            if (clazz.getAnnotation(IntellifSimpleJob.class) != null) {
                logger.info("-------------------------------->load class to intellif-job：" + clazz.getName());
                addSimpleJob(clazz);
            }
        }
    }

    /**
     * 开启多线程运行job
     */
    private void doStartJobs() {
        doStartJob();
    }

    private void doStartJob() {
        logger.info("--------------------------------> there are " + simpleCores.size() + " intellif job waiting start");
        for (; ; ) {
            CoreDefination coreDefination = simpleCores.poll();
            if (coreDefination == null) {
                return;
            }
            try {
                logger.info("-------------------------------->begain start intellif-job the class is:" + coreDefination.getClazz().getName());
                int shardingTotalCount = coreDefination.getShardingTotalCount();
                JobCoreConfiguration jobCoreConfiguration = null;
                JobCoreConfiguration.Builder builder = JobCoreConfiguration.newBuilder(coreDefination.getName(), coreDefination.getCron(), shardingTotalCount);
                if (!coreDefination.getItemParameters().equals("")) {
                    builder.shardingItemParameters(coreDefination.getItemParameters());
                }
                jobCoreConfiguration = builder.build();
                SimpleJobConfiguration simpleJobConfiguration = new SimpleJobConfiguration(jobCoreConfiguration, coreDefination.getClazz().getCanonicalName());
                //在分布式的场景下由于网络、时钟等原因，可能导致Zookeeper的数据与真实运行的作业产生不一致，这种不一致通过正向的校验无法完全避免。需要另外启动一个线程定时校验注册中心数据与真实作业状态的一致性，即维持Elastic-Job的最终一致性。
                JobScheduler jobScheduler = new JobScheduler(zookeeperRegistryCenter, LiteJobConfiguration.newBuilder(simpleJobConfiguration).reconcileIntervalMinutes(10).build());
                jobScheduler.init();
                logger.info("-------------------------------->success start intellif-job the class is:" + coreDefination.getClazz().getName());
            } catch (Exception e) {
                logger.info("--------------error-------------->start intellif-job fail:" + coreDefination.getClazz().getName() + " cause:" + e);
                logger.error("--------------error-------------->start intellif-job fail:" + coreDefination.getClazz().getName()+" cause:"+e);
                System.exit(-1);
            }
        }
    }

}
