package com.bewakoof.bewakoof.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartItemId;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    @JsonBackReference("product")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "color_id", nullable = false)
    @JsonBackReference("color")
    private ColorVariant colorVariant;

    @ManyToOne
    @JoinColumn(name = "size_id", nullable = false)
    @JsonBackReference("size")
    private SizeVariant sizeVariant;

    private int quantity = 1;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    @JsonBackReference("cart")
    private Cart cart;
}
