package com.Agro_Shop.agroshop.Dto;

import java.math.BigDecimal;

public record CartLine(Long productId, String productName, BigDecimal unitPrice,
                       BigDecimal quantity, BigDecimal subtotal) {}
