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
 * 货架信息表
 * </p>
 *
 * @author ikaros
 * @since 2026-02-25
 */
@Getter
@Setter
@TableName("shelf_info")
@Schema(name = "ShelfInfo", description = "货架信息表")
public class ShelfInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "货架编号(如: 1, 2, 3)")
    @TableField("shelf_code")
    private Integer shelfCode;

    @Schema(description = "货架层数(如: 1, 2, 3)")
    @TableField("shelf_layer")
    private Integer shelfLayer;

    @Schema(description = "货架名称/描述")
    @TableField("shelf_name")
    private String shelfName;

    @Schema(description = "货架类型: 0-标准小件, 1-大件, 2-冷链, 3-易碎")
    @TableField("shelf_type")
    private Byte shelfType;

    @Schema(description = "总格数/容量")
    @TableField("total_capacity")
    private Integer totalCapacity;

    @Schema(description = "当前已占用格数")
    @TableField("current_usage")
    private Integer currentUsage;

    @Schema(description = "所属区域(如: 门口区, 核心仓库区)")
    @TableField("location_area")
    private String locationArea;

    @Schema(description = "推荐优先级(数字越大越优先推荐)")
    @TableField("priority")
    private Integer priority;

    @Schema(description = "货架状态: 0-维修中, 1-可用")
    @TableField("status")
    private Byte status;

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
