package com.express.system.controller;

import com.express.system.common.ApiResponse;
import com.express.system.common.exception.BusinessException;
import com.express.system.common.page.PageResponse;
import com.express.system.dto.query.ExpressPageQuery;
import com.express.system.entity.ExpressInfo;
import com.express.system.entity.enums.UserRole;
import com.express.system.security.CurrentUserProvider;
import com.express.system.security.JwtUser;
import com.express.system.service.IExpressInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 快递信息表 前端控制器
 * </p>
 *
 * @author ikaros
 * @since 2026-02-25
 */
@RestController
@RequestMapping("/system/expresses")
@Tag(name = "快递管理")
public class ExpressInfoController {

    private final IExpressInfoService expressInfoService;
    private final CurrentUserProvider currentUserProvider;

    public ExpressInfoController(IExpressInfoService expressInfoService,
                                 CurrentUserProvider currentUserProvider) {
        this.expressInfoService = expressInfoService;
        this.currentUserProvider = currentUserProvider;
    }

    @Operation(summary = "快递列表查询")
    @GetMapping
    public ApiResponse<PageResponse<ExpressInfo>> list(ExpressPageQuery query) {
        if (query == null) {
            query = new ExpressPageQuery();
        }
        JwtUser currentUser = currentUserProvider.getCurrentUserOrNull();
        if (currentUser != null && currentUser.getRole() == UserRole.USER) {
            String phone = currentUser.getUsername();
            if (phone == null || phone.isBlank()) {
                throw BusinessException.badRequest("当前用户未绑定手机号");
            }
            return ApiResponse.success(expressInfoService.pageForUser(
                    currentUser.getUserId(), phone, query.getTrackingNumber(), query.getStatus(),
                    query.getOverdueOnly(), query));
        }
        return ApiResponse.success(expressInfoService.pageByFilter(
                query.getTrackingNumber(), query.getReceiverPhone(), query.getStatus(),
                query.getShelfCode(), query.getShelfLayer(), query.getSizeType(), query.getOverdueOnly(), query));
    }

    @Operation(summary = "快递详情")
    @GetMapping("/{id}")
    public ApiResponse<ExpressInfo> detail(@Parameter(description = "快递ID") @PathVariable("id") Long id) {
        ExpressInfo info = expressInfoService.getById(id);
        if (info == null) {
            throw BusinessException.notFound("快递不存在");
        }
        return ApiResponse.success(info);
    }

    @Operation(summary = "快递入库")
    @PostMapping
    public ApiResponse<ExpressInfo> checkIn(@RequestBody ExpressCheckinRequest expressInfo) {
        return ApiResponse.success("入库成功", expressInfoService.checkIn(expressInfo));
    }

    @Operation(summary = "快递出库")
    @PostMapping("/{trackingNumber}/checkout")
    public ApiResponse<Boolean> checkOut(@Parameter(description = "快递单号") @PathVariable("trackingNumber") String trackingNumber) {
        JwtUser currentUser = currentUserProvider.getCurrentUserOrThrow();
        String operatorPhone = currentUser.getUsername();
        if (operatorPhone == null || operatorPhone.isBlank()) {
            throw BusinessException.badRequest("当前用户未绑定手机号");
        }
        expressInfoService.checkOut(trackingNumber, operatorPhone, currentUser.getRole(), currentUser.getUserId());
        return ApiResponse.success("取件成功", true);
    }

    @Operation(summary = "用户按单号与收件人手机号添加包裹")
    @PostMapping("/claim")
    public ApiResponse<ExpressInfo> claim(@RequestBody ExpressClaimRequest request) {
        JwtUser currentUser = currentUserProvider.requireRole(UserRole.USER);
        String userPhone = currentUser.getUsername();
        if (userPhone == null || userPhone.isBlank()) {
            throw BusinessException.badRequest("当前用户未绑定手机号");
        }
        ExpressInfo expressInfo = expressInfoService.claimForUser(
                currentUser.getUserId(), userPhone, request.getTrackingNumber(), request.getReceiverPhone());
        return ApiResponse.success("添加成功", expressInfo);
    }

