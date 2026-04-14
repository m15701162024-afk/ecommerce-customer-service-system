package com.ecommerce.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.service.ProductService;
import com.ecommerce.product.service.SourceMatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品控制器
 */
@Slf4j
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final SourceMatchService sourceMatchService;

    /**
     * 商品列表(分页)
     * GET /products/list
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getProductList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status) {

        log.info("查询商品列表: pageNum={}, pageSize={}, name={}, categoryId={}, status={}",
                pageNum, pageSize, name, categoryId, status);

        IPage<Product> page = productService.getProductList(pageNum, pageSize, name, categoryId, status);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of(
                "list", page.getRecords(),
                "total", page.getTotal(),
                "pageNum", page.getCurrent(),
                "pageSize", page.getSize(),
                "pages", page.getPages()
        ));

        return ResponseEntity.ok(result);
    }

    /**
     * 商品详情
     * GET /products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getProduct(@PathVariable Long id) {
        log.info("查询商品详情: id={}", id);

        Product product = productService.getProductById(id);
        // 增加浏览次数
        productService.incrementViewCount(id);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", product);

        return ResponseEntity.ok(result);
    }

    /**
     * 创建商品
     * POST /products
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCT_MANAGER')")
    @PostMapping
    public ResponseEntity<Map<String, Object>> createProduct(@Valid @RequestBody Product product) {
        log.info("创建商品: name={}", product.getName());

        Product created = productService.createProduct(product);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "创建成功");
        result.put("data", created);

        return ResponseEntity.ok(result);
    }

    /**
     * 更新商品
     * PUT /products/{id}
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCT_MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody Product product) {

        log.info("更新商品: id={}", id);

        Product updated = productService.updateProduct(id, product);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "更新成功");
        result.put("data", updated);

        return ResponseEntity.ok(result);
    }

    /**
     * 删除商品
     * DELETE /products/{id}
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCT_MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable Long id) {
        log.info("删除商品: id={}", id);

        productService.deleteProduct(id);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "删除成功");

        return ResponseEntity.ok(result);
    }

    /**
     * 上架商品
     * PUT /products/{id}/publish
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCT_MANAGER')")
    @PutMapping("/{id}/publish")
    public ResponseEntity<Map<String, Object>> publishProduct(@PathVariable Long id) {
        log.info("上架商品: id={}", id);

        productService.publishProduct(id);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "上架成功");

        return ResponseEntity.ok(result);
    }

    /**
     * 下架商品
     * PUT /products/{id}/unpublish
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCT_MANAGER')")
    @PutMapping("/{id}/unpublish")
    public ResponseEntity<Map<String, Object>> unpublishProduct(@PathVariable Long id) {
        log.info("下架商品: id={}", id);

        productService.unpublishProduct(id);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "下架成功");

        return ResponseEntity.ok(result);
    }

    /**
     * 匹配1688货源
     * POST /products/{id}/match-source
     */
    @PostMapping("/{id}/match-source")
    public ResponseEntity<Map<String, Object>> matchSource(
            @PathVariable Long id,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info("匹配1688货源: productId={}, keyword={}", id, keyword);

        // 如果没有提供关键词，使用商品名称
        if (keyword == null || keyword.isEmpty()) {
            Product product = productService.getProductById(id);
            keyword = product.getName();
        }

        List<Map<String, Object>> matches = sourceMatchService.matchSourceFrom1688(keyword, page, size);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "匹配成功");
        result.put("data", Map.of(
                "productId", id,
                "keyword", keyword,
                "matches", matches,
                "total", matches.size()
        ));

        return ResponseEntity.ok(result);
    }

    /**
     * 搜索1688货源
     * GET /products/source/search
     */
    @GetMapping("/source/search")
    public ResponseEntity<Map<String, Object>> searchSource(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info("搜索1688货源: keyword={}", keyword);

        List<Map<String, Object>> sources = sourceMatchService.matchSourceFrom1688(keyword, page, size);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of(
                "keyword", keyword,
                "list", sources,
                "total", sources.size()
        ));

        return ResponseEntity.ok(result);
    }

    /**
     * 从1688导入商品
     * POST /products/source/import
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCT_MANAGER')")
    @PostMapping("/source/import")
    public ResponseEntity<Map<String, Object>> importFromSource(
            @RequestParam String sourceId) {

        log.info("从1688导入商品: sourceId={}", sourceId);

        Product product = sourceMatchService.importFrom1688(sourceId, productService);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "导入成功");
        result.put("data", product);

        return ResponseEntity.ok(result);
    }

    /**
     * 热门商品
     * GET /products/hot
     */
    @GetMapping("/hot")
    public ResponseEntity<Map<String, Object>> getHotProducts(
            @RequestParam(defaultValue = "10") Integer limit) {

        log.info("获取热门商品: limit={}", limit);

        List<Product> products = productService.getHotProducts(limit);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", products);

        return ResponseEntity.ok(result);
    }

    /**
     * 新品商品
     * GET /products/new
     */
    @GetMapping("/new")
    public ResponseEntity<Map<String, Object>> getNewProducts(
            @RequestParam(defaultValue = "10") Integer limit) {

        log.info("获取新品商品: limit={}", limit);

        List<Product> products = productService.getNewProducts(limit);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", products);

        return ResponseEntity.ok(result);
    }

    /**
     * 更新库存
     * PUT /products/{id}/stock
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCT_MANAGER', 'WAREHOUSE')")
    @PutMapping("/{id}/stock")
    public ResponseEntity<Map<String, Object>> updateStock(
            @PathVariable Long id,
            @RequestParam Integer quantity) {

        log.info("更新库存: id={}, quantity={}", id, quantity);

        productService.updateStock(id, quantity);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "库存更新成功");

        return ResponseEntity.ok(result);
    }

    /**
     * 根据分类获取商品
     * GET /products/category/{categoryId}
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Map<String, Object>> getByCategory(@PathVariable Long categoryId) {
        log.info("根据分类获取商品: categoryId={}", categoryId);

        List<Product> products = productService.getByCategoryId(categoryId);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", products);

        return ResponseEntity.ok(result);
    }
}