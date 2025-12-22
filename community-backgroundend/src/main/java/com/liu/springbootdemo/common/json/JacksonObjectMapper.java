package com.liu.springbootdemo.common.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


/**
 * 自定义Jackson对象映射器
 *
 * ObjectMapper，将Java对象与JSON字符串相互转换的对象映射器
 * JacksonObjectMapper，基于jackson将Java对象与JSON字符串相互转换的对象映射器
 *
 * @author Liu
 * @since 2025.12.22
 */
@Component  //通用组件
public class JacksonObjectMapper extends ObjectMapper {

    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

    public JacksonObjectMapper() {
        super();
        //忽略传入的多余字段,配置反序列化中遇到未知属性抛异常为false
        this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        //创建一个简单模块对象，用于注册自定义的序列化器和反序列化器
        SimpleModule module = new SimpleModule()
                //序列化配置
                .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT)))
                .addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)))
                .addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT)))
                //反序列化配置    添加反序列化规则，如果遇到LocalDateTime类型，就使用指定的LocalDateTimeDeserializer，并制定Pattern格式为DEFAULT_DATE_TIME_FORMAT
                .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT)))
                .addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)))
                .addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT)));

        //注册这两个自定义序列化器
        this.registerModule(module);

    }






















}
