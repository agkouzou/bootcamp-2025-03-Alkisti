package dev.ctrlspace.bootcamp_2025_03.controllers;

import dev.ctrlspace.bootcamp_2025_03.exceptions.BootcampException;
import dev.ctrlspace.bootcamp_2025_03.model.Product;
import dev.ctrlspace.bootcamp_2025_03.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    @GetMapping("/products/{id}")
    public Product getProductById(@PathVariable("id") long id) throws BootcampException {
        return productService.getProductById(id);
    }

    @PostMapping("/products")
    public Product createProduct(@RequestBody Product product) throws BootcampException {
        return productService.createProduct(product);
    }

    @PutMapping("/products/{id}")
    public Product updateProduct(@PathVariable("id") long id, @RequestBody Product product) throws BootcampException {
        return productService.updateProduct(id, product);
    }

    @DeleteMapping("/products/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable("id") long id) throws BootcampException {
        productService.deleteProduct(id);
    }
}
