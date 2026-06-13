/**
 * Modal for adding a friend via three methods:
 *   - By email
 *   - By user tag
 *   - QR code (generates the user's own invite link and renders a QR for others to scan)
 *
 * The QR mode fetches the invite link on demand when the tab is selected.
 * Sending a request by email or tag calls the friendship service and reports success inline.
 *
 * @param {Function} onClose
 * @param {Function} onSuccess - called after a request is sent (triggers friend list refresh)
 */
import { useState, useEffect } from 'react';
import { QRCodeSVG } from 'qrcode.react';
import { addFriendByEmail, addFriendByTag } from '../../services/friendshipService';
import { getMyInvite } from '../../services/profileService';
import './AddFriendModal.css';

const MODES = [
  { id: 'email', label: 'Por Email' },
  { id: 'tag',   label: 'Por Tag' },
  { id: 'qr',    label: 'QR Code' },
];

export default function AddFriendModal({ onClose, onSuccess }) {
  const [mode, setMode]         = useState('email');
  const [value, setValue]       = useState('');
  const [loading, setLoading]   = useState(false);
  const [error, setError]       = useState('');
  const [success, setSuccess]   = useState('');
  const [invite, setInvite]     = useState(null);
  const [copied, setCopied]     = useState(false);

  useEffect(() => {
    if (mode === 'qr') {
      getMyInvite().then(setInvite).catch(e => setError(e.message));
    }
  }, [mode]);

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);
    try {
      if (mode === 'email') {
        await addFriendByEmail(value.trim());
      } else {
        await addFriendByTag(value.trim());
      }
      setSuccess('Pedido enviado!');
      setValue('');
      onSuccess?.();
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  async function copyLink() {
    if (!invite) return;
    await navigator.clipboard.writeText(invite.inviteUrl);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  }

  return (
    <div className="add-friend-modal__overlay" onClick={onClose}>
      <div className="add-friend-modal" onClick={e => e.stopPropagation()}>
        <button className="add-friend-modal__close" onClick={onClose} aria-label="Fechar">✕</button>
        <h2 className="add-friend-modal__title">Adicionar Amigo</h2>

        <div className="add-friend-modal__modes">
          {MODES.map(m => (
            <button
              key={m.id}
              className={`add-friend-modal__mode-btn${mode === m.id ? ' add-friend-modal__mode-btn--active' : ''}`}
              onClick={() => { setMode(m.id); setError(''); setSuccess(''); setValue(''); }}
            >
              {m.label}
            </button>
          ))}
        </div>

        {mode !== 'qr' && (
          <form className="add-friend-modal__form" onSubmit={handleSubmit}>
            <input
              className="add-friend-modal__input"
              type={mode === 'email' ? 'email' : 'text'}
              placeholder={mode === 'email' ? 'email@exemplo.com' : '@usertag'}
              value={value}
              onChange={e => setValue(e.target.value)}
              required
              autoFocus
            />
            <button className="add-friend-modal__submit" type="submit" disabled={loading || !value.trim()}>
              {loading ? 'A enviar...' : 'Enviar pedido'}
            </button>
          </form>
        )}

        {mode === 'qr' && (
          <div className="add-friend-modal__qr">
            {invite ? (
              <>
                <QRCodeSVG
                  value={invite.inviteUrl}
                  size={200}
                  bgColor="transparent"
                  fgColor="#ffffff"
                />
                <p className="add-friend-modal__qr-hint">
                  Outro utilizador lê este QR e envia-te um pedido de amizade.
                </p>
                <div className="add-friend-modal__qr-link">
                  <span className="add-friend-modal__qr-url">{invite.inviteUrl}</span>
                  <button className="add-friend-modal__copy-btn" onClick={copyLink}>
                    {copied ? 'Copiado!' : 'Copiar'}
                  </button>
                </div>
              </>
            ) : (
              !error && <p className="add-friend-modal__loading">A gerar QR code...</p>
            )}
          </div>
        )}

        {error   && <p className="add-friend-modal__error">{error}</p>}
        {success && <p className="add-friend-modal__success">{success}</p>}
      </div>
    </div>
  );
}
