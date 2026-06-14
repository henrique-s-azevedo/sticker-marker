/**
 * Entry-point modal for trade/sell interactions with a specific friend.
 * Presents three options: sticker-for-sticker trade, sell, or buy.
 *
 * "Sticker for sticker" calculates the mutual trade opportunities inline and shows
 * the result before navigating to /trade/:friendId. The other options navigate
 * directly to /sell/:friendId with a mode flag in location state.
 *
 * @param {{ id: number, displayName: string }} friend
 * @param {Function} onClose
 */
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { calculateTrade } from '../../services/tradeService';
import './TradeOptionsModal.css';

export default function TradeOptionsModal({ friend, onClose }) {
  const navigate = useNavigate();
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
        <h2 className="trade-modal__title">Trades with {friend.displayName}</h2>

        {!result ? (
          <div className="trade-modal__options">
            <button
              className="trade-modal__option"
              onClick={handleOption1}
              disabled={loading}
            >
              <span className="trade-modal__option-icon">🔄</span>
              <div>
                <p className="trade-modal__option-title">Sticker for sticker</p>
                <p className="trade-modal__option-desc">See the stickers you can swap with each other</p>
              </div>
            </button>

            <button
              className="trade-modal__option"
              onClick={() => { onClose(); navigate(`/sell/${friend.id}`, { state: { mode: 'sell' } }); }}
            >
              <span className="trade-modal__option-icon">💰</span>
              <div>
                <p className="trade-modal__option-title">Sell</p>
                <p className="trade-modal__option-desc">Sell your duplicates to this friend</p>
              </div>
            </button>

            <button
              className="trade-modal__option"
              onClick={() => { onClose(); navigate(`/sell/${friend.id}`, { state: { mode: 'buy' } }); }}
            >
              <span className="trade-modal__option-icon">🛒</span>
              <div>
                <p className="trade-modal__option-title">Buy</p>
                <p className="trade-modal__option-desc">Buy duplicates from this friend</p>
              </div>
            </button>
          </div>
        ) : (
          <div className="trade-modal__result">
            {result.maxTrades === 0 ? (
              <div className="trade-modal__no-trades">
                <p>No trades possible at the moment.</p>
                <p className="trade-modal__sub">
                  {result.myOfferings.length === 0
                    ? 'You have no duplicates your friend needs.'
                    : 'Your friend has no duplicates you need.'}
                </p>
              </div>
            ) : (
              <div className="trade-modal__trades-found">
                <p className="trade-modal__count">
                  <strong>{result.maxTrades}</strong> possible sticker-for-sticker trade{result.maxTrades !== 1 ? 's' : ''}
                </p>
                <p className="trade-modal__sub">
                  You have {result.myOfferings.length} sticker{result.myOfferings.length !== 1 ? 's' : ''} to offer
                  and can receive {result.friendOfferings.length}.
                </p>
                <button className="trade-modal__btn-primary" onClick={handleGoToTrade}>
                  View and propose trade
                </button>
              </div>
            )}
            <button className="trade-modal__btn-back" onClick={() => setResult(null)}>← Back</button>
          </div>
        )}

        {error && <p className="trade-modal__error">{error}</p>}
        {loading && <p className="trade-modal__loading">Calculating...</p>}

        <button className="trade-modal__close" onClick={onClose}>Close</button>
      </div>
    </div>
  );
}
