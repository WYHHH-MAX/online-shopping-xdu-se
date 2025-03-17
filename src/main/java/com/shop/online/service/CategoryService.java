package com.shop.online.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shop.online.dto.category.CategoryDTO;
import com.shop.online.entity.Category;
import com.shop.online.vo.category.CategoryVO;

import java.util.List;

public interface CategoryService extends IService<Category> {
    /**
     * 获取分类树形结构
     */
    List<CategoryVO> tree();
    
    /**
     * 创建分类
     */
    void create(CategoryDTO dto);
    
    /**
     * 更新分类
     */
    void update(CategoryDTO dto);
    
    /**
     * 删除分类
     */
    void delete(Long id);
    
    /**
     * 获取指定层级的分类列表
     */
    List<CategoryVO> listByLevel(Integer level);
    
    /**
     * 获取指定父级的子分类
     */
    List<CategoryVO> listByParentId(Long parentId);

    CategoryVO getCategoryById(Long id);
} 