/**
 * Trade response page (/trade-respond/:tradeId).
 */
import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { getTrade, calculateTrade, respondTrade } from '../services/tradeService';
import { getAccessToken } from '../context/AuthContext';
import './TradePage.css';

export default function TradeRespondPage() {
  const { tradeId } = useParams();
  const navigate = useNavigate();
  const { t } = useTranslation();
  const [trade, setTrade] = useState(null);
  const [calc, setCalc] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [sending, setSending] = useState(false);
  const [selectedOffer, setSelectedOffer] = useState(new Set());
  const [myId, setMyId] = useState(null);

  useEffect(() => {
    const token = getAccessToken();
    if (token) {
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        setMyId(payload.userId);
      } catch { /* ignore */ }
    }
  }, []);

  useEffect(() => {
    getTrade(Number(tradeId))
      .then(async t => {
        setTrade(t);
        setSelectedOffer(new Set(t.counterpartItems.map(s => s.code)));
        const calc = await calculateTrade(t.proposerId).catch(() => null);
        setCalc(calc);
      })
      .catch(e => setError(e.message))
      .finally(() => setLoading(false));
  }, [tradeId]);

  function toggleOffer(code) {
    setSelectedOffer(prev => {
      const next = new Set(prev);
      next.has(code) ? next.delete(code) : next.add(code);
      return next;
    });
  }

  async function handleRespond(accept) {
    setSending(true);
    try {
      await respondTrade(Number(tradeId), accept, accept ? [...selectedOffer] : []);
      navigate(`/chat/${accept ? trade.proposerId : trade.proposerId}`);
    } catch (e) {
      setError(e.message);
    } finally {
      setSending(false);
    }
  }

  if (loading) return <div className="trade-page__status">{t('trade.loading')}</div>;
  if (error && !trade) return <div className="trade-page__status trade-page__status--error">{error}</div>;
  if (!trade) return null;

  const requiredCount = trade.proposerItems.length;
  const offerCount = selectedOffer.size;
  const valid = offerCount === requiredCount;
  const availableOfferings = calc?.myOfferings ?? [];

  return (
    <div className="trade-page">
      <header className="trade-page__header">
        <button className="trade-page__back" onClick={() => navigate(`/chat/${trade.proposerId}`)}>←</button>
        <div>
          <h1 className="trade-page__title">{t('trade_respond.title')}</h1>
          <p className="trade-page__subtitle">{t('trade_respond.from', { tag: trade.proposerUserTag })}</p>
        </div>
      </header>

      <div className="trade-page__counter">
        <span className={`trade-page__count ${valid ? 'trade-page__count--ok' : 'trade-page__count--warn'}`}>
          {t('trade_respond.select', { count: requiredCount, selected: offerCount })}
        </span>
      </div>

      <div className="trade-page__columns">
        <div className="trade-page__column">
          <h2 className="trade-page__col-title">
            {t('trade_respond.receive_from', { tag: trade.proposerUserTag })}
          </h2>
          {trade.proposerItems.map(s => (
            <div key={s.code} className="trade-page__sticker" style={{ cursor: 'default' }}>
              <span className="trade-page__sticker-code">{s.code}</span>
              <span className="trade-page__sticker-name">{s.playerName || s.teamName}</span>
              <span className="trade-page__sticker-team">{s.teamInitial}</span>
              <span className="trade-page__sticker-check">✓</span>
            </div>
          ))}
        </div>

        <div className="trade-page__column">
          <h2 className="trade-page__col-title">
            {t('trade_respond.give_to', { tag: trade.proposerUserTag })}
            <span className="trade-page__col-tag">{t('trade_respond.select_n', { count: requiredCount })}</span>
          </h2>
          {availableOfferings.length === 0 && trade.counterpartItems.map(s => (
            <button
              key={s.code}
              className={`trade-page__sticker${selectedOffer.has(s.code) ? ' trade-page__sticker--selected' : ''}`}
              onClick={() => toggleOffer(s.code)}
            >
              <span className="trade-page__sticker-code">{s.code}</span>
              <span className="trade-page__sticker-name">{s.playerName || s.teamName}</span>
              <span className="trade-page__sticker-team">{s.teamInitial}</span>
              <span className="trade-page__sticker-check">{selectedOffer.has(s.code) ? '✓' : ''}</span>
            </button>
          ))}
          {availableOfferings.map(s => (
            <button
              key={s.code}
              className={`trade-page__sticker${selectedOffer.has(s.code) ? ' trade-page__sticker--selected' : ''}`}
              onClick={() => toggleOffer(s.code)}
            >
              <span className="trade-page__sticker-code">{s.code}</span>
              <span className="trade-page__sticker-name">{s.playerName || s.teamName}</span>
              <span className="trade-page__sticker-team">{s.teamInitial}</span>
              <span className="trade-page__sticker-check">{selectedOffer.has(s.code) ? '✓' : ''}</span>
            </button>
          ))}
        </div>
      </div>

      {error && <p className="trade-page__error">{error}</p>}

      <div className="trade-page__footer">
        <button
          className="trade-page__btn-propose"
          onClick={() => handleRespond(true)}
          disabled={!valid || sending}
        >
          {sending ? t('trade_respond.sending') : t('trade_respond.accept')}
        </button>
        <button
          className="trade-page__btn-cancel"
          onClick={() => handleRespond(false)}
          disabled={sending}
        >
          {t('trade_respond.reject')}
        </button>
      </div>
    </div>
  );
}
