/**
 * Dropdown for selecting the sticker section sort order.
 * Available options: ALBUM (physical page order), AZ, ZA, MOST_COMPLETE, LEAST_COMPLETE.
 * Closes on outside click via mousedown listener.
 *
 * @param {string} value - currently selected option key
 * @param {Function} onChange - called with the new option key
 */
import { useState, useRef, useEffect } from 'react';
import './SortDropdown.css';

const OPTIONS = [
  { key: 'ALBUM', label: 'Album order' },
  { key: 'AZ', label: 'A → Z' },
  { key: 'ZA', label: 'Z → A' },
  { key: 'MOST_COMPLETE', label: 'Most complete' },
  { key: 'LEAST_COMPLETE', label: 'Least complete' },
];

export default function SortDropdown({ value, onChange }) {
  const [open, setOpen] = useState(false);
  const containerRef = useRef(null);
  const current = OPTIONS.find(o => o.key === value) ?? OPTIONS[0];

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
        <span>{current.label}</span>
        <svg className={`sort-dropdown__chevron${open ? ' sort-dropdown__chevron--open' : ''}`} width="12" height="12" viewBox="0 0 14 14" fill="none" aria-hidden="true">
          <path d="M2.5 5l4.5 4 4.5-4" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
      </button>

      {open && (
        <ul className="sort-dropdown__menu" role="listbox" aria-label="Sort order">
          {OPTIONS.map(opt => (
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
              {opt.label}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
