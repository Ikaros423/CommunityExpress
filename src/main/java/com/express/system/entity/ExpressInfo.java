package com.express.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 快递信息表
 * </p>
 *
 * @author ikaros
 * @since 2026-02-25
 */
@Getter
@Setter
@TableName("express_info")
@Schema(name = "ExpressInfo", description = "快递信息表")
public class ExpressInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "快递单号")
    @TableField("tracking_number")
    private String trackingNumber;

    @Schema(description = "物流公司(如：顺丰、中通)")
    @TableField("logistics_company")
    private String logisticsCompany;

    @Schema(description = "规格,0-标准(小件),1-大件,2-冷链,3-易碎")
    @TableField("size_type")
    private Byte sizeType;

    @Schema(description = "收件人姓名")
    @TableField("receiver_name")
    private String receiverName;

    @Schema(description = "收件人手机号")
    @TableField("receiver_phone")
    private String receiverPhone;

    @Schema(description = "取件码(货架号-层-随机4位)")
    @TableField("pickup_code")
    private String pickupCode;

    @Schema(description = "货架code")
    @TableField("shelf_code")
    private Integer shelfCode;

    @Schema(description = "货架layer")
    @TableField("shelf_layer")
    private Integer shelfLayer;

    @Schema(description = "实际取件人手机号")
    @TableField("pickup_phone")
    private String pickupPhone;

    @Schema(description = "状态: 0-待入库, 1-待取件, 2-已取件, 3-已退回")
    @TableField("status")
    private Byte status;

    @Schema(description = "逻辑删除: 0-未删除, 1-已删除")
    @TableField("is_deleted")
    @TableLogic
    private Byte isDeleted;

    @Schema(description = "入库时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField("update_time")
    private LocalDateTime updateTime;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;
}
