package com.Agro_Shop.agroshop.Dto;

import com.Agro_Shop.agroshop.Entities.Order;
import com.Agro_Shop.agroshop.Entities.OrderItem;
import com.Agro_Shop.agroshop.Entities.Payment;

import java.util.List;

public record CheckoutResponse(Order order, List<OrderItem> items, Payment payment) {}
