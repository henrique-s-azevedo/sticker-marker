import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/common/ProtectedRoute';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import CollectionPage from './pages/CollectionPage';
import ProfilePage from './pages/ProfilePage';
import PublicCollectionPage from './pages/PublicCollectionPage';
import InvitePage from './pages/InvitePage';
import ChatPage from './pages/ChatPage';
import TradePage from './pages/TradePage';
import TradeRespondPage from './pages/TradeRespondPage';
import SellPage from './pages/SellPage';

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter basename={import.meta.env.BASE_URL}>
        <Routes>
          <Route path="/login"    element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/collection" element={<ProtectedRoute><CollectionPage /></ProtectedRoute>} />
          <Route path="/profile"    element={<ProtectedRoute><ProfilePage /></ProtectedRoute>} />
          <Route path="/collection/:userTag" element={<ProtectedRoute><PublicCollectionPage /></ProtectedRoute>} />
          <Route path="/invite/:code"        element={<ProtectedRoute><InvitePage /></ProtectedRoute>} />
          <Route path="/chat/:friendId"      element={<ProtectedRoute><ChatPage /></ProtectedRoute>} />
          <Route path="/trade/:friendId"     element={<ProtectedRoute><TradePage /></ProtectedRoute>} />
          <Route path="/trade-respond/:tradeId" element={<ProtectedRoute><TradeRespondPage /></ProtectedRoute>} />
          <Route path="/sell/:friendId"         element={<ProtectedRoute><SellPage /></ProtectedRoute>} />
          <Route path="*" element={<Navigate to="/collection" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
