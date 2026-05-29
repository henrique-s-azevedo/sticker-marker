import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const API_BASE = import.meta.env.VITE_API_URL ?? '/api';

export default function InvitePage() {
  const { code }    = useParams();
  const navigate    = useNavigate();
  const { token }   = useAuth();
  const [status, setStatus] = useState('loading');
  const [message, setMessage] = useState('');

  useEffect(() => {
    if (!token) {
      sessionStorage.setItem('pendingInvite', code);
      navigate('/login');
      return;
    }

    fetch(`${API_BASE}/invite/${code}/accept`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${token}` },
    })
      .then(async res => {
        if (!res.ok) {
          const d = await res.json().catch(() => ({}));
          throw new Error(d.message ?? 'Erro ao aceitar convite');
        }
        setStatus('success');
        setMessage('Pedido de amizade enviado!');
        setTimeout(() => navigate('/profile'), 2000);
      })
      .catch(e => {
        setStatus('error');
        setMessage(e.message);
      });
  }, [code, token, navigate]);

  return (
    <div style={{ minHeight: '100vh', background: 'var(--color-bg)', display: 'flex', alignItems: 'center', justifyContent: 'center', flexDirection: 'column', gap: '16px', color: 'var(--color-text)' }}>
      {status === 'loading' && <p>A processar convite...</p>}
      {status === 'success' && (
        <>
          <p style={{ color: 'var(--color-stat-owned)', fontSize: '18px', fontWeight: 700 }}>✓ {message}</p>
          <p style={{ color: 'var(--color-text-muted)', fontSize: '13px' }}>A redirecionar para o teu perfil...</p>
        </>
      )}
      {status === 'error' && (
        <>
          <p style={{ color: 'var(--color-stat-missing)', fontSize: '16px' }}>{message}</p>
          <button onClick={() => navigate('/collection')} style={{ background: 'none', border: '1px solid var(--color-border)', borderRadius: '6px', padding: '8px 16px', color: 'var(--color-text-muted)', cursor: 'pointer' }}>
            Ir para a coleção
          </button>
        </>
      )}
    </div>
  );
}
