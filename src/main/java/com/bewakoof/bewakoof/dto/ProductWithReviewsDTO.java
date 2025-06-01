package com.bewakoof.bewakoof.dto;

import com.bewakoof.bewakoof.model.ColorVariant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    private String category;

    private List<ColorVariant> colorVariants;
    private List<ReviewDTO> reviews;

}
