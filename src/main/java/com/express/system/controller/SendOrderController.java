package com.express.system.controller;

import com.express.system.common.ApiResponse;
import com.express.system.entity.SendOrder;
import com.express.system.entity.enums.UserRole;
import com.express.system.security.JwtUser;
import com.express.system.service.ISendOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/system/send-orders")
@Tag(name = "寄件申请")
@Validated
public class SendOrderController {

    @Autowired
    private ISendOrderService sendOrderService;

    @Operation(summary = "创建寄件申请")
    @PostMapping
    public ApiResponse<SendOrder> create(@Valid @RequestBody SendOrderCreateRequest request) {
        JwtUser currentUser = getCurrentUser();
        if (currentUser == null || currentUser.getRole() != UserRole.USER) {
            throw new RuntimeException("仅普通用户可提交寄件申请");
        }
        SendOrder created = sendOrderService.createForUser(
                currentUser.getUserId(),
                currentUser.getUsername(),
                request.getSenderAddress(),
                request.getReceiverName(),
                request.getReceiverPhone(),
                request.getReceiverAddress(),
                request.getPackageType(),
                request.getRemark()
        );
        return ApiResponse.success("提交成功", created);
    }

    @Operation(summary = "寄件申请列表查询")
    @GetMapping
    public ApiResponse<List<SendOrder>> list(
            @Parameter(description = "状态筛选") @RequestParam(value = "status", required = false) Byte status,
            @Parameter(description = "寄件人手机号筛选（员工/管理员）")
            @RequestParam(value = "senderPhone", required = false) String senderPhone) {
        JwtUser currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("未登录或登录已过期");
        }
        if (currentUser.getRole() == UserRole.USER) {
            return ApiResponse.success(sendOrderService.listByUser(currentUser.getUserId(), status));
        }
        return ApiResponse.success(sendOrderService.listForStaff(status, senderPhone));
    }

    @Operation(summary = "更新寄件申请状态")
    @PutMapping("/{id}/status")
    public ApiResponse<SendOrder> updateStatus(@PathVariable("id") Long id,
                                               @Valid @RequestBody SendOrderStatusUpdateRequest request) {
        JwtUser currentUser = getCurrentUser();
        if (currentUser == null || (currentUser.getRole() != UserRole.STAFF && currentUser.getRole() != UserRole.ADMIN)) {
            throw new RuntimeException("仅员工或管理员可更新寄件状态");
        }
        return ApiResponse.success("更新成功", sendOrderService.updateStatus(id, request.getStatus()));
    }

    @Schema(description = "创建寄件申请请求")
    public static class SendOrderCreateRequest {
        @Schema(description = "寄件人手机号（兼容字段，后端按当前登录账号手机号为准）")
        private String senderPhone;

        @Schema(description = "寄件地址")
        @NotBlank(message = "寄件地址不能为空")
        private String senderAddress;

        @Schema(description = "收件人姓名")
        @NotBlank(message = "收件人姓名不能为空")
        private String receiverName;

        @Schema(description = "收件人手机号")
        @NotBlank(message = "收件人手机号不能为空")
        @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
        private String receiverPhone;

        @Schema(description = "收件地址")
        @NotBlank(message = "收件地址不能为空")
        private String receiverAddress;

        @Schema(description = "包裹类型：0-标准，1-大件，2-冷链，3-易碎")
        @NotNull(message = "包裹类型不能为空")
        private Byte packageType;

        @Schema(description = "备注")
        private String remark;

        public String getSenderPhone() {
            return senderPhone;
        }

        public void setSenderPhone(String senderPhone) {
            this.senderPhone = senderPhone;
        }

        public String getSenderAddress() {
            return senderAddress;
        }

        public void setSenderAddress(String senderAddress) {
            this.senderAddress = senderAddress;
        }

        public String getReceiverName() {
            return receiverName;
        }

        public void setReceiverName(String receiverName) {
            this.receiverName = receiverName;
        }

        public String getReceiverPhone() {
            return receiverPhone;
        }

        public void setReceiverPhone(String receiverPhone) {
            this.receiverPhone = receiverPhone;
        }

        public String getReceiverAddress() {
            return receiverAddress;
        }

        public void setReceiverAddress(String receiverAddress) {
            this.receiverAddress = receiverAddress;
        }

        public Byte getPackageType() {
            return packageType;
        }

        public void setPackageType(Byte packageType) {
            this.packageType = packageType;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }
    }

    @Schema(description = "更新寄件状态请求")
    public static class SendOrderStatusUpdateRequest {
        @Schema(description = "状态：0-待处理，1-已受理，2-已寄出，3-已取消")
        @NotNull(message = "状态不能为空")
        private Byte status;

        public Byte getStatus() {
            return status;
        }

        public void setStatus(Byte status) {
            this.status = status;
        }
    }

    private JwtUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtUser)) {
            return null;
        }
        return (JwtUser) authentication.getPrincipal();
    }
}
