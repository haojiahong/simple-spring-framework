package com.hjh.ioc;

import com.hjh.aop.BeanPostProcessor;

/**
 * 简单工厂模式中，当工厂能创建很多类产品，如果需要某类产品，需要告诉工厂
 *
 * @author haojiahong created on 2019/12/6
 */
public interface BeanFactory {

    /**
     * 获取bean
     *
     * @param name
     * @return
     * @throws Exception
     */
    Object getBean(String name) throws Exception;


    void registerBeanPostProcessor(BeanPostProcessor beanPostProcessor);
}
