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

  if (!res.ok) {
    throw new Error(data.message ?? data.error ?? `Error ${res.status}`);
  }

  return data;
}

export function fetchStickers(collectionId) {
  return request(`/collections/${collectionId}/me/stickers`);
}

export function fetchProgress(collectionId) {
  return request(`/collections/${collectionId}/me/progress`);
}

export function markOwned(stickerCode) {
  return request('/me/stickers', { method: 'POST', body: { stickerCode } });
}

export function removeOwned(stickerCode) {
  return request(`/me/stickers/${encodeURIComponent(stickerCode)}`, { method: 'DELETE' });
}

export function addDuplicate(stickerCode, quantity) {
  return request('/me/duplicates', { method: 'POST', body: { stickerCode, quantity } });
}

export function updateDuplicate(stickerCode, quantity) {
  return request(`/me/duplicates/${encodeURIComponent(stickerCode)}`, { method: 'PUT', body: { quantity } });
}

export function removeDuplicate(stickerCode) {
  return request(`/me/duplicates/${encodeURIComponent(stickerCode)}`, { method: 'DELETE' });
}
