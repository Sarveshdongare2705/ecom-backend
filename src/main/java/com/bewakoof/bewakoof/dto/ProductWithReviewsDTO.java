package com.bewakoof.bewakoof.dto;
import com.bewakoof.bewakoof.enums.*;
import com.bewakoof.bewakoof.model.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductWithReviewsDTO {
    private Long productId;
    private String productName;
    private String productDescription;
    private String productBrand;
    private Double productPrice;
    private Double discountPercent;
    private Double discountPrice;
    private String mainCategory;
    private String category;
    private Boolean isPlusSize;
    private Boolean isCustomizable;
    private String material;

    // Target Demographics
    private Gender targetGender;
    private AgeGroup targetAgeGroup;

    // Product Specifications
    private FitType fitType;
    private NeckType neckType;
    private SleeveType sleeveType;
    private DesignType designType;
    private Occasion occasion;

    // Offers and Promotions
    private String offerText;
    private Boolean hasComboOffer;
    private Integer comboQuantity;
    private Double comboPrice;

    // Social Proof
    private Integer recentPurchases;
    private Double averageRating;
    private Integer totalReviews;

    // Inventory
    private Integer totalStock;
    private Integer soldCount;
    private Boolean isActive;

    // Official Merchandise
    private Boolean isOfficialMerchandise;
    private String licenseInfo;

    // Shipping and Returns
    private Boolean isFreeShippingEligible;
    private Integer returnPolicyDays;
    private Boolean isExchangeable;

    // SEO
    private String slug;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Relationships
    private List<ColorVariant> colorVariants;
    private List<ReviewDTO> reviews;
}
