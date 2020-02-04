package com.imooc.controller;

import com.imooc.pojo.vo.CommentCountsVO;
import com.imooc.service.ItemService;
import com.imooc.utils.IMOOCJSONResult;
import com.imooc.utils.PagedGridResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

/**
 * Author: leesanghyuk
 * Date: 2020-01-30 15:46
 * Description:
 */
@ApiIgnore
@RestController
@RequestMapping("items")
public class ItemsController extends BasicController{
    @Autowired
    private ItemService itemService;

    @GetMapping("/commentLevel")
    public IMOOCJSONResult commentLevel(@RequestParam String itemId){
        if(StringUtils.isBlank(itemId)){
            return IMOOCJSONResult.errorMsg("缺少查询的商品id");
        }
        CommentCountsVO commentCountsVO=itemService.queryCommentCounts(itemId);
        return IMOOCJSONResult.ok(commentCountsVO);
    }

    @GetMapping("/comments")
    public IMOOCJSONResult comments(@RequestParam String itemId,
                                    @RequestParam Integer level,
                                    @RequestParam Integer page,
                                    @RequestParam Integer pageSize){
        if(StringUtils.isBlank(itemId)){
            return IMOOCJSONResult.errorMsg("缺少查询的商品id");
        }
        if(page==null){
            page=1;
        }
        if(pageSize==null){
            pageSize=COMMENT_PAGE_SIZE;
        }
        PagedGridResult pagedGridResult=itemService.queryPagedComments(itemId,level,page,pageSize);
        return IMOOCJSONResult.ok(pagedGridResult);
    }
}
