package dev.ctrlspace.bootcamp_2025_03.repository;

import dev.ctrlspace.bootcamp_2025_03.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query(nativeQuery = true,
            value = "SELECT * FROM products WHERE name = ?1")
    Optional<Product> findByNameWithNativeQuery(String name);

//    @Query("select p from Product p where p.name = ?1")
//    Optional<Product> findByNameWithJPQL(String name);

//    Optional<Product> findById(long id);

    Optional<Product> findByName(String name);

//    Optional<Product> findByPriceAndName(Double price, String name);




}
