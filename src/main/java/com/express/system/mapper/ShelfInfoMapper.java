package com.express.system.mapper;

import com.express.system.entity.ShelfInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 货架信息表 Mapper 接口
 * </p>
 *
 * @author ikaros
 * @since 2026-02-25
 */
public interface ShelfInfoMapper extends BaseMapper<ShelfInfo> {

    @Select("SELECT * FROM shelf_info WHERE id = #{id}")
    ShelfInfo selectByIdIncludeDeleted(Long id);

    @Update({
            "<script>",
            "UPDATE shelf_info",
            "<set>",
            "<if test='shelfCode != null'>shelf_code = #{shelfCode},</if>",
            "<if test='shelfLayer != null'>shelf_layer = #{shelfLayer},</if>",
            "<if test='shelfName != null'>shelf_name = #{shelfName},</if>",
            "<if test='shelfType != null'>shelf_type = #{shelfType},</if>",
            "<if test='totalCapacity != null'>total_capacity = #{totalCapacity},</if>",
            "<if test='currentUsage != null'>current_usage = #{currentUsage},</if>",
            "<if test='locationArea != null'>location_area = #{locationArea},</if>",
            "<if test='priority != null'>priority = #{priority},</if>",
            "<if test='status != null'>status = #{status},</if>",
            "<if test='createTime != null'>create_time = #{createTime},</if>",
            "<if test='updateTime != null'>update_time = #{updateTime},</if>",
            "<if test='isDeleted != null'>is_deleted = #{isDeleted},</if>",
            "</set>",
            "WHERE id = #{id}",
            "</script>"
    })
    int updateByIdIncludeDeleted(ShelfInfo entity);
}
