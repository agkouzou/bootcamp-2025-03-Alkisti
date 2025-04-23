package dev.ctrlspace.bootcamp_2025_03.controllers;

import dev.ctrlspace.bootcamp_2025_03.model.Order;
import dev.ctrlspace.bootcamp_2025_03.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OrderController {

    private OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }


    @GetMapping("/orders")
    public List<Order> getOrders() {
        return orderService.getAll();
    }

    @GetMapping("/orders/{id}")
    public Order getOrderById(long id) {
        return orderService.getById(id);
    }

}
