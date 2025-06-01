package com.bewakoof.bewakoof.repository;

import com.bewakoof.bewakoof.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByAppUser_UserId(Long userId);
}

