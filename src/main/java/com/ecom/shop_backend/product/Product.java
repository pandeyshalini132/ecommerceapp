package com.ecom.shop_backend.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 64)
	private String sku;

	@Column(nullable = false, length = 160)
	private String name;

	@Column(nullable = false, length = 2000)
	private String description;

	@Column(nullable = false, length = 80)
	private String category;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal price;

	@Column(name = "image_url", nullable = false, length = 500)
	private String imageUrl;

	@Column(nullable = false)
	private int stock;

	@Column(nullable = false)
	private boolean active;

	protected Product() {
	}

	public Product(String sku, String name, String description, String category, BigDecimal price, String imageUrl,
			int stock) {
		this.sku = sku;
		this.name = name;
		this.description = description;
		this.category = category;
		this.price = price;
		this.imageUrl = imageUrl;
		this.stock = stock;
		this.active = true;
	}

	public Long getId() {
		return id;
	}

	public String getSku() {
		return sku;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getCategory() {
		return category;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public int getStock() {
		return stock;
	}

	public boolean isActive() {
		return active;
	}

	public void removeStock(int quantity) {
		if (quantity < 1 || quantity > stock) {
			throw new IllegalArgumentException("Quantity exceeds available stock");
		}
		stock -= quantity;
	}
}
