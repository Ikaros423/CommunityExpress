package com.express.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("send_order")
@Schema(name = "SendOrder", description = "寄件申请单")
public class SendOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "申请用户ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "寄件人手机号")
    @TableField("sender_phone")
    private String senderPhone;

    @Schema(description = "寄件地址")
    @TableField("sender_address")
    private String senderAddress;

    @Schema(description = "收件人姓名")
    @TableField("receiver_name")
    private String receiverName;

    @Schema(description = "收件人手机号")
    @TableField("receiver_phone")
    private String receiverPhone;

    @Schema(description = "收件地址")
    @TableField("receiver_address")
    private String receiverAddress;

    @Schema(description = "包裹类型：0-标准，1-大件，2-冷链，3-易碎")
    @TableField("package_type")
    private Byte packageType;

    @Schema(description = "状态：0-待处理，1-已受理，2-已寄出，3-已取消")
    @TableField("status")
    private Byte status;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField("update_time")
    private LocalDateTime updateTime;

    @Schema(description = "逻辑删除")
    @TableField("is_deleted")
    @TableLogic
    private Byte isDeleted;
}
