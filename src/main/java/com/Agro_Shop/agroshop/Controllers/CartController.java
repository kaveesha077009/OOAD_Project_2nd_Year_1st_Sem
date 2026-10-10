package com.Agro_Shop.agroshop.Controllers;

import com.Agro_Shop.agroshop.Dto.AddToCartRequest;
import com.Agro_Shop.agroshop.Dto.CartResponse;
import com.Agro_Shop.agroshop.Services.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/** Customer shopping cart. customerId will later come from the logged-in user. */
@RestController
@RequestMapping("/api/cart/{customerId}")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public CartResponse viewCart(@PathVariable Long customerId) {
        return cartService.getCart(customerId);
    }

    @PostMapping("/items")
    public CartResponse addItem(@PathVariable Long customerId, @RequestBody AddToCartRequest request) {
        return cartService.addToCart(customerId, request.productId(), request.quantity());
    }

    @PutMapping("/items/{productId}")
    public CartResponse updateItem(@PathVariable Long customerId, @PathVariable Long productId,
                                   @RequestParam BigDecimal quantity) {
        return cartService.updateQuantity(customerId, productId, quantity);
    }

    @DeleteMapping("/items/{productId}")
    public CartResponse removeItem(@PathVariable Long customerId, @PathVariable Long productId) {
        return cartService.removeItem(customerId, productId);
    }

    @DeleteMapping
    public void clearCart(@PathVariable Long customerId) {
        cartService.clearCart(customerId);
    }
}
