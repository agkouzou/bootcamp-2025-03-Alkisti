package dev.ctrlspace.bootcamp_2025_03.controllers;

import dev.ctrlspace.bootcamp_2025_03.exceptions.BootcampException;
import dev.ctrlspace.bootcamp_2025_03.model.Order;
import dev.ctrlspace.bootcamp_2025_03.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    public Order getOrderById(@PathVariable("id") long id) throws BootcampException {
        return orderService.getById(id);
    }

    @PostMapping("/orders")
    public Order createOrder(@RequestBody Order order) throws BootcampException {
        return orderService.create(order);
    }

    @PutMapping("/orders/{id}")
    public Order updateOrder(@PathVariable("id") long id, @RequestBody Order updatedOrder) throws BootcampException {
        updatedOrder.setId(id);
        return orderService.update(updatedOrder);
    }

    @DeleteMapping("/orders/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrder(@PathVariable("id") long id) throws BootcampException {
        orderService.delete(id);
    }
}
