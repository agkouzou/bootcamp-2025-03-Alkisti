package dev.ctrlspace.bootcamp_2025_03.services;

import dev.ctrlspace.bootcamp_2025_03.exceptions.BootcampException;
import dev.ctrlspace.bootcamp_2025_03.model.Product;
import dev.ctrlspace.bootcamp_2025_03.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(long id) throws BootcampException {
        Optional<Product> productOptional =  productRepository.findById(id);

        if (productOptional.isPresent()) {
            System.out.println("Product found: " + productOptional.get());
            return productOptional.get();
        } else {
            throw new BootcampException(HttpStatus.NOT_FOUND, "Product not found");
        }
    }

//    public Product getProductByName(String name) throws BootcampException {
//        // Using findByName to get product by name
//        Optional<Product> productOptional = productRepository.findByName(name);
//
//        if (productOptional.isPresent()) {
//            return productOptional.get();
//        } else {
//            throw new BootcampException(HttpStatus.NOT_FOUND, "Product not found");
//        }
//    }

    public Product getProductByName(String name) throws BootcampException {
        Optional<Product> productOptional = productRepository.findByNameWithNativeQuery(name);

        if (productOptional.isPresent()) {
            return productOptional.get();
        } else {
            throw new BootcampException(HttpStatus.NOT_FOUND, "Product not found");
        }
    }

    public Product createProduct(Product newProduct) throws BootcampException {

        if (newProduct.getId() != null) {
            throw new BootcampException(HttpStatus.BAD_REQUEST,"Product id must be null");
        }

        Optional<Product> existingProductOptional = productRepository.findByName(newProduct.getName());

//        Optional<Product> existingProductOptional = productRepository.findByNameWithNativeQuery(newProduct.getName());

        if (existingProductOptional.isPresent()) {
            throw new BootcampException(HttpStatus.BAD_REQUEST, "Product with the same name already exists");
        }

        if (newProduct.getPrice() <= 0) {
            throw new BootcampException(HttpStatus.BAD_REQUEST, "Product price must be greater than 0");
        }

        newProduct = productRepository.save(newProduct);
        return newProduct;
    }

    public Product updateProduct(long id, Product updatedProduct) throws BootcampException {

        if (updatedProduct.getId() == null || updatedProduct.getId() != id) {
            throw new BootcampException(HttpStatus.BAD_REQUEST, "Product id must match the provided ID");
        }

        Optional<Product> existingProductOptional = productRepository.findById(id);

        if (existingProductOptional.isEmpty()) {
            throw new BootcampException(HttpStatus.NOT_FOUND, "Product not found");
        }

        Product existingProduct = existingProductOptional.get();

        Optional<Product> productWithSameNameOptional = productRepository.findByName(updatedProduct.getName());

//        Optional<Product> productWithSameNameOptional = productRepository.findByNameWithNativeQuery(updatedProduct.getName());

        if (productWithSameNameOptional.isPresent() && !productWithSameNameOptional.get().getId().equals(id)) {
            throw new BootcampException(HttpStatus.BAD_REQUEST, "Product with the same name already exists");
        }

        if (updatedProduct.getPrice() <= 0) {
            throw new BootcampException(HttpStatus.BAD_REQUEST, "Product price must be greater than 0");
        }

        existingProduct.setName(updatedProduct.getName());
        existingProduct.setPrice(updatedProduct.getPrice());

        existingProduct = productRepository.save(existingProduct);
        return existingProduct;
    }

    public void deleteProduct(long id) throws BootcampException {
        Optional<Product> productOptional = productRepository.findById(id);

        if (productOptional.isPresent()) {
            productRepository.delete(productOptional.get());
        } else {
            throw new BootcampException(HttpStatus.NOT_FOUND, "Product not found");
        }
    }
}
