import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api/axios.js';
import { normalizeApiError } from '../api/error-utils.js';

export default function RegisterPage() {
  const [form, setForm] = useState({
    name: '',
    surname: '',
    birthDate: '',
    email: '',
    password: '',
    confirmPassword: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const passwordsMatch = form.password === form.confirmPassword;

  const onChange = (event) => {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const onSubmit = async (event) => {
    event.preventDefault();

    if (!passwordsMatch) {
      setError('Passwords do not match.');
      return;
    }

    try {
      setLoading(true);
      setError('');

      await api.post('/api/v1/auth/register', {
        login: form.email,
        name: form.name,
        surname: form.surname,
        birthDate: form.birthDate,
        email: form.email,
        password: form.password
      });

      navigate('/login');
    } catch (e) {
      setError(normalizeApiError(e, 'Registration failed.'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="container min-vh-100 d-flex align-items-center justify-content-center py-4">
      <div className="card shadow-sm border-0" style={{ maxWidth: 560, width: '100%' }}>
        <div className="card-body p-4 p-md-5">
          <h1 className="h4 mb-1">Create account</h1>
          <p className="text-muted mb-4">Innowise Internship · Order Management System</p>

          <form onSubmit={onSubmit}>
            <div className="row g-3">
              <div className="col-md-6">
                <label className="form-label">Name</label>
                <input className="form-control" name="name" value={form.name} onChange={onChange} required />
              </div>
              <div className="col-md-6">
                <label className="form-label">Surname</label>
                <input className="form-control" name="surname" value={form.surname} onChange={onChange} required />
              </div>
              <div className="col-md-6">
                <label className="form-label">Birth date</label>
                <input className="form-control" type="date" name="birthDate" value={form.birthDate} onChange={onChange} required />
              </div>
              <div className="col-md-6">
                <label className="form-label">Email</label>
                <input className="form-control" type="email" name="email" value={form.email} onChange={onChange} required />
              </div>
              <div className="col-md-6">
                <label className="form-label">Password</label>
                <input className="form-control" type="password" name="password" minLength="6" value={form.password} onChange={onChange} required />
              </div>
              <div className="col-md-6">
                <label className="form-label">Confirm password</label>
                <input className="form-control" type="password" name="confirmPassword" minLength="6" value={form.confirmPassword} onChange={onChange} required />
              </div>
            </div>

            {error ? <div className="alert alert-danger py-2 mt-3 mb-0">{error}</div> : null}

            <button className="btn btn-primary w-100 mt-3" type="submit" disabled={loading}>
              {loading ? 'Creating...' : 'Create account'}
            </button>
          </form>

          <p className="mt-3 mb-0 text-center">
            Already registered? <Link to="/login">Sign in</Link>
          </p>
        </div>
      </div>
    </main>
  );
}
