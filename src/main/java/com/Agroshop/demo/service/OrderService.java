package com.agroshop.demo.service;

import com.agroshop.demo.dto.OrderRequest;
import com.agroshop.demo.dto.OrderResponse;
import com.agroshop.demo.entity.*;
import com.agroshop.demo.exception.BadRequestException;
import com.agroshop.demo.exception.ResourceNotFoundException;
import com.agroshop.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Set<String> ONLINE_METHODS = Set.of("CASH_ON_DELIVERY", "BANK_TRANSFER");
    private static final Set<String> COUNTER_METHODS = Set.of("CASH", "CARD");
    private static final String WALK_IN_EMAIL = "walkin@agroshop.lk";

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    public OrderService(OrderRepository orderRepository, PaymentRepository paymentRepository,
                        ProductRepository productRepository, InventoryRepository inventoryRepository,
                        UserRepository userRepository, CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
    }

    /** A customer checks out their cart. Payment stays PENDING until staff confirm it. */
    @Transactional
    public OrderResponse placeOnlineOrder(String username, OrderRequest req) {
        requireMethod(ONLINE_METHODS, req.paymentMethod());
        return create(customerOf(username), req, false);
    }

    /** An employee records a sale made at the counter. Paid and handed over straight away. */
    @Transactional
    public OrderResponse counterSale(OrderRequest req) {
        requireMethod(COUNTER_METHODS, req.paymentMethod());
        Customer walkIn = customerRepository.findByEmail(WALK_IN_EMAIL)
                .orElseThrow(() -> new ResourceNotFoundException("Walk-in customer record is missing. Re-run Agroshop.sql"));
        return create(walkIn, req, true);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> myOrders(String username) {
        Customer c = customerOf(username);
        return orderRepository.findByCustomerIdOrderByIdDesc(c.getId()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> allOrders() {
        Map<Long, Payment> payments = paymentRepository.findAll().stream()
                .collect(Collectors.toMap(p -> p.getOrder().getId(), Function.identity()));
        return orderRepository.findAllByOrderByIdDesc().stream()
                .map(o -> OrderResponse.of(o, payments.get(o.getId()))).toList();
    }

    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus status) {
        CustomerOrder order = find(id);
        Payment payment = paymentOf(id);
        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new BadRequestException("This order is already " + order.getStatus().name().toLowerCase() + " and cannot change");
        }
        if (status == OrderStatus.CANCELLED) {
            for (OrderItem item : order.getItems()) {
                Inventory inv = inventoryRepository.findForUpdate(item.getProduct().getId()).orElseThrow();
                inv.setQuantity(inv.getQuantity().add(item.getQuantity()));
            }
            payment.setStatus(payment.getStatus() == PaymentStatus.COMPLETED ? PaymentStatus.REFUNDED : PaymentStatus.FAILED);
        }
        if (status == OrderStatus.DELIVERED && payment.getStatus() == PaymentStatus.PENDING
                && "CASH_ON_DELIVERY".equals(payment.getPaymentMethod())) {
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setPaymentDate(LocalDateTime.now());
        }
        order.setStatus(status);
        return OrderResponse.of(order, payment);
    }

    /** Staff confirm that a bank transfer arrived. */
    @Transactional
    public OrderResponse confirmPayment(Long id) {
        CustomerOrder order = find(id);
        Payment payment = paymentOf(id);
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BadRequestException("This payment is already " + payment.getStatus().name().toLowerCase());
        }
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPaymentDate(LocalDateTime.now());
        if (order.getStatus() == OrderStatus.PENDING) order.setStatus(OrderStatus.PROCESSING);
        return OrderResponse.of(order, payment);
    }

    private OrderResponse create(Customer customer, OrderRequest req, boolean counter) {
        // TreeMap = products always locked in the same order, which avoids deadlocks
        Map<Long, BigDecimal> wanted = new TreeMap<>();
        for (OrderRequest.Line l : req.items()) {
            if (l.quantity().signum() <= 0) throw new BadRequestException("Quantity must be more than zero");
            wanted.merge(l.productId(), l.quantity(), BigDecimal::add);
        }

        CustomerOrder order = new CustomerOrder();
        order.setCustomer(customer);
        order.setOrderDate(LocalDateTime.now());
        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Long, BigDecimal> e : wanted.entrySet()) {
            Product p = productRepository.findById(e.getKey()).filter(Product::getStatus)
                    .orElseThrow(() -> new ResourceNotFoundException("Product " + e.getKey() + " is no longer available"));
            Inventory inv = inventoryRepository.findForUpdate(p.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("No stock record for " + p.getName()));
            if (inv.getQuantity().compareTo(e.getValue()) < 0) {
                throw new BadRequestException("Only " + inv.getQuantity().stripTrailingZeros().toPlainString() + " "
                        + p.getUnit() + " of " + p.getName() + " left in stock");
            }
            inv.setQuantity(inv.getQuantity().subtract(e.getValue()));

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(p);
            item.setQuantity(e.getValue());
            item.setUnitPrice(p.getPrice());                      // price always comes from the database
            item.setSubtotal(p.getPrice().multiply(e.getValue()).setScale(2, RoundingMode.HALF_UP));
            order.getItems().add(item);
            total = total.add(item.getSubtotal());
        }
        order.setTotalAmount(total);
        order.setStatus(counter ? OrderStatus.DELIVERED : OrderStatus.PENDING);
        order = orderRepository.save(order);

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(total);
        payment.setPaymentMethod(req.paymentMethod());
        payment.setStatus(counter ? PaymentStatus.COMPLETED : PaymentStatus.PENDING);
        payment.setPaymentDate(counter ? LocalDateTime.now() : null);
        return OrderResponse.of(order, paymentRepository.save(payment));
    }

    private Customer customerOf(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        return customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("This account has no customer profile"));
    }

    private OrderResponse toResponse(CustomerOrder o) {
        return OrderResponse.of(o, paymentRepository.findByOrderId(o.getId()).orElse(null));
    }

    private CustomerOrder find(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order " + id + " not found"));
    }

    private Payment paymentOf(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment for order " + orderId + " not found"));
    }

    private static void requireMethod(Set<String> allowed, String method) {
        if (!allowed.contains(method)) throw new BadRequestException("Payment method must be one of " + allowed);
    }
}
