package com.express.system.service;

import com.express.system.dto.ShelfLoadVO;
import com.express.system.entity.ShelfInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.express.system.common.page.PageRequest;
import com.express.system.common.page.PageResponse;

/**
 * <p>
 * 货架信息表 服务类
 * </p>
 *
 * @author ikaros
 * @since 2026-02-25
 */
public interface IShelfInfoService extends IService<ShelfInfo> {

    /**
     * 推荐货架
     * @param sizeType 快递规格
     * @return 推荐货架
     */
    ShelfInfo getRecommendShelf(Integer sizeType);

    /**
     * 更新货架占用量
     * @param shelfId 货架ID
     * @param delta 变化量
     * @return 是否更新成功
     */
    boolean updateUsage(Long shelfId, int delta);

    /**
     * 根据货架编码和层获取货架信息
     * @param shelfCode 货架编码
     * @param shelfLayer 货架层
     * @return 货架信息
     */
    ShelfInfo getByCodeAndLayer(Integer shelfCode, Integer shelfLayer);

    /**
     * 条件查询货架列表
     * @param shelfType 货架类型
     * @param status 货架状态
     * @param shelfCode 货架编号
     * @param shelfLayer 货架层
     * @param locationArea 所属区域
     * @return 货架列表
     */
    java.util.List<ShelfInfo> listByFilter(Integer shelfType,
                                           Integer status,
                                           Integer shelfCode,
                                           Integer shelfLayer);

    PageResponse<ShelfInfo> pageByFilter(Integer shelfType,
                                         Integer status,
                                         Integer shelfCode,
                                         Integer shelfLayer,
                                         PageRequest pageRequest);

    java.util.List<ShelfLoadVO> listLoadByFilter(Integer shelfType,
                                                 Integer status,
                                                 Integer shelfCode,
                                                 Integer shelfLayer);

    /**
     * 获取货架详情
     * @param id 货架ID
     * @return 货架信息
     */
    ShelfInfo getDetail(Long id);

    /**
     * 新增货架
     * @param shelfInfo 货架信息
     * @return 新增后的货架
     */
    ShelfInfo createShelf(ShelfInfo shelfInfo);

    /**
     * 更新货架
     * @param shelfInfo 货架信息
     * @return 更新后的货架
     */
    ShelfInfo updateShelf(ShelfInfo shelfInfo);

    /**
     * 删除货架
     * @param id 货架ID
     * @return 是否删除成功
     */
    boolean deleteShelf(Long id);

}
