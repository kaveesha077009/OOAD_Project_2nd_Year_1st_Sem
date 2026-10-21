package com.agroshop.agroshopmangementsystem.entity;

import jakarta.persistence.*;
<<<<<<< HEAD
=======
import com.fasterxml.jackson.annotation.JsonIgnore;
>>>>>>> origin/feature/supplier-purchasing-integration
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String email;
<<<<<<< HEAD
=======
    @JsonIgnore
>>>>>>> origin/feature/supplier-purchasing-integration
    private String password;
    private String role;
    private Integer status;

<<<<<<< HEAD
    private String resetToken;
=======
    @JsonIgnore
    private String resetToken;
    @JsonIgnore
>>>>>>> origin/feature/supplier-purchasing-integration
    private LocalDateTime tokenExpiry;

    public User() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }

    public LocalDateTime getTokenExpiry() { return tokenExpiry; }
    public void setTokenExpiry(LocalDateTime tokenExpiry) { this.tokenExpiry = tokenExpiry; }
}