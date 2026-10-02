package com.ecom.shop_backend.order;

import com.ecom.shop_backend.product.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

    @Getter
    @Column(name = "product_name", nullable = false, length = 160)
	private String productName;

	@Getter
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal unitPrice;

	@Getter
    @Column(nullable = false)
	private int quantity;

	@Getter
    @Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal subtotal;

	protected OrderItem() {
	}

	public OrderItem(CustomerOrder order, Product product, int quantity) {
        this.productName = product.getName();
		this.unitPrice = product.getPrice();
		this.quantity = quantity;
		this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
	}

}
