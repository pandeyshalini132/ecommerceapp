package com.ecom.shop_backend.order;

import com.ecom.shop_backend.product.Product;
import com.ecom.shop_backend.product.ProductRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OrderService {

	private final ProductRepository products;
	private final OrderRepository orders;

	public OrderService(ProductRepository products, OrderRepository orders) {
		this.products = products;
		this.orders = orders;
	}

	@Transactional
	public OrderResponse place(PlaceOrderRequest request) {
		Map<Long, Integer> quantities = new TreeMap<>();
		for (PlaceOrderRequest.OrderLineRequest item : request.items()) {
			quantities.merge(item.productId(), item.quantity(), Integer::sum);
		}

		Map<Product, Integer> reservedProducts = new TreeMap<>((first, second) -> first.getId().compareTo(second.getId()));
		for (Map.Entry<Long, Integer> item : quantities.entrySet()) {
			Product product = products.findActiveByIdForUpdate(item.getKey())
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
							"Product " + item.getKey() + " was not found"));
			if (item.getValue() > product.getStock()) {
				throw new InsufficientStockException(product.getName());
			}
			reservedProducts.put(product, item.getValue());
		}

		BigDecimal total = reservedProducts.entrySet().stream()
				.map(item -> item.getKey().getPrice().multiply(BigDecimal.valueOf(item.getValue())))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		CustomerOrder order = new CustomerOrder(request.customerName().trim(), request.email().trim(),
				request.shippingAddress().trim(), total);
		for (Map.Entry<Product, Integer> item : reservedProducts.entrySet()) {
			Product product = item.getKey();
			int quantity = item.getValue();
			product.removeStock(quantity);
			order.addItem(new OrderItem(order, product, quantity));
		}

		return OrderResponse.from(orders.save(order));
	}
}
