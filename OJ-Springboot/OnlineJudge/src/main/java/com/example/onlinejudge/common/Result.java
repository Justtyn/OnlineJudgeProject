package com.example.onlinejudge.common;

/**
 * 统一响应结果类
 * 用于封装 API 接口的返回结果，包含状态码、消息和数据
 */
public class Result {
    /**
     * 状态码，表示请求处理的结果
     * 例如：200 表示成功，500 表示错误
     */
    private String code;
    
    /**
     * 消息，对处理结果的文字描述
     */
    private String msg;
    
    /**
     * 数据，请求返回的实际数据内容
     */
    private Object data;

    /**
     * 带数据的构造方法
     * @param data 响应数据
     */
    private Result(Object data) {
        this.data = data;
    }

    /**
     * 无参构造方法
     */
    public Result() {
    }

    /**
     * 创建成功响应（无数据）
     * @return 包含成功状态码和消息的 Result 对象
     */
    public static Result success() {
        Result result = new Result();
        result.setCode("200");
        result.setMsg("请求成功");
        return result;
    }

    /**
     * 创建带数据的成功响应
     * @param data 需要返回的数据
     * @return 包含成功状态码、消息和数据的 Result 对象
     */
    public static Result success(Object data) {
        Result result = success();
        result.setData(data);
        return result;
    }

    /**
     * 创建错误响应（使用默认错误消息）
     * @return 包含错误状态码和默认错误消息的 Result 对象
     */
    public static Result error() {
        Result result = new Result();
        result.setCode("500");
        result.setMsg("请求失败");
        return result;
    }

    /**
     * 创建带自定义错误消息的错误响应
     * @param msg 自定义错误消息
     * @return 包含错误状态码和自定义错误消息的 Result 对象
     */
    public static Result error(String msg) {
        Result result = new Result();
        result.setCode("500");
        result.setMsg(msg);
        return result;
    }

    /**
     * 获取状态码
     * @return 状态码
     */
    public String getCode() {
        return code;
    }

    /**
     * 设置状态码
     * @param code 状态码
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * 获取消息
     * @return 消息内容
     */
    public String getMsg() {
        return msg;
    }

    /**
     * 设置消息
     * @param msg 消息内容
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }

    /**
     * 获取数据
     * @return 响应数据
     */
    public Object getData() {
        return data;
    }

    /**
     * 设置数据
     * @param data 响应数据
     */
    public void setData(Object data) {
        this.data = data;
    }
}
