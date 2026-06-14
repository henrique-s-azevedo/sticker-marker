/**
 * Read-only view of a friend's sticker collection.
 */
import { useState, useEffect, useMemo } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { getFriendStickers, getFriendProgress } from '../services/friendshipService';
import StatsBar from '../components/collection/StatsBar';
import TabBar from '../components/collection/TabBar';
import StickerSection from '../components/collection/StickerSection';
import StickerSearch from '../components/common/StickerSearch';
import './CollectionPage.css';

const COLLECTION_ID = 1;

function groupByPrefix(stickers) {
  const groups = {};
  for (const s of stickers) {
    if (!groups[s.teamInitial]) groups[s.teamInitial] = [];
    groups[s.teamInitial].push(s);
  }
  return groups;
}

export default function PublicCollectionPage() {
  const { userTag }      = useParams();
  const navigate         = useNavigate();
  const { t }            = useTranslation();
  const [stickers, setStickers]   = useState([]);
  const [progress, setProgress]   = useState(null);
  const [activeTab, setActiveTab] = useState('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading]     = useState(true);
  const [error, setError]         = useState(null);

  useEffect(() => {
    Promise.all([
      getFriendStickers(userTag, COLLECTION_ID),
      getFriendProgress(userTag, COLLECTION_ID),
    ])
      .then(([s, p]) => { setStickers(s); setProgress(p); })
      .catch(e => setError(e.message))
      .finally(() => setLoading(false));
  }, [userTag]);

  const filtered = useMemo(() => {
    if (activeTab === 'ALL') return stickers;
    if (activeTab === 'OWNED') return stickers.filter(s => s.status === 'OWNED' || s.status === 'DUPLICATE');
    return stickers.filter(s => s.status === activeTab);
  }, [stickers, activeTab]);

  const sortedSections = useMemo(() => {
    const q = searchQuery.trim().toLowerCase();
    const base = q ? filtered.filter(s => s.code.toLowerCase().includes(q)) : filtered;
    return Object.entries(groupByPrefix(base)).sort(([, a], [, b]) =>
      Math.min(...a.map(s => s.pageNumber)) - Math.min(...b.map(s => s.pageNumber))
    );
  }, [filtered, searchQuery]);

  return (
    <div className="collection-page">
      <header className="collection-page__header">
        <button
          style={{ background: 'none', border: '1px solid var(--color-border)', borderRadius: 'var(--radius-sm)', padding: '4px 12px', color: 'var(--color-text-muted)', fontSize: 'var(--font-size-sm)', cursor: 'pointer' }}
          onClick={() => navigate(-1)}
        >
          {t('public.back')}
        </button>
        <h1 className="collection-page__title">@{userTag} · WC 2026</h1>
        <span style={{ fontSize: 'var(--font-size-xs)', color: 'var(--color-text-muted)', padding: '4px 8px', border: '1px solid var(--color-border)', borderRadius: '999px' }}>
          {t('public.read_only')}
        </span>
      </header>

      <main className="collection-page__main">
        {loading && <p className="collection-page__status">{t('public.loading')}</p>}
        {error   && <p className="collection-page__status collection-page__status--error">{error}</p>}

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
            </div>
            <div className="collection-page__sections">
              {sortedSections.map(([prefix, items]) => (
                <StickerSection
                  key={prefix}
                  prefix={prefix}
                  stickers={items}
                  onSave={() => {}}
                />
              ))}
              {sortedSections.length === 0 && (
                <p className="collection-page__empty">{t('public.empty')}</p>
              )}
            </div>
          </>
        )}
      </main>
    </div>
  );
}
