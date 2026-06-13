/**
 * Friendship service — friend list, friend requests, user search, and
 * read-only access to a friend's public collection.
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
  if (!res.ok) throw new Error(data.message ?? data.error ?? `Error ${res.status}`);
  return data;
}

export const getFriends          = ()              => request('/me/friends');
export const removeFriend        = (friendId)      => request(`/me/friends/${friendId}`, { method: 'DELETE' });
export const getFriendRequests   = ()              => request('/me/friend-requests');
export const getSentRequests     = ()              => request('/me/friend-requests/sent');
/** Returns { count: number } — used for the badge on the header profile button. */
export const getPendingCount     = ()              => request('/me/friend-requests/count');
export const addFriendByEmail    = (email)         => request('/me/friends/request/email', { method: 'POST', body: { email } });
export const addFriendByTag      = (userTag)       => request('/me/friends/request/tag', { method: 'POST', body: { userTag } });
export const acceptRequest       = (requestId)     => request(`/me/friend-requests/${requestId}/accept`, { method: 'POST' });
export const rejectRequest       = (requestId)     => request(`/me/friend-requests/${requestId}/reject`, { method: 'POST' });
/** Searches by display name or userTag. Requires at least 2 characters from the caller. */
export const searchUsers         = (q)             => request(`/users/search?q=${encodeURIComponent(q)}`);
/** Reads a friend's sticker list — only works if their collection visibility allows it. */
export const getFriendStickers   = (userTag, cid)  => request(`/users/${userTag}/collection/${cid}/stickers`);
export const getFriendProgress   = (userTag, cid)  => request(`/users/${userTag}/collection/${cid}/progress`);
