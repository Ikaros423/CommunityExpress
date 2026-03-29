package com.express.system.service;

import com.express.system.entity.SendOrder;
import com.express.system.service.impl.SendOrderServiceImpl;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SendOrderServiceImplTest {

    @Test
    void updateStatusShouldRejectInvalidTransition() {
        TestableSendOrderService service = new TestableSendOrderService();
        SendOrder order = new SendOrder();
        order.setId(1L);
        order.setStatus((byte) 1);
        service.setStoredOrder(order);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.updateStatus(1L, (byte) 0));
        assertEquals("寄件状态流转不合法", ex.getMessage());
    }

    @Test
    void updateStatusShouldAllowForwardTransition() {
        TestableSendOrderService service = new TestableSendOrderService();
        SendOrder order = new SendOrder();
        order.setId(1L);
        order.setStatus((byte) 0);
        service.setStoredOrder(order);

        SendOrder updated = service.updateStatus(1L, (byte) 1);
        assertEquals((byte) 1, updated.getStatus());
    }

    private static class TestableSendOrderService extends SendOrderServiceImpl {
        private SendOrder storedOrder;

        void setStoredOrder(SendOrder order) {
            this.storedOrder = order;
        }

        @Override
        public SendOrder getById(Serializable id) {
            return storedOrder;
        }

        @Override
        public boolean updateById(SendOrder entity) {
            this.storedOrder = entity;
            return true;
        }
    }
}
