package com.express.system.dto.query;

import com.express.system.common.page.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "快递分页查询参数")
public class ExpressPageQuery extends PageRequest {

    @Schema(description = "快递单号")
    private String trackingNumber;

    @Schema(description = "收件人手机号")
    private String receiverPhone;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "货架编号")
    private Integer shelfCode;

    @Schema(description = "货架层数")
    private Integer shelfLayer;

    @Schema(description = "快递规格类型")
    private Integer sizeType;

    @Schema(description = "是否仅查询滞留快递")
    private Boolean overdueOnly;

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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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

    public Boolean getOverdueOnly() {
        return overdueOnly;
    }

    public void setOverdueOnly(Boolean overdueOnly) {
        this.overdueOnly = overdueOnly;
    }
}
