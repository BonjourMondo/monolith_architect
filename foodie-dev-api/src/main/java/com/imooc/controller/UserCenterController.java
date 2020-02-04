package com.imooc.controller;

import com.imooc.pojo.bo.UserInfoBO;
import com.imooc.resource.FileUpload;
import com.imooc.utils.DateUtil;
import com.imooc.utils.IMOOCJSONResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: leesanghyuk
 * Date: 2020-02-04 15:35
 * Description:
 */
@Api(value = "用户中心", tags = {"用户中心的相关接口"})
@RestController
@RequestMapping("usercenter")
public class UserCenterController{
    @Autowired
    private FileUpload fileUpload;

    @ApiOperation(value = "文件上传", notes = "文件上传", httpMethod = "POST")
    @PostMapping("/uploadfile")
    public IMOOCJSONResult uploadFile(@ApiParam(name = "userId", value = "用户id", required = true)
                                      @RequestParam String userId,
                                      @ApiParam(name = "file", value = "用户头像", required = true)
                                              MultipartFile file,
                                      HttpServletRequest request, HttpServletResponse response) {
        //定义文件保存位置
        String fileSpace = fileUpload.getImageUserFaceLocation();
        String uploadPrefixPath = File.separator + userId;

        //开始文件上传
        if (file != null) {
            FileOutputStream fileOutputStream=null;
            try {
                String fileName = file.getOriginalFilename();
                if (StringUtils.isNotBlank(fileName)) {
                    //文件重命名，如imooc.png->"imooc"和"png"
                    String[] fileNameArr = fileName.split("\\.");
                    //获取文件后缀名
                    String suffix = fileNameArr[fileNameArr.length - 1];

                    if(!suffix.equalsIgnoreCase("png")&&
                            !suffix.equalsIgnoreCase("jpg")&&
                            !suffix.equalsIgnoreCase("jpeg")
                    ){
                        //防止出现黑客上传.sh 之类的文件
                        return IMOOCJSONResult.errorMsg("图片格式不正确");
                        //另外，可能又恶意上传大文件的行为，我们在application.yml中进行文件大小限制
                    }
                    //文件名称重组 覆盖式上传 增量式可以加一个Date
                    String newFileName = "face-" + userId + "." + suffix;
                    //上传的文件最终保存的位置
                    String finalFacePath = fileSpace + uploadPrefixPath + File.separator + newFileName;

                    //用于提供给浏览器访问(保存到数据库)的地址
                    //由于浏览器访问路径是统一的"/"，所以不需要File.separator
                    uploadPrefixPath+=("/"+newFileName);

                    File outFile = new File(finalFacePath);
                    if (outFile.getParentFile() != null) {
                        //创建文件夹(创建多级父目录)
                        outFile.getParentFile().mkdirs();
                    }
                    //文件输出到目录
                    fileOutputStream = new FileOutputStream(outFile);
                    InputStream inputStream = file.getInputStream();
                    IOUtils.copy(inputStream, fileOutputStream);
                }
            } catch (Exception e) {
                    e.printStackTrace();
            }finally {
                if (fileOutputStream!=null) {
                    try {
                        fileOutputStream.flush();
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            return IMOOCJSONResult.errorMsg("文件不能为空");
        }
        //获得浏览器访问的图片的地址前缀
        String imageServerURL=fileUpload.getImageServerURL();
        //由于前段存在缓存，所以还需要加入时间戳以及时更新头像
        //在URL中加时间戳就会保证每一次发起的请求都是一个不同于之前的请求,这样就能避免浏览器对URL的缓存。
        String finalFaceServerURL=imageServerURL+uploadPrefixPath
                +"?t="+ DateUtil.getCurrentDateString(DateUtil.DATE_PATTERN);
        //更新到数据库
        //Users user=centerUserService.updateFaceURL(userId,finalFaceServerURL);
        //更新缓存
        //Cookies.setCookie(request,response,"user",JsonUtils.objectToJson(user),true);


        return IMOOCJSONResult.ok(finalFaceServerURL);
    }


    @ApiOperation(value = "信息修改", notes = "用户信息修改", httpMethod = "POST")
    @PostMapping("/userinfo")
    public IMOOCJSONResult userInfo(@Valid @RequestBody UserInfoBO userInfoBO,
                                    BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> resultMap = getErrors(result);
            return IMOOCJSONResult.errorMap(resultMap);
        } else {
            //继续其他业务
        }
        return IMOOCJSONResult.ok();
    }

    private Map<String, String> getErrors(BindingResult result) {
        Map<String, String> map = new HashMap<>();
        List<FieldError> list = result.getFieldErrors();
        for (FieldError error : list) {
            System.out.println("error.getField():" + error.getField());
            System.out.println("error.getDefaultMessage():" + error.getDefaultMessage());

            map.put(error.getField(), error.getDefaultMessage());
        }
        return map;
    }

}
