package com.hjh.ioc.test;

/**
 * @author haojiahong created on 2019/12/6
 */
public class Bean1 {
    public void doSomething() {
        System.out.println(System.currentTimeMillis() + " " + this);
    }

    public void init() {
        System.out.println("bean1的init已执行");
    }

    public void destroy() {
        System.out.println("bean1的destroy已执行");
    }

}
