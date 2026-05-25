import { useState, useRef, useEffect, useId } from 'react';
import './CountrySelect.css';

export default function CountrySelect({
  options = [],
  value,
  onChange,
  placeholder = 'Selecionar país...',
}) {
  const [open, setOpen] = useState(false);
  const [query, setQuery] = useState('');
  const [highlightedIndex, setHighlightedIndex] = useState(-1);

  const containerRef = useRef(null);
  const inputRef = useRef(null);
  const listRef = useRef(null);
  const id = useId();

  const selected = options.find(o => o.value === value) ?? null;

  const filtered = query.trim() === ''
    ? options
    : options.filter(o =>
        o.label.toLowerCase().includes(query.toLowerCase()) ||
        o.value.toLowerCase().includes(query.toLowerCase())
      );

  function openDropdown() {
    setOpen(true);
    setQuery('');
    setHighlightedIndex(selected ? options.indexOf(selected) : 0);
  }

  function closeDropdown(restoreInput = true) {
    setOpen(false);
    setHighlightedIndex(-1);
    if (restoreInput) setQuery('');
  }

  function selectOption(option) {
    onChange?.(option.value);
    closeDropdown();
    inputRef.current?.blur();
  }

  function handleInputChange(e) {
    setQuery(e.target.value);
    setOpen(true);
    setHighlightedIndex(0);
  }

  function handleKeyDown(e) {
    if (!open) {
      if (e.key === 'ArrowDown' || e.key === 'Enter' || e.key === ' ') {
        e.preventDefault();
        openDropdown();
      }
      return;
    }

    switch (e.key) {
      case 'ArrowDown':
        e.preventDefault();
        setHighlightedIndex(i => (i + 1) % filtered.length);
        break;
      case 'ArrowUp':
        e.preventDefault();
        setHighlightedIndex(i => (i - 1 + filtered.length) % filtered.length);
        break;
      case 'Enter':
        e.preventDefault();
        if (highlightedIndex >= 0 && filtered[highlightedIndex]) {
          selectOption(filtered[highlightedIndex]);
        }
        break;
      case 'Escape':
        e.preventDefault();
        closeDropdown();
        inputRef.current?.blur();
        break;
      case 'Tab':
        closeDropdown();
        break;
    }
  }

  function handleToggleClick() {
    if (open) {
      closeDropdown();
      inputRef.current?.blur();
    } else {
      inputRef.current?.focus();
      openDropdown();
    }
  }

  // Scroll highlighted item into view
  useEffect(() => {
    if (open && highlightedIndex >= 0 && listRef.current) {
      listRef.current.children[highlightedIndex]?.scrollIntoView({ block: 'nearest' });
    }
  }, [highlightedIndex, open]);

  // Close on outside click
  useEffect(() => {
    function onMouseDown(e) {
      if (!containerRef.current?.contains(e.target)) closeDropdown();
    }
    document.addEventListener('mousedown', onMouseDown);
    return () => document.removeEventListener('mousedown', onMouseDown);
  }, []);

  const displayValue = open ? query : (selected?.label ?? '');

  return (
    <div
      className="country-select"
      ref={containerRef}
    >
      <div className={`country-select__control${open ? ' country-select__control--open' : ''}`}>
        <input
          ref={inputRef}
          id={id}
          className="country-select__input"
          type="text"
          value={displayValue}
          placeholder={placeholder}
          onChange={handleInputChange}
          onFocus={openDropdown}
          onKeyDown={handleKeyDown}
          autoComplete="off"
          spellCheck={false}
          role="combobox"
          aria-expanded={open}
          aria-autocomplete="list"
          aria-controls={`${id}-listbox`}
          aria-activedescendant={
            open && highlightedIndex >= 0 ? `${id}-opt-${highlightedIndex}` : undefined
          }
        />
        <button
          type="button"
          className={`country-select__arrow${open ? ' country-select__arrow--open' : ''}`}
          onClick={handleToggleClick}
          tabIndex={-1}
          aria-label={open ? 'Fechar lista' : 'Abrir lista'}
        >
          <svg width="14" height="14" viewBox="0 0 14 14" fill="none" aria-hidden="true">
            <path d="M2.5 5l4.5 4 4.5-4" stroke="currentColor" strokeWidth="1.8"
              strokeLinecap="round" strokeLinejoin="round" />
          </svg>
        </button>
      </div>

      {open && (
        <ul
          ref={listRef}
          id={`${id}-listbox`}
          className="country-select__dropdown"
          role="listbox"
          aria-label="Países"
        >
          {filtered.length === 0 ? (
            <li className="country-select__empty">Nenhum resultado</li>
          ) : (
            filtered.map((option, i) => (
              <li
                key={option.value}
                id={`${id}-opt-${i}`}
                className={[
                  'country-select__option',
                  option.value === value && 'country-select__option--selected',
                  i === highlightedIndex && 'country-select__option--highlighted',
                ].filter(Boolean).join(' ')}
                role="option"
                aria-selected={option.value === value}
                onMouseDown={e => { e.preventDefault(); selectOption(option); }}
                onMouseEnter={() => setHighlightedIndex(i)}
              >
                <span className="country-select__badge">{option.value}</span>
                <span className="country-select__label">{option.label}</span>
              </li>
            ))
          )}
        </ul>
      )}
    </div>
  );
}
