package com.ecom.shop_backend.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
		Long id,
		String customerName,
		String email,
		String shippingAddress,
		String status,
		BigDecimal total,
		Instant createdAt,
		List<Item> items) {

	static OrderResponse from(CustomerOrder order) {
		List<Item> items = order.getItems().stream()
				.map(item -> new Item(item.getProductName(), item.getUnitPrice(), item.getQuantity(), item.getSubtotal()))
				.toList();
		return new OrderResponse(order.getId(), order.getCustomerName(), order.getEmail(), order.getShippingAddress(),
				order.getStatus().name(), order.getTotal(), order.getCreatedAt(), items);
	}

	public record Item(String productName, BigDecimal unitPrice, int quantity, BigDecimal subtotal) {
	}
}
