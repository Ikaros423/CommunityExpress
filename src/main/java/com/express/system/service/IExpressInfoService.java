package com.express.system.service;

import com.express.system.entity.ExpressInfo;
import com.baomidou.mybatisplus.extension.service.IService;

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
    ExpressInfo checkIn(ExpressInfo expressInfo);

    /**
     * 快递出库核销
     * @param pickupCode 取件码
     * @return 是否核销成功
     */
    boolean checkOut(String pickupCode);

}
