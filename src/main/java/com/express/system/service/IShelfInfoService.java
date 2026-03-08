package com.express.system.service;

import com.express.system.entity.ShelfInfo;
import com.baomidou.mybatisplus.extension.service.IService;

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

}
