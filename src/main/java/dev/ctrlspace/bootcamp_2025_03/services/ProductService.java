package dev.ctrlspace.bootcamp_2025_03.services;

import dev.ctrlspace.bootcamp_2025_03.exceptions.BootcampException;
import dev.ctrlspace.bootcamp_2025_03.model.Product;
import dev.ctrlspace.bootcamp_2025_03.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {


    private List<Product> products;

    public ProductService() {
        products = new ArrayList<>();
        products.add(new Product(1L, "Product 1", 100.0));
        products.add(new Product(2L, "Product 2", 200.0));
        products.add(new Product(3L, "Product 3", 300.0));
        products.add(new Product(4L, "Product 4", 400.0));

    }

    public List<Product> getProducts() {
        return products;
    }

    public Product getProductById(long id) {
        for (Product product : products) {
            if (product.getId() == id) {
                return product;
            }
        }
        return null;
    }

    public Product getProductByName(String name) {
        for (Product product : products) {
            if (product.getName().equals(name)) {
                return product;
            }
        }
        return null;
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

        newProduct.setId(products.size() + 1L);


        products.add(newProduct);
        return newProduct;
    }

}
