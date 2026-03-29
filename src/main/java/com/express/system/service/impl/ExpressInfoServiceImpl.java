package com.express.system.service.impl;

import com.express.system.entity.ExpressInfo;
import com.express.system.entity.ExpressUserBinding;
import com.express.system.entity.ShelfInfo;
import com.express.system.entity.enums.UserRole;
import com.express.system.mapper.ExpressInfoMapper;
import com.express.system.mapper.ExpressUserBindingMapper;
import com.express.system.service.IExpressInfoService;
import com.express.system.service.IShelfInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    @Autowired
    private ExpressUserBindingMapper expressUserBindingMapper;

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

        // 选择目标货架：推荐或指定的货架编号+层。
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

        // 构建入库记录（避免直接修改请求对象）。
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

        // 保存成功后占用货架容量。
        boolean shelfUpdated = shelfInfoService.updateUsage(shelf.getId(), 1);
        if (!shelfUpdated) {
            throw new RuntimeException("更新货架占用失败");
        }

        return storeExpressInfo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean checkOut(String trackingNumber, String pickupPhone, UserRole operatorRole, Long operatorUserId) {
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
        if (operatorRole == UserRole.USER) {
            boolean selfReceiver = normalizedPickupPhone.equals(safeTrim(expressInfo.getReceiverPhone()));
            boolean isBound = isExpressBoundToUser(expressInfo.getId(), operatorUserId);
            if (!selfReceiver && !isBound) {
                throw new RuntimeException("该快递不属于当前用户，无法出库");
            }
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

        // 出库成功后释放货架容量。
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

        // update 接口禁止修改货架相关字段，换柜走 relocate。
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

        // 待取件状态删除时先释放货架容量。
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
        // 仅允许待取件状态换柜。
        if (expressInfo.getStatus() == null || expressInfo.getStatus() != 1) {
            throw new RuntimeException("当前快递状态不允许换柜");
        }

        ShelfInfo targetShelf;
        // 未提供货架位置时自动推荐。
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

        // 释放原货架占用（如果存在）。
        if (expressInfo.getShelfCode() != null && expressInfo.getShelfLayer() != null) {
            ShelfInfo currentShelf = shelfInfoService.getByCodeAndLayer(expressInfo.getShelfCode(), expressInfo.getShelfLayer());
            if (currentShelf != null) {
                boolean currentUpdated = shelfInfoService.updateUsage(currentShelf.getId(), -1);
                if (!currentUpdated) {
                    throw new RuntimeException("释放原货架空间失败");
                }
            }
        }

        // 更新快递记录前先占用目标货架。
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
                                          Integer sizeType,
                                          Boolean overdueOnly) {
        String normalizedTrackingNumber = safeTrim(trackingNumber);
        String normalizedReceiverPhone = safeTrim(receiverPhone);
        boolean queryOverdueOnly = Boolean.TRUE.equals(overdueOnly);
        LocalDateTime overdueThreshold = LocalDateTime.now().minus(48, ChronoUnit.HOURS);
        // 根据筛选条件动态构造查询。
        var query = this.lambdaQuery()
                .eq(status != null, ExpressInfo::getStatus, status)
                .eq(shelfCode != null, ExpressInfo::getShelfCode, shelfCode)
                .eq(shelfLayer != null, ExpressInfo::getShelfLayer, shelfLayer)
                .eq(sizeType != null, ExpressInfo::getSizeType, sizeType)
                .like(normalizedTrackingNumber != null && !normalizedTrackingNumber.isBlank(),
                        ExpressInfo::getTrackingNumber, normalizedTrackingNumber)
                .like(normalizedReceiverPhone != null && !normalizedReceiverPhone.isBlank(),
                        ExpressInfo::getReceiverPhone, normalizedReceiverPhone);

        if (queryOverdueOnly) {
            query.eq(ExpressInfo::getStatus, 1)
                    .le(ExpressInfo::getCreateTime, overdueThreshold)
                    .orderByAsc(ExpressInfo::getCreateTime)
                    .orderByAsc(ExpressInfo::getId);
        } else {
            query.orderByDesc(ExpressInfo::getUpdateTime)
                    .orderByDesc(ExpressInfo::getCreateTime);
        }
        return query.list();
    }

    @Override
    public List<ExpressInfo> listForUser(Long userId,
                                         String userPhone,
                                         String trackingNumber,
                                         Integer status,
                                         Boolean overdueOnly) {
        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        String normalizedUserPhone = safeTrim(userPhone);
        if (normalizedUserPhone == null || normalizedUserPhone.isBlank()) {
            throw new RuntimeException("当前用户未绑定手机号");
        }

        String normalizedTrackingNumber = safeTrim(trackingNumber);
        boolean queryOverdueOnly = Boolean.TRUE.equals(overdueOnly);
        LocalDateTime overdueThreshold = LocalDateTime.now().minus(48, ChronoUnit.HOURS);

        List<ExpressUserBinding> bindings = expressUserBindingMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ExpressUserBinding>()
                        .eq("user_id", userId)
                        .eq("is_deleted", 0)
        );
        List<Long> bindingIds = new ArrayList<>();
        for (ExpressUserBinding binding : bindings) {
            if (binding.getExpressId() != null) {
                bindingIds.add(binding.getExpressId());
            }
        }

        var query = this.lambdaQuery()
                .eq(status != null, ExpressInfo::getStatus, status)
                .like(normalizedTrackingNumber != null && !normalizedTrackingNumber.isBlank(),
                        ExpressInfo::getTrackingNumber, normalizedTrackingNumber);

        if (bindingIds.isEmpty()) {
            query.eq(ExpressInfo::getReceiverPhone, normalizedUserPhone);
        } else {
            query.and(wrapper -> wrapper
                    .eq(ExpressInfo::getReceiverPhone, normalizedUserPhone)
                    .or()
                    .in(ExpressInfo::getId, bindingIds));
        }

        if (queryOverdueOnly) {
            query.eq(ExpressInfo::getStatus, 1)
                    .le(ExpressInfo::getCreateTime, overdueThreshold)
                    .orderByAsc(ExpressInfo::getCreateTime)
                    .orderByAsc(ExpressInfo::getId);
        } else {
            query.orderByDesc(ExpressInfo::getUpdateTime)
                    .orderByDesc(ExpressInfo::getCreateTime);
        }

        List<ExpressInfo> list = query.list();
        if (bindingIds.isEmpty()) {
            return list;
        }
        // 去重兜底：同一快递可能同时满足“本人手机号 + 已绑定”。
        Set<Long> seen = new HashSet<>();
        List<ExpressInfo> deduped = new ArrayList<>();
        for (ExpressInfo item : list) {
            if (item.getId() == null || seen.add(item.getId())) {
                deduped.add(item);
            }
        }
        return deduped;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExpressInfo claimForUser(Long userId, String userPhone, String trackingNumber, String receiverPhone) {
        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        String normalizedUserPhone = safeTrim(userPhone);
        String normalizedTrackingNumber = safeTrim(trackingNumber);
        String normalizedReceiverPhone = safeTrim(receiverPhone);
        if (normalizedUserPhone == null || normalizedUserPhone.isBlank()) {
            throw new RuntimeException("当前用户未绑定手机号");
        }
        if (normalizedTrackingNumber == null || normalizedTrackingNumber.isBlank()) {
            throw new RuntimeException("快递单号不能为空");
        }
        if (normalizedReceiverPhone == null || normalizedReceiverPhone.isBlank()) {
            throw new RuntimeException("收件人手机号不能为空");
        }

        ExpressInfo expressInfo = this.lambdaQuery()
                .eq(ExpressInfo::getTrackingNumber, normalizedTrackingNumber)
                .one();
        if (expressInfo == null) {
            throw new RuntimeException("快递不存在");
        }
        if (expressInfo.getStatus() == null || (expressInfo.getStatus() != 0 && expressInfo.getStatus() != 1)) {
            throw new RuntimeException("当前快递状态不支持绑定");
        }
        if (!normalizedReceiverPhone.equals(safeTrim(expressInfo.getReceiverPhone()))) {
            throw new RuntimeException("单号与收件人手机号不匹配");
        }
        if (normalizedUserPhone.equals(safeTrim(expressInfo.getReceiverPhone()))) {
            return expressInfo;
        }

        ExpressUserBinding existingBinding = expressUserBindingMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ExpressUserBinding>()
                        .eq("express_id", expressInfo.getId())
                        .eq("user_id", userId)
                        .eq("is_deleted", 0)
        );
        if (existingBinding != null) {
            return expressInfo;
        }

        ExpressUserBinding binding = new ExpressUserBinding();
        binding.setExpressId(expressInfo.getId());
        binding.setUserId(userId);
        LocalDateTime now = LocalDateTime.now();
        binding.setCreateTime(now);
        binding.setUpdateTime(now);
        binding.setIsDeleted((byte) 0);
        int inserted = expressUserBindingMapper.insert(binding);
        if (inserted <= 0) {
            throw new RuntimeException("添加包裹失败");
        }
        return expressInfo;
    }

    private boolean isExpressBoundToUser(Long expressId, Long userId) {
        if (expressId == null || userId == null) {
            return false;
        }
        ExpressUserBinding binding = expressUserBindingMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ExpressUserBinding>()
                        .eq("express_id", expressId)
                        .eq("user_id", userId)
                        .eq("is_deleted", 0)
        );
        return binding != null;
    }

    private String safeTrim(String value) {
        return value == null ? null : value.trim();
    }

    private String generatePickupCode(Integer shelfCode, Integer shelfLayer) {
        // 生成当前货架+层的唯一取件码。
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
