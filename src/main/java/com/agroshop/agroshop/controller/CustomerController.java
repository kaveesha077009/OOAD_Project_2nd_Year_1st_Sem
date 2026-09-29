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

    // 1. All Customers List (/customers)
    @GetMapping
    public String listCustomers(Model model) {
        model.addAttribute("customers", customerService.getAllCustomers());
        return "customer-list";
    }

    // 2. Show Add Customer Form (/customers/add)
    @GetMapping("/add")
    public String showAddCustomerForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "add-customer";
    }

    // 3. Save New Customer (/customers/save)
    @PostMapping("/save")
    public String saveCustomer(@ModelAttribute("customer") Customer customer,
                               @RequestParam("username") String username,
                               @RequestParam("password") String password) {
        customerService.saveCustomerWithUser(customer, username, password);
        return "redirect:/customers";
    }

    // 4. Show Edit Customer Form (/customers/edit/{id})
    @GetMapping("/edit/{id}")
    public String showEditCustomerForm(@PathVariable("id") Long id, Model model) {
        Customer customer = customerService.getCustomerById(id);
        if (customer == null) {
            return "redirect:/customers";
        }
        model.addAttribute("customer", customer);
        return "edit-customer";
    }

    // 5. Update Customer Details (/customers/update/{id})
    @PostMapping("/update/{id}")
    public String updateCustomer(@PathVariable("id") Long id, @ModelAttribute("customer") Customer customer) {
        customer.setId(id);
        customerService.updateCustomer(customer);
        return "redirect:/customers";
    }

    // 6. Delete Customer (/customers/delete/{id})
    @GetMapping("/delete/{id}")
    public String deleteCustomer(@PathVariable("id") Long id) {
        customerService.deleteCustomer(id);
        return "redirect:/customers";
    }
}