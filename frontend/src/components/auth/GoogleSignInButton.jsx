/**
 * Renders the Google Sign-In button using the Google Identity Services SDK.
 *
 * The SDK is loaded via a <script> tag in index.html. If it is not yet available
 * when this component mounts, initialization is deferred via `window.onGoogleLibraryLoad`.
 * The `initialized` ref prevents double-initialization in React StrictMode.
 *
 * @param {Function} onError - called with an error message string if sign-in fails
 */
import { useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { googleLogin } from '../../services/authService';

export default function GoogleSignInButton({ onError }) {
  const containerRef = useRef(null);
  const { saveSession } = useAuth();
  const navigate = useNavigate();
  const initialized = useRef(false);

  useEffect(() => {
    const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID;
    if (!clientId) return;

    function init() {
      if (initialized.current || !containerRef.current) return;
      const width = containerRef.current.offsetWidth || 300;
      initialized.current = true;

      window.google.accounts.id.initialize({
        client_id: clientId,
        callback: async ({ credential }) => {
          try {
            const data = await googleLogin(credential);
            saveSession(data.accessToken, data.refreshToken);
            navigate('/collection');
          } catch (e) {
            onError?.(e.message || 'Google login failed');
          }
        },
      });

      window.google.accounts.id.renderButton(containerRef.current, {
        theme: 'outline',
        size: 'large',
        text: 'continue_with',
        width,
      });
    }

    if (window.google?.accounts?.id) {
      init();
    } else {
      // Defer until the SDK fires its load callback
      const prev = window.onGoogleLibraryLoad;
      window.onGoogleLibraryLoad = () => { prev?.(); init(); };
    }
  }, []);

  return <div ref={containerRef} style={{ flex: 1, minWidth: 0 }} />;
}
