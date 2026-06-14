import { useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../../context/AuthContext';
import { googleLogin } from '../../services/authService';

const LOCALE_MAP = { en: 'en', pt: 'pt-PT' };

export default function GoogleSignInButton({ onError, locale = 'en' }) {
  const containerRef = useRef(null);
  const { saveSession } = useAuth();
  const navigate = useNavigate();
  const { t } = useTranslation();
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
            onError?.(e.message || t('login.google_failed'));
          }
        },
      });

      window.google.accounts.id.renderButton(containerRef.current, {
        theme: 'outline',
        size: 'large',
        text: 'continue_with',
        width,
        locale: LOCALE_MAP[locale] ?? 'en',
      });
    }

    if (window.google?.accounts?.id) {
      init();
    } else {
      const prev = window.onGoogleLibraryLoad;
      window.onGoogleLibraryLoad = () => { prev?.(); init(); };
    }
  }, []);

  return <div ref={containerRef} style={{ flex: 1, minWidth: 0 }} />;
}
