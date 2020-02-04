package com.imooc.service;

import com.imooc.pojo.Carousel;

import java.util.List;

/**
 * Author: leesanghyuk
 * Date: 2020-01-29 15:43
 * Description:
 */
public interface CarouselService {

    /**
     * 查询所有轮播图
     * @param isShow
     * @return
     */
     List<Carousel> queryAll(Integer isShow);
}
