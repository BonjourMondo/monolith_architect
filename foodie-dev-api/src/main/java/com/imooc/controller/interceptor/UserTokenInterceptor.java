package com.imooc.controller.interceptor;

import com.imooc.utils.IMOOCJSONResult;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.RedisOperator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Author: leesanghyuk
 * Date: 2020-02-15 10:44
 * Description:构建拦截器
 */
public class UserTokenInterceptor implements HandlerInterceptor {

    public static final String REDIS_USER_TOKEN="redis_user_token";

    @Autowired
    private RedisOperator redisOperator;
    /**
     * 拦截请求，在访问controller调用之前
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userToken=request.getHeader("userToken");
        String userId=request.getHeader("userId");

        if (StringUtils.isBlank(userId)||StringUtils.isBlank(userToken)){
            returnErrorResponse(response,IMOOCJSONResult.errorMsg("请登陆..."));
            return false;
        }else{
            String uniqueToken=redisOperator.get(REDIS_USER_TOKEN+":"+userId);
            if (StringUtils.isBlank(uniqueToken)){
                returnErrorResponse(response,IMOOCJSONResult.errorMsg("请登陆..."));
                return false;
            }else{
                if (!uniqueToken.equals(userToken)){
                    returnErrorResponse(response,IMOOCJSONResult.errorMsg("检测到异地登陆，请重新登陆..."));
                    return false;
                }
            }
        }


        /**
         * false代表被拦截，说明请求被驳回
         * true代表请求成功
         */
        //全部通过
        return true;
    }

    /**
     * 由于preHandler返回的是boolean，所以采用一种传统的方法输出到前端
     * @param response
     * @param result
     */
    public void returnErrorResponse(HttpServletResponse response,
                                    IMOOCJSONResult result) {
        OutputStream out = null;
        try {
            response.setCharacterEncoding("utf-8");
            response.setContentType("text/json");
            out = response.getOutputStream();
            out.write(JsonUtils.objectToJson(result).getBytes("utf-8"));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 在访问controller调用之后，在渲染视图之前
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    /**
     * 在渲染视图之后
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
