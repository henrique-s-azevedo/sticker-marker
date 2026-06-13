/**
 * Profile service — authenticated user account settings and invite code.
 * Covers visibility control, the two-step password change flow, and invite generation.
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

/** Returns UserProfileDTO including pending friend request count and google account flag. */
export const getProfile               = ()                                          => request('/me/profile');
export const updateVisibility         = (visibility)                                 => request('/me/collection/visibility', { method: 'PUT', body: { visibility } });
/** Step 1 of password change: sends a verification code to the user's registered email. */
export const sendPasswordChangeCode   = ()                                           => request('/me/change-password/send-code', { method: 'POST' });
/** Step 2 of password change: submits the code along with current and new password. */
export const changePassword           = (currentPassword, newPassword, verificationCode) => request('/me/change-password', { method: 'POST', body: { currentPassword, newPassword, verificationCode } });
/** Returns InviteCodeResponseDTO with a reusable invite URL valid for 7 days. */
export const getMyInvite              = ()                                           => request('/me/invite');
