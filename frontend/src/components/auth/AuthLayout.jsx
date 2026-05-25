import albumFace from '../../assets/images/album-face.png';
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
