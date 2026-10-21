package com.agroshop.agroshopmangementsystem.controller;

import com.agroshop.agroshopmangementsystem.dto.EmployeeDTO;
import com.agroshop.agroshopmangementsystem.entity.Employee;
import com.agroshop.agroshopmangementsystem.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @PostMapping
    public ResponseEntity<?> addEmployee(@RequestBody EmployeeDTO dto) {
        try {
            Employee saved = employeeService.addEmployee(dto);
<<<<<<< HEAD
            return ResponseEntity.ok(saved);
=======
            return ResponseEntity.status(201).body(
                    java.util.Map.of(
                            "message", "Employee created successfully",
                            "id", saved.getId()
                    )
            );
>>>>>>> origin/feature/supplier-purchasing-integration
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

<<<<<<< HEAD
=======

    // Update an existing employee
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDTO> updateEmployee(
            @PathVariable Long id,
            @RequestBody EmployeeDTO dto) {

        EmployeeDTO updated = employeeService.updateEmployee(id, dto);
        return ResponseEntity.ok(updated);
    }

>>>>>>> origin/feature/supplier-purchasing-integration
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok().build();
    }
}