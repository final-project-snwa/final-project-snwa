import { createBrowserRouter, RouterProvider } from 'react-router';
import { AuthProvider } from './contexts/AuthContext';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import ForgotPasswordPage from './pages/ForgotPasswordPage';
import ResetPasswordPage from './pages/ResetPasswordPage';
import VerifyEmailPage from './pages/VerifyEmailPage';
import MainPage from './pages/MainPage';
import ArticleDetailPage from './pages/ArticleDetailPage';
import MyPage from './pages/MyPage';
import AdminPage from './pages/AdminPage';
import AuthTestPage from './pages/AuthTestPage';

const router = createBrowserRouter([
  {
    path: '/',
    element: <MainPage />,
  },
  {
    path: '/login',
    element: <LoginPage />,
  },
  {
    path: '/signup',
    element: <SignupPage />,
  },
  {
    path: '/forgot-password',
    element: <ForgotPasswordPage />,
  },
  {
    path: '/reset-password',
    element: <ResetPasswordPage />,
  },
  {
    path: '/verify-email',
    element: <VerifyEmailPage />,
  },
  {
    path: '/articles/:id',
    element: <ArticleDetailPage />,
  },
  {
    path: '/mypage',
    element: <MyPage />,
  },
  {
    path: '/admin',
    element: <AdminPage />,
  },
  {
    path: '/auth-test',
    element: <AuthTestPage />,
  },
]);

export default function App() {
  return (
    <AuthProvider>
      <RouterProvider router={router} />
    </AuthProvider>
  );
}
