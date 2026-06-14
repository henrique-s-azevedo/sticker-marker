import { useState, useRef, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import './SortDropdown.css';

const OPTION_KEYS = [
  { key: 'ALBUM',          tKey: 'sort.album' },
  { key: 'AZ',             tKey: 'sort.az' },
  { key: 'ZA',             tKey: 'sort.za' },
  { key: 'MOST_COMPLETE',  tKey: 'sort.most_complete' },
  { key: 'LEAST_COMPLETE', tKey: 'sort.least_complete' },
];

export default function SortDropdown({ value, onChange }) {
  const { t } = useTranslation();
  const [open, setOpen] = useState(false);
  const containerRef = useRef(null);
  const current = OPTION_KEYS.find(o => o.key === value) ?? OPTION_KEYS[0];

  useEffect(() => {
    function onMouseDown(e) {
      if (!containerRef.current?.contains(e.target)) setOpen(false);
    }
    document.addEventListener('mousedown', onMouseDown);
    return () => document.removeEventListener('mousedown', onMouseDown);
  }, []);

  function select(key) {
    onChange(key);
    setOpen(false);
  }

  return (
    <div className="sort-dropdown" ref={containerRef}>
      <button
        type="button"
        className={`sort-dropdown__trigger${open ? ' sort-dropdown__trigger--open' : ''}`}
        onClick={() => setOpen(o => !o)}
        aria-haspopup="listbox"
        aria-expanded={open}
      >
        <svg className="sort-dropdown__icon" width="14" height="14" viewBox="0 0 14 14" fill="none" aria-hidden="true">
          <path d="M2 4h10M4 7h6M6 10h2" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" />
        </svg>
        <span>{t(current.tKey)}</span>
        <svg className={`sort-dropdown__chevron${open ? ' sort-dropdown__chevron--open' : ''}`} width="12" height="12" viewBox="0 0 14 14" fill="none" aria-hidden="true">
          <path d="M2.5 5l4.5 4 4.5-4" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
      </button>

      {open && (
        <ul className="sort-dropdown__menu" role="listbox" aria-label="Sort order">
          {OPTION_KEYS.map(opt => (
            <li
              key={opt.key}
              role="option"
              aria-selected={opt.key === value}
              className={`sort-dropdown__option${opt.key === value ? ' sort-dropdown__option--active' : ''}`}
              onMouseDown={() => select(opt.key)}
            >
              {opt.key === value && (
                <svg width="12" height="12" viewBox="0 0 14 14" fill="none" aria-hidden="true">
                  <path d="M2 7l4 4 6-6" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
                </svg>
              )}
              {t(opt.tKey)}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
