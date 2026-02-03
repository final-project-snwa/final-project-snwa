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
import CoinPurchasePage from './pages/CoinPurchasePage';
import AdminPage from './pages/AdminPage';
import AuthTestPage from './pages/AuthTestPage';

// ✅ 추가: 결제 테스트 페이지들
import PayPage from './pages/PayPage';
import PaySuccessPage from './pages/PaySuccessPage';
import PayFailPage from './pages/PayFailPage';

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
        path: '/coins',
        element: <CoinPurchasePage />,
    },
    {
        path: '/admin',
        element: <AdminPage />,
    },
    {
        path: '/auth-test',
        element: <AuthTestPage />,
    },

    // ✅ 추가: 결제 라우트 3개
    {
        path: '/pay',
        element: <PayPage />,
    },
    {
        path: '/pay/success',
        element: <PaySuccessPage />,
    },
    {
        path: '/pay/fail',
        element: <PayFailPage />,
    },
]);

export default function App() {
    return (
        <AuthProvider>
            <RouterProvider router={router} />
        </AuthProvider>
    );
}
