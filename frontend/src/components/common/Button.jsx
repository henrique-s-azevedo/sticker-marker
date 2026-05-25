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
