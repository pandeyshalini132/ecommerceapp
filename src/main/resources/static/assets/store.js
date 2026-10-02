const state = {
  products: [],
  category: "",
  query: "",
  sort: "featured",
  cart: JSON.parse(localStorage.getItem("common-goods-cart") || "{}"),
};

const grid = document.querySelector("#product-grid");
const statusMessage = document.querySelector("#shop-status");
const drawer = document.querySelector("#cart-drawer");
const money = (value) => new Intl.NumberFormat("en-US", { style: "currency", currency: "USD" }).format(value);
const escapeHtml = (value) => String(value).replace(/[&<>"']/g, (character) => ({
  "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;",
})[character]);

async function loadProducts() {
  statusMessage.textContent = "Finding the good stuff…";
  try {
    const response = await fetch("/api/products");
    if (!response.ok) throw new Error("The shop is having trouble loading. Please try again.");
    state.products = await response.json();
    statusMessage.textContent = "";
    buildFilters();
    renderProducts();
    renderCart();
  } catch (error) {
    statusMessage.textContent = error.message;
  }
}

function buildFilters() {
  const categories = [...new Set(state.products.map((product) => product.category))].sort();
  const filters = document.querySelector("#filters");
  filters.innerHTML = `<button class="filter-button ${state.category ? "" : "active"}" type="button" data-category="">Everything <span>${state.products.length}</span></button>`
    + categories.map((category) => `<button class="filter-button ${state.category === category ? "active" : ""}" type="button" data-category="${escapeHtml(category)}">${escapeHtml(category)}</button>`).join("");
}

function visibleProducts() {
  const query = state.query.trim().toLowerCase();
  const result = state.products.filter((product) =>
    (!state.category || product.category === state.category)
    && (!query || `${product.name} ${product.description} ${product.category}`.toLowerCase().includes(query)));
  if (state.sort === "price-asc") result.sort((a, b) => a.price - b.price);
  if (state.sort === "price-desc") result.sort((a, b) => b.price - a.price);
  if (state.sort === "name") result.sort((a, b) => a.name.localeCompare(b.name));
  return result;
}

function renderProducts() {
  const products = visibleProducts();
  if (!products.length) {
    grid.innerHTML = "";
    statusMessage.textContent = "Nothing here just yet. Try another search or category.";
    return;
  }
  statusMessage.textContent = "";
  grid.innerHTML = products.map((product, index) => `
    <article class="product-card">
      <div class="product-image-wrap">
        <img class="product-image" src="${escapeHtml(product.imageUrl)}" alt="${escapeHtml(product.name)}" loading="lazy">
        ${index < 2 && !state.category && !state.query ? '<span class="product-tag">A good find</span>' : ""}
        <button class="add-button" type="button" data-add="${product.id}" ${product.stock < 1 ? "disabled" : ""}>
          <span>${product.stock < 1 ? "Sold out" : "Add to bag"}</span><span aria-hidden="true">+</span>
        </button>
      </div>
      <div class="product-info"><div><h3 class="product-name">${escapeHtml(product.name)}</h3><p class="product-category">${escapeHtml(product.category)}</p></div><p class="product-price">${money(product.price)}</p></div>
    </article>`).join("");
}

function persistCart() {
  localStorage.setItem("common-goods-cart", JSON.stringify(state.cart));
  renderCart();
}

function cartLines() {
  return Object.entries(state.cart)
    .map(([id, quantity]) => {
      const product = state.products.find((item) => item.id === Number(id));
      return product ? { product, quantity: Math.min(quantity, product.stock) } : null;
    })
    .filter((line) => line && line.quantity > 0);
}

function renderCart() {
  const lines = cartLines();
  const count = lines.reduce((sum, line) => sum + line.quantity, 0);
  const subtotal = lines.reduce((sum, line) => sum + line.product.price * line.quantity, 0);
  document.querySelector("#cart-count").textContent = count;
  document.querySelector("#drawer-count").textContent = `(${count})`;
  document.querySelector("#cart-subtotal").textContent = money(subtotal);
  document.querySelector("#checkout-total").textContent = money(subtotal);
  const content = document.querySelector("#cart-content");
  content.innerHTML = lines.length ? lines.map(({ product, quantity }) => `
    <div class="cart-line">
      <img src="${escapeHtml(product.imageUrl)}" alt="">
      <div><h3>${escapeHtml(product.name)}</h3><p class="cart-line-price">${money(product.price)}</p>
        <div class="quantity-controls" aria-label="Quantity for ${escapeHtml(product.name)}">
          <button type="button" data-quantity="${product.id}" data-change="-1" aria-label="Decrease quantity">−</button>
          <span>${quantity}</span>
          <button type="button" data-quantity="${product.id}" data-change="1" aria-label="Increase quantity" ${quantity >= product.stock ? "disabled" : ""}>+</button>
        </div>
      </div>
      <button class="remove-line" type="button" data-remove="${product.id}">Remove</button>
    </div>`).join("") : '<p class="empty-cart">Your bag is taking a little breather.</p>';
  document.querySelector("#show-checkout").disabled = lines.length === 0;
  document.querySelector("#cart-footer").hidden = lines.length === 0;
  document.querySelector("#checkout-form").hidden = true;
  if (lines.length) {
    state.cart = Object.fromEntries(lines.map(({ product, quantity }) => [product.id, quantity]));
    localStorage.setItem("common-goods-cart", JSON.stringify(state.cart));
  }
}

