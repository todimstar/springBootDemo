package com.liu.springbootdemo.POJO.vo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// T 是泛型，表示data可以是任意类型
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private Integer code;   //状态码，0-成功，1-失败
    private String message; //提示信息
    private T data; //数据

    // 静态方法
    // 带成功数据返回
    public static <E> Result<E> success(E data){
        return new Result<>(0,"操作成功",data);
    }
    // 简要成功信息无数据返回
    public static Result success() {
        return new Result(0,"操作成功",null);
    }
    //自定义成功信息无数据返回
    public static Result success(String message) {
        return new Result(0,message,null);
    }
    //自定义成功信息带数据返回
    public static <E> Result<E> success(String message, E data){ return new Result<>(0,message,data);}

    // 失败信息返回
    public static Result error(String message){
        return new Result(1,message,null);
    }
    //失败自定义状态码返回
    public static <E> Result<E> error(Integer code,String message){
        return new Result<>(code,message,null);
    }
    // ErrorResponse类型失败返回
    public static Result error(ErrorResponse error){
        return new Result(1,error.getMessage(),error);
    }



}