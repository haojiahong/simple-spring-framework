package com.hjh;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author haojiahong created on 2019/12/6
 */
public class DefaultBeanFactory implements BeanFactory, BeanDefinitionRegistry, Closeable {
    //common-logging包和log4j-api包配合即可
    private final Log logger = LogFactory.getLog(getClass());

    //考虑并发情况,256个前不需要进行扩容
    private Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);

    private Map<String, Object> beanMap = new ConcurrentHashMap<>(256);

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) throws Exception {
        //参数检查
        Objects.requireNonNull(beanName, "注册bean需要输入beanName");
        Objects.requireNonNull(beanDefinition, "注册bean需要输入beanDefinition");

        //检验给入的bean是否合法
        if (!beanDefinition.validate()) {
            throw new Exception("名字为[" + beanName + "]的bean定义不合法," + beanDefinition);
        }

        if (this.containsBeanDefinition(beanName)) {
            throw new Exception("名字为[" + beanName + "]的bean定义已经存在," + this.getBeanDefinition(beanName));
        }

        this.beanDefinitionMap.put(beanName, beanDefinition);
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName) {
        return this.beanDefinitionMap.get(beanName);
    }

    @Override
    public Boolean containsBeanDefinition(String beanName) {
        return this.beanDefinitionMap.containsKey(beanName);
    }

    @Override
    public Object getBean(String name) throws Exception {
        return this.doGetBean(name);
    }

    //不需要判断scope,因为只有单例bean才需要放入map中
    //使用protected保证只有DefaultBeanFactory的子类可以调用该方法
    protected Object doGetBean(String beanName) throws Exception {
        Objects.requireNonNull(beanName, "beanName不能为空");
        Object instance = beanMap.get(beanName);

        if (instance != null) {
            return instance;
        }
        BeanDefinition beanDefinition = this.getBeanDefinition(beanName);
        Objects.requireNonNull(beanDefinition, "beanDefinition不能为空");

        Class<?> type = beanDefinition.getBeanClass();

        //因为总共就只有3种方式,也不需要扩充或者是修改代码了,所以就不需要考虑使用策略模式了
        if (type != null) {
            if (StringUtils.isBlank(beanDefinition.getFactoryMethodName())) {
                instance = this.createInstanceByConstructor(beanDefinition);
            } else {
                instance = this.createInstanceByStaticFactoryMethod(beanDefinition);
            }
        } else {
            instance = this.createInstanceByFactoryBean(beanDefinition);
        }

        this.doInit(beanDefinition, instance);

        if (beanDefinition.isSingleton()) {
            beanMap.put(beanName, instance);
        }

        return instance;
    }

    //构造方法来创建对象
    private Object createInstanceByConstructor(BeanDefinition beanDefinition) throws IllegalAccessException, InstantiationException {
        try {
            return beanDefinition.getBeanClass().newInstance();
        } catch (SecurityException e) {
            logger.error("创建bean的实例异常,beanDefinition" + beanDefinition, e);
            throw e;
        }
    }

    //静态工厂方法(暂时不考虑带参数)
    private Object createInstanceByStaticFactoryMethod(BeanDefinition beanDefinition) throws Exception {
        Class<?> type = beanDefinition.getBeanClass();
        Method method = type.getMethod(beanDefinition.getFactoryMethodName(), null);
        return method.invoke(type, null);
    }

    //工厂bean方法来创建对象(暂时不考虑带参数)
    private Object createInstanceByFactoryBean(BeanDefinition beanDefinition) throws Exception {
        Object factoryBean = this.doGetBean(beanDefinition.getFactoryBeanName());
        Method method = factoryBean.getClass().getMethod(beanDefinition.getFactoryMethodName(), null);
        return method.invoke(factoryBean, null);
    }

    //初始化方法
    private void doInit(BeanDefinition beanDefinition, Object instance) throws Exception {
        if (StringUtils.isNotBlank(beanDefinition.getInitMethodName())) {
            Method method = instance.getClass().getMethod(beanDefinition.getInitMethodName(), null);
            method.invoke(instance, null);
        }
    }

    @Override
    public void close() throws IOException {
        //执行单例实例的销毁方法
        //遍历map把bean都取出来然后调用每个bean的销毁方法
        for (Map.Entry<String, BeanDefinition> entry : this.beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();

            if (beanDefinition.isSingleton() && StringUtils.isNotBlank(beanDefinition.getDestoryMethodName())) {
                Object instance = this.beanMap.get(beanName);
                try {
                    Method method = instance.getClass().getMethod(beanDefinition.getDestoryMethodName(), null);
                    method.invoke(instance, null);
                } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    logger.error("执行bean[" + beanName + "] " + beanDefinition + "的销毁方法异常", e);
                }
            }
        }
    }


    private Object[] getConstructorArgumentValues(BeanDefinition beanDefinition) throws Exception {
        return this.getRealValues(beanDefinition.getConstructorArgumentValues());
    }

    private Object[] getRealValues(List<?> defs) throws Exception {
        if (CollectionUtils.isEmpty(defs)) {
            return null;
        }
        Object[] values = new Object[defs.size()];
        int i = 0;
        //values数组的元素
        Object value = null;
        for (Object realValue : defs) {
            if (realValue == null) {
                value = null;
            } else if (realValue instanceof BeanReference) {
                value = this.doGetBean(((BeanReference) realValue).getBeanName());
            } else {
                value = realValue;
            }
            values[i++] = value;
        }
        return values;
    }


}
