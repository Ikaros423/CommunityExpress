package com.express.system.service;

import com.express.system.entity.ExpressInfo;
import com.express.system.entity.enums.UserRole;
import com.baomidou.mybatisplus.extension.service.IService;
import com.express.system.common.page.PageRequest;
import com.express.system.common.page.PageResponse;
import com.express.system.controller.ExpressInfoController.ExpressCheckinRequest;

/**
 * <p>
 * 快递信息表 服务类
 * </p>
 *
 * @author ikaros
 * @since 2026-02-25
 */
public interface IExpressInfoService extends IService<ExpressInfo> {

    /**
     * 快递入库
     * @param expressInfo 快递信息
     * @return 入库后的快递记录
     */
    ExpressInfo checkIn(ExpressCheckinRequest expressInfo);

    /**
     * 快递出库核销（单号 + 手机号组合校验）
     * @param trackingNumber 快递单号
     * @param pickupPhone 实际取件人手机号
     * @return 是否核销成功
     */
    boolean checkOut(String trackingNumber, String pickupPhone, UserRole operatorRole, Long operatorUserId);

    /**
     * 更新快递信息
     * @param expressInfo 快递信息（需包含id）
     * @return 更新后的快递记录
     */
    ExpressInfo updateExpress(ExpressInfo expressInfo);

    /**
     * 删除快递信息
     * @param id 快递ID
     * @return 是否删除成功
     */
    boolean deleteExpress(Long id);

    /**
     * 货架移位（换柜）
     * @param id 快递ID
     * @param shelfCode 新货架编号
     * @param shelfLayer 新货架层
     * @return 更新后的快递记录
     */
    ExpressInfo relocateShelf(Long id, Integer shelfCode, Integer shelfLayer, Integer sizeType);

    /**
     * 条件查询快递列表
     * @param trackingNumber 快递单号
     * @param receiverPhone 收件人手机号
     * @param pickupPhone 实际取件人手机号
     * @param status 快递状态
     * @param shelfCode 货架编号
     * @param shelfLayer 货架层
     * @param sizeType 快递规格
     * @param overdueOnly 是否仅查询滞留快递（超过48小时未取）
     * @return 快递列表
     */
    java.util.List<ExpressInfo> listByFilter(String trackingNumber,
                                             String receiverPhone,
                                             Integer status,
                                             Integer shelfCode,
                                             Integer shelfLayer,
                                             Integer sizeType,
                                             Boolean overdueOnly);

    PageResponse<ExpressInfo> pageByFilter(String trackingNumber,
                                           String receiverPhone,
                                           Integer status,
                                           Integer shelfCode,
                                           Integer shelfLayer,
                                           Integer sizeType,
                                           Boolean overdueOnly,
                                           PageRequest pageRequest);

    java.util.List<ExpressInfo> listForUser(Long userId,
                                            String userPhone,
                                            String trackingNumber,
                                            Integer status,
                                            Boolean overdueOnly);

    PageResponse<ExpressInfo> pageForUser(Long userId,
                                          String userPhone,
                                          String trackingNumber,
                                          Integer status,
                                          Boolean overdueOnly,
                                          PageRequest pageRequest);

    ExpressInfo claimForUser(Long userId, String userPhone, String trackingNumber, String receiverPhone);

}
