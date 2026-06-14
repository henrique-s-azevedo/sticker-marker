import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import './Input.css';

export default function Input({ type = 'text', placeholder, value, onChange, name, required }) {
  const { t } = useTranslation();
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
          aria-label={showPassword ? t('input.hide_password') : t('input.show_password')}
        >
          {showPassword ? '🙈' : '👁'}
        </button>
      )}
    </div>
  );
}
