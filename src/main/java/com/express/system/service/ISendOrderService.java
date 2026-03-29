package com.express.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.express.system.common.page.PageRequest;
import com.express.system.common.page.PageResponse;
import com.express.system.entity.SendOrder;

import java.util.List;

public interface ISendOrderService extends IService<SendOrder> {

    SendOrder createForUser(Long userId,
                            String senderPhoneFromAccount,
                            String senderAddress,
                            String receiverName,
                            String receiverPhone,
                            String receiverAddress,
                            Byte packageType,
                            String remark);

    List<SendOrder> listByUser(Long userId, Byte status);

    PageResponse<SendOrder> pageByUser(Long userId, Byte status, PageRequest pageRequest);

    List<SendOrder> listForStaff(Byte status, String senderPhone);

    PageResponse<SendOrder> pageForStaff(Byte status, String senderPhone, PageRequest pageRequest);

    SendOrder updateStatus(Long id, Byte status);
}
