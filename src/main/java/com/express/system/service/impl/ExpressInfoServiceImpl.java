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
import java.util.List;
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
        String trackingNumber = safeTrim(expressInfo.getTrackingNumber());
        if (trackingNumber == null || trackingNumber.isBlank()) {
            throw new RuntimeException("快递单号不能为空");
        }
        String receiverPhone = safeTrim(expressInfo.getReceiverPhone());
        if (receiverPhone == null || receiverPhone.isBlank()) {
            throw new RuntimeException("收件人手机号不能为空");
        }
        if (expressInfo.getSizeType() == null) {
            throw new RuntimeException("快递规格不能为空");
        }

        long existing = this.lambdaQuery()
                .eq(ExpressInfo::getTrackingNumber, trackingNumber)
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
        storeExpressInfo.setTrackingNumber(trackingNumber);
        storeExpressInfo.setLogisticsCompany(safeTrim(expressInfo.getLogisticsCompany()));
        storeExpressInfo.setSizeType(expressInfo.getSizeType());
        storeExpressInfo.setReceiverName(safeTrim(expressInfo.getReceiverName()));
        storeExpressInfo.setReceiverPhone(receiverPhone);
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
        String normalizedTrackingNumber = safeTrim(trackingNumber);
        if (normalizedTrackingNumber == null || normalizedTrackingNumber.isBlank()) {
            throw new RuntimeException("快递单号不能为空");
        }
        String normalizedPickupPhone = safeTrim(pickupPhone);
        if (normalizedPickupPhone == null || normalizedPickupPhone.isBlank()) {
            throw new RuntimeException("实际取件人手机号不能为空");
        }

        ExpressInfo expressInfo = this.lambdaQuery()
                .eq(ExpressInfo::getTrackingNumber, normalizedTrackingNumber)
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
        expressInfo.setPickupPhone(normalizedPickupPhone);
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExpressInfo updateExpress(ExpressInfo expressInfo) {
        if (expressInfo == null || expressInfo.getId() == null) {
            throw new RuntimeException("快递ID不能为空");
        }
        ExpressInfo existing = this.getById(expressInfo.getId());
        if (existing == null) {
            throw new RuntimeException("快递不存在");
        }

        String normalizedTrackingNumber = safeTrim(expressInfo.getTrackingNumber());
        if (normalizedTrackingNumber != null) {
            if (normalizedTrackingNumber.isBlank()) {
                throw new RuntimeException("快递单号不能为空");
            }
            if (!normalizedTrackingNumber.equals(existing.getTrackingNumber())) {
                long count = this.lambdaQuery()
                        .eq(ExpressInfo::getTrackingNumber, normalizedTrackingNumber)
                        .ne(ExpressInfo::getId, expressInfo.getId())
                        .count();
                if (count > 0) {
                    throw new RuntimeException("该快递单号已存在");
                }
            }
            expressInfo.setTrackingNumber(normalizedTrackingNumber);
        }

        String normalizedReceiverPhone = safeTrim(expressInfo.getReceiverPhone());
        if (normalizedReceiverPhone != null) {
            expressInfo.setReceiverPhone(normalizedReceiverPhone);
        }
        String normalizedPickupPhone = safeTrim(expressInfo.getPickupPhone());
        if (normalizedPickupPhone != null) {
            expressInfo.setPickupPhone(normalizedPickupPhone);
        }
        String normalizedReceiverName = safeTrim(expressInfo.getReceiverName());
        if (normalizedReceiverName != null) {
            expressInfo.setReceiverName(normalizedReceiverName);
        }
        String normalizedLogisticsCompany = safeTrim(expressInfo.getLogisticsCompany());
        if (normalizedLogisticsCompany != null) {
            expressInfo.setLogisticsCompany(normalizedLogisticsCompany);
        }
        String normalizedRemark = safeTrim(expressInfo.getRemark());
        if (normalizedRemark != null) {
            expressInfo.setRemark(normalizedRemark);
        }

        // 禁止在 update 接口修改与货架相关的字段
        expressInfo.setPickupCode(null);
        expressInfo.setShelfCode(null);
        expressInfo.setShelfLayer(null);
        expressInfo.setSizeType(null);

        expressInfo.setUpdateTime(LocalDateTime.now());
        boolean updated = this.updateById(expressInfo);
        if (!updated) {
            throw new RuntimeException("更新快递失败");
        }
        return this.getById(expressInfo.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteExpress(Long id) {
        if (id == null) {
            throw new RuntimeException("快递ID不能为空");
        }
        ExpressInfo existing = this.getById(id);
        if (existing == null) {
            throw new RuntimeException("快递不存在");
        }

        if (existing.getStatus() != null && existing.getStatus() == 1
                && existing.getShelfCode() != null && existing.getShelfLayer() != null) {
            ShelfInfo shelf = shelfInfoService.getByCodeAndLayer(existing.getShelfCode(), existing.getShelfLayer());
            if (shelf == null) {
                throw new RuntimeException("关联货架不存在，无法释放库存");
            }
            boolean shelfUpdated = shelfInfoService.updateUsage(shelf.getId(), -1);
            if (!shelfUpdated) {
                throw new RuntimeException("释放货架空间失败");
            }
        }

        boolean removed = this.removeById(id);
        if (!removed) {
            throw new RuntimeException("删除快递失败");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExpressInfo relocateShelf(Long id, Integer shelfCode, Integer shelfLayer, Integer sizeType) {
        if (id == null) {
            throw new RuntimeException("快递ID不能为空");
        }
        if (sizeType == null) {
            throw new RuntimeException("快递规格不能为空");
        }

        ExpressInfo expressInfo = this.getById(id);
        if (expressInfo == null) {
            throw new RuntimeException("快递不存在");
        }
        if (expressInfo.getStatus() == null || expressInfo.getStatus() != 1) {
            throw new RuntimeException("当前快递状态不允许换柜");
        }

        ShelfInfo targetShelf;
        if (shelfCode == null || shelfLayer == null) {
            targetShelf = shelfInfoService.getRecommendShelf(sizeType);
            if (targetShelf == null) {
                throw new RuntimeException("没有可用货架");
            }
        } else {
            targetShelf = shelfInfoService.getByCodeAndLayer(shelfCode, shelfLayer);
        }
        if (targetShelf == null) {
            throw new RuntimeException("目标货架不存在");
        }
        if (targetShelf.getStatus() == null || targetShelf.getStatus() != 1) {
            throw new RuntimeException("目标货架不可用");
        }

        if (expressInfo.getShelfCode() != null && expressInfo.getShelfLayer() != null) {
            ShelfInfo currentShelf = shelfInfoService.getByCodeAndLayer(expressInfo.getShelfCode(), expressInfo.getShelfLayer());
            if (currentShelf != null) {
                boolean currentUpdated = shelfInfoService.updateUsage(currentShelf.getId(), -1);
                if (!currentUpdated) {
                    throw new RuntimeException("释放原货架空间失败");
                }
            }
        }

        boolean targetUpdated = shelfInfoService.updateUsage(targetShelf.getId(), 1);
        if (!targetUpdated) {
            throw new RuntimeException("占用目标货架空间失败");
        }

        expressInfo.setSizeType(sizeType.byteValue());
        expressInfo.setShelfCode(targetShelf.getShelfCode());
        expressInfo.setShelfLayer(targetShelf.getShelfLayer());
        expressInfo.setPickupCode(generatePickupCode(targetShelf.getShelfCode(), targetShelf.getShelfLayer()));
        expressInfo.setUpdateTime(LocalDateTime.now());

        boolean updated = this.updateById(expressInfo);
        if (!updated) {
            throw new RuntimeException("换柜更新失败");
        }
        return this.getById(id);
    }

    @Override
    public List<ExpressInfo> listByFilter(String trackingNumber,
                                          String receiverPhone,
                                          Integer status,
                                          Integer shelfCode,
                                          Integer shelfLayer,
                                          Integer sizeType) {
        String normalizedTrackingNumber = safeTrim(trackingNumber);
        String normalizedReceiverPhone = safeTrim(receiverPhone);
        return this.lambdaQuery()
                .eq(status != null, ExpressInfo::getStatus, status)
                .eq(shelfCode != null, ExpressInfo::getShelfCode, shelfCode)
                .eq(shelfLayer != null, ExpressInfo::getShelfLayer, shelfLayer)
                .eq(sizeType != null, ExpressInfo::getSizeType, sizeType)
                .like(normalizedTrackingNumber != null && !normalizedTrackingNumber.isBlank(),
                        ExpressInfo::getTrackingNumber, normalizedTrackingNumber)
                .like(normalizedReceiverPhone != null && !normalizedReceiverPhone.isBlank(),
                        ExpressInfo::getReceiverPhone, normalizedReceiverPhone)
                .orderByDesc(ExpressInfo::getUpdateTime)
                .orderByDesc(ExpressInfo::getCreateTime)
                .list();
    }

    private String safeTrim(String value) {
        return value == null ? null : value.trim();
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
