
package com.Agro_Shop.agroshop.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class PageController {

    @GetMapping("/")
    public String home() { return "redirect:/shop"; }


    @GetMapping("/shop")
    public String shop() { return "shop"; }

    @GetMapping("/cart")
    public String cart() { return "cart"; }

    @GetMapping("/checkout")
    public String checkout() { return "checkout"; }

    @GetMapping("/order-success")
    public String orderSuccess() { return "order-success"; }

    @GetMapping("/my-orders")
    public String myOrders() { return "my-orders"; }
}
