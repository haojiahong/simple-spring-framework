package com.hjh.aop.advisor;

import com.hjh.aop.pointcut.AspectJExpressionPointcut;

/**
 * @author haojiahong created on 2019/12/8
 */
public class AspectJPointcutAdvisor implements Advisor {

    private String adviceBeanName;
    private String expression;
    private AspectJExpressionPointcut pointcut;

    public AspectJPointcutAdvisor(String adviceBeanName, String expression) {
        this.adviceBeanName = adviceBeanName;
        this.expression = expression;
        this.pointcut = new AspectJExpressionPointcut(this.expression);
    }

    @Override
    public String getAdviceBeanName() {
        return this.adviceBeanName;
    }

    @Override
    public String getExpression() {
        return this.expression;
    }

    public AspectJExpressionPointcut getPointcut() {
        return pointcut;
    }
}
