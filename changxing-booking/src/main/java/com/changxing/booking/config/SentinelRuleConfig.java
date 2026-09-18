package com.changxing.booking.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

// Sentinel 限流规则：代码方式加载（生产通常走控制台动态下发）
@Configuration
public class SentinelRuleConfig {

    @PostConstruct
    public void initRules() {
        // 1) 下单接口整体 QPS 限流：超阈值快速失败，不排队
        FlowRule flowRule = new FlowRule("createBooking");
        flowRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        flowRule.setCount(100);
        flowRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
        FlowRuleManager.loadRules(Collections.singletonList(flowRule));

        // 2) 热点参数限流：对热门车型 trimId 单独限流
        //    说明：热点参数要求"热点 key"是方法直接入参；
        //    这里以 checkAvailability(参数索引0=trimId) 为示例，生产下单入口可把 carId 提为直接入参，或经网关按 carId 维度配置
        ParamFlowRule paramRule = new ParamFlowRule("checkAvailability");
        paramRule.setParamIdx(0);
        paramRule.setCount(20);
        ParamFlowRuleManager.loadRules(Collections.singletonList(paramRule));
    }
}