package com.liu.springbootdemo.controller;

import com.liu.springbootdemo.POJO.Result.Result;
import com.liu.springbootdemo.common.enums.ErrorCode;
import com.liu.springbootdemo.common.enums.FileType;
import com.liu.springbootdemo.common.exception.BusinessException;
import com.liu.springbootdemo.service.MinioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/upload")
public class UploadFileController {
    @Autowired
    private MinioService minioService;

    //上传的其他类型文件
    @PostMapping("/upload")
    public Result<String> uploadFile(@RequestParam("file")MultipartFile file){
        try {
            String objectName = minioService.uploadFile(file, FileType.OTHER);
            return Result.success("上传成功", objectName);
        }catch (BusinessException e){
            throw e;
        }catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR);
        }
    }

    @GetMapping("/getUrl")
    public Result<String> getFileUrl(@RequestParam String objectName){
        try {
            String url = minioService.getFileUrl(objectName);
            return Result.success("获取成功", url);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_URL_GET_BAD);
        }
    }

    @DeleteMapping("/delete")
    public Result<String> deleteFile(@RequestParam String objectName){
        try {
            minioService.deleteFile(objectName);
            return Result.success("删除成功", objectName);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_DEL_FAIL);
        }
    }
}