    @Operation(summary = "快递更新")
    @PutMapping("/{id}")
    public ApiResponse<ExpressInfo> update(@Parameter(description = "快递ID") @PathVariable("id") Long id,
                                           @RequestBody ExpressInfo expressInfo) {
        expressInfo.setId(id);
        return ApiResponse.success("更新成功", expressInfoService.updateExpress(expressInfo));
    }

    @Operation(summary = "快递删除")
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@Parameter(description = "快递ID") @PathVariable("id") Long id) {
        return ApiResponse.success("删除成功", expressInfoService.deleteExpress(id));
    }

    @Operation(summary = "快递换柜")
    @PostMapping("/{id}/relocate")
    public ApiResponse<ExpressInfo> relocate(@Parameter(description = "快递ID") @PathVariable("id") Long id,
                                             @RequestBody ExpressRelocateRequest request) {
        ExpressInfo updated = expressInfoService.relocateShelf(
                id, request.getShelfCode(), request.getShelfLayer(), request.getSizeType());
        return ApiResponse.success("换柜成功", updated);
    }


    @Schema(description = "快递入库请求")
    public static class ExpressCheckinRequest {
        @Schema(description = "快递单号")
        private String trackingNumber;
        @Schema(description = "物流公司")
        private String logisticsCompany;
        @Schema(description = "快递尺寸类型")
        private Byte sizeType;
        @Schema(description = "收件人姓名")
        private String receiverName;
        @Schema(description = "收件人手机号")
        private String receiverPhone;
        @Schema(description = "货架编号")
        private Integer shelfCode;
        @Schema(description = "货架层数")
        private Integer shelfLayer;
        @Schema(description = "备注")
        private String remark;
        @Schema(description = "是否使用推荐货架")
        private Boolean useRecommendShelf = true;

        public String getTrackingNumber() {
            return trackingNumber;
        }

        public void setTrackingNumber(String trackingNumber) {
            this.trackingNumber = trackingNumber;
        }

        public String getLogisticsCompany() {
            return logisticsCompany;
        }

        public void setLogisticsCompany(String logisticsCompany) {
            this.logisticsCompany = logisticsCompany;
        }

        public Byte getSizeType() {
            return sizeType;
        }

        public void setSizeType(Byte sizeType) {
            this.sizeType = sizeType;
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

        public Integer getShelfCode() {
            return shelfCode;
        }

        public void setShelfCode(Integer shelfCode) {
            this.shelfCode = shelfCode;
        }

        public Integer getShelfLayer() {
            return shelfLayer;
        }

        public void setShelfLayer(Integer shelfLayer) {
            this.shelfLayer = shelfLayer;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public Boolean getUseRecommendShelf() {
            return useRecommendShelf;
        }

        public void setUseRecommendShelf(Boolean useRecommendShelf) {
            this.useRecommendShelf = useRecommendShelf;
        }
    }


    @Schema(description = "快递换柜请求")
    public static class ExpressRelocateRequest {
        @Schema(description = "货架编号")
        private Integer shelfCode;
        @Schema(description = "货架层数")
        private Integer shelfLayer;
        @Schema(description = "快递尺寸类型")
        private Integer sizeType;

        public Integer getShelfCode() {
            return shelfCode;
        }

        public void setShelfCode(Integer shelfCode) {
            this.shelfCode = shelfCode;
        }

        public Integer getShelfLayer() {
            return shelfLayer;
        }

        public void setShelfLayer(Integer shelfLayer) {
            this.shelfLayer = shelfLayer;
        }

        public Integer getSizeType() {
            return sizeType;
        }

        public void setSizeType(Integer sizeType) {
            this.sizeType = sizeType;
        }
    }

    @Schema(description = "用户添加包裹请求")
    public static class ExpressClaimRequest {
        @Schema(description = "快递单号")
        private String trackingNumber;
        @Schema(description = "收件人手机号")
        private String receiverPhone;

        public String getTrackingNumber() {
            return trackingNumber;
        }

        public void setTrackingNumber(String trackingNumber) {
            this.trackingNumber = trackingNumber;
        }

        public String getReceiverPhone() {
            return receiverPhone;
        }

        public void setReceiverPhone(String receiverPhone) {
            this.receiverPhone = receiverPhone;
        }
    }
}
