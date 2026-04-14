package com.ecommerce.platform.xiaohongshu.dto;

import lombok.Data;
import java.util.List;

/**
 * 小红书API通用响应
 */
@Data
public class ApiResponse<T> {
    /**
     * 错误码，0表示成功
     */
    private Integer code;
    
    /**
     * 错误信息
     */
    private String msg;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 请求ID
     */
    private String requestId;
    
    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return code != null && code == 0;
    }
    
    /**
     * 创建成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(0);
        response.setMsg("success");
        response.setData(data);
        return response;
    }
    
    /**
     * 创建失败响应
     */
    public static <T> ApiResponse<T> error(Integer code, String msg) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(code);
        response.setMsg(msg);
        return response;
    }
}