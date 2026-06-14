/**
 * Modal for sharing the user's sticker list via WhatsApp, Telegram, Messenger, or clipboard.
 *
 * Flow:
 *   1. User selects a category (MISSING, DUPLICATE, OWNED, or ALL).
 *   2a. If mode is "whatsapp", opens WhatsApp directly with a pre-built message.
 *   2b. Otherwise, shows a second step to choose the sharing platform.
 *
 * The shared text is formatted as team-grouped sticker codes (e.g. "BRA: 1, 3, 5").
 * Teams are sorted by their lowest page number so the output matches the physical album order.
 * Messenger does not support text pre-fill, so the text is copied to the clipboard first.
 *
 * @param {string} mode - "whatsapp" (skips platform step) | "general"
 * @param {Object[]} stickers - all stickers with status and teamInitial fields
 * @param {Function} onClose
 */
import { useState } from 'react';
import './ShareModal.css';

const CATEGORIES = [
  { id: 'MISSING',   label: 'Missing' },
  { id: 'DUPLICATE', label: 'Duplicates' },
  { id: 'OWNED',     label: 'Owned' },
  { id: 'ALL',       label: 'All' },
];

const FLAG_EMOJI = {
  ALG: '🇩🇿', ARG: '🇦🇷', AUS: '🇦🇺', AUT: '🇦🇹', BEL: '🇧🇪',
  BIH: '🇧🇦', BRA: '🇧🇷', CAN: '🇨🇦', CIV: '🇨🇮', COD: '🇨🇩',
  COL: '🇨🇴', CPV: '🇨🇻', CRO: '🇭🇷', CUW: '🇨🇼', CZE: '🇨🇿',
  ECU: '🇪🇨', EGY: '🇪🇬', ENG: '🏴󠁧󠁢󠁥󠁮󠁧󠁿', EPS: '🇪🇸', FRA: '🇫🇷',
  FWC: '🌍', GER: '🇩🇪', GHA: '🇬🇭', HAI: '🇭🇹', IRN: '🇮🇷',
  IRQ: '🇮🇶', JOR: '🇯🇴', JPN: '🇯🇵', KOR: '🇰🇷', KSA: '🇸🇦',
  MAR: '🇲🇦', MEX: '🇲🇽', NED: '🇳🇱', NOR: '🇳🇴', NZL: '🇳🇿',
  PAN: '🇵🇦', PAR: '🇵🇾', POR: '🇵🇹', QAT: '🇶🇦', RSA: '🇿🇦',
  SCO: '🏴󠁧󠁢󠁳󠁣󠁴󠁿', SEN: '🇸🇳', SUI: '🇨🇭', SWE: '🇸🇪', TUN: '🇹🇳',
  TUR: '🇹🇷', URU: '🇺🇾', USA: '🇺🇸', UZB: '🇺🇿',
};

function getSubset(stickers, categoryId) {
  switch (categoryId) {
    case 'MISSING':   return stickers.filter(s => s.status === 'MISSING');
    case 'DUPLICATE': return stickers.filter(s => s.status === 'DUPLICATE');
    case 'OWNED':     return stickers.filter(s => s.status === 'OWNED' || s.status === 'DUPLICATE');
    default:          return stickers;
  }
}

function buildSection(stickers, categoryId) {
  const subset = getSubset(stickers, categoryId);
  if (subset.length === 0) return '(none)';

  const groups = {};
  for (const s of subset) {
    if (!groups[s.teamInitial]) groups[s.teamInitial] = [];
    groups[s.teamInitial].push(s);
  }

  return Object.entries(groups)
    .sort(([, a], [, b]) =>
      Math.min(...a.map(s => s.pageNumber)) - Math.min(...b.map(s => s.pageNumber))
    )
    .map(([prefix, items]) => {
      const flag = FLAG_EMOJI[prefix] ?? '';
      const nums = items
        .sort((a, b) => a.pageNumber - b.pageNumber)
        .map(s => s.code.startsWith(prefix) ? s.code.slice(prefix.length) : s.code);
      return `${prefix}${flag}: ${nums.join(', ')}`;
    })
    .join('\n');
}

function buildMessage(stickers, categoryId, categoryLabel) {
  const mainSection = buildSection(stickers, categoryId);
  const ownedSection  = buildSection(stickers, 'OWNED');
  const missingSection = buildSection(stickers, 'MISSING');
  const dupSection    = buildSection(stickers, 'DUPLICATE');

  return [
    `WC 2026 - ${categoryLabel}`,
    mainSection,
    '',
    `Owned:\n${ownedSection}`,
    `Missing:\n${missingSection}`,
    `Duplicates:\n${dupSection}`,
  ].join('\n');
}

