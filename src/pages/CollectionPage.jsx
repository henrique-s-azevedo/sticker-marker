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

  const countryOptions = useMemo(() => {
    const seen = new Map();
    for (const s of stickers) {
      if (!seen.has(s.teamInitial)) seen.set(s.teamInitial, s.teamName);
    }
    return [...seen.entries()]
      .map(([value, label]) => ({ value, label }))
      .sort((a, b) => a.label.localeCompare(b.label));
  }, [stickers]);

  const grouped = useMemo(() => {
    const byCountry = selectedCountries.length > 0
      ? filtered.filter(s => selectedCountries.includes(s.teamInitial))
      : filtered;
    return groupByPrefix(byCountry);
  }, [filtered, selectedCountries]);

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

            <div className="collection-page__filters">
              <div className="collection-page__filters-row">
                <CountrySelect
                  options={countryOptions}
                  value={null}
                  onChange={handleCountrySelect}
                  placeholder="Filtrar por país..."
                />
                {selectedCountries.length > 0 && (
                  <button
                    className="collection-page__reset"
                    onClick={() => setSelectedCountries([])}
                  >
                    Limpar filtros
                  </button>
                )}
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
