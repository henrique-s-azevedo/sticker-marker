/**
 * Individual sticker card with two interaction modes:
 *
 * Normal mode: clicking opens an inline overlay with +/- counter and a Save button.
 *   The delta is relative to the current total (MISSING=0, OWNED=1, DUPLICATE=1+duplicateCount).
 *   Saving with delta=0 is a no-op.
 *
 * Quick mode: clicking immediately applies +1 (for MISSING tab) or -1 (for OWNED/DUPLICATE tab),
 *   with a brief CSS flash animation for feedback. Useful for bulk entry.
 *
 * The parent (StickerSection → CollectionPage) handles the actual API calls via `onSave(delta)`.
 *
 * @param {string} code - sticker code (e.g. "BRA1")
 * @param {string} status - "MISSING" | "OWNED" | "DUPLICATE"
 * @param {number} duplicateCount - number of extra copies (only meaningful when DUPLICATE)
 * @param {Function} onSave - called with a numeric delta when the user saves
 * @param {boolean} quickMode - enables single-click mode
 * @param {string} activeTab - current tab; determines quick-mode direction
 */
import { useState } from 'react';
import defaultSticker from '../../assets/images/default-sticker.jpg';
import './StickerCard.css';

function currentTotal(status, duplicateCount) {
  if (status === 'MISSING') return 0;
  if (status === 'OWNED') return 1;
  return 1 + (duplicateCount || 0);
}

export default function StickerCard({ code, status, duplicateCount, onSave, quickMode, activeTab }) {
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
              Guardar
            </button>
          </div>
        )}
      </div>
    </>
  );
}
