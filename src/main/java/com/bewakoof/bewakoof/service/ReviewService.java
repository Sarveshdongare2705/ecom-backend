package com.bewakoof.bewakoof.service;

import com.bewakoof.bewakoof.dto.ProductWithReviewsDTO;
import com.bewakoof.bewakoof.dto.ReviewDTO;
import com.bewakoof.bewakoof.model.AppUser;
import com.bewakoof.bewakoof.model.Product;
import com.bewakoof.bewakoof.model.Review;
import com.bewakoof.bewakoof.repository.ProductRepository;
import com.bewakoof.bewakoof.repository.ReviewRepository;
import com.bewakoof.bewakoof.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    public ProductWithReviewsDTO addReview(Long productId , Review review , UserDetails userDetails){
        Product p = productRepository.findById(productId).orElse(null);
        if(p == null){
            throw new RuntimeException("Product not found");
        }
        AppUser user = userRepository.findByEmail(userDetails.getUsername());
        if(user == null){
            throw new RuntimeException("User not found");
        }

        review.setProduct(p);
        review.setUser(user);
        reviewRepository.save(review);

        return convertToProductWithReviewsDTO(review.getProduct());
    }

    public ProductWithReviewsDTO updateReview(Long productId ,Long reviewId ,Review review , UserDetails userDetails){
        Product p = productRepository.findById(productId).orElse(null);
        if(p == null){
            throw new RuntimeException("Product not found");
        }
        AppUser user = userRepository.findByEmail(userDetails.getUsername());
        if(user == null){
            throw new RuntimeException("User not found");
        }

        Review existingReview = reviewRepository.findById(reviewId).orElse(null);
        if(existingReview == null){
            throw new RuntimeException("Review not found");
        }

        existingReview.setRating(review.getRating());
        existingReview.setComment(review.getComment());
        existingReview.setProduct(p);
        existingReview.setUser(user);
        reviewRepository.save(existingReview);
        return convertToProductWithReviewsDTO(existingReview.getProduct());
    }

    public ProductWithReviewsDTO deleteReview(Long productId , Long reviewId , UserDetails userDetails){
        Product p = productRepository.findById(productId).orElse(null);
        if(p == null){
            throw new RuntimeException("Product not found");
        }
        AppUser user = userRepository.findByEmail(userDetails.getUsername());
        if(user == null){
            throw new RuntimeException("User not found");
        }

        Review existingReview = reviewRepository.findById(reviewId).orElse(null);
        if(existingReview == null){
            throw new RuntimeException("Review not found");
        }

        reviewRepository.deleteById(reviewId);
        return convertToProductWithReviewsDTO(existingReview.getProduct());
    }




    // Helper method to convert Product to ProductWithReviewsDTO
    private ProductWithReviewsDTO convertToProductWithReviewsDTO(Product product) {
        ProductWithReviewsDTO dto = new ProductWithReviewsDTO();
        dto.setProductId(product.getProductId());
        dto.setProductName(product.getProductName());
        dto.setProductDescription(product.getProductDescription());
        dto.setProductBrand(product.getProductBrand());
        dto.setProductPrice(product.getProductPrice());
        dto.setDiscountPrice(product.getDiscountPrice());
        dto.setDiscountPercent(product.getDiscountPercent());
        dto.setCategory(product.getCategory());
        dto.setColorVariants(product.getColorVariants());
        // Handle possible null reviews
        List<ReviewDTO> reviewDTOs = Optional.ofNullable(product.getReviews())
                .orElse(Collections.emptyList())
                .stream()
                .map(this::convertToReviewDTO)
                .collect(Collectors.toList());

        dto.setReviews(reviewDTOs);

        return dto;
    }

    // Helper method to convert Review to ReviewDTO
    private ReviewDTO convertToReviewDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setReviewId(review.getReviewId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());

        // Add user information
        if(review.getUser() != null) {
            dto.setUserName(review.getUser().getUserName());
            dto.setProfilePic(review.getUser().getProfilePic());
            dto.setCity(review.getUser().getCity());
        }

        return dto;
    }
}
