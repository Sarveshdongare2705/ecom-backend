package com.bewakoof.bewakoof.service;

import com.bewakoof.bewakoof.dto.CartItemDTO;
import com.bewakoof.bewakoof.model.*;
import com.bewakoof.bewakoof.repository.CartItemRepository;
import com.bewakoof.bewakoof.repository.CartRepository;
import com.bewakoof.bewakoof.repository.ProductRepository;
import com.bewakoof.bewakoof.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    public List<CartItemDTO> getCartItems(UserDetails userDetails) {
        String email = userDetails.getUsername();
        AppUser user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException(email);
        }
        Optional<Cart> cartOptional = cartRepository.findByAppUser_UserId(user.getUserId());

        if (cartOptional.isEmpty()) {
            return List.of();
        }

        Cart cart = cartOptional.get();
        List<CartItem> items = cart.getCartItems();

        if(items.isEmpty()){
            return List.of();
        }

        return items.stream()
                .map(item -> new CartItemDTO(
                        item.getProduct(),
                        item.getColorVariant(),
                        item.getSizeVariant(),
                        getFirstImageForColor(item.getProduct().getProductId() , item.getColorVariant().getColorId()),
                        item.getQuantity(),
                        item.getCart().getCartId(),
                        item.getCartItemId()
                ))
                .collect(Collectors.toList());
    }

    public List<CartItemDTO> addCartItem(UserDetails userDetails, Long productId, Long colorId, Long sizeId, int quantity) {
        String email = userDetails.getUsername();
        AppUser user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException(email);
        }

        // Get or create cart
        Cart cart = cartRepository.findByAppUser_UserId(user.getUserId()).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setAppUser(user);
            return cartRepository.save(newCart);
        });

        // Try to find existing CartItem (better to query DB)
        Optional<CartItem> existingItemOpt = cart.getCartItems() != null
                ? cart.getCartItems().stream()
                .filter(item ->
                        item.getProduct().getProductId().equals(productId) &&
                                item.getColorVariant().getColorId().equals(colorId) &&
                                item.getSizeVariant().getSizeId().equals(sizeId))
                .findFirst()
                : Optional.empty();

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            cartItemRepository.save(existingItem);
        } else {
            // Add new cart item
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

// Get the color variant from product
            ColorVariant colorVariant = product.getColorVariants().stream()
                    .filter(cv -> cv.getColorId().equals(colorId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Color not found"));

// Get the size variant from color variant
            SizeVariant sizeVariant = colorVariant.getSizes().stream()
                    .filter(sv -> sv.getSizeId().equals(sizeId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Size not found"));

// Create and save new cart item
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setQuantity(quantity);
            newItem.setProduct(product);
            newItem.setColorVariant(colorVariant);
            newItem.setSizeVariant(sizeVariant);

            cartItemRepository.save(newItem);

        }

        return getCartItems(userDetails); // Return updated cart items
    }


    public List<CartItemDTO> deleteCartItem(UserDetails userDetails, Long cartItemId) {
        String email = userDetails.getUsername();
        AppUser user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException(email);
        }

        // Fetch the user's cart
        Cart cart = cartRepository.findByAppUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Cart not found for user"));

        // Fetch the cart item
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        // Check if the item belongs to the current user's cart
        if (!cartItem.getCart().getCartId().equals(cart.getCartId())) {
            throw new RuntimeException("Unauthorized: This cart item does not belong to the user");
        }

        // Remove the item
        cartItemRepository.delete(cartItem);

        return getCartItems(userDetails);
    }




    public String getFirstImageForColor(Long productId, Long colorId) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) return null;

        Product product = productOpt.get();
        return product.getColorVariants().stream()
                .filter(cv -> cv.getColorId().equals(colorId))
                .findFirst()
                .flatMap(cv -> cv.getImages().stream().findFirst())
                .map(image -> image.getUrl())
                .orElse(null);
    }

}
