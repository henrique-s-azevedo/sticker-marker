import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import defaultSticker from '../../assets/images/default-sticker.jpg';
import './StickerCard.css';

function currentTotal(status, duplicateCount) {
  if (status === 'MISSING') return 0;
  if (status === 'OWNED') return 1;
  return 1 + (duplicateCount || 0);
}

export default function StickerCard({ code, status, duplicateCount, onSave, quickMode, activeTab }) {
  const { t } = useTranslation();
  const [isOpen, setIsOpen] = useState(false);
  const [delta, setDelta] = useState(0);
  const [flash, setFlash] = useState(null);

  const total = currentTotal(status, duplicateCount);
  const statusClass = status?.toLowerCase() ?? 'missing';
  const isDuplicate = status === 'DUPLICATE';

  function openOverlay() {
    setDelta(0);
    setIsOpen(true);
  }

  function handleSave() {
    onSave?.(delta);
    setIsOpen(false);
  }

  function handleQuickClick() {
    const removing = activeTab === 'OWNED' || activeTab === 'DUPLICATE';
    const d = removing ? -1 : 1;
    if (d === -1 && status === 'MISSING') return;
    onSave?.(d);
    setFlash(d > 0 ? 'add' : 'remove');
    setTimeout(() => setFlash(null), 300);
  }

  function handleClick() {
    if (isOpen) return;
    if (quickMode) { handleQuickClick(); return; }
    openOverlay();
  }

  return (
    <>
      {isOpen && (
        <div className="sticker-overlay__backdrop" onClick={() => setIsOpen(false)} />
      )}
      <div
        className={`sticker-card sticker-card--${statusClass}${isOpen ? ' sticker-card--open' : ''}${quickMode ? ' sticker-card--quick' : ''}${flash ? ` sticker-card--flash-${flash}` : ''}`}
        onClick={handleClick}
      >
        <span className="sticker-card__code">{code}</span>
        <img src={defaultSticker} alt={code} className="sticker-card__img" />
        {isDuplicate && duplicateCount > 0 && (
          <span className="sticker-card__badge">{duplicateCount}</span>
        )}

        {isOpen && (
          <div className="sticker-card__overlay" onClick={e => e.stopPropagation()}>
            <div className="sticker-card__overlay-counter">
              <button
                className="sticker-card__overlay-btn"
                onClick={() => setDelta(d => Math.max(-total, d - 1))}
              >−</button>
              <span className="sticker-card__overlay-qty">{delta > 0 ? `+${delta}` : delta}</span>
              <button
                className="sticker-card__overlay-btn"
                onClick={() => setDelta(d => d + 1)}
              >+</button>
            </div>
            <button className="sticker-card__overlay-save" onClick={handleSave}>
              {t('sticker.save')}
            </button>
          </div>
        )}
      </div>
    </>
  );
}
