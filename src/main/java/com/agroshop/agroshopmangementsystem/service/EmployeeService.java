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

<<<<<<< HEAD
    @Transactional
    public Employee addEmployee(EmployeeDTO dto) {
=======

    // Validate information before creating an employee account
    private void validateNewEmployee(EmployeeDTO dto) {
        if (dto == null
                || dto.getFirstName() == null || dto.getFirstName().isBlank()
                || dto.getLastName() == null || dto.getLastName().isBlank()
                || dto.getUsername() == null || dto.getUsername().isBlank()
                || dto.getEmail() == null || !dto.getEmail().matches(
                        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
                || dto.getPassword() == null || dto.getPassword().isBlank()
                || dto.getSalary() == null || !Double.isFinite(dto.getSalary())
                || dto.getSalary() < 0) {
            throw new IllegalArgumentException("Invalid employee information");
        }

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email is already registered");
        }
    }

    @Transactional
    public Employee addEmployee(EmployeeDTO dto) {
        validateNewEmployee(dto);
>>>>>>> origin/feature/supplier-purchasing-integration
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

<<<<<<< HEAD
=======

    // Update an existing employee's details
    @Transactional
    public EmployeeDTO updateEmployee(Long id, EmployeeDTO dto) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND,
                        "Employee not found"));

        if (dto == null
                || dto.getFirstName() == null || dto.getFirstName().isBlank()
                || dto.getLastName() == null || dto.getLastName().isBlank()
                || dto.getSalary() == null
                || !Double.isFinite(dto.getSalary())
                || dto.getSalary() < 0) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Invalid employee information");
        }

        employee.setFirstName(dto.getFirstName().trim());
        employee.setLastName(dto.getLastName().trim());
        employee.setDesignation(dto.getDesignation());
        employee.setPosition(dto.getDesignation());
        employee.setSalary(dto.getSalary());
        employee.setPhoneNum(dto.getPhoneNum());
        employee.setAddress(dto.getAddress());

        employeeRepository.save(employee);

        EmployeeDTO result = new EmployeeDTO();
        result.setId(employee.getId());
        result.setFirstName(employee.getFirstName());
        result.setLastName(employee.getLastName());
        result.setDesignation(employee.getDesignation());
        result.setSalary(employee.getSalary());
        result.setPhoneNum(employee.getPhoneNum());
        result.setAddress(employee.getAddress());

        if (employee.getUser() != null) {
            result.setUsername(employee.getUser().getUsername());
            result.setEmail(employee.getUser().getEmail());
            result.setActive(Integer.valueOf(1).equals(
                    employee.getUser().getStatus()));
        } else {
            result.setEmail(employee.getEmail());
        }

        return result;
    }

>>>>>>> origin/feature/supplier-purchasing-integration
    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
    }
}