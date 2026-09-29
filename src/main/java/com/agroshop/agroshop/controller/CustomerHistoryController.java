package com.agroshop.agroshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/customers")
public class CustomerHistoryController {


    @GetMapping("/history/{id}")
    public String viewCustomerHistory(@PathVariable("id") Long customerId, Model model) {
        model.addAttribute("customerId", customerId);

        return "customer-history";
    }
}