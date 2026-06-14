import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import AuthLayout from '../components/auth/AuthLayout';
import Input from '../components/common/Input';
import Button from '../components/common/Button';
import GoogleSignInButton from '../components/auth/GoogleSignInButton';
import LanguageToggle from '../components/common/LanguageToggle';
import { login } from '../services/authService';
import { useAuth } from '../context/AuthContext';
import './LoginPage.css';

export default function LoginPage() {
  const { saveSession } = useAuth();
  const navigate = useNavigate();
  const { t, i18n } = useTranslation();
  const [form, setForm] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  function handleChange(e) {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const data = await login(form.email, form.password);
      saveSession(data.accessToken, data.refreshToken);
      navigate('/collection');
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthLayout>
      <div className="auth-form">
        <h1 className="auth-form__title">{t('login.title')}</h1>
        <p className="auth-form__subtitle">
          {t('login.no_account')} <Link to="/register">{t('login.create_one')}</Link>
        </p>

        <form className="auth-form__fields" onSubmit={handleSubmit}>
          <Input
            name="email"
            type="email"
            placeholder={t('login.email')}
            value={form.email}
            onChange={handleChange}
            required
          />
          <Input
            name="password"
            type="password"
            placeholder={t('login.password')}
            value={form.password}
            onChange={handleChange}
            required
          />
          {error && <p className="auth-form__error">{error}</p>}
          <Button type="submit" fullWidth disabled={loading}>
            {loading ? t('login.loading') : t('login.submit')}
          </Button>
        </form>

        <div className="auth-form__divider">
          <span>{t('login.or_sign_in')}</span>
        </div>

        <div className="auth-form__social">
          <GoogleSignInButton key={i18n.language} locale={i18n.language} onError={setError} />
        </div>

        <LanguageToggle />
      </div>
    </AuthLayout>
  );
}
