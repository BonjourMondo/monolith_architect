package com.imooc.controller;

import com.imooc.utils.RedisOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Author: leesanghyuk
 * Date: 2020-01-16 15:30
 * Description:
 */
@ApiIgnore
@RestController
public class HelloController {
    public static Logger logger = LoggerFactory.getLogger(HelloController.class);
    @Autowired
    private RedisOperator redisOperator;

    /**
     * 测试logger
     * @return
     */
    @GetMapping("/hello")
    public Object hello() {
        logger.debug("hello");
        logger.info("hello");
        logger.warn("hello");
        logger.error("hello");
        return "Hello world";
    }

    /**
     * 测试session
     * @param request
     * @return
     */
    @GetMapping("/setSession")
    public Object setSession(HttpServletRequest request){
        HttpSession httpSession=request.getSession();
        httpSession.setAttribute("userinfo","new user");
        httpSession.setMaxInactiveInterval(3600);
        httpSession.getAttribute("userinfo");
        httpSession.removeAttribute("userinfo");
        return "ok";
    }

    /**
     * 测试redis
     * @param key
     * @return
     */
    @GetMapping("/setRedis")
    public Object setRedis(String key,String value){
//        redisTemplate.opsForValue().set(key,value);
        redisOperator.set(key,value);
        return "ok";
    }
    @GetMapping("/getRedis")
    public String getRedis(String key){
//        return (String)redisTemplate.opsForValue().get(key);
        return redisOperator.get(key);
    }
    @GetMapping("/delRedis")
    public Object delRedis(String key){
//        redisTemplate.delete(key);
        redisOperator.del(key);
        return "ok";
    }
}
