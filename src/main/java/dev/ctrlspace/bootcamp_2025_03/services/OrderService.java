package dev.ctrlspace.bootcamp_2025_03.services;

import dev.ctrlspace.bootcamp_2025_03.exceptions.BootcampException;
import dev.ctrlspace.bootcamp_2025_03.model.CartItem;
import dev.ctrlspace.bootcamp_2025_03.model.Order;
import dev.ctrlspace.bootcamp_2025_03.repository.OrderRepository;
import dev.ctrlspace.bootcamp_2025_03.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private OrderRepository orderRepository;
    private ProductRepository productRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    public List<Order> getAll() {
        return orderRepository.findAll();
    }

    public Order getById(long id) throws BootcampException {
        return orderRepository.findById(id)
                .orElseThrow(() -> new BootcampException(HttpStatus.NOT_FOUND, "Order with ID " + id + " not found."));
    }

    public Order create(Order order) throws BootcampException {
        // For each CartItem inside the order
        List<CartItem> cartItems = order.getCartItems();

        for (CartItem item : cartItems) {
            // Ensure product exists in the database
            var product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new BootcampException(HttpStatus.NOT_FOUND, "Product with ID " + item.getProduct().getId() + " not found."));

            // Check stock
            if (product.getStock() < item.getQuantity()) {
                throw new BootcampException(HttpStatus.BAD_REQUEST, "Not enough stock for product: " + product.getName());
            }

            // Reduce stock
            product.setStock(product.getStock() - item.getQuantity());
            productRepository.save(product); // Save updated product
            item.setProduct(product); // Link the product to the cart item
        }

        // Save the order
        return orderRepository.save(order);
    }

    public Order update(Order updatedOrder) throws BootcampException {
        // Retrieve the existing order from the repository
        Order existingOrder = orderRepository.findById(updatedOrder.getId())
                .orElseThrow(() -> new BootcampException(HttpStatus.NOT_FOUND, "Order with ID " + updatedOrder.getId() + " not found."));

        // If updating to 'cancelled', return products to stock
        if ("cancelled".equalsIgnoreCase(updatedOrder.getStatus()) && !"cancelled".equalsIgnoreCase(existingOrder.getStatus())) {
            returnStockToProducts(existingOrder); // Restore the stock for products in the cancelled order
        }

        // Update order status and any other details, if needed (e.g., date, userId, etc.)
        existingOrder.setStatus(updatedOrder.getStatus());
        // Here, you can update any other fields, for example:
        // existingOrder.setUser(updatedOrder.getUser());
        // existingOrder.setCreatedAt(updatedOrder.getCreatedAt());

        // Save the updated order in the database
        return orderRepository.save(existingOrder);
    }

    // Method to handle restoring stock when an order is cancelled
    private void returnStockToProducts(Order existingOrder) throws BootcampException {
        List<CartItem> cartItems = existingOrder.getCartItems();

        for (CartItem item : cartItems) {
            // Retrieve the latest product from the database
            var product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new BootcampException(HttpStatus.NOT_FOUND, "Product with ID " + item.getProduct().getId() + " not found."));

            // Increase stock back
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product); // Save updated product
        }
    }

    public void delete(long id) throws BootcampException {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BootcampException(HttpStatus.NOT_FOUND, "Order with ID " + id + " not found."));

        orderRepository.deleteById(id);
    }
}
