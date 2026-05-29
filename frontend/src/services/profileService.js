import { getAccessToken } from '../context/AuthContext';

const API_BASE = import.meta.env.VITE_API_URL ?? '/api';

async function request(path, { method = 'GET', body } = {}) {
  const res = await fetch(`${API_BASE}${path}`, {
    method,
    headers: {
      Authorization: `Bearer ${getAccessToken()}`,
      ...(body ? { 'Content-Type': 'application/json' } : {}),
    },
    ...(body ? { body: JSON.stringify(body) } : {}),
  });
  if (res.status === 204 || res.headers.get('content-length') === '0') return null;
  const data = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(data.message ?? data.error ?? `Error ${res.status}`);
  return data;
}

export const getProfile         = ()                     => request('/me/profile');
export const updateVisibility   = (visibility)           => request('/me/collection/visibility', { method: 'PUT', body: { visibility } });
export const changePassword     = (currentPassword, newPassword) => request('/me/change-password', { method: 'POST', body: { currentPassword, newPassword } });
export const getMyInvite        = ()                     => request('/me/invite');
