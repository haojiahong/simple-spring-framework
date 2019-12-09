package com.hjh.aop;

import com.hjh.aop.advisor.Advisor;
import com.hjh.aop.advisor.AspectJPointcutAdvisor;
import com.hjh.ioc.BeanFactory;
import org.apache.commons.collections4.CollectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author haojiahong created on 2019/12/9
 */
public class AopProxyUtils {


    public static Object applyAdvices(Object target, Method method, Object[] args,
                                      List<Advisor> matchAdvisors, Object proxy, BeanFactory beanFactory) throws Throwable {
        // 这里要做什么？
        // 1、获取要对当前方法进行增强的advice
        List<Object> advices = AopProxyUtils.getShouldApplyAdvices(target.getClass(), method, matchAdvisors,
                beanFactory);
        // 2、如有增强的advice，责任链式增强执行
        if (CollectionUtils.isEmpty(advices)) {
            return method.invoke(target, args);
        } else {
            // 责任链式执行增强
            AopAdviceChainInvocation chain = new AopAdviceChainInvocation(proxy, target, method, args, advices);
            return chain.invoke();
        }
    }

    public static List<Object> getShouldApplyAdvices(Class<?> beanClass, Method method, List<Advisor> matchAdvisors,
                                                     BeanFactory beanFactory) throws Throwable {
        if (CollectionUtils.isEmpty(matchAdvisors)) {
            return null;
        }
        List<Object> advices = new ArrayList<>();
        for (Advisor ad : matchAdvisors) {
            if (ad instanceof AspectJPointcutAdvisor) {
                if (((AspectJPointcutAdvisor) ad).getPointcut().matchMethod(method, beanClass)) {
                    advices.add(beanFactory.getBean(ad.getAdviceBeanName()));
                }
            }
        }

        return advices;
    }
}
