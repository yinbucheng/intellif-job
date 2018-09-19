package cn.intellif.intellifjob.excute;


import cn.intellif.intellifjob.annotation.IntellifSimpleJob;
import cn.intellif.intellifjob.config.EnviromentUtils;
import cn.intellif.intellifjob.core.CoreDefination;
import cn.intellif.intellifjob.event.ApplicationFinishEvent;
import cn.intellif.utils.ScannerUtils;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.lite.api.JobScheduler;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


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
        String core = intellifSimpleJob.core();
        core = resovleName(core);
        String data = intellifSimpleJob.shardingTotalCount();
        data = resovleName(data);
        int count = Integer.parseInt(data);
        String itemParameters = intellifSimpleJob.itemParameters();
        itemParameters = resovleName(itemParameters);
        CoreDefination coreDefination = new CoreDefination();
        coreDefination.setClazz(clazz);
        coreDefination.setCore(core);
        coreDefination.setName(id);
        coreDefination.setItemParameters(itemParameters);
        coreDefination.setShardingTotalCount(count);
        simpleCores.add(coreDefination);
    }

    private static String resovleName(String name){
        if(name.startsWith("${")){
            String temp = name.substring(2,name.length()-1);
           return (String) EnviromentUtils.get(temp);
        }
        return name;
    }

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
       List<String> classNames =  ScannerUtils.resovleClass(packageName);
       for(String className:classNames){
           try {
               Class clazz =Class.forName(className);
               if(clazz.getAnnotation(IntellifSimpleJob.class)!=null){
                   addSimpleJob(clazz);
               }
           } catch (ClassNotFoundException e) {
               e.printStackTrace();
           }
       }
    }


    private void startJobs(){
      if(simpleCores.size()>0){
              for(CoreDefination coreDefination:simpleCores){
                  int shardingTotalCount = coreDefination.getShardingTotalCount();
                  JobCoreConfiguration jobCoreConfiguration = null;
                  JobCoreConfiguration.Builder builder = JobCoreConfiguration.newBuilder(coreDefination.getName(), coreDefination.getCore(), shardingTotalCount);
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
