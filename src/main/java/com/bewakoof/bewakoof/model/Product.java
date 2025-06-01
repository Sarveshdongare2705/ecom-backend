package com.bewakoof.bewakoof.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

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
    private String category;
    private String material;
    //colors
    @OneToMany(mappedBy = "product" , cascade = CascadeType.ALL , orphanRemoval = true)
    @JsonManagedReference
    private List<ColorVariant> colorVariants;

    //reviews
    @OneToMany(mappedBy = "product" , cascade = CascadeType.ALL , orphanRemoval = true)
    @JsonManagedReference("product")
    private List<Review> reviews;

    //auto calculate discounted price
    @PrePersist
    @PreUpdate
    public void calculateDiscountPrice(){
        if(discountPercent != null && productPrice != null){
            this.discountPrice = productPrice - (productPrice * discountPercent/100);
        }
        else{
            this.discountPrice = productPrice;
        }
    }

    public Product(){}
}
