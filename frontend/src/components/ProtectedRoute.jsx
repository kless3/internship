import { Navigate } from 'react-router-dom';
import { useAuth } from '../auth/useAuth.js';

export default function ProtectedRoute({ children }) {
  const { initialized, authenticated } = useAuth();

  if (!initialized) {
    return <div className="container py-5 text-center">Authorizing...</div>;
  }

  if (!authenticated) {
    return <Navigate to="/login" replace />;
  }

  return children;
}
