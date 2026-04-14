package com.ecommerce.product.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ecommerce.product.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 货源匹配服务
 * 对接1688平台进行货源匹配
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SourceMatchService {

    @Value("${source.1688.api-url:https://api.1688.com}")
    private String api1688Url;

    @Value("${source.1688.app-key:}")
    private String appKey;

    @Value("${source.1688.app-secret:}")
    private String appSecret;

    @Value("${source.1688.enabled:false}")
    private Boolean enabled1688;

    /**
     * 匹配1688货源
     *
     * @param keyword 搜索关键词
     * @param page    页码
     * @param size    每页大小
     * @return 匹配结果列表
     */
    public List<Map<String, Object>> matchSourceFrom1688(String keyword, Integer page, Integer size) {
        if (!enabled1688) {
            log.warn("1688货源匹配功能未启用");
            return new ArrayList<>();
        }

        if (StrUtil.isBlank(keyword)) {
            throw new IllegalArgumentException("搜索关键词不能为空");
        }

        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1 || size > 100) {
            size = 20;
        }

        try {
            // 构建请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("keyword", keyword);
            params.put("pageNo", page);
            params.put("pageSize", size);
            params.put("sortType", "default"); // 默认排序

            // 调用1688 API
            String response = call1688Api("alibaba.product.search", params);
            JSONObject result = JSONUtil.parseObj(response);

            // 解析结果
            List<Map<String, Object>> products = new ArrayList<>();
            if (result.containsKey("result") && result.get("result") instanceof JSONArray) {
                JSONArray items = result.getJSONArray("result");
                for (int i = 0; i < items.size(); i++) {
                    JSONObject item = items.getJSONObject(i);
                    Map<String, Object> product = new HashMap<>();
                    product.put("sourceId", item.getStr("productId"));
                    product.put("name", item.getStr("subject"));
                    product.put("price", parsePrice(item.getStr("price")));
                    product.put("minOrder", item.getInt("minOrderQuantity", 1));
                    product.put("imageUrl", item.getStr("imageUri"));
                    product.put("detailUrl", item.getStr("detailUrl"));
                    product.put("supplierName", item.getStr("companyName"));
                    product.put("supplierId", item.getStr("companyId"));
                    product.put("location", item.getStr("sendGoodsAddress"));
                    product.put("matchScore", calculateMatchScore(keyword, item.getStr("subject")));
                    products.add(product);
                }
            }

            log.info("1688货源匹配完成: keyword={}, count={}", keyword, products.size());
            return products;
        } catch (Exception e) {
            log.error("1688货源匹配失败: keyword={}", keyword, e);
            throw new RuntimeException("货源匹配失败: " + e.getMessage());
        }
    }

    /**
     * 根据商品匹配货源
     *
     * @param productId 商品ID
     * @return 匹配结果
     */
    public Map<String, Object> matchSourceForProduct(Long productId) {
        // 这里应该调用ProductService获取商品信息，然后进行匹配
        // 简化实现，直接返回模拟数据
        Map<String, Object> result = new HashMap<>();
        result.put("productId", productId);
        result.put("matches", new ArrayList<>());
        result.put("message", "请先配置1688 API");
        return result;
    }

    /**
     * 将1688商品导入到商品库
     *
     * @param sourceId     1688商品ID
     * @param productService 商品服务
     * @return 导入后的商品
     */
    public Product importFrom1688(String sourceId, ProductService productService) {
        if (!enabled1688) {
            throw new RuntimeException("1688货源匹配功能未启用");
        }

        if (StrUtil.isBlank(sourceId)) {
            throw new IllegalArgumentException("商品ID不能为空");
        }

        try {
            // 获取1688商品详情
            Map<String, Object> params = new HashMap<>();
            params.put("productId", sourceId);

            String response = call1688Api("alibaba.product.get", params);
            JSONObject result = JSONUtil.parseObj(response);

            if (!result.containsKey("product")) {
                throw new RuntimeException("获取商品详情失败");
            }

            JSONObject productData = result.getJSONObject("product");

            // 转换为商品实体
            Product product = new Product();
            product.setName(productData.getStr("subject"));
            product.setProductCode("1688-" + sourceId);
            product.setDescription(productData.getStr("description"));
            product.setMainImage(productData.getStr("imageUri"));
            product.setSalePrice(parsePrice(productData.getStr("price")));
            product.setCostPrice(parsePrice(productData.getStr("price")));
            product.setSource("1688");
            product.setSource1688Id(sourceId);
            product.setSource1688Url(productData.getStr("detailUrl"));
            product.setSupplierName(productData.getStr("companyName"));
            product.setStatus(0); // 默认下架
            product.setStock(productData.getInt("quantity", 0));

            // 保存商品
            productService.createProduct(product);

            log.info("1688商品导入成功: sourceId={}, productId={}", sourceId, product.getId());
            return product;
        } catch (Exception e) {
            log.error("1688商品导入失败: sourceId={}", sourceId, e);
            throw new RuntimeException("商品导入失败: " + e.getMessage());
        }
    }

    /**
     * 批量匹配货源
     *
     * @param productIds 商品ID列表
     * @return 匹配结果映射
     */
    public Map<Long, List<Map<String, Object>>> batchMatchSource(List<Long> productIds) {
        Map<Long, List<Map<String, Object>>> result = new HashMap<>();

        for (Long productId : productIds) {
            try {
                // 这里应该根据商品信息进行匹配
                result.put(productId, new ArrayList<>());
            } catch (Exception e) {
                log.error("批量匹配货源失败: productId={}", productId, e);
                result.put(productId, new ArrayList<>());
            }
        }

        return result;
    }

    /**
     * 调用1688 API
     */
    private String call1688Api(String method, Map<String, Object> params) {
        // 实际项目中需要实现签名逻辑
        // 这里返回模拟数据用于开发测试
        log.info("调用1688 API: method={}, params={}", method, params);

        // 模拟返回数据
        if ("alibaba.product.search".equals(method)) {
            return buildMockSearchResponse();
        } else if ("alibaba.product.get".equals(method)) {
            return buildMockProductResponse(params.get("productId").toString());
        }

        return "{}";
    }

    /**
     * 构建模拟搜索响应
     */
    private String buildMockSearchResponse() {
        JSONObject response = new JSONObject();
        JSONArray result = new JSONArray();

        for (int i = 1; i <= 5; i++) {
            JSONObject item = new JSONObject();
            item.put("productId", "1688-MOCK-" + i);
            item.put("subject", "模拟商品 " + i);
            item.put("price", "99.00");
            item.put("minOrderQuantity", 1);
            item.put("imageUri", "https://via.placeholder.com/200x200");
            item.put("detailUrl", "https://detail.1688.com/offer/" + i + ".html");
            item.put("companyName", "模拟供应商" + i);
            item.put("companyId", "COMP" + i);
            item.put("sendGoodsAddress", "浙江杭州");
            result.add(item);
        }

        response.put("result", result);
        return response.toString();
    }

    /**
     * 构建模拟商品详情响应
     */
    private String buildMockProductResponse(String productId) {
        JSONObject response = new JSONObject();
        JSONObject product = new JSONObject();

        product.put("productId", productId);
        product.put("subject", "模拟商品详情");
        product.put("description", "这是一个模拟的商品详情描述");
        product.put("price", "99.00");
        product.put("imageUri", "https://via.placeholder.com/400x400");
        product.put("detailUrl", "https://detail.1688.com/offer/" + productId + ".html");
        product.put("companyName", "模拟供应商");
        product.put("quantity", 1000);

        response.put("product", product);
        return response.toString();
    }

    /**
     * 解析价格
     */
    private BigDecimal parsePrice(String priceStr) {
        if (StrUtil.isBlank(priceStr)) {
            return BigDecimal.ZERO;
        }
        try {
            // 处理价格区间，取最低价
            if (priceStr.contains("-")) {
                priceStr = priceStr.split("-")[0];
            }
            return new BigDecimal(priceStr.replaceAll("[^0-9.]", ""));
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * 计算匹配分数
     */
    private double calculateMatchScore(String keyword, String title) {
        if (StrUtil.isBlank(keyword) || StrUtil.isBlank(title)) {
            return 0.0;
        }

        keyword = keyword.toLowerCase();
        title = title.toLowerCase();

        // 简单的匹配度计算
        if (title.contains(keyword)) {
            return 1.0;
        }

        // 分词匹配
        String[] keywords = keyword.split("\\s+");
        int matchCount = 0;
        for (String kw : keywords) {
            if (title.contains(kw)) {
                matchCount++;
            }
        }

        return (double) matchCount / keywords.length;
    }
}