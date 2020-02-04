package com.imooc.service;

import com.imooc.pojo.vo.CommentCountsVO;
import com.imooc.pojo.vo.ItemCommentVO;
import com.imooc.utils.PagedGridResult;

import java.util.List;

/**
 * Author: leesanghyuk
 * Date: 2020-01-30 15:28
 * Description:
 */
public interface ItemService {
    /**
     * 根据商品id查询评价数目
     * @param itemId
     */
    CommentCountsVO queryCommentCounts(String itemId);

    /**
     * 根据商品id和level查询商品评价
     * @param itemId
     * @param level
     * @return
     */
    PagedGridResult queryPagedComments(String itemId, Integer level,
                                       Integer page, Integer pageSize);

}
