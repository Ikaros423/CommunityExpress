package com.express.system.controller;

import com.express.system.common.ApiResponse;
import com.express.system.entity.ExpressInfo;
import com.express.system.service.IExpressInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 快递信息表 前端控制器
 * </p>
 *
 * @author ikaros
 * @since 2026-02-25
 */
@RestController
@RequestMapping("/system/expressInfo")
public class ExpressInfoController {

    @Autowired
    private IExpressInfoService expressInfoService;

    @GetMapping("/list")
    public ApiResponse<List<ExpressInfo>> list(
            @RequestParam(value = "trackingNumber", required = false) String trackingNumber,
            @RequestParam(value = "receiverPhone", required = false) String receiverPhone,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "shelfCode", required = false) Integer shelfCode,
            @RequestParam(value = "shelfLayer", required = false) Integer shelfLayer,
            @RequestParam(value = "sizeType", required = false) Integer sizeType) {
        return ApiResponse.success(expressInfoService.listByFilter(
                trackingNumber, receiverPhone, status, shelfCode, shelfLayer, sizeType));
    }

    @PostMapping("/checkin")
    public ApiResponse<ExpressInfo> checkIn(@RequestBody ExpressCheckinRequest expressInfo) {
        return ApiResponse.success("入库成功", expressInfoService.checkIn(expressInfo));
    }

    @PostMapping("/checkout")
    public ApiResponse<Boolean> checkOut(@RequestBody ExpressCheckoutRequest request) {
        expressInfoService.checkOut(request.getTrackingNumber(), request.getPickupPhone());
        return ApiResponse.success("取件成功", true);
    }

    @PostMapping("/update")
    public ApiResponse<ExpressInfo> update(@RequestBody ExpressInfo expressInfo) {
        return ApiResponse.success("更新成功", expressInfoService.updateExpress(expressInfo));
    }

    @PostMapping("/delete")
    public ApiResponse<Boolean> delete(@RequestParam("id") Long id) {
        return ApiResponse.success("删除成功", expressInfoService.deleteExpress(id));
    }

    @PostMapping("/relocate")
    public ApiResponse<ExpressInfo> relocate(@RequestBody ExpressRelocateRequest request) {
        ExpressInfo updated = expressInfoService.relocateShelf(
                request.getId(), request.getShelfCode(), request.getShelfLayer(), request.getSizeType());
        return ApiResponse.success("换柜成功", updated);
    }


    public static class ExpressCheckinRequest {
        private String trackingNumber;
        private String logisticsCompany;
        private Byte sizeType;
        private String receiverName;
        private String receiverPhone;
        private Integer shelfCode;
        private Integer shelfLayer;
        private String remark;
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


    public static class ExpressCheckoutRequest {
        private String trackingNumber;
        private String pickupPhone;

        public String getTrackingNumber() {
            return trackingNumber;
        }

        public void setTrackingNumber(String trackingNumber) {
            this.trackingNumber = trackingNumber;
        }

        public String getPickupPhone() {
            return pickupPhone;
        }

        public void setPickupPhone(String pickupPhone) {
            this.pickupPhone = pickupPhone;
        }
    }

    public static class ExpressRelocateRequest {
        private Long id;
        private Integer shelfCode;
        private Integer shelfLayer;
        private Integer sizeType;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
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

        public Integer getSizeType() {
            return sizeType;
        }

        public void setSizeType(Integer sizeType) {
            this.sizeType = sizeType;
        }
    }
}
