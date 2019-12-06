package com.hjh.test;

import com.hjh.BeanDefinition;
import com.hjh.DefaultBeanFactory;
import com.hjh.GeneralBeanDefinition;
import org.junit.AfterClass;
import org.junit.Test;

/**
 * @author haojiahong created on 2019/12/6
 */
public class DefaultBeanFactoryTest {
    static DefaultBeanFactory defaultBeanFactory = new DefaultBeanFactory();

    @Test
    public void testRegist() throws Exception {
        GeneralBeanDefinition generalBeanDefinition = new GeneralBeanDefinition();
        generalBeanDefinition.setBeanClass(Bean1.class);
        generalBeanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
        generalBeanDefinition.setInitMethodName("init");
        generalBeanDefinition.setDestroyMethodName("destroy");

        defaultBeanFactory.registerBeanDefinition("bean1", generalBeanDefinition);
    }

    @Test
    public void testRegistStaticFactoryMethod() throws Exception {
        GeneralBeanDefinition generalBeanDefinition = new GeneralBeanDefinition();
        generalBeanDefinition.setBeanClass(Bean1Factory.class);
        generalBeanDefinition.setFactoryMethodName("getBean1");
        defaultBeanFactory.registerBeanDefinition("staticBean1", generalBeanDefinition);
    }

    @Test
    public void testRegistFactoryMethod() throws Exception {
        GeneralBeanDefinition generalBeanDefinition = new GeneralBeanDefinition();
        generalBeanDefinition.setBeanClass(Bean1Factory.class);
        String factoryBeanName = "factory";
        defaultBeanFactory.registerBeanDefinition(factoryBeanName, generalBeanDefinition);

        generalBeanDefinition = new GeneralBeanDefinition();
        generalBeanDefinition.setFactoryBeanName(factoryBeanName);
        generalBeanDefinition.setFactoryMethodName("getOtherBean1");
        generalBeanDefinition.setScope(BeanDefinition.SCOPE_PROTOTYPE);
        defaultBeanFactory.registerBeanDefinition("factoryBean", generalBeanDefinition);
    }

    @AfterClass
    public static void testGetBean() throws Exception {
        System.out.println("构造方法方式···");
        for (int i = 0; i < 3; i++) {
            Bean1 bean1 = (Bean1) defaultBeanFactory.getBean("bean1");
            bean1.doSomething();
        }

        System.out.println("静态工厂方法方式···");
        for (int i = 0; i < 3; i++) {
            Bean1 bean1 = (Bean1) defaultBeanFactory.getBean("staticBean1");
            bean1.doSomething();
        }

        System.out.println("工厂方法方式···");
        for (int i = 0; i < 3; i++) {
            Bean1 bean1 = (Bean1) defaultBeanFactory.getBean("factoryBean");
            bean1.doSomething();
        }

        defaultBeanFactory.close();
    }
}
