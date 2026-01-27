import { useState } from 'react';
import { Link } from 'react-router';

const API_BASE_URL = '/api/auth';

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState('');
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setMessage('');
    setLoading(true);

    try {
      const response = await fetch(`${API_BASE_URL}/forgot-password`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email }),
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
        setMessage(data.message || '비밀번호 재설정 링크가 이메일로 발송되었습니다.');
        setEmail('');
      } else {
        setError(data.message || '비밀번호 찾기에 실패했습니다.');
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
            <p className="text-sm text-gray-500 mt-2">비밀번호 찾기</p>
          </div>

          {/* Form */}
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
              <p className="text-xs text-gray-500 mt-1">
                회원가입 시 사용한 이메일 주소를 입력해주세요.
              </p>
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
              disabled={loading}
              className="w-full bg-gray-900 text-white py-3 rounded-lg font-medium hover:bg-gray-800 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? '전송 중...' : '비밀번호 재설정 링크 보내기'}
            </button>
          </form>

          {/* Links */}
          <div className="mt-6 text-center space-y-2">
            <p className="text-sm text-gray-600">
              <Link to="/login" className="text-gray-900 font-medium hover:underline">
                로그인으로 돌아가기
              </Link>
            </p>
            <p className="text-sm text-gray-600">
              계정이 없으신가요?{' '}
              <Link to="/signup" className="text-gray-900 font-medium hover:underline">
                회원가입
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
