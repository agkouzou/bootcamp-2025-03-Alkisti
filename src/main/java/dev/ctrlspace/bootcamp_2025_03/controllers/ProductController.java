package dev.ctrlspace.bootcamp_2025_03.controllers;

import dev.ctrlspace.bootcamp_2025_03.model.Product;
import dev.ctrlspace.bootcamp_2025_03.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/products")
    public List<Product> getProduct() {
        return productService.getProducts();
    }

    @PostMapping("/products")
    public Product createProduct(@RequestBody Product product) throws Exception {
        return productService.createProduct(product);
    }
}
