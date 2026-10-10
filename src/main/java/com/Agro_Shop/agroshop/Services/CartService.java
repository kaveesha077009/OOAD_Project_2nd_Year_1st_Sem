package com.Agro_Shop.agroshop.Services;

import com.Agro_Shop.agroshop.Dto.CartLine;
import com.Agro_Shop.agroshop.Dto.CartResponse;
import com.Agro_Shop.agroshop.Entities.CartItem;
import com.Agro_Shop.agroshop.Entities.Product;
import com.Agro_Shop.agroshop.Repositories.CartItemRepository;
import com.Agro_Shop.agroshop.Repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public CartResponse addToCart(Long customerId, Long productId, BigDecimal quantity) {
        requirePositive(quantity);
        Product product = findProduct(productId);

        CartItem item = cartItemRepository.findByCustomerIdAndProductId(customerId, productId)
                .orElseGet(() -> {
                    CartItem c = new CartItem();
                    c.setCustomerId(customerId);
                    c.setProductId(productId);
                    c.setQuantity(BigDecimal.ZERO);
                    return c;
                });

        BigDecimal newQty = item.getQuantity().add(quantity);
        checkStock(product, newQty);
        item.setQuantity(newQty);
        cartItemRepository.save(item);
        return getCart(customerId);
    }

    @Transactional
    public CartResponse updateQuantity(Long customerId, Long productId, BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            return removeItem(customerId, productId);
        }
        Product product = findProduct(productId);
        checkStock(product, quantity);

        CartItem item = cartItemRepository.findByCustomerIdAndProductId(customerId, productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not in cart"));
        item.setQuantity(quantity);
        cartItemRepository.save(item);
        return getCart(customerId);
    }

    @Transactional
    public CartResponse removeItem(Long customerId, Long productId) {
        cartItemRepository.findByCustomerIdAndProductId(customerId, productId)
                .ifPresent(cartItemRepository::delete);
        return getCart(customerId);
    }

    @Transactional
    public void clearCart(Long customerId) {
        cartItemRepository.deleteByCustomerId(customerId);
    }

    public CartResponse getCart(Long customerId) {
        List<CartLine> lines = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem item : cartItemRepository.findByCustomerId(customerId)) {
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            if (product == null) continue; // product was deleted by inventory team

            BigDecimal subtotal = product.getPrice().multiply(item.getQuantity());
            lines.add(new CartLine(product.getId(), product.getName(), product.getPrice(),
                    item.getQuantity(), subtotal));
            total = total.add(subtotal);
        }
        return new CartResponse(lines, total);
    }

    // ---------- helpers ----------
    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    private void requirePositive(BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be greater than 0");
        }
    }

    private void checkStock(Product product, BigDecimal wanted) {
        BigDecimal stock = product.getStockQuantity();
        if (stock != null && stock.compareTo(wanted) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Not enough stock for " + product.getName() + " (available: " + stock + ")");
        }
    }
}
