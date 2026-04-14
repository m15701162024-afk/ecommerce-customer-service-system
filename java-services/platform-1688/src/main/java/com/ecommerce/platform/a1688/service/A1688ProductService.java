package com.ecommerce.platform.a1688.service;

import com.ecommerce.platform.a1688.client.A1688ApiClient;
import com.ecommerce.platform.a1688.dto.request.ProductSearchRequest;
import com.ecommerce.platform.a1688.dto.response.ProductDetailResponse;
import com.ecommerce.platform.a1688.dto.response.ProductSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 1688商品服务
 * 
 * 功能：
 * - 商品搜索
 * - 商品详情查询
 * - 商品收藏
 * - 商品缓存
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class A1688ProductService {
    
    private final A1688ApiClient apiClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    private static final String PRODUCT_CACHE = "1688-products";
    private static final String PRODUCT_DETAIL_CACHE = "1688-product-detail";
    private static final String PRODUCT_TOPIC = "ecommerce-product-events";
    
    // ==================== 商品搜索 ====================
    
    /**
     * 搜索商品
     * 
     * @param shopId 店铺ID
     * @param keyword 搜索关键词
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @return 搜索结果
     */
    public ProductSearchResponse searchProducts(Long shopId, String keyword, int pageNo, int pageSize) {
        ProductSearchRequest request = ProductSearchRequest.builder()
                .keyword(keyword)
                .pageNo(pageNo)
                .pageSize(pageSize)
                .build();
        
        return searchProducts(shopId, request);
    }
    
    /**
     * 搜索商品（高级搜索）
     * 
     * @param shopId 店铺ID
     * @param request 搜索请求
     * @return 搜索结果
     */
    public ProductSearchResponse searchProducts(Long shopId, ProductSearchRequest request) {
        log.info("搜索1688商品: shopId={}, keyword={}, page={}", 
                shopId, request.getKeyword(), request.getPageNo());
        
        ProductSearchResponse response = apiClient.searchProducts(shopId, request);
        
        if (response != null && response.getProducts() != null) {
            // 发布商品搜索事件
            ProductSearchEvent event = ProductSearchEvent.builder()
                    .platform("A1688")
                    .shopId(shopId)
                    .keyword(request.getKeyword())
                    .resultCount(response.getTotalCount())
                    .pageNo(request.getPageNo())
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            kafkaTemplate.send(PRODUCT_TOPIC, "search", event);
            
            log.info("商品搜索完成: keyword={}, total={}", 
                    request.getKeyword(), response.getTotalCount());
        }
        
        return response;
    }
    
    /**
     * 按类目搜索商品
     * 
     * @param shopId 店铺ID
     * @param categoryId 类目ID
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @return 搜索结果
     */
    public ProductSearchResponse searchByCategory(Long shopId, String categoryId, int pageNo, int pageSize) {
        ProductSearchRequest request = ProductSearchRequest.builder()
                .keyword("")
                .categoryId(categoryId)
                .pageNo(pageNo)
                .pageSize(pageSize)
                .build();
        
        return searchProducts(shopId, request);
    }
    
    /**
     * 按价格区间搜索商品
     * 
     * @param shopId 店铺ID
     * @param keyword 搜索关键词
     * @param minPrice 最低价格（分）
     * @param maxPrice 最高价格（分）
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @return 搜索结果
     */
    public ProductSearchResponse searchByPriceRange(Long shopId, String keyword, 
            Long minPrice, Long maxPrice, int pageNo, int pageSize) {
        ProductSearchRequest request = ProductSearchRequest.builder()
                .keyword(keyword)
                .startPrice(minPrice)
                .endPrice(maxPrice)
                .sortType("price_asc")
                .pageNo(pageNo)
                .pageSize(pageSize)
                .build();
        
        return searchProducts(shopId, request);
    }
    
    /**
     * 搜索热销商品
     * 
     * @param shopId 店铺ID
     * @param keyword 搜索关键词
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @return 搜索结果
     */
    public ProductSearchResponse searchHotProducts(Long shopId, String keyword, int pageNo, int pageSize) {
        ProductSearchRequest request = ProductSearchRequest.builder()
                .keyword(keyword)
                .sortType("sales_desc")
                .pageNo(pageNo)
                .pageSize(pageSize)
                .build();
        
        return searchProducts(shopId, request);
    }
    
    /**
     * 搜索实力商家商品
     * 
     * @param shopId 店铺ID
     * @param keyword 搜索关键词
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @return 搜索结果
     */
    public ProductSearchResponse searchPowerSellerProducts(Long shopId, String keyword, 
            int pageNo, int pageSize) {
        ProductSearchRequest request = ProductSearchRequest.builder()
                .keyword(keyword)
                .onlyPowerSeller(true)
                .sortType("credit_desc")
                .pageNo(pageNo)
                .pageSize(pageSize)
                .build();
        
        return searchProducts(shopId, request);
    }
    
    // ==================== 商品详情 ====================
    
    /**
     * 获取商品详情（带缓存）
     * 
     * @param shopId 店铺ID
     * @param productId 商品ID
     * @return 商品详情
     */
    @Cacheable(value = PRODUCT_DETAIL_CACHE, key = "#productId", unless = "#result == null")
    public ProductDetailResponse getProductDetail(Long shopId, String productId) {
        log.info("获取1688商品详情: shopId={}, productId={}", shopId, productId);
        
        ProductDetailResponse response = apiClient.getProductDetail(shopId, productId);
        
        if (response != null) {
            // 发布商品浏览事件
            ProductViewEvent event = ProductViewEvent.builder()
                    .platform("A1688")
                    .shopId(shopId)
                    .productId(productId)
                    .productName(response.getSubject())
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            kafkaTemplate.send(PRODUCT_TOPIC, productId, event);
            
            log.info("商品详情获取成功: productId={}, name={}", productId, response.getSubject());
        }
        
        return response;
    }
    
    /**
     * 批量获取商品详情
     * 
     * @param shopId 店铺ID
     * @param productIds 商品ID列表
     * @return 商品详情列表
     */
    public List<ProductDetailResponse> batchGetProductDetails(Long shopId, List<String> productIds) {
        log.info("批量获取商品详情: shopId={}, count={}", shopId, productIds.size());
        
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        return productIds.parallelStream()
                .map(productId -> {
                    try {
                        return getProductDetail(shopId, productId);
                    } catch (Exception e) {
                        log.error("获取商品详情失败: productId={}", productId, e);
                        return null;
                    }
                })
                .filter(response -> response != null)
                .collect(Collectors.toList());
    }
    
    /**
     * 刷新商品详情缓存
     * 
     * @param productId 商品ID
     */
    @CacheEvict(value = PRODUCT_DETAIL_CACHE, key = "#productId")
    public void refreshProductDetailCache(String productId) {
        log.info("刷新商品详情缓存: productId={}", productId);
    }
    
    // ==================== 商品推荐 ====================
    
    /**
     * 获取相似商品
     * 
     * @param shopId 店铺ID
     * @param productId 商品ID
     * @param size 数量
     * @return 相似商品列表
     */
    public ProductSearchResponse getSimilarProducts(Long shopId, String productId, int size) {
        // 先获取商品详情
        ProductDetailResponse detail = getProductDetail(shopId, productId);
        
        if (detail == null) {
            return ProductSearchResponse.builder()
                    .products(Collections.emptyList())
                    .totalCount(0)
                    .build();
        }
        
        // 使用商品标题的关键词搜索相似商品
        String keyword = extractKeyword(detail.getSubject());
        
        ProductSearchRequest request = ProductSearchRequest.builder()
                .keyword(keyword)
                .categoryId(detail.getCategoryInfo() != null ? 
                        detail.getCategoryInfo().getCategoryId() : null)
                .pageNo(1)
                .pageSize(size)
                .build();
        
        ProductSearchResponse response = searchProducts(shopId, request);
        
        // 过滤掉当前商品
        if (response != null && response.getProducts() != null) {
            List<ProductSearchResponse.ProductItem> filteredProducts = response.getProducts().stream()
                    .filter(p -> !p.getProductId().equals(productId))
                    .collect(Collectors.toList());
            
            response.setProducts(filteredProducts);
            response.setTotalCount(filteredProducts.size());
        }
        
        return response;
    }
    
    /**
     * 提取关键词
     */
    private String extractKeyword(String subject) {
        if (subject == null || subject.isEmpty()) {
            return "";
        }
        
        // 简单的关键词提取：取前10个字符
        int length = Math.min(subject.length(), 10);
        return subject.substring(0, length);
    }
    
    // ==================== 事件定义 ====================
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ProductSearchEvent {
        private String platform;
        private Long shopId;
        private String keyword;
        private Integer resultCount;
        private Integer pageNo;
        private Long timestamp;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ProductViewEvent {
        private String platform;
        private Long shopId;
        private String productId;
        private String productName;
        private Long timestamp;
    }
}