package com.imooc.controller;

import com.imooc.pojo.Users;
import com.imooc.pojo.bo.UserBO;
import com.imooc.service.UserService;
import com.imooc.utils.CookieUtils;
import com.imooc.utils.IMOOCJSONResult;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.MD5Utils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Author: leesanghyuk
 * Date: 2020-01-27 15:05
 * Description:
 */
@Api(value = "注册登陆", tags = {"用于注册登陆相关的接口"})
@RestController
@RequestMapping("passport")
public class PassportController {
    @Autowired
    public UserService userService;

    @ApiOperation(value = "用户名查重", notes = "用户名查重", httpMethod = "GET")
    @GetMapping("/usernameIsExist")
    public IMOOCJSONResult usernameIsExist(@RequestParam String username) {
        //1.判断入参不为空
        if (StringUtils.isBlank(username)) {
            return IMOOCJSONResult.errorMsg("用户名不能为空");
        }
        //2.查找用户名存在
        boolean isExist = userService.queryUsernameIsExist(username);
        if (isExist) {
            return IMOOCJSONResult.errorMsg("用户名已存在");
        } else {
            //3.请求成功，用户名没有重复
            return IMOOCJSONResult.ok();
        }
    }

    @ApiOperation(value = "用户注册", notes = "用户注册", httpMethod = "POST")
    @PostMapping("/regist")
    public IMOOCJSONResult regist(@RequestBody UserBO userBO) {
        String username = userBO.getUsername();
        String password = userBO.getPassword();
        String confirmPWD = userBO.getConfirmPassword();
        //1. 判断入参不为空
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password) || StringUtils.isBlank(confirmPWD)) {
            return IMOOCJSONResult.errorMsg("用户名或密码不能为空");
        }
        //2. 判断用户名是否重复
        boolean isExist = userService.queryUsernameIsExist(username);
        if (isExist) {
            return IMOOCJSONResult.errorMsg("用户名已存在");
        }
        //3. 密码长度不能少于6位
        if (password.length() < 6) {
            return IMOOCJSONResult.errorMsg("密码长度不能小于6");
        }
        //4. 判断两次密码是否一致
        if (!password.equals(confirmPWD)) {
            return IMOOCJSONResult.errorMsg("两次密码输入不一致");
        }
        //5. 实现注册
        userService.createUser(userBO);
        // TODO: 2020-01-31 创建用户token，存入redis缓存
        // TODO: 2020-01-31 同步购物车数据
        return IMOOCJSONResult.ok();
    }

    @ApiOperation(value = "用户登陆", notes = "用户登陆", httpMethod = "POST")
    @PostMapping("/login")
    public IMOOCJSONResult login(@RequestBody UserBO userBO,
                                 HttpServletRequest request, HttpServletResponse response) throws Exception {
        String username = userBO.getUsername();
        String password = userBO.getPassword();
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            return IMOOCJSONResult.errorMsg("用户名或密码不能为空");
        }
        Users userResult = userService.queryUserForLogin(username, MD5Utils.getMD5Str(password));

        if(userResult==null) {
            return IMOOCJSONResult.errorMsg("用户名或密码不正确");
        }
        //设置为空，不在前端显示
        userResult=setNULL(userResult);
        CookieUtils.setCookie(request,response,"user",
                JsonUtils.objectToJson(userResult),true);
        // TODO: 2020-01-31 创建用户token，存入redis缓存
        // TODO: 2020-01-31 同步购物车数据
        return IMOOCJSONResult.ok(userResult);
    }

    @ApiOperation(value = "用户注销", notes = "用户注销", httpMethod = "POST")
    @PostMapping("/logout")
    public IMOOCJSONResult logout(@RequestParam String userId,
                                  HttpServletRequest request,HttpServletResponse response){
        CookieUtils.deleteCookie(request,response,"user");
        // TODO: 2020-01-29 用户退出时，需要清空购物车
        // TODO: 2020-01-29 分布式会话中需要清除用户数据
        return IMOOCJSONResult.ok();
    }

    private Users setNULL(Users userResult){
        userResult.setPassword(null);
        userResult.setCreatedTime(null);
        userResult.setUpdatedTime(null);
        return userResult;
    }

}
