/**
 * Trade proposal creation page (/trade/:friendId).
 */
import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { calculateTrade, proposeTrade } from '../services/tradeService';
import './TradePage.css';

export default function TradePage() {
  const { friendId } = useParams();
  const navigate = useNavigate();
  const { t } = useTranslation();
  const [calc, setCalc] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [sending, setSending] = useState(false);
  const [selectedWant, setSelectedWant] = useState(new Set());
  const [selectedOffer, setSelectedOffer] = useState(new Set());

  useEffect(() => {
    calculateTrade(Number(friendId))
      .then(setCalc)
      .catch(e => setError(e.message))
      .finally(() => setLoading(false));
  }, [friendId]);

  function toggleWant(code) {
    setSelectedWant(prev => {
      const next = new Set(prev);
      next.has(code) ? next.delete(code) : next.add(code);
      return next;
    });
  }

  function toggleOffer(code) {
    setSelectedOffer(prev => {
      const next = new Set(prev);
      next.has(code) ? next.delete(code) : next.add(code);
      return next;
    });
  }

  const wantCount = selectedWant.size;
  const offerCount = selectedOffer.size;
  const balanced = wantCount > 0 && offerCount > 0 && wantCount === offerCount;
  const overMax = calc && wantCount > calc.maxTrades;

  async function handlePropose() {
    if (!balanced || overMax || sending) return;
    setSending(true);
    try {
      await proposeTrade(
        Number(friendId),
        [...selectedOffer],
        [...selectedWant]
      );
      navigate(`/chat/${friendId}`);
    } catch (e) {
      setError(e.message);
    } finally {
      setSending(false);
    }
  }

  if (loading) return <div className="trade-page__status">{t('trade.loading')}</div>;
  if (error) return <div className="trade-page__status trade-page__status--error">{error}</div>;
  if (!calc) return null;

  const counterText = [
    t('trade.counter', { want: wantCount, offer: offerCount }),
    !balanced && wantCount + offerCount > 0 ? t('trade.must_equal') : '',
    overMax ? t('trade.max_exceeded', { max: calc.maxTrades }) : '',
  ].join('');

  return (
    <div className="trade-page">
      <header className="trade-page__header">
        <button className="trade-page__back" onClick={() => navigate(`/chat/${friendId}`)}>←</button>
        <div>
          <h1 className="trade-page__title">{t('trade.title', { name: calc.friendDisplayName })}</h1>
          <p className="trade-page__subtitle">{t('trade.max', { count: calc.maxTrades })}</p>
        </div>
      </header>

      <div className="trade-page__counter">
        <span className={`trade-page__count ${!balanced ? 'trade-page__count--warn' : 'trade-page__count--ok'}`}>
          {counterText}
        </span>
      </div>

      <div className="trade-page__columns">
        <div className="trade-page__column">
          <h2 className="trade-page__col-title">
            {t('trade.receive_col')} <span className="trade-page__col-tag">{t('trade.duplicates_from', { tag: calc.friendUserTag })}</span>
          </h2>
          {calc.friendOfferings.length === 0
            ? <p className="trade-page__empty">{t('trade.no_need')}</p>
            : calc.friendOfferings.map(s => (
              <StickerRow
                key={s.code}
                sticker={s}
                selected={selectedWant.has(s.code)}
                onToggle={() => toggleWant(s.code)}
              />
            ))
          }
        </div>

        <div className="trade-page__column">
          <h2 className="trade-page__col-title">
            {t('trade.offer_col')} <span className="trade-page__col-tag">{t('trade.your_dupes')}</span>
          </h2>
          {calc.myOfferings.length === 0
            ? <p className="trade-page__empty">{t('trade.friend_no_need')}</p>
            : calc.myOfferings.map(s => (
              <StickerRow
                key={s.code}
                sticker={s}
                selected={selectedOffer.has(s.code)}
                onToggle={() => toggleOffer(s.code)}
              />
            ))
          }
        </div>
      </div>

      {error && <p className="trade-page__error">{error}</p>}

      <div className="trade-page__footer">
        <button
          className="trade-page__btn-propose"
          onClick={handlePropose}
          disabled={!balanced || overMax || sending}
        >
          {sending ? t('trade.sending') : t('trade.propose')}
        </button>
        <button className="trade-page__btn-cancel" onClick={() => navigate(`/chat/${friendId}`)}>
          {t('trade.cancel')}
        </button>
      </div>
    </div>
  );
}

function StickerRow({ sticker, selected, onToggle }) {
  return (
    <button
      className={`trade-page__sticker${selected ? ' trade-page__sticker--selected' : ''}`}
      onClick={onToggle}
    >
      <span className="trade-page__sticker-code">{sticker.code}</span>
      <span className="trade-page__sticker-name">{sticker.playerName || sticker.teamName}</span>
      <span className="trade-page__sticker-team">{sticker.teamInitial}</span>
      <span className="trade-page__sticker-check">{selected ? '✓' : ''}</span>
    </button>
  );
}
