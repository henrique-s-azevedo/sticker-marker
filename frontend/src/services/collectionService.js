/**
 * Collection service — manages sticker ownership and duplicate inventory
 * for the authenticated user (all /me/* endpoints).
 *
 * The access token is retrieved from memory via getAccessToken(); no token
 * is explicitly passed to avoid prop-drilling throughout the app.
 */

import { getAccessToken } from '../context/AuthContext';

const API_BASE = import.meta.env.VITE_API_URL;

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

/** Returns all stickers in the collection annotated with the user's ownership status. */
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

/** Sending quantity=0 deletes the duplicate record on the server side. */
export function updateDuplicate(stickerCode, quantity) {
  return request(`/me/duplicates/${encodeURIComponent(stickerCode)}`, { method: 'PUT', body: { quantity } });
}

export function removeDuplicate(stickerCode) {
  return request(`/me/duplicates/${encodeURIComponent(stickerCode)}`, { method: 'DELETE' });
}
