/**
 * Two-column authentication layout: album cover image on the left,
 * form panel on the right.
 * Used by LoginPage and RegisterPage.
 */
import albumFace from '../../assets/images/capa_caderneta_copa_2026.png';
import './AuthLayout.css';

export default function AuthLayout({ children }) {
  return (
    <div className="auth-layout">
      <div className="auth-layout__image">
        <img src={albumFace} alt="FIFA World Cup 2026 Album" />
      </div>
      <div className="auth-layout__panel">
        {children}
      </div>
    </div>
  );
}
