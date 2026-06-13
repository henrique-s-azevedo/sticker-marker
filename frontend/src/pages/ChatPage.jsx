/**
 * Chat page (/chat/:friendId) — real-time conversation view with trade/sell integration.
 *
 * Message polling: fetches new messages every 5 seconds via setInterval.
 * After each load, markRead() is called to clear the unread badge for this conversation.
 * The bottom of the message list is scrolled into view when messages change.
 *
 * Message rendering:
 *   - Plain text messages: standard chat bubble.
 *   - TRADE_PROPOSAL / TRADE_RESPONSE / TRADE_CONFIRMED / TRADE_REJECTED: TradeMessageCard.
 *   - SELL_PROPOSAL / BUY_PROPOSAL: SellMessageCard.
 *
 * The authenticated user's ID is decoded from the JWT access token to determine
 * message direction (mine vs. theirs) and button visibility.
 *
 * TradeMessageCard — shows action buttons based on tradeStatus and who sent the message:
 *   PENDING_COUNTERPART + not mine → "Ver e aceitar" + "Rejeitar"
 *   PENDING_PROPOSER   + not mine → "Confirmar troca" + "Cancelar"
 *   CONFIRMED                     → "Marcar como concluída" (transfers stickers)
 *
 * SellMessageCard — shows accept/reject buttons to the recipient of the proposal:
 *   SELL_PROPOSAL: recipient is the buyer (not the seller).
 *   BUY_PROPOSAL:  recipient is the seller (iAmSeller check).
 *   COMPLETED: shows an "Atualizar" button that navigates to /collection.
 */
import { useState, useEffect, useRef, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { getConversation, sendMessage, markRead } from '../services/messageService';
import { getFriends } from '../services/friendshipService';
import { confirmTrade, respondTrade, completeTrade } from '../services/tradeService';
import { completeSell, cancelSell } from '../services/sellService';
import { getAccessToken } from '../context/AuthContext';
import './ChatPage.css';

export default function ChatPage() {
  const { friendId } = useParams();
  const navigate = useNavigate();
  const [messages, setMessages] = useState([]);
  const [friend, setFriend] = useState(null);
  const [myId, setMyId] = useState(null);
  const [text, setText] = useState('');
  const [sending, setSending] = useState(false);
  const [error, setError] = useState('');
  const bottomRef = useRef(null);
  const pollRef = useRef(null);

  const load = useCallback(async () => {
    try {
      const msgs = await getConversation(Number(friendId));
      setMessages(msgs);
      await markRead(Number(friendId));
    } catch (e) {
      setError(e.message);
    }
  }, [friendId]);

  useEffect(() => {
    getFriends().then(friends => {
      const f = friends.find(f => f.id === Number(friendId));
      setFriend(f ?? null);
    }).catch(() => {});

    const token = getAccessToken();
    if (token) {
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        setMyId(payload.userId);
      } catch { /* ignore */ }
    }
  }, [friendId]);

  useEffect(() => {
    load();
    pollRef.current = setInterval(load, 5000);
    return () => clearInterval(pollRef.current);
  }, [load]);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  async function handleSend(e) {
    e.preventDefault();
    const content = text.trim();
    if (!content || sending) return;
    setSending(true);
    try {
      const msg = await sendMessage(Number(friendId), content);
      setMessages(prev => [...prev, msg]);
      setText('');
    } catch (err) {
      setError(err.message);
    } finally {
      setSending(false);
    }
  }

  function formatTime(isoString) {
    if (!isoString) return '';
    const d = new Date(isoString);
    const today = new Date();
    const isToday = d.toDateString() === today.toDateString();
    if (isToday) {
      return d.toLocaleTimeString('pt-PT', { hour: '2-digit', minute: '2-digit' });
    }
    return d.toLocaleDateString('pt-PT', { day: '2-digit', month: '2-digit' }) +
      ' ' + d.toLocaleTimeString('pt-PT', { hour: '2-digit', minute: '2-digit' });
  }

  return (
    <div className="chat-page">
      <header className="chat-page__header">
        <button className="chat-page__back" onClick={() => navigate('/profile')}>←</button>
        {friend ? (
          <div className="chat-page__friend-info">
            <div className="chat-page__friend-avatar">
              {friend.displayName?.[0]?.toUpperCase()}
            </div>
            <div>
              <p className="chat-page__friend-name">{friend.displayName}</p>
              <p className="chat-page__friend-tag">@{friend.userTag}</p>
            </div>
          </div>
        ) : (
          <p className="chat-page__friend-name">Chat</p>
        )}
      </header>

      <div className="chat-page__messages">
        {error && <p className="chat-page__error">{error}</p>}
        {messages.length === 0 && !error && (
          <p className="chat-page__empty">Ainda não há mensagens. Di olá!</p>
        )}
        {messages.map(msg => {
          const mine = msg.senderId === myId;
          const TRADE_TYPES = ['TRADE_PROPOSAL', 'TRADE_RESPONSE', 'TRADE_CONFIRMED', 'TRADE_REJECTED'];
          const SELL_TYPES  = ['SELL_PROPOSAL', 'BUY_PROPOSAL'];

          if (TRADE_TYPES.includes(msg.messageType)) {
            return (
              <TradeMessageCard
                key={msg.id}
                msg={msg}
                mine={mine}
                myId={myId}
                formatTime={formatTime}
                navigate={navigate}
                onRefresh={load}
              />
            );
          }

          if (SELL_TYPES.includes(msg.messageType)) {
            return (
              <SellMessageCard
                key={msg.id}
                msg={msg}
                mine={mine}
                myId={myId}
                friend={friend}
                formatTime={formatTime}
                onRefresh={load}
                navigate={navigate}
              />
            );
          }

          return (
            <div key={msg.id} className={`chat-page__bubble-wrap${mine ? ' chat-page__bubble-wrap--mine' : ''}`}>
              <div className={`chat-page__bubble${mine ? ' chat-page__bubble--mine' : ''}`}>
                <p className="chat-page__bubble-text">{msg.content}</p>
                <span className="chat-page__bubble-time">{formatTime(msg.sentAt)}</span>
              </div>
            </div>
          );
        })}
        <div ref={bottomRef} />
      </div>

      <form className="chat-page__input-area" onSubmit={handleSend}>
        <input
          className="chat-page__input"
          type="text"
          placeholder="Escreve uma mensagem..."
          value={text}
          onChange={e => setText(e.target.value)}
          maxLength={1000}
          autoFocus
        />
        <button
          className="chat-page__send-btn"
          type="submit"
          disabled={!text.trim() || sending}
        >
          Enviar
        </button>
      </form>
    </div>
  );
}