export default function ShareModal({ mode, stickers, onClose }) {
  const [step, setStep]           = useState('category');
  const [category, setCategory]   = useState(null);
  const [feedback, setFeedback]   = useState('');

  function showFeedback(msg) {
    setFeedback(msg);
    setTimeout(() => setFeedback(''), 3000);
  }

  function handleCategoryClick(cat) {
    if (mode === 'whatsapp') {
      const msg = buildMessage(stickers, cat.id, cat.label);
      window.open(`https://wa.me/?text=${encodeURIComponent(msg)}`, '_blank');
      onClose();
    } else {
      setCategory(cat);
      setStep('platform');
    }
  }

  async function handleCopy() {
    const msg = buildMessage(stickers, category.id, category.label);
    try {
      await navigator.clipboard.writeText(msg);
      showFeedback('Text copied!');
    } catch {
      showFeedback('Could not copy.');
    }
  }

  function handleTelegram() {
    const msg = buildMessage(stickers, category.id, category.label);
    const url = encodeURIComponent(window.location.href);
    window.open(`https://t.me/share/url?url=${url}&text=${encodeURIComponent(msg)}`, '_blank');
    onClose();
  }

  async function handleMessenger() {
    const msg = buildMessage(stickers, category.id, category.label);
    try {
      await navigator.clipboard.writeText(msg);
    } catch { /* ignore */ }
    window.open('https://www.messenger.com/', '_blank');
    showFeedback('Text copied! Paste in Messenger.');
  }

  async function handleWebShare() {
    const msg = buildMessage(stickers, category.id, category.label);
    try {
      await navigator.share({ text: msg });
      onClose();
    } catch {
      // user cancelled
    }
  }

  return (
    <div className="share-modal__overlay" onClick={onClose}>
      <div className="share-modal" onClick={e => e.stopPropagation()}>
        <button className="share-modal__close" onClick={onClose} aria-label="Close">✕</button>

        {step === 'category' && (
          <>
            <h2 className="share-modal__title">
              {mode === 'whatsapp' ? 'Share on WhatsApp' : 'Share'}
            </h2>
            <p className="share-modal__subtitle">What do you want to share?</p>
            <div className="share-modal__categories">
              {CATEGORIES.map(cat => (
                <button
                  key={cat.id}
                  className="share-modal__cat-btn"
                  onClick={() => handleCategoryClick(cat)}
                >
                  {cat.label}
                </button>
              ))}
            </div>
          </>
        )}

        {step === 'platform' && category && (
          <>
            <button className="share-modal__back" onClick={() => setStep('category')}>
              ← Back
            </button>
            <h2 className="share-modal__title">{category.label}</h2>
            <p className="share-modal__subtitle">How do you want to share?</p>
            <div className="share-modal__platforms">
              <button className="share-modal__platform-btn" onClick={handleCopy}>
                <ClipboardIcon />
                Copy text
              </button>
              <button className="share-modal__platform-btn share-modal__platform-btn--messenger" onClick={handleMessenger}>
                <MessengerIcon />
                Messenger
              </button>
              <button className="share-modal__platform-btn share-modal__platform-btn--telegram" onClick={handleTelegram}>
                <TelegramIcon />
                Telegram
              </button>
              {typeof navigator !== 'undefined' && navigator.share && (
                <button className="share-modal__platform-btn" onClick={handleWebShare}>
                  <NativeShareIcon />
                  More options
                </button>
              )}
            </div>
            {feedback && <p className="share-modal__feedback">{feedback}</p>}
          </>
        )}
      </div>
    </div>
  );
}

function ClipboardIcon() {
  return (
    <svg className="share-modal__platform-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <rect x="9" y="2" width="6" height="4" rx="1" />
      <path d="M17 4h1a2 2 0 012 2v14a2 2 0 01-2 2H6a2 2 0 01-2-2V6a2 2 0 012-2h1" />
    </svg>
  );
}

function MessengerIcon() {
  return (
    <svg className="share-modal__platform-icon" viewBox="0 0 24 24">
      <path fill="#0099FF" d="M12 0C5.373 0 0 5.16 0 11.52c0 3.6 1.8 6.83 4.62 8.97v4.07L8.5 22.4a12.1 12.1 0 003.5.48C18.627 22.88 24 17.72 24 11.36S18.627 0 12 0zm1.19 15.52l-3.06-3.26-5.97 3.26L10.43 9l3.14 3.26L19.5 9l-6.31 6.52z"/>
    </svg>
  );
}

function TelegramIcon() {
  return (
    <svg className="share-modal__platform-icon" viewBox="0 0 24 24">
      <path fill="#2AABEE" d="M12 0C5.373 0 0 5.373 0 12s5.373 12 12 12 12-5.373 12-12S18.627 0 12 0zm5.894 8.221l-1.97 9.28c-.145.658-.537.818-1.084.508l-3-2.21-1.447 1.394c-.16.16-.295.295-.605.295l.213-3.053 5.56-5.023c.242-.213-.054-.333-.373-.12L7.17 13.667l-2.96-.924c-.643-.204-.657-.643.136-.953l11.57-4.461c.537-.194 1.006.131.978.892z"/>
    </svg>
  );
}

function NativeShareIcon() {
  return (
    <svg className="share-modal__platform-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M4 12v8a2 2 0 002 2h12a2 2 0 002-2v-8"/>
      <polyline points="16 6 12 2 8 6"/>
      <line x1="12" y1="2" x2="12" y2="15"/>
    </svg>
  );
}
