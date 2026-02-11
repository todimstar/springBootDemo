package com.liu.springbootdemo.service;

import com.liu.springbootdemo.POJO.entity.User;

/**
 * 用户画像风险加权服务
 * 根据用户画像因子计算风险评分的加权系数
 */
public interface UserRiskWeightService {

    /**
     * 计算用户画像加权系数
     * 取所有命中因子中最大的加权系数，默认为 1.0
     *
     * @param user 用户信息
     * @return 加权系数（>= 1.0）
     */
    double calculateMultiplier(User user);
}
