package com.express.system.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.express.system.common.exception.BusinessException;
import com.express.system.common.page.PageRequest;
import com.express.system.common.page.PageResponse;
import com.express.system.entity.SendOrder;
import com.express.system.mapper.SendOrderMapper;
import com.express.system.service.ISendOrderService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SendOrderServiceImpl extends ServiceImpl<SendOrderMapper, SendOrder> implements ISendOrderService {

    @Override
    public SendOrder createForUser(Long userId,
                                   String senderPhoneFromAccount,
                                   String senderAddress,
                                   String receiverName,
                                   String receiverPhone,
                                   String receiverAddress,
                                   Byte packageType,
                                   String remark) {
        if (userId == null) {
            throw BusinessException.badRequest("用户ID不能为空");
        }
        String normalizedSenderPhone = safeTrim(senderPhoneFromAccount);
        String normalizedSenderAddress = safeTrim(senderAddress);
        String normalizedReceiverName = safeTrim(receiverName);
        String normalizedReceiverPhone = safeTrim(receiverPhone);
        String normalizedReceiverAddress = safeTrim(receiverAddress);
        if (normalizedSenderPhone == null || normalizedSenderPhone.isBlank()) {
            throw BusinessException.badRequest("寄件人手机号不能为空");
        }
        if (normalizedSenderAddress == null || normalizedSenderAddress.isBlank()) {
            throw BusinessException.badRequest("寄件地址不能为空");
        }
        if (normalizedReceiverName == null || normalizedReceiverName.isBlank()) {
            throw BusinessException.badRequest("收件人姓名不能为空");
        }
        if (normalizedReceiverPhone == null || normalizedReceiverPhone.isBlank()) {
            throw BusinessException.badRequest("收件人手机号不能为空");
        }
        if (normalizedReceiverAddress == null || normalizedReceiverAddress.isBlank()) {
            throw BusinessException.badRequest("收件地址不能为空");
        }
        if (packageType == null || packageType < 0 || packageType > 3) {
            throw BusinessException.badRequest("包裹类型不正确");
        }

        SendOrder order = new SendOrder();
        order.setUserId(userId);
        order.setSenderPhone(normalizedSenderPhone);
        order.setSenderAddress(normalizedSenderAddress);
        order.setReceiverName(normalizedReceiverName);
        order.setReceiverPhone(normalizedReceiverPhone);
        order.setReceiverAddress(normalizedReceiverAddress);
        order.setPackageType(packageType);
        order.setRemark(safeTrim(remark));
        order.setStatus((byte) 0);
        LocalDateTime now = LocalDateTime.now();
        order.setCreateTime(now);
        order.setUpdateTime(now);

        boolean saved = this.save(order);
        if (!saved) {
            throw BusinessException.badRequest("提交寄件申请失败");
        }
        return order;
    }

    @Override
    public List<SendOrder> listByUser(Long userId, Byte status) {
        if (userId == null) {
            throw BusinessException.badRequest("用户ID不能为空");
        }
        return buildUserQuery(userId, status)
                .list();
    }

    @Override
    public PageResponse<SendOrder> pageByUser(Long userId, Byte status, PageRequest pageRequest) {
        long pageNo = pageRequest == null ? 1L : pageRequest.safePage();
        long pageSize = pageRequest == null ? 15L : pageRequest.safePageSize();
        Page<SendOrder> page = new Page<>(pageNo, pageSize);
        buildUserQuery(userId, status).page(page);
        return PageResponse.of(page.getRecords(), page.getTotal(), pageNo, pageSize);
    }

    @Override
    public List<SendOrder> listForStaff(Byte status, String senderPhone) {
        String normalizedSenderPhone = safeTrim(senderPhone);
        return buildStaffQuery(status, normalizedSenderPhone)
                .list();
    }

    @Override
    public PageResponse<SendOrder> pageForStaff(Byte status, String senderPhone, PageRequest pageRequest) {
        long pageNo = pageRequest == null ? 1L : pageRequest.safePage();
        long pageSize = pageRequest == null ? 15L : pageRequest.safePageSize();
        Page<SendOrder> page = new Page<>(pageNo, pageSize);
        buildStaffQuery(status, safeTrim(senderPhone)).page(page);
        return PageResponse.of(page.getRecords(), page.getTotal(), pageNo, pageSize);
    }

    private com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper<SendOrder> buildUserQuery(Long userId, Byte status) {
        if (userId == null) {
            throw BusinessException.badRequest("用户ID不能为空");
        }
        return this.lambdaQuery()
                .eq(SendOrder::getUserId, userId)
                .eq(status != null, SendOrder::getStatus, status)
                .orderByDesc(SendOrder::getUpdateTime)
                .orderByDesc(SendOrder::getCreateTime);
    }

    private com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper<SendOrder> buildStaffQuery(Byte status, String normalizedSenderPhone) {
        return this.lambdaQuery()
                .eq(status != null, SendOrder::getStatus, status)
                .like(normalizedSenderPhone != null && !normalizedSenderPhone.isBlank(),
                        SendOrder::getSenderPhone, normalizedSenderPhone)
                .orderByDesc(SendOrder::getUpdateTime)
                .orderByDesc(SendOrder::getCreateTime);
    }

    @Override
    public SendOrder updateStatus(Long id, Byte status) {
        if (id == null) {
            throw BusinessException.badRequest("寄件单ID不能为空");
        }
        if (status == null || status < 0 || status > 3) {
            throw BusinessException.badRequest("寄件状态不正确");
        }
        SendOrder existing = this.getById(id);
        if (existing == null) {
            throw BusinessException.badRequest("寄件单不存在");
        }
        Byte currentStatus = existing.getStatus();
        if (!isValidStatusTransition(currentStatus, status)) {
            throw BusinessException.badRequest("寄件状态流转不合法");
        }
        existing.setStatus(status);
        existing.setUpdateTime(LocalDateTime.now());
        boolean updated = this.updateById(existing);
        if (!updated) {
            throw BusinessException.badRequest("更新寄件状态失败");
        }
        return this.getById(id);
    }

    private String safeTrim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isValidStatusTransition(Byte currentStatus, Byte targetStatus) {
        if (currentStatus == null) {
            return false;
        }
        if (currentStatus.equals(targetStatus)) {
            return true;
        }
        // 0=待处理, 1=已受理, 2=已寄出, 3=已取消
        if (currentStatus == 0) {
            return targetStatus == 1 || targetStatus == 3;
        }
        if (currentStatus == 1) {
            return targetStatus == 2 || targetStatus == 3;
        }
        return false;
    }
}
