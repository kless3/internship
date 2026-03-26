import { useAuth } from '../auth/useAuth.js';

export default function Navbar({ userEmail }) {
  const { logout } = useAuth();

  const handleLogout = () => {
    logout();
  };

  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-dark border-bottom">
      <div className="container-fluid">
        <span className="navbar-brand fw-semibold">Innowise Internship · Order Management System</span>
        <div className="d-flex align-items-center gap-2">
          <span className="badge text-bg-secondary">{userEmail}</span>
          <button className="btn btn-outline-light btn-sm" onClick={handleLogout}>Log out</button>
        </div>
      </div>
    </nav>
  );
}
