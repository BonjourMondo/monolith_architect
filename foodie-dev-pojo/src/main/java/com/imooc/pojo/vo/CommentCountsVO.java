package com.imooc.pojo.vo;

/**
 * Author: leesanghyuk
 * Date: 2020-01-30 15:29
 * Description:用于展示商品评价数目的vo
 */
public class CommentCountsVO {
    private int totalCounts;
    private int goodCounts;
    private int badCounts;
    private int normalCounts;

    public int getTotalCounts() {
        return totalCounts;
    }

    public void setTotalCounts(int totalCounts) {
        this.totalCounts = totalCounts;
    }

    public int getGoodCounts() {
        return goodCounts;
    }

    public void setGoodCounts(int goodCounts) {
        this.goodCounts = goodCounts;
    }

    public int getBadCounts() {
        return badCounts;
    }

    public void setBadCounts(int badCounts) {
        this.badCounts = badCounts;
    }

    public int getNormalCounts() {
        return normalCounts;
    }

    public void setNormalCounts(int normalCounts) {
        this.normalCounts = normalCounts;
    }
}
