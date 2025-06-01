package com.bewakoof.bewakoof.service;

import com.bewakoof.bewakoof.dto.ProductWithReviewsDTO;
import com.bewakoof.bewakoof.dto.ReviewDTO;
import com.bewakoof.bewakoof.model.ColorVariant;
import com.bewakoof.bewakoof.model.Product;
import com.bewakoof.bewakoof.model.Review;
import com.bewakoof.bewakoof.model.SizeVariant;
import com.bewakoof.bewakoof.repository.ColorVariantRepository;
import com.bewakoof.bewakoof.repository.ProductRepository;
import com.bewakoof.bewakoof.repository.SizeVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ColorVariantRepository colorVariantRepository;

    @Autowired
    private SizeVariantRepository sizeVariantRepository;

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
        existingProduct.setProductBrand(updatedProduct.getProductBrand());
        existingProduct.setProductPrice(updatedProduct.getProductPrice());
        existingProduct.setDiscountPercent(updatedProduct.getDiscountPercent());

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
