package com.ecom.shop_backend.product;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/products")
public class ProductController {

	private final ProductRepository products;

	public ProductController(ProductRepository products) {
		this.products = products;
	}

	@GetMapping
	public List<ProductResponse> list(
			@RequestParam(required = false) String q,
			@RequestParam(required = false) String category) {
		String query = q == null ? "" : q.trim().toLowerCase();
		String selectedCategory = category == null ? "" : category.trim();
		return products.findByActiveTrueOrderByIdAsc().stream()
				.filter(product -> selectedCategory.isEmpty() || product.getCategory().equalsIgnoreCase(selectedCategory))
				.filter(product -> query.isEmpty()
						|| product.getName().toLowerCase().contains(query)
						|| product.getDescription().toLowerCase().contains(query)
						|| product.getCategory().toLowerCase().contains(query))
				.map(ProductResponse::from)
				.toList();
	}

	@GetMapping("/{id}")
	public ProductResponse get(@PathVariable Long id) {
		Product product = products.findById(id)
				.filter(Product::isActive)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
		return ProductResponse.from(product);
	}
}
