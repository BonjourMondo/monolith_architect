package com.imooc.enums;

/**
 * Author: leesanghyuk
 * Date: 2020-01-30 15:39
 * Description:
 */
public enum ItemLevel {
    GOOD(1,"GOOD"),
    BAD(3,"BAD"),
    NOMAL(2,"NORMAL");

    public final Integer type;
    public final  String level;

    ItemLevel(Integer type, String level) {
        this.type = type;
        this.level = level;
    }
}
