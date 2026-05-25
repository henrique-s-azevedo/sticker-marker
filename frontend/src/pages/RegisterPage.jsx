import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AuthLayout from '../components/auth/AuthLayout';
import Input from '../components/common/Input';
import Button from '../components/common/Button';
import { register } from '../services/authService';
import { useAuth } from '../context/AuthContext';
import './RegisterPage.css';

export default function RegisterPage() {
  const { saveSession } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    terms: false,
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
        <h1 className="auth-form__title">Create an account</h1>
        <p className="auth-form__subtitle">
          Already have an account? <Link to="/login">Log in</Link>
        </p>

        <form className="auth-form__fields" onSubmit={handleSubmit}>
          <div className="auth-form__row">
            <Input
              name="firstName"
              placeholder="First name"
              value={form.firstName}
              onChange={handleChange}
              required
            />
            <Input
              name="lastName"
              placeholder="Last name"
              value={form.lastName}
              onChange={handleChange}
              required
            />
          </div>

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

          <label className="auth-form__checkbox">
            <input
              type="checkbox"
              name="terms"
              checked={form.terms}
              onChange={handleChange}
            />
            <span>I agree to the <Link to="/terms">Terms &amp; Conditions</Link></span>
          </label>

          {error && <p className="auth-form__error">{error}</p>}

          <Button type="submit" fullWidth disabled={!form.terms || loading}>
            {loading ? 'A criar conta...' : 'Create account'}
          </Button>
        </form>

        <div className="auth-form__divider">
          <span>Or register with</span>
        </div>

        <div className="auth-form__social">
          <Button variant="outline" fullWidth>
            <img src="https://www.google.com/favicon.ico" width={16} height={16} alt="" />
            Google
          </Button>
          <Button variant="outline" fullWidth>
            Apple
          </Button>
        </div>
      </div>
    </AuthLayout>
  );
}
