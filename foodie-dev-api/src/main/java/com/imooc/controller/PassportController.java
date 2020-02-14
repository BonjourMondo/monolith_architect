package com.imooc.controller;

import com.imooc.pojo.Users;
import com.imooc.pojo.bo.ShopcartBO;
import com.imooc.pojo.bo.UserBO;
import com.imooc.pojo.vo.UsersVO;
import com.imooc.service.UserService;
import com.imooc.utils.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Author: leesanghyuk
 * Date: 2020-01-27 15:05
 * Description:
 */
@Api(value = "注册登陆", tags = {"用于注册登陆相关的接口"})
@RestController
@RequestMapping("passport")
public class PassportController extends BasicController {
    @Autowired
    private UserService userService;
    @Autowired
    private RedisOperator redisOperator;

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
    public IMOOCJSONResult regist(@RequestBody UserBO userBO,
                                  HttpServletRequest request, HttpServletResponse response) {
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
        Users userResult = userService.createUser(userBO);
        //创建用户token，存入redis缓存
        UsersVO usersVO = convertUsersVO(userResult);
        //同步购物车数据
        syncShopcartData(usersVO.getId(), request, response);
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

        if (userResult == null) {
            return IMOOCJSONResult.errorMsg("用户名或密码不正确");
        }
        //设置为空，不在前端显示
//        userResult=setNULL(userResult); 使用了UsersVO，这里直接注释

        //创建用户token，存入redis缓存
        UsersVO usersVO = convertUsersVO(userResult);

        CookieUtils.setCookie(request, response, "user",
                JsonUtils.objectToJson(usersVO), true);

        //同步购物车数据
        syncShopcartData(usersVO.getId(), request, response);
        return IMOOCJSONResult.ok(usersVO);
    }

    @ApiOperation(value = "用户注销", notes = "用户注销", httpMethod = "POST")
    @PostMapping("/logout")
    public IMOOCJSONResult logout(@RequestParam String userId,
                                  HttpServletRequest request, HttpServletResponse response) {
        CookieUtils.deleteCookie(request, response, "user");
        // 用户退出时，需要清空购物车
        CookieUtils.deleteCookie(request, response, FOODIE_SHOPCART);
        //分布式会话中需要清除用户数据
        redisOperator.del(REDIS_USER_TOKEN+":"+userId);

        return IMOOCJSONResult.ok();
    }

//    private Users setNULL(Users userResult){
//        userResult.setPassword(null);
//        userResult.setCreatedTime(null);
//        userResult.setUpdatedTime(null);
//        return userResult;
//    }

    public UsersVO convertUsersVO(Users userResult) {
        String uniqueToken = UUID.randomUUID().toString().trim();
        redisOperator.set(REDIS_USER_TOKEN + ":" + userResult.getId(), uniqueToken);
        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(userResult, usersVO);
        usersVO.setUserUniqueToken(uniqueToken);
        return usersVO;
    }

    /**
     * 同步cookie和redis中的购物车数据：
     * 如果cookie和redis数据都为空，那么不做任何处理
     * 如果某一个数据为空，那么使得两个数据相同
     * 如果都不为空，如果存在cookie中的数据与redis数据相同的部分，以cookie为准再同步
     */
    private void syncShopcartData(String userId, HttpServletRequest request, HttpServletResponse response) {
        //redis
        String shopcartStrRedis = redisOperator.get(FOODIE_SHOPCART + ":" + userId);
        //cookie
        String shopcartStrCookie = CookieUtils.getCookieValue(request, FOODIE_SHOPCART, true);
        if (StringUtils.isBlank(shopcartStrRedis)) {
            if (StringUtils.isNotBlank(shopcartStrCookie)) {
                redisOperator.set(FOODIE_SHOPCART + ":" + userId, shopcartStrCookie);
            }
        } else {
            if (StringUtils.isBlank(shopcartStrCookie)) {
                CookieUtils.setCookie(request, response, FOODIE_SHOPCART, shopcartStrRedis, true);
            } else {
                //两者都不为空
                List<ShopcartBO> shopcartBORedisList = JsonUtils.jsonToList(shopcartStrRedis, ShopcartBO.class);
                List<ShopcartBO> shopcartBOCookieList = JsonUtils.jsonToList(shopcartStrCookie, ShopcartBO.class);

                //定义一个待删除List
                List<ShopcartBO> pendingDeleteList = new ArrayList<>();
                for (ShopcartBO redisBo : shopcartBORedisList) {
                    String redisSpecId = redisBo.getSpecId();
                    for (ShopcartBO cookieBo : shopcartBOCookieList) {
                        String cookieSpecId = cookieBo.getSpecId();
                        if (redisSpecId.equalsIgnoreCase(cookieSpecId)) {
                            //覆盖购买数目，不是累加
                            redisBo.setBuyCounts(cookieBo.getBuyCounts());
                            //把cookieBo放入待删除列表，用于最后的合并
                            pendingDeleteList.add(cookieBo);
                        }
                    }
                }
                //删除cookie中重复的
                shopcartBOCookieList.removeAll(pendingDeleteList);
                //合并两个
                shopcartBORedisList.addAll(shopcartBOCookieList);
                //以现在的redisList为准，更新redis和cookie
                redisOperator.set(FOODIE_SHOPCART + ":" + userId, JsonUtils.objectToJson(shopcartBORedisList));
                CookieUtils.setCookie(request, response, FOODIE_SHOPCART, JsonUtils.objectToJson(shopcartBORedisList), true);
            }
        }

    }
}
