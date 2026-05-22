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
import './CollectionPage.css';

const COLLECTION_ID = 1;

function groupByPrefix(stickers) {
  const groups = {};
  for (const s of stickers) {
    const prefix = s.code.split(' ')[0];
    if (!groups[prefix]) groups[prefix] = [];
    groups[prefix].push(s);
  }
  return groups;
}

export default function CollectionPage() {
  const { logout } = useAuth();
  const navigate = useNavigate();

  const [stickers, setStickers] = useState([]);
  const [progress, setProgress] = useState(null);
  const [activeTab, setActiveTab] = useState('ALL');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

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

  const grouped = useMemo(() => groupByPrefix(filtered), [filtered]);

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
    <div className="collection-page">
      <header className="collection-page__header">
        <h1 className="collection-page__title">Panini WC 2026 Album</h1>
        <button className="collection-page__logout" onClick={handleLogout}>
          Sair
        </button>
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

            <div className="collection-page__sections">
              {Object.entries(grouped).map(([prefix, items]) => (
                <StickerSection
                  key={prefix}
                  prefix={prefix}
                  stickers={items}
                  onSave={handleSave}
                />
              ))}
              {Object.keys(grouped).length === 0 && (
                <p className="collection-page__empty">Nenhum cromo nesta aba.</p>
              )}
            </div>
          </>
        )}
      </main>
    </div>
  );
}
