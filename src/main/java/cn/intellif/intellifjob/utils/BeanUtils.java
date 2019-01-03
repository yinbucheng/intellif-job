package cn.intellif.intellifjob.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @ClassName BeanUtils
 * @Author buchengyin
 * @Date 2019/1/3 9:46
 **/
public class BeanUtils {
    /**
     * 获取当前时间
     * @return
     */
    public static String getCurrentTime(){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(new Date());
    }
}
