package com.bewakoof.bewakoof.dto;

import com.bewakoof.bewakoof.model.ColorVariant;
import com.bewakoof.bewakoof.model.Product;
import com.bewakoof.bewakoof.model.SizeVariant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class CartItemDTO {

    private Long cartItemId;
    private Long cartId;
    private Long productId;
    private String productName;
    private String productBrand;

    private String cartItemImage;

    private int quantity;

    private Double productPrice;
    private Double discountPrice;

    private Double totalPrice;
    private ColorVariant colorVariant;
    private SizeVariant sizeVariant;

    public Double getTotalPrice() {
        if (discountPrice == null) return 0.0;
        return quantity * discountPrice;
    }

    public CartItemDTO(Product p , ColorVariant c , SizeVariant s , String image , int quantity , Long cartId , Long cartItemId) {
        this.productName = p.getProductName();
        this.productId = p.getProductId();
        this.productBrand = p.getProductBrand();
        this.cartItemImage = image;
        this.quantity = quantity;
        this.productPrice = p.getProductPrice();
        this.discountPrice = p.getDiscountPrice();
        this.totalPrice = getTotalPrice();
        this.cartId = cartId;
        this.cartItemId = cartItemId;
        this.colorVariant = c;
        this.sizeVariant = s;
    }
}
