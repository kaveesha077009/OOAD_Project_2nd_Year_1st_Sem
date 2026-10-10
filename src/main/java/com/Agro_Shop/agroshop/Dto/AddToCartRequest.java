package com.Agro_Shop.agroshop.Dto;

import java.math.BigDecimal;

public record AddToCartRequest(Long productId, BigDecimal quantity) {}
