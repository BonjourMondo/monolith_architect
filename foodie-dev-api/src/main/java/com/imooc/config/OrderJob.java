package com.imooc.config;

import com.imooc.utils.DateUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Author: leesanghyuk
 * Date: 2020-02-03 16:03
 * Description:
 */
@Component
public class OrderJob {
    //在线cron表达式生成器：http://cron.qqe2.com/

    /**
     * 定时任务弊端：
     * 1。在集群下会产生多个定时任务，
     * 2。定时间隔产生时间差，使得程序不严谨
     * 3。定时任务仅仅适用于小型的轻量级的项目，
     *      如果向是下面的关闭订单，会对整个数据表进行定期的全表扫描，对性能产生影响（替代方案MQ）
     *
     */

//    @Scheduled(cron = "0/4 * * * * ? ")
//    public void autoCloserOrder(){
//
//        System.out.println("执行定时任务当前时间为："+
//                DateUtil.getCurrentDateString(DateUtil.DATE_PATTERN));
//
//
//    }
}