function SellMessageCard({ msg, mine, myId, friend, formatTime, onRefresh, navigate }) {
  const [acting, setActing] = useState(false);
  const [err, setErr] = useState('');
  const [dismissed, setDismissed] = useState(false);

  const sellId = msg.sellProposalId;
  const sellStatus = msg.sellProposalStatus;
  const isSellProposal = msg.messageType === 'SELL_PROPOSAL';
  const iAmSeller = myId === msg.sellProposalSellerId;
  const friendName = friend?.displayName ?? 'o outro utilizador';

  // While pending: only the RECIPIENT of the proposal sees the action buttons
  // SELL_PROPOSAL → receiver is the buyer (not the seller)
  // BUY_PROPOSAL  → receiver is the seller (iAmSeller)
  const shouldSeeButtons = sellStatus === 'PENDING' && (
    (isSellProposal && !iAmSeller) ||
    (!isSellProposal && iAmSeller)
  );
  const acceptLabel = isSellProposal ? 'Aceitar a compra' : 'Aceitar a venda';
  const rejectLabel = isSellProposal ? 'Rejeitar a compra' : 'Rejeitar a venda';

  async function act(fn) {
    setActing(true);
    setErr('');
    try {
      await fn();
      onRefresh();
    } catch (e) {
      setErr(e.message);
    } finally {
      setActing(false);
    }
  }

  if (dismissed) return null;

  const wrapClass = `chat-page__trade-wrap${mine ? ' chat-page__trade-wrap--mine' : ''}`;

  if (sellStatus === 'COMPLETED') {
    let message;
    if (mine) {
      message = isSellProposal
        ? `O "${friendName}" aceitou a tua venda.\nQuando completares a venda em mão e tiveres os cromos clica no botão para atualizar automaticamente a tua coleção:`
        : `O "${friendName}" aceitou a tua compra.\nQuando completares a compra em mão e tiveres os cromos clica no botão para atualizar automaticamente a tua coleção:`;
    } else {
      message = isSellProposal
        ? `Aceitaste a proposta.\nQuando completares a compra em mão e tiveres os cromos clica no botão para atualizar automaticamente a tua coleção:`
        : `Aceitaste a proposta.\nQuando completares a venda em mão e tiveres os cromos clica no botão para atualizar automaticamente a tua coleção:`;
    }
    return (
      <div className={wrapClass}>
        <div className="chat-page__trade-card chat-page__trade-card--confirmed">
          <p className="chat-page__trade-content">{message}</p>
          <div className="chat-page__trade-actions">
            <button
              className="chat-page__trade-btn chat-page__trade-btn--complete"
              onClick={() => { setDismissed(true); navigate('/collection'); }}
            >
              Atualizar
            </button>
          </div>
        </div>
      </div>
    );
  }

  if (sellStatus === 'CANCELLED') {
    return (
      <div className={wrapClass}>
        <div className="chat-page__trade-card chat-page__trade-card--rejected">
          <p className="chat-page__trade-content">
            {isSellProposal ? 'Venda rejeitada' : 'Compra rejeitada'}
          </p>
          <div className="chat-page__trade-actions">
            <button
              className="chat-page__trade-btn"
              onClick={() => setDismissed(true)}
            >
              OK
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className={wrapClass}>
      <div className="chat-page__trade-card">
        <p className="chat-page__trade-content">{msg.content}</p>
        <span className="chat-page__trade-time">{formatTime(msg.sentAt)}</span>
        {shouldSeeButtons && (
          <div className="chat-page__trade-actions">
            <button
              className="chat-page__trade-btn chat-page__trade-btn--complete"
              disabled={acting}
              onClick={() => act(() => completeSell(sellId))}
            >
              {acting ? 'A processar...' : acceptLabel}
            </button>
            <button
              className="chat-page__trade-btn chat-page__trade-btn--danger"
              disabled={acting}
              onClick={() => act(() => cancelSell(sellId))}
            >
              {acting ? 'A processar...' : rejectLabel}
            </button>
          </div>
        )}
        {err && <p className="chat-page__trade-err">{err}</p>}
      </div>
    </div>
  );
}

function TradeMessageCard({ msg, mine, myId, formatTime, navigate, onRefresh }) {
  const [acting, setActing] = useState(false);
  const [err, setErr] = useState('');
  const type = msg.messageType;
  const tradeId = msg.tradeProposalId;

  async function act(fn) {
    setActing(true);
    setErr('');
    try {
      await fn();
      onRefresh();
    } catch (e) {
      setErr(e.message);
    } finally {
      setActing(false);
    }
  }

  const isProposal  = type === 'TRADE_PROPOSAL';
  const isResponse  = type === 'TRADE_RESPONSE';
  const isConfirmed = type === 'TRADE_CONFIRMED';
  const isRejected  = type === 'TRADE_REJECTED';

  const tradeStatus = msg.tradeStatus;

  // Buttons only show when the trade is still in the matching actionable state
  const canActOnProposal  = isProposal  && !mine && tradeStatus === 'PENDING_COUNTERPART';
  const canActOnResponse  = isResponse  && !mine && tradeStatus === 'PENDING_PROPOSER';
  const canComplete       = isConfirmed           && tradeStatus === 'CONFIRMED';

  return (
    <div className={`chat-page__trade-wrap${mine ? ' chat-page__trade-wrap--mine' : ''}`}>
      <div className={`chat-page__trade-card${isRejected ? ' chat-page__trade-card--rejected' : ''}${isConfirmed ? ' chat-page__trade-card--confirmed' : ''}`}>
        <p className="chat-page__trade-content">{msg.content}</p>
        <span className="chat-page__trade-time">{formatTime(msg.sentAt)}</span>

        {canActOnProposal && (
          <div className="chat-page__trade-actions">
            <button
              className="chat-page__trade-btn chat-page__trade-btn--primary"
              disabled={acting}
              onClick={() => navigate(`/trade-respond/${tradeId}`)}
            >
              Ver e aceitar
            </button>
            <button
              className="chat-page__trade-btn chat-page__trade-btn--danger"
              disabled={acting}
              onClick={() => act(() => respondTrade(tradeId, false, []))}
            >
              Rejeitar
            </button>
          </div>
        )}

        {canActOnResponse && (
          <div className="chat-page__trade-actions">
            <button
              className="chat-page__trade-btn chat-page__trade-btn--primary"
              disabled={acting}
              onClick={() => act(() => confirmTrade(tradeId, true))}
            >
              Confirmar troca
            </button>
            <button
              className="chat-page__trade-btn chat-page__trade-btn--danger"
              disabled={acting}
              onClick={() => act(() => confirmTrade(tradeId, false))}
            >
              Cancelar
            </button>
          </div>
        )}

        {canComplete && (
          <div className="chat-page__trade-actions">
            <button
              className="chat-page__trade-btn chat-page__trade-btn--complete"
              disabled={acting}
              onClick={() => act(() => completeTrade(tradeId))}
            >
              {acting ? 'A aplicar...' : 'Marcar como concluída'}
            </button>
          </div>
        )}

        {err && <p className="chat-page__trade-err">{err}</p>}
      </div>
    </div>
  );
}
