/**
 * Authentication service — wraps the public /auth/* endpoints.
 * These routes require no authorization header; the tokens they return
 * are stored by AuthContext (access token in memory, refresh in localStorage).
 */

const BASE = `${import.meta.env.VITE_API_URL}/auth`;

async function request(path, body) {
  const res = await fetch(`${BASE}${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });

  const data = await res.json().catch(() => ({}));

  if (!res.ok) {
    throw new Error(data.message ?? data.error ?? `Error ${res.status}`);
  }

  return data;
}

export function login(email, password) {
  return request('/login', { email, password });
}

export function register(displayName, email, password) {
  return request('/register', { displayName, email, password });
}

export function refreshTokens(refreshToken) {
  return request('/refresh', { refreshToken });
}

/** Authenticates using a Google ID token obtained from the frontend Google Sign-In SDK. */
export function googleLogin(idToken) {
  return request('/google', { idToken });
}
