// /static/js/auth.js
window.Auth = (function () {
  const TOKEN_KEY = 'innovate_jwt';

  function saveToken(token) {
    localStorage.setItem(TOKEN_KEY, token);
  }
  function getToken() {
    return localStorage.getItem(TOKEN_KEY);
  }
  function removeToken() {
    localStorage.removeItem(TOKEN_KEY);
  }

  // Basic JWT decode (payload only) - no validation (server does validation on each request)
  function decodePayload(token) {
    if (!token) return null;
    try {
      const parts = token.split('.');
      if (parts.length !== 3) return null;
      const payload = parts[1];
      const json = atob(payload.replace(/-/g,'+').replace(/_/g,'/'));
      return JSON.parse(decodeURIComponent(escape(json)));
    } catch (e) {
      return null;
    }
  }

  async function login(username, password) {
    const resp = await fetch('/auth/login', {
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify({ username, password })
    });
    if (!resp.ok) {
      const txt = await resp.text().catch(()=>null) || 'Login failed';
      throw new Error(txt);
    }
    const data = await resp.json();
    if (!data.token) throw new Error('No token returned');
    saveToken(data.token);
    return data.token;
  }

  function logout() {
    removeToken();
    // redirect optionally
    window.location.href = '/login-page';
  }

  function getUsername() {
    const payload = decodePayload(getToken());
    return payload ? payload.sub || payload.username : null;
  }

  function getRole() {
    const payload = decodePayload(getToken());
    // we stored role claim as 'role' (see JwtUtils.generateToken)
    return payload ? payload.role : null;
  }

  function isAdmin() {
    const role = getRole();
    if (!role) return false;
    return role === 'ROLE_ADMIN' || role === 'ADMIN';
  }

  // convenience wrapper to include Authorization header
  async function fetchWithAuth(url, opts = {}) {
  try {
        document.getElementById("mask").style.display = "flex";
        opts.headers = opts.headers || {};
        const token = getToken();
        if (token) opts.headers['Authorization'] = 'Bearer ' + token;
        return fetch(url, opts);
       } finally {
             document.getElementById("mask").style.display = "none";
       }
  }

  return {
    login,
    logout,
    getToken,
    getUsername,
    getRole,
    isAdmin,
    fetchWithAuth
  };
})();