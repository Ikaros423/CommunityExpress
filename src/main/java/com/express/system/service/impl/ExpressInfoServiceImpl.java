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

import com.express.system.controller.ExpressInfoController.ExpressCheckinRequest;

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
    public ExpressInfo checkIn(ExpressCheckinRequest expressInfo) {
        if (expressInfo == null) {
            throw new RuntimeException("入库参数不能为空");
        }
        if (expressInfo.getTrackingNumber() == null || expressInfo.getTrackingNumber().isBlank()) {
            throw new RuntimeException("快递单号不能为空");
        }
        if (expressInfo.getReceiverPhone() == null || expressInfo.getReceiverPhone().isBlank()) {
            throw new RuntimeException("收件人手机号不能为空");
        }
        if (expressInfo.getSizeType() == null) {
            throw new RuntimeException("快递规格不能为空");
        }

        long existing = this.lambdaQuery()
                .eq(ExpressInfo::getTrackingNumber, expressInfo.getTrackingNumber().trim())
                .count();
        if (existing > 0) {
            throw new RuntimeException("该快递单号已存在，请勿重复入库");
        }

        ShelfInfo shelf;
        if (expressInfo.getUseRecommendShelf()) {
            shelf = shelfInfoService.getRecommendShelf((int) expressInfo.getSizeType());
            if (shelf == null) {
                throw new RuntimeException("没有可用货架，请先新增或启用货架");
            }
        } else {
            shelf = shelfInfoService.getByCodeAndLayer(expressInfo.getShelfCode(), expressInfo.getShelfLayer());
            if (shelf == null) {
                throw new RuntimeException("手动选择的货架不存在");
            }
            if (shelf.getStatus() == null || shelf.getStatus() != 1) {
                throw new RuntimeException("手动选择的货架不可用");
            }
        }

        ExpressInfo storeExpressInfo = new ExpressInfo();
        storeExpressInfo.setTrackingNumber(expressInfo.getTrackingNumber());
        storeExpressInfo.setLogisticsCompany(expressInfo.getLogisticsCompany());
        storeExpressInfo.setSizeType(expressInfo.getSizeType());
        storeExpressInfo.setReceiverName(expressInfo.getReceiverName());
        storeExpressInfo.setReceiverPhone(expressInfo.getReceiverPhone());
        storeExpressInfo.setReceiverName(expressInfo.getReceiverPhone());
        storeExpressInfo.setReceiverName(expressInfo.getReceiverPhone());
        storeExpressInfo.setPickupCode(generatePickupCode(shelf.getShelfCode(), shelf.getShelfLayer()));
        storeExpressInfo.setShelfCode(shelf.getShelfCode());
        storeExpressInfo.setShelfLayer(shelf.getShelfLayer());
        storeExpressInfo.setStatus((byte) 1);

        LocalDateTime now = LocalDateTime.now();
        storeExpressInfo.setCreateTime(now);
        storeExpressInfo.setUpdateTime(now);

        boolean saved = this.save(storeExpressInfo);
        if (!saved) {
            throw new RuntimeException("快递入库失败");
        }

        boolean shelfUpdated = shelfInfoService.updateUsage(shelf.getId(), 1);
        if (!shelfUpdated) {
            throw new RuntimeException("更新货架占用失败");
        }

        return storeExpressInfo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean checkOut(String trackingNumber, String pickupPhone) {
        if (trackingNumber == null || trackingNumber.isBlank()) {
            throw new RuntimeException("快递单号不能为空");
        }
        if (pickupPhone == null || pickupPhone.isBlank()) {
            throw new RuntimeException("实际取件人手机号不能为空");
        }

        ExpressInfo expressInfo = this.lambdaQuery()
                .eq(ExpressInfo::getTrackingNumber, trackingNumber.trim())
                .one();

        if (expressInfo == null) {
            throw new RuntimeException("单号或手机号不匹配，无法核销");
        }
        if (expressInfo.getStatus() == null || expressInfo.getStatus() != 1) {
            throw new RuntimeException("当前快递状态不允许取件");
        }
        if (expressInfo.getShelfCode() == null || expressInfo.getShelfLayer() == null) {
            throw new RuntimeException("快递未关联货架，无法释放库存");
        }

        ShelfInfo shelf = shelfInfoService.getByCodeAndLayer(expressInfo.getShelfCode(), expressInfo.getShelfLayer());
        if (shelf == null) {
            throw new RuntimeException("关联货架不存在，无法释放库存");
        }

        expressInfo.setStatus((byte) 2);
        expressInfo.setPickupPhone(pickupPhone.trim());
        expressInfo.setUpdateTime(LocalDateTime.now());
        boolean expressUpdated = this.updateById(expressInfo);
        if (!expressUpdated) {
            throw new RuntimeException("更新快递状态失败");
        }

        boolean shelfUpdated = shelfInfoService.updateUsage(shelf.getId(), -1);
        if (!shelfUpdated) {
            throw new RuntimeException("释放货架空间失败");
        }

        return true;
    }

    private String generatePickupCode(Integer shelfCode, Integer shelfLayer) {
        if (shelfCode == null) {
            throw new RuntimeException("货架编号不能为空");
        }
        if (shelfLayer == null) {
            throw new RuntimeException("货架层不能为空");
        }
        for (int i = 0; i < 10; i++) {
            String random4 = String.format("%04d", ThreadLocalRandom.current().nextInt(0, 10_000));
            String code = shelfCode + "-" + shelfLayer + "-" + random4;
            long exists = this.lambdaQuery()
                    .eq(ExpressInfo::getPickupCode, code)
                    .eq(ExpressInfo::getStatus, 1)
                    .count();
            if (exists == 0) {
                return code;
            }
        }
        throw new RuntimeException("生成取件码失败，请重试");
    }
}
