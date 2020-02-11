package com.imooc.controller;

import com.imooc.enums.YesOrNo;
import com.imooc.pojo.Carousel;
import com.imooc.pojo.Category;
import com.imooc.pojo.vo.CategoryVO;
import com.imooc.pojo.vo.SubCategoryVO;
import com.imooc.service.CarouselService;
import com.imooc.service.CategoryService;
import com.imooc.utils.IMOOCJSONResult;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.RedisOperator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Author: leesanghyuk
 * Date: 2020-01-29 15:49
 * Description:
 */
@Api(value = "首页", tags = {"首页展示的相关接口"})
@RestController
@RequestMapping("index")
public class IndexController {
    @Autowired
    private CarouselService carouselService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private RedisOperator redisOperator;

    @ApiOperation(value = "轮播图", notes = "轮播图", httpMethod = "GET")
    @GetMapping("/carousel")
    public IMOOCJSONResult carousel() {
        String carouselStr=redisOperator.get("carousel");
        List<Carousel> carouselList ;
        if (StringUtils.isBlank(carouselStr)){
             carouselList=carouselService.queryAll(YesOrNo.YES.type);
            redisOperator.set("carousel", JsonUtils.objectToJson(carouselList));
        }else {
            carouselList=JsonUtils.jsonToList(carouselStr,Carousel.class);
        }
        return IMOOCJSONResult.ok(carouselList);
    }

    @ApiOperation(value = "获取商品分类（一级分类）", notes = "获取商品分类（一级分类）", httpMethod = "GET")
    @GetMapping("/cats")
    public IMOOCJSONResult cats() {
        List<Category> categoryList;
        String categoryListStr=redisOperator.get("cats");
        if(StringUtils.isBlank(categoryListStr)){
            categoryList = categoryService.queryAllRootLevelCat();
            redisOperator.set("cats",JsonUtils.objectToJson(categoryList));
        }else{
            categoryList=JsonUtils.jsonToList(categoryListStr,Category.class);
        }


        return IMOOCJSONResult.ok(categoryList);
    }

    @ApiOperation(value = "获取商品子分类", notes = "获取商品子分类", httpMethod = "GET")
    @GetMapping("/subCat/{rootCatId}")
    public IMOOCJSONResult subCat(
            @ApiParam(name = "rootCatId",value = "一级分类Id",required = true)
            @PathVariable Integer rootCatId) {
        if(rootCatId==null){
            return IMOOCJSONResult.errorMsg("分类不存在");
        }
        List<CategoryVO> categoryVOS;
        String categoryVOSStr=redisOperator.get("subcat");
        if (StringUtils.isBlank(categoryVOSStr)){
            categoryVOS=categoryService.getSubCatList(rootCatId);
            redisOperator.set("subcat",JsonUtils.objectToJson(categoryVOS));
        }else{
            categoryVOS=JsonUtils.jsonToList(categoryVOSStr, CategoryVO.class);
        }

        return IMOOCJSONResult.ok(categoryVOS);
    }

}
