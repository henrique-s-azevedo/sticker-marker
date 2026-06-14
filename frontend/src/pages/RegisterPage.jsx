import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import AuthLayout from '../components/auth/AuthLayout';
import Input from '../components/common/Input';
import Button from '../components/common/Button';
import GoogleSignInButton from '../components/auth/GoogleSignInButton';
import LanguageToggle from '../components/common/LanguageToggle';
import { register } from '../services/authService';
import { useAuth } from '../context/AuthContext';
import './RegisterPage.css';

export default function RegisterPage() {
  const { saveSession } = useAuth();
  const navigate = useNavigate();
  const { t, i18n } = useTranslation();
  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  function handleChange(e) {
    const { name, value, type, checked } = e.target;
    setForm(prev => ({ ...prev, [name]: type === 'checkbox' ? checked : value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const displayName = `${form.firstName} ${form.lastName}`.trim();
      const data = await register(displayName, form.email, form.password);
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
        <h1 className="auth-form__title">{t('register.title')}</h1>
        <p className="auth-form__subtitle">
          {t('register.have_account')} <Link to="/login">{t('register.log_in')}</Link>
        </p>

        <form className="auth-form__fields" onSubmit={handleSubmit}>
          <div className="auth-form__row">
            <Input
              name="firstName"
              placeholder={t('register.first_name')}
              value={form.firstName}
              onChange={handleChange}
              required
            />
            <Input
              name="lastName"
              placeholder={t('register.last_name')}
              value={form.lastName}
              onChange={handleChange}
              required
            />
          </div>

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
            {loading ? t('register.loading') : t('register.submit')}
          </Button>
        </form>

        <div className="auth-form__divider">
          <span>{t('register.or_register')}</span>
        </div>

        <div className="auth-form__social">
          <GoogleSignInButton key={i18n.language} locale={i18n.language} onError={setError} />
        </div>

        <LanguageToggle />
      </div>
    </AuthLayout>
  );
}
