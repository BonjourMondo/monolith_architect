package com.imooc.exception;

import com.imooc.utils.IMOOCJSONResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Author: leesanghyuk
 * Date: 2020-02-04 17:01
 * Description:
 */
@RestControllerAdvice
public class CustomExceptionHandler {
    //上传文件超过500k捕获异常：MaxUploadSizeExceededException
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public IMOOCJSONResult handleMaxUploadFile(MaxUploadSizeExceededException exceededException){
        return IMOOCJSONResult.errorMsg("文件上传大小不能超过500k，请压缩图片后重试");
    }
}
