package com.imooc.controller;

import com.imooc.pojo.bo.ShopcartBO;
import com.imooc.utils.IMOOCJSONResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Author: leesanghyuk
 * Date: 2020-01-31 15:49
 * Description:
 */
@ApiIgnore
@RestController
@RequestMapping("shopcart")
public class ShopcartController {
    @PostMapping("/add")
    public IMOOCJSONResult add(@RequestParam String userId,
                               @RequestBody ShopcartBO shopcartBO,
                               HttpServletRequest request,
                               HttpServletResponse response) {
        if (StringUtils.isBlank(userId)) {
            //未登陆时，通过前段保存cookie即可，不需要后端操作。
            return IMOOCJSONResult.errorMsg("参数不能为空");
        }
        // TODO: 2020-01-31 前端用户在登陆的情况下，添加商品至购物车会在redis中同步

        return IMOOCJSONResult.ok();
    }
    @PostMapping("/del")
    public IMOOCJSONResult del(@RequestParam String userId,
                               @RequestParam String itemSpecId,
                               HttpServletRequest request,
                               HttpServletResponse response) {
        if (StringUtils.isBlank(userId)||StringUtils.isBlank(itemSpecId)) {
            return IMOOCJSONResult.errorMsg("参数不能为空");
        }
        // TODO: 2020-01-31 前端用户在登陆的情况下，删除商品至购物车会在redis中同步
        return IMOOCJSONResult.ok();
    }

}
