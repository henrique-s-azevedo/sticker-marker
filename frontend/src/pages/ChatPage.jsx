/**
 * Chat page — conversation view with trade/sell message integration.
 */
import { useState, useEffect, useRef, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { getConversation, sendMessage, markRead } from '../services/messageService';
import { getFriends } from '../services/friendshipService';
import { confirmTrade, respondTrade, completeTrade } from '../services/tradeService';
import { completeSell, cancelSell } from '../services/sellService';
import { getAccessToken } from '../context/AuthContext';
import './ChatPage.css';

export default function ChatPage() {
  const { friendId } = useParams();
  const navigate = useNavigate();
  const { t, i18n } = useTranslation();
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
    const locale = i18n.language === 'pt' ? 'pt-PT' : 'en-GB';
    const isToday = d.toDateString() === today.toDateString();
    if (isToday) {
      return d.toLocaleTimeString(locale, { hour: '2-digit', minute: '2-digit' });
    }
    return d.toLocaleDateString(locale, { day: '2-digit', month: '2-digit' }) +
      ' ' + d.toLocaleTimeString(locale, { hour: '2-digit', minute: '2-digit' });
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
          <p className="chat-page__friend-name">{t('chat.title')}</p>
        )}
      </header>

      <div className="chat-page__messages">
        {error && <p className="chat-page__error">{error}</p>}
        {messages.length === 0 && !error && (
          <p className="chat-page__empty">{t('chat.empty')}</p>
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
          placeholder={t('chat.placeholder')}
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
          {t('chat.send')}
        </button>
      </form>
    </div>
  );
}

function SellMessageCard({ msg, mine, myId, friend, formatTime, onRefresh, navigate }) {
  const { t } = useTranslation();
  const [acting, setActing] = useState(false);
  const [err, setErr] = useState('');
  const [dismissed, setDismissed] = useState(false);

  const sellId = msg.sellProposalId;
  const sellStatus = msg.sellProposalStatus;
  const isSellProposal = msg.messageType === 'SELL_PROPOSAL';
  const iAmSeller = myId === msg.sellProposalSellerId;
  const friendName = friend?.displayName ?? 'the other user';

  const shouldSeeButtons = sellStatus === 'PENDING' && (
    (isSellProposal && !iAmSeller) ||
    (!isSellProposal && iAmSeller)
  );
  const acceptLabel = isSellProposal ? t('chat.accept_purchase') : t('chat.accept_sale');
  const rejectLabel = isSellProposal ? t('chat.reject_purchase') : t('chat.reject_sale');

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
        ? t('chat.sell_mine_sell', { name: friendName })
        : t('chat.sell_mine_buy', { name: friendName });
    } else {
      message = isSellProposal
        ? t('chat.sell_their_sell')
        : t('chat.sell_their_buy');
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
              {t('chat.update_collection')}
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
            {isSellProposal ? t('chat.sale_rejected') : t('chat.purchase_rejected')}
          </p>
          <div className="chat-page__trade-actions">
            <button
              className="chat-page__trade-btn"
              onClick={() => setDismissed(true)}
            >
              {t('chat.ok')}
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
              {acting ? t('chat.processing') : acceptLabel}
            </button>
            <button
              className="chat-page__trade-btn chat-page__trade-btn--danger"
              disabled={acting}
              onClick={() => act(() => cancelSell(sellId))}
            >
              {acting ? t('chat.processing') : rejectLabel}
            </button>
          </div>
        )}
        {err && <p className="chat-page__trade-err">{err}</p>}
      </div>
    </div>
  );
}

function TradeMessageCard({ msg, mine, myId, formatTime, navigate, onRefresh }) {
  const { t } = useTranslation();
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

  const canActOnProposal = isProposal  && !mine && tradeStatus === 'PENDING_COUNTERPART';
  const canActOnResponse = isResponse  && !mine && tradeStatus === 'PENDING_PROPOSER';
  const canComplete      = isConfirmed           && tradeStatus === 'CONFIRMED';

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
              {t('chat.view_accept')}
            </button>
            <button
              className="chat-page__trade-btn chat-page__trade-btn--danger"
              disabled={acting}
              onClick={() => act(() => respondTrade(tradeId, false, []))}
            >
              {t('chat.reject')}
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
              {t('chat.confirm_trade')}
            </button>
            <button
              className="chat-page__trade-btn chat-page__trade-btn--danger"
              disabled={acting}
              onClick={() => act(() => confirmTrade(tradeId, false))}
            >
              {t('chat.cancel')}
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
              {acting ? t('chat.applying') : t('chat.mark_completed')}
            </button>
          </div>
        )}

        {err && <p className="chat-page__trade-err">{err}</p>}
      </div>
    </div>
  );
}
