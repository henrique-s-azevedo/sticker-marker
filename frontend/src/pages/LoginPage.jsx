/**
 * Login page — email/password form and Google Sign-In.
 * On success, calls saveSession() from AuthContext and navigates to /collection.
 * Errors from either auth method are displayed inline below the form.
 */
import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AuthLayout from '../components/auth/AuthLayout';
import Input from '../components/common/Input';
import Button from '../components/common/Button';
import GoogleSignInButton from '../components/auth/GoogleSignInButton';
import { login } from '../services/authService';
import { useAuth } from '../context/AuthContext';
import './LoginPage.css';

export default function LoginPage() {
  const { saveSession } = useAuth();
  const navigate = useNavigate();
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
        <h1 className="auth-form__title">Welcome back</h1>
        <p className="auth-form__subtitle">
          Don&apos;t have an account? <Link to="/register">Create one</Link>
        </p>

        <form className="auth-form__fields" onSubmit={handleSubmit}>
          <Input
            name="email"
            type="email"
            placeholder="Email"
            value={form.email}
            onChange={handleChange}
            required
          />
          <Input
            name="password"
            type="password"
            placeholder="Enter your password"
            value={form.password}
            onChange={handleChange}
            required
          />
          {error && <p className="auth-form__error">{error}</p>}
          <Button type="submit" fullWidth disabled={loading}>
            {loading ? 'A entrar...' : 'Log in'}
          </Button>
        </form>

        <div className="auth-form__divider">
          <span>Or sign in with</span>
        </div>

        <div className="auth-form__social">
          <GoogleSignInButton onError={setError} />
        </div>
      </div>
    </AuthLayout>
  );
}
