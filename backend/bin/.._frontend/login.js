document.getElementById('loginForm').addEventListener('submit', async event => {
    event.preventDefault();
    const button = event.submitter;
    const error = document.getElementById('errorMsg');
    error.textContent = ''; button.disabled = true;
    try {
        const data = await apiFetch('/auth/login', {
            method: 'POST', body: JSON.stringify({
                username: document.getElementById('username').value.trim(),
                password: document.getElementById('password').value
            })
        }, false);
        localStorage.setItem('jwt_token', data.token);
        localStorage.setItem('username', data.username);
        localStorage.setItem('roles', JSON.stringify(data.roles));
        localStorage.setItem('expirationTime', String(data.expirationTime));
        location.href = 'index.html';
    } catch (ex) { error.textContent = ex.message; }
    finally { button.disabled = false; }
});
