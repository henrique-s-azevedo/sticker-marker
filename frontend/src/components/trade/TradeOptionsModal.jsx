/**
 * Entry-point modal for trade/sell interactions with a specific friend.
 * Presents three options: sticker-for-sticker trade, sell, or buy.
 *
 * "Troca por troca" calculates the mutual trade opportunities inline and shows
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
        <h2 className="trade-modal__title">Trocas com {friend.displayName}</h2>

        {!result ? (
          <div className="trade-modal__options">
            <button
              className="trade-modal__option"
              onClick={handleOption1}
              disabled={loading}
            >
              <span className="trade-modal__option-icon">🔄</span>
              <div>
                <p className="trade-modal__option-title">Troca por troca</p>
                <p className="trade-modal__option-desc">Ver os cromos que podes trocar mutuamente</p>
              </div>
            </button>

            <button
              className="trade-modal__option"
              onClick={() => { onClose(); navigate(`/sell/${friend.id}`, { state: { mode: 'sell' } }); }}
            >
              <span className="trade-modal__option-icon">💰</span>
              <div>
                <p className="trade-modal__option-title">Vender</p>
                <p className="trade-modal__option-desc">Vende os teus repetidos a este amigo</p>
              </div>
            </button>

            <button
              className="trade-modal__option"
              onClick={() => { onClose(); navigate(`/sell/${friend.id}`, { state: { mode: 'buy' } }); }}
            >
              <span className="trade-modal__option-icon">🛒</span>
              <div>
                <p className="trade-modal__option-title">Comprar</p>
                <p className="trade-modal__option-desc">Compra repetidos a este amigo</p>
              </div>
            </button>
          </div>
        ) : (
          <div className="trade-modal__result">
            {result.maxTrades === 0 ? (
              <div className="trade-modal__no-trades">
                <p>Sem trocas possíveis neste momento.</p>
                <p className="trade-modal__sub">
                  {result.myOfferings.length === 0
                    ? 'Não tens repetidos que o teu amigo precise.'
                    : 'O teu amigo não tem repetidos que tu precises.'}
                </p>
              </div>
            ) : (
              <div className="trade-modal__trades-found">
                <p className="trade-modal__count">
                  <strong>{result.maxTrades}</strong> troca{result.maxTrades !== 1 ? 's' : ''} por troca{result.maxTrades !== 1 ? 's' : ''} possíve{result.maxTrades !== 1 ? 'is' : 'l'}
                </p>
                <p className="trade-modal__sub">
                  Tens {result.myOfferings.length} cromo{result.myOfferings.length !== 1 ? 's' : ''} para oferecer
                  e podes receber {result.friendOfferings.length}.
                </p>
                <button className="trade-modal__btn-primary" onClick={handleGoToTrade}>
                  Ver e propor troca
                </button>
              </div>
            )}
            <button className="trade-modal__btn-back" onClick={() => setResult(null)}>← Voltar</button>
          </div>
        )}

        {error && <p className="trade-modal__error">{error}</p>}
        {loading && <p className="trade-modal__loading">A calcular...</p>}

        <button className="trade-modal__close" onClick={onClose}>Fechar</button>
      </div>
    </div>
  );
}
