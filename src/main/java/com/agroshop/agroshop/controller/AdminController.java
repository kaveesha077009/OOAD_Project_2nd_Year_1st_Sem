package com.agroshop.agroshop.controller;

import com.agroshop.agroshop.Entity.User; // මෙහි Capital 'E' යොදා ඇත
import com.agroshop.agroshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/dashboard")
    public String showDashboard() {
        return "admin-dashboard";
    }

    @GetMapping("/employees/create")
    public String showCreateEmployeeForm(Model model) {
        model.addAttribute("employee", new User());
        return "add-customer";
    }

    @PostMapping("/employees/create")
    public String createEmployee(@ModelAttribute("employee") User employee, RedirectAttributes redirectAttributes) {

        if (userRepository.existsByEmail(employee.getEmail())) {
            redirectAttributes.addFlashAttribute("error", "මෙම Email එක දැනටමත් පද්ධතියේ පවතී!");
            return "redirect:/admin/employees/create";
        }

        String rawPassword = employee.getPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        employee.setPassword(encodedPassword);

        employee.setRole("EMPLOYEE");
        employee.setStatus(1);

        userRepository.save(employee);

        redirectAttributes.addFlashAttribute("success", "Employee සාර්ථකව එකතු කරන ලදී!");
        return "redirect:/admin/dashboard";
    }
}