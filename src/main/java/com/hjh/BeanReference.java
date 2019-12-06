package com.hjh;

/**
 * @author haojiahong created on 2019/12/6
 */
public class BeanReference {
    private String beanName;

    public BeanReference(String beanName) {
        super();
        this.beanName = beanName;
    }

    /**
     * 获得引用的beanName
     *
     * @return
     */
    public String getBeanName() {
        return this.beanName;
    }
}
