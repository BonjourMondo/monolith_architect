package com.imooc.service;

import com.imooc.pojo.Category;
import com.imooc.pojo.vo.CategoryVO;

import java.util.List;

public interface CategoryService {
    /**
     * 查询1级列表
     * @return
     */
    List<Category> queryAllRootLevelCat();

    /**
     * 根据分类id查询子列表（2、3级列表）
     * @param rootCatId
     * @return
     */
    List<CategoryVO> getSubCatList(Integer rootCatId);
}
