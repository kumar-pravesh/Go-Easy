import { Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Register from './pages/Register';
import UserHome from './pages/UserHome';
import DriverDashboard from './pages/DriverDashboard';
import LandingPage from './pages/LandingPage';
import CorporateLogin from './pages/CorporateLogin';
import CorporateDashboard from './pages/CorporateDashboard';
import RideReceipt from './pages/RideReceipt';
import PublicTracking from './pages/PublicTracking';
import AdminDashboard from './pages/AdminDashboard';
import { AuthProvider, useAuth } from './context/AuthContext';
import ErrorBoundary from './components/ErrorBoundary';

const ProtectedRoute = ({ children, role }) => {
  const { user, role: userRole } = useAuth();
  // Simple check - in real app check token expiry etc.
  // For now just pass through if we have token
  const token = localStorage.getItem('token');
  if (!token) return <Navigate to="/" />;
  return children;
};

function App() {
  return (
    <ErrorBoundary>
    <AuthProvider>
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/admin" element={<AdminDashboard />} />
        <Route path="/home" element={
          <ProtectedRoute>
            <UserHome />
          </ProtectedRoute>
        } />
        <Route path="/driver" element={
          <ProtectedRoute>
            <DriverDashboard />
          </ProtectedRoute>
        } />
        <Route path="/corporate/login"     element={<CorporateLogin />} />
        <Route path="/corporate/dashboard" element={<CorporateDashboard />} />
        <Route path="/receipt/:bookingId" element={
          <ProtectedRoute>
            <RideReceipt />
          </ProtectedRoute>
        } />
        <Route path="/track/:bookingId" element={<PublicTracking />} />
      </Routes>
    </AuthProvider>
    </ErrorBoundary>
  );
}

export default App;
