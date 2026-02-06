package com.liu.springbootdemo;

import com.liu.springbootdemo.common.enums.FileType;
import com.liu.springbootdemo.common.utils.ImageCompressUtil;
import net.coobird.thumbnailator.Thumbnails;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;

public class CompressTest {
    @Test
    void testCompressAvatar() throws Exception{
        File origin = new File("C:\\Users\\hp1080\\Pictures\\image(1).png");
        MultipartFile mf = new MockMultipartFile(
                "file","sample-test.png", "image/png", new FileInputStream(origin)
        );
        MultipartFile compressed = ImageCompressUtil.compressIfNeeded(mf, FileType.COMMENT_IMAGE);

        System.out.println("origin size = "+origin.length()+" 字节");
        System.out.println("compressed size = "+compressed.getSize()+" 字节");
        try(InputStream in = compressed.getInputStream()){
            BufferedImage img = ImageIO.read(in);
            System.out.println("w=" + img.getWidth() + ", h=" + img.getHeight());
        }
    }

    /**
     * 首次使用Buffer作为缓冲区读取文件流
     * InputStream -> byte[] ->outputStream
     * @throws IOException
     */
    @Test
    void testBufferRedisRead() throws IOException {
        File origin = new File("C:\\Users\\hp1080\\Pictures\\image(1).png");
        MultipartFile file = new MockMultipartFile(
                origin.getName(), new FileInputStream(origin)
        );
        try(InputStream in = file.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream()){

            byte[] buffer = new byte[8192]; //也叫缓冲区，用于8KB缓冲读取
            int len;
            while((len = in.read(buffer)) != -1){//用Buffer实现滑动窗口读取InputStream
                out.write(buffer, 0, len);
            }
            byte[] data = out.toByteArray();    //最终字节数组
            System.out.println("读取到字节数："+data.length);

        }
    }

    /**
     * 读取图像边长
     */
    @Test
    public void testReadImageSize() throws IOException {
        File origin = new File("C:\\Users\\hp1080\\Pictures\\image(1).png");
        MultipartFile file = new MockMultipartFile(
                origin.getName(), new FileInputStream(origin)
        );
        BufferedImage image;
        try(InputStream in = file.getInputStream()){
            //ImageIO.read的各种参数读取
            //InputStream
            image = ImageIO.read(in);
            System.out.println("InputStream w=" + image.getWidth() + ", h=" + image.getHeight());
            //File
            image = ImageIO.read(origin);
            System.out.println("File w=" + image.getWidth() + ", h=" + image.getHeight());
            //new URL
            image = ImageIO.read(new URL("http://localhost:9000/forum-images/bae1fa29-228d-4603-96db-0fa082dfface.jpg"));
            System.out.println("URL w=" + image.getWidth() + ", h=" + image.getHeight());
        }
    }

    /**
     * 测试Thumbnailator的压缩
     */
    @Test
    public void testThumbnailator() throws IOException {
        BufferedImage image= ImageIO.read(new URL("http://localhost:9000/forum-images/bae1fa29-228d-4603-96db-0fa082dfface.jpg"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

//        System.out.println("原始宽" + image.getWidth() + ", 高" + image.getHeight());

        Thumbnails.of(image)
                .size(512, 512)          // 按比例缩放，最长边 512
                .outputQuality(0.8)      // 质量因子 0~1
                .outputFormat("jpg")     // 输出格式
                .toOutputStream(baos);

        byte[] compressedBytes = baos.toByteArray();
        System.out.println("压缩后字节数：" + compressedBytes.length);
//        System.out.println("压缩后宽高读取测试：");
//        try(InputStream in = new ByteArrayInputStream(compressedBytes)){
//            BufferedImage compressedImage = ImageIO.read(in);
//            System.out.println("w=" + compressedImage.getWidth() + ", h=" + compressedImage.getHeight());
//        }
    }
}
