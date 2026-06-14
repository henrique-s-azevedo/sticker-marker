/**
 * Entry-point modal for trade/sell interactions with a specific friend.
 */
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { calculateTrade } from '../../services/tradeService';
import './TradeOptionsModal.css';

export default function TradeOptionsModal({ friend, onClose }) {
  const navigate = useNavigate();
  const { t } = useTranslation();
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');

  async function handleOption1() {
    setLoading(true);
    setError('');
    try {
      const data = await calculateTrade(friend.id);
      setResult(data);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  function handleGoToTrade() {
    onClose();
    navigate(`/trade/${friend.id}`);
  }

  return (
    <div className="trade-modal__overlay" onClick={onClose}>
      <div className="trade-modal" onClick={e => e.stopPropagation()}>
        <h2 className="trade-modal__title">{t('trade_modal.title', { name: friend.displayName })}</h2>

        {!result ? (
          <div className="trade-modal__options">
            <button
              className="trade-modal__option"
              onClick={handleOption1}
              disabled={loading}
            >
              <span className="trade-modal__option-icon">🔄</span>
              <div>
                <p className="trade-modal__option-title">{t('trade_modal.sticker_for_sticker')}</p>
                <p className="trade-modal__option-desc">{t('trade_modal.sticker_desc')}</p>
              </div>
            </button>

            <button
              className="trade-modal__option"
              onClick={() => { onClose(); navigate(`/sell/${friend.id}`, { state: { mode: 'sell' } }); }}
            >
              <span className="trade-modal__option-icon">💰</span>
              <div>
                <p className="trade-modal__option-title">{t('trade_modal.sell')}</p>
                <p className="trade-modal__option-desc">{t('trade_modal.sell_desc')}</p>
              </div>
            </button>

            <button
              className="trade-modal__option"
              onClick={() => { onClose(); navigate(`/sell/${friend.id}`, { state: { mode: 'buy' } }); }}
            >
              <span className="trade-modal__option-icon">🛒</span>
              <div>
                <p className="trade-modal__option-title">{t('trade_modal.buy')}</p>
                <p className="trade-modal__option-desc">{t('trade_modal.buy_desc')}</p>
              </div>
            </button>
          </div>
        ) : (
          <div className="trade-modal__result">
            {result.maxTrades === 0 ? (
              <div className="trade-modal__no-trades">
                <p>{t('trade_modal.no_trades')}</p>
                <p className="trade-modal__sub">
                  {result.myOfferings.length === 0
                    ? t('trade_modal.no_my_offer')
                    : t('trade_modal.no_friend_offer')}
                </p>
              </div>
            ) : (
              <div className="trade-modal__trades-found">
                <p className="trade-modal__count">
                  <strong>{t('trade_modal.count', { count: result.maxTrades })}</strong>
                </p>
                <p className="trade-modal__sub">
                  {t('trade_modal.offer_sub', { count: result.myOfferings.length, receive: result.friendOfferings.length })}
                </p>
                <button className="trade-modal__btn-primary" onClick={handleGoToTrade}>
                  {t('trade_modal.view_propose')}
                </button>
              </div>
            )}
            <button className="trade-modal__btn-back" onClick={() => setResult(null)}>{t('trade_modal.back')}</button>
          </div>
        )}

        {error && <p className="trade-modal__error">{error}</p>}
        {loading && <p className="trade-modal__loading">{t('trade_modal.calculating')}</p>}

        <button className="trade-modal__close" onClick={onClose}>{t('trade_modal.close')}</button>
      </div>
    </div>
  );
}
