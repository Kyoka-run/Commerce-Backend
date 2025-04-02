package com.commerce.project.service;

import com.commerce.project.payload.OrderDTO;
import jakarta.transaction.Transactional;

import java.util.List;

public interface OrderService {
    @Transactional
    OrderDTO placeOrder(String emailId, Long addressId, String paymentMethod, String pgName, String pgPaymentId, String pgStatus, String pgResponseMessage);

    List<OrderDTO> getUserOrders(String emailId);
}

