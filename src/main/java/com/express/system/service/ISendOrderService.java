package com.express.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
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

    List<SendOrder> listForStaff(Byte status, String senderPhone);

    SendOrder updateStatus(Long id, Byte status);
}
