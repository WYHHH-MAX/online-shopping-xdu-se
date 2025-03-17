package com.shop.online.utils;

import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Bean拷贝工具类
 */
public class BeanCopyUtils {

    /**
     * 单个对象拷贝
     */
    public static <T> T copyObject(Object source, Class<T> clazz) {
        if (source == null) {
            return null;
        }
        T target;
        try {
            target = clazz.newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 集合对象拷贝
     */
    public static <S, T> List<T> copyList(List<S> sourceList, Class<T> targetClass) {
        if (sourceList == null) {
            return null;
        }
        List<T> targetList = new ArrayList<>(sourceList.size());
        for (S source : sourceList) {
            T target = copyObject(source, targetClass);
            targetList.add(target);
        }
        return targetList;
    }

    /**
     * 集合对象拷贝（支持额外的操作）
     */
    public static <S, T> List<T> copyList(List<S> sourceList, Supplier<T> targetSupplier,
                                         java.util.function.BiConsumer<S, T> consumer) {
        if (sourceList == null) {
            return null;
        }
        List<T> targetList = new ArrayList<>(sourceList.size());
        for (S source : sourceList) {
            T target = targetSupplier.get();
            BeanUtils.copyProperties(source, target);
            consumer.accept(source, target);
            targetList.add(target);
        }
        return targetList;
    }
} 