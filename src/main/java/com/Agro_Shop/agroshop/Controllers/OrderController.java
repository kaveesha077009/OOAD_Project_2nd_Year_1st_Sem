package com.Agro_Shop.agroshop.Controllers;

import com.Agro_Shop.agroshop.Entities.Order;
import com.Agro_Shop.agroshop.Entities.OrderItem;
import com.Agro_Shop.agroshop.Repositories.OrderItemRepository;
import com.Agro_Shop.agroshop.Repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @GetMapping("/orders")
    public String showOrdersPage(Model model) {
        model.addAttribute("orders", orderRepository.findAll());
        return "orders";
    }

    @GetMapping("/sales")
    public String showSalesPage() {
        return "sales";
    }


    @GetMapping("/invoice")
    public String showInvoicePage(@RequestParam(name = "id", required = false) Long id, Model model) {
        if (id != null) {
            Order order = orderRepository.findById(id).orElse(null);
            if (order != null) {
                model.addAttribute("order", order);
                List<OrderItem> items = orderItemRepository.findByOrderId(id);
                model.addAttribute("items", items);
            }
        }
        return "invoice";
    }
}