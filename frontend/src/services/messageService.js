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

export const sendMessage      = (friendId, content) => request(`/me/messages/${friendId}`, { method: 'POST', body: { content } });
export const getConversation  = (friendId)          => request(`/me/messages/${friendId}`);
export const getConversations = ()                  => request('/me/conversations');
export const markRead         = (friendId)          => request(`/me/conversations/${friendId}/read`, { method: 'POST' });
export const getUnreadCount   = ()                  => request('/me/messages/unread');
