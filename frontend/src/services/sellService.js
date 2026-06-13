/**
 * Sell service — buy/sell proposal lifecycle between friends.
 *
 * Two proposal directions:
 *   proposeSell — seller initiates (SELL_PROPOSAL system message)
 *   proposeBuy  — buyer initiates (BUY_PROPOSAL system message)
 * Both create the same SellProposal model; the message type signals direction.
 *
 * State machine: PENDING → COMPLETED (completeSell) or CANCELLED (cancelSell).
 * Completing transfers sticker ownership on the server.
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

/** Returns SellCalculationDTO: the seller's duplicates that the buyer is missing. */
export const calculateSell  = (friendId)               => request(`/me/sell/calculate-sell/${friendId}`);
/** Returns SellCalculationDTO: the friend's duplicates that the current user is missing (buy perspective). */
export const calculateBuy   = (friendId)               => request(`/me/sell/calculate-buy/${friendId}`);
export const proposeSell    = (friendId, batches)      => request(`/me/sell/propose-sell/${friendId}`, { method: 'POST', body: { batches } });
export const proposeBuy     = (friendId, batches)      => request(`/me/sell/propose-buy/${friendId}`,  { method: 'POST', body: { batches } });
/** Accepts the proposal and transfers sticker ownership to the buyer. */
export const completeSell   = (sellId)                 => request(`/me/sell/${sellId}/complete`, { method: 'POST' });
export const cancelSell     = (sellId)                 => request(`/me/sell/${sellId}/cancel`,   { method: 'POST' });
export const getSell        = (sellId)                 => request(`/me/sell/${sellId}`);
