package com.saffronstone.model;

import java.math.BigDecimal;

public record FoodItem(long id, String name, String description, String category, BigDecimal price, String imageUrl, boolean vegetarian, double rating) {}