const currency = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' });

function updateCartTotals() {
  let itemCount = 0;
  let subtotal = 0;

  document.querySelectorAll('.cart-line').forEach((line) => {
    const quantityInput = line.querySelector('.cart-quantity');
    const quantity = Math.max(0, Math.min(25, Number(quantityInput.value) || 0));
    const lineTotal = Number(quantityInput.dataset.price) * quantity;
    quantityInput.value = String(quantity);
    itemCount += quantity;
    subtotal += lineTotal;
    line.querySelector('.line-total').textContent = currency.format(lineTotal);
  });

  document.querySelectorAll('[data-cart-count]').forEach((counter) => {
    counter.textContent = counter.tagName === 'SPAN' ? `(${itemCount})` : String(itemCount);
  });

  const subtotalLabel = document.querySelector('.cart-subtotal');
  if (subtotalLabel) subtotalLabel.textContent = currency.format(subtotal);
}

document.querySelectorAll('.cart-quantity').forEach((quantityInput) => {
  quantityInput.addEventListener('input', updateCartTotals);
  quantityInput.addEventListener('change', () => quantityInput.form.requestSubmit());
});