package com.saffronstone.model;

import java.math.BigDecimal;

public record CartLine(FoodItem item, int quantity) {
    public BigDecimal lineTotal() { return item.price().multiply(BigDecimal.valueOf(quantity)); }
}