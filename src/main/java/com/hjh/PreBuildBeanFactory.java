package com.hjh;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author haojiahong created on 2019/12/6
 */
public class PreBuildBeanFactory extends DefaultBeanFactory {
    private Log logger = LogFactory.getLog(getClass());

    private List<String> beanNames = new ArrayList<>();

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) throws Exception {
        super.registerBeanDefinition(beanName, beanDefinition);
        synchronized (beanNames) {
            beanNames.add(beanName);
        }
    }

    //使用synchronized解决线程安全问题
    public void preInstantiateSingletons() throws Exception {
        synchronized (beanNames) {
            for (String name : beanNames) {
                BeanDefinition beanDefinition = this.getBeanDefinition(name);
                if (beanDefinition.isSingleton()) {
                    this.doGetBean(name);
                    if (logger.isDebugEnabled()) {
                        logger.debug("preInstantiate:name=" + name + " " + beanDefinition);
                    }
                }
            }
        }
    }
}
