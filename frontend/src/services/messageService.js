/**
 * Message service — direct messaging between friends.
 * System messages (trade/sell notifications) appear in conversations but
 * are created server-side; this service handles only user-initiated messages
 * and conversation state.
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

export const sendMessage      = (friendId, content) => request(`/me/messages/${friendId}`, { method: 'POST', body: { content } });
export const getConversation  = (friendId)          => request(`/me/messages/${friendId}`);
/** Returns all conversations sorted by most-recent message first. */
export const getConversations = ()                  => request('/me/conversations');
/** Marks all messages from friendId to the authenticated user as read. */
export const markRead         = (friendId)          => request(`/me/conversations/${friendId}/read`, { method: 'POST' });
/** Returns { count: number } — total unread messages across all conversations. */
export const getUnreadCount   = ()                  => request('/me/messages/unread');
