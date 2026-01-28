import { useState } from 'react';

const API_BASE_URL = '/api/auth';

export default function AuthTestPage() {
  const [signupData, setSignupData] = useState({
    email: '',
    password: '',
    nickname: '',
  });
  const [loginData, setLoginData] = useState({
    email: '',
    password: '',
  });
  const [emailToken, setEmailToken] = useState('');
  const [token, setToken] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  // 회원가입
  const handleSignup = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setMessage('');

    try {
      const response = await fetch(`${API_BASE_URL}/signup`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(signupData),
      });

      const data = await response.json();

      if (response.ok) {
        setMessage(`✅ ${data.message}`);
        setSignupData({ email: '', password: '', nickname: '' });
      } else {
        setError(`❌ ${data.message || '회원가입 실패'}`);
      }
    } catch (err) {
      setError(`❌ 서버 연결 실패: ${err instanceof Error ? err.message : '알 수 없는 오류'}`);
    }
  };

  // 이메일 인증
  const handleVerifyEmail = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setMessage('');

    if (!emailToken) {
      setError('인증 토큰을 입력해주세요.');
      return;
    }

    try {
      const response = await fetch(`${API_BASE_URL}/verify-email?token=${emailToken}`, {
        method: 'GET',
      });

      const data = await response.json();

      if (response.ok) {
        setMessage(`✅ ${data.message}`);
        setEmailToken('');
      } else {
        setError(`❌ ${data.message || '이메일 인증 실패'}`);
      }
    } catch (err) {
      setError(`❌ 서버 연결 실패: ${err instanceof Error ? err.message : '알 수 없는 오류'}`);
    }
  };

  // 로그인
  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setMessage('');

    try {
      const response = await fetch(`${API_BASE_URL}/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(loginData),
      });

      const data = await response.json();

      if (response.ok) {
        setToken(data.token);
        setMessage(`✅ ${data.message} - 토큰이 저장되었습니다.`);
        setLoginData({ email: '', password: '' });
      } else {
        setError(`❌ ${data.message || '로그인 실패'}`);
      }
    } catch (err) {
      setError(`❌ 서버 연결 실패: ${err instanceof Error ? err.message : '알 수 없는 오류'}`);
    }
  };

  // 로그아웃
  const handleLogout = async () => {
    setError('');
    setMessage('');

    if (!token) {
      setError('로그인된 토큰이 없습니다.');
      return;
    }

    try {
      const response = await fetch(`${API_BASE_URL}/logout`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });

      const data = await response.json();

      if (response.ok) {
        setMessage(`✅ ${data.message}`);
        setToken('');
      } else {
        setError(`❌ ${data.message || '로그아웃 실패'}`);
      }
    } catch (err) {
      setError(`❌ 서버 연결 실패: ${err instanceof Error ? err.message : '알 수 없는 오류'}`);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4">
      <div className="max-w-4xl mx-auto">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">인증 API 테스트</h1>
          <p className="text-gray-600">회원가입 → 이메일 인증 → 로그인 → 로그아웃 순서로 테스트하세요</p>
        </div>

        {/* 메시지 표시 */}
        {message && (
          <div className="mb-6 p-4 bg-green-50 border border-green-200 rounded-lg text-green-800">
            {message}
          </div>
        )}
        {error && (
          <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg text-red-800">
            {error}
          </div>
        )}

        {/* 현재 토큰 표시 */}
        {token && (
          <div className="mb-6 p-4 bg-blue-50 border border-blue-200 rounded-lg">
            <p className="text-sm font-medium text-blue-900 mb-2">현재 로그인된 토큰:</p>
            <p className="text-xs text-blue-700 break-all font-mono">{token}</p>
          </div>
        )}

        <div className="grid md:grid-cols-2 gap-6">
          {/* 회원가입 */}
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <h2 className="text-xl font-semibold text-gray-900 mb-4">1. 회원가입</h2>
            <form onSubmit={handleSignup} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  이메일 (아이디로 사용)
                </label>
                <input
                  type="email"
                  value={signupData.email}
                  onChange={(e) => setSignupData({ ...signupData, email: e.target.value })}
                  required
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="test@example.com"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  비밀번호 (영문+숫자+특수문자 8자 이상)
                </label>
                <input
                  type="password"
                  value={signupData.password}
                  onChange={(e) => setSignupData({ ...signupData, password: e.target.value })}
                  required
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="Test123!@#"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  닉네임 (1-12자)
                </label>
                <input
                  type="text"
                  value={signupData.nickname}
                  onChange={(e) => setSignupData({ ...signupData, nickname: e.target.value })}
                  required
                  maxLength={12}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="테스트유저"
                />
              </div>
              <button
                type="submit"
                className="w-full bg-blue-600 text-white py-2 rounded-lg font-medium hover:bg-blue-700 transition-colors"
              >
                회원가입
              </button>
            </form>
          </div>

          {/* 이메일 인증 */}
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <h2 className="text-xl font-semibold text-gray-900 mb-4">2. 이메일 인증</h2>
            <form onSubmit={handleVerifyEmail} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  인증 토큰
                </label>
                <input
                  type="text"
                  value={emailToken}
                  onChange={(e) => setEmailToken(e.target.value)}
                  required
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="이메일로 받은 토큰 입력"
                />
                <p className="text-xs text-gray-500 mt-1">
                  회원가입 후 이메일로 받은 링크의 token 파라미터 값을 입력하세요
                </p>
              </div>
              <button
                type="submit"
                className="w-full bg-green-600 text-white py-2 rounded-lg font-medium hover:bg-green-700 transition-colors"
              >
                이메일 인증
              </button>
            </form>
          </div>

          {/* 로그인 */}
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <h2 className="text-xl font-semibold text-gray-900 mb-4">3. 로그인</h2>
            <form onSubmit={handleLogin} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  이메일 (아이디)
                </label>
                <input
                  type="email"
                  value={loginData.email}
                  onChange={(e) => setLoginData({ ...loginData, email: e.target.value })}
                  required
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="test@example.com"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  비밀번호
                </label>
                <input
                  type="password"
                  value={loginData.password}
                  onChange={(e) => setLoginData({ ...loginData, password: e.target.value })}
                  required
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="Test123!@#"
                />
              </div>
              <button
                type="submit"
                className="w-full bg-purple-600 text-white py-2 rounded-lg font-medium hover:bg-purple-700 transition-colors"
              >
                로그인
              </button>
            </form>
          </div>

          {/* 로그아웃 */}
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <h2 className="text-xl font-semibold text-gray-900 mb-4">4. 로그아웃</h2>
            <div className="space-y-4">
              <p className="text-sm text-gray-600">
                현재 저장된 토큰으로 로그아웃합니다.
              </p>
              <button
                onClick={handleLogout}
                disabled={!token}
                className="w-full bg-red-600 text-white py-2 rounded-lg font-medium hover:bg-red-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              >
                로그아웃
              </button>
            </div>
          </div>
        </div>

        {/* 사용 가이드 */}
        <div className="mt-8 bg-yellow-50 border border-yellow-200 rounded-lg p-6">
          <h3 className="text-lg font-semibold text-yellow-900 mb-3">📋 테스트 가이드</h3>
          <ol className="list-decimal list-inside space-y-2 text-sm text-yellow-800">
            <li>회원가입: 이메일(아이디로 사용), 비밀번호(영문+숫자+특수문자 8자 이상), 닉네임 입력</li>
            <li>이메일 인증: 회원가입 후 이메일로 받은 인증 링크의 token 값을 입력하거나, DB에서 email_verification_token 테이블의 token 값을 확인</li>
            <li>로그인: 회원가입한 이메일과 비밀번호로 로그인 (이메일 인증 완료 후 가능)</li>
            <li>로그아웃: 로그인 후 받은 토큰으로 로그아웃</li>
          </ol>
        </div>
      </div>
    </div>
  );
}
