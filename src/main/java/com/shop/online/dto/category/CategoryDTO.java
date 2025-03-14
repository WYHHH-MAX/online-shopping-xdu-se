package com.shop.online.dto.category;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class CategoryDTO {
    private Long id;
    
    @NotNull(message = "父级分类ID不能为空")
    private Long parentId;
    
    @NotBlank(message = "分类名称不能为空")
    private String name;
    
    @NotNull(message = "分类层级不能为空")
    private Integer level;
    
    private Integer sort;
    
    private String icon;
} 