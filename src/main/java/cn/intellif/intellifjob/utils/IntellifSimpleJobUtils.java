package cn.intellif.intellifjob.utils;


import cn.intellif.intellifjob.config.ApplicationUtils;
import cn.intellif.intellifjob.event.ApplicationFinishEvent;
import cn.intellif.intellifjob.excute.JobExcute;

/**
 * author 尹冲
 */
public abstract class IntellifSimpleJobUtils {
    public static void  initSimpleJobs(Class ... clazzs){
        for(Class clazz:clazzs){
            JobExcute.addSimpleJob(clazz);
        }
    }



    public  static void startJob(){
        ApplicationUtils.getApplicationContext().publishEvent(new ApplicationFinishEvent("finish"));
    }

    public static void startJob(String packageName){
        ApplicationUtils.getApplicationContext().publishEvent(new ApplicationFinishEvent(packageName));
    }
}
