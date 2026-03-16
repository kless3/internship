import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api/axios.js';
import { normalizeApiError } from '../api/error-utils.js';

export default function LoginPage() {
  const [form, setForm] = useState({ login: '', password: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const onChange = (event) => {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const onSubmit = async (event) => {
    event.preventDefault();

    try {
      setLoading(true);
      setError('');
      const { data } = await api.post('/api/v1/auth/login', {
        login: form.login,
        password: form.password
      });

      localStorage.setItem('accessToken', data.accessToken);
      localStorage.setItem('refreshToken', data.refreshToken);
      navigate('/orders');
    } catch (e) {
      setError(normalizeApiError(e, 'Login failed.'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="container min-vh-100 d-flex align-items-center justify-content-center py-4">
      <div className="card shadow-sm border-0" style={{ maxWidth: 460, width: '100%' }}>
        <div className="card-body p-4 p-md-5">
          <h1 className="h4 mb-1">Sign in</h1>
          <p className="text-muted mb-4">Innowise Internship · Order Management System</p>

          <form onSubmit={onSubmit}>
            <div className="mb-3">
              <label className="form-label">Login (email)</label>
              <input
                className="form-control"
                type="email"
                name="login"
                value={form.login}
                onChange={onChange}
                required
                placeholder="name@example.com"
              />
            </div>

            <div className="mb-3">
              <label className="form-label">Password</label>
              <input
                className="form-control"
                type="password"
                name="password"
                minLength="6"
                value={form.password}
                onChange={onChange}
                required
                placeholder="Password"
              />
            </div>

            {error ? <div className="alert alert-danger py-2">{error}</div> : null}

            <button className="btn btn-primary w-100" type="submit" disabled={loading}>
              {loading ? 'Signing in...' : 'Sign in'}
            </button>
          </form>

          <p className="mt-3 mb-0 text-center">
            No account yet? <Link to="/register">Create one</Link>
          </p>
        </div>
      </div>
    </main>
  );
}
