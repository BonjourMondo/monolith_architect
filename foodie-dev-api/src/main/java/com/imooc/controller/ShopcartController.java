package com.imooc.controller;

import com.imooc.pojo.bo.ShopcartBO;
import com.imooc.utils.IMOOCJSONResult;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.RedisOperator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: leesanghyuk
 * Date: 2020-01-31 15:49
 * Description:
 */
@ApiIgnore
@RestController
@RequestMapping("shopcart")
public class ShopcartController extends BasicController {
    @Autowired
    private RedisOperator redisOperator;

    @PostMapping("/add")
    public IMOOCJSONResult add(@RequestParam String userId,
                               @RequestBody ShopcartBO shopcartBO,
                               HttpServletRequest request,
                               HttpServletResponse response) {
        if (StringUtils.isBlank(userId)) {
            //未登陆时，通过前段保存cookie即可，不需要后端操作。
            return IMOOCJSONResult.errorMsg("参数不能为空");
        }
        String shopcartStr = redisOperator.get(FOODIE_SHOPCART + ":" + userId);
        List<ShopcartBO> shopcartBOS = null;
        if (StringUtils.isBlank(shopcartStr)) {
            //如果缓存中没有
            shopcartBOS = new ArrayList<>();
            shopcartBOS.add(shopcartBO);
        } else {
            //如果缓存中有
            shopcartBOS = JsonUtils.jsonToList(shopcartStr, ShopcartBO.class);
            boolean isHaving = false;
            for (ShopcartBO bo : shopcartBOS) {
                String tmpSpecId = bo.getSpecId();
                if (tmpSpecId.equalsIgnoreCase(shopcartBO.getSpecId())) {
                    //说明购物车里已经有了这个物品，只需要累加
                    bo.setBuyCounts(bo.getBuyCounts() + shopcartBO.getBuyCounts());
                    isHaving = true;
                }
            }
            if (!isHaving) {
                shopcartBOS.add(shopcartBO);
            }
        }
        redisOperator.set(FOODIE_SHOPCART + ":" + userId, JsonUtils.objectToJson(shopcartBOS));
        return IMOOCJSONResult.ok();
    }

    @PostMapping("/del")
    public IMOOCJSONResult del(@RequestParam String userId,
                               @RequestParam String itemSpecId,
                               HttpServletRequest request,
                               HttpServletResponse response) {
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(itemSpecId)) {
            return IMOOCJSONResult.errorMsg("参数不能为空");
        }
        String shopcartStr = redisOperator.get(FOODIE_SHOPCART + ":" + userId);
        if (!StringUtils.isBlank(shopcartStr)) {
            List<ShopcartBO> shopcartBOS = JsonUtils.jsonToList(shopcartStr, ShopcartBO.class);
            for (ShopcartBO bo : shopcartBOS) {
                if (bo.getSpecId().equalsIgnoreCase(itemSpecId)) {
                    shopcartBOS.remove(bo);
                    break;
                }
            }
            redisOperator.set(FOODIE_SHOPCART + ":" + userId, JsonUtils.objectToJson(shopcartBOS));
        }
        return IMOOCJSONResult.ok();
    }

}
