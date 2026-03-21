import { useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/useAuth.js';

export default function RegisterPage() {
  const navigate = useNavigate();
  const { initialized, authenticated, register, loginWithGoogle } = useAuth();

  useEffect(() => {
    if (initialized && authenticated) {
      navigate('/orders');
    }
  }, [initialized, authenticated, navigate]);

  return (
    <main className="container min-vh-100 d-flex align-items-center justify-content-center py-4">
      <div className="card shadow-sm border-0" style={{ maxWidth: 560, width: '100%' }}>
        <div className="card-body p-4 p-md-5">
          <h1 className="h4 mb-1">Create account</h1>
          <p className="text-muted mb-4">Account registration is handled by Keycloak.</p>

          <button className="btn btn-primary w-100 mb-2" type="button" onClick={() => register()}>
            Sign up with Keycloak
          </button>

          <button className="btn btn-outline-danger w-100" type="button" onClick={() => loginWithGoogle()}>
            Sign up with Google
          </button>

          <p className="mt-3 mb-0 text-center">
            Already registered? <Link to="/login">Sign in</Link>
          </p>
        </div>
      </div>
    </main>
  );
}
