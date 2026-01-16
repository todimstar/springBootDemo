package com.liu.springbootdemo.service;

import com.liu.springbootdemo.common.enums.ErrorCode;
import com.liu.springbootdemo.common.exception.BusinessException;
import com.liu.springbootdemo.config.MinioConfig;
import io.minio.*;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class MinioService {
    @Autowired
    private MinioClient minioClient;
    @Autowired
    private MinioConfig minioConfig;

    // 允许的图片类型列表
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
        "image/jpeg",   
        "image/png",
        "image/gif",
        "image/webp"
    );

    // 最大文件大小：5MB
    private static final long MAX_FILE_SIZE = 5*1024*1024;

    /**
     * 初始化：检查存储桶是否存在，如果不存在则创建并设置为公开读
     * @PostConstruct: 依赖注入完成后自动执行
     */
    @PostConstruct
    public void init() {
        try {
            // 1.检查Bucket是否存在
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(minioConfig.getBucket()).build()
            );

            if (!exists) {
                // 不存在则创建
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(minioConfig.getBucket()).build()
                );

                // 2. 设置为公开读权限 (Public Read)
                // 允许任何人(Principal: *)执行GetObject操作(Action: s3:GetObject)
                String policy = """
                    {
                        "Version": "2012-10-17",
                        "Statement": [
                            {
                                "Effect": "Allow",
                                "Principal": {"AWS": ["*"]},
                                "Action": ["s3:GetObject"],
                                "Resource": ["arn:aws:s3:::%s/*"]
                            }
                        ]
                    }
                    """.formatted(minioConfig.getBucket());

                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder()
                                .bucket(minioConfig.getBucket())
                                .config(policy)
                                .build()
                );
                log.debug("MinIO存储桶已创建并设置为公开读: {}", minioConfig.getBucket());
            }
        } catch (Exception e) {
            log.error("MinIO初始化失败: {}", e.getMessage());
        }
    }

    /**
     * 上传文件到MinIO服务器-头像版 //TODO:之后可能有不同的文件类型限制，有枚举类TYPE对应不同的校验列表
     * @param file 要上传的文件(Spring MultipartFile)
     * @return 文件在MinIO中的唯一存储名称
     */
    public String uploadFile(MultipartFile file) throws Exception{
        // 1.文件校验
        //文件为空检查
        if(file == null || file.isEmpty()){
            throw new BusinessException(ErrorCode.INPUT_INVALID, "请选择要上传的文件");
        }//文件大小检查
        if(file.getSize() > MAX_FILE_SIZE){
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE,"文件大小不能超过"+MAX_FILE_SIZE/1024/1024+"MB");
        }
        // 文件类型校验
        String contentType = file.getContentType();
        if(contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)){
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED);
        }

        //2.生成唯一文件名，避免重名
        String originalFilename = file.getOriginalFilename();
        String fileExtension = null;
        try {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }catch(Exception e){
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED);//可能是文件名为空，丢失后缀的情况
        }
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String objectName = datePath + "/" + UUID.randomUUID().toString() + fileExtension;   //手动加前缀路径以便区分，防止大量文件平铺在一层

        //3.上传文件到MinIO
        minioClient.putObject(
            PutObjectArgs.builder()
                    .bucket(minioConfig.getBucket())    //指定桶名称
                    .object(objectName)                     //文件名
                    .stream(file.getInputStream(),file.getSize(),-1)//文件输入流交给Minio以读取，文件大小，每次读取上传大小，-1表示自动
                    .contentType(file.getContentType())
                    .build()
        );

        //4.返回唯一标识名
        return objectName;
    }


    public String getFileUrl(String objectName) {
        // 假设 endpoint 是 http://localhost:9000
        // 目标: http://localhost:9000/forum-images/2026/01/13/xxx.jpg
        return minioConfig.getEndpoint() + "/" + minioConfig.getBucket() + "/" + objectName;
    }

    /**
     * 用文件名查询临时文件url
     * @param objectName 文件名
     * @param expiry 有效期(分钟)
     * @return url
     */
    public String getFileUrlWithTime(String objectName,int expiry) throws Exception{
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(minioConfig.getBucket())
                        .object(objectName)
                        .expiry(expiry, TimeUnit.MINUTES)
                        .build()
        );
    }

    /**
     * 删除文件
     * @param objectName 指定文件唯一名
     * @throws Exception
     */
    public void deleteFile(String objectName) throws Exception{
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(minioConfig.getBucket())
                        .object(objectName)
                        .build()
        );
    }

}
