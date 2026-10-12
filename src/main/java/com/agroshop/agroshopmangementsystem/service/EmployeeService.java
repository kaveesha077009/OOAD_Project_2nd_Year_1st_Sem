package com.agroshop.agroshopmangementsystem.service;

import com.agroshop.agroshopmangementsystem.dto.EmployeeDTO;
import com.agroshop.agroshopmangementsystem.entity.Employee;
import com.agroshop.agroshopmangementsystem.entity.User;
import com.agroshop.agroshopmangementsystem.repository.EmployeeRepository;
import com.agroshop.agroshopmangementsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder encoder;

    public List<EmployeeDTO> getAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        List<EmployeeDTO> dtoList = new ArrayList<>();

        for (Employee emp : employees) {
            EmployeeDTO dto = new EmployeeDTO();
            dto.setId(emp.getId());
            dto.setFirstName(emp.getFirstName());
            dto.setLastName(emp.getLastName());

            String desig = emp.getDesignation() != null ? emp.getDesignation() : emp.getPosition();
            dto.setDesignation(desig != null ? desig : "Staff");

            dto.setSalary(emp.getSalary());
            dto.setAddress(emp.getAddress());
            dto.setPhoneNum(emp.getPhoneNum());

            if (emp.getUser() != null) {
                dto.setUsername(emp.getUser().getUsername());
                dto.setEmail(emp.getUser().getEmail());
                dto.setActive(emp.getUser().getStatus() != null && emp.getUser().getStatus() == 1);
            } else {
                dto.setEmail(emp.getEmail());
                dto.setActive(true);
            }

            dtoList.add(dto);
        }

        return dtoList;
    }

    @Transactional
    public Employee addEmployee(EmployeeDTO dto) {
        if (dto.getUsername() != null && userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username '" + dto.getUsername() + "' is already registered!");
        }

        // 1. Create and Save User Profile
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setRole("EMPLOYEE");
        user.setStatus(1);
        user.setPassword(encoder.encode(dto.getPassword()));

        User savedUser = userRepository.saveAndFlush(user); // Force write to MySQL DB

        // 2. Create and Save Employee Profile
        Employee employee = new Employee();
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setEmail(dto.getEmail());
        employee.setPhoneNum(dto.getPhoneNum());
        employee.setDesignation(dto.getDesignation());
        employee.setPosition(dto.getDesignation());
        employee.setSalary(dto.getSalary());
        employee.setAddress(dto.getAddress());
        employee.setUser(savedUser);

        return employeeRepository.saveAndFlush(employee); // Force write to MySQL DB
    }

    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
    }
}