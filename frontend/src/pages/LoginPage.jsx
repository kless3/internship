import { useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/useAuth.js';

export default function LoginPage() {
  const navigate = useNavigate();
  const { initialized, authenticated, login, loginWithGoogle } = useAuth();

  useEffect(() => {
    if (initialized && authenticated) {
      navigate('/orders');
    }
  }, [initialized, authenticated, navigate]);

  return (
    <main className="container min-vh-100 d-flex align-items-center justify-content-center py-4">
      <div className="card shadow-sm border-0" style={{ maxWidth: 460, width: '100%' }}>
        <div className="card-body p-4 p-md-5">
          <h1 className="h4 mb-1">Sign in</h1>
          <p className="text-muted mb-4">Innowise Internship · Order Management System</p>

          <button className="btn btn-primary w-100 mb-2" type="button" onClick={() => login()}>
            Continue with Keycloak
          </button>

          <button className="btn btn-outline-danger w-100" type="button" onClick={() => loginWithGoogle()}>
            Continue with Google
          </button>

          <p className="mt-3 mb-0 text-center">
            No account yet? <Link to="/register">Create one</Link>
          </p>
        </div>
      </div>
    </main>
  );
}
