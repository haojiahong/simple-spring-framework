package com.hjh.test;

/**
 * @author haojiahong created on 2019/12/6
 */
public class Bean1Factory {
    public static Bean1 getBean1() {
        return new Bean1();
    }

    public Bean1 getOtherBean1() {
        return new Bean1();
    }
}
