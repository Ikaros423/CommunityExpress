package com.express.system.service;

import com.express.system.entity.ExpressInfo;
import com.baomidou.mybatisplus.extension.service.IService;
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
    boolean checkOut(String trackingNumber, String pickupPhone);

}
