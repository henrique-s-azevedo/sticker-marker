/**
 * Modal for adding a friend via email, user tag, or QR code.
 */
import { useState, useEffect } from 'react';
import { QRCodeSVG } from 'qrcode.react';
import { useTranslation } from 'react-i18next';
import { addFriendByEmail, addFriendByTag } from '../../services/friendshipService';
import { getMyInvite } from '../../services/profileService';
import './AddFriendModal.css';

export default function AddFriendModal({ onClose, onSuccess }) {
  const { t } = useTranslation();
  const [mode, setMode]         = useState('email');
  const [value, setValue]       = useState('');
  const [loading, setLoading]   = useState(false);
  const [error, setError]       = useState('');
  const [success, setSuccess]   = useState('');
  const [invite, setInvite]     = useState(null);
  const [copied, setCopied]     = useState(false);

  const MODES = [
    { id: 'email', label: t('add_friend.by_email') },
    { id: 'tag',   label: t('add_friend.by_tag') },
    { id: 'qr',    label: t('add_friend.qr_code') },
  ];

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
      setSuccess(t('add_friend.request_sent'));
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
        <button className="add-friend-modal__close" onClick={onClose} aria-label="Close">✕</button>
        <h2 className="add-friend-modal__title">{t('add_friend.title')}</h2>

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
              placeholder={mode === 'email' ? t('add_friend.email_placeholder') : t('add_friend.tag_placeholder')}
              value={value}
              onChange={e => setValue(e.target.value)}
              required
              autoFocus
            />
            <button className="add-friend-modal__submit" type="submit" disabled={loading || !value.trim()}>
              {loading ? t('add_friend.sending') : t('add_friend.send_request')}
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
                  {t('add_friend.qr_hint')}
                </p>
                <div className="add-friend-modal__qr-link">
                  <span className="add-friend-modal__qr-url">{invite.inviteUrl}</span>
                  <button className="add-friend-modal__copy-btn" onClick={copyLink}>
                    {copied ? t('add_friend.copied') : t('add_friend.copy')}
                  </button>
                </div>
              </>
            ) : (
              !error && <p className="add-friend-modal__loading">{t('add_friend.generating')}</p>
            )}
          </div>
        )}

        {error   && <p className="add-friend-modal__error">{error}</p>}
        {success && <p className="add-friend-modal__success">{success}</p>}
      </div>
    </div>
  );
}
