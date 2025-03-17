package com.shop.online.controller;

import com.shop.online.vo.Result;
import com.shop.online.dto.category.CategoryDTO;
import com.shop.online.service.CategoryService;
import com.shop.online.vo.category.CategoryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/public/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/tree")
    public Result<List<CategoryVO>> getCategoryTree() {
        return Result.success(categoryService.tree());
    }

    @GetMapping("/list")
    public Result<List<CategoryVO>> getCategories(@RequestParam(required = false) Long parentId) {
        if (parentId != null) {
            return Result.success(categoryService.listByParentId(parentId));
        }
        return Result.success(categoryService.listByLevel(1));  // 默认返回一级分类
    }

    @PostMapping
    @PreAuthorize("hasRole('SELLER')")
    public Result<Void> create(@Validated @RequestBody CategoryDTO dto) {
        categoryService.create(dto);
        return Result.success();
    }

    @PutMapping
    @PreAuthorize("hasRole('SELLER')")
    public Result<Void> update(@Validated @RequestBody CategoryDTO dto) {
        categoryService.update(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<CategoryVO> getCategoryById(@PathVariable Long id) {
        return Result.success(categoryService.getCategoryById(id));
    }
} 