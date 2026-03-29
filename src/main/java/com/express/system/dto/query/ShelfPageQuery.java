package com.express.system.dto.query;

import com.express.system.common.page.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "货架分页查询参数")
public class ShelfPageQuery extends PageRequest {

    @Schema(description = "货架类型")
    private Integer shelfType;

    @Schema(description = "货架状态")
    private Integer status;

    @Schema(description = "货架编号")
    private Integer shelfCode;

    @Schema(description = "货架层数")
    private Integer shelfLayer;

    public Integer getShelfType() {
        return shelfType;
    }

    public void setShelfType(Integer shelfType) {
        this.shelfType = shelfType;
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
}
