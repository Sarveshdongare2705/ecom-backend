package com.bewakoof.bewakoof.service;

import com.bewakoof.bewakoof.dto.AddCartItemRequestDTO;
import com.bewakoof.bewakoof.dto.CartItemResponseDTO;
import com.bewakoof.bewakoof.dto.CartResponseDTO;
import com.bewakoof.bewakoof.model.*;
import com.bewakoof.bewakoof.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    @Autowired
    private ProductAvailabilityRepository productAvailabilityRepository;

    public CartResponseDTO getCartItems(UserDetails userDetails) {
        String email = userDetails.getUsername();
        AppUser user = userRepository.findByEmail(email);
        if (user == null)
            throw new UsernameNotFoundException(email);

        Cart cart = cartRepository.findByAppUser_UserId(user.getUserId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setAppUser(user);
                    return cartRepository.save(newCart);
                });

        List<CartItemResponseDTO> cartItemDTOs = cart.getCartItems().stream().map(item -> {
            Product product = item.getProduct();
            ProductAvailability avail = item.getProductAvailability();


            ComboOfferResult comboResult = calculateComboOfferPrice(product, item.getQuantity());
            
            double itemTotalPrice = comboResult.getTotalPrice() + avail.getDeliveryCharge();

            return new CartItemResponseDTO(
                    item.getCartItemId(),
                    item.getQuantity(),

                    // Product
                    product.getProductId(),
                    product.getProductName(),
                    product.getProductPrice(),
                    product.getDiscountPrice(),
                    product.getProductPrice() - product.getDiscountPrice(),
                    product.getProductPrice() * item.getQuantity(),
                    comboResult.getTotalPrice(), 
                    (product.getProductPrice() * item.getQuantity()) - comboResult.getTotalPrice(),
                    product.getHasComboOffer(),
                    product.getOfferText(),
                    product.getComboQuantity(),
                    product.getComboPrice(),

                    // Color
                    item.getColorVariant().getColorId(),
                    item.getColorVariant().getColorName(),
                    item.getColorVariant().getImages().get(0).getUrl(),

                    // Size
                    item.getSizeVariant().getSizeId(),
                    item.getSizeVariant().getSize(),

                    // Availability
                    avail.getAvailId(),
                    avail.getPincode(),
                    avail.getDeliveryCharge(),
                    avail.getEstimatedDeliveryDays(),
                    avail.getReturnDays(),

                    // Total Price (with combo offer + delivery)
                    itemTotalPrice);
        }).toList();

        double value = recalculateCartTotals(cart);
        return new CartResponseDTO(
                cart.getCartId(),
                value,
                cart.getTotalQuantity(),
                cart.getSelectedAddress(),
                cartItemDTOs);
    }

    /**
     * Calculate combo offer pricing based on quantity
     * Logic: Buy 2 for combo price, remaining items at regular discount price
     */
    private ComboOfferResult calculateComboOfferPrice(Product product, int quantity) {
        if (!product.getHasComboOffer() || product.getComboQuantity() == null || product.getComboPrice() == null) {
            double totalPrice = product.getDiscountPrice() * quantity;
            return new ComboOfferResult(totalPrice, 0, quantity);
        }

        int comboQuantity = product.getComboQuantity(); 
        double comboPrice = product.getComboPrice(); 
        double regularPrice = product.getDiscountPrice(); 

        // Calculate how many complete combo sets we can make
        int comboSets = quantity / comboQuantity;
        int remainingItems = quantity % comboQuantity;

        // Calculate total price
        double totalComboPrice = comboSets * comboPrice;
        double totalRemainingPrice = remainingItems * regularPrice;
        double totalPrice = totalComboPrice + totalRemainingPrice;

        return new ComboOfferResult(totalPrice, comboSets, remainingItems);
    }

    
    private static class ComboOfferResult {
        private final double totalPrice;
        private final int comboSetsUsed;
        private final int remainingItems;

        public ComboOfferResult(double totalPrice, int comboSetsUsed, int remainingItems) {
            this.totalPrice = totalPrice;
            this.comboSetsUsed = comboSetsUsed;
            this.remainingItems = remainingItems;
        }
        public double getTotalPrice() {
            return totalPrice;
        }
        public int getComboSetsUsed() {
            return comboSetsUsed;
        }
        public int getRemainingItems() {
            return remainingItems;
        }
    }

    public boolean addCartItem(UserDetails userDetails, AddCartItemRequestDTO requestDTO) {
        String email = userDetails.getUsername();
        AppUser user = userRepository.findByEmail(email);
        if (user == null)
            throw new UsernameNotFoundException(email);

        Cart cart = cartRepository.findByAppUser_UserId(user.getUserId()).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setAppUser(user);
            return cartRepository.save(newCart);
        });

        Product product = productRepository.findById(requestDTO.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ColorVariant colorVariant = product.getColorVariants().stream()
                .filter(cv -> cv.getColorId().equals(requestDTO.getColorId()))
                .findFirst().orElseThrow(() -> new RuntimeException("Color not found"));

        SizeVariant sizeVariant = colorVariant.getSizes().stream()
                .filter(sv -> sv.getSizeId().equals(requestDTO.getSizeId()))
                .findFirst().orElseThrow(() -> new RuntimeException("Size not found"));

        ProductAvailability availability = productAvailabilityRepository.findById(requestDTO.getAvailId())
                .orElseThrow(() -> new RuntimeException("Invalid availability"));

        if (!availability.getProduct().getProductId().equals(product.getProductId())) {
            throw new RuntimeException("Availability doesn't match the product");
        }

        Optional<CartItem> existingItemOpt = cart.getCartItems() != null
                ? cart.getCartItems().stream()
                        .filter(item -> item.getProduct().getProductId().equals(product.getProductId()) &&
                                item.getColorVariant().getColorId().equals(colorVariant.getColorId()) &&
                                item.getSizeVariant().getSizeId().equals(sizeVariant.getSizeId()) &&
                                item.getProductAvailability().getAvailId().equals(availability.getAvailId()))
                        .findFirst()
                : Optional.empty();

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + requestDTO.getQuantity());
            cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setQuantity(requestDTO.getQuantity());
            newItem.setProduct(product);
            newItem.setColorVariant(colorVariant);
            newItem.setSizeVariant(sizeVariant);
            newItem.setProductAvailability(availability);
            cartItemRepository.save(newItem);
        }
        recalculateCartTotals(cart);

        return true;
    }

    public boolean deleteCartItem(UserDetails userDetails, Long cartItemId) {
        String email = userDetails.getUsername();
        AppUser user = userRepository.findByEmail(email);
        if (user == null)
            throw new UsernameNotFoundException(email);

        Cart cart = cartRepository.findByAppUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Cart not found for user"));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!cartItem.getCart().getCartId().equals(cart.getCartId())) {
            throw new RuntimeException("Unauthorized");
        }

        cartItemRepository.delete(cartItem);
        recalculateCartTotals(cart);
        return true;
    }

    private Cart getUserCartWithItems(UserDetails userDetails) {
        String email = userDetails.getUsername();
        AppUser user = userRepository.findByEmail(email);
        if (user == null)
            throw new UsernameNotFoundException(email);

        return cartRepository.findByAppUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Cart not found for user"));
    }

    public boolean validateCartBeforeCheckout(UserDetails userDetails, Integer selectedPincode) {
        Cart cart = getUserCartWithItems(userDetails);

        // REVALIDATE HERE to ensure cart matches selected pincode
        revalidateCartItemsForPincode(cart, selectedPincode);

        // Re-fetch the latest cart after revalidation (if necessary)
        cart = cartRepository.findById(cart.getCartId()).orElseThrow();

        for (CartItem item : cart.getCartItems()) {
            ProductAvailability avail = item.getProductAvailability();
            if (avail == null || !avail.getPincode().equals(selectedPincode)) {
                return false;
            }
        }
        return true;
    }

    public void revalidateCartItemsForPincode(Cart cart, Integer newPincode) {
        // Clone the list to avoid ConcurrentModificationException or lazy proxy issues
        List<CartItem> items = List.copyOf(cart.getCartItems());

        for (CartItem item : items) {
            Product product = item.getProduct();

            // Check if product is available for the new pincode
            ProductAvailability newAvailability = productAvailabilityRepository
                    .findByProduct_ProductIdAndPincode(product, newPincode).orElse(null);

            if (newAvailability != null) {
                // Update availability
                item.setProductAvailability(newAvailability);
                cartItemRepository.save(item);
            } else {
                // Remove item from DB
                cartItemRepository.delete(item);
            }
        }

        // Re-fetch the updated cart to ensure fresh item list
        Cart updatedCart = cartRepository.findById(cart.getCartId()).orElseThrow();
        cartRepository.save(updatedCart);

        // Recalculate totals with updated items
        recalculateCartTotals(updatedCart);
    }

    public double recalculateCartTotals(Cart cart) {
        double totalPrice = 0.0;
        int totalQty = 0;

        if (cart.getCartItems() != null) {
            for (CartItem item : cart.getCartItems()) {
                totalQty += item.getQuantity();
                
                // Use combo offer pricing for calculation
                ComboOfferResult comboResult = calculateComboOfferPrice(item.getProduct(), item.getQuantity());
                double itemTotal = comboResult.getTotalPrice();
                double deliveryCharge = item.getProductAvailability().getDeliveryCharge();
                totalPrice += itemTotal + deliveryCharge;
            }
        }

        cart.setTotalQuantity(totalQty);
        cart.setTotalPrice(totalPrice);
        cartRepository.save(cart);
        return totalPrice;
    }
}