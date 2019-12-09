package com.hjh.aop;

import com.hjh.aop.advisor.Advisor;
import com.hjh.ioc.BeanFactory;

import java.util.List;

/**
 * @author haojiahong created on 2019/12/9
 */
public class DefaultAopProxyFactory implements AopProxyFactory {
    @Override
    public AopProxy createAopProxy(Object bean, String beanName, List<Advisor> matchAdvisors, BeanFactory beanFactory) throws Exception {
        // 是该用jdk动态代理还是cglib？
        if (shouldUseJDKDynamicProxy(bean, beanName)) {
            return new JdkDynamicAopProxy(beanName, bean, matchAdvisors, beanFactory);
        } else {
            return new CglibDynamicAopProxy(beanName, bean, matchAdvisors, beanFactory);
        }
    }

    private boolean shouldUseJDKDynamicProxy(Object bean, String beanName) {
        // 如何判断？有实现接口就用JDK,没有就用cglib？
        return false;
    }
}
