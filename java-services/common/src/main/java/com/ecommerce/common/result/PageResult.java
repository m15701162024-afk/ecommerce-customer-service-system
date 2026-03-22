package com.ecommerce.common.result;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 分页响应结果
 */
@Data
public class PageResult<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 数据列表
     */
    private List<T> list;
    
    /**
     * 总数
     */
    private Long total;
    
    /**
     * 当前页码
     */
    private Long pageNum;
    
    /**
     * 每页数量
     */
    private Long pageSize;
    
    /**
     * 总页数
     */
    private Long pages;
    
    public PageResult() {
    }
    
    public PageResult(List<T> list, Long total, Long pageNum, Long pageSize) {
        this.list = list;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.pages = (total + pageSize - 1) / pageSize;
    }
    
    public static <T> PageResult<T> of(List<T> list, Long total, Long pageNum, Long pageSize) {
        return new PageResult<>(list, total, pageNum, pageSize);
    }
}