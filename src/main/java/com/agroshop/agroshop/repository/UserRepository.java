package com.agroshop.agroshop.repository;

import com.agroshop.agroshop.Entity.User; // මෙහි Capital 'E' භාවිතා කර ඇත
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    User findByEmail(String email);
}