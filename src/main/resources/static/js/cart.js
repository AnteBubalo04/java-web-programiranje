async function addToCart(tierId, quantityInputId = 'quantity') {
    const quantity = document.getElementById(quantityInputId).value;

    const response = await fetch('/api/cart/add', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({
            tierId: tierId,
            quantity: Number.parseInt(quantity)
        })
    });

    const msg = document.getElementById('cartMessage');
    if (response.ok) {
        msg.innerHTML = '<div class="alert alert-success mt-2">✓ Added to cart!</div>';
        setTimeout(() => msg.innerHTML = '', 3000);
    } else {
        msg.innerHTML = '<div class="alert alert-danger mt-2">Error adding to cart!</div>';
    }
}

async function removeFromCart(tierId) {
    const response = await fetch(`/api/cart/remove/${tierId}`, {
        method: 'DELETE'
    });
    if (response.ok) location.reload();
}

async function updateQuantity(tierId, quantity) {
    if (quantity <= 0) {
        await removeFromCart(tierId);
        return;
    }
    const response = await fetch(`/api/cart/update/${tierId}`, {
        method: 'PUT',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({quantity: quantity})
    });
    if (response.ok) location.reload();
}

async function clearCart() {
    const response = await fetch('/api/cart/clear', {
        method: 'DELETE'
    });
    if (response.ok) location.reload();
}