package com.imooc.mapper;

import com.imooc.pojo.vo.ItemCommentVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * Author: leesanghyuk
 * Date: 2020-01-30 16:13
 * Description:
 */
public interface ItemsMapperCustom {
    /**
     * 查询商品评价
     * @param map
     * @return
     */
    List<ItemCommentVO> queryItemComments(@Param("paramsMap") Map<String,Object> map);
}
