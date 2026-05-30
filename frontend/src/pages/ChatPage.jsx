import { useState, useEffect, useRef, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { getConversation, sendMessage, markRead } from '../services/messageService';
import { getFriends } from '../services/friendshipService';
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
