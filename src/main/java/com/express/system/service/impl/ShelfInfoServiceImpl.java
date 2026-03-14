package com.express.system.service.impl;

import com.express.system.entity.ShelfInfo;
import com.express.system.mapper.ShelfInfoMapper;
import com.express.system.service.IShelfInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.lang.Integer.max;

/**
 * <p>
 * 货架信息表 服务实现类
 * </p>
 *
 * @author ikaros
 * @since 2026-02-25
 */
@Service
public class ShelfInfoServiceImpl extends ServiceImpl<ShelfInfoMapper, ShelfInfo> implements IShelfInfoService {

    @Override
    public ShelfInfo getRecommendShelf(Integer sizeType) {
        return this.lambdaQuery()
                .eq(ShelfInfo::getShelfType, sizeType)
                .eq(ShelfInfo::getStatus, 1)
                // 排序规则：
                // 第一优先：未满的货架，按占用率升序
                // 第二优先：已满的货架，按超限数升序（谁超的最少推荐谁）
                .last("ORDER BY CASE WHEN current_usage < total_capacity THEN 0 ELSE 1 END, " +
                        "(current_usage * 1.0 / total_capacity) ASC LIMIT 1")
                .one();
    }

    /**
     * 更新货架使用量
     * @param shelfId 货架ID
     * @param delta 变化量：入库传 1，出库传 -1
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateUsage(Long shelfId, int delta) {
        ShelfInfo shelf = this.getById(shelfId);
        if (shelf == null) throw new RuntimeException("货架不存在");

        int currentUsage = shelf.getCurrentUsage() == null ? 0 : shelf.getCurrentUsage();
        // 1. 防止实际容量小于0
        int newUsage = max(currentUsage + delta, 0);

        // 2. 超过最大容量时，我们只在日志或业务层做记录，不抛异常
        if (newUsage > shelf.getTotalCapacity()) {
            // 这里可以记录一个警告日志，或者在前端返回一个“超额存储”的状态
            System.out.println("警告：货架 " + shelf.getShelfCode() + shelf.getShelfLayer() + " 已超负荷存储！");
        }

        // 3. 执行更新
        return this.update()
                .setSql("current_usage = CASE WHEN IFNULL(current_usage, 0) + " + delta + " < 0 THEN 0 ELSE IFNULL(current_usage, 0) + " + delta + " END")
                .eq("id", shelfId)
                .update();
    }

    public ShelfInfo getByCodeAndLayer(Integer shelfCode, Integer shelfLayer) {
        if (shelfCode == null || shelfLayer == null) {
            throw new IllegalArgumentException("shelfCode 和 shelfLayer 不能为空");
        }
        return this.lambdaQuery()
                .eq(ShelfInfo::getShelfCode, shelfCode)
                .eq(ShelfInfo::getShelfLayer, shelfLayer)
                .one();
    }
}
