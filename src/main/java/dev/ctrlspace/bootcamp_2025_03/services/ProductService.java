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
        Optional<Product> product =  productRepository.findById(id);

        if (product.isPresent()) {
            return product.get();
        } else {
            throw new BootcampException(HttpStatus.NOT_FOUND, "Product not found");
        }

    }

    public Product getProductByName(String name) throws BootcampException {
        Optional<Product> product = productRepository.findByNameWithNativeQuery(name);

        if (product.isPresent()) {
            return product.get();
        }
        throw new BootcampException(HttpStatus.NOT_FOUND, "Product not found");
    }

    public Product createProduct(Product newProduct) throws Exception {

        if (newProduct.getId() != null) {
            throw new BootcampException(HttpStatus.BAD_REQUEST,"Product id must be null");
        }

        Product existingProduct = getProductByName(newProduct.getName());

        if (existingProduct != null) {
            throw new BootcampException(HttpStatus.BAD_REQUEST, "Product with the same name already exists");
        }

        if (newProduct.getPrice() <= 0) {
            throw new BootcampException(HttpStatus.BAD_REQUEST, "Product price must be greater than 0");
        }

        newProduct = productRepository.save(newProduct);
        return newProduct;
    }

}
