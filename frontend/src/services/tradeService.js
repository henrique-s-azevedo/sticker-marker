import { getAccessToken } from '../context/AuthContext';

const API_BASE = import.meta.env.VITE_API_URL ?? '/api';

async function request(path, { method = 'GET', body } = {}) {
  const res = await fetch(`${API_BASE}${path}`, {
    method,
    headers: {
      Authorization: `Bearer ${getAccessToken()}`,
      ...(body !== undefined ? { 'Content-Type': 'application/json' } : {}),
    },
    ...(body !== undefined ? { body: JSON.stringify(body) } : {}),
  });
  if (res.status === 204 || res.headers.get('content-length') === '0') return null;
  const data = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(data.message ?? data.error ?? `Error ${res.status}`);
  return data;
}

export const calculateTrade  = (friendId)                                   => request(`/me/trades/calculate/${friendId}`);
export const proposeTrade    = (friendId, proposerItems, counterpartItems)  => request(`/me/trades/propose/${friendId}`, { method: 'POST', body: { proposerItems, counterpartItems } });
export const getMyTrades     = ()                                            => request('/me/trades');
export const getTrade        = (tradeId)                                     => request(`/me/trades/${tradeId}`);
export const respondTrade    = (tradeId, accept, counterpartItems)           => request(`/me/trades/${tradeId}/respond`, { method: 'POST', body: { accept, counterpartItems } });
export const confirmTrade    = (tradeId, accept)                             => request(`/me/trades/${tradeId}/confirm`, { method: 'POST', body: { accept } });
export const completeTrade   = (tradeId)                                     => request(`/me/trades/${tradeId}/complete`, { method: 'POST' });
