/**
 * Sell/buy proposal creation page (/sell/:friendId).
 */
import { useState, useEffect } from 'react';
import { useNavigate, useParams, useLocation } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { calculateSell, calculateBuy, proposeSell, proposeBuy } from '../services/sellService';
import './SellPage.css';

export default function SellPage() {
  const { friendId } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const { t } = useTranslation();
  const mode = location.state?.mode ?? 'sell';
  const isSell = mode === 'sell';

  const [calc, setCalc] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [sending, setSending] = useState(false);
  const [selected, setSelected] = useState(new Set());
  const [price, setPrice] = useState('');
  const [batches, setBatches] = useState([]);

  const usedCodes = new Set(batches.flatMap(b => b.stickerCodes));

  useEffect(() => {
    const fn = isSell ? calculateSell : calculateBuy;
    fn(Number(friendId))
      .then(setCalc)
      .catch(e => setError(e.message))
      .finally(() => setLoading(false));
  }, [friendId, isSell]);

  function toggleSticker(code) {
    setSelected(prev => {
      const next = new Set(prev);
      next.has(code) ? next.delete(code) : next.add(code);
      return next;
    });
  }

  function confirmBatch() {
    if (selected.size === 0) return;
    const parsedPrice = parseFloat(price);
    if (isNaN(parsedPrice) || parsedPrice < 0) {
      setError(t('sell.valid_price'));
      return;
    }
    setError('');
    setBatches(prev => [...prev, {
      stickerCodes: [...selected],
      pricePerUnit: parsedPrice,
    }]);
    setSelected(new Set());
    setPrice('');
  }

  function removeBatch(index) {
    setBatches(prev => prev.filter((_, i) => i !== index));
  }

  async function handleFinalize() {
    if (batches.length === 0 || sending) return;
    setSending(true);
    setError('');
    try {
      const fn = isSell ? proposeSell : proposeBuy;
      await fn(Number(friendId), batches);
      navigate(`/chat/${friendId}`);
    } catch (e) {
      setError(e.message);
    } finally {
      setSending(false);
    }
  }

  const availableStickers = calc?.availableStickers.filter(s => !usedCodes.has(s.code)) ?? [];
  const total = batches.reduce((sum, b) => sum + b.pricePerUnit * b.stickerCodes.length, 0);

  if (loading) return <div className="sell-page__status">{t('sell.loading')}</div>;
  if (error && !calc) return <div className="sell-page__status sell-page__status--error">{error}</div>;

  return (
    <div className="sell-page">
      <header className="sell-page__header">
        <button className="sell-page__back" onClick={() => navigate(`/chat/${friendId}`)}>←</button>
        <div>
          <h1 className="sell-page__title">
            {isSell
              ? t('sell.title_sell', { name: calc?.friendDisplayName })
              : t('sell.title_buy', { name: calc?.friendDisplayName })}
          </h1>
          <p className="sell-page__subtitle">
            {isSell
              ? t('sell.subtitle_sell', { tag: calc?.friendUserTag })
              : t('sell.subtitle_buy', { tag: calc?.friendUserTag })}
          </p>
        </div>
      </header>

      <div className="sell-page__body">
        <div className="sell-page__controls">
          <div className="sell-page__price-row">
            <label className="sell-page__price-label">{t('sell.price_per')}</label>
            <input
              className="sell-page__price-input"
              type="number"
              min="0"
              step="0.05"
              placeholder="0.25"
              value={price}
              onChange={e => setPrice(e.target.value)}
            />
            <span className="sell-page__price-unit">€</span>
          </div>
          <button
            className="sell-page__btn-confirm"
            onClick={confirmBatch}
            disabled={selected.size === 0}
          >
            {t('sell.confirm', { count: selected.size })}
          </button>
          {error && <p className="sell-page__error">{error}</p>}
        </div>

        <div className="sell-page__stickers">
          <h2 className="sell-page__section-title">
            {t('sell.available', { count: availableStickers.length })}
          </h2>
          {availableStickers.length === 0 ? (
            <p className="sell-page__empty">
              {calc?.availableStickers.length === 0
                ? (isSell ? t('sell.no_sell_dupes') : t('sell.no_buy_dupes'))
                : t('sell.all_in_groups')}
            </p>
          ) : (
            <div className="sell-page__sticker-list">
              {availableStickers.map(s => (
                <button
                  key={s.code}
                  className={`sell-page__sticker${selected.has(s.code) ? ' sell-page__sticker--selected' : ''}`}
                  onClick={() => toggleSticker(s.code)}
                >
                  <span className="sell-page__sticker-code">{s.code}</span>
                  <span className="sell-page__sticker-name">{s.playerName || s.teamName}</span>
                  <span className="sell-page__sticker-team">{s.teamInitial}</span>
                  <span className="sell-page__sticker-check">{selected.has(s.code) ? '✓' : ''}</span>
                </button>
              ))}
            </div>
          )}
        </div>

        {batches.length > 0 && (
          <div className="sell-page__batches">
            <h2 className="sell-page__section-title">{t('sell.sale_list')}</h2>
            {batches.map((batch, i) => {
              const batchTotal = (batch.pricePerUnit * batch.stickerCodes.length).toFixed(2);
              return (
                <div key={i} className="sell-page__batch">
                  <div className="sell-page__batch-info">
                    <span className="sell-page__batch-codes">{batch.stickerCodes.join(', ')}</span>
                    <span className="sell-page__batch-price">
                      {batchTotal}€ ({batch.pricePerUnit.toFixed(2)}€ each)
                    </span>
                  </div>
                  <button className="sell-page__batch-remove" onClick={() => removeBatch(i)}>×</button>
                </div>
              );
            })}
            <div className="sell-page__total">
              {t('sell.total')} <strong>{total.toFixed(2)}€</strong>
            </div>
          </div>
        )}
      </div>

      <div className="sell-page__footer">
        <button
          className="sell-page__btn-finalize"
          onClick={handleFinalize}
          disabled={batches.length === 0 || sending}
        >
          {sending ? t('sell.sending') : (isSell ? t('sell.finalize_sell') : t('sell.finalize_buy'))}
        </button>
        <button className="sell-page__btn-cancel" onClick={() => navigate(`/chat/${friendId}`)}>
          {t('sell.cancel')}
        </button>
      </div>
    </div>
  );
}
