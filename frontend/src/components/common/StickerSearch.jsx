/**
 * Inline search bar for filtering stickers by code, team initial, or number.
 * Shows a clear button when a query is active.
 *
 * @param {string} value - controlled query string
 * @param {Function} onChange - called with the new string value
 */
import './StickerSearch.css';

export default function StickerSearch({ value, onChange }) {
  return (
    <div className={`sticker-search${value ? ' sticker-search--active' : ''}`}>
      <svg className="sticker-search__icon" width="15" height="15" viewBox="0 0 15 15" fill="none" aria-hidden="true">
        <circle cx="6.5" cy="6.5" r="4.5" stroke="currentColor" strokeWidth="1.6" />
        <path d="M10 10l3 3" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" />
      </svg>
      <input
        className="sticker-search__input"
        type="text"
        value={value}
        onChange={e => onChange(e.target.value)}
        placeholder="Pesquisar cromo... (ex: CAN, CAN1, 1)"
        autoComplete="off"
        spellCheck={false}
      />
      {value && (
        <button
          type="button"
          className="sticker-search__clear"
          onClick={() => onChange('')}
          aria-label="Limpar pesquisa"
        >
          <svg width="12" height="12" viewBox="0 0 14 14" fill="none" aria-hidden="true">
            <path d="M2 2l10 10M12 2L2 12" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" />
          </svg>
        </button>
      )}
    </div>
  );
}
