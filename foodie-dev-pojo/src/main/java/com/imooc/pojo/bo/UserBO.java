package com.imooc.pojo.bo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Author: leesanghyuk
 * Date: 2020-01-27 15:27
 * Description:
 */
@ApiModel(value = "用户对象BO",description = "从客户端由用户传入的数据封装在此Entity")
public class UserBO {
    @ApiModelProperty(value = "用户名",name = "用户名",example = "imooc",required = true)
    private String username;
    @ApiModelProperty(value = "密码",name = "密码",example = "123456",required = true)
    private String password;
    @ApiModelProperty(value = "确认密码",name = "确认密码",example = "123456",required = false)
    private String confirmPassword;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
