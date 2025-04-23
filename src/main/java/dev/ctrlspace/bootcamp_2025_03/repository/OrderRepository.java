package dev.ctrlspace.bootcamp_2025_03.repository;

import dev.ctrlspace.bootcamp_2025_03.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

//    @Query(nativeQuery = true,
//            value = "SELECT * FROM orders " +
//                    " inner join public.cart_items ci on orders.id = ci.order_id " +
//                    " inner join public.products p on p.id = ci.product_id")
//    List<Order> findAllCustom();
}
