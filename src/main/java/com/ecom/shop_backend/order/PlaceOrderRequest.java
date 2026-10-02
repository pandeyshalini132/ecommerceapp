package com.ecom.shop_backend.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public record PlaceOrderRequest(
		@NotBlank @Size(max = 160) String customerName,
		@NotBlank @Email @Size(max = 254) String email,
		@NotBlank @Size(max = 1000) String shippingAddress,
		@NotEmpty @Size(max = 50) List<@Valid OrderLineRequest> items) {

	public record OrderLineRequest(
			@NotNull @Positive Long productId,
			@Positive @Max(99) int quantity) {
	}
}
