package com.ecom.shop_backend.order;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "orders")
public class CustomerOrder {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "customer_name", nullable = false, length = 160)
	private String customerName;

	@Column(nullable = false, length = 254)
	private String email;

	@Column(name = "shipping_address", nullable = false, length = 1000)
	private String shippingAddress;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 24)
	private OrderStatus status;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal total;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OrderItem> items = new ArrayList<>();

	protected CustomerOrder() {
	}

	public CustomerOrder(String customerName, String email, String shippingAddress, BigDecimal total) {
		this.customerName = customerName;
		this.email = email;
		this.shippingAddress = shippingAddress;
		this.total = total;
		this.status = OrderStatus.PLACED;
		this.createdAt = Instant.now();
	}

	public void addItem(OrderItem item) {
		items.add(item);
	}

}
