package com.Agro_Shop.agroshop.Dto;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(List<CartLine> items, BigDecimal total) {}
