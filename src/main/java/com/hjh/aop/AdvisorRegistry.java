package com.hjh.aop;

import com.hjh.aop.advisor.Advisor;

import java.util.List;

/**
 * @author haojiahong created on 2019/12/8
 */
public interface AdvisorRegistry {
    public void registAdvisor(Advisor ad);

    public List<Advisor> getAdvisors();

}
