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

export const getFriends          = ()              => request('/me/friends');
export const removeFriend        = (friendId)      => request(`/me/friends/${friendId}`, { method: 'DELETE' });
export const getFriendRequests   = ()              => request('/me/friend-requests');
export const getSentRequests     = ()              => request('/me/friend-requests/sent');
export const getPendingCount     = ()              => request('/me/friend-requests/count');
export const addFriendByEmail    = (email)         => request('/me/friends/request/email', { method: 'POST', body: { email } });
export const addFriendByTag      = (userTag)       => request('/me/friends/request/tag', { method: 'POST', body: { userTag } });
export const acceptRequest       = (requestId)     => request(`/me/friend-requests/${requestId}/accept`, { method: 'POST' });
export const rejectRequest       = (requestId)     => request(`/me/friend-requests/${requestId}/reject`, { method: 'POST' });
export const searchUsers         = (q)             => request(`/users/search?q=${encodeURIComponent(q)}`);
export const getFriendStickers   = (userTag, cid)  => request(`/users/${userTag}/collection/${cid}/stickers`);
export const getFriendProgress   = (userTag, cid)  => request(`/users/${userTag}/collection/${cid}/progress`);
