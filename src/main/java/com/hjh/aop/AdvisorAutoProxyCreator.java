package com.hjh.aop;

import com.hjh.aop.advisor.Advisor;
import com.hjh.aop.advisor.AspectJPointcutAdvisor;
import com.hjh.aop.pointcut.Pointcut;
import com.hjh.ioc.BeanFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author haojiahong created on 2019/12/9
 */
public class AdvisorAutoProxyCreator implements BeanPostProcessor {

    private List<Advisor> advisors;
    private BeanFactory beanFactory;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws Exception {
        // 第一步：在此判断bean是否需要进行切面增强
        List<Advisor> matchAdvisors = getMatchedAdvisors(bean, beanName);

        // 第二步：如需要就进行增强,创建代理对象，进行代理增强。再返回增强的对象。
        if (CollectionUtils.isNotEmpty(matchAdvisors)) {
            bean = this.createProxy(bean, beanName, matchAdvisors);
        }
        return bean;
    }

    private List<Advisor> getMatchedAdvisors(Object bean, String beanName) {
        if (CollectionUtils.isEmpty(advisors)) {
            return null;
        }

        // 得到类、所有的方法
        Class<?> beanClass = bean.getClass();
        List<Method> allMethods = this.getAllMethodForClass(beanClass);

        // 存放匹配的Advisor的list
        List<Advisor> matchAdvisors = new ArrayList<>();
        // 遍历Advisor来找匹配的
        for (Advisor ad : this.advisors) {
            if (ad instanceof AspectJPointcutAdvisor) {
                if (isPointcutMatchBean((AspectJPointcutAdvisor) ad, beanClass, allMethods)) {
                    matchAdvisors.add(ad);
                }
            }
        }

        return matchAdvisors;
    }

    private List<Method> getAllMethodForClass(Class<?> beanClass) {
        List<Method> allMethods = new LinkedList<>();
        Set<Class<?>> classes = new LinkedHashSet<>(ClassUtils.getAllInterfacesForClassAsSet(beanClass));
        classes.add(beanClass);
        for (Class<?> clazz : classes) {
            Method[] methods = ReflectionUtils.getAllDeclaredMethods(clazz);
            for (Method m : methods) {
                allMethods.add(m);
            }
        }

        return allMethods;
    }

    private Object createProxy(Object bean, String beanName, List<Advisor> matchAdvisors) throws Exception {
        // 通过AopProxyFactory工厂去完成选择、和创建代理对象的工作。
        return AopProxyFactory.getDefaultAopProxyFactory().createAopProxy(bean, beanName, matchAdvisors, beanFactory)
                .getProxy();
    }

    private boolean isPointcutMatchBean(AspectJPointcutAdvisor pa, Class<?> beanClass, List<Method> methods) {
        Pointcut p = pa.getPointcut();

        // 首先判断类是否匹配
        // 注意之前说过的AspectJ情况下这个匹配是不可靠的，需要通过方法来匹配
        //这里的判断仅仅起到过滤作用，类不匹配的前提下直接跳过
        if (!p.matchClass(beanClass)) {
            return false;
        }

        // 再判断是否有方法匹配
        for (Method method : methods) {
            if (p.matchMethod(method, beanClass)) {
                return true;
            }
        }
        return false;
    }
}
