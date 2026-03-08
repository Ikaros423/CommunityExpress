package com.express.system.service.impl;

import com.express.system.entity.ExpressInfo;
import com.express.system.entity.ShelfInfo;
import com.express.system.mapper.ExpressInfoMapper;
import com.express.system.service.IExpressInfoService;
import com.express.system.service.IShelfInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

/**
 * <p>
 * 快递信息表 服务实现类
 * </p>
 *
 * @author ikaros
 * @since 2026-02-25
 */
@Service
public class ExpressInfoServiceImpl extends ServiceImpl<ExpressInfoMapper, ExpressInfo> implements IExpressInfoService {

    @Autowired
    private IShelfInfoService shelfInfoService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExpressInfo checkIn(ExpressInfo expressInfo) {
        if (expressInfo == null) {
            throw new RuntimeException("入库参数不能为空");
        }
        if (expressInfo.getReceiverPhone() == null || expressInfo.getReceiverPhone().isBlank()) {
            throw new RuntimeException("收件人手机号不能为空");
        }
        if (expressInfo.getSizeType() == null) {
            throw new RuntimeException("快递规格不能为空");
        }

        ShelfInfo shelf;
        if (expressInfo.getShelfId() != null) {
            shelf = shelfInfoService.getById(expressInfo.getShelfId());
            if (shelf == null) {
                throw new RuntimeException("手动选择的货架不存在");
            }
            if (shelf.getStatus() == null || shelf.getStatus() != 1) {
                throw new RuntimeException("手动选择的货架不可用");
            }
        } else {
            shelf = shelfInfoService.getRecommendShelf((int) expressInfo.getSizeType());
            if (shelf == null) {
                throw new RuntimeException("没有可用货架，请先新增或启用货架");
            }
        }

        expressInfo.setShelfId(shelf.getId());
        expressInfo.setShelfLocation(shelf.getShelfCode());
        expressInfo.setPickupCode(generatePickupCode());
        expressInfo.setStatus((byte) 1);

        LocalDateTime now = LocalDateTime.now();
        expressInfo.setCreateTime(now);
        expressInfo.setUpdateTime(now);

        boolean saved = this.save(expressInfo);
        if (!saved) {
            throw new RuntimeException("快递入库失败");
        }

        boolean shelfUpdated = shelfInfoService.updateUsage(shelf.getId(), 1);
        if (!shelfUpdated) {
            throw new RuntimeException("更新货架占用失败");
        }

        return expressInfo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean checkOut(String pickupCode) {
        if (pickupCode == null || pickupCode.isBlank()) {
            throw new RuntimeException("取件码不能为空");
        }

        ExpressInfo expressInfo = this.lambdaQuery()
                .eq(ExpressInfo::getPickupCode, pickupCode.trim())
                .one();

        if (expressInfo == null) {
            throw new RuntimeException("取件码无效或快递不存在");
        }
        if (expressInfo.getStatus() == null || expressInfo.getStatus() != 1) {
            throw new RuntimeException("当前快递状态不允许取件");
        }
        if (expressInfo.getShelfId() == null) {
            throw new RuntimeException("快递未关联货架，无法释放库存");
        }

        expressInfo.setStatus((byte) 2);
        boolean expressUpdated = this.updateById(expressInfo);
        if (!expressUpdated) {
            throw new RuntimeException("更新快递状态失败");
        }

        boolean shelfUpdated = shelfInfoService.updateUsage(expressInfo.getShelfId(), -1);
        if (!shelfUpdated) {
            throw new RuntimeException("释放货架空间失败");
        }

        return true;
    }

    private String generatePickupCode() {
        for (int i = 0; i < 10; i++) {
            String code = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
            long exists = this.lambdaQuery()
                    .eq(ExpressInfo::getPickupCode, code)
                    .count();
            if (exists == 0) {
                return code;
            }
        }
        throw new RuntimeException("生成取件码失败，请重试");
    }
}
