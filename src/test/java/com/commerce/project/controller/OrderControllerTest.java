package com.commerce.project.controller;

import com.commerce.project.Util.AuthUtil;
import com.commerce.project.payload.OrderDTO;
import com.commerce.project.payload.OrderRequestDTO;
import com.commerce.project.payload.StripePaymentDTO;
import com.commerce.project.service.OrderService;
import com.commerce.project.service.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private AuthUtil authUtil;

    @Mock
    private StripeService stripeService;

    @Mock
    private PaymentIntent paymentIntent;

    @InjectMocks
    private OrderController orderController;

    private OrderDTO orderDTO;
    private OrderRequestDTO orderRequestDTO;
    private StripePaymentDTO stripePaymentDTO;
    private String email;
    private Long addressId;
    private String paymentMethod;

    @BeforeEach
    void setUp() {
        // Set up test data
        email = "test@example.com";
        addressId = 1L;
        paymentMethod = "Stripe";

        orderDTO = new OrderDTO();
        orderDTO.setOrderId(1L);
        orderDTO.setEmail(email);
        orderDTO.setTotalAmount(100.0);
        orderDTO.setOrderStatus("Order Accepted");
        orderDTO.setAddressId(addressId);

        orderRequestDTO = new OrderRequestDTO();
        orderRequestDTO.setAddressId(addressId);
        orderRequestDTO.setPaymentMethod(paymentMethod);
        orderRequestDTO.setPgName("Stripe");
        orderRequestDTO.setPgPaymentId("pi_123456");
        orderRequestDTO.setPgStatus("succeeded");
        orderRequestDTO.setPgResponseMessage("Payment successful");

        stripePaymentDTO = new StripePaymentDTO();
        stripePaymentDTO.setAmount(10000L);
        stripePaymentDTO.setCurrency("usd");
    }

    @Test
    void orderProducts_ShouldReturnOrderDTO() {
        // Arrange
        when(authUtil.loggedInEmail()).thenReturn(email);
        when(orderService.placeOrder(
                anyString(), anyLong(), anyString(), anyString(),
                anyString(), anyString(), anyString()
        )).thenReturn(orderDTO);

        // Act
        ResponseEntity<OrderDTO> response = orderController.orderProducts(
                paymentMethod, orderRequestDTO
        );

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(orderDTO, response.getBody());
        verify(authUtil, times(1)).loggedInEmail();
        verify(orderService, times(1)).placeOrder(
                email,
                orderRequestDTO.getAddressId(),
                paymentMethod,
                orderRequestDTO.getPgName(),
                orderRequestDTO.getPgPaymentId(),
                orderRequestDTO.getPgStatus(),
                orderRequestDTO.getPgResponseMessage()
        );
    }

    @Test
    void createStripeClientSecret_ShouldReturnClientSecret() throws StripeException {
        // Arrange
        String clientSecret = "cs_test_123456";
        when(stripeService.paymentIntent(any(StripePaymentDTO.class))).thenReturn(paymentIntent);
        when(paymentIntent.getClientSecret()).thenReturn(clientSecret);

        // Act
        ResponseEntity<String> response = orderController.createStripeClientSecret(stripePaymentDTO);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(clientSecret, response.getBody());
        verify(stripeService, times(1)).paymentIntent(stripePaymentDTO);
        verify(paymentIntent, times(1)).getClientSecret();
    }
}