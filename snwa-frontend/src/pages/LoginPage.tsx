import { useState } from 'react';
import { Link, useNavigate } from 'react-router';

const API_BASE_URL = '/api/auth';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await fetch(`${API_BASE_URL}/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          email,
          password,
        }),
      });

      // 응답 본문이 있는지 확인
      const contentType = response.headers.get('content-type');
      let data = { token: '', message: '' };
      
      if (contentType && contentType.includes('application/json')) {
        const text = await response.text();
        if (text) {
          try {
            data = JSON.parse(text);
          } catch (e) {
            console.error('JSON 파싱 오류:', e);
          }
        }
      }

      if (response.ok) {
        // JWT 토큰을 localStorage에 저장
        if (data.token) {
          localStorage.setItem('snwa_token', data.token);
          localStorage.setItem('snwa_user', JSON.stringify({ email }));
        }
        navigate('/');
      } else {
        setError(data.message || '이메일 또는 비밀번호가 올바르지 않습니다.');
      }
    } catch (err) {
      setError(`서버 연결 실패: ${err instanceof Error ? err.message : '알 수 없는 오류'}`);
    }
    
    setLoading(false);
  };

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
      <div className="w-full max-w-md">
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-8">
          {/* Logo */}
          <div className="text-center mb-8">
            <Link to="/" className="text-3xl font-bold text-gray-900 tracking-tight">
              SNWA
            </Link>
            <p className="text-sm text-gray-500 mt-2">해외 스포츠 뉴스 번역 플랫폼</p>
          </div>

          {/* Login Form */}
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-1">
                이메일
              </label>
              <input
                id="email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-gray-900 focus:border-transparent"
                placeholder="example@email.com"
              />
            </div>

            <div>
              <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-1">
                비밀번호
              </label>
              <input
                id="password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-gray-900 focus:border-transparent"
                placeholder="••••••••"
              />
            </div>

            {error && (
              <div className="text-sm text-red-600 bg-red-50 px-3 py-2 rounded">
                {error}
              </div>
            )}

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-gray-900 text-white py-3 rounded-lg font-medium hover:bg-gray-800 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? '로그인 중...' : '로그인'}
            </button>
          </form>

          {/* Signup Link */}
          <div className="mt-6 text-center space-y-2">
            <p className="text-sm text-gray-600">
              계정이 없으신가요?{' '}
              <Link to="/signup" className="text-gray-900 font-medium hover:underline">
                회원가입
              </Link>
            </p>
            <p className="text-sm text-gray-600">
              비밀번호를 잊으셨나요?{' '}
              <Link to="/forgot-password" className="text-gray-900 font-medium hover:underline">
                비밀번호 찾기
              </Link>
            </p>
          </div>

          {/* Guest Notice */}
          <div className="mt-6 pt-6 border-t border-gray-200">
            <p className="text-xs text-gray-500 text-center">
              비회원도 기사 열람이 가능합니다
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}