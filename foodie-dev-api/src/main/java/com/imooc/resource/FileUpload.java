package com.imooc.resource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * Author: leesanghyuk
 * Date: 2020-02-04 16:15
 * Description:资源配置类
 */
@Component
@ConfigurationProperties(prefix = "file")
@PropertySource("classpath:file-upload-dev.properties")
public class FileUpload {
    private String imageUserFaceLocation;
    private String imageServerURL;

    public String getImageServerURL() {
        return imageServerURL;
    }

    public void setImageServerURL(String imageServerURL) {
        this.imageServerURL = imageServerURL;
    }

    public String getImageUserFaceLocation() {
        return imageUserFaceLocation;
    }

    public void setImageUserFaceLocation(String imageUserFaceLocation) {
        this.imageUserFaceLocation = imageUserFaceLocation;
    }
}
