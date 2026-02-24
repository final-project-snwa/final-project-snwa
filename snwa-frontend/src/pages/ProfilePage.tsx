import { useEffect, useState, useRef } from 'react';
import { useNavigate } from 'react-router';
import { useAuth } from '../contexts/AuthContext';
import Header from '../components/Header';
import { User, Camera, Lock, LogOut, AlertTriangle, Check,  Calendar, Shield, Mail, Hash } from 'lucide-react';

interface UserProfile {
    id: number;
    email: string;
    nickname: string | null;
    introduction: string | null;
    phoneNumber: string | null;
    profileImageUrl: string | null;
    discordWebhookUrl: string | null;
    status: string;
    role: string;
    createdDate: string;
    updatedDate: string;
}

function getAuthHeader(): Record<string, string> | null {
    const token = sessionStorage.getItem('snwa_token');
    if (!token) return null;
    return { Authorization: `Bearer ${token}` };
}

export default function ProfilePage() {
    const { user, logout } = useAuth();
    const navigate = useNavigate();
    const fileInputRef = useRef<HTMLInputElement>(null);

    // 프로필 상태
    const [profile, setProfile] = useState<UserProfile | null>(null);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [saved, setSaved] = useState(false);

    // 폼 상태
    const [nickname, setNickname] = useState('');
    const [introduction, setIntroduction] = useState('');
    const [phoneNumber, setPhoneNumber] = useState('');
    const [discordWebhookUrl, setDiscordWebhookUrl] = useState('');
    const [profileImageUrl, setProfileImageUrl] = useState<string | null>(null);

    // 비밀번호 변경 상태
    const [showPasswordForm, setShowPasswordForm] = useState(false);
    const [currentPassword, setCurrentPassword] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [passwordError, setPasswordError] = useState('');
    const [passwordSuccess, setPasswordSuccess] = useState(false);

    // 회원탈퇴 상태
    const [showWithdrawModal, setShowWithdrawModal] = useState(false);
    const [withdrawing, setWithdrawing] = useState(false);

    // 이미지 업로드 상태
    const [uploading, setUploading] = useState(false);

    useEffect(() => {
        if (!user) {
            navigate('/login');
            return;
        }
        fetchProfile();
    }, [user, navigate]);

    const fetchProfile = async () => {
        const auth = getAuthHeader();
        if (!auth) return;

        try {
            const res = await fetch('/api/users/profile', { headers: auth });
            if (res.ok) {
                const data: UserProfile = await res.json();
                setProfile(data);
                setNickname(data.nickname || '');
                setIntroduction(data.introduction || '');
                setPhoneNumber(data.phoneNumber || '');
                setDiscordWebhookUrl(data.discordWebhookUrl || '');
                setProfileImageUrl(data.profileImageUrl);
            }
        } catch (error) {
            console.error('프로필 조회 실패:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleSaveProfile = async () => {
        const auth = getAuthHeader();
        if (!auth) return;

        setSaving(true);
        try {
            const res = await fetch('/api/users/profile', {
                method: 'PATCH',
                headers: { ...auth, 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    nickname: nickname || null,
                    introduction: introduction || null,
                    phoneNumber: phoneNumber || null,
                    discordWebhookUrl: discordWebhookUrl || null,
                    profileImageUrl: profileImageUrl,
                }),
            });

            if (res.ok) {
                const updatedProfile: UserProfile = await res.json();
                setProfile(updatedProfile);
                setSaved(true);
                setTimeout(() => setSaved(false), 2000);
            }
        } catch (error) {
            console.error('프로필 저장 실패:', error);
        } finally {
            setSaving(false);
        }
    };

    const handleImageClick = () => {
        fileInputRef.current?.click();
    };

    const handleImageUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (!file) return;

        const auth = getAuthHeader();
        if (!auth) return;

        setUploading(true);
        try {
            // 1. Presigned URL 요청
            const presignedRes = await fetch('/api/users/profile/presigned-url', {
                method: 'POST',
                headers: { ...auth, 'Content-Type': 'application/json' },
                body: JSON.stringify({ contentType: file.type }),
            });

            if (!presignedRes.ok) {
                const errText = await presignedRes.text();
                throw new Error(`Presigned URL 발급 실패 (${presignedRes.status})`);
            }

            const { presignedUrl, imageUrl } = await presignedRes.json();

            // 2. S3에 이미지 업로드
            const uploadRes = await fetch(presignedUrl, {
                method: 'PUT',
                headers: { 'Content-Type': file.type },
                body: file,
            });

            if (!uploadRes.ok) throw new Error(`S3 업로드 실패 (${uploadRes.status})`);

            // 3. 이미지 URL 상태 업데이트
            setProfileImageUrl(imageUrl);
        } catch (error: any) {
            console.error('이미지 업로드 상세 에러:', error);
            alert(`업로드 에러 발생: ${error.message}`);
        } finally {
            setUploading(false);
        }
    };

    const handlePasswordChange = async () => {
        setPasswordError('');
        setPasswordSuccess(false);

        if (newPassword !== confirmPassword) {
            setPasswordError('새 비밀번호가 일치하지 않습니다.');
            return;
        }

        if (newPassword.length < 8) {
            setPasswordError('비밀번호는 8자 이상이어야 합니다.');
            return;
        }

        const auth = getAuthHeader();
        if (!auth) return;

        try {
            const res = await fetch('/api/users/profile/password', {
                method: 'PATCH',
                headers: { ...auth, 'Content-Type': 'application/json' },
                body: JSON.stringify({ currentPassword, newPassword }),
            });

            if (res.ok) {
                setPasswordSuccess(true);
                setCurrentPassword('');
                setNewPassword('');
                setConfirmPassword('');
                setTimeout(() => {
                    setShowPasswordForm(false);
                    setPasswordSuccess(false);
                }, 2000);
            } else {
                const error = await res.json();
                setPasswordError(error.message || '비밀번호 변경에 실패했습니다.');
            }
        } catch (error) {
            setPasswordError('비밀번호 변경 중 오류가 발생했습니다.');
        }
    };

    const handleWithdraw = async () => {
        const auth = getAuthHeader();
        if (!auth) return;

        setWithdrawing(true);
        try {
            const res = await fetch('/api/users/profile/withdraw', {
                method: 'DELETE',
                headers: auth,
            });

            if (res.ok) {
                logout();
                navigate('/');
            } else {
                alert('회원탈퇴에 실패했습니다.');
            }
        } catch (error) {
            console.error('회원탈퇴 실패:', error);
            alert('회원탈퇴 중 오류가 발생했습니다.');
        } finally {
            setWithdrawing(false);
            setShowWithdrawModal(false);
        }
    };

    if (!user || loading) {
        return (
            <div className="min-h-screen bg-gray-50">
                <Header />
                <main className="max-w-4xl mx-auto px-4 py-8">
                    <div className="animate-pulse">
                        <div className="h-8 bg-gray-200 rounded w-1/4 mb-8"></div>
                        <div className="bg-white rounded-lg p-6 space-y-4">
                            <div className="h-24 w-24 bg-gray-200 rounded-full mx-auto"></div>
                            <div className="h-10 bg-gray-200 rounded"></div>
                            <div className="h-10 bg-gray-200 rounded"></div>
                        </div>
                    </div>
                </main>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gray-50">
            <Header />

            <main className="max-w-4xl mx-auto px-4 py-8">
                <h1 className="text-3xl font-bold text-gray-900 mb-8">프로필 설정</h1>

                {/* 계정 정보 섹션 */}
                <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
                    <h2 className="text-lg font-bold text-gray-900 mb-4">계정 정보</h2>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
                            <Hash className="w-5 h-5 text-gray-400" />
                            <div>
                                <p className="text-xs text-gray-500">회원 ID</p>
                                <p className="text-sm font-medium text-gray-900">#{profile?.id}</p>
                            </div>
                        </div>

                        <div className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
                            <Mail className="w-5 h-5 text-gray-400" />
                            <div>
                                <p className="text-xs text-gray-500">이메일</p>
                                <p className="text-sm font-medium text-gray-900">{profile?.email}</p>
                            </div>
                        </div>

                        <div className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
                            <Shield className="w-5 h-5 text-gray-400" />
                            <div>
                                <p className="text-xs text-gray-500">계정 상태</p>
                                <p className="text-sm font-medium">
                                    <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${profile?.status === 'ACTIVE'
                                        ? 'bg-green-100 text-green-700'
                                        : profile?.status === 'SUSPENDED'
                                            ? 'bg-red-100 text-red-700'
                                            : 'bg-gray-100 text-gray-700'
                                        }`}>
                                        {profile?.status === 'ACTIVE' ? '활성' :
                                            profile?.status === 'SUSPENDED' ? '정지됨' :
                                                profile?.status === 'WITHDRAWN' ? '탈퇴' : profile?.status}
                                    </span>
                                </p>
                            </div>
                        </div>

                        <div className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
                            <User className="w-5 h-5 text-gray-400" />
                            <div>
                                <p className="text-xs text-gray-500">회원 등급</p>
                                <p className="text-sm font-medium">
                                    <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${profile?.role === 'ADMIN'
                                        ? 'bg-purple-100 text-purple-700'
                                        : 'bg-blue-100 text-blue-700'
                                        }`}>
                                        {profile?.role === 'ADMIN' ? '관리자' : '일반회원'}
                                    </span>
                                </p>
                            </div>
                        </div>

                        <div className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
                            <Calendar className="w-5 h-5 text-gray-400" />
                            <div>
                                <p className="text-xs text-gray-500">가입일</p>
                                <p className="text-sm font-medium text-gray-900">
                                    {profile?.createdDate
                                        ? new Date(profile.createdDate).toLocaleDateString('ko-KR', {
                                            year: 'numeric',
                                            month: 'long',
                                            day: 'numeric'
                                        })
                                        : '-'}
                                </p>
                            </div>
                        </div>

                        <div className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
                            <Calendar className="w-5 h-5 text-gray-400" />
                            <div>
                                <p className="text-xs text-gray-500">마지막 수정일</p>
                                <p className="text-sm font-medium text-gray-900">
                                    {profile?.updatedDate
                                        ? new Date(profile.updatedDate).toLocaleDateString('ko-KR', {
                                            year: 'numeric',
                                            month: 'long',
                                            day: 'numeric'
                                        })
                                        : '-'}
                                </p>
                            </div>
                        </div>
                    </div>
                </div>

                {/* 프로필 이미지 섹션 */}
                <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
                    <div className="flex flex-col items-center justify-center">
                        <div
                            className="relative cursor-pointer group"
                            onClick={handleImageClick}
                        >
                            <div className="w-32 h-32 rounded-full overflow-hidden bg-gray-100 border-4 border-gray-200">
                                {profileImageUrl ? (
                                    <img
                                        src={profileImageUrl}
                                        alt="프로필"
                                        className="w-full h-full object-cover"
                                    />
                                ) : (
                                    <div className="w-full h-full flex items-center justify-center">
                                        <User className="w-16 h-16 text-gray-400" />
                                    </div>
                                )}
                            </div>
                            <div className="absolute inset-0 rounded-full bg-black bg-opacity-50 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
                                {uploading ? (
                                    <div className="w-8 h-8 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                                ) : (
                                    <Camera className="w-8 h-8 text-white" />
                                )}
                            </div>
                        </div>
                        <input
                            ref={fileInputRef}
                            type="file"
                            accept="image/*"
                            onChange={handleImageUpload}
                            className="hidden"
                        />
                        <p className="mt-3 text-sm text-gray-500 text-center">이미지 변경은 클릭</p>
                    </div>
                </div>

                {/* 프로필 정보 섹션 */}
                <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
                    <h2 className="text-lg font-bold text-gray-900 mb-4">기본 정보</h2>

                    <div className="space-y-4">
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">이메일</label>
                            <input
                                type="email"
                                value={profile?.email || ''}
                                disabled
                                className="w-full px-4 py-3 rounded-lg border border-gray-200 bg-gray-50 text-gray-500"
                            />
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">닉네임</label>
                            <input
                                type="text"
                                value={nickname}
                                onChange={(e) => setNickname(e.target.value)}
                                placeholder="닉네임을 입력하세요"
                                className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:border-gray-900 focus:outline-none transition-colors"
                            />
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">소개</label>
                            <textarea
                                value={introduction}
                                onChange={(e) => setIntroduction(e.target.value)}
                                placeholder="자기소개를 입력하세요"
                                rows={3}
                                className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:border-gray-900 focus:outline-none transition-colors resize-none"
                            />
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">전화번호</label>
                            <input
                                type="tel"
                                value={phoneNumber}
                                onChange={(e) => setPhoneNumber(e.target.value)}
                                placeholder="010-0000-0000"
                                className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:border-gray-900 focus:outline-none transition-colors"
                            />
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                디스코드 웹후크 URL
                                <span className="ml-2 text-xs text-gray-400 font-normal">(선택사항)</span>
                            </label>
                            <input
                                type="url"
                                value={discordWebhookUrl}
                                onChange={(e) => setDiscordWebhookUrl(e.target.value)}
                                placeholder="https://discord.com/api/webhooks/..."
                                className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:border-gray-900 focus:outline-none transition-colors"
                            />
                            <p className="mt-1 text-xs text-gray-500">
                                * 디스코드 채널 설정 &gt; 연동 &gt; 웹후크 만들기에서 URL을 복사하여 입력하세요.
                                <br />
                                * 관심사에 맞는 새 기사가 등록되면 해당 채널로 알림이 전송됩니다.
                            </p>
                        </div>
                    </div>

                    <button
                        onClick={handleSaveProfile}
                        disabled={saving}
                        className="mt-6 w-full bg-gray-900 text-white py-3 rounded-lg font-medium hover:bg-gray-800 transition-colors disabled:bg-gray-400 flex items-center justify-center gap-2"
                    >
                        {saving ? (
                            <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                        ) : saved ? (
                            <>
                                <Check className="w-5 h-5" />
                                저장되었습니다
                            </>
                        ) : (
                            '변경사항 저장'
                        )}
                    </button>
                </div>

                {/* 계정 관리 섹션 */}
                <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
                    <h2 className="text-lg font-bold text-gray-900 mb-4">계정 관리</h2>

                    <div className="space-y-4">
                        {/* 비밀번호 변경 */}
                        <button
                            onClick={() => setShowPasswordForm(!showPasswordForm)}
                            className="w-full flex items-center justify-between px-4 py-3 rounded-lg border border-gray-200 hover:bg-gray-50 transition-colors"
                        >
                            <div className="flex items-center gap-3">
                                <Lock className="w-5 h-5 text-gray-500" />
                                <span className="text-gray-700">비밀번호 변경</span>
                            </div>
                        </button>

                        {showPasswordForm && (
                            <div className="space-y-4 p-4 bg-gray-50 rounded-lg">
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">현재 비밀번호</label>
                                    <input
                                        type="password"
                                        value={currentPassword}
                                        onChange={(e) => setCurrentPassword(e.target.value)}
                                        className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:border-gray-900 focus:outline-none transition-colors bg-white"
                                    />
                                </div>

                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">새 비밀번호</label>
                                    <input
                                        type="password"
                                        value={newPassword}
                                        onChange={(e) => setNewPassword(e.target.value)}
                                        className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:border-gray-900 focus:outline-none transition-colors bg-white"
                                    />
                                </div>

                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">새 비밀번호 확인</label>
                                    <input
                                        type="password"
                                        value={confirmPassword}
                                        onChange={(e) => setConfirmPassword(e.target.value)}
                                        className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:border-gray-900 focus:outline-none transition-colors bg-white"
                                    />
                                </div>

                                {passwordError && (
                                    <p className="text-sm text-red-600">{passwordError}</p>
                                )}

                                {passwordSuccess && (
                                    <p className="text-sm text-green-600">비밀번호가 성공적으로 변경되었습니다.</p>
                                )}

                                <button
                                    onClick={handlePasswordChange}
                                    className="w-full bg-gray-900 text-white py-3 rounded-lg font-medium hover:bg-gray-800 transition-colors"
                                >
                                    비밀번호 변경
                                </button>
                            </div>
                        )}

                        {/* 로그아웃 */}
                        <button
                            onClick={() => {
                                logout();
                                navigate('/');
                            }}
                            className="w-full flex items-center justify-between px-4 py-3 rounded-lg border border-gray-200 hover:bg-gray-50 transition-colors"
                        >
                            <div className="flex items-center gap-3">
                                <LogOut className="w-5 h-5 text-gray-500" />
                                <span className="text-gray-700">로그아웃</span>
                            </div>
                        </button>

                        {/* 회원탈퇴 */}
                        <button
                            onClick={() => setShowWithdrawModal(true)}
                            className="w-full flex items-center justify-between px-4 py-3 rounded-lg border border-red-200 bg-red-50 hover:bg-red-100 transition-colors"
                        >
                            <div className="flex items-center gap-3">
                                <AlertTriangle className="w-5 h-5 text-red-500" />
                                <span className="text-red-700">회원탈퇴</span>
                            </div>
                        </button>
                    </div>
                </div>
            </main>

            {/* 회원탈퇴 모달 */}
            {showWithdrawModal && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 px-4">
                    <div className="bg-white rounded-lg max-w-md w-full p-6">
                        <div className="flex items-center gap-3 mb-4">
                            <div className="w-12 h-12 rounded-full bg-red-100 flex items-center justify-center">
                                <AlertTriangle className="w-6 h-6 text-red-600" />
                            </div>
                            <div>
                                <h3 className="text-lg font-bold text-gray-900">회원탈퇴</h3>
                                <p className="text-sm text-gray-500">정말 탈퇴하시겠습니까?</p>
                            </div>
                        </div>

                        <p className="text-sm text-gray-600 mb-6">
                            회원탈퇴 시 모든 데이터가 삭제되며 복구할 수 없습니다.
                        </p>

                        <div className="flex gap-3">
                            <button
                                onClick={() => setShowWithdrawModal(false)}
                                className="flex-1 px-4 py-3 rounded-lg border border-gray-200 font-medium hover:bg-gray-50 transition-colors"
                            >
                                취소
                            </button>
                            <button
                                onClick={handleWithdraw}
                                disabled={withdrawing}
                                className="flex-1 px-4 py-3 rounded-lg bg-red-600 text-white font-medium hover:bg-red-700 transition-colors disabled:bg-red-400 flex items-center justify-center"
                            >
                                {withdrawing ? (
                                    <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                                ) : (
                                    '탈퇴하기'
                                )}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
