package com.liu.springbootdemo.common.utils;

import com.liu.springbootdemo.common.enums.FileType;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

public class ImageCompressUtil {
    // 白名单 http传来的contentType 与 扩展名
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );
    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "webp", "gif");

    //压缩规则类
    private record CompressRule(int maxSide, float quality) {}

    //压缩规则
    private static final Map<FileType,CompressRule> RULES = Map.of(
            FileType.AVATAR, new CompressRule(512, 0.80f),
            FileType.POST_COVER, new CompressRule(1280, 0.82f),
            FileType.POST_IMAGE, new CompressRule(1600, 0.85f),
            FileType.COMMENT_IMAGE, new CompressRule(1600, 0.85f)
            // 其他类型默认不压
    );

    /**
     * 压缩文件
     * 智能根据图片格式、大小等各种规则压缩
     * MultipartFile -> BufferedImage -> Multi...
     * @return Multi...
     */
    public static MultipartFile compressIfNeeded(MultipartFile file, FileType fileType) throws IOException {
        // 1.校验
        // 2.根据大小决定策略
        // 3.封装回原格式返回
        String contentType = StringUtils.defaultIfBlank(file.getContentType(),"").toLowerCase();
        if(!ALLOWED_CONTENT_TYPES.contains(contentType)){
            //非允许图片类型直接返回在调用处或上传处会抛异常
            return file;
        }

        //拓展名不对也不行,是gif不用压缩
        String ext = getExtension(file.getOriginalFilename());
        if(!ALLOWED_EXT.contains(ext) || "gif".equals(ext)){
            return file;
        }

        //看文件类型找对应压缩规则
        CompressRule rule = RULES.get(fileType);
        if(rule == null){
            return file;
        }

        //正式开始压缩
        BufferedImage image;
        try(InputStream in = file.getInputStream()){//try-with块结束后自动关闭流
            image = ImageIO.read(in);               //解析成 BufferedImage 内存像素矩阵
        }
        if(image == null){return file;}// 读取失败，咋进来的？给调用方检查去

        int maxSize = Math.max(image.getHeight(),image.getWidth());

        //如果够小就不压缩了
        if(maxSize <= rule.maxSide){return file;}

        //依照规则压缩,先缩最长边，再调质量，保持原格式
        ByteArrayOutputStream baos = new ByteArrayOutputStream();//装结果的最后好mock返回
        Thumbnails.of(image)
                .size(rule.maxSide, rule.maxSide)  //Thumbnailator会按比例缩放并保证最长边为maxSide;
                .outputQuality(rule.quality)   //设置一下压缩质量系数
                .outputFormat(ext)             //保持原格式
                .toOutputStream(baos);

        //包成自建的MultipartFile子类回给上层
        return new InMemoryMultipartFile(
                file.getName(), //form字段名？啥玩意
                file.getOriginalFilename(), //原始名
                contentType,
                baos.toByteArray()  //数据转字节流
        );
    }

    private static String getExtension(String filename) {
        if (StringUtils.isBlank(filename) || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

}
