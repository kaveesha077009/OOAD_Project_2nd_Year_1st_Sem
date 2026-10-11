package com.Agro_Shop.agroshop.Services;

import com.Agro_Shop.agroshop.Dto.CheckoutResponse;
import com.Agro_Shop.agroshop.Entities.*;
import com.Agro_Shop.agroshop.Repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class CheckoutService {

    private static final Set<String> PAYMENT_METHODS = Set.of("CASH", "BANK_TRANSFER");

    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private PaymentRepository paymentRepository;


    @Transactional
    public CheckoutResponse placeOrder(Long customerId, String paymentMethod) {
        if (customerId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "customerId is required");
        }
        String method = (paymentMethod == null) ? "" : paymentMethod.trim().toUpperCase();
        if (!PAYMENT_METHODS.contains(method)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Payment method must be one of " + PAYMENT_METHODS);
        }

        List<CartItem> cart = cartItemRepository.findByCustomerId(customerId);
        if (cart.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty");
        }

        Order order = new Order();
        order.setCustomerId(customerId);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");
        order.setTotalAmount(0.0);
        order = orderRepository.save(order);

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> savedItems = new ArrayList<>();

        for (CartItem c : cart) {
            Product product = productRepository.findById(c.getProductId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Product " + c.getProductId() + " no longer exists"));

            BigDecimal stock = product.getStockQuantity();
            if (stock != null && stock.compareTo(c.getQuantity()) < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Not enough stock for " + product.getName());
            }

            BigDecimal subtotal = product.getPrice().multiply(c.getQuantity());

            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setProductId(product.getId());
            item.setQuantity(c.getQuantity());
            item.setUnitPrice(product.getPrice());
            item.setSubtotal(subtotal);
            savedItems.add(orderItemRepository.save(item));

            if (stock != null) {
                product.setStockQuantity(stock.subtract(c.getQuantity()));
                productRepository.save(product);
            }
            total = total.add(subtotal);
        }


        order.setTotalAmount(total.doubleValue());
        order = orderRepository.save(order);

        Payment payment = new Payment();
        payment.setOrderId(order.getId());
        payment.setAmount(total);
        payment.setPaymentMethod(method);
        payment.setStatus("PENDING");
        payment = paymentRepository.save(payment);

        cartItemRepository.deleteByCustomerId(customerId);

        return new CheckoutResponse(order, savedItems, payment);
    }
}
