package com.hjh.aop;

/**
 * @author haojiahong created on 2019/12/9
 */
public interface AopProxy {
    Object getProxy();

    Object getProxy(ClassLoader classLoader);
}
