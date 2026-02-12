# SNWA API 명세서

## 1. 인증 (Authentication)

### 1.1 회원가입
- **Endpoint:** `/api/auth/signup`
- **Method:** `POST`
- **Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "nickname": "킬러슬라이드"
}
```
- **Description:** 새로운 사용자를 등록하고 이메일 인증 메일을 발송합니다.

### 1.2 로그인
- **Endpoint:** `/api/auth/login`
- **Method:** `POST`
- **Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```
- **Response Body:**
```json
{
  "token": "eyJhbG...",
  "message": "로그인 성공",
  "attendanceRewardGiven": false
}
```
- **Description:** 이메일과 비밀번호로 인증하고 JWT 토큰을 발급받습니다.

### 1.3 로그아웃
- **Endpoint:** `/api/auth/logout`
- **Method:** `POST`
- **Header:** `Authorization: Bearer <token>`
- **Description:** 현재 토큰을 무효화하고 로그아웃 처리합니다.

---

## 2. 뉴스 기사 (Articles)

### 2.1 기사 목록 조회
- **Endpoint:** `/api/articles`
- **Method:** `GET`
- **Query Params:**
    - `categoryId`: (Optional) 카테고리 필터
    - `publisherName`: (Optional) 출판사 필터
    - `page`: 페이지 번호
    - `size`: 페이지 크기 (기본 10)
- **Description:** 필터링 및 페이징된 뉴스 기사 목록을 조회합니다.

### 2.2 기사 상세 조회
- **Endpoint:** `/api/articles/{id}`
- **Method:** `GET`
- **Query Params:** `recordView` (boolean, 조회수 증가 여부)
- **Description:** 특정 기사의 상세 내용을 조회합니다.

### 2.3 기사 검색
- **Endpoint:** `/api/articles/search`
- **Method:** `GET`
- **Query Params:** `keyword` (검색어)
- **Description:** 제목 또는 내용에 키워드가 포함된 기사를 검색합니다.

---

## 3. 댓글 (Comments)

### 3.1 댓글 작성
- **Endpoint:** `/api/articles/{articleId}/comments`
- **Method:** `POST`
- **Request Body:**
```json
{
  "content": "좋은 정보 감사합니다! 👍"
}
```
- **Description:** 특정 기사에 댓글을 작성합니다.

### 3.2 댓글 목록 조회
- **Endpoint:** `/api/articles/{articleId}/comments`
- **Method:** `GET`
- **Query Params:** `page`, `size`
- **Description:** 기사의 댓글 목록을 페이징하여 조회합니다.

### 3.3 댓글 수정
- **Endpoint:** `/api/comments/{commentId}`
- **Method:** `PUT`
- **Request Body:**
```json
{
  "content": "수정된 댓글 내용입니다."
}
```
- **Description:** 본인이 작성한 댓글을 수정합니다.

### 3.4 댓글 삭제
- **Endpoint:** `/api/comments/{commentId}`
- **Method:** `DELETE`
- **Description:** 본인이 작성한 댓글을 삭제합니다.

---

## 4. 관심사 및 구독 (Interests)

### 4.1 구독 대상 검색
- **Endpoint:** `/api/targets`
- **Method:** `GET`
- **Query Params:** `keyword`
- **Description:** 구독할 수 있는 토픽(리그, 팀 등)을 검색합니다.

### 4.2 구독 토글
- **Endpoint:** `/api/subscriptions/{targetId}`
- **Method:** `POST`
- **Description:** 특정 토픽에 대한 구독 상태를 켜거나 끕니다. (Request Body 없음)

### 4.3 내 구독 목록 조회
- **Endpoint:** `/api/subscriptions/me`
- **Method:** `GET`
- **Description:** 현재 사용자가 구독 중인 목록을 조회합니다.

---

## 5. 결제 (Payments)

### 5.1 결제 승인 요청
- **Endpoint:** `/api/payments/confirm`
- **Method:** `POST`
- **Request Body:**
```json
{
  "paymentKey": "tgen_20240501123456",
  "orderId": "order_uuid_1234",
  "amount": 15000
}
```
- **Description:** 결제 게이트웨이(Toss) 승인 후 서버에 검증 및 처리를 요청합니다.

### 5.2 결제 취소
- **Endpoint:** `/api/payments/{paymentKey}/cancel`
- **Method:** `POST`
- **Request Body:**
```json
{
  "cancelReason": "단순 변심",
  "cancelAmount": 15000
}
```
- **Description:** 완료된 결제를 취소합니다. `cancelAmount`가 null이면 전액 취소됩니다.

### 5.3 내 결제 내역 조회
- **Endpoint:** `/api/payments/users/{userId}`
- **Method:** `GET`
- **Description:** 사용자의 결제 이력을 조회합니다.

---

## 6. 알림 (Notifications)

### 6.1 알림 목록 조회
- **Endpoint:** `/api/notifications`
- **Method:** `GET`
- **Description:** 사용자의 모든 알림을 페이징하여 조회합니다.

### 6.2 읽지 않은 알림 뱃지
- **Endpoint:** `/api/notifications/unread/count`
- **Method:** `GET`
- **Description:** 읽지 않은 알림의 개수를 반환합니다.

### 6.3 알림 읽음 처리
- **Endpoint:** `/api/notifications/{notificationId}/read`
- **Method:** `PATCH`
- **Description:** 특정 알림을 읽음 상태로 변경합니다. (Request Body 없음)

---

## 7. 관리자 (Admin)

### 7.1 크롤링 작업 제어
- **Endpoint:** `/api/admin/crawler/jobs` (GET: 목록)
- **Endpoint:** `/api/admin/crawler/jobs/{jobId}/run` (POST: 수동 실행)
- **Endpoint:** `/api/admin/crawler/jobs/{jobId}` (PATCH: 활성 상태 변경)
- **Method:** `PATCH`
- **Request Body:**
```json
{
  "isActive": true,
  "cronExpression": "0 0/30 * * * ?"
}
```
- **Description:** 크롤링 스케줄러의 활성 상태나 실행 주기를 변경합니다.

### 7.2 사용자 전체 조회
- **Endpoint:** `/api/admin/users`
- **Method:** `GET`
- **Description:** 전체 회원 목록을 조회합니다.

### 7.3 사용자 정보 수정 (관리자)
- **Endpoint:** `/api/admin/users/{userId}`
- **Method:** `PATCH`
- **Request Body:**
```json
{
  "nickname": "변경된닉네임",
  "status": "ACTIVE",
  "emailVerified": true,
  "phoneNumber": "010-1234-5678",
  "introduction": "관리자에 의한 수정",
  "profileImageUrl": "https://..."
}
```
- **Description:** 관리자가 회원의 정보를 강제로 수정하거나 상태(정지 등)를 변경합니다.

### 7.4 사용자 삭제 (관리자)
- **Endpoint:** `/api/admin/users/{userId}`
- **Method:** `DELETE`
- **Description:** 회원을 탈퇴 처리(Soft Delete) 합니다.

### 7.5 기사 관리
- **Endpoint:** `/api/admin/articles` (GET: 전체 기사 조회)
- **Endpoint:** `/api/admin/articles/{articleId}` (DELETE: 기사 삭제)
