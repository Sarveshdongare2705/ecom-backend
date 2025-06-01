package com.bewakoof.bewakoof.controller;

import com.bewakoof.bewakoof.dto.ProductWithReviewsDTO;
import com.bewakoof.bewakoof.model.ColorVariant;
import com.bewakoof.bewakoof.model.Product;
import com.bewakoof.bewakoof.model.SizeVariant;
import com.bewakoof.bewakoof.service.ImageService;
import com.bewakoof.bewakoof.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ImageService imageService;

    //product endpoints

    @GetMapping("/products/all")
    public List<ProductWithReviewsDTO> getAllProducts() {
        return productService.getAllProducts();
    }
    @GetMapping("/product/{id}")
    public ProductWithReviewsDTO getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }
    @PostMapping("/products/add")
    public ProductWithReviewsDTO addProduct(@RequestBody Product product) {
        return productService.addProduct(product);
    }
    @PutMapping("/product/{id}")
    public ProductWithReviewsDTO updateProduct(@PathVariable Long id, @RequestBody Product product) {
        return productService.updateProduct(id, product);
    }
    @DeleteMapping("/product/{id}")
    public boolean deleteProduct(@PathVariable Long id) {
        return productService.deleteProduct(id);
    }


    //color variant endpoints
    @PostMapping("/product/{productId}/colors")
    public ProductWithReviewsDTO addProductColors(@PathVariable Long productId, @RequestBody ColorVariant variant) {
        return productService.addColorVariant(productId , variant);
    }

    @PutMapping("/colors/{colorId}")
    public ProductWithReviewsDTO updateProductColors(@PathVariable Long colorId, @RequestBody ColorVariant variant) {
        return productService.updateColorVariant(colorId , variant);
    }

    @DeleteMapping("/colors/{colorId}")
    public ProductWithReviewsDTO deleteProductColors(@PathVariable Long colorId) {
        return productService.deleteColorVariant(colorId);
    }


    //images
    @PostMapping("/color/{colorVariantId}/image")
    public ProductWithReviewsDTO addProductImage(@PathVariable Long colorVariantId, @RequestPart MultipartFile file) {
        return imageService.uploadImage(colorVariantId, file);
    }

    @DeleteMapping("/images/{imageId}")
    public ProductWithReviewsDTO deleteProductImage(@PathVariable Long imageId) {
        return imageService.deleteImage(imageId);
    }


    @PostMapping("/color/{variantId}/size")
    public ProductWithReviewsDTO addSizeVariant(@PathVariable Long variantId, @RequestBody SizeVariant sizeVariant) {
        return productService.addSizeVariant(variantId, sizeVariant);
    }

    @PutMapping("/color/{variantId}/size/{sizeId}")
    public ProductWithReviewsDTO updateSizeVariant(
            @PathVariable Long variantId,
            @PathVariable Long sizeId,
            @RequestBody SizeVariant sizeVariant
    ) {
        return productService.updateSizeVariant(variantId, sizeId, sizeVariant);
    }

    @DeleteMapping("/color/size/{sizeId}")
    public ProductWithReviewsDTO deleteSizeVariant(@PathVariable Long sizeId) {
        return productService.deleteSizeVariant(sizeId);
    }

}   
