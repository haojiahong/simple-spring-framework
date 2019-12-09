package com.hjh.aop.advisor;

/**
 * @author haojiahong created on 2019/12/8
 */
public interface Advisor {
    String getAdviceBeanName();

    String getExpression();
}
