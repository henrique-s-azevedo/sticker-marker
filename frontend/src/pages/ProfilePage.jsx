import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  getProfile, updateVisibility, changePassword,
} from '../services/profileService';
import {
  getFriends, removeFriend,
  getFriendRequests, getSentRequests,
  acceptRequest, rejectRequest,
  searchUsers, addFriendByTag,
} from '../services/friendshipService';
import { getConversations } from '../services/messageService';
import AddFriendModal from '../components/profile/AddFriendModal';
import './ProfilePage.css';

const VISIBILITY_LABELS = {
  PUBLIC:       'Pública',
  FRIENDS_ONLY: 'Só amigos',
  PRIVATE:      'Privada',
};

const COLLECTION_ID = 1;

export default function ProfilePage() {
  const navigate  = useNavigate();
  const { logout } = useAuth();
  const [tab, setTab]                 = useState('profile');
  const [profile, setProfile]         = useState(null);
  const [friends, setFriends]         = useState([]);
  const [requests, setRequests]       = useState([]);
  const [sentReqs, setSentReqs]       = useState([]);
  const [conversations, setConversations] = useState([]);
  const [searchQ, setSearchQ]         = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [showAddModal, setShowAddModal] = useState(false);
  const [pwForm, setPwForm]           = useState({ current: '', next: '', confirm: '' });
  const [pwError, setPwError]         = useState('');
  const [pwSuccess, setPwSuccess]     = useState('');
  const [saving, setSaving]           = useState(false);
  const [error, setError]             = useState('');

  const loadProfile       = useCallback(() => getProfile().then(setProfile).catch(e => setError(e.message)), []);
  const loadFriends       = useCallback(() => getFriends().then(setFriends).catch(() => {}), []);
  const loadRequests      = useCallback(() => {
    getFriendRequests().then(setRequests).catch(() => {});
    getSentRequests().then(setSentReqs).catch(() => {});
  }, []);
  const loadConversations = useCallback(() => getConversations().then(setConversations).catch(() => {}), []);

  useEffect(() => { loadProfile(); }, [loadProfile]);
  useEffect(() => {
    if (tab === 'friends')       { loadFriends(); setSearchResults([]); setSearchQ(''); }
    if (tab === 'requests')      { loadRequests(); }
    if (tab === 'messages')      { loadConversations(); }
  }, [tab, loadFriends, loadRequests, loadConversations]);

  async function handleVisibility(vis) {
    setSaving(true);
    try {
      await updateVisibility(vis);
      setProfile(p => ({ ...p, collectionVisibility: vis }));
    } catch (e) { setError(e.message); }
    finally { setSaving(false); }
  }

  async function handleChangePassword(e) {
    e.preventDefault();
    setPwError(''); setPwSuccess('');
    if (pwForm.next !== pwForm.confirm) { setPwError('Passwords não coincidem'); return; }
    setSaving(true);
    try {
      await changePassword(pwForm.current, pwForm.next);
      setPwSuccess('Password alterada com sucesso!');
      setPwForm({ current: '', next: '', confirm: '' });
    } catch (err) {
      setPwError(err.message);
    } finally { setSaving(false); }
  }

  async function handleSearch(e) {
    const q = e.target.value;
    setSearchQ(q);
    if (q.trim().length < 2) { setSearchResults([]); return; }
    const q2 = q.startsWith('@') ? q.slice(1) : q;
    searchUsers(q2).then(setSearchResults).catch(() => {});
  }

  async function handleRemoveFriend(friendId) {
    await removeFriend(friendId);
    setFriends(prev => prev.filter(f => f.id !== friendId));
  }

  async function handleAccept(requestId) {
    await acceptRequest(requestId);
    setRequests(prev => prev.filter(r => r.id !== requestId));
    loadFriends();
    loadProfile();
  }

  async function handleReject(requestId) {
    await rejectRequest(requestId);
    setRequests(prev => prev.filter(r => r.id !== requestId));
    loadProfile();
  }

  return (
    <div className="profile-page">
      <header className="profile-page__header">
        <button className="profile-page__back" onClick={() => navigate('/collection')}>
          ← Voltar
        </button>
        <h1 className="profile-page__title">Perfil</h1>
        <button className="profile-page__logout" onClick={() => { logout(); navigate('/login'); }}>
          Sair
        </button>
      </header>

      {profile && (
        <div className="profile-page__hero">
          <div className="profile-page__avatar">{profile.displayName?.[0]?.toUpperCase() ?? '?'}</div>
          <div>
            <p className="profile-page__name">{profile.displayName}</p>
            <p className="profile-page__tag">@{profile.userTag}</p>
          </div>
        </div>
      )}

      <nav className="profile-page__tabs">
        {[
          { id: 'profile',  label: 'Perfil' },
          { id: 'friends',  label: `Amigos${friends.length ? ` (${friends.length})` : ''}` },
          { id: 'requests', label: `Pedidos${profile?.pendingRequestsCount > 0 ? ` · ${profile.pendingRequestsCount}` : ''}` },
          { id: 'messages', label: `Mensagens${conversations.some(c => c.unreadCount > 0) ? ` · ${conversations.reduce((s, c) => s + c.unreadCount, 0)}` : ''}` },
        ].map(t => (
          <button
            key={t.id}
            className={`profile-page__tab${tab === t.id ? ' profile-page__tab--active' : ''}`}
            onClick={() => setTab(t.id)}
          >
            {t.label}
          </button>
        ))}
      </nav>

      <main className="profile-page__main">
        {error && <p className="profile-page__error">{error}</p>}

        {tab === 'profile' && profile && (
          <div className="profile-page__section">
            <div className="profile-page__card">
              <h2 className="profile-page__card-title">Informação</h2>
              <div className="profile-page__info-row">
                <span className="profile-page__info-label">Nome</span>
                <span className="profile-page__info-value">{profile.displayName}</span>
              </div>
              <div className="profile-page__info-row">
                <span className="profile-page__info-label">Email</span>
                <span className="profile-page__info-value">{profile.email}</span>
              </div>
              <div className="profile-page__info-row">
                <span className="profile-page__info-label">Tag</span>
                <span className="profile-page__info-value profile-page__tag-badge">@{profile.userTag}</span>
              </div>
            </div>

            <div className="profile-page__card">
              <h2 className="profile-page__card-title">Visibilidade da Coleção</h2>
              <p className="profile-page__card-desc">Quem pode ver a tua coleção</p>
              <div className="profile-page__vis-options">
                {Object.entries(VISIBILITY_LABELS).map(([key, label]) => (
                  <button
                    key={key}
                    disabled={saving}
                    className={`profile-page__vis-btn${profile.collectionVisibility === key ? ' profile-page__vis-btn--active' : ''}`}
                    onClick={() => handleVisibility(key)}
                  >
                    {label}
                  </button>
                ))}
              </div>
            </div>

            <div className="profile-page__card">
              <h2 className="profile-page__card-title">Alterar Password</h2>
              <form className="profile-page__pw-form" onSubmit={handleChangePassword}>
                <input
                  className="profile-page__input"
                  type="password"
                  placeholder="Password atual"
                  value={pwForm.current}
                  onChange={e => setPwForm(p => ({ ...p, current: e.target.value }))}
                  required
                />
                <input
                  className="profile-page__input"
                  type="password"
                  placeholder="Nova password (mín. 8 carateres)"
                  value={pwForm.next}
                  onChange={e => setPwForm(p => ({ ...p, next: e.target.value }))}
                  required
                  minLength={8}
                />
                <input
                  className="profile-page__input"
                  type="password"
                  placeholder="Confirmar nova password"
                  value={pwForm.confirm}
                  onChange={e => setPwForm(p => ({ ...p, confirm: e.target.value }))}
                  required
                />
                {pwError   && <p className="profile-page__error">{pwError}</p>}
                {pwSuccess && <p className="profile-page__success">{pwSuccess}</p>}
                <button className="profile-page__btn-primary" type="submit" disabled={saving}>
                  {saving ? 'A guardar...' : 'Alterar password'}
                </button>
              </form>
            </div>
          </div>
        )}

        {tab === 'friends' && (
          <div className="profile-page__section">
            <div className="profile-page__friends-header">
              <input
                className="profile-page__search"
                type="text"
                placeholder="Pesquisar utilizadores (@tag ou nome)..."
                value={searchQ}
                onChange={handleSearch}
              />
              <button className="profile-page__btn-primary" onClick={() => setShowAddModal(true)}>
                + Adicionar Amigo
              </button>
            </div>

            {searchQ.trim().length >= 2 && (
              <div className="profile-page__card">
                <h3 className="profile-page__card-title">Resultados</h3>
                {searchResults.length === 0
                  ? <p className="profile-page__empty">Nenhum utilizador encontrado.</p>
                  : searchResults.map(u => (
                    <UserSearchRow key={u.id} user={u} onRequestSent={loadRequests} />
                  ))
                }
              </div>
            )}

            <div className="profile-page__card">
              <h3 className="profile-page__card-title">Os teus amigos ({friends.length})</h3>
              {friends.length === 0
                ? <p className="profile-page__empty">Ainda não tens amigos adicionados.</p>
                : friends.map(f => (
                  <div key={f.id} className="profile-page__friend-row">
                    <div className="profile-page__friend-avatar">{f.displayName?.[0]?.toUpperCase()}</div>
                    <div className="profile-page__friend-info">
                      <span className="profile-page__friend-name">{f.displayName}</span>
                      <span className="profile-page__friend-tag">@{f.userTag}</span>
                    </div>
                    <div className="profile-page__friend-actions">
                      <button
                        className="profile-page__btn-secondary"
                        onClick={() => navigate(`/chat/${f.id}`)}
                      >
                        Mensagem
                      </button>
                      {f.collectionVisibility !== 'PRIVATE' && (
                        <button
                          className="profile-page__btn-secondary"
                          onClick={() => navigate(`/collection/${f.userTag}`)}
                        >
                          Ver coleção
                        </button>
                      )}
                      <button
                        className="profile-page__btn-danger"
                        onClick={() => handleRemoveFriend(f.id)}
                      >
                        Remover
                      </button>
                    </div>
                  </div>
                ))
              }
            </div>
          </div>
        )}

        {tab === 'requests' && (
          <div className="profile-page__section">
            <div className="profile-page__card">
              <h3 className="profile-page__card-title">Pedidos recebidos ({requests.length})</h3>
              {requests.length === 0
                ? <p className="profile-page__empty">Nenhum pedido pendente.</p>
                : requests.map(r => (
                  <div key={r.id} className="profile-page__request-row">
                    <div className="profile-page__friend-avatar">{r.requesterDisplayName?.[0]?.toUpperCase()}</div>
                    <div className="profile-page__friend-info">
                      <span className="profile-page__friend-name">{r.requesterDisplayName}</span>
                      <span className="profile-page__friend-tag">@{r.requesterUserTag}</span>
                    </div>
                    <div className="profile-page__friend-actions">
                      <button className="profile-page__btn-primary" onClick={() => handleAccept(r.id)}>Aceitar</button>
                      <button className="profile-page__btn-danger"  onClick={() => handleReject(r.id)}>Rejeitar</button>
                    </div>
                  </div>
                ))
              }
            </div>

            <div className="profile-page__card">
              <h3 className="profile-page__card-title">Pedidos enviados ({sentReqs.length})</h3>
              {sentReqs.length === 0
                ? <p className="profile-page__empty">Nenhum pedido enviado.</p>
                : sentReqs.map(r => (
                  <div key={r.id} className="profile-page__request-row">
                    <div className="profile-page__friend-avatar">{r.addresseeDisplayName?.[0]?.toUpperCase()}</div>
                    <div className="profile-page__friend-info">
                      <span className="profile-page__friend-name">{r.addresseeDisplayName}</span>
                      <span className="profile-page__friend-tag">@{r.addresseeUserTag}</span>
                    </div>
                    <span className="profile-page__pending-badge">Pendente</span>
                  </div>
                ))
              }
            </div>
          </div>
        )}

        {tab === 'messages' && (
          <div className="profile-page__section">
            <div className="profile-page__card">
              <h3 className="profile-page__card-title">Conversas</h3>
              {conversations.length === 0
                ? <p className="profile-page__empty">Ainda não há mensagens. Abre o chat de um amigo.</p>
                : conversations.map(c => (
                  <div
                    key={c.friendId}
                    className="profile-page__friend-row profile-page__conversation-row"
                    onClick={() => navigate(`/chat/${c.friendId}`)}
                  >
                    <div className="profile-page__friend-avatar">{c.friendDisplayName?.[0]?.toUpperCase()}</div>
                    <div className="profile-page__friend-info">
                      <span className="profile-page__friend-name">{c.friendDisplayName}</span>
                      <span className="profile-page__friend-tag profile-page__last-msg">
                        {c.lastMessage?.content?.slice(0, 40)}{c.lastMessage?.content?.length > 40 ? '…' : ''}
                      </span>
                    </div>
                    {c.unreadCount > 0 && (
                      <span className="profile-page__unread-badge">{c.unreadCount}</span>
                    )}
                  </div>
                ))
              }
            </div>
          </div>
        )}
      </main>

      {showAddModal && (
        <AddFriendModal
          onClose={() => setShowAddModal(false)}
          onSuccess={() => { loadFriends(); loadRequests(); }}
        />
      )}
    </div>
  );
}

function UserSearchRow({ user, onRequestSent }) {
  const [status, setStatus] = useState(user.friendshipStatus ?? null);
  const [loading, setLoading] = useState(false);

  async function send() {
    setLoading(true);
    try {
      await addFriendByTag(user.userTag);
      setStatus('PENDING');
      onRequestSent?.();
    } catch { /* ignore */ }
    finally { setLoading(false); }
  }

  return (
    <div className="profile-page__friend-row">
      <div className="profile-page__friend-avatar">{user.displayName?.[0]?.toUpperCase()}</div>
      <div className="profile-page__friend-info">
        <span className="profile-page__friend-name">{user.displayName}</span>
        <span className="profile-page__friend-tag">@{user.userTag}</span>
      </div>
      {status === 'ACCEPTED'
        ? <span className="profile-page__already-friends">Amigos</span>
        : status === 'PENDING'
          ? <span className="profile-page__pending-badge">Pedido enviado</span>
          : (
            <button className="profile-page__btn-secondary" onClick={send} disabled={loading}>
              {loading ? '...' : '+ Adicionar'}
            </button>
          )
      }
    </div>
  );
}
