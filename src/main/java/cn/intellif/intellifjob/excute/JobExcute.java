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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

/**
 * author 尹冲
 */
public class JobExcute implements ApplicationListener {
    @Autowired
    private ZookeeperRegistryCenter zookeeperRegistryCenter;

    private static Set<CoreDefination> simpleCores = new HashSet<>();

    public static void addSimpleJob(Class clazz){
        IntellifSimpleJob intellifSimpleJob = (IntellifSimpleJob) clazz.getAnnotation(IntellifSimpleJob.class);
        String id = intellifSimpleJob.id();
        if(id.equals("")){
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
        boolean addFlag = simpleCores.add(coreDefination);
        if(addFlag){
            reflectField(clazz);
        }
    }

    /**
     * 解析是否为springboot中的${}表达式如果是从springboot中获取其值
     * @param name
     * @return
     */
    private static String resovleName(String name){
        if(name.startsWith("${")){
            String temp = name.substring(2,name.length()-1);
           return (String) EnviromentUtils.get(temp);
        }
        return name;
    }

    /**
     * 将定时器类中使用spring ioc对象反射上去
     * @param clazz
     */
    private static void reflectField(Class clazz){
        try {
            Field[] fields = clazz.getDeclaredFields();
            if (fields != null && fields.length > 0) {
                for (Field field : fields) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        Autowired autowired = field.getAnnotation(Autowired.class);
                        if (autowired != null) {
                            field.setAccessible(true);
                            field.set(null, ApplicationUtils.getBean(field.getType()));
                        }else{
                           Resource resource =  field.getAnnotation(Resource.class);
                           if(resource!=null){
                              String name =  resource.name();
                              field.setAccessible(true);
                              field.set(null,ApplicationUtils.getBean(name,field.getType()));
                           }
                        }
                    }
                }
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取事件通知并执行开启定时器
     * @param applicationEvent
     */
    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if(applicationEvent instanceof ApplicationFinishEvent){
            String packageName = (String) applicationEvent.getSource();
            if(!packageName.equals("finish")) {
                initJob(packageName);
            }
            startJobs();
        }
    }

    private static void initJob(String packageName){
       Set<Class<?>> clazzs = ScannerClassUtils.getClzFromPkg(packageName);
       for(Class clazz:clazzs){
           if(clazz.getAnnotation(IntellifSimpleJob.class)!=null){
               addSimpleJob(clazz);
           }
       }
    }


    private void startJobs(){
      if(simpleCores.size()>0){
              for(CoreDefination coreDefination:simpleCores){
                  int shardingTotalCount = coreDefination.getShardingTotalCount();
                  JobCoreConfiguration jobCoreConfiguration = null;
                  JobCoreConfiguration.Builder builder = JobCoreConfiguration.newBuilder(coreDefination.getName(), coreDefination.getCron(), shardingTotalCount);
                  if(!coreDefination.getItemParameters().equals("")) {
                      builder.shardingItemParameters(coreDefination.getItemParameters());
                  }
                  jobCoreConfiguration = builder.build();
                  SimpleJobConfiguration simpleJobConfiguration = new SimpleJobConfiguration(jobCoreConfiguration,coreDefination.getClazz().getCanonicalName());
                  JobScheduler jobScheduler = new JobScheduler(zookeeperRegistryCenter, LiteJobConfiguration.newBuilder(simpleJobConfiguration).build());
                  try {
                      jobScheduler.init();
                  } catch (Exception e) {
                      e.printStackTrace();
                      throw new RuntimeException("定时任务创建失败");
                  }
              }
          simpleCores = null;
      }
    }
}
