/**
 * Generic button component with variant and full-width support.
 *
 * @param {string} variant - CSS modifier: "primary" (default) or "secondary"
 * @param {boolean} fullWidth - adds btn--full class for 100% width
 * @param {boolean} disabled
 * @param {string} type - HTML button type attribute (default "button")
 */
import './Button.css';

export default function Button({ children, type = 'button', variant = 'primary', onClick, disabled, fullWidth }) {
  return (
    <button
      type={type}
      className={`btn btn--${variant}${fullWidth ? ' btn--full' : ''}`}
      onClick={onClick}
      disabled={disabled}
    >
      {children}
    </button>
  );
}
