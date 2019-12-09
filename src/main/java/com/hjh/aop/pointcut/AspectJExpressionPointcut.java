package com.hjh.aop.pointcut;

import org.aspectj.weaver.tools.PointcutExpression;
import org.aspectj.weaver.tools.PointcutParser;
import org.aspectj.weaver.tools.ShadowMatch;

import java.lang.reflect.Method;

/**
 * @author haojiahong created on 2019/12/8
 */
public class AspectJExpressionPointcut implements Pointcut {

    //得到了一个全局的切点解析器
    private static PointcutParser pp = PointcutParser
            .getPointcutParserSupportingAllPrimitivesAndUsingContextClassloaderForResolution();
    private PointcutExpression pe;


    private String expression;

    public AspectJExpressionPointcut(String expression) {
        super();
        this.expression = expression;
        //解析成对应的表达式对象
        pe = pp.parsePointcutExpression(expression);
    }

    public String getExpression() {
        return this.expression;
    }

    @Override
    public boolean matchClass(Class<?> targetClass) {
        return pe.couldMatchJoinPointsInType(targetClass);
    }

    @Override
    public boolean matchMethod(Method method, Class<?> targetClass) {
        ShadowMatch sm = pe.matchesMethodExecution(method);
        return sm.alwaysMatches();
    }
}
