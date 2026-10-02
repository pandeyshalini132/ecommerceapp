package com.ecom.shop_backend.product;

import java.math.BigDecimal;

public record ProductResponse(
		Long id,
		String sku,
		String name,
		String description,
		String category,
		BigDecimal price,
		String imageUrl,
		int stock) {

	static ProductResponse from(Product product) {
		return new ProductResponse(product.getId(), product.getSku(), product.getName(), product.getDescription(),
				product.getCategory(), product.getPrice(), product.getImageUrl(), product.getStock());
	}
}
