package com.commerce.project.service.impl;

import com.commerce.project.exception.APIException;
import com.commerce.project.exception.ResourceNotFoundException;
import com.commerce.project.model.*;
import com.commerce.project.payload.OrderDTO;
import com.commerce.project.payload.OrderItemDTO;
import com.commerce.project.repository.*;
import com.commerce.project.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartService cartService;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private OrderServiceImpl orderService;

    private String email;
    private Long addressId;
    private Long cartId;
    private Long productId;
    private String paymentMethod;
    private String pgName;
    private String pgPaymentId;
    private String pgStatus;
    private String pgResponseMessage;
    private User user;
    private Cart cart;
    private Product product;
    private CartItem cartItem;
    private Address address;
    private Order order;
    private OrderItem orderItem;
    private Payment payment;
    private List<CartItem> cartItems;
    private List<Order> orderList;

    @BeforeEach
    void setUp() {
        // Initialize test data
        email = "test@example.com";
        addressId = 1L;
        cartId = 1L;
        productId = 1L;
        paymentMethod = "Stripe";
        pgName = "Stripe";
        pgPaymentId = "pi_123456";
        pgStatus = "succeeded";
        pgResponseMessage = "Payment successful";

        // Set up user
        user = new User();
        user.setUserId(1L);
        user.setUserName("testuser");
        user.setEmail(email);

        // Set up product
        product = new Product();
        product.setProductId(productId);
        product.setProductName("Test Product");
        product.setDescription("This is a test product");
        product.setQuantity(10);
        product.setPrice(100.0);
        product.setDiscount(10.0);
        product.setSpecialPrice(90.0);

        // Set up cart item
        cartItem = new CartItem();
        cartItem.setCartItemId(1L);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cartItem.setPrice(product.getSpecialPrice());
        cartItem.setDiscount(product.getDiscount());

        // Set up cart items list
        cartItems = new ArrayList<>();
        cartItems.add(cartItem);

        // Set up cart
        cart = new Cart();
        cart.setCartId(cartId);
        cart.setUser(user);
        cart.setTotalPrice(180.0); // 2 * 90.0
        cart.setCartItems(cartItems);

        // Set up address
        address = new Address();
        address.setAddressId(addressId);
        address.setStreet("123 Test Street");
        address.setCity("Test City");
        address.setCountry("Test Country");
        address.setPostcode("12345");
        address.setUser(user);

        // Set up payment
        payment = new Payment();
        payment.setPaymentId(1L);
        payment.setPaymentMethod(paymentMethod);
        payment.setPgName(pgName);
        payment.setPgPaymentId(pgPaymentId);
        payment.setPgStatus(pgStatus);
        payment.setPgResponseMessage(pgResponseMessage);

        // Set up order item
        orderItem = new OrderItem();
        orderItem.setOrderItemId(1L);
        orderItem.setProduct(product);
        orderItem.setQuantity(cartItem.getQuantity());
        orderItem.setDiscount(cartItem.getDiscount());
        orderItem.setOrderedProductPrice(cartItem.getPrice());

        // Set up order
        order = new Order();
        order.setOrderId(1L);
        order.setEmail(email);
        order.setOrderDate(LocalDate.now());
        order.setTotalAmount(cart.getTotalPrice());
        order.setOrderStatus("Order Accepted");
        order.setAddress(address);
        order.setPayment(payment);

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItem);
        order.setOrderItems(orderItems);

        // Connect payment to order
        payment.setOrder(order);

        // Connect order item to order
        orderItem.setOrder(order);

        // Set up order list
        orderList = new ArrayList<>();
        orderList.add(order);

        // Create a second order to add to the list
        Order order2 = new Order();
        order2.setOrderId(2L);
        order2.setEmail(email);
        order2.setOrderDate(LocalDate.now().minusDays(1));
        order2.setTotalAmount(150.0);
        order2.setOrderStatus("Order Accepted");
        order2.setAddress(address);
        order2.setPayment(payment);
        order2.setOrderItems(orderItems);

        orderList.add(order2);
    }

    @Test
    void placeOrder_ShouldCreateOrderSuccessfully() {
        // Arrange
        when(cartRepository.findCartByEmail(email)).thenReturn(cart);
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderItemRepository.saveAll(anyList())).thenReturn(List.of(orderItem));
        // Here's the fix: deleteProductFromCart returns a String, not void
        when(cartService.deleteProductFromCart(anyLong(), anyLong())).thenReturn("Product removed from cart");
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // Act
        OrderDTO result = orderService.placeOrder(
                email, addressId, paymentMethod, pgName, pgPaymentId, pgStatus, pgResponseMessage
        );

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals(order.getOrderId(), result.getOrderId());
        assertEquals(order.getTotalAmount(), result.getTotalAmount());
        assertEquals(order.getOrderStatus(), result.getOrderStatus());
        assertEquals(addressId, result.getAddressId());
        verify(cartRepository, times(1)).findCartByEmail(email);
        verify(addressRepository, times(1)).findById(addressId);
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderItemRepository, times(1)).saveAll(anyList());
        verify(productRepository, times(1)).save(any(Product.class));
        verify(cartService, times(1)).deleteProductFromCart(eq(cartId), eq(productId));
    }

    @Test
    void placeOrder_ShouldThrowResourceNotFoundException_WhenCartNotFound() {
        // Arrange
        when(cartRepository.findCartByEmail(email)).thenReturn(null);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                orderService.placeOrder(email, addressId, paymentMethod, pgName, pgPaymentId, pgStatus, pgResponseMessage)
        );
        verify(cartRepository, times(1)).findCartByEmail(email);
        verify(addressRepository, never()).findById(anyLong());
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void placeOrder_ShouldThrowResourceNotFoundException_WhenAddressNotFound() {
        // Arrange
        when(cartRepository.findCartByEmail(email)).thenReturn(cart);
        when(addressRepository.findById(addressId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                orderService.placeOrder(email, addressId, paymentMethod, pgName, pgPaymentId, pgStatus, pgResponseMessage)
        );
        verify(cartRepository, times(1)).findCartByEmail(email);
        verify(addressRepository, times(1)).findById(addressId);
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void placeOrder_ShouldThrowAPIException_WhenCartIsEmpty() {
        // Arrange
        Cart emptyCart = new Cart();
        emptyCart.setCartId(cartId);
        emptyCart.setUser(user);
        emptyCart.setCartItems(new ArrayList<>());

        when(cartRepository.findCartByEmail(email)).thenReturn(emptyCart);
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act & Assert
        assertThrows(APIException.class, () ->
                orderService.placeOrder(email, addressId, paymentMethod, pgName, pgPaymentId, pgStatus, pgResponseMessage)
        );
        verify(cartRepository, times(1)).findCartByEmail(email);
        verify(addressRepository, times(1)).findById(addressId);
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderItemRepository, never()).saveAll(anyList());
    }

    @Test
    void getUserOrders_ShouldReturnOrdersList() {
        // Arrange
        when(orderRepository.findByEmailOrderByOrderDateDesc(email)).thenReturn(orderList);

        // Act
        List<OrderDTO> result = orderService.getUserOrders(email);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(email, result.get(0).getEmail());
        assertEquals(order.getOrderId(), result.get(0).getOrderId());
        assertEquals(order.getTotalAmount(), result.get(0).getTotalAmount());
        assertEquals(order.getOrderStatus(), result.get(0).getOrderStatus());
        assertEquals(addressId, result.get(0).getAddressId());

        // Check second order
        assertEquals(email, result.get(1).getEmail());
        assertEquals(2L, result.get(1).getOrderId());
        assertEquals(150.0, result.get(1).getTotalAmount());

        // Verify orderItems are mapped correctly
        assertNotNull(result.get(0).getOrderItems());
        assertFalse(result.get(0).getOrderItems().isEmpty());
        OrderItemDTO orderItemDTO = result.get(0).getOrderItems().get(0);
        assertEquals(orderItem.getQuantity(), orderItemDTO.getQuantity());
        assertEquals(orderItem.getOrderedProductPrice(), orderItemDTO.getOrderedProductPrice());

        verify(orderRepository, times(1)).findByEmailOrderByOrderDateDesc(email);
    }

    @Test
    void getUserOrders_ShouldReturnEmptyList_WhenNoOrdersFound() {
        // Arrange
        when(orderRepository.findByEmailOrderByOrderDateDesc(email)).thenReturn(new ArrayList<>());

        // Act
        List<OrderDTO> result = orderService.getUserOrders(email);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderRepository, times(1)).findByEmailOrderByOrderDateDesc(email);
    }
}