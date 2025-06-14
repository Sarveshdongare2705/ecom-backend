package com.bewakoof.bewakoof.controller;

import com.bewakoof.bewakoof.dto.AddCartItemRequestDTO;
import com.bewakoof.bewakoof.dto.CartItemDTO;
import com.bewakoof.bewakoof.dto.CartSummaryDTO;
import com.bewakoof.bewakoof.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/cart")
    public List<CartItemDTO> getCart(@AuthenticationPrincipal UserDetails userDetails){
        return cartService.getCartItems(userDetails);
    }

    @PostMapping("/cart")
    public List<CartItemDTO> addToCart(@AuthenticationPrincipal UserDetails userDetails, @RequestBody AddCartItemRequestDTO requestDTO){
        System.out.println(requestDTO);
        return cartService.addCartItem(userDetails , requestDTO.getProductId() , requestDTO.getColorId() , requestDTO.getSizeId() , requestDTO.getQuantity());
    }

    @DeleteMapping("/cart/cart-item/{id}")
    public List<CartItemDTO> deleteCartItem(@AuthenticationPrincipal UserDetails userDetails, @PathVariable("id") Long cartItemId){
        return cartService.deleteCartItem(userDetails , cartItemId);
    }

    @GetMapping("/cart-summary")
    public CartSummaryDTO getCartSummary(@AuthenticationPrincipal UserDetails userDetails){
        return cartService.getCartSummary(userDetails);
    }
}
