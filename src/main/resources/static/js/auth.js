async function register() {
    const username = document.getElementById('username').value;
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const errorDiv = document.getElementById('errorMessage');

    errorDiv.innerHTML = '';

    const response = await fetch('/api/auth/register', {
        method: 'POST',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        body: `username=${encodeURIComponent(username)}&email=${encodeURIComponent(email)}&password=${encodeURIComponent(password)}`
    });

    if (response.ok) {
        this.location.href = '/auth/login?registered=true';
    } else {
        const error = await response.text();
        errorDiv.innerHTML = `<div class="alert alert-danger mt-2">${error}</div>`;
    }
}