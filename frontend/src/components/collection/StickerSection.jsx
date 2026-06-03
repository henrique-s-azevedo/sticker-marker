import StickerCard from './StickerCard';
import './StickerSection.css';

export default function StickerSection({ prefix, stickers, flagEmoji, onSave, quickMode, activeTab }) {
  const ownedCount = stickers.filter(s => s.status === 'OWNED' || s.status === 'DUPLICATE').length;
  const progress = stickers.length > 0 ? Math.round((ownedCount / stickers.length) * 100) : 0;

  return (
    <section className="sticker-section">
      <div className="sticker-section__header">
        <span className="sticker-section__flag">{flagEmoji ?? '🌐'}</span>
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
