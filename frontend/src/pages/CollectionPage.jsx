import { useState, useEffect, useMemo, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  fetchStickers, fetchProgress,
  markOwned, removeOwned,
  addDuplicate, updateDuplicate, removeDuplicate,
} from '../services/collectionService';
import StatsBar from '../components/collection/StatsBar';
import TabBar from '../components/collection/TabBar';
import StickerSection from '../components/collection/StickerSection';
import CountrySelect from '../components/common/CountrySelect';
import SortDropdown from '../components/common/SortDropdown';
import StickerSearch from '../components/common/StickerSearch';
import ShareModal from '../components/common/ShareModal';
import { getPendingCount } from '../services/friendshipService';
import { getUnreadCount } from '../services/messageService';
import './CollectionPage.css';

const COLLECTION_ID = 1;

function groupByPrefix(stickers) {
  const groups = {};
  for (const s of stickers) {
    const key = s.teamInitial;
    if (!groups[key]) groups[key] = [];
    groups[key].push(s);
  }
  return groups;
}

export default function CollectionPage() {
  const { logout } = useAuth();
  const navigate = useNavigate();

  const [stickers, setStickers] = useState([]);
  const [progress, setProgress] = useState(null);
  const [activeTab, setActiveTab] = useState('ALL');
  const [selectedCountries, setSelectedCountries] = useState([]);
  const [sortOrder, setSortOrder] = useState('ALBUM');
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [shareMode, setShareMode] = useState(null);
  const [pendingCount, setPendingCount] = useState(0);
  const [unreadMessages, setUnreadMessages] = useState(0);

  useEffect(() => {
    getPendingCount().then(d => setPendingCount(d?.count ?? 0)).catch(() => {});
    getUnreadCount().then(d => setUnreadMessages(d?.count ?? 0)).catch(() => {});
  }, []);

  useEffect(() => {
    Promise.all([
      fetchStickers(COLLECTION_ID),
      fetchProgress(COLLECTION_ID),
    ])
      .then(([stickerData, progressData]) => {
        setStickers(stickerData);
        setProgress(progressData);
      })
      .catch(err => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  const filtered = useMemo(() => {
    if (activeTab === 'ALL') return stickers;
    if (activeTab === 'OWNED') return stickers.filter(s => s.status === 'OWNED' || s.status === 'DUPLICATE');
    return stickers.filter(s => s.status === activeTab);
  }, [stickers, activeTab]);

  const countryOptions = useMemo(() => {
    const seen = new Map();
    for (const s of stickers) {
      if (!seen.has(s.teamInitial)) seen.set(s.teamInitial, s.teamName);
    }
    return [...seen.entries()]
      .map(([value, label]) => ({ value, label }))
      .sort((a, b) => a.label.localeCompare(b.label));
  }, [stickers]);

  const sortedSections = useMemo(() => {
    const q = searchQuery.trim().toLowerCase();
    let base = q
      ? filtered.filter(s => s.code.toLowerCase().includes(q))
      : filtered;

    if (selectedCountries.length > 0) {
      base = base.filter(s => selectedCountries.includes(s.teamInitial));
    }

    const entries = Object.entries(groupByPrefix(base));

    if (sortOrder === 'AZ') {
      return entries.sort(([, a], [, b]) =>
        a[0].teamName.localeCompare(b[0].teamName)
      );
    }
    if (sortOrder === 'ZA') {
      return entries.sort(([, a], [, b]) =>
        b[0].teamName.localeCompare(a[0].teamName)
      );
    }
    if (sortOrder === 'MOST_COMPLETE' || sortOrder === 'LEAST_COMPLETE') {
      const pct = group => {
        const owned = group.filter(s => s.status === 'OWNED' || s.status === 'DUPLICATE').length;
        return group.length > 0 ? owned / group.length : 0;
      };
      return entries.sort(([, a], [, b]) =>
        sortOrder === 'MOST_COMPLETE' ? pct(b) - pct(a) : pct(a) - pct(b)
      );
    }
    return entries.sort(([, a], [, b]) => {
      const minA = Math.min(...a.map(s => s.pageNumber));
      const minB = Math.min(...b.map(s => s.pageNumber));
      return minA - minB;
    });
  }, [filtered, searchQuery, selectedCountries, sortOrder]);

  function handleCountrySelect(value) {
    setSelectedCountries(prev =>
      prev.includes(value) ? prev : [...prev, value]
    );
  }

  function handleCountryRemove(value) {
    setSelectedCountries(prev => prev.filter(v => v !== value));
  }

  const patchSticker = useCallback((code, patch) => {
    setStickers(prev => prev.map(s => s.code === code ? { ...s, ...patch } : s));
  }, []);

  const refreshProgress = useCallback(() => {
    fetchProgress(COLLECTION_ID).then(setProgress).catch(() => {});
  }, []);

  const handleSave = useCallback(async (sticker, delta) => {
    if (delta === 0) return;
    const { code, status, duplicateQuantity } = sticker;
    const currentTotal = status === 'MISSING' ? 0 : status === 'OWNED' ? 1 : 1 + (duplicateQuantity || 0);
    const newTotal = currentTotal + delta;

    try {
      if (newTotal === 0) {
        if (status === 'DUPLICATE') await removeDuplicate(code);
        if (status !== 'MISSING') await removeOwned(code);
        patchSticker(code, { status: 'MISSING', duplicateQuantity: 0 });
      } else if (newTotal === 1) {
        if (status === 'MISSING') await markOwned(code);
        if (status === 'DUPLICATE') await removeDuplicate(code);
        patchSticker(code, { status: 'OWNED', duplicateQuantity: 0 });
      } else {
        const dupQty = newTotal - 1;
        if (status === 'MISSING') {
          await markOwned(code);
          await addDuplicate(code, dupQty);
        } else if (status === 'OWNED') {
          await addDuplicate(code, dupQty);
        } else {
          await updateDuplicate(code, dupQty);
        }
        patchSticker(code, { status: 'DUPLICATE', duplicateQuantity: dupQty });
      }
      refreshProgress();
    } catch (err) {
      console.error('Erro ao guardar cromo:', err);
    }
  }, [patchSticker, refreshProgress]);

  function handleLogout() {
    logout();
    navigate('/login');
  }

  return (
    <>
    <div className="collection-page">
      <header className="collection-page__header">
        <h1 className="collection-page__title">Panini WC 2026 Album</h1>
        <div className="collection-page__header-actions">
          <button
            className="collection-page__profile-btn"
            onClick={() => navigate('/profile')}
            title="Mensagens"
            aria-label="Mensagens"
          >
            <ChatIcon />
            {unreadMessages > 0 && (
              <span className="collection-page__badge">{unreadMessages}</span>
            )}
          </button>
          <button
            className="collection-page__profile-btn"
            onClick={() => navigate('/profile')}
            title="Perfil"
            aria-label="Perfil"
          >
            <ProfileIcon />
            {pendingCount > 0 && (
              <span className="collection-page__badge">{pendingCount}</span>
            )}
          </button>
          <button className="collection-page__logout" onClick={handleLogout}>
            Sair
          </button>
        </div>
      </header>

      <main className="collection-page__main">
        {loading && <p className="collection-page__status">A carregar...</p>}
        {error && <p className="collection-page__status collection-page__status--error">{error}</p>}

        {!loading && !error && (
          <>
            <StatsBar
              total={progress?.total ?? stickers.length}
              owned={progress?.owned ?? 0}
              missing={progress?.missing ?? 0}
              duplicates={progress?.duplicates ?? 0}
            />

            <TabBar activeTab={activeTab} onTabChange={setActiveTab} />

            <div className="collection-page__filters">
              <StickerSearch value={searchQuery} onChange={setSearchQuery} />

              <div className="collection-page__filters-row">
                <SortDropdown value={sortOrder} onChange={setSortOrder} />
                <div className="collection-page__country-wrap">
                  <CountrySelect
                    options={countryOptions}
                    value={null}
                    onChange={handleCountrySelect}
                    placeholder="Filtrar por país..."
                  />
                </div>
                {selectedCountries.length > 0 && (
                  <button
                    className="collection-page__reset"
                    onClick={() => setSelectedCountries([])}
                  >
                    Limpar filtros
                  </button>
                )}
                <div className="collection-page__share-icons">
                  <button
                    className="collection-page__share-btn collection-page__share-btn--whatsapp"
                    onClick={() => setShareMode('whatsapp')}
                    title="Partilhar no WhatsApp"
                    aria-label="Partilhar no WhatsApp"
                  >
                    <WhatsAppIcon />
                  </button>
                  <button
                    className="collection-page__share-btn"
                    onClick={() => setShareMode('general')}
                    title="Partilhar"
                    aria-label="Partilhar"
                  >
                    <ShareIcon />
                  </button>
                </div>
              </div>

              {selectedCountries.length > 0 && (
                <div className="collection-page__tags">
                  {selectedCountries.map(v => {
                    const label = countryOptions.find(o => o.value === v)?.label ?? v;
                    return (
                      <span key={v} className="collection-page__tag">
                        {label}
                        <button
                          className="collection-page__tag-remove"
                          onClick={() => handleCountryRemove(v)}
                          aria-label={`Remover ${label}`}
                        >
                          ✕
                        </button>
                      </span>
                    );
                  })}
                </div>
              )}
            </div>

            <div className="collection-page__sections">
              {sortedSections.map(([prefix, items]) => (
                <StickerSection
                  key={prefix}
                  prefix={prefix}
                  stickers={items}
                  onSave={handleSave}
                />
              ))}
              {sortedSections.length === 0 && (
                <p className="collection-page__empty">Nenhum cromo nesta aba.</p>
              )}
            </div>
          </>
        )}
      </main>
    </div>
    {shareMode && (
      <ShareModal
        mode={shareMode}
        stickers={stickers}
        onClose={() => setShareMode(null)}
      />
    )}
    </>
  );
}

function ProfileIcon() {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/>
      <circle cx="12" cy="7" r="4"/>
    </svg>
  );
}

function ChatIcon() {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/>
    </svg>
  );
}

function WhatsAppIcon() {
  return (
    <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
      <path fill="#25D366" d="M17.472 14.382c-.297-.149-1.758-.867-2.03-.967-.273-.099-.471-.148-.67.15-.197.297-.767.966-.94 1.164-.173.199-.347.223-.644.075-.297-.15-1.255-.463-2.39-1.475-.883-.788-1.48-1.761-1.653-2.059-.173-.297-.018-.458.13-.606.134-.133.298-.347.446-.52.149-.174.198-.298.298-.497.099-.198.05-.371-.025-.52-.075-.149-.669-1.612-.916-2.207-.242-.579-.487-.5-.669-.51-.173-.008-.371-.01-.57-.01-.198 0-.52.074-.792.372-.272.297-1.04 1.016-1.04 2.479 0 1.462 1.065 2.875 1.213 3.074.149.198 2.096 3.2 5.077 4.487.709.306 1.262.489 1.694.625.712.227 1.36.195 1.871.118.571-.085 1.758-.719 2.006-1.413.248-.694.248-1.289.173-1.413-.074-.124-.272-.198-.57-.347m-5.421 7.403h-.004a9.87 9.87 0 01-5.031-1.378l-.361-.214-3.741.982.998-3.648-.235-.374a9.86 9.86 0 01-1.51-5.26c.001-5.45 4.436-9.884 9.888-9.884 2.64 0 5.122 1.03 6.988 2.898a9.825 9.825 0 012.893 6.994c-.003 5.45-4.437 9.884-9.885 9.884m8.413-18.297A11.815 11.815 0 0012.05 0C5.495 0 .16 5.335.157 11.892c0 2.096.547 4.142 1.588 5.945L.057 24l6.305-1.654a11.882 11.882 0 005.683 1.448h.005c6.554 0 11.89-5.335 11.893-11.893a11.821 11.821 0 00-3.48-8.413z"/>
    </svg>
  );
}

function ShareIcon() {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M4 12v8a2 2 0 002 2h12a2 2 0 002-2v-8"/>
      <polyline points="16 6 12 2 8 6"/>
      <line x1="12" y1="2" x2="12" y2="15"/>
    </svg>
  );
}
