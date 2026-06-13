/**
 * Trade service — the full trade proposal lifecycle.
 *
 * State flow from the frontend perspective:
 *   proposeTrade → PENDING_COUNTERPART
 *   respondTrade (accept) → PENDING_PROPOSER (if counterpart items override) or CONFIRMED
 *   confirmTrade (accept) → CONFIRMED
 *   completeTrade → COMPLETED (transfers stickers in the database)
 */

import { getAccessToken } from '../context/AuthContext';

const API_BASE = import.meta.env.VITE_API_URL;

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

/** Returns a TradeCalculationDTO — which stickers each party can offer and the max balanced swap size. */
export const calculateTrade  = (friendId)                                   => request(`/me/trades/calculate/${friendId}`);
export const proposeTrade    = (friendId, proposerItems, counterpartItems)  => request(`/me/trades/propose/${friendId}`, { method: 'POST', body: { proposerItems, counterpartItems } });
export const getMyTrades     = ()                                            => request('/me/trades');
export const getTrade        = (tradeId)                                     => request(`/me/trades/${tradeId}`);
/** If counterpartItems is provided, creates a counter-proposal (PENDING_PROPOSER). If null, moves to CONFIRMED. */
export const respondTrade    = (tradeId, accept, counterpartItems)           => request(`/me/trades/${tradeId}/respond`, { method: 'POST', body: { accept, counterpartItems } });
/** Proposer confirms or cancels after receiving a counter-proposal. */
export const confirmTrade    = (tradeId, accept)                             => request(`/me/trades/${tradeId}/confirm`, { method: 'POST', body: { accept } });
/** Marks the trade COMPLETED and transfers sticker ownership on the server. */
export const completeTrade   = (tradeId)                                     => request(`/me/trades/${tradeId}/complete`, { method: 'POST' });
