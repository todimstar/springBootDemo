package com.liu.springbootdemo.service;

import com.liu.springbootdemo.common.enums.ErrorCode;
import com.liu.springbootdemo.common.exception.BusinessException;
import com.liu.springbootdemo.config.MinioConfig;
import io.minio.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class MinioService {
    @Autowired
    private MinioClient minioClient;
    @Autowired
    private MinioConfig minioConfig;

    /**
     * 上传文件到MinIO服务器
     * @param file 要上传的文件(Spring MultipartFile)
     * @return 文件在MinIO中的唯一存储名称
     */
    public String uploadFile(MultipartFile file) throws Exception{
        //1.检查Bucket是否存在，不存在则创建
        if(!minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioConfig.getBucket()).build())){
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioConfig.getBucket()).build());
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

    /**
     * 用文件名查询临时文件url
     * @param objectName 文件名
     * @param expiry 有效期(分钟)
     * @return url
     */
    public String getFileUrl(String objectName,int expiry) throws Exception{
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
