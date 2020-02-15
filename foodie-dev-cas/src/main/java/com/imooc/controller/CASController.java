package com.imooc.controller;

import com.imooc.pojo.Users;
import com.imooc.pojo.vo.UsersVO;
import com.imooc.service.UserService;
import com.imooc.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * Author: leesanghyuk
 * Date: 2020-02-15 14:19
 * Description:
 */
@Controller
public class CASController {
    @Autowired
    private UserService userService;
    @Autowired
    private RedisOperator redisOperator;

    public static final String REDIS_USER_TOKEN = "redis_user_token";
    public static final String REDIS_USER_TICKET = "redis_user_ticket";
    public static final String REDIS_TMP_TICKET = "redis_tmp_ticket";
    public static final String COOKIE_USER_TICKET = "cookie_user_ticket";

    @GetMapping("/login")
    public String login(String returnUrl,
                        Model model, HttpServletRequest request, HttpServletResponse response) {
        model.addAttribute("returnUrl", returnUrl);

        //完善校验是否已经登陆
        //获取用户门票userticket，如果已经登陆过，签发临时票据
        if (verifyUserTicket(request)) {
            String tmpTicket = createTmpTicket();
            return "redirect:" + returnUrl + "?tmpTicket=" + tmpTicket;
        }


        return "login";
    }

    /**
     * CAS统一接口的目的：
     * 登陆后创建用户的全局会话  -> userToken
     * 创建用户的全局门票，用以表示用户是否在CAS端已登陆  -> userTicket
     * 创建用户的临时门票，用以回跳和回传  -> tmpTicket
     */
    @PostMapping("/doLogin")
    public String doLogin(String returnUrl, String username, String password,
                          Model model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        model.addAttribute("returnUrl", returnUrl);

        //1.登陆
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            model.addAttribute("errmsg", "用户名或密码不能为空");
            return "login";
        }
        Users userResult = userService.queryUserForLogin(username, MD5Utils.getMD5Str(password));

        if (userResult == null) {
            model.addAttribute("errmsg", "用户名或密码不正确");
            return "login";
        }

        //2.实现用户会话
        String uniqueToken = UUID.randomUUID().toString().trim();
        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(userResult, usersVO);
        usersVO.setUserUniqueToken(uniqueToken);
        redisOperator.set(REDIS_USER_TOKEN + ":" + userResult.getId(), JsonUtils.objectToJson(usersVO));

        //3. 生成用户全局门票
        String userTicket = UUID.randomUUID().toString().trim();
        //3.1 用户全局门票需要放入cookie中
        setCookie(COOKIE_USER_TICKET, userTicket, response);
        //4. userTicket需要关联userId，并且放入到redis中
        redisOperator.set(REDIS_USER_TICKET + ":" + userTicket, userResult.getId());

        //5. 生成临时票据，回跳到调用端网站，是由CAS签发的临时ticket
        String tmpTicket = createTmpTicket();
        /**
         * userTicket用于表示用户在CAS端登陆的状态：是否已经登陆
         * tmpTicket用于给用户颁发登陆的票据，是一次性的。这个凭证可以获取用户登陆态的信息
         */

        //用户从未登陆过，则跳转到cas的统一登陆页面

        return "redirect:" + returnUrl + "?tmpTicket=" + tmpTicket;

//        return "login";
    }

    /**
     * 使用一次性临时票据来检测用户是否登陆过，使用完毕后需要销毁临时票据
     *
     * @param tmpTicket
     * @param request
     * @param response
     * @return
     */
    @PostMapping("/verifyTmpTicket")
    @ResponseBody
    public IMOOCJSONResult verifyTmpTicket(@RequestParam("tmpTicket") String tmpTicket,
                                           HttpServletRequest request, HttpServletResponse response) throws Exception {
        String tmpTicketValue = redisOperator.get(REDIS_TMP_TICKET + ":" + tmpTicket);
        if (StringUtils.isBlank(tmpTicketValue)) {
            return IMOOCJSONResult.errorMsg("用户票据异常");
        }
        //0.如果临时票据ok，那么需要销毁，并且拿到CAS端的全局ticket，以此再获取用户会话
        if (!tmpTicketValue.equals(MD5Utils.getMD5Str(tmpTicket))) {
            return IMOOCJSONResult.errorMsg("用户票据异常");
        } else {
            //临时票据校验成功，销毁它
            redisOperator.del(REDIS_TMP_TICKET + ":" + tmpTicket);
        }

        //1. 验证，并且换取userTicket
        String userTicket = getCookie(COOKIE_USER_TICKET, request);
        if (StringUtils.isBlank(userTicket)) {
            return IMOOCJSONResult.errorMsg("用户票据异常");
        }
        String userId = redisOperator.get(REDIS_USER_TICKET + ":" + userTicket);
        if (StringUtils.isBlank(userId)) {
            return IMOOCJSONResult.errorMsg("用户票据异常");
        }

        //2.验证userId对应的会话是否存在
        String userSession = redisOperator.get(REDIS_USER_TOKEN + ":" + userId);
        if (StringUtils.isBlank(userSession)) {
            return IMOOCJSONResult.errorMsg("用户票据异常");
        }
        //3.验证成功，返回用户会话
        return IMOOCJSONResult.ok(JsonUtils.jsonToPojo(userSession, UsersVO.class));
    }

    @PostMapping("/logout")
    @ResponseBody
    public IMOOCJSONResult logout(String userId,
                                  HttpServletRequest request,
                                  HttpServletResponse response) throws Exception {

        // 0. 获取CAS中的用户门票
        String userTicket = getCookie(COOKIE_USER_TICKET, request);

        // 1. 清除userTicket票据，redis/cookie
        deleteCookie(COOKIE_USER_TICKET, response);
        redisOperator.del(REDIS_USER_TICKET + ":" + userTicket);

        // 2. 清除用户全局会话（分布式会话）
        redisOperator.del(REDIS_USER_TOKEN + ":" + userId);

        return IMOOCJSONResult.ok();
    }

    //创建临时票据
    private String createTmpTicket() {
        String tmpTicket = UUID.randomUUID().toString().trim();
        try {
            redisOperator.set(REDIS_TMP_TICKET + ":" + tmpTicket, MD5Utils.getMD5Str(tmpTicket), 600);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tmpTicket;
    }

    private void setCookie(String key, String value, HttpServletResponse response) {
        Cookie cookie = new Cookie(key, value);
        cookie.setDomain("cas.com");
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    private void deleteCookie(String key, HttpServletResponse response) {
        Cookie cookie = new Cookie(key, null);
        cookie.setDomain("cas.com");
        cookie.setPath("/");
        cookie.setMaxAge(-1);
        response.addCookie(cookie);

    }

    private String getCookie(String key, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || StringUtils.isBlank(key)) {
            return null;
        }
        String cookieValue = null;
        for (Cookie c : cookies) {
            if (c.getName().equals(key)) {
                cookieValue = c.getValue();
                break;
            }
        }
        return cookieValue;

    }

    private boolean verifyUserTicket(HttpServletRequest request) {
        String userTicket = getCookie(COOKIE_USER_TICKET, request);
        if (StringUtils.isBlank(userTicket)) {
            return false;
        }
        String userId = redisOperator.get(REDIS_USER_TICKET + ":" + userTicket);
        if (StringUtils.isBlank(userId)) {
            return false;
        }
        String userSession = redisOperator.get(REDIS_USER_TOKEN + ":" + userId);
        if (StringUtils.isBlank(userSession)) {
            return false;
        }
        return true;
    }


}
