package com.express.system.service.impl;

import com.express.system.entity.ShelfInfo;
import com.express.system.mapper.ShelfInfoMapper;
import com.express.system.service.IShelfInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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

    @Override
    public List<ShelfInfo> listByFilter(Integer shelfType,
                                        Integer status,
                                        Integer shelfCode,
                                        Integer shelfLayer) {
        return this.lambdaQuery()
                .eq(shelfType != null, ShelfInfo::getShelfType, shelfType)
                .eq(status != null, ShelfInfo::getStatus, status)
                .eq(shelfCode != null, ShelfInfo::getShelfCode, shelfCode)
                .eq(shelfLayer != null, ShelfInfo::getShelfLayer, shelfLayer)
                .orderByAsc(ShelfInfo::getShelfCode, ShelfInfo::getShelfLayer)
                .list();
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

    @Override
    public ShelfInfo getByCodeAndLayer(Integer shelfCode, Integer shelfLayer) {
        if (shelfCode == null || shelfLayer == null) {
            throw new IllegalArgumentException("shelfCode 和 shelfLayer 不能为空");
        }
        return this.lambdaQuery()
                .eq(ShelfInfo::getShelfCode, shelfCode)
                .eq(ShelfInfo::getShelfLayer, shelfLayer)
                .one();
    }

    @Override
    public ShelfInfo getDetail(Long id) {
        if (id == null) {
            throw new RuntimeException("货架ID不能为空");
        }
        ShelfInfo shelf = this.getById(id);
        if (shelf == null) {
            throw new RuntimeException("货架不存在");
        }
        return shelf;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShelfInfo createShelf(ShelfInfo shelfInfo) {
        if (shelfInfo == null) {
            throw new RuntimeException("货架信息不能为空");
        }
        if (shelfInfo.getShelfCode() == null || shelfInfo.getShelfLayer() == null) {
            throw new RuntimeException("货架编号和层数不能为空");
        }
        if (shelfInfo.getShelfType() == null) {
            throw new RuntimeException("货架类型不能为空");
        }
        if (shelfInfo.getTotalCapacity() == null) {
            throw new RuntimeException("货架容量不能为空");
        }
        ShelfInfo existing = getByCodeAndLayer(shelfInfo.getShelfCode(), shelfInfo.getShelfLayer());
        if (existing != null) {
            throw new RuntimeException("该货架编号和层已存在");
        }

        if (shelfInfo.getCurrentUsage() == null) {
            shelfInfo.setCurrentUsage(0);
        }
        if (shelfInfo.getStatus() == null) {
            shelfInfo.setStatus((byte) 1);
        }
        if (shelfInfo.getPriority() == null) {
            shelfInfo.setPriority(0);
        }
        LocalDateTime now = LocalDateTime.now();
        shelfInfo.setCreateTime(now);
        shelfInfo.setUpdateTime(now);

        boolean saved = this.save(shelfInfo);
        if (!saved) {
            throw new RuntimeException("新增货架失败");
        }
        return shelfInfo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShelfInfo updateShelf(ShelfInfo shelfInfo) {
        if (shelfInfo == null || shelfInfo.getId() == null) {
            throw new RuntimeException("货架ID不能为空");
        }
        ShelfInfo existing = this.getById(shelfInfo.getId());
        if (existing == null) {
            throw new RuntimeException("货架不存在");
        }

        if (shelfInfo.getShelfCode() != null && shelfInfo.getShelfLayer() != null) {
            ShelfInfo dup = getByCodeAndLayer(shelfInfo.getShelfCode(), shelfInfo.getShelfLayer());
            if (dup != null && !dup.getId().equals(shelfInfo.getId())) {
                throw new RuntimeException("该货架编号和层已存在");
            }
        }

        shelfInfo.setUpdateTime(LocalDateTime.now());
        boolean updated = this.updateById(shelfInfo);
        if (!updated) {
            throw new RuntimeException("更新货架失败");
        }
        return this.getById(shelfInfo.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteShelf(Long id) {
        if (id == null) {
            throw new RuntimeException("货架ID不能为空");
        }
        ShelfInfo shelf = this.getById(id);
        if (shelf == null) {
            throw new RuntimeException("货架不存在");
        }
        boolean removed = this.removeById(id);
        if (!removed) {
            throw new RuntimeException("删除货架失败");
        }
        return true;
    }
}
