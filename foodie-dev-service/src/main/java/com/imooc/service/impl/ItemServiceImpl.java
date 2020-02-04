package com.imooc.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.imooc.enums.ItemLevel;
import com.imooc.mapper.ItemsCommentsMapper;
import com.imooc.mapper.ItemsMapperCustom;
import com.imooc.pojo.ItemsComments;
import com.imooc.pojo.vo.CommentCountsVO;
import com.imooc.pojo.vo.ItemCommentVO;
import com.imooc.service.ItemService;
import com.imooc.utils.DesensitizationUtil;
import com.imooc.utils.PagedGridResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: leesanghyuk
 * Date: 2020-01-30 15:30
 * Description:
 */
@Service
public class ItemServiceImpl implements ItemService {
    @Autowired
    private ItemsCommentsMapper itemsCommentsMapper;
    @Autowired
    private ItemsMapperCustom itemsMapperCustom;

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public CommentCountsVO queryCommentCounts(String itemId) {
        Integer goodCounts = getCommentsCounts(itemId, ItemLevel.GOOD.type);
        Integer normalCounts = getCommentsCounts(itemId, ItemLevel.NOMAL.type);
        Integer badCounts = getCommentsCounts(itemId, ItemLevel.BAD.type);
        Integer totalCounts = goodCounts + normalCounts + badCounts;

        CommentCountsVO commentCountsVO = new CommentCountsVO();
        commentCountsVO.setGoodCounts(goodCounts);
        commentCountsVO.setTotalCounts(totalCounts);
        commentCountsVO.setBadCounts(badCounts);
        commentCountsVO.setNormalCounts(normalCounts);
        return commentCountsVO;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public PagedGridResult queryPagedComments(String itemId, Integer level,
                                              Integer page, Integer pageSize) {
        Map<String, Object> map = new HashMap<>();
        map.put("itemId", itemId);
        map.put("level", level);

        PageHelper.startPage(page, pageSize);
        List<ItemCommentVO> itemCommentVOS = itemsMapperCustom.queryItemComments(map);

        //信息脱敏
        for(ItemCommentVO itemCommentVO:itemCommentVOS){
            itemCommentVO.setNickname(DesensitizationUtil.commonDisplay(itemCommentVO.getNickname()));
        }
        return setPagedGrid(itemCommentVOS, page);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    Integer getCommentsCounts(String itemId, Integer level) {
        Example example = new Example(ItemsComments.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("itemId", itemId);
        if (level != null) {
            criteria.andEqualTo("commentLevel", level);
        }
        return itemsCommentsMapper.selectCountByExample(example);

    }

    PagedGridResult setPagedGrid(List<?> list, Integer page) {
        PageInfo<?> pageInfo = new PageInfo<>(list);
        PagedGridResult pagedGridResult = new PagedGridResult();
        pagedGridResult.setPage(page);
        pagedGridResult.setRows(list);
        pagedGridResult.setRecords(pageInfo.getTotal());
        pagedGridResult.setTotal(pageInfo.getPages());
        return pagedGridResult;
    }
}
