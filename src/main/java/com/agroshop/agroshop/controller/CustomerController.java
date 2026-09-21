package com.agroshop.agroshop.controller;

import com.agroshop.agroshop.Entity.Customer;
import com.agroshop.agroshop.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping
    public String listCustomers(Model model) {
        model.addAttribute("customers", customerService.getAllCustomers());
        return "customer-list";
    }

    @GetMapping("/add")
    public String showAddCustomerForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "add-customer";
    }

    @PostMapping("/save")
    public String saveCustomer(@ModelAttribute("customer") Customer customer,
                               @RequestParam("username") String username,
                               @RequestParam("password") String password) {
        customerService.saveCustomerWithUser(customer, username, password);
        return "redirect:/customers";
    }

    @GetMapping("/edit/{id}")
    public String showEditCustomerForm(@PathVariable("id") Long id, Model model) {
        Customer customer = customerService.getCustomerById(id);
        model.addAttribute("customer", customer);
        return "edit-customer";
    }

    @PostMapping("/update")
    public String updateCustomer(@ModelAttribute("customer") Customer customer) {
        customerService.updateCustomer(customer);
        return "redirect:/customers";
    }

    @GetMapping("/delete/{id}")
    public String deleteCustomer(@PathVariable("id") Long id) {
        customerService.deleteCustomer(id);
        return "redirect:/customers";
    }
}