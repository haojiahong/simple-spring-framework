package com.hjh.ioc;

import com.hjh.aop.BeanPostProcessor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author haojiahong created on 2019/12/6
 */
public class DefaultBeanFactory implements BeanFactory, BeanDefinitionRegistry, Closeable {
    //common-logging包和log4j-api包配合即可
    private final Log logger = LogFactory.getLog(getClass());
    private ThreadLocal<Set<String>> buildingBeans = new ThreadLocal<>();


    //考虑并发情况,256个前不需要进行扩容
    private Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);

    private Map<String, Object> beanMap = new ConcurrentHashMap<>(256);

    private List<BeanPostProcessor> beanPostProcessors = Collections.synchronizedList(new ArrayList<>());


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

        // 记录正在创建的Bean
        Set<String> ingBeans = this.buildingBeans.get();
        if (ingBeans == null) {
            ingBeans = new HashSet<>();
            this.buildingBeans.set(ingBeans);
        }

        // 检测循环依赖
        if (ingBeans.contains(beanName)) {
            throw new Exception(beanName + " 循环依赖！" + ingBeans);
        }

        // 记录正在创建的Bean
        ingBeans.add(beanName);

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
        // 创建好实例后，移除创建中记录
        ingBeans.remove(beanName);

        // 给入属性依赖
        this.setPropertyDIValues(beanDefinition, instance);

        // 应用bean初始化前的处理
        instance = this.applyPostProcessBeforeInitialization(instance, beanName);

        this.doInit(beanDefinition, instance);

        // 应用bean初始化后的处理
        instance = this.applyPostProcessAfterInitialization(instance, beanName);

        if (beanDefinition.isSingleton()) {
            beanMap.put(beanName, instance);
        }


        return instance;
    }

    // 应用bean初始化前的处理
    private Object applyPostProcessBeforeInitialization(Object bean, String beanName) throws Exception {
        for (BeanPostProcessor bpp : this.beanPostProcessors) {
            bean = bpp.postProcessBeforeInitialization(bean, beanName);
        }
        return bean;
    }

    // 应用bean初始化后的处理
    private Object applyPostProcessAfterInitialization(Object bean, String beanName) throws Exception {
        for (BeanPostProcessor bpp : this.beanPostProcessors) {
            bean = bpp.postProcessAfterInitialization(bean, beanName);
        }
        return bean;
    }

    private void setPropertyDIValues(BeanDefinition bd, Object instance) throws Exception {
        if (CollectionUtils.isEmpty(bd.getPropertyValues())) {
            return;
        }
        for (PropertyValue pv : bd.getPropertyValues()) {
            if (StringUtils.isBlank(pv.getName())) {
                continue;
            }
            Class<?> clazz = instance.getClass();
            Field p = clazz.getDeclaredField(pv.getName());

            p.setAccessible(true);

            Object rv = pv.getValue();
            Object v = null;
            if (rv == null) {
                v = null;
            } else if (rv instanceof BeanReference) {
                v = this.doGetBean(((BeanReference) rv).getBeanName());
            } else if (rv instanceof Object[]) {
                // TODO 处理集合中的bean引用
            } else if (rv instanceof Collection) {
                // TODO 处理集合中的bean引用
            } else if (rv instanceof Properties) {
                // TODO 处理properties中的bean引用
            } else if (rv instanceof Map) {
                // TODO 处理Map中的bean引用
            } else {
                v = rv;
            }

            p.set(instance, v);

        }
    }

    //构造方法来创建对象
    private Object createInstanceByConstructor(BeanDefinition beanDefinition) throws IllegalAccessException, InstantiationException {
        try {
            //获取真正的参数值
            Object[] args = this.getConstructorArgumentValues(beanDefinition);
            if (args == null) {
                return beanDefinition.getBeanClass().newInstance();
            } else {
                // 决定构造方法
                return this.determineConstructor(beanDefinition, args).newInstance(args);
            }
        } catch (Exception e1) {
            logger.error("创建bean的实例异常,beanDefinition：" + beanDefinition, e1);
            throw new RuntimeException(e1);
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

    private Constructor determineConstructor(BeanDefinition beanDefinition, Object[] args) throws Exception {
        Constructor constructor = null;

        //当没有任何一个参数时直接获取无参构造方法
        if (args == null) {
            return beanDefinition.getBeanClass().getConstructor(null);
        }

        //对于原型bean,第二次开始获取Bean实例时,可直接获取第一次缓存的构造方法
        constructor = beanDefinition.getConstructor();
        if (constructor != null) {
            return constructor;
        }

        //根据参数类型获取精确匹配的构造方法
        Class<?>[] paramTypes = new Class[args.length];
        int j = 0;
        for (Object paramType : args) {
            paramTypes[j++] = paramType.getClass();
        }
        try {
            constructor = beanDefinition.getConstructor();
        } catch (Exception e) {
            //此异常不需要进行处理
        }

        if (constructor == null) {
            //把所有的构造器全部遍历出来一一比对
            Outer:
            for (Constructor<?> allConstructor : beanDefinition.getBeanClass().getConstructors()) {
                Class<?>[] pTypes = allConstructor.getParameterTypes();
                //此构造方法的参数长度等于提供参数长度
                if (pTypes.length == args.length) {
                    for (int i = 0; i < pTypes.length; i++) {

                        //如果第一个参数的类型就已经不匹配了,就直接不再继续比对了,直接跳转到外循环
                        if (!pTypes[i].isAssignableFrom(args[i].getClass())) {
                            continue Outer;
                        }
                    }

                    //如果以上皆匹配的话,就直接获取到这个构造器,然后直接让循环终止
                    constructor = allConstructor;
                    break Outer;
                }
            }
        }

        if (constructor != null) {
            if (beanDefinition.isPrototype()) {
                //对原型bean构造器进行缓存方便下次查找
                beanDefinition.setConstructor(constructor);
            }
            return constructor;
        } else {
            throw new Exception("不存在对应的构造方法!" + beanDefinition);
        }
    }


    @Override
    public void registerBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        this.beanPostProcessors.add(beanPostProcessor);
//        if (beanPostProcessor instanceof BeanFactoryAware) {
//            ((BeanFactoryAware) beanPostProcessor).setBeanFactory(this);
//        }
    }
}
