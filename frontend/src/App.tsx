import { useEffect } from 'react';
import { Routes, Route, NavLink, Navigate } from 'react-router-dom';
import DashboardPage from './pages/DashboardPage';
import ImportsPage from './pages/ImportsPage';
import InventoryPage from './pages/InventoryPage';
import WorkforcePage from './pages/WorkforcePage';
import FinancePage from './pages/FinancePage';
import ReportsPage from './pages/ReportsPage';
import LoginPage from './pages/LoginPage';
import UsersPage from './pages/UsersPage';
import ProtectedRoute from './components/ProtectedRoute';
import { useAuth } from './context/AuthContext';

const NAV = [
  { to: '/', label: 'Overview' },
  { to: '/imports', label: 'Import centre' },
  { to: '/inventory', label: 'Inventory' },
  { to: '/workforce', label: 'Workforce' },
  { to: '/finance', label: 'Finance & approvals' },
  { to: '/reports', label: 'Reports' },
];

function Shell() {
  const { user, logout } = useAuth();
  const nav = user?.role === 'ADMIN' ? [...NAV, { to: '/users', label: 'Users' }] : NAV;
  return (
    <div className="app-shell">
      <aside className="sidebar">
        <h2 style={{ marginTop: 0 }}>SAARTHI</h2>
        <nav>
          {nav.map((item) => (
            <NavLink key={item.to} to={item.to} end={item.to === '/'} className={({ isActive }) => (isActive ? 'active' : '')}>
              {item.label}
            </NavLink>
          ))}
        </nav>
        {user && (
          <div style={{ marginTop: 24, paddingTop: 16, borderTop: '1px solid #33473a', fontSize: 12, color: '#cfd8cf' }}>
            <div>{user.displayName}</div>
            <div style={{ opacity: 0.7 }}>{user.role.replace('_', ' ')}</div>
            <button
              onClick={logout}
              style={{ marginTop: 8, background: 'transparent', border: '1px solid #4a5c4d', color: '#cfd8cf', padding: '6px 10px' }}
            >
              Sign out
            </button>
          </div>
        )}
      </aside>
      <main className="main">
        <Routes>
          <Route path="/" element={<DashboardPage />} />
          <Route path="/imports" element={<ImportsPage />} />
          <Route path="/inventory" element={<InventoryPage />} />
          <Route path="/workforce" element={<WorkforcePage />} />
          <Route path="/finance" element={<FinancePage />} />
          <Route path="/reports" element={<ReportsPage />} />
          <Route path="/users" element={<UsersPage />} />
        </Routes>
      </main>
    </div>
  );
}

export default function App() {
  const { logout, isAuthenticated } = useAuth();

  // If any API call comes back 401 (expired/invalid token), sign the user out
  // and let ProtectedRoute redirect to /login.
  useEffect(() => {
    const handler = () => logout();
    window.addEventListener('auth:unauthorized', handler);
    return () => window.removeEventListener('auth:unauthorized', handler);
  }, [logout]);

  return (
    <Routes>
      <Route path="/login" element={isAuthenticated ? <Navigate to="/" replace /> : <LoginPage />} />
      <Route
        path="/*"
        element={
          <ProtectedRoute>
            <Shell />
          </ProtectedRoute>
        }
      />
    </Routes>
  );
}
