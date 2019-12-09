package com.hjh.aop.advice;

import com.hjh.aop.advice.Advice;

import java.lang.reflect.Method;

/**
 * @author haojiahong created on 2019/12/8
 */
public interface MethodBeforeAdvice extends Advice {


    void before(Method method, Object[] args, Object target) throws Throwable;
}
