package com.shop.online.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.online.dto.category.CategoryDTO;
import com.shop.online.entity.Category;
import com.shop.online.exception.BusinessException;
import com.shop.online.mapper.CategoryMapper;
import com.shop.online.service.CategoryService;
import com.shop.online.vo.category.CategoryVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<CategoryVO> tree() {
        // 获取所有分类
        List<Category> categories = list(new LambdaQueryWrapper<Category>()
                .orderByAsc(Category::getSort));
        
        // 转换为VO
        List<CategoryVO> categoryVOs = categories.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        // 构建树形结构
        return buildTree(categoryVOs);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(CategoryDTO dto) {
        // 检查父级分类是否存在
        if (!dto.getParentId().equals(0L)) {
            Category parent = getById(dto.getParentId());
            if (parent == null) {
                throw new BusinessException("父级分类不存在");
            }
            // 检查层级是否正确
            if (!dto.getLevel().equals(parent.getLevel() + 1)) {
                throw new BusinessException("分类层级不正确");
            }
        } else if (!dto.getLevel().equals(1)) {
            throw new BusinessException("顶级分类层级必须为1");
        }
        
        // 检查同级分类名称是否重复
        long count = count(new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, dto.getParentId())
                .eq(Category::getName, dto.getName()));
        if (count > 0) {
            throw new BusinessException("同级分类名称已存在");
        }
        
        // 创建分类
        Category category = new Category();
        BeanUtils.copyProperties(dto, category);
        save(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(CategoryDTO dto) {
        // 检查分类是否存在
        Category category = getById(dto.getId());
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        
        // 检查父级分类是否存在
        if (!dto.getParentId().equals(0L)) {
            Category parent = getById(dto.getParentId());
            if (parent == null) {
                throw new BusinessException("父级分类不存在");
            }
            // 检查层级是否正确
            if (!dto.getLevel().equals(parent.getLevel() + 1)) {
                throw new BusinessException("分类层级不正确");
            }
        } else if (!dto.getLevel().equals(1)) {
            throw new BusinessException("顶级分类层级必须为1");
        }
        
        // 检查同级分类名称是否重复
        long count = count(new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, dto.getParentId())
                .eq(Category::getName, dto.getName())
                .ne(Category::getId, dto.getId()));
        if (count > 0) {
            throw new BusinessException("同级分类名称已存在");
        }
        
        // 更新分类
        BeanUtils.copyProperties(dto, category);
        updateById(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 检查是否有子分类
        long count = count(new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, id));
        if (count > 0) {
            throw new BusinessException("请先删除子分类");
        }
        
        // 删除分类
        removeById(id);
    }

    @Override
    public List<CategoryVO> listByLevel(Integer level) {
        List<Category> categories = list(new LambdaQueryWrapper<Category>()
                .eq(Category::getLevel, level)
                .orderByAsc(Category::getSort));
        
        return categories.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryVO> listByParentId(Long parentId) {
        List<Category> categories = list(new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, parentId)
                .orderByAsc(Category::getSort));
        
        return categories.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryVO getCategoryById(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            return null;
        }
        
        CategoryVO vo = convertToVO(category);
        
        // 如果不是顶级分类，获取父分类信息
        if (category.getParentId() != 0L) {
            Category parentCategory = categoryMapper.selectById(category.getParentId());
            if (parentCategory != null) {
                CategoryVO parentVO = convertToVO(parentCategory);
                vo.setParent(parentVO);
            }
        }
        
        return vo;
    }

    private CategoryVO convertToVO(Category category) {
        CategoryVO vo = new CategoryVO();
        BeanUtils.copyProperties(category, vo);
        return vo;
    }

    private List<CategoryVO> buildTree(List<CategoryVO> categories) {
        // 按父ID分组
        Map<Long, List<CategoryVO>> parentMap = categories.stream()
                .collect(Collectors.groupingBy(CategoryVO::getParentId));
        
        // 设置子分类
        categories.forEach(category -> 
            category.setChildren(parentMap.getOrDefault(category.getId(), new ArrayList<>()))
        );
        
        // 返回顶级分类
        return parentMap.getOrDefault(0L, new ArrayList<>());
    }
} 