package com.express.system.mapper;

import com.express.system.entity.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 用户信息表 Mapper 接口
 * </p>
 *
 * @author ikaros
 * @since 2026-02-25
 */
public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("SELECT * FROM sys_user WHERE id = #{id}")
    SysUser selectByIdIncludeDeleted(Long id);

    @Update({
            "<script>",
            "UPDATE sys_user",
            "<set>",
            "<if test='username != null'>username = #{username},</if>",
            "<if test='password != null'>password = #{password},</if>",
            "<if test='nickname != null'>nickname = #{nickname},</if>",
            "<if test='email != null'>email = #{email},</if>",
            "<if test='avatar != null'>avatar = #{avatar},</if>",
            "<if test='role != null'>role = #{role},</if>",
            "<if test='status != null'>status = #{status},</if>",
            "<if test='createTime != null'>create_time = #{createTime},</if>",
            "<if test='updateTime != null'>update_time = #{updateTime},</if>",
            "<if test='isDeleted != null'>is_deleted = #{isDeleted},</if>",
            "</set>",
            "WHERE id = #{id}",
            "</script>"
    })
    int updateByIdIncludeDeleted(SysUser entity);
}
