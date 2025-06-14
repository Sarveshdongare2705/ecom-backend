package com.bewakoof.bewakoof.model;

import com.bewakoof.bewakoof.enums.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    // NEW FIELDS TO ADD:

    // Target Demographics
    @Enumerated(EnumType.STRING)
    private Gender targetGender; // MEN, WOMEN, BOYS, GIRLS, UNISEX

    @Enumerated(EnumType.STRING)
    private AgeGroup targetAgeGroup; // KIDS, TEENS, ADULTS, ALL

    // Product Specifications (from Key Highlights in image)
    @Enumerated(EnumType.STRING)
    private FitType fitType; // REGULAR, SLIM, OVERSIZED

    @Enumerated(EnumType.STRING)
    private NeckType neckType; // ROUND_NECK, V_NECK, COLLAR

    @Enumerated(EnumType.STRING)
    private SleeveType sleeveType; // HALF_SLEEVE, FULL_SLEEVE

    @Enumerated(EnumType.STRING)
    private DesignType designType; // GRAPHIC_PRINT, PLAIN, TYPOGRAPHY

    @Enumerated(EnumType.STRING)
    private Occasion occasion; // CASUAL_WEAR, FORMAL, PARTY, SPORTS

    // Offers and Promotions (from "Buy 3 for 999" in image)
    private String offerText; // "Buy 3 for 999", "Flat 50% OFF"
    private Boolean hasComboOffer;
    private Integer comboQuantity; // 3 for combo offers
    private Double comboPrice; // 999 for combo offers

    // Social Proof (from image: "1017 people bought this")
    private Integer recentPurchases; // 1017 people bought this in last 7 days
    private Double averageRating; // 4.6
    private Integer totalReviews; // 576 Reviews

    // Inventory
    private Integer totalStock;
    private Integer soldCount;
    private Boolean isActive; // Product visibility

    // Official Merchandise (from "OFFICIAL MARVEL MERCHANDISE")
    private Boolean isOfficialMerchandise;
    private String licenseInfo; // "Official Marvel Merchandise"

    // Shipping and Returns (from image)
    private Boolean isFreeShippingEligible;
    private Integer returnPolicyDays; // 15 days return
    private Boolean isExchangeable;

    //filters


    // SEO
    private String slug; // URL-friendly version

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // EXISTING RELATIONSHIPS (UNCHANGED)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ColorVariant> colorVariants;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("product")
    private List<Review> reviews;

    // EXISTING METHOD (ENHANCED)
    @PrePersist
    @PreUpdate
    public void calculateDiscountPrice(){
        if(discountPercent != null && productPrice != null){
            this.discountPrice = productPrice - (productPrice * discountPercent/100);
        }
        else{
            this.discountPrice = productPrice;
        }

        // Update timestamps
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        this.updatedAt = now;

        // Generate slug if not exists
        if (this.slug == null || this.slug.isEmpty()) {
            this.slug = generateSlug();
        }
    }

    private String generateSlug() {
        if (productName != null) {
            return productName.toLowerCase()
                    .replaceAll("[^a-z0-9\\s]", "")
                    .replaceAll("\\s+", "-");
        }
        return "";
    }

    public String getProductName() {
        return productName;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public String getProductBrand() {
        return productBrand;
    }

    public Double getProductPrice() {
        return productPrice;
    }

    public Double getDiscountPercent() {
        return discountPercent;
    }

    public Double getDiscountPrice() {
        return discountPrice;
    }

    public String getCategory() {
        return category;
    }

    public String getMaterial() {
        return material;
    }

    public Gender getTargetGender() {
        return targetGender;
    }

    public AgeGroup getTargetAgeGroup() {
        return targetAgeGroup;
    }

    public FitType getFitType() {
        return fitType;
    }

    public NeckType getNeckType() {
        return neckType;
    }

    public SleeveType getSleeveType() {
        return sleeveType;
    }

    public DesignType getDesignType() {
        return designType;
    }

    public Occasion getOccasion() {
        return occasion;
    }

    public String getOfferText() {
        return offerText;
    }

    public Boolean getHasComboOffer() {
        return hasComboOffer;
    }

    public Integer getComboQuantity() {
        return comboQuantity;
    }

    public Double getComboPrice() {
        return comboPrice;
    }

    public Integer getRecentPurchases() {
        return recentPurchases;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public Integer getTotalReviews() {
        return totalReviews;
    }

    public Integer getTotalStock() {
        return totalStock;
    }

    public Integer getSoldCount() {
        return soldCount;
    }

    public Boolean getActive() {
        return isActive;
    }

    public Boolean getOfficialMerchandise() {
        return isOfficialMerchandise;
    }

    public String getLicenseInfo() {
        return licenseInfo;
    }

    public Boolean getFreeShippingEligible() {
        return isFreeShippingEligible;
    }

    public Integer getReturnPolicyDays() {
        return returnPolicyDays;
    }

    public Boolean getExchangeable() {
        return isExchangeable;
    }

    public String getSlug() {
        return slug;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<ColorVariant> getColorVariants() {
        return colorVariants;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public Long getProductId() {
        return productId;
    }

    public Product(){}
}
