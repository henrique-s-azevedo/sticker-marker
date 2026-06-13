/**
 * Authentication context — manages session state across the app.
 *
 * Token storage strategy:
 *   - Access token (15 min JWT) is kept in the module-level `memoryAccessToken`
 *     variable to reduce XSS exposure (never written to localStorage or the DOM).
 *   - Refresh token (7-day UUID) is persisted in localStorage under `sm_refresh_token`
 *     so the session survives page reloads.
 *
 * On mount, AuthProvider automatically re-hydrates the session via the stored
 * refresh token. While this is in progress, `loading` is true and ProtectedRoute
 * renders nothing, preventing a flash of the login page.
 *
 * Consumers:
 *   - `useAuth()` — access to { user, loading, saveSession, logout }
 *   - `getAccessToken()` — called by service modules to attach the Bearer header
 */

import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { refreshTokens } from '../services/authService';

const AuthContext = createContext(null);

const REFRESH_KEY = 'sm_refresh_token';
// Access token lives only in memory to reduce XSS exposure
let memoryAccessToken = null;

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null); // { email } decoded from token
  const [loading, setLoading] = useState(true);

  const saveSession = useCallback((accessToken, refreshToken) => {
    memoryAccessToken = accessToken;
    localStorage.setItem(REFRESH_KEY, refreshToken);
    // Decode email from JWT payload (no library needed for display purposes)
    try {
      const payload = JSON.parse(atob(accessToken.split('.')[1]));
      setUser({ email: payload.sub });
    } catch {
      setUser({});
    }
  }, []);

  const logout = useCallback(() => {
    memoryAccessToken = null;
    localStorage.removeItem(REFRESH_KEY);
    setUser(null);
  }, []);

  // On mount: try to restore session via stored refresh token
  useEffect(() => {
    const storedRefresh = localStorage.getItem(REFRESH_KEY);
    if (!storedRefresh) {
      setLoading(false);
      return;
    }
    refreshTokens(storedRefresh)
      .then(data => saveSession(data.accessToken, data.refreshToken))
      .catch(() => localStorage.removeItem(REFRESH_KEY))
      .finally(() => setLoading(false));
  }, [saveSession]);

  return (
    <AuthContext.Provider value={{ user, loading, saveSession, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}

/** Returns the current in-memory access token for use in Authorization headers. */
export function getAccessToken() {
  return memoryAccessToken;
}
