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

        updateProductRatingAndReviews(p);

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

        updateProductRatingAndReviews(p);

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
        updateProductRatingAndReviews(p);

        return convertToProductWithReviewsDTO(existingReview.getProduct());
    }


    private void updateProductRatingAndReviews(Product product) {
        List<Review> reviews = reviewRepository.findByProduct(product);
        int totalReviews = reviews.size();
        double averageRating = 0.0;

        if (totalReviews > 0) {
            averageRating = reviews.stream()
                    .mapToDouble(Review::getRating)
                    .average()
                    .orElse(0.0);
        }

        product.setTotalReviews(totalReviews);
        product.setAverageRating(averageRating);
        productRepository.save(product);
    }





    // Helper method to convert Product to ProductWithReviewsDTO
    private ProductWithReviewsDTO convertToProductWithReviewsDTO(Product product) {
        ProductWithReviewsDTO dto = new ProductWithReviewsDTO();
        dto.setProductId(product.getProductId());
        dto.setProductName(product.getProductName());
        dto.setProductDescription(product.getProductDescription());
        dto.setProductBrand(product.getProductBrand());
        dto.setProductPrice(product.getProductPrice());
        dto.setDiscountPercent(product.getDiscountPercent());
        dto.setDiscountPrice(product.getDiscountPrice());
        dto.setCategory(product.getCategory());
        dto.setMaterial(product.getMaterial());

        // Target Demographics
        dto.setTargetGender(product.getTargetGender());
        dto.setTargetAgeGroup(product.getTargetAgeGroup());

        // Product Specifications
        dto.setFitType(product.getFitType());
        dto.setNeckType(product.getNeckType());
        dto.setSleeveType(product.getSleeveType());
        dto.setDesignType(product.getDesignType());
        dto.setOccasion(product.getOccasion());

        // Offers and Promotions
        dto.setOfferText(product.getOfferText());
        dto.setHasComboOffer(product.getHasComboOffer());
        dto.setComboQuantity(product.getComboQuantity());
        dto.setComboPrice(product.getComboPrice());

        // Social Proof
        dto.setRecentPurchases(product.getRecentPurchases());
        dto.setAverageRating(product.getAverageRating());
        dto.setTotalReviews(product.getTotalReviews());

        // Inventory
        dto.setTotalStock(product.getTotalStock());
        dto.setSoldCount(product.getSoldCount());
        dto.setIsActive(product.getIsActive());

        // Official Merchandise
        dto.setIsOfficialMerchandise(product.getIsOfficialMerchandise());
        dto.setLicenseInfo(product.getLicenseInfo());

        // Shipping and Returns
        dto.setIsFreeShippingEligible(product.getIsFreeShippingEligible());
        dto.setReturnPolicyDays(product.getReturnPolicyDays());
        dto.setIsExchangeable(product.getIsExchangeable());

        // SEO
        dto.setSlug(product.getSlug());
        dto.setMainCategory(product.getMainCategory());
        dto.setIsPlusSize(product.getIsPlusSize());
        dto.setIsCustomizable(product.getIsCustomizable());

        // Timestamps
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());

        // Relationships
        dto.setColorVariants(product.getColorVariants());
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
