/**
 * Route guard that redirects unauthenticated users to /login.
 * Renders nothing while the session is still being re-hydrated (loading=true)
 * to prevent a flash of the login page on page reload.
 */
import { Navigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export default function ProtectedRoute({ children }) {
  const { user, loading } = useAuth();

  if (loading) return null;
  if (!user) return <Navigate to="/login" replace />;
  return children;
}
