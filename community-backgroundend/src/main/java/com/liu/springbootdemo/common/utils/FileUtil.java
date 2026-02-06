package com.liu.springbootdemo.common.utils;

import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文件工具类
 * 提供文件 URL 解析、路径生成等方法
 */
public class FileUtil {
    /**
     * 从完整URL中提取ObjectName
     *
     * 用于需要删除旧资源时从数据库保存的URL里提取ObjectName
     *
     * 如：输入: http://localhost:9000/forum-images/avatars/2026/01/17/uuid.jpg
     *    输出: avatars/2026/01/17/uuid.jpg
     * @param fileUrl 完整文件访问url
     * @param bucketName 桶名称
     * @return ObjectName,解析失败为null
     */
    public static String extractObjectName(String fileUrl,String bucketName){
        if(!StringUtils.hasText(fileUrl) || !StringUtils.hasText(bucketName)){
            return null;
        }

        //期待格式: http://host:port/bucket/objectName
        String searchKey = "/" + bucketName + "/";
        int index = fileUrl.indexOf(searchKey);//裁到http://host:port
        if(index != -1){
            System.out.println("走的substring解析url");
            return fileUrl.substring(index + searchKey.length());//再多把/bucket/裁掉
        }

        //备选：正则匹配 /bucketName/ 之后的所有内容
        Pattern pattern = Pattern.compile("/" + Pattern.quote(bucketName) + "/(.+)");
        Matcher matcher = pattern.matcher(fileUrl);
        if(matcher.find()){
            System.out.println("走的正则解析url");
            return matcher.group(1);
        }
        return null;
    }

}