function setDrawerOpen(open) {
  document.body.classList.toggle("cart-open", open);
  drawer.setAttribute("aria-hidden", String(!open));
  if (!open) showBag();
}

function showBag() {
  document.querySelector("#checkout-form").hidden = true;
  document.querySelector("#cart-footer").hidden = cartLines().length === 0;
}

let toastTimer;
function showToast(message) {
  const toast = document.querySelector("#toast");
  toast.textContent = message;
  toast.classList.add("visible");
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => toast.classList.remove("visible"), 2200);
}

document.querySelector("#filters").addEventListener("click", (event) => {
  const button = event.target.closest("[data-category]");
  if (!button) return;
  state.category = button.dataset.category;
  buildFilters();
  renderProducts();
});

document.querySelector("#product-grid").addEventListener("click", (event) => {
  const button = event.target.closest("[data-add]");
  if (!button) return;
  const product = state.products.find((item) => item.id === Number(button.dataset.add));
  if (!product || (state.cart[product.id] || 0) >= product.stock) return;
  state.cart[product.id] = (state.cart[product.id] || 0) + 1;
  persistCart();
  showToast(`${product.name} added to your bag`);
});

document.querySelector("#cart-content").addEventListener("click", (event) => {
  const remove = event.target.closest("[data-remove]");
  if (remove) {
    delete state.cart[remove.dataset.remove];
    persistCart();
    return;
  }
  const quantityButton = event.target.closest("[data-quantity]");
  if (!quantityButton) return;
  const id = quantityButton.dataset.quantity;
  const next = (state.cart[id] || 0) + Number(quantityButton.dataset.change);
  if (next < 1) delete state.cart[id];
  else state.cart[id] = next;
  persistCart();
});

document.querySelector("#search").addEventListener("input", (event) => {
  state.query = event.target.value;
  renderProducts();
});
document.querySelector("#sort-products").addEventListener("change", (event) => {
  state.sort = event.target.value;
  renderProducts();
});
document.querySelectorAll("[data-nav-category]").forEach((link) => link.addEventListener("click", () => {
  state.category = link.dataset.navCategory;
  buildFilters();
  renderProducts();
}));
document.querySelector("#open-cart").addEventListener("click", () => setDrawerOpen(true));
document.querySelector("#close-cart").addEventListener("click", () => setDrawerOpen(false));
document.querySelector("#drawer-backdrop").addEventListener("click", () => setDrawerOpen(false));
document.addEventListener("keydown", (event) => {
  if (event.key === "Escape") setDrawerOpen(false);
});
document.querySelector("#show-checkout").addEventListener("click", () => {
  document.querySelector("#cart-footer").hidden = true;
  document.querySelector("#checkout-form").hidden = false;
  document.querySelector("#checkout-form input").focus();
});
document.querySelector("#back-to-bag").addEventListener("click", showBag);

document.querySelector("#checkout-form").addEventListener("submit", async (event) => {
  event.preventDefault();
  const form = event.currentTarget;
  const errorMessage = document.querySelector("#checkout-error");
  errorMessage.textContent = "";
  const submit = form.querySelector('button[type="submit"]');
  submit.disabled = true;
  try {
    const response = await fetch("/api/orders", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        customerName: form.elements.customerName.value,
        email: form.elements.email.value,
        shippingAddress: form.elements.shippingAddress.value,
        items: cartLines().map(({ product, quantity }) => ({ productId: product.id, quantity })),
      }),
    });
    const order = await response.json();
    if (!response.ok) throw new Error(order.message || "We couldn't place your order. Please try again.");
    state.cart = {};
    persistCart();
    form.reset();
    setDrawerOpen(false);
    await loadProducts();
    showToast(`Thank you! Order #${order.id} is placed.`);
  } catch (error) {
    errorMessage.textContent = error.message;
  } finally {
    submit.disabled = false;
  }
});

loadProducts();
