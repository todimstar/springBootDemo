package com.liu.springbootdemo.entity.VO;

// T 是泛型，表示data可以是任意类型
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

    // 成功但有错误信息，默认代码2，可以自定义代码，错误提示词，无数据返回
    public static Result successWarnning(String message){
        return new Result(2,message,null);
    }
    public static Result successWarnning(Integer code, String message){
        return new Result(code,message,null);
    }
    // 成功但有错误信息，带数据
    public static <E> Result<E> successWarnning(String message, E data){
        return new Result(2,message,data);
    }
    public static <E> Result<E> successWarnning(Integer code, String message, E data){
        return new Result(code,message,data);
    }

    // 失败信息返回
    public static Result error(String message){
        return new Result(1,message,null);
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }


    
}