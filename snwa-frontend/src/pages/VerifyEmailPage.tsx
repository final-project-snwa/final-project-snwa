import { useState, useEffect } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router';

const API_BASE_URL = '/api/auth';

export default function VerifyEmailPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(true);
  const token = searchParams.get('token');

  useEffect(() => {
    if (!token) {
      setError('유효하지 않은 링크입니다.');
      setLoading(false);
      return;
    }

    // 이메일 인증 API 호출
    const verifyEmail = async () => {
      try {
        const response = await fetch(`${API_BASE_URL}/verify-email?token=${token}`, {
          method: 'GET',
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
          setMessage(data.message || '이메일 인증이 완료되었습니다.');
          // 3초 후 로그인 페이지로 이동
          setTimeout(() => {
            navigate('/login');
          }, 3000);
        } else {
          setError(data.message || '이메일 인증에 실패했습니다.');
        }
      } catch (err) {
        setError(`서버 연결 실패: ${err instanceof Error ? err.message : '알 수 없는 오류'}`);
      } finally {
        setLoading(false);
      }
    };

    verifyEmail();
  }, [token, navigate]);

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
      <div className="w-full max-w-md">
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-8">
          {/* Logo */}
          <div className="text-center mb-8">
            <Link to="/" className="text-3xl font-bold text-gray-900 tracking-tight">
              SNWA
            </Link>
            <p className="text-sm text-gray-500 mt-2">이메일 인증</p>
          </div>

          {loading ? (
            <div className="text-center py-8">
              <p className="text-gray-600">이메일 인증 중...</p>
            </div>
          ) : (
            <>
              {message && (
                <div className="text-sm text-green-600 bg-green-50 px-3 py-2 rounded mb-4">
                  {message}
                </div>
              )}
              {error && (
                <div className="text-sm text-red-600 bg-red-50 px-3 py-2 rounded mb-4">
                  {error}
                </div>
              )}

              <div className="mt-6 text-center">
                <p className="text-sm text-gray-600">
                  <Link to="/login" className="text-gray-900 font-medium hover:underline">
                    로그인 페이지로 이동
                  </Link>
                </p>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
