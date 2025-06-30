package com.bewakoof.bewakoof.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.bewakoof.bewakoof.model.Address;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartResponseDTO {
    private Long cartId;
    private Double totalPrice;
    private Integer totalQuantity;
    private Address selectedAddress; // Can replace with full Address DTO if needed
    private List<CartItemResponseDTO> cartItems;
}