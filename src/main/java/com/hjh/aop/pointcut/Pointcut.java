package com.hjh.aop.pointcut;

import java.lang.reflect.Method;

/**
 * @author haojiahong created on 2019/12/8
 */
public interface Pointcut {
    //提供两个方法,匹配类和匹配方法
    boolean matchClass(Class<?> targetClass);

    boolean matchMethod(Method method, Class<?> targetClass);
}
