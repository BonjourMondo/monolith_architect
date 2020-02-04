package com.imooc.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Author: leesanghyuk
 * Date: 2020-01-29 11:58
 * Description:
 */
@Component
@Aspect
public class ServiceLogAspect {
    public static final Logger log= LoggerFactory.getLogger(ServiceLogAspect.class);

    /**
     *  execution 代表所要执行的表达式主体
     *  第一处 *   代表方法返回类型，*表示所有类型
     *  第二处 包名  代表aop监控的类所在的包
     *  第三处 ..  代表该包下面的所有类
     *  第四处 * 代表类
     *  第五处 *(..) 代表类中的方法名，(..)表示方法中的任何参数
     * @param proceedingJoinPoint
     * @return
     * @throws Throwable
     */
    @Around("execution(* com.imooc.service.impl..*.*(..))")
    public Object recodeTimeLog(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        log.info("===== 开始执行 {}.{} =====",
                proceedingJoinPoint.getTarget().getClass(),
                proceedingJoinPoint.getSignature().getName());
        long begin=System.currentTimeMillis();
        //执行目标service
        Object o=proceedingJoinPoint.proceed();
        long end=System.currentTimeMillis();
        long takeTime=end-begin;
        if(takeTime>3000){
            log.error("===== 执行结束，耗时{}ms ======",takeTime);
        }else if (takeTime>2000){
            log.warn("===== 执行结束，耗时{}ms ======",takeTime);
        }else{
            log.info("===== 执行结束，耗时{}ms ======",takeTime);
        }
        return o;

    }
}
