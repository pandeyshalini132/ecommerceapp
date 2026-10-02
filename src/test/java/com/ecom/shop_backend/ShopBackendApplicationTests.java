package com.ecom.shop_backend;

import com.ecom.shop_backend.order.OrderResponse;
import com.ecom.shop_backend.order.OrderService;
import com.ecom.shop_backend.order.PlaceOrderRequest;
import com.ecom.shop_backend.order.InsufficientStockException;
import com.ecom.shop_backend.product.Product;
import com.ecom.shop_backend.product.ProductRepository;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@ActiveProfiles("test")
class ShopBackendApplicationTests {

	@Autowired
	private ProductRepository products;

	@Autowired
	private OrderService orderService;

	@Test
	void contextLoads() {
	}

	@Test
	@Transactional
	void placingOrderStoresSnapshotAndReservesInventory() {
		Product product = products.findByActiveTrueOrderByIdAsc().get(0);
		int startingStock = product.getStock();
		PlaceOrderRequest request = new PlaceOrderRequest(
				"Taylor Example",
				"taylor@example.com",
				"12 Garden Lane",
				List.of(
						new PlaceOrderRequest.OrderLineRequest(product.getId(), 1),
						new PlaceOrderRequest.OrderLineRequest(product.getId(), 1)));

		OrderResponse order = orderService.place(request);

		Assertions.assertEquals("PLACED", order.status());
		Assertions.assertEquals(product.getPrice().multiply(java.math.BigDecimal.valueOf(2)), order.total());
		Assertions.assertEquals(2, order.items().get(0).quantity());
		Assertions.assertEquals(startingStock - 2, product.getStock());
	}

	@Test
	@Transactional
	void insufficientInventoryDoesNotReserveAnyItems() {
		Product product = products.findByActiveTrueOrderByIdAsc().get(0);
		int startingStock = product.getStock();
		PlaceOrderRequest request = new PlaceOrderRequest(
				"Taylor Example",
				"taylor@example.com",
				"12 Garden Lane",
				List.of(new PlaceOrderRequest.OrderLineRequest(product.getId(), startingStock + 1)));

		Assertions.assertThrows(InsufficientStockException.class, () -> orderService.place(request));
		Assertions.assertEquals(startingStock, product.getStock());
	}
}
