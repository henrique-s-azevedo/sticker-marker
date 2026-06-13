/**
 * Styled text input with an optional show/hide toggle for password fields.
 *
 * @param {string} type - input type; "password" enables the toggle button
 * @param {string} placeholder
 * @param {string} value
 * @param {Function} onChange
 * @param {string} name
 * @param {boolean} required
 */
import { useState } from 'react';
import './Input.css';

export default function Input({ type = 'text', placeholder, value, onChange, name, required }) {
  const [showPassword, setShowPassword] = useState(false);
  const isPassword = type === 'password';

  return (
    <div className="input-wrapper">
      <input
        className="input"
        type={isPassword && showPassword ? 'text' : type}
        placeholder={placeholder}
        value={value}
        onChange={onChange}
        name={name}
        required={required}
        autoComplete={isPassword ? 'current-password' : undefined}
      />
      {isPassword && (
        <button
          type="button"
          className="input__toggle-password"
          onClick={() => setShowPassword(v => !v)}
          aria-label={showPassword ? 'Hide password' : 'Show password'}
        >
          {showPassword ? '🙈' : '👁'}
        </button>
      )}
    </div>
  );
}
