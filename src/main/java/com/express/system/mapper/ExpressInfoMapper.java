package com.express.system.mapper;

import com.express.system.entity.ExpressInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 快递信息表 Mapper 接口
 * </p>
 *
 * @author ikaros
 * @since 2026-02-25
 */
public interface ExpressInfoMapper extends BaseMapper<ExpressInfo> {

    @Select("SELECT * FROM express_info WHERE id = #{id}")
    ExpressInfo selectByIdIncludeDeleted(Long id);

    @Update({
            "<script>",
            "UPDATE express_info",
            "<set>",
            "<if test='trackingNumber != null'>tracking_number = #{trackingNumber},</if>",
            "<if test='logisticsCompany != null'>logistics_company = #{logisticsCompany},</if>",
            "<if test='sizeType != null'>size_type = #{sizeType},</if>",
            "<if test='receiverName != null'>receiver_name = #{receiverName},</if>",
            "<if test='receiverPhone != null'>receiver_phone = #{receiverPhone},</if>",
            "<if test='pickupCode != null'>pickup_code = #{pickupCode},</if>",
            "<if test='shelfCode != null'>shelf_code = #{shelfCode},</if>",
            "<if test='shelfLayer != null'>shelf_layer = #{shelfLayer},</if>",
            "<if test='pickupPhone != null'>pickup_phone = #{pickupPhone},</if>",
            "<if test='status != null'>status = #{status},</if>",
            "<if test='isDeleted != null'>is_deleted = #{isDeleted},</if>",
            "<if test='createTime != null'>create_time = #{createTime},</if>",
            "<if test='updateTime != null'>update_time = #{updateTime},</if>",
            "<if test='remark != null'>remark = #{remark},</if>",
            "</set>",
            "WHERE id = #{id}",
            "</script>"
    })
    int updateByIdIncludeDeleted(ExpressInfo entity);
}
