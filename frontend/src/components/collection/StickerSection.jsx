import StickerCard from './StickerCard';
import './StickerSection.css';

function flagSrc(code) {
  const base = import.meta.env.BASE_URL ?? '/';
  const ext = code === 'FWC' ? 'webp' : 'svg';
  return `${base}flags/${code}.${ext}`;
}

export default function StickerSection({ prefix, stickers, onSave, quickMode, activeTab }) {
  const ownedCount = stickers.filter(s => s.status === 'OWNED' || s.status === 'DUPLICATE').length;
  const progress = stickers.length > 0 ? Math.round((ownedCount / stickers.length) * 100) : 0;

  return (
    <section className="sticker-section">
      <div className="sticker-section__header">
        <img
          className="sticker-section__flag"
          src={flagSrc(prefix)}
          alt={prefix}
          onError={e => { e.currentTarget.style.display = 'none'; }}
        />
        <span className="sticker-section__name">{prefix}</span>
        <span className="sticker-section__progress">{progress}%</span>
      </div>
      <div className="sticker-section__grid">
        {stickers.map(sticker => (
          <StickerCard
            key={sticker.id}
            code={sticker.code}
            status={sticker.status}
            duplicateCount={sticker.duplicateQuantity}
            onSave={delta => onSave?.(sticker, delta)}
            quickMode={quickMode}
            activeTab={activeTab}
          />
        ))}
      </div>
    </section>
  );
}
