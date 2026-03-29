package com.express.system.dto.query;

import com.express.system.common.page.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "寄件申请分页查询参数")
public class SendOrderPageQuery extends PageRequest {

    @Schema(description = "状态筛选")
    private Byte status;

    @Schema(description = "寄件人手机号筛选（员工/管理员）")
    private String senderPhone;

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public String getSenderPhone() {
        return senderPhone;
    }

    public void setSenderPhone(String senderPhone) {
        this.senderPhone = senderPhone;
    }
}
