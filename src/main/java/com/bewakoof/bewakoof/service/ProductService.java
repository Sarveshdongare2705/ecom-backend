package com.bewakoof.bewakoof.service;

import com.bewakoof.bewakoof.dto.ProductWithReviewsDTO;
import com.bewakoof.bewakoof.dto.ReviewDTO;
import com.bewakoof.bewakoof.model.*;
import com.bewakoof.bewakoof.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ColorVariantRepository colorVariantRepository;

    @Autowired
    private SizeVariantRepository sizeVariantRepository;

    @Autowired
    private SearchHistoryRepository searchHistoryRepository;
    @Autowired
    private UserRepository userRepository;


    public List<ProductWithReviewsDTO> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(this::convertToProductWithReviewsDTO)
                .collect(Collectors.toList());
    }
    public ProductWithReviewsDTO getProductById(Long id) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            return null;
        }
        return convertToProductWithReviewsDTO(product);
    }


    public ProductWithReviewsDTO addProduct(Product product) {
        Product savedProduct = productRepository.save(product);
        return convertToProductWithReviewsDTO(savedProduct);
    }

    public ProductWithReviewsDTO updateProduct(Long id, Product updatedProduct) {
        System.out.println(id);
        Product existingProduct = productRepository.findById(id).orElse(null);
        if (existingProduct == null) {
            return null;
        }

        existingProduct.setProductName(updatedProduct.getProductName());
        existingProduct.setProductDescription(updatedProduct.getProductDescription());
        existingProduct.setProductPrice(updatedProduct.getProductPrice());
        existingProduct.setDiscountPercent(updatedProduct.getDiscountPercent());
        existingProduct.setAverageRating(updatedProduct.getAverageRating());
        existingProduct.setTotalReviews(updatedProduct.getTotalReviews());
        existingProduct.setProductBrand(updatedProduct.getProductBrand());


        // colors and reviews not updated here (handled in separate endpoints)
        Product savedProduct = productRepository.save(existingProduct);
        return convertToProductWithReviewsDTO(savedProduct);
    }

    public boolean deleteProduct(Long id) {
        Product existingProduct = productRepository.findById(id).orElse(null);
        if (existingProduct == null) {
            return false;
        }
        productRepository.delete(existingProduct);
        return true;
    }



    //color variants
    public ProductWithReviewsDTO addColorVariant(Long productId, ColorVariant colorVariant) {
        Product existingProduct = productRepository.findById(productId).orElse(null);
        if (existingProduct == null) {
            throw new RuntimeException("Product not found with id: " + productId);
        }

        colorVariant.setProduct(existingProduct);
        colorVariantRepository.save(colorVariant);

        // Refresh product to get the new color variant
        Product refreshedProduct = productRepository.findById(productId).orElse(null);
        return convertToProductWithReviewsDTO(refreshedProduct);
    }

    public ProductWithReviewsDTO updateColorVariant(Long colorId, ColorVariant variant) {
        ColorVariant existingVariant = colorVariantRepository.findById(colorId).orElse(null);
        if (existingVariant == null) {
            throw new RuntimeException("Color variant not found with id: " + colorId);
        }

        existingVariant.setColorName(variant.getColorName());

        colorVariantRepository.save(existingVariant);

        // Fix: Use product ID, not color ID
        Long productId = existingVariant.getProduct().getProductId();
        Product refreshedProduct = productRepository.findById(productId).orElse(null);
        return convertToProductWithReviewsDTO(refreshedProduct);
    }

    public ProductWithReviewsDTO deleteColorVariant(Long colorId) {
        ColorVariant existingVariant = colorVariantRepository.findById(colorId).orElse(null);
        if (existingVariant == null) {
            throw new RuntimeException("Color variant not found with id: " + colorId);
        }

        // Get product ID before deleting the variant
        Long productId = existingVariant.getProduct().getProductId();
        colorVariantRepository.delete(existingVariant);

        Product refreshedProduct = productRepository.findById(productId).orElse(null);
        return convertToProductWithReviewsDTO(refreshedProduct);
    }


    public ProductWithReviewsDTO addSizeVariant(Long variantId, SizeVariant sizeVariant) {
        ColorVariant colorVariant = colorVariantRepository.findById(variantId).orElse(null);
        if (colorVariant == null) {
            throw new RuntimeException("Color variant not found with id: " + variantId);
        }

        sizeVariant.setColorVariant(colorVariant);
        sizeVariantRepository.save(sizeVariant);

        Product product = colorVariant.getProduct();
        return getProductById(product.getProductId());
    }

    public ProductWithReviewsDTO updateSizeVariant(Long variantId, Long sizeId, SizeVariant updatedVariant) {
        SizeVariant existing = sizeVariantRepository.findById(sizeId).orElse(null);
        if (existing == null) {
            throw new RuntimeException("Size variant not found with id: " + sizeId);
        }

        existing.setSize(updatedVariant.getSize());
        existing.setQuantity(updatedVariant.getQuantity());
        existing.setAvailable(updatedVariant.isAvailable());

        sizeVariantRepository.save(existing);

        ColorVariant colorVariant = colorVariantRepository.findById(variantId).orElse(null);
        if (colorVariant == null) {
            throw new RuntimeException("Color variant not found with id: " + variantId);
        }

        Product product = colorVariant.getProduct();
        return getProductById(product.getProductId());
    }

    public ProductWithReviewsDTO deleteSizeVariant(Long sizeId) {
        SizeVariant existing = sizeVariantRepository.findById(sizeId).orElse(null);
        if (existing == null) {
            throw new RuntimeException("Size variant not found with id: " + sizeId);
        }

        Product product = existing.getColorVariant().getProduct();
        sizeVariantRepository.delete(existing);

        return getProductById(product.getProductId());
    }



    //search feature
    public List<ProductWithReviewsDTO> searchProducts(String searchedTerm) {

        List<Product> matchedProducts = productRepository.searchByTerm(searchedTerm.toLowerCase());
        return matchedProducts.stream().map(product -> convertToProductWithReviewsDTO(product)).collect(Collectors.toList());
    }

    //get recent products based on search histroy
    public List<ProductWithReviewsDTO> getRecentProducts(UserDetails userDetails) {
        String email = userDetails.getUsername();
        AppUser user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException(email);
        }
        
        List<SearchHistory> recentSearches = searchHistoryRepository.findTop5SearchOfUser(user);
        Set<Long> addedProductIds = new HashSet<>();

        List<ProductWithReviewsDTO> results = new ArrayList<>();

        for (SearchHistory history : recentSearches) {
            List<ProductWithReviewsDTO> matches = searchProducts(history.getText());
            for (ProductWithReviewsDTO product : matches) {
                if (addedProductIds.add(product.getProductId())) {
                    results.add(product);
                }
            }
        }

        return results;
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
        dto.setMainCategory(product.getMainCategory());
        dto.setIsPlusSize(product.getIsPlusSize());
        dto.setIsCustomizable(product.getIsCustomizable());

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
