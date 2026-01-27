import { useState, useEffect } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router';

const API_BASE_URL = '/api/auth';

export default function ResetPasswordPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const token = searchParams.get('token');

  useEffect(() => {
    if (!token) {
      setError('유효하지 않은 링크입니다. 이메일에서 받은 링크를 사용해주세요.');
    }
  }, [token]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setMessage('');

    // Validation
    if (password !== confirmPassword) {
      setError('비밀번호가 일치하지 않습니다.');
      return;
    }

    // 비밀번호 검증: 영문+숫자+특수문자 8자 이상
    const passwordRegex = /^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>/?]).{8,}$/;
    if (!passwordRegex.test(password)) {
      setError('비밀번호는 영문, 숫자, 특수문자를 포함하여 8자 이상이어야 합니다.');
      return;
    }

    if (!token) {
      setError('토큰이 없습니다.');
      return;
    }

    setLoading(true);

    try {
      const response = await fetch(`${API_BASE_URL}/reset-password`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          token,
          newPassword: password,
        }),
      });

      // 응답 본문이 있는지 확인
      const contentType = response.headers.get('content-type');
      let data = { message: '' };
      
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
        setMessage(data.message || '비밀번호가 성공적으로 재설정되었습니다.');
        // 즉시 로그인 페이지로 이동
        setTimeout(() => {
          navigate('/login');
        }, 1500);
      } else {
        setError(data.message || '비밀번호 재설정에 실패했습니다.');
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
            <p className="text-sm text-gray-500 mt-2">비밀번호 재설정</p>
          </div>

          {/* Form */}
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-1">
                새 비밀번호
              </label>
              <input
                id="password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-gray-900 focus:border-transparent"
                placeholder="영문+숫자+특수문자 8자 이상"
              />
            </div>

            <div>
              <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-700 mb-1">
                새 비밀번호 확인
              </label>
              <input
                id="confirmPassword"
                type="password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                required
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-gray-900 focus:border-transparent"
                placeholder="비밀번호 재입력"
              />
            </div>

            {message && (
              <div className="text-sm text-green-600 bg-green-50 px-3 py-2 rounded">
                {message}
              </div>
            )}
            {error && (
              <div className="text-sm text-red-600 bg-red-50 px-3 py-2 rounded">
                {error}
              </div>
            )}

            <button
              type="submit"
              disabled={loading || !token}
              className="w-full bg-gray-900 text-white py-3 rounded-lg font-medium hover:bg-gray-800 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? '재설정 중...' : '비밀번호 재설정'}
            </button>
          </form>

          {/* Link */}
          <div className="mt-6 text-center">
            <p className="text-sm text-gray-600">
              <Link to="/login" className="text-gray-900 font-medium hover:underline">
                로그인으로 돌아가기
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
