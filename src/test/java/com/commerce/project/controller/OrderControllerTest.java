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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    private List<OrderDTO> orderDTOList;

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
        orderDTO.setOrderDate(LocalDate.now());
        orderDTO.setOrderItems(new ArrayList<>());

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

        // Set up order list for user orders test
        orderDTOList = new ArrayList<>();
        orderDTOList.add(orderDTO);

        // Create a second order to add to the list
        OrderDTO orderDTO2 = new OrderDTO();
        orderDTO2.setOrderId(2L);
        orderDTO2.setEmail(email);
        orderDTO2.setTotalAmount(150.0);
        orderDTO2.setOrderStatus("Order Accepted");
        orderDTO2.setAddressId(addressId);
        orderDTO2.setOrderDate(LocalDate.now().minusDays(1));
        orderDTO2.setOrderItems(new ArrayList<>());

        orderDTOList.add(orderDTO2);
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

    @Test
    void getUserOrders_ShouldReturnOrdersList() {
        // Arrange
        when(authUtil.loggedInEmail()).thenReturn(email);
        when(orderService.getUserOrders(email)).thenReturn(orderDTOList);

        // Act
        ResponseEntity<List<OrderDTO>> response = orderController.getUserOrders();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(orderDTOList, response.getBody());
        assertEquals(2, response.getBody().size());
        verify(authUtil, times(1)).loggedInEmail();
        verify(orderService, times(1)).getUserOrders(email);
    }
}