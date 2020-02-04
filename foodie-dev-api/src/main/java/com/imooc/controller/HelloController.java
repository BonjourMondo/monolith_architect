package com.imooc.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @GetMapping("/hello")
    public Object hello() {
        logger.debug("hello");
        logger.info("hello");
        logger.warn("hello");
        logger.error("hello");
        return "Hello world";
    }

    @GetMapping("/setSession")
    public Object setSession(HttpServletRequest request){
        HttpSession httpSession=request.getSession();
        httpSession.setAttribute("userinfo","new user");
        httpSession.setMaxInactiveInterval(3600);
        httpSession.getAttribute("userinfo");
        httpSession.removeAttribute("userinfo");
        return "ok";
    }

}
