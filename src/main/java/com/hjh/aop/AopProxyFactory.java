package com.hjh.aop;

import com.hjh.aop.advisor.Advisor;
import com.hjh.ioc.BeanFactory;

import java.util.List;

/**
 * @author haojiahong created on 2019/12/9
 */
public interface AopProxyFactory {
    AopProxy createAopProxy(Object bean, String beanName, List<Advisor> matchAdvisors, BeanFactory beanFactory)
            throws Exception;

    /**
     * 获得默认的AopProxyFactory实例
     *
     * @return AopProxyFactory
     */
    static AopProxyFactory getDefaultAopProxyFactory() {
        return (AopProxyFactory) new DefaultAopProxyFactory();
    }
}
