package com.commerce.project.service.impl;

import com.commerce.project.payload.StripePaymentDTO;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StripeServiceImplTest {

    @InjectMocks
    private StripeServiceImpl stripeService;

    @Mock
    private PaymentIntent paymentIntent;

    private StripePaymentDTO stripePaymentDTO;
    private String currency;
    private Long amount;

    @BeforeEach
    void setUp() {
        // Set up test data
        currency = "usd";
        amount = 1000L; // $10.00

        stripePaymentDTO = new StripePaymentDTO();
        stripePaymentDTO.setAmount(amount);
        stripePaymentDTO.setCurrency(currency);

        // Set stripe API key using reflection
        ReflectionTestUtils.setField(stripeService, "stripeApiKey", "test_stripe_api_key");
    }

    @Test
    void paymentIntent_ShouldReturnPaymentIntent() throws StripeException {
        try (MockedStatic<PaymentIntent> mockPaymentIntentStatic = mockStatic(PaymentIntent.class)) {
            // Arrange
            mockPaymentIntentStatic.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                    .thenReturn(paymentIntent);

            // Act
            PaymentIntent result = stripeService.paymentIntent(stripePaymentDTO);

            // Assert
            assertNotNull(result);
            assertEquals(paymentIntent, result);
            mockPaymentIntentStatic.verify(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)), times(1));
        }
    }
}