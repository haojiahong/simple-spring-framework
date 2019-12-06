package com.hjh;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author haojiahong created on 2019/12/6
 */
public interface BeanDefinition {
    String SCOPE_SINGLETON = "singleton";
    String SCOPE_PROTOTYPE = "prototype";

    Class<?> getBeanClass();

    String getScope();

    boolean isSingleton();

    boolean isPrototype();

    String getFactoryBeanName();

    String getFactoryMethodName();

    String getInitMethodName();

    String getDestoryMethodName();

    List<?> getConstructorArgumentValues();

    //tips:java8开始就可以直接写接口默认方法了
    default boolean validate() {
        //class没指定,工厂bean或工厂method不指定皆为不合法情况
        if (this.getBeanClass() == null) {
            if (StringUtils.isBlank(getFactoryBeanName()) || StringUtils.isBlank(getFactoryMethodName())) {
                return false;
            }
        }

        //class和工厂bean同时存在
        if (this.getBeanClass() != null && StringUtils.isNotBlank(getFactoryBeanName())) {
            return false;
        }
        return true;
    }

}
