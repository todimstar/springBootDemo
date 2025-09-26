package com.liu.springbootdemo.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liu.springbootdemo.entity.VO.Result;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class ResponseUtil {
    private static final ObjectMapper mapper = new ObjectMapper();  //jackson的ObjectMapper，java的json库
                                                                    //将java对象转为json字符串,
                                                                    //用mapper.writeValueAsString(JavaObject)
                                                                    //将json转为java对象,
                                                                    //mapper.readValue(jsonString,JavaObject.class)
                                                                    //需要转为java集合的,
                                                                    //要new TypeReference<集合对象>() {}来充当JavaObject.class
    public static void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");

        Result<Object> errorResult = Result.error(message);
        //序列化写回响应体
        response.getWriter().write(mapper.writeValueAsString(errorResult));
    }

}
